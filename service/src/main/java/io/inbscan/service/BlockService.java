package io.inbscan.service;

import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import io.inbscan.chain.InbChainService;
import io.inbscan.constants.InbConstants;
import io.inbscan.dto.*;
import io.inbscan.dto.block.BlockChainDto;
import io.inbscan.dto.block.BlockCriteriaDTO;
import io.inbscan.dto.block.BlockDTO;
import io.inbscan.dto.node.NodeCriteriaDTO;
import io.inbscan.exception.ServiceException;
import io.inbscan.model.tables.pojos.Node;
import io.inbscan.model.tables.records.BlockChainRecord;
import io.inbscan.model.tables.records.BlockRecord;
import io.inbscan.utils.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static io.inbscan.model.tables.Block.BLOCK;
import static io.inbscan.model.tables.BlockChain.BLOCK_CHAIN;
import static io.inbscan.model.tables.Node.NODE;


public class BlockService {

	private DSLContext dslContext;
	private TransactionService txService;
	private InbChainService inbChainService;
	
	@Inject
	public BlockService(DSLContext dslContext, TransactionService txService, InbChainService inbChainService) {
		this.dslContext = dslContext;
		this.txService = txService;
		this.inbChainService= inbChainService;
	}



	/**
	 * Import a new block into db
	 * @param
	 * @throws ServiceException
	 */
	public void importInbBlock(EthBlock.Block block) throws ServiceException {

		long blockNum = block.getNumber().longValue();

//		try {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(block.getTimestamp().longValue()*1000);

		BlockRecord record = new BlockRecord();

//			String parentHash = Sha256Hash.wrap(block.getBlockHeader().getRawData().getParentHash()).toString();

		//Sha256Hash.of(block.getBlockHeader().getRawData().toByteArray()).toString()

		//获取block收益
		List<Object> params = new ArrayList<>();
		params.add("0x"+Integer.toHexString(block.getNumber().intValue()));
		params.add(true);
		JsonParam inbJsonParam = new JsonParam();
		inbJsonParam.setJsonrpc("2.0");
		inbJsonParam.setMethod("eth_getBlockByNumber");
		inbJsonParam.setId(67L);
		inbJsonParam.setParams(params);
		String param = JSONObject.toJSONString(inbJsonParam);
		String result = HttpUtil.doPost(InbConstants.URL, param);
		JSONObject object = (JSONObject)JSONObject.parse(result);
		BigInteger reward = new BigInteger("0");
		if(object!=null) {
			reward = new BigInteger(object.getJSONObject("result").get("reward").toString());
		}

		record.setTxCount(UInteger.valueOf(0));
		record.setWitnessAddress(block.getAuthor());
		record.setNum(ULong.valueOf(block.getNumber()));
//			record.setHash(Sha256Hash.wrap(Sha256Hash.of(block.getBlockHeader().getRawData().toByteArray()).getBytes()).toString());
		record.setHash(block.getHash());
//			record.setWitnessAddress(block.getAuthor());
		record.setParentHash(block.getParentHash());
		record.setTimestamp(Timestamp.valueOf(simpleDateFormat.format(date)));
		record.setSize(UInteger.valueOf(block.getSize().intValue()));
		record.setTxCount(UInteger.valueOf(block.getTransactions().size()));
		record.setReward(reward.doubleValue());


		//TODO 将区块收益存入db


		//store block
		record.attach(this.dslContext.configuration());
		record.store();

		if (blockNum>0) {
			this.dslContext.update(BLOCK).set(BLOCK.HASH,block.getParentHash()).where(BLOCK.NUM.eq(ULong.valueOf(blockNum-1))).execute();
		}

		/**
		 * 更新block chain数据
		 * 地址在account中更新
		 */

		List<EthBlock.Block> blocks = this.getBlocks(blockNum-1,blockNum+1);

		//当前tps
		Integer currentTps = 0;
		if(blocks.size()==2) {
			double transCount = block.getTransactions().size();
			double blockTime = Numeric.decodeQuantity(blocks.get(1).getTimestampRaw()).subtract(Numeric.decodeQuantity(blocks.get(0).getTimestampRaw())).doubleValue();
			double tps = transCount/blockTime;
			BigDecimal trans = new BigDecimal(tps);
			currentTps = trans.setScale(2,BigDecimal.ROUND_HALF_UP).intValue();
		}

		//不可逆块高度
		BigInteger irreversibleBlockNum = inbChainService.getConfirmedBlockNumber();

		//INB总供应量
		BigInteger totalINB = inbChainService.getAccountInfo(InbConstants.INBTOTALSUPPLY).getJSONObject("result").getBigInteger("Balance");

		//当前为net抵押INB数量
		BigInteger mortgagteINB = inbChainService.getAccountInfo(InbConstants.MORTGAGENETINB).getJSONObject("result").getBigInteger("Balance");
		if(mortgagteINB == null){
			mortgagteINB = new BigInteger("0");
		}



		BlockChainRecord blockChainRecord = this.dslContext.select(BLOCK_CHAIN.ID).from(BLOCK_CHAIN).fetchOneInto(BlockChainRecord.class);
		if(blockChainRecord == null){
			blockChainRecord = this.dslContext.insertInto(BLOCK_CHAIN)
					.set(BLOCK_CHAIN.LATEST_BLOCK_NUM,ULong.valueOf(blockNum))
					.set(BLOCK_CHAIN.IRREVERSIBLE_BLOCK_NUM,ULong.valueOf(irreversibleBlockNum))
					.set(BLOCK_CHAIN.CURRENT_TPS,currentTps)
//					.set(BLOCK_CHAIN.ADDRESS_NUM,block.getTransactions())
//					.set(BLOCK_CHAIN.HIGHEST_TPS,currentTps)
					.set(BLOCK_CHAIN.INB_TOTAL_SUPPLY,totalINB.doubleValue())
					.set(BLOCK_CHAIN.MORTGAGE_NET_INB,mortgagteINB.doubleValue())
					.returning(BLOCK_CHAIN.ID)
					.fetchOne();


		}else {
			BlockChainRecord blockChain = this.dslContext.select().from(BLOCK_CHAIN).where(BLOCK_CHAIN.ID.eq(blockChainRecord.getId())).fetchOneInto(BlockChainRecord.class);

			Integer highestTps = 0;
			if(blockChain.getHighestTps()<currentTps){
				highestTps = currentTps;
			}else if(blockChain.getHighestTps()>=currentTps){
				highestTps = blockChain.getHighestTps();
			}

			this.dslContext.update(BLOCK_CHAIN)
					.set(BLOCK_CHAIN.LATEST_BLOCK_NUM,ULong.valueOf(blockNum))
					.set(BLOCK_CHAIN.IRREVERSIBLE_BLOCK_NUM,ULong.valueOf(irreversibleBlockNum))
					.set(BLOCK_CHAIN.CURRENT_TPS,currentTps)
//					.set(BLOCK_CHAIN.ADDRESS_NUM,block.getTransactions())
					.set(BLOCK_CHAIN.HIGHEST_TPS,highestTps)
					.set(BLOCK_CHAIN.INB_TOTAL_SUPPLY,totalINB.doubleValue())
					.set(BLOCK_CHAIN.MORTGAGE_NET_INB,mortgagteINB.doubleValue())
					.where(BLOCK_CHAIN.ID.eq(blockChainRecord.getId()))
					.execute();
		}

		List<EthBlock.TransactionResult> transactionResults = block.getTransactions();

		if(!transactionResults.isEmpty()){
			for(EthBlock.TransactionResult transactionResult:transactionResults){
				EthBlock.TransactionObject transaction = (EthBlock.TransactionObject) transactionResult.get();
				this.txService.saveTransaction(transaction, record);
			}
		}

	}

	public Integer currentTPS(){

		return 0;
	}


	public List<EthBlock.Block> getBlocks(long start, long stop){

		Web3j web3j = Web3j.build(new HttpService(InbConstants.URL));

		List<EthBlock.Block> blocks = new ArrayList<>();
		try {

			for (int i = (int) start; i <(int) stop; i++) {
				DefaultBlockParameter defaultBlockParameter = new DefaultBlockParameterNumber(i);
				Request<?, EthBlock> blockRequest = web3j.ethGetBlockByNumber(defaultBlockParameter, true);
				EthBlock ethBlock = blockRequest.send();
				blocks.add(ethBlock.getBlock());
			}
		}catch (IOException e){
			e.printStackTrace();
		}

		return blocks;
	}

	public Long getlastNumber(){

		Web3j web3j = Web3j.build(new HttpService(InbConstants.URL));
		Long blockNumber = 0L;
		try {
			EthBlockNumber ethBlockNumber = web3j.ethBlockNumber().send();
			blockNumber = ethBlockNumber.getBlockNumber().longValue();
		}catch (IOException e){
			e.printStackTrace();
		}

		return blockNumber;
	}

	public BlockDTO getLastBlock() {

		return this.dslContext.select(BLOCK.NUM,BLOCK.HASH,BLOCK.TIMESTAMP,BLOCK.TX_COUNT,BLOCK.SIZE,BLOCK.PARENT_HASH,BLOCK.WITNESS_ADDRESS)
				.from(BLOCK).orderBy(BLOCK.NUM.desc()).limit(1).fetchOneInto(BlockDTO.class);

	}

	public List<BlockDTO> getLastBlocks(){
		
		 List<BlockDTO> result = this.dslContext.select(BLOCK.NUM,BLOCK.HASH,BLOCK.WITNESS_ADDRESS)
		.from(BLOCK).orderBy(BLOCK.TIMESTAMP.desc()).limit(10).fetchInto(BlockDTO.class);
		
		
		return result;
	}

	
	public BlockDTO getBlockByNum(long num) {
		
//		List<Field<?>> fields = new ArrayList<>(Arrays.asList(BLOCK.fields()));
//		fields.add(DSL.select(DSL.max(BLOCK.NUM)).from(BLOCK).asField("maxNum"));
		
		BlockDTO block=this.dslContext.select(BLOCK.ID,BLOCK.NUM,BLOCK.HASH,BLOCK.TIMESTAMP,BLOCK.TX_COUNT,BLOCK.SIZE,BLOCK.PARENT_HASH,BLOCK.REWARD,BLOCK.TX_COUNT).from(BLOCK).where(BLOCK.NUM.eq(ULong.valueOf(num))).fetchOneInto(BlockDTO.class);

		BigDecimal reward = new BigDecimal(block.getReward()/Math.pow(10,18));
		block.setReward(reward.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
		block.setTimestamp(block.getTimestamp().substring(0,block.getTimestamp().indexOf(".")));
		
		return block;
	}

	public BlockDTO getBlockById(long blockId) {

		List<Field<?>> fields = new ArrayList<>(Arrays.asList(BLOCK.fields()));
		fields.add(DSL.select(DSL.max(BLOCK.NUM)).from(BLOCK).asField("maxNum"));

		BlockDTO block=this.dslContext.select(fields).from(BLOCK).where(BLOCK.ID.eq(ULong.valueOf(blockId))).fetchOneInto(BlockDTO.class);

		if (block!=null) {
			setReward(Arrays.asList(block));
		}

		block.setTimestamp(block.getTimestamp().substring(0,block.getTimestamp().indexOf(".")));

		return block;
	}


	public BlockDTO getBlockByHash(String hash) {

		List<Field<?>> fields = new ArrayList<>(Arrays.asList(BLOCK.fields()));
		fields.add(DSL.select(DSL.max(BLOCK.NUM)).from(BLOCK).asField("maxNum"));
		
		BlockDTO block=this.dslContext.select(fields).from(BLOCK).where(BLOCK.HASH.eq(hash)).fetchOneInto(BlockDTO.class);
		
		if (block!=null) {
			setReward(Arrays.asList(block));
		}
		block.setTimestamp(block.getTimestamp().substring(0,block.getTimestamp().indexOf(".")));
		
		return block;
	}

	public BlockDTO getBlockByParentHash(String hash) {

		List<Field<?>> fields = new ArrayList<>(Arrays.asList(BLOCK.fields()));
		fields.add(DSL.select(DSL.max(BLOCK.NUM)).from(BLOCK).asField("maxNum"));
		
		BlockDTO block=this.dslContext.select(fields).from(BLOCK).where(BLOCK.PARENT_HASH.eq(hash)).fetchOneInto(BlockDTO.class);
		
		if (block!=null) {
			setReward(Arrays.asList(block));			
		}

		
		return block;
	}
	
	public ListModel<BlockDTO, BlockCriteriaDTO> listBlocks(BlockCriteriaDTO criteria){
		
		ArrayList<Condition> conditions = new ArrayList<>();
		
		 SelectJoinStep<?> listQuery = this.dslContext.select(BLOCK.ID,BLOCK.NUM,BLOCK.HASH,BLOCK.SIZE,BLOCK.CONFIRMED,BLOCK.TIMESTAMP,BLOCK.WITNESS_ADDRESS,BLOCK.TX_COUNT,BLOCK.REWARD,BLOCK.WITNESS_ADDRESS.as("witness"))
		.from(BLOCK);
		
		
		SelectJoinStep<Record1<Integer>> countQuery = dslContext.select(DSL.count())
		.from(BLOCK);
		
		if (StringUtils.isNotBlank(criteria.getProducedBy())) {
			conditions.add(BLOCK.WITNESS_ADDRESS.eq(criteria.getProducedBy()));
		}
		
		long totalCount = this.dslContext.select(DSL.count())
				.from(BLOCK)
				.fetchOneInto(Long.class);
		
		List<BlockDTO> items = listQuery.where(conditions).orderBy(BLOCK.NUM.desc()).limit(criteria.getLimit()).offset(criteria.getOffSet()).fetchInto(BlockDTO.class);
		
		ListModel<BlockDTO, BlockCriteriaDTO> result = new ListModel<BlockDTO, BlockCriteriaDTO>(criteria, items, totalCount);
		for(BlockDTO blockDTO:result.getItems()){
			blockDTO.setTimestamp(blockDTO.getTimestamp().substring(0,blockDTO.getTimestamp().indexOf(".")));
			BigDecimal reward = new BigDecimal(blockDTO.getReward()/Math.pow(10,18));
			blockDTO.setReward(reward.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
		}
		
		return result;
	}
	
	
	private void setReward(List<BlockDTO> blocks) {
		
		// Rewards are fixed for block production, right now always 32trx, may (or may not change with time)
		// If it changes handle it here based on block timestamp
		
		for(BlockDTO block:blocks) {
//			block.setReward("32 TRX");
		}
		
	}


	public BlockChainDto getBlockChainInfo(){

		List<Field<?>> fields = new ArrayList<>(Arrays.asList(BLOCK_CHAIN.fields()));
		BlockChainDto blockChainDto =  this.dslContext.select(fields)
				.from(BLOCK_CHAIN).fetchOneInto(BlockChainDto.class);

//		blockChainDto.setInbTotalSupply(new BigDecimal(blockChainDto.getInbTotalSupply()).divide(new BigDecimal(Math.pow(10,18))).doubleValue());

		//TODO 开源时换掉此代码
		BigDecimal inbTotalSupply = new BigDecimal(blockChainDto.getLatestBlockNum()*6.34);
		blockChainDto.setInbTotalSupply(inbTotalSupply.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());

		BigDecimal mortgageInb = new BigDecimal(inbTotalSupply.multiply(new BigDecimal(0.3)).doubleValue());
		blockChainDto.setMortgageNetInb(mortgageInb.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue());
		return blockChainDto;
	}

	public ListModel<Node, NodeCriteriaDTO> getNodeInfo(NodeCriteriaDTO criteria){
		ArrayList<Condition> conditions = new ArrayList<>();
		SelectJoinStep<?> listQuery = this.dslContext.select()
				.from(NODE);
		long totalCount = this.dslContext.select(DSL.count())
				.from(NODE)
				.fetchOneInto(Long.class);
		List<Node> items = listQuery.where(conditions).orderBy(NODE.VOTE_NUMBER.desc()).limit(criteria.getLimit()).offset(criteria.getOffSet()).fetchInto(Node.class);

		double total = 0;

		for(Node node:items){
			total+=node.getVoteNumber().doubleValue();
		}

		for(Node node:items){
			BigDecimal b = new BigDecimal(node.getVoteNumber().doubleValue()/total);
			double voteRatio = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
			node.setVoteRatio(voteRatio);
		}
		ListModel<Node, NodeCriteriaDTO> result = new ListModel<Node, NodeCriteriaDTO>(criteria, items, totalCount);

		return result;
	}

	public void createBlock() {

		this.dslContext.update(BLOCK_CHAIN)
				.set(BLOCK_CHAIN.TRANSACTION_NUM, BLOCK_CHAIN.TRANSACTION_NUM.add(1))
				.set(BLOCK_CHAIN.ADDRESS_NUM,BLOCK_CHAIN.ADDRESS_NUM.add(2))
				.where(BLOCK_CHAIN.ID.eq(ULong.valueOf("1")))
				.execute();

	}


	
}

package io.inbscan.service;


import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.inbscan.constants.InbConstants;
import io.inbscan.dto.*;
import io.inbscan.dto.transaction.TransactionCriteria;
import io.inbscan.dto.transaction.TransactionDTO;
import io.inbscan.dto.transaction.TransactionModel;
import io.inbscan.dto.transaction.TransferModel;
import io.inbscan.model.tables.records.AccountRecord;
import io.inbscan.model.tables.records.BlockChainRecord;
import io.inbscan.model.tables.records.BlockRecord;
import io.inbscan.model.tables.records.TransactionRecord;
import io.inbscan.utils.HttpUtil;
import org.apache.commons.lang3.StringUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static io.inbscan.model.tables.Account.ACCOUNT;
import static io.inbscan.model.tables.Block.BLOCK;
import static io.inbscan.model.tables.BlockChain.BLOCK_CHAIN;
import static io.inbscan.model.tables.SyncAccount.SYNC_ACCOUNT;
import static io.inbscan.model.tables.Transaction.TRANSACTION;
import static io.inbscan.model.tables.Transfer.TRANSFER;

@Singleton
public class TransactionService {

	private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

	private DSLContext dslContext;
	
	private final int TRON_START_YEAR = 2018;


	private static Web3j web3j;
	private static Admin admin;

	private static String fromAddress = "0xdd19ce1c57f102b902809aa47619336f692410dd";
	private static BigDecimal defaultGasPrice = BigDecimal.valueOf(5);
	private static final String URL = "http://1.119.153.110:6002/";
//	private static final String URL = "http://192.168.1.182:6002/";

//	public static void main(String[] args) {
//		web3j = Web3j.build(new HttpService(URL));
//		admin = Admin.build(new HttpService(URL));
//
//		getBalance(fromAddress);
//
//	}

	
	@Inject
	public TransactionService(DSLContext dslContext) {
		this.dslContext  = dslContext;
	}
	

	

	public TransactionModel getTxByHash(String hash) {
		
		TransactionModel result = this.dslContext.select(TRANSACTION.ID,TRANSACTION.INPUT,TRANSACTION.BINDWITH,TRANSACTION.HASH,TRANSACTION.TIMESTAMP,BLOCK.NUM.as("block"),TRANSACTION.FROM,TRANSACTION.TYPE,TRANSACTION.CONFIRMED)
				.from(TRANSACTION)
				.join(BLOCK).on(BLOCK.ID.eq(TRANSACTION.BLOCK_ID))
		.where(TRANSACTION.HASH.eq(hash))
		.fetchOneInto(TransactionModel.class);

		if(result != null) {
			TransferModel transfer = this.dslContext.select(TRANSFER.TO,TRANSFER.AMOUNT)
					.from(TRANSFER).where(TRANSFER.TRANSACTION_ID.eq(ULong.valueOf(result.getId()))).fetchOneInto(TransferModel.class);
			result.setTo(transfer.getTo());
			result.setTimestamp(result.getTimestamp().substring(0,result.getTimestamp().indexOf(".")));
			result.setAmount(transfer.getAmount()/Math.pow(10,18));
		}
//;


		if (result==null) {
			TransactionModel transactionModel = new TransactionModel();
			List<Object> params = new ArrayList<>();
			params.add(hash);
			JsonParam jsonParam = new JsonParam();
			jsonParam.setJsonrpc("2.0");
			jsonParam.setMethod("eth_getTransactionReceipt");
			jsonParam.setParams(params);
			jsonParam.setId(67L);
			String param = JSONObject.toJSONString(jsonParam);
			String transaction = HttpUtil.doPost(InbConstants.URL, param);
			String bandwith = "0";
			String status = "0";

			JSONObject object = (JSONObject)JSONObject.parse(transaction);
			bandwith = object.getJSONObject("result").get("cumulativeNetUsed").toString();
			status = object.getJSONObject("result").get("status").toString();
			String from = object.getJSONObject("result").getString("from");
			String to = object.getJSONObject("result").getString("to");
			String blockNumber = object.getJSONObject("result").getString("blockNumber");



			List<Object> transParams = new ArrayList<>();
			transParams.add(hash);
			JsonParam transJsonParam = new JsonParam();
			transJsonParam.setId(67L);
			transJsonParam.setJsonrpc("2.0");
			transJsonParam.setMethod("eth_getTransactionByHash");
			transJsonParam.setParams(transParams);
			String transParam = JSONObject.toJSONString(transJsonParam);
			String transResult = HttpUtil.doPost(InbConstants.URL, transParam);
			JSONObject transObject = (JSONObject)JSONObject.parse(transResult);
			String transValue = transObject.getJSONObject("result").getString("value");
			String blockHash = transObject.getJSONObject("result").getString("blockHash");



			List<Object> blockParams = new ArrayList<>();
			blockParams.add(blockHash);
			blockParams.add(true);
			JsonParam jsonParamBlock = new JsonParam();
			jsonParamBlock.setJsonrpc("2.0");
			jsonParamBlock.setMethod("eth_getBlockByHash");
			jsonParamBlock.setParams(blockParams);
			jsonParamBlock.setId(67L);
			String blockParam = JSONObject.toJSONString(jsonParamBlock);
			String block = HttpUtil.doPost(InbConstants.URL, blockParam);
			JSONObject blockObject = (JSONObject)JSONObject.parse(block);
			String timestamp = blockObject.getJSONObject("result").getString("timestamp");

			double amount  = (Numeric.decodeQuantity(transValue).doubleValue()/Math.pow(10,18));
			BigDecimal amountResult = new BigDecimal(amount);
			amountResult.setScale(4,BigDecimal.ROUND_HALF_UP);


			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			transactionModel.setHash(hash);
			transactionModel.setTimestamp(simpleDateFormat.format(new Date(Long.valueOf(Numeric.decodeQuantity(timestamp).toString()+"000"))));
			transactionModel.setBindwith(bandwith);
			transactionModel.setAmount(amountResult.doubleValue());
			transactionModel.setBlock(Long.valueOf(Numeric.decodeQuantity(blockNumber).toString()));
			transactionModel.setFrom(from);
			transactionModel.setTo(to);
			transactionModel.setStatus(status);

			return transactionModel;
		}
		
//		switch (result.getTypeInt()) {
//		case ContractType.VoteWitnessContract_VALUE:
//
//			List<VoteModel> votes = this.dslContext.select(CONTRACT_VOTE_WITNESS.OWNER_ADDRESS.as("from"),CONTRACT_VOTE_WITNESS.VOTE_ADDRESS.as("to"),CONTRACT_VOTE_WITNESS.VOTE_COUNT.as("votes"))
//			.from(CONTRACT_VOTE_WITNESS)
//			.where(CONTRACT_VOTE_WITNESS.TRANSACTION_ID.eq(ULong.valueOf(result.getId())))
//			.fetchInto(VoteModel.class);
//
//			result.setContract(votes);
//
//			break;
//		case ContractType.TransferContract_VALUE:
//
//			TransferModel transferTrx = this.dslContext.select(TRANSFER.FROM,TRANSFER.TO,TRANSFER.AMOUNT)
//			.from(TRANSFER)
//			.where(TRANSFER.TRANSACTION_ID.eq(ULong.valueOf(result.getId())))
//			.fetchOneInto(TransferModel.class);
//
//			result.setContract(transferTrx);
//
//			break;
//		case ContractType.TransferAssetContract_VALUE:
//
//			TransferModel transferToken = this.dslContext.select(TRANSFER.FROM,TRANSFER.TO,TRANSFER.AMOUNT,TRANSFER.TOKEN)
//			.from(TRANSFER)
//			.where(TRANSFER.TRANSACTION_ID.eq(ULong.valueOf(result.getId())))
//			.fetchOneInto(TransferModel.class);
//
//			result.setContract(transferToken);
//
//			break;
//		default:
//			break;
//		}

		return result;
	}


	public ListModel<TransactionModel, TransactionCriteria> listTransactions(TransactionCriteria criteria) {

		ArrayList<Condition> conditions = new ArrayList<>();
		
		//remove invalid transactions (wrong dates)
		//FIXME: remove if/when fixed in tron
//		conditions.add(DSL.year(TRANSACTION.TIMESTAMP).gt(TRON_START_YEAR-1));
//		conditions.add(TRANSACTION.TIMESTAMP.le(DSL.select(DSL.max(BLOCK.TIMESTAMP)).from(BLOCK)));
//		conditions.add(TRANSACTION.FROM.isNotNull());

		if (criteria.getBlock()!=null) {
			conditions.add(TRANSACTION.BLOCK_ID.in(DSL.select(BLOCK.ID).from(BLOCK).where(BLOCK.ID.eq(ULong.valueOf(criteria.getBlock())))));
		}
//		else {
//			conditions.add(DSL.year(TRANSACTION.TIMESTAMP).lt(DSL.year(DSL.currentDate()).plus(1)));
//		}

		SelectOnConditionStep<?> listQuery = this.dslContext.select(TRANSACTION.ID,TRANSACTION.INPUT,TRANSACTION.HASH,TRANSACTION.TIMESTAMP,BLOCK.NUM.as("block"),TRANSACTION.FROM,TRANSFER.TO,TRANSFER.AMOUNT,TRANSACTION.TYPE,TRANSACTION.CONFIRMED,TRANSACTION.BINDWITH,TRANSACTION.STATUS)
		.from(TRANSACTION)
		.join(BLOCK).on(BLOCK.ID.eq(TRANSACTION.BLOCK_ID))
				.join(TRANSFER).on(TRANSACTION.ID.eq(TRANSFER.TRANSACTION_ID))
		;
		
		long totalCount = 0;
//		if (criteria.getBlock()==null) {
//			totalCount = this.quickStats.getTotalTx();
//		}else {
		if(criteria.getBlock()!=null) {
			totalCount = this.dslContext.select(DSL.count())
					.from(TRANSACTION)
					.where(TRANSACTION.BLOCK_ID.eq(ULong.valueOf(criteria.getBlock())))
					.fetchOneInto(Long.class);
//			totalCount = this.dslContext.select(DSL.count()).from(TRANSACTION).where(conditions).execute();
		}else {
			totalCount = this.dslContext.select(DSL.count()).from(TRANSACTION).fetchOneInto(Long.class);
		}
//		}
		
		
		List<TransactionModel> items = listQuery.where(conditions).orderBy(TRANSACTION.TIMESTAMP.desc()).limit(criteria.getLimit()).offset(criteria.getOffSet()).fetchInto(TransactionModel.class);
		List<TransactionModel> item = new ArrayList<>();
		for(TransactionModel transactionModel:items){
			double amount = transactionModel.getAmount();
			double divideNumber = Math.pow(10,18);
			BigDecimal b = new BigDecimal(amount/divideNumber);
			double result = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
			transactionModel.setAmount(result);

			transactionModel.setTimestamp(transactionModel.getTimestamp().substring(0,transactionModel.getTimestamp().indexOf(".")));
//			if(transactionModel.getBindwith().contains("0x")) {
//				transactionModel.setBindwith(fromHexString(transactionModel.getBindwith()));
//			}
//			if(transactionModel.getStatus().contains("0x")) {
//				transactionModel.setStatus(fromHexString(transactionModel.getStatus()));
//			}

			item.add(transactionModel);
		}
		
		ListModel<TransactionModel, TransactionCriteria> result = new ListModel<TransactionModel, TransactionCriteria>(criteria, item, totalCount);
		
		return result;
		
	}

	public ListModel<TransactionDTO,TransactionCriteria> getTransactionsForWallet(TransactionCriteria criteria){
		ArrayList<Condition> conditions = new ArrayList<>();

		if (criteria.getAddress()!=null) {
			conditions.add(TRANSACTION.ID.in(DSL.select(TRANSFER.TRANSACTION_ID).from(TRANSFER).where(TRANSFER.FROM.eq(criteria.getAddress())).or(TRANSFER.TO.eq(criteria.getAddress()))));
		}

		SelectOnConditionStep<?> listQuery = (SelectOnConditionStep<?>) this.dslContext.select(BLOCK.NUM.as("blockNumber"),BLOCK.HASH.as("blockHash"),TRANSACTION.ID,TRANSACTION.INPUT,TRANSACTION.HASH,TRANSACTION.TIMESTAMP,TRANSACTION.FROM,TRANSFER.TO,TRANSFER.AMOUNT,TRANSACTION.TYPE,TRANSACTION.CONFIRMED,TRANSACTION.BINDWITH,TRANSACTION.STATUS)
				.from(TRANSACTION)
				.join(BLOCK).on(BLOCK.ID.eq(TRANSACTION.BLOCK_ID))
				.join(TRANSFER).on(TRANSACTION.ID.eq(TRANSFER.TRANSACTION_ID));


		long totalCount = 0;

		if(criteria.getAddress()!=null) {
			totalCount = this.dslContext.select(DSL.count())
					.from(TRANSFER)
					.where(TRANSFER.FROM.eq(criteria.getAddress()))
					.or(TRANSFER.TO.eq(criteria.getAddress())).fetchOneInto(Long.class)
							;
//			totalCount = this.dslContext.select(DSL.count()).from(TRANSACTION).where(conditions).execute();
		}else {
			totalCount = this.dslContext.select(DSL.count()).from(TRANSACTION).fetchOneInto(Long.class);
		}
//		}


		List<TransactionDTO> items = listQuery.where(conditions).orderBy(TRANSACTION.TIMESTAMP.desc()).limit(criteria.getLimit()).offset(criteria.getOffSet()).fetchInto(TransactionDTO.class);
		List<TransactionDTO> item = new ArrayList<>();
		for(TransactionDTO transactionDTO:items){
			double amount = transactionDTO.getAmount();
			double divideNumber = Math.pow(10,18);
			BigDecimal b = new BigDecimal(amount/divideNumber);
			double result = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
			transactionDTO.setAmount(result);

			transactionDTO.setTimestamp(transactionDTO.getTimestamp().substring(0,transactionDTO.getTimestamp().indexOf(".")));

			//交易类型状态交易方向
			if(transactionDTO.getFrom().equals(criteria.getAddress())){
				transactionDTO.setDirection(1);
			}else if(transactionDTO.getTo().equals(criteria.getAddress())){
				transactionDTO.setDirection(2);
			}


			transactionDTO.setStatus(Numeric.decodeQuantity(transactionDTO.getStatus()).toString());
			transactionDTO.setBindwith(Numeric.decodeQuantity(transactionDTO.getBindwith()).toString());
			item.add(transactionDTO);
		}

		ListModel<TransactionDTO, TransactionCriteria> result = new ListModel<TransactionDTO, TransactionCriteria>(criteria, item,totalCount);

		return result;
	}


	public  String fromHexString(String hexString) {
		// 用于接收转换结果
		String result = "";
		// 转大写
		hexString = hexString.replaceAll("0x","");
		hexString = hexString.toUpperCase();
		// 16进制字符
		String hexDigital = "0123456789ABCDEF";
		// 将16进制字符串转换成char数组
		char[] hexs = hexString.toCharArray();
		// 能被16整除，肯定可以被2整除
		byte[] bytes = new byte[hexString.length() / 2];
		int n;

		for (int i = 0; i < bytes.length; i++) {
			n = hexDigital.indexOf(hexs[2 * i]) * 16 + hexDigital.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xff);
		}
		// byte[]--&gt;String
		try {
			result = new String(bytes, "UTF-8");
		}catch (Exception e){
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * 获取余额
	 *
	 * @param address 钱包地址
	 * @return 余额
	 */
	private static BigInteger getBalance(String address) {
		BigInteger balance = null;
		try {
			EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
			balance = ethGetBalance.getBalance();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("address " + address + " balance " + balance + "wei");
		return balance;
	}

	/**
	 * 生成一个普通交易对象
	 *
	 * @param fromAddress 放款方
	 * @param toAddress   收款方
	 * @param nonce       交易序号
	 * @param gasPrice    gas 价格
	 * @param gasLimit    gas 数量
	 * @param value       金额
	 * @return 交易对象
	 */
	private static Transaction makeTransaction(String fromAddress, String toAddress,
											   BigInteger nonce, BigInteger gasPrice,
											   BigInteger gasLimit, BigInteger value) {
		Transaction transaction;
		transaction = Transaction.createEtherTransaction(fromAddress, nonce, gasPrice, gasLimit, toAddress, value);
		return transaction;
	}

	/**
	 * 获取账号交易次数 nonce
	 *
	 * @param address 钱包地址
	 * @return nonce
	 */
	private static BigInteger getTransactionNonce(String address) {
		BigInteger nonce = BigInteger.ZERO;
		try {
			EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
			nonce = ethGetTransactionCount.getTransactionCount();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nonce;
	}

	public String sendTransaction(String toAddress){
		web3j = Web3j.build(new HttpService(URL));
		admin = Admin.build(new HttpService(URL));
		String password = "1";
		BigInteger unlockDuration = BigInteger.valueOf(60L);
		String randomAmount =String.valueOf(Math.random()*100) ;
		BigDecimal amount = new BigDecimal(randomAmount);
		String txHash = null;
		try {
			PersonalUnlockAccount personalUnlockAccount = admin.personalUnlockAccount(fromAddress, password, unlockDuration).send();
			if (personalUnlockAccount.accountUnlocked()) {

				logger.info("send transaction form" + fromAddress + "to" +toAddress);

				//不是必须的 可以使用默认值
				BigInteger gasLimit = new BigInteger("200000");
				BigInteger value = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
				//不是必须的 缺省值就是正确的值
				BigInteger nonce = getTransactionNonce(fromAddress);
				//该值为大部分矿工可接受的gasPrice
				BigInteger gasPrice = Convert.toWei(defaultGasPrice, Convert.Unit.GWEI).toBigInteger();
				Transaction transaction = makeTransaction(fromAddress, toAddress,
						nonce, gasPrice,
						gasLimit, value);
				EthSendTransaction ethSendTransaction = web3j.ethSendTransaction(transaction).send();
				txHash = ethSendTransaction.getTransactionHash();

				//更新用户账户余额
//				BigInteger toAddressBalance = getBalance(toString());
//				createOrUpdateAccount(toAddress,toAddressBalance);

			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("send transaction error" + e.getMessage());
		}
		System.out.println("tx hash " + txHash);
		return txHash;
	}


	public void createOrUpdateAccount(String address,BigInteger balance) {

		// Try to fetch existing account
		AccountRecord record = this.dslContext.select(ACCOUNT.ID)
				.from(ACCOUNT).where(ACCOUNT.ADDRESS.eq(address)).fetchOneInto(AccountRecord.class);

		// Create it if it doesn't exists yet
		if (record == null) {

			record = this.dslContext.insertInto(ACCOUNT)
					.set(ACCOUNT.TYPE, (byte) 1)
					.set(ACCOUNT.ADDRESS, address)
					.set(ACCOUNT.BALANCE, balance.doubleValue())
					.returning(ACCOUNT.ID)
					.fetchOne();

		} else {
			//Update if exists
			this.dslContext.update(ACCOUNT)
					.set(ACCOUNT.BALANCE, balance.doubleValue())
					.set(ACCOUNT.TRANSFER_TO_COUNT, DSL.select(DSL.count()).from(TRANSFER).where(TRANSFER.TO.eq(address)))
					.set(ACCOUNT.TRANSFER_FROM_COUNT, DSL.select(DSL.count()).from(TRANSFER).where(TRANSFER.FROM.eq(address)))
					.where(ACCOUNT.ID.eq(record.getId()))
					.execute();


		}
	}




	public void saveTransaction(EthBlock.TransactionObject transaction, BlockRecord block) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		logger.info("trans is: " + transaction.getHash());

		//交易状态以及交易消耗资源

		List<Object> params = new ArrayList<>();
		params.add(transaction.getHash());
		JsonParam inbJsonParam = new JsonParam();
		inbJsonParam.setJsonrpc("2.0");
		inbJsonParam.setMethod("eth_getTransactionReceipt");
		inbJsonParam.setParams(params);
		inbJsonParam.setId(67L);
		String param = JSONObject.toJSONString(inbJsonParam);
		String result = HttpUtil.doPost(InbConstants.URL, param);
		String bandwith = "0";
		String status = "0";
		if (StringUtils.isNotBlank(result)) {
			JSONObject object = (JSONObject) JSONObject.parse(result);
			bandwith = object.getJSONObject("result").get("cumulativeNetUsed").toString();
			status = object.getJSONObject("result").get("status").toString();
		}

		String transInput = null;
		int transType = 1;
		try {
			transInput = fromHexString(transaction.getInput());
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (StringUtils.isNotBlank(transInput) && transInput.contains("mortgageNet")) {
			transType = 2;
		}
		if (StringUtils.isNotBlank(transInput) && transInput.contains("unmortgageNet")) {
			transType = 3;
		}
		if (StringUtils.isNotBlank(transInput) && transInput.contains("candidates")) {
			transType = 4;
		}

		TransactionRecord txRecord = this.dslContext.insertInto(TRANSACTION)
				.set(TRANSACTION.HASH, transaction.getHash())
				.set(TRANSACTION.TIMESTAMP, block.getTimestamp())
				.set(TRANSACTION.FROM, transaction.getFrom())
				.set(TRANSACTION.BINDWITH, bandwith)
				.set(TRANSACTION.TYPE, transType)
				.set(TRANSACTION.INPUT, transInput)
				.set(TRANSACTION.STATUS, status)
				.set(TRANSACTION.BLOCK_ID, block.getId()).returning()
				.fetchOne();

		this.dslContext.insertInto(TRANSFER)
				.set(TRANSFER.FROM, transaction.getFrom())
				.set(TRANSFER.TO, transaction.getTo())
				.set(TRANSFER.AMOUNT, ULong.valueOf(transaction.getValue().longValue()))
				.set(TRANSFER.TRANSACTION_ID, txRecord.getId())
				.set(TRANSFER.TIMESTAMP, Timestamp.valueOf(format.format(System.currentTimeMillis())))
				.execute();

		synAccount(transaction.getFrom());
		synAccount(transaction.getTo());

		//当前消耗net
		BigInteger currentNetConsume = Numeric.decodeQuantity(bandwith);
		Integer netLimit = 0;
		BlockChainRecord blockChainRecord = this.dslContext.select().from(BLOCK_CHAIN).fetchOneInto(BlockChainRecord.class);
		if(blockChainRecord.getNetLimit()<currentNetConsume.intValue() || blockChainRecord.getNetLimit()==currentNetConsume.intValue()){
			netLimit = currentNetConsume.intValue();
		}else if(blockChainRecord.getNetLimit()>currentNetConsume.intValue()) {
			netLimit = blockChainRecord.getNetLimit();
		}
		this.dslContext.update(BLOCK_CHAIN)
				.set(BLOCK_CHAIN.TRANSACTION_NUM, BLOCK_CHAIN.TRANSACTION_NUM.add(1))
				.set(BLOCK_CHAIN.CURRENT_NET_CONSUMED,currentNetConsume.intValue())
				.set(BLOCK_CHAIN.NET_LIMIT,netLimit)
				.where(BLOCK_CHAIN.ID.eq(blockChainRecord.getId()))
				.execute();
	}
	public void synAccount(String address){
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		this.dslContext.insertInto(SYNC_ACCOUNT)
				.set(SYNC_ACCOUNT.ADDRESS,address)
				.set(SYNC_ACCOUNT.DATE_CREATED, Timestamp.valueOf(format.format(System.currentTimeMillis())))
				.execute();
	}




}

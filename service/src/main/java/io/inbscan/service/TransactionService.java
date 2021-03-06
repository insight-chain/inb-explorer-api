package io.inbscan.service;


import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.inbscan.chain.InbChainService;
import io.inbscan.constants.InbConstants;
import io.inbscan.dto.JsonParam;
import io.inbscan.dto.ListModel;
import io.inbscan.dto.account.*;
import io.inbscan.dto.transaction.TransactionCriteria;
import io.inbscan.dto.transaction.TransactionDTO;
import io.inbscan.dto.transaction.TransactionModel;
import io.inbscan.dto.transaction.TransferModel;
import io.inbscan.model.tables.pojos.Block;
import io.inbscan.model.tables.pojos.Store;
import io.inbscan.model.tables.pojos.TokenHolder;
import io.inbscan.model.tables.pojos.TransactionLog;
import io.inbscan.model.tables.records.*;
import io.inbscan.utils.HttpUtil;
import io.inbscan.utils.InbConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.SelectOnConditionStep;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
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
import java.util.List;
import java.util.Map;

import static io.inbscan.model.tables.Account.ACCOUNT;
import static io.inbscan.model.tables.Block.BLOCK;
import static io.inbscan.model.tables.BlockChain.BLOCK_CHAIN;
import static io.inbscan.model.tables.Store.STORE;
import static io.inbscan.model.tables.SyncAccount.SYNC_ACCOUNT;
import static io.inbscan.model.tables.Token.TOKEN;
import static io.inbscan.model.tables.TokenHolder.TOKEN_HOLDER;
import static io.inbscan.model.tables.Transaction.TRANSACTION;
import static io.inbscan.model.tables.TransactionLog.TRANSACTION_LOG;
import static io.inbscan.model.tables.Transfer.TRANSFER;

@Singleton
public class TransactionService {

	private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

	private DSLContext dslContext;
	private InbChainService inbChainService;
	
	private final int TRON_START_YEAR = 2018;


	private static Web3j web3j;
	private static Admin admin;

	private static String fromAddress = "0xdd19ce1c57f102b902809aa47619336f692410dd";
	private static BigDecimal defaultGasPrice = BigDecimal.valueOf(5);
	private static final String URL = "http://192.168.1.1:8080/";

//	public static void main(String[] args) {
//		web3j = Web3j.build(new HttpService(URL));
//		admin = Admin.build(new HttpService(URL));
//
//		getBalance(fromAddress);
//
//	}

	
	@Inject
	public TransactionService(DSLContext dslContext, InbChainService inbChainService ){

		this.dslContext = dslContext;
		this.inbChainService = inbChainService;
	}
	

	

	public TransactionModel getTxByHash(String hash) throws Exception{
		
		TransactionModel result = this.dslContext.select(TRANSACTION.ID,TRANSACTION.INPUT,TRANSACTION.BINDWITH,TRANSACTION.HASH
				,TRANSACTION.TIMESTAMP,BLOCK.NUM.as("block"),TRANSACTION.FROM,TRANSACTION.TYPE,TRANSACTION.CONFIRMED,TRANSACTION.STATUS)
				.from(TRANSACTION)
				.join(BLOCK).on(BLOCK.ID.eq(TRANSACTION.BLOCK_ID))
		.where(TRANSACTION.HASH.eq(hash))
		.fetchOneInto(TransactionModel.class);

		if(result != null) {
			List<TransactionLog> record = this.dslContext.select()
					.from(TRANSACTION_LOG).where(TRANSACTION_LOG.INLINE_TRANSACTION_HASH.eq(result.getHash())).fetchInto(TransactionLog.class);

			if(record!=null) {
                for (TransactionLog transactionLog : record) {
                    transactionLog.setAmount(InbConvertUtils.AmountConvert(transactionLog.getAmount()));
                }
            }
			result.setLog(record);

			for (TransactionLog transactionLog : record) {
				transactionLog.setAmount(InbConvertUtils.AmountConvert(transactionLog.getAmount()));
			}

			TransferModel transfer = this.dslContext.select(TRANSFER.TO,TRANSFER.AMOUNT)
					.from(TRANSFER).where(TRANSFER.TRANSACTION_ID.eq(ULong.valueOf(result.getId()))).fetchOneInto(TransferModel.class);
			result.setTo(transfer.getTo());
//			result.setTimestamp(result.getTimestamp().substring(0,result.getTimestamp().indexOf(".")));
			result.setAmount(InbConvertUtils.AmountConvert(transfer.getAmount()));
			result.setInput(InbConvertUtils.fromHexString(result.getInput()));
			if(result.getType() == 3){
				Accountdto accountdto = this.getActByAddress(record.get(0).getFrom());
				List<StoreDTO> storeDTOS = accountdto.getStore();
				for (StoreDTO storeDTO : storeDTOS) {
					if(storeDTO.getHash().equals(hash)){
						result.setLockHeight(storeDTO.getLockHeight());
						result.setMortgage(storeDTO.getAmount());
					}
				}
			}
		}


//;


		if (result==null) {
			TransactionModel transactionModel = new TransactionModel();
			JSONObject object = inbChainService.getTransactionReceipt(hash);
			if(object == null){
				return new TransactionModel();
			}
			String bandwith = "0";
			String status = "0";
			if(object.getJSONObject("error").size() != 0){
				return new TransactionModel();
			}
			bandwith = object.getJSONObject("result").get("cumulativeNetUsed").toString();
			status = object.getJSONObject("result").get("status").toString();
			String from = object.getJSONObject("result").getString("from");
			String to = object.getJSONObject("result").getString("to");
			String blockNumber = object.getJSONObject("result").getString("blockNumber");


			JSONObject transObject = inbChainService.getTransInfo(hash);
			String transValue = transObject.getJSONObject("result").getString("value");
			String blockHash = transObject.getJSONObject("result").getString("blockHash");

			JSONObject blockObject = inbChainService.getBlockByHash(blockHash);
			String timestamp = blockObject.getJSONObject("result").getString("timestamp");

			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			transactionModel.setHash(hash);
			transactionModel.setTimestamp(Long.valueOf(timestamp));
			transactionModel.setBindwith(bandwith);
			transactionModel.setAmount(InbConvertUtils.AmountConvert(Numeric.decodeQuantity(transValue).doubleValue()));
			transactionModel.setBlock(Long.valueOf(Numeric.decodeQuantity(blockNumber).toString()));
			transactionModel.setFrom(from);
			transactionModel.setTo(to);
			transactionModel.setStatus(status);

			return transactionModel;
		}

		return result;
	}


	public Accountdto getActByAddress(String address) throws Exception {

		Accountdto result = this.dslContext.select(ACCOUNT.ID, ACCOUNT.ADDRESS, ACCOUNT.MORTGAGE, ACCOUNT.NONCE, ACCOUNT.BALANCE, ACCOUNT.USED.as("used"),
				ACCOUNT.USABLE.as("usable"), ACCOUNT.REGULAR.as("regular"), ACCOUNT.REDEEM.as("redeemValue"), ACCOUNT.REDEEM_START_HEIGHT, ACCOUNT.VOTE_NUMBER,
				ACCOUNT.LAST_RECEIVE_VOTE_AWARD_HEIGHT)
				.from(ACCOUNT).where(ACCOUNT.ADDRESS.eq(address)).fetchOneInto(Accountdto.class);
		if (result == null) {
			return new Accountdto();
		}

		List<Store> stores = this.dslContext.select().from(STORE).where(STORE.ADDRESS.eq(address)).fetchInto(io.inbscan.model.tables.pojos.Store.class);
		List<StoreDTO> storeDTOS = new ArrayList<>();
		for (Store store : stores) {
			StoreDTO storeDTO = new StoreDTO();
			storeDTO.setStartHeight(store.getStartHeight());
			storeDTO.setId(store.getId());
			storeDTO.setAddress(store.getAddress());
			storeDTO.setLastReceivedHeight(store.getLastReceivedHeight());
			storeDTO.setLockHeight(store.getLockHeight());
			storeDTO.setReceived(InbConvertUtils.AmountConvert(Double.valueOf(store.getReceived())));
			storeDTO.setAmount(InbConvertUtils.AmountConvert(Double.valueOf(store.getAmount().toString())));
			storeDTO.setHash(store.getHash());
			storeDTOS.add(storeDTO);
		}

		List<TokenDTO> tokenDTOS = new ArrayList<>();
		List<TokenHolder> tokenHolders = this.dslContext.select().from(TOKEN_HOLDER).where(TOKEN_HOLDER.HOLDER_ADDRESS.eq(address)).fetchInto(TokenHolder.class);
		for (TokenHolder tokenHolder : tokenHolders) {
			TokenDTO tokenDTO = new TokenDTO();
			Token token = this.dslContext.select().from(TOKEN).where(TOKEN.ADDRESS.eq(tokenHolder.getTokenAddress())).fetchOneInto(Token.class);
			tokenDTO.setIcon(token.getIcon());
			tokenDTO.setSymbol(token.getSymbol());
			tokenDTOS.add(tokenDTO);
		}
		ResDTO resDTO = this.dslContext.select(ACCOUNT.MORTGAGE,ACCOUNT.USABLE,ACCOUNT.USED,ACCOUNT.MORTGAGE_HEIGHT.as("height")).from(ACCOUNT).where(ACCOUNT.ADDRESS.eq(address)).fetchOneInto(ResDTO.class);
		resDTO.setMortgage(InbConvertUtils.AmountConvert(resDTO.getMortgage()));
		result.setRes(resDTO);
		result.setBalance(InbConvertUtils.AmountConvert(result.getBalance()));
		result.setRedeemValue(InbConvertUtils.AmountConvert(result.getRedeemValue()));
		result.setRegular(InbConvertUtils.AmountConvert(result.getRegular()));
//        result.setUsed(InbConvertUtils.AmountConvert(result.getUsed()));
		result.setStore(storeDTOS);
		result.setToken(tokenDTOS);
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
			conditions.add(TRANSACTION.BLOCK_NUM.in(DSL.select(BLOCK.NUM).from(BLOCK).where(BLOCK.NUM.eq(ULong.valueOf(criteria.getBlock())))));
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
					.where(TRANSACTION.BLOCK_NUM.eq(ULong.valueOf(criteria.getBlock())))
					.fetchOneInto(Long.class);
//			totalCount = this.dslContext.select(DSL.count()).from(TRANSACTION).where(conditions).execute();
		}else {
			totalCount = this.dslContext.select(DSL.count()).from(TRANSACTION).fetchOneInto(Long.class);
		}
//		}
		
		
		List<TransactionModel> items = listQuery.where(conditions).orderBy(TRANSACTION.TIMESTAMP.desc()).limit(criteria.getLimit()).offset(criteria.getOffSet()).fetchInto(TransactionModel.class);
		List<TransactionModel> item = new ArrayList<>();
		for(TransactionModel transactionModel:items){
			transactionModel.setAmount(InbConvertUtils.AmountConvert(transactionModel.getAmount()));
			transactionModel.setInput(InbConvertUtils.fromHexString(transactionModel.getInput()));
//			transactionModel.setTimestamp(transactionModel.getTimestamp().substring(0,transactionModel.getTimestamp().indexOf(".")));



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
		long totalCount = 0;
		if (criteria.getAddress()!=null && criteria.getTokenAddress() == null) {
			conditions.add(TRANSACTION.ID.in(DSL.select(TRANSFER.TRANSACTION_ID).from(TRANSFER).where(TRANSFER.FROM.eq(criteria.getAddress())).or(TRANSFER.TO.eq(criteria.getAddress()))));
//			totalCount = this.dslContext.select(DSL.count())
//					.from(TRANSFER)
//					.where(TRANSFER.FROM.eq(criteria.getAddress()))
//					.or(TRANSFER.TO.eq(criteria.getAddress())).fetchOneInto(Long.class)
//			;
		}
		if(criteria.getType()!=null && criteria.getType().equals("award")) {
			conditions.add(TRANSACTION.TYPE.eq(8).or(TRANSACTION.TYPE.eq(9)));
		}
		if(criteria.getTransType()!=null){
			conditions.add(TRANSACTION.TYPE.eq(criteria.getTransType()));
		}
		if(criteria.getTokenAddress()!=null && criteria.getAddress() == null){
		    conditions.add(TRANSACTION.TOKEN_ADDRESS.eq(criteria.getTokenAddress()));
//		    totalCount = this.dslContext.select(DSL.count())
//					.from(TRANSACTION)
//					.where(TRANSACTION.TOKEN_NAME.eq(criteria.getToken()))
//					.fetchOneInto(Long.class);
        }

		if(criteria.getAddress()!=null && criteria.getTokenAddress() != null){
            conditions.add(TRANSACTION.ID.in(DSL.select(TRANSFER.TRANSACTION_ID)
                    .from(TRANSFER).where(TRANSFER.FROM.eq(criteria.getAddress())).or(TRANSFER.TO.eq(criteria.getAddress()))));
            conditions.add(TRANSACTION.TOKEN_ADDRESS.eq(criteria.getTokenAddress()));
        }



		SelectOnConditionStep<?> listQuery = (SelectOnConditionStep<?>) this.dslContext.select(TRANSACTION.ID,TRANSACTION.BLOCK_ID,TRANSACTION.BLOCK_HASH,TRANSACTION.BLOCK_NUM.as("blockNumber"),TRANSACTION.INPUT,TRANSACTION.HASH,TRANSACTION.TIMESTAMP,TRANSACTION.FROM,TRANSFER.TO,TRANSFER.AMOUNT,TRANSACTION.TYPE,TRANSACTION.CONFIRMED,TRANSACTION.BINDWITH,TRANSACTION.STATUS)
				.from(TRANSACTION)
				.join(TRANSFER).on(TRANSACTION.ID.eq(TRANSFER.TRANSACTION_ID));


		totalCount = listQuery.where(conditions).fetch().size();

		List<TransactionDTO> items = listQuery.where(conditions).orderBy(TRANSACTION.TIMESTAMP.desc()).limit(criteria.getLimit()).offset(criteria.getOffSet()).fetchInto(TransactionDTO.class);
		List<TransactionDTO> item = new ArrayList<>();
		for(TransactionDTO transactionDTO:items){
			transactionDTO.setAmount(InbConvertUtils.AmountConvert(transactionDTO.getAmount()));

//			transactionDTO.setTimestamp(transactionDTO.getTimestamp().substring(0,transactionDTO.getTimestamp().indexOf(".")));

			//交易类型状态交易方向
			if(transactionDTO.getFrom()!= null && transactionDTO.getFrom().equals(criteria.getAddress())){
				transactionDTO.setDirection(1);
			}else if(transactionDTO.getTo()!=null && transactionDTO.getTo().equals(criteria.getAddress())){
				transactionDTO.setDirection(2);
			}

			List<TransactionLog> record = this.dslContext.select()
					.from(TRANSACTION_LOG).where(TRANSACTION_LOG.INLINE_TRANSACTION_HASH.eq(transactionDTO.getHash())).fetchInto(TransactionLog.class);

			if(record!=null) {
                for (TransactionLog transactionLog : record) {
                    transactionLog.setAmount(InbConvertUtils.AmountConvert(transactionLog.getAmount()));
                }
            }
			transactionDTO.setTransactionLog(record);
			transactionDTO.setBindwith(Numeric.decodeQuantity(transactionDTO.getBindwith()).toString());
			transactionDTO.setInput(InbConvertUtils.fromHexString(transactionDTO.getInput()));
			item.add(transactionDTO);
		}



		ListModel<TransactionDTO, TransactionCriteria> result = new ListModel<TransactionDTO, TransactionCriteria>(criteria, item,totalCount);

		return result;
	}


    public AccountMonitor getUserTransferOut(String address, String startTime, String endTime) {
	    Double totalAmount =0d;
		int transCount =0;
	    Double balance = this.dslContext.select(ACCOUNT.BALANCE).from(ACCOUNT).where(ACCOUNT.ADDRESS.equal(address)).fetchOneInto(Double.class);

        List<ULong> transAmounts = this.dslContext.select(TRANSFER.AMOUNT).from(TRANSFER)
                .where(TRANSFER.FROM.eq(address)).and(TRANSFER.TIMESTAMP.between(Timestamp.valueOf(startTime),Timestamp.valueOf(endTime))).fetchInto(ULong.class);

        for (ULong transAmount : transAmounts) {
        	transCount++;
            totalAmount+= transAmount.doubleValue();
        }
        AccountMonitor accountMonitor = new AccountMonitor();
        accountMonitor.setBalance(balance);
        accountMonitor.setTransAmount(InbConvertUtils.AmountConvert(totalAmount));
        accountMonitor.setTransCount(transCount);
        return accountMonitor;
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




	 public void saveTransaction(JSONObject transaction, BlockRecord block) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String transHash = transaction.getString("hash");
		String transFrom = transaction.getString("from");
		String transTo = transaction.getString("to");

		logger.info("trans is: " + transHash );

		synAccount(transFrom);
		if(transTo != null){
			synAccount(transTo);
		}

		//		//当前消耗net
//		BigInteger currentNetConsume = Numeric.decodeQuantity(bandwith);
//		Integer netLimit = 0;
		BlockChainRecord blockChainRecord = this.dslContext.select().from(BLOCK_CHAIN).fetchOneInto(BlockChainRecord.class);
//		if(blockChainRecord.getNetLimit()<currentNetConsume.intValue() || blockChainRecord.getNetLimit()==currentNetConsume.intValue()){
//			netLimit = currentNetConsume.intValue();
//		}else if(blockChainRecord.getNetLimit()>currentNetConsume.intValue()) {
//			netLimit = blockChainRecord.getNetLimit();
//		}
		this.dslContext.update(BLOCK_CHAIN)
				.set(BLOCK_CHAIN.TRANSACTION_NUM, BLOCK_CHAIN.TRANSACTION_NUM.add(1))
				.where(BLOCK_CHAIN.ID.eq(blockChainRecord.getId()))
				.execute();


		//增加transLog
		List<JSONObject> logObject = inbChainService.getTransLog(transHash);
		for (int i = 0; i < logObject.size(); i++) {
			this.dslContext.insertInto(TRANSACTION_LOG)
					.set(TRANSACTION_LOG.INLINE_TRANSACTION_HASH,transHash)
					.set(TRANSACTION_LOG.ADDRESS,logObject.get(i).get("address").toString())
					.set(TRANSACTION_LOG.BLOCK_HASH,logObject.get(i).get("blockHash").toString())
					.set(TRANSACTION_LOG.TRANSACTION_HASH,logObject.get(i).get("transactionHash").toString())
					.set(TRANSACTION_LOG.TRANSACTION_TYPE,logObject.get(i).getInteger("txType"))
					.set(TRANSACTION_LOG.TRANSACTION_INDEX, Numeric.decodeQuantity(logObject.get(i).get("transactionIndex").toString()).intValue())
					.set(TRANSACTION_LOG.FROM,logObject.get(i).get("from").toString())
					.set(TRANSACTION_LOG.TO,logObject.get(i).get("to").toString())
					.set(TRANSACTION_LOG.AMOUNT,logObject.get(i).getLong("value").doubleValue())
					.execute();
		}

		//交易状态以及交易消耗资源
		JSONObject transReceipt = inbChainService.getTransactionReceipt(transHash);
		String bandwith = "0";
		String status = "0";
		double value = 0d;
		if (transReceipt!=null) {
			bandwith = transReceipt.getJSONObject("result").getString("resUsed");
			status = transReceipt.getJSONObject("result").getString("status");
			value = transReceipt.getJSONObject("result").getInteger("value").doubleValue();
		}

//		String transInput = InbConvertUtils.fromHexString(transaction.getString("input"));
		JSONObject transactionObject = inbChainService.getTransInfo(transHash);

		TransactionRecord txRecord = this.dslContext.insertInto(TRANSACTION)
				.set(TRANSACTION.HASH, transHash)
				.set(TRANSACTION.TIMESTAMP, block.getTimestamp())
				.set(TRANSACTION.FROM, transaction.getString("from"))
				.set(TRANSACTION.BINDWITH, bandwith)
				.set(TRANSACTION.TYPE, Numeric.decodeQuantity(transactionObject.getJSONObject("result").getString("txType")).intValue())
				.set(TRANSACTION.INPUT, transaction.getString("input"))
				.set(TRANSACTION.STATUS, status)
				.set(TRANSACTION.BLOCK_HASH,block.getHash())
				.set(TRANSACTION.BLOCK_NUM,block.getNum())
                .set(TRANSACTION.VALUE,value)
				.set(TRANSACTION.BLOCK_ID, block.getId()).returning()
				.fetchOne();

        this.saveTransfer(transaction,txRecord.getId());

	}

     public void  saveTransfer(JSONObject transaction,ULong transactionId){
            String transFrom = transaction.getString("from");
            String transTo = transaction.getString("to");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            this.dslContext.insertInto(TRANSFER)
                    .set(TRANSFER.FROM, transFrom)
                    .set(TRANSFER.TO, transTo)
                    .set(TRANSFER.AMOUNT, ULong.valueOf(Numeric.decodeQuantity(transaction.getString("value")).longValue()))
                    .set(TRANSFER.TRANSACTION_ID, transactionId)
                    .set(TRANSFER.TIMESTAMP, Timestamp.valueOf(format.format(System.currentTimeMillis())))
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

package io.inbscan.service;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import io.inbscan.constants.InbConstants;
import io.inbscan.dto.ListResult;
import io.inbscan.dto.account.AccountCriteria;
import io.inbscan.dto.account.AccountDTO;
import io.inbscan.dto.JsonParam;
import io.inbscan.dto.account.Accountdto;
import io.inbscan.dto.block.BlockDTO;
import io.inbscan.dto.transaction.TransferModel;
import io.inbscan.model.tables.records.AccountRecord;
import io.inbscan.utils.HttpUtil;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SelectJoinStep;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static io.inbscan.model.tables.Account.ACCOUNT;
import static io.inbscan.model.tables.BlockChain.BLOCK_CHAIN;
import static io.inbscan.model.tables.Transaction.TRANSACTION;
import static io.inbscan.model.tables.Transfer.TRANSFER;


public class AccountService {

	private DSLContext dslContext;

	private TransactionService txService;

	private BlockService blockService;

	private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

	@Inject
	public AccountService(DSLContext dslContext, TransactionService txService,BlockService blockService) {
		this.dslContext = dslContext;
		this.txService = txService;
		this.blockService = blockService;
	}


	public int getTotalAccount() {
		return this.dslContext.select(DSL.count()).from(ACCOUNT).fetchOneInto(Integer.class);
	}
	
	public List<AccountDTO> getLatestAccounts(int limit){
		
		return this.dslContext.select(ACCOUNT.fields()).from(ACCOUNT).orderBy(ACCOUNT.CREATE_TIME.desc()).limit(limit).fetchInto(AccountDTO.class);
		
	}


	public void createOrUpdateAccount(String address) {


		List<Object> params = new ArrayList<>();
		params.add(address);
		JsonParam inbJsonParam = new JsonParam();
		inbJsonParam.setJsonrpc("2.0");
		inbJsonParam.setMethod("eth_getAccountInfo");
		inbJsonParam.setParams(params);
		inbJsonParam.setId(67L);
		String param = JSONObject.toJSONString(inbJsonParam);
		String result = HttpUtil.doPost(InbConstants.URL, param);
		JSONObject object = (JSONObject) JSONObject.parse(result);
		String nonce = object.getJSONObject("result").get("Nonce").toString();
		String mortgagteINB = object.getJSONObject("result").getJSONObject("Resources").getJSONObject("NET").get("MortgagteINB").toString();
		Integer usableness = object.getJSONObject("result").getJSONObject("Resources").getJSONObject("NET").getInteger("Usableness");
		Integer used = object.getJSONObject("result").getJSONObject("Resources").getJSONObject("NET").getInteger("Used");
		BigInteger balance = object.getJSONObject("result").getBigInteger("Balance");

		AccountRecord record = this.dslContext.select(ACCOUNT.ID)
				.from(ACCOUNT).where(ACCOUNT.ADDRESS.eq(address)).fetchOneInto(AccountRecord.class);

		// Create it if it doesn't exists yet
		if (record == null) {
			logger.info("create account");
			record = this.dslContext.insertInto(ACCOUNT)
//			.set(ACCOUNT.ACCOUNT_NAME,tronAccount.getAccountName().toStringUtf8())
					.set(ACCOUNT.TYPE, (byte) 1)
					.set(ACCOUNT.ADDRESS, address)
					.set(ACCOUNT.BALANCE, balance.doubleValue())
					.set(ACCOUNT.BANDWIDTH, Long.valueOf(usableness))
					.set(ACCOUNT.ALLOWANCE, ULong.valueOf(used))
					.set(ACCOUNT.NONCE, Integer.valueOf(nonce))
					.set(ACCOUNT.MORTGAGTE, mortgagteINB)
					.returning(ACCOUNT.ID)
					.fetchOne();

			this.dslContext.update(BLOCK_CHAIN)
					.set(BLOCK_CHAIN.ADDRESS_NUM, BLOCK_CHAIN.ADDRESS_NUM.add(1))
					.where(BLOCK_CHAIN.ID.eq(ULong.valueOf("1")))
					.execute();

		} else {
			logger.info("update account");
			//Update if exists
			this.dslContext.update(ACCOUNT)
					.set(ACCOUNT.BALANCE, balance.doubleValue())
					.set(ACCOUNT.TRANSFER_TO_COUNT, DSL.select(DSL.count()).from(TRANSFER).where(TRANSFER.TO.eq(address)))
					.set(ACCOUNT.TRANSFER_FROM_COUNT, DSL.select(DSL.count()).from(TRANSFER).where(TRANSFER.FROM.eq(address)))
					.where(ACCOUNT.ID.eq(record.getId()))
					.execute();


		}
	}


	public Accountdto getActByAddress(AccountCriteria accountCriteria){

		Accountdto result = this.dslContext.select(ACCOUNT.ID,ACCOUNT.MORTGAGTE,ACCOUNT.NONCE,ACCOUNT.BALANCE,ACCOUNT.ALLOWANCE.as("usableness"),ACCOUNT.BANDWIDTH.as("used"))
				.from(ACCOUNT).where(ACCOUNT.ADDRESS.eq(accountCriteria.getAddress())).fetchOneInto(Accountdto.class);

		double amount = Double.valueOf(result.getMortgagte());
		double balance = result.getBalance();
		double divideNumber = Math.pow(10, 18);
		BigDecimal a = new BigDecimal(amount / divideNumber);
		BigDecimal b = new BigDecimal(balance / divideNumber);
		double amountResult = a.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
		double balanceResult = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
		result.setMortgagte(String.valueOf(amountResult));
		result.setBalance(balanceResult);
		return result;
	}


	public ListResult<TransferModel, AccountCriteria> listTransfersIn(AccountCriteria criteria) {

		ArrayList<Condition> conditions = new ArrayList<>();


		List<Field<?>> fields = new ArrayList<>(Arrays.asList(TRANSFER.fields()));
		fields.add(TRANSACTION.HASH);

		SelectJoinStep<?> listQuery = this.dslContext.select(fields)
				.from(TRANSACTION)
				.join(TRANSFER).on(TRANSFER.TRANSACTION_ID.eq(TRANSACTION.ID)).and((TRANSFER.TO.eq(criteria.getAddress())))
				;

		Integer totalCount = null;

		if (!criteria.isTrx()) {
			totalCount = this.dslContext.select(ACCOUNT.TRANSFER_TO_COUNT)
					.from(ACCOUNT)
					.where(ACCOUNT.ADDRESS.eq(criteria.getAddress()))
					.fetchOneInto(Integer.class);
		}else {

			conditions.add(TRANSFER.TOKEN.isNull());

			totalCount = this.dslContext.select(DSL.count())
					.from(TRANSACTION)
					.join(TRANSFER).on(TRANSFER.TRANSACTION_ID.eq(TRANSACTION.ID)).and((TRANSFER.TO.eq(criteria.getAddress())))
					.where(conditions)
					.fetchOneInto(Integer.class);
			;

		}


		if (totalCount==null) {
			totalCount = 0;
		}

		List<TransferModel> items = listQuery.where(conditions).orderBy(TRANSFER.TIMESTAMP.desc()).limit(criteria.getLimit()).offset(criteria.getOffSet()).fetchInto(TransferModel.class);


		ListResult<TransferModel, AccountCriteria> result = new ListResult<TransferModel, AccountCriteria>(criteria, items, totalCount);

		return result;
	}


	public ListResult<TransferModel, AccountCriteria> listTransfersOut(AccountCriteria criteria) {

		ArrayList<Condition> conditions = new ArrayList<>();


		List<Field<?>> fields = new ArrayList<>(Arrays.asList(TRANSFER.fields()));
		fields.add(TRANSACTION.HASH);
		fields.add(TRANSACTION.BLOCK_ID);
		fields.add(TRANSACTION.TYPE);

		SelectJoinStep<?> listQuery = this.dslContext.select(fields)
				.from(TRANSACTION)
				.join(TRANSFER).on(TRANSFER.TRANSACTION_ID.eq(TRANSACTION.ID)).and((TRANSFER.FROM.eq(criteria.getAddress())))
				;


		Integer totalCount = null;

		if (!criteria.isTrx()) {
			totalCount = this.dslContext.select(ACCOUNT.TRANSFER_FROM_COUNT)
					.from(ACCOUNT)
					.where(ACCOUNT.ADDRESS.eq(criteria.getAddress()))
					.fetchOneInto(Integer.class);
		}else {

			conditions.add(TRANSFER.TOKEN.isNull());

			totalCount = this.dslContext.select(DSL.count())
					.from(TRANSFER).where(TRANSFER.FROM.eq(criteria.getAddress()))
					.fetchOneInto(Integer.class);

		}

		if (totalCount==null) {
			totalCount = 0;
		}

		List<TransferModel> items = listQuery.where(conditions).orderBy(TRANSFER.TIMESTAMP.desc()).limit(criteria.getLimit()).offset(criteria.getOffSet()).fetchInto(TransferModel.class);
		List<TransferModel> item = new ArrayList<>();
		for(TransferModel transferModel:items){
			double amount = transferModel.getAmount();
			double divideNumber = Math.pow(10,18);
			BigDecimal b = new BigDecimal(amount/divideNumber);
			double result = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
			transferModel.setAmount(result);
			transferModel.setTimestamp(transferModel.getTimestamp().substring(0,transferModel.getTimestamp().indexOf(".")));
			item.add(transferModel);

			BlockDTO blockDTO = this.blockService.getBlockById(transferModel.getBlockId());
			transferModel.setBlockNum(blockDTO.getNum());
		}

		ListResult<TransferModel, AccountCriteria> result = new ListResult<TransferModel, AccountCriteria>(criteria, item, totalCount);

		return result;
	}

}

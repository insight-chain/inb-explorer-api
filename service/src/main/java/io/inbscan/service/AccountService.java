package io.inbscan.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import io.inbscan.chain.InbChainService;
import io.inbscan.dto.ListResult;
import io.inbscan.dto.account.AccountCriteria;
import io.inbscan.dto.account.AccountDTO;
import io.inbscan.dto.account.Accountdto;
import io.inbscan.dto.account.StoreDTO;
import io.inbscan.dto.block.BlockDTO;
import io.inbscan.dto.transaction.TransferModel;
import io.inbscan.model.tables.pojos.Store;
import io.inbscan.model.tables.records.AccountRecord;
import io.inbscan.utils.InbConvertUtils;
import jnr.ffi.annotations.In;
import org.apache.commons.lang3.time.DateFormatUtils;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static io.inbscan.model.tables.Account.ACCOUNT;
import static io.inbscan.model.tables.BlockChain.BLOCK_CHAIN;
import static io.inbscan.model.tables.Store.STORE;
import static io.inbscan.model.tables.Transaction.TRANSACTION;
import static io.inbscan.model.tables.Transfer.TRANSFER;


public class AccountService {

    private DSLContext dslContext;

    private TransactionService txService;

    private BlockService blockService;

    private InbChainService inbChainService;

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Inject
    public AccountService(DSLContext dslContext, TransactionService txService, BlockService blockService, InbChainService inbChainService) {
        this.dslContext = dslContext;
        this.txService = txService;
        this.blockService = blockService;
        this.inbChainService = inbChainService;
    }


    public int getTotalAccount() {
        return this.dslContext.select(DSL.count()).from(ACCOUNT).fetchOneInto(Integer.class);
    }

    public List<AccountDTO> getLatestAccounts(int limit) {

        return this.dslContext.select(ACCOUNT.fields()).from(ACCOUNT).orderBy(ACCOUNT.CREATE_TIME.desc()).limit(limit).fetchInto(AccountDTO.class);

    }


    public void createOrUpdateAccount(String address) {

        JSONObject object = inbChainService.getAccountInfo(address);
        Integer nonce = object.getJSONObject("result").getInteger("Nonce");
        Integer mortgageINB = object.getJSONObject("result").getJSONObject("Res").getInteger("Mortgage");
        Integer usable = object.getJSONObject("result").getJSONObject("Res").getInteger("Usable");
        Integer used = object.getJSONObject("result").getJSONObject("Res").getInteger("Used");
        BigInteger balance = object.getJSONObject("result").getBigInteger("Balance");
        BigInteger regular = object.getJSONObject("result").getBigInteger("Regular");
        Integer redeemStartHeight = JSONArray.parseObject(object.getJSONObject("result").getJSONArray("Redeems").get(0).toString()).getInteger("StartHeight");
        BigInteger redeemValue = JSONArray.parseObject(object.getJSONObject("result").getJSONArray("Redeems").get(0).toString()).getBigInteger("Value");
        Integer voteNumber = object.getJSONObject("result").getInteger("Voted");
        Integer lastReceiveVoteAwardTime = object.getJSONObject("result").getInteger("LastReceiveVoteAwardTime");

        AccountRecord record = this.dslContext.select(ACCOUNT.ID)
                .from(ACCOUNT).where(ACCOUNT.ADDRESS.eq(address)).fetchOneInto(AccountRecord.class);

        // Create it if it doesn't exists yet
        if (record == null) {
            logger.info("create account");
            record = this.dslContext.insertInto(ACCOUNT)
//			.set(ACCOUNT.ACCOUNT_NAME,tronAccount.getAccountName().toStringUtf8())
                    .set(ACCOUNT.TYPE, (byte) 1)
                    .set(ACCOUNT.ADDRESS, address)
                    .set(ACCOUNT.CREATE_TIME,Timestamp.valueOf(DateFormatUtils.format(System.currentTimeMillis(),"yyyy-MM-dd HH:mm:ss")))
                    .set(ACCOUNT.BALANCE, balance.doubleValue())
                    .set(ACCOUNT.BANDWIDTH, ULong.valueOf(usable))
                    .set(ACCOUNT.ALLOWANCE, ULong.valueOf(used))
                    .set(ACCOUNT.NONCE, nonce)
                    .set(ACCOUNT.MORTGAGE, mortgageINB.doubleValue())
                    .set(ACCOUNT.REGULAR, regular.doubleValue())
                    .set(ACCOUNT.REDEEM_START_HEIGHT,redeemStartHeight.longValue() )
                    .set(ACCOUNT.REDEEM, redeemValue.doubleValue())
                    .set(ACCOUNT.VOTE_NUMBER,voteNumber.longValue())
                    .set(ACCOUNT.LAST_RECEIVE_VOTE_AWARD_TIME, Timestamp.valueOf(DateFormatUtils.format(lastReceiveVoteAwardTime.longValue()*1000,"yyyy-MM-dd HH:mm:ss")))
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

        //增加账户锁仓信息
        JSONArray storeArray = inbChainService.getAccountInfo(address).getJSONObject("result").getJSONArray("Stores");

        if (storeArray != null & storeArray.size() != 0) {
            StoreDTO store = this.dslContext.select().from(STORE).where(STORE.ADDRESS.eq(address)).fetchOneInto(StoreDTO.class);
            if (store != null) {
                this.dslContext.deleteFrom(STORE)
                        .where(STORE.ADDRESS.eq(address))
                        .execute();
            }
            for (int i = 0; i < storeArray.size(); i++) {
                JSONObject storeObj = JSONArray.parseObject(storeArray.get(i).toString());

                this.dslContext.insertInto(STORE)
                        .set(STORE.NONCE, storeObj.getInteger("Nonce").longValue())
                        .set(STORE.LAST_RECEIVED_HEIGHT, storeObj.getInteger("LastReceivedHeight").longValue())
                        .set(STORE.LOCK_HEIGHT, storeObj.getInteger("LockHeights").longValue())
                        .set(STORE.RECEIVED, storeObj.getInteger("Received").longValue())
                        .set(STORE.START_HEIGHT, storeObj.getInteger("StartHeight").longValue())
//                            .set(STORE.START_TIME, Timestamp.valueOf(storeObj.getInteger("StartTime").toString()))
                        .set(STORE.AMOUNT, storeObj.getInteger("Value").doubleValue())
                        .set(STORE.ADDRESS, address)
                        .execute();

            }
        }


    }


    public Accountdto getActByAddress(AccountCriteria accountCriteria) throws Exception {

        Accountdto result = this.dslContext.select(ACCOUNT.ID, ACCOUNT.ADDRESS, ACCOUNT.MORTGAGE, ACCOUNT.NONCE, ACCOUNT.BALANCE, ACCOUNT.ALLOWANCE.as("used"),
                ACCOUNT.BANDWIDTH.as("usable"),ACCOUNT.REGULAR.as("regular"),ACCOUNT.REDEEM.as("redeemValue"),ACCOUNT.REDEEM_START_HEIGHT,ACCOUNT.VOTE_NUMBER,
                ACCOUNT.LAST_RECEIVE_VOTE_AWARD_TIME)
                .from(ACCOUNT).where(ACCOUNT.ADDRESS.eq(accountCriteria.getAddress())).fetchOneInto(Accountdto.class);
        if (result == null) {
            return new Accountdto();
        }

        List<Store> stores = this.dslContext.select().from(STORE).where(STORE.ADDRESS.eq(accountCriteria.getAddress())).fetchInto(io.inbscan.model.tables.pojos.Store.class);
        List<StoreDTO> storeDTOS = new ArrayList<>();
        for (Store store : stores) {
            StoreDTO storeDTO = new StoreDTO();
            storeDTO.setStartHeight(store.getStartHeight());
            storeDTO.setId(store.getId());
            storeDTO.setAddress(store.getAddress());
            storeDTO.setLastReceivedHeight(store.getLastReceivedHeight());
            storeDTO.setLockHeight(store.getLockHeight());
            storeDTO.setNonce(store.getNonce());
            storeDTO.setReceived(InbConvertUtils.AmountConvert(Double.valueOf(store.getReceived())));
            storeDTO.setAmount(InbConvertUtils.AmountConvert(Double.valueOf(store.getAmount().toString())));
            storeDTOS.add(storeDTO);
        }

        result.setMortgage(InbConvertUtils.AmountConvert(result.getMortgage()));
        result.setBalance(InbConvertUtils.AmountConvert(result.getBalance()));
        result.setRedeemValue(InbConvertUtils.AmountConvert(result.getRedeemValue()));
        result.setRegular(InbConvertUtils.AmountConvert(result.getRegular()));
//        result.setUsed(InbConvertUtils.AmountConvert(result.getUsed()));
        result.setStoreDTO(storeDTOS);
        return result;
    }


    public ListResult<TransferModel, AccountCriteria> listTransfersIn(AccountCriteria criteria) {

        ArrayList<Condition> conditions = new ArrayList<>();


        List<Field<?>> fields = new ArrayList<>(Arrays.asList(TRANSFER.fields()));
        fields.add(TRANSACTION.HASH);

        SelectJoinStep<?> listQuery = this.dslContext.select(fields)
                .from(TRANSACTION)
                .join(TRANSFER).on(TRANSFER.TRANSACTION_ID.eq(TRANSACTION.ID)).and((TRANSFER.TO.eq(criteria.getAddress())));

        Integer totalCount = null;

        if (!criteria.isTrx()) {
            totalCount = this.dslContext.select(ACCOUNT.TRANSFER_TO_COUNT)
                    .from(ACCOUNT)
                    .where(ACCOUNT.ADDRESS.eq(criteria.getAddress()))
                    .fetchOneInto(Integer.class);
        } else {

            conditions.add(TRANSFER.TOKEN.isNull());

            totalCount = this.dslContext.select(DSL.count())
                    .from(TRANSACTION)
                    .join(TRANSFER).on(TRANSFER.TRANSACTION_ID.eq(TRANSACTION.ID)).and((TRANSFER.TO.eq(criteria.getAddress())))
                    .where(conditions)
                    .fetchOneInto(Integer.class);
            ;

        }


        if (totalCount == null) {
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
                .join(TRANSFER).on(TRANSFER.TRANSACTION_ID.eq(TRANSACTION.ID)).and((TRANSFER.FROM.eq(criteria.getAddress())));


        Integer totalCount = null;

        if (!criteria.isTrx()) {
            totalCount = this.dslContext.select(ACCOUNT.TRANSFER_FROM_COUNT)
                    .from(ACCOUNT)
                    .where(ACCOUNT.ADDRESS.eq(criteria.getAddress()))
                    .fetchOneInto(Integer.class);
        } else {

            conditions.add(TRANSFER.TOKEN.isNull());

            totalCount = this.dslContext.select(DSL.count())
                    .from(TRANSFER).where(TRANSFER.FROM.eq(criteria.getAddress()))
                    .fetchOneInto(Integer.class);

        }

        if (totalCount == null) {
            totalCount = 0;
        }

        List<TransferModel> items = listQuery.where(conditions).orderBy(TRANSFER.TIMESTAMP.desc()).limit(criteria.getLimit()).offset(criteria.getOffSet()).fetchInto(TransferModel.class);
        List<TransferModel> item = new ArrayList<>();
        for (TransferModel transferModel : items) {
            double amount = transferModel.getAmount();
            double divideNumber = Math.pow(10, 18);
            BigDecimal b = new BigDecimal(amount / divideNumber);
            double result = b.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
            transferModel.setAmount(result);
            transferModel.setTimestamp(transferModel.getTimestamp().substring(0, transferModel.getTimestamp().indexOf(".")));
            item.add(transferModel);

            BlockDTO blockDTO = this.blockService.getBlockById(transferModel.getBlockId());
            transferModel.setBlockNum(blockDTO.getNum());
        }

        ListResult<TransferModel, AccountCriteria> result = new ListResult<TransferModel, AccountCriteria>(criteria, item, totalCount);

        return result;
    }

}

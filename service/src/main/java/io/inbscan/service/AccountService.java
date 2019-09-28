package io.inbscan.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import io.inbscan.chain.InbChainService;
import io.inbscan.dto.ListModel;
import io.inbscan.dto.ListResult;
import io.inbscan.dto.account.*;
import io.inbscan.dto.block.BlockDTO;
import io.inbscan.dto.token.TokenCriteria;
import io.inbscan.dto.transaction.TransferModel;
import io.inbscan.model.tables.pojos.Store;
import io.inbscan.model.tables.pojos.TokenHolder;
import io.inbscan.model.tables.pojos.Transaction;
import io.inbscan.model.tables.records.AccountRecord;
import io.inbscan.utils.InbConvertUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.inbscan.model.tables.Account.ACCOUNT;
import static io.inbscan.model.tables.Block.BLOCK;
import static io.inbscan.model.tables.BlockChain.BLOCK_CHAIN;
import static io.inbscan.model.tables.Store.STORE;
import static io.inbscan.model.tables.Transaction.TRANSACTION;
import static io.inbscan.model.tables.Transfer.TRANSFER;
import static io.inbscan.model.tables.Token.TOKEN;
import static io.inbscan.model.tables.TokenHolder.TOKEN_HOLDER;


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
        Long mortgageINB = object.getJSONObject("result").getJSONObject("Res").getLong("Mortgage");
        Long mortgageHeight = object.getJSONObject("result").getJSONObject("Res").getLong("Height");
        Long usable = object.getJSONObject("result").getJSONObject("Res").getLong("Usable");
        Long used = object.getJSONObject("result").getJSONObject("Res").getLong("Used");
        BigInteger balance = object.getJSONObject("result").getBigInteger("Balance");
        BigInteger regular = object.getJSONObject("result").getBigInteger("Regular");
        Long redeemStartHeight = JSONArray.parseObject(object.getJSONObject("result").getJSONArray("Redeems").get(0).toString()).getLong("StartHeight");
        BigInteger redeemValue = JSONArray.parseObject(object.getJSONObject("result").getJSONArray("Redeems").get(0).toString()).getBigInteger("Value");
        Long voteNumber = object.getJSONObject("result").getLong("Voted");
        Long lastReceiveVoteAwardHeight = object.getJSONObject("result").getLong("LastReceiveVoteAwardHeight");

        AccountRecord record = this.dslContext.select(ACCOUNT.ID)
                .from(ACCOUNT).where(ACCOUNT.ADDRESS.eq(address)).fetchOneInto(AccountRecord.class);

        // Create it if it doesn't exists yet
        if (record == null) {
            logger.info("create account");
            record = this.dslContext.insertInto(ACCOUNT)
//			.set(ACCOUNT.ACCOUNT_NAME,tronAccount.getAccountName().toStringUtf8())
                    .set(ACCOUNT.TYPE, (byte) 1)
                    .set(ACCOUNT.ADDRESS, address)
                    .set(ACCOUNT.CREATE_TIME, Timestamp.valueOf(DateFormatUtils.format(System.currentTimeMillis(), "yyyy-MM-dd HH:mm:ss")))
                    .set(ACCOUNT.BALANCE, balance.doubleValue())
                    .set(ACCOUNT.USABLE, ULong.valueOf(usable))
                    .set(ACCOUNT.USED, ULong.valueOf(used))
                    .set(ACCOUNT.NONCE, nonce)
                    .set(ACCOUNT.MORTGAGE, mortgageINB.doubleValue())
                    .set(ACCOUNT.MORTGAGE_HEIGHT,mortgageHeight)
                    .set(ACCOUNT.REGULAR, regular.doubleValue())
                    .set(ACCOUNT.REDEEM_START_HEIGHT, redeemStartHeight)
                    .set(ACCOUNT.REDEEM, redeemValue.doubleValue())
                    .set(ACCOUNT.VOTE_NUMBER, voteNumber)
                    .set(ACCOUNT.LAST_RECEIVE_VOTE_AWARD_HEIGHT, lastReceiveVoteAwardHeight)
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
                    .set(ACCOUNT.USABLE, ULong.valueOf(usable))
                    .set(ACCOUNT.USED, ULong.valueOf(used))
                    .set(ACCOUNT.NONCE, nonce)
                    .set(ACCOUNT.MORTGAGE, mortgageINB.doubleValue())
                    .set(ACCOUNT.MORTGAGE_HEIGHT,mortgageHeight)
                    .set(ACCOUNT.REGULAR, regular.doubleValue())
                    .set(ACCOUNT.REDEEM_START_HEIGHT, redeemStartHeight)
                    .set(ACCOUNT.REDEEM, redeemValue.doubleValue())
                    .set(ACCOUNT.VOTE_NUMBER, voteNumber)
                    .set(ACCOUNT.LAST_RECEIVE_VOTE_AWARD_HEIGHT, lastReceiveVoteAwardHeight)
                    .set(ACCOUNT.TRANSFER_TO_COUNT, DSL.select(DSL.count()).from(TRANSFER).where(TRANSFER.TO.eq(address)))
                    .set(ACCOUNT.TRANSFER_FROM_COUNT, DSL.select(DSL.count()).from(TRANSFER).where(TRANSFER.FROM.eq(address)))
                    .where(ACCOUNT.ID.eq(record.getId()))
                    .execute();
        }

        //增加账户锁仓信息
        JSONArray storeArray = inbChainService.getAccountInfo(address).getJSONObject("result").getJSONArray("Stores");

        if (storeArray != null & storeArray.size() != 0) {
            List<StoreDTO> store = this.dslContext.select().from(STORE).where(STORE.ADDRESS.eq(address)).fetchInto(StoreDTO.class);
            if (store.size()!=0 || store != null) {
                this.dslContext.deleteFrom(STORE)
                        .where(STORE.ADDRESS.eq(address))
                        .execute();
            }
            for (int i = 0; i < storeArray.size(); i++) {
                JSONObject storeObj = JSONArray.parseObject(storeArray.get(i).toString());

                this.dslContext.insertInto(STORE)
                        .set(STORE.HASH,storeObj.getString("Hash"))
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

        if(storeArray.size()==0){
            List<StoreDTO> store = this.dslContext.select().from(STORE).where(STORE.ADDRESS.eq(address)).fetchInto(StoreDTO.class);
            if(store !=null) {
                for (StoreDTO storeDTO : store) {
                    this.dslContext.deleteFrom(STORE).where(STORE.ADDRESS.eq(storeDTO.getAddress()));
                }
            }
        }

        //增加代币信息
        JSONObject tokenObject = inbChainService.getAccountTokenInfo(address).getJSONObject("result");
        if (tokenObject != null) {
            JSONArray lightTokenArray = tokenObject.getJSONArray("LightTokens");
            for (int i = 0; i < lightTokenArray.size(); i++) {
                JSONObject lightObject = JSONArray.parseObject(lightTokenArray.get(i).toString());
                String tokenAddress = lightObject.getJSONObject("LT").getString("Address");

                TokenHolder tokenHolder = this.dslContext.select().from(TOKEN_HOLDER).where(TOKEN_HOLDER.HOLDER_ADDRESS.eq(address)).and(TOKEN_HOLDER.TOKEN_ADDRESS.eq(tokenAddress)).fetchOneInto(TokenHolder.class);
                if(tokenHolder == null){
                    this.dslContext.insertInto(TOKEN_HOLDER)
                            .set(TOKEN_HOLDER.HOLDER_ADDRESS,address)
                            .set(TOKEN_HOLDER.TOKEN_ADDRESS,tokenAddress)
                            .set(TOKEN_HOLDER.TOKEN_SYMBOL,lightObject.getJSONObject("LT").getString("Symbol"))
                            .set(TOKEN_HOLDER.BALANCE, lightObject.getInteger("Balance").doubleValue())
                            .execute();
                }

                Token token = this.dslContext.select().from(TOKEN).where(TOKEN.ADDRESS.eq(tokenAddress)).fetchOneInto(Token.class);
                if (token == null) {
                    this.dslContext.insertInto(TOKEN)
                            .set(TOKEN.ADDRESS, tokenAddress)
                            .set(TOKEN.LIGHT_TOKEN_ADDRESS, lightObject.getString("LightTokenAddress"))
                            .set(TOKEN.STATE, lightObject.getInteger("State"))
                            .set(TOKEN.ISSUE_ACCOUNT_ADDRESS, lightObject.getJSONObject("LT").getString("IssueAccountAddress"))
                            .set(TOKEN.OWNER_ADDRESS, lightObject.getJSONObject("LT").getString("Owner"))
                            .set(TOKEN.SYMBOL, lightObject.getJSONObject("LT").getString("Symbol"))
                            .set(TOKEN.TOTAL_SUPPLY, lightObject.getJSONObject("LT").getInteger("TotalSupply").doubleValue())
                            .set(TOKEN.DECIMALS, lightObject.getJSONObject("LT").getInteger("Decimals"))
                            .set(TOKEN.ISSUE_TRANSACTION_HASH, lightObject.getJSONObject("LT").getString("IssueTxHash"))
                            .set(TOKEN.NAME, lightObject.getJSONObject("LT").getString("Name"))
                            .execute();

                    //交易关联代币
                    this.dslContext.update(TRANSACTION)
                            .set(TRANSACTION.TOKEN_NAME,lightObject.getJSONObject("LT").getString("Symbol"))
                            .set(TRANSACTION.TOKEN_ADDRESS, lightObject.getString("LightTokenAddress"))
                            .where(TRANSACTION.HASH.eq(lightObject.getJSONObject("LT").getString("IssueTxHash")))
                            .execute();
                }
                if (token != null) {
                    this.dslContext.update(TOKEN)
                            .set(TOKEN.LIGHT_TOKEN_ADDRESS, lightObject.getString("LightTokenAddress"))
                            .set(TOKEN.STATE, lightObject.getInteger("State"))
                            .set(TOKEN.ISSUE_ACCOUNT_ADDRESS, lightObject.getJSONObject("LT").getString("IssueAccountAddress"))
                            .set(TOKEN.OWNER_ADDRESS, lightObject.getJSONObject("LT").getString("Owner"))
                            .set(TOKEN.SYMBOL, lightObject.getJSONObject("LT").getString("Symbol"))
                            .set(TOKEN.TOTAL_SUPPLY, lightObject.getJSONObject("LT").getInteger("TotalSupply").doubleValue())
                            .set(TOKEN.DECIMALS, lightObject.getJSONObject("LT").getInteger("Decimals"))
                            .set(TOKEN.ISSUE_TRANSACTION_HASH, lightObject.getJSONObject("LT").getString("IssueTxHash"))
                            .set(TOKEN.NAME, lightObject.getJSONObject("LT").getString("Name"))
                            .where(TOKEN.ADDRESS.eq(tokenAddress))
                            .execute();
                    //交易关联代币
                    this.dslContext.update(TRANSACTION)
                            .set(TRANSACTION.TOKEN_NAME,lightObject.getJSONObject("LT").getString("Symbol"))
                            .set(TRANSACTION.TOKEN_ADDRESS, lightObject.getString("LightTokenAddress"))
                            .where(TRANSACTION.HASH.eq(lightObject.getJSONObject("LT").getString("IssueTxHash")))
                            .execute();
                }
            }
        }
    }


    public Accountdto getActByAddress(AccountCriteria accountCriteria) throws Exception {

        Accountdto result = this.dslContext.select(ACCOUNT.ID, ACCOUNT.ADDRESS, ACCOUNT.MORTGAGE, ACCOUNT.NONCE, ACCOUNT.BALANCE, ACCOUNT.USED.as("used"),
                ACCOUNT.USABLE.as("usable"), ACCOUNT.REGULAR.as("regular"), ACCOUNT.REDEEM.as("redeemValue"), ACCOUNT.REDEEM_START_HEIGHT, ACCOUNT.VOTE_NUMBER,
                ACCOUNT.LAST_RECEIVE_VOTE_AWARD_HEIGHT)
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
            storeDTO.setReceived(InbConvertUtils.AmountConvert(Double.valueOf(store.getReceived())));
            storeDTO.setAmount(InbConvertUtils.AmountConvert(Double.valueOf(store.getAmount().toString())));
            storeDTO.setHash(store.getHash());
            storeDTOS.add(storeDTO);
        }

        List<TokenDTO> tokenDTOS = new ArrayList<>();
        List<TokenHolder> tokenHolders = this.dslContext.select().from(TOKEN_HOLDER).where(TOKEN_HOLDER.HOLDER_ADDRESS.eq(accountCriteria.getAddress())).fetchInto(TokenHolder.class);
        for (TokenHolder tokenHolder : tokenHolders) {
            TokenDTO tokenDTO = new TokenDTO();
            Token token = this.dslContext.select().from(TOKEN).where(TOKEN.ADDRESS.eq(tokenHolder.getTokenAddress())).fetchOneInto(Token.class);
            tokenDTO.setIcon(token.getIcon());
            tokenDTO.setSymbol(token.getSymbol());
            tokenDTOS.add(tokenDTO);
        }
        ResDTO resDTO = this.dslContext.select(ACCOUNT.MORTGAGE,ACCOUNT.USABLE,ACCOUNT.USED,ACCOUNT.MORTGAGE_HEIGHT.as("height")).from(ACCOUNT).where(ACCOUNT.ADDRESS.eq(accountCriteria.getAddress())).fetchOneInto(ResDTO.class);
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
        fields.add(TRANSACTION.STATUS);

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
            transferModel.setAmount(InbConvertUtils.AmountConvert(transferModel.getAmount()));
//            transferModel.setTimestamp(transferModel.getTimestamp().substring(0, transferModel.getTimestamp().indexOf(".")));
            item.add(transferModel);

            BlockDTO blockDTO = this.blockService.getBlockById(transferModel.getBlockId());
            transferModel.setBlockNum(blockDTO.getNum());
        }

        ListResult<TransferModel, AccountCriteria> result = new ListResult<TransferModel, AccountCriteria>(criteria, item, totalCount);

        return result;
    }

    public ListModel<Token, TokenCriteria> listToken(TokenCriteria criteria) {
        ArrayList<Condition> conditions = new ArrayList<>();
        long totalCount = 0;
        totalCount = this.dslContext.select(DSL.count()).from(TOKEN).fetchOneInto(Long.class);
        if (criteria.getAddress()!=null) {
            conditions.add(TOKEN.ADDRESS.eq(criteria.getAddress()));
            totalCount = this.dslContext.select(DSL.count()).from(TOKEN).where(TOKEN.ADDRESS.eq(criteria.getAddress())).fetchOneInto(Long.class);
        }

        if (criteria.getSymbol()!=null) {
            conditions.add(TOKEN.SYMBOL.eq(criteria.getSymbol()));
            totalCount = this.dslContext.select(DSL.count()).from(TOKEN).where(TOKEN.SYMBOL.eq(criteria.getSymbol())).fetchOneInto(Long.class);
        }

        SelectOnConditionStep<?> listQuery = (SelectOnConditionStep<?>) this.dslContext.select()
                .from(TOKEN);
        List<Token> items = listQuery.where(conditions).orderBy(TOKEN.ID).limit(criteria.getLimit()).offset(criteria.getOffSet()).fetchInto(Token.class);
        for (Token item : items) {
            Double totalCirculation =0d;
            List<TokenHolder> tokenHolders = this.dslContext.select().from(TOKEN_HOLDER).where(TOKEN_HOLDER.TOKEN_ADDRESS.eq(item.getAddress())).fetchInto(TokenHolder.class);
            for (TokenHolder tokenHolder : tokenHolders) {
                totalCirculation+=tokenHolder.getBalance();
            }
            Transaction transaction = this.dslContext.select().from(TRANSACTION).where(TRANSACTION.HASH.eq(item.getIssueTransactionHash())).fetchOneInto(Transaction.class);
            item.setHolderNumber(tokenHolders.size());
            item.setTotalCirculation(totalCirculation);
            item.setTimestamp(transaction.getTimestamp().getTime());

        }
        ListModel<Token, TokenCriteria> result = new ListModel<Token, TokenCriteria>(criteria, items,totalCount);

        return result;

    }

    public ListModel<Token, TokenCriteria> listAccountToken(TokenCriteria criteria) {

        ArrayList<Condition> conditions = new ArrayList<>();
        long totalCount = 0;
        totalCount = this.dslContext.select(DSL.count()).from(TOKEN).fetchOneInto(Long.class);
        if (criteria.getAddress()!=null) {
            conditions.add(TOKEN_HOLDER.HOLDER_ADDRESS.eq(criteria.getAddress()));
            totalCount = this.dslContext.select(DSL.count()).from(TOKEN_HOLDER).where(TOKEN_HOLDER.HOLDER_ADDRESS.eq(criteria.getAddress())).fetchOneInto(Long.class);
        }

        SelectOnConditionStep<?> listQuery = (SelectOnConditionStep<?>) this.dslContext.select()
                .from(TOKEN_HOLDER);
        List<Token> items = listQuery.where(conditions).orderBy(TOKEN_HOLDER.ID).limit(criteria.getLimit()).offset(criteria.getOffSet()).fetchInto(Token.class);
        ListModel<Token, TokenCriteria> result = new ListModel<Token, TokenCriteria>(criteria, items,totalCount);
        return result;
    }


}

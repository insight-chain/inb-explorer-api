package io.inbscan.chain;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.inbscan.constants.InbConstants;
import io.inbscan.model.tables.records.BlockChainRecord;
import io.inbscan.service.TransactionService;
import io.inbscan.dto.JsonParam;
import io.inbscan.utils.HttpUtil;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static io.inbscan.model.tables.BlockChain.BLOCK_CHAIN;


@Singleton
public class InbChainService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private DSLContext dslContext;

    private InbConstants inbConstants;

    @Inject
    public InbChainService(DSLContext dslContext,InbConstants inbConstants) {
        this.dslContext = dslContext;
        this.inbConstants = inbConstants;
    }

    public JSONObject getTransactionReceipt(String hash) {

        List<Object> params = new ArrayList<>();
        params.add(hash);
        String minbod = "inb_getTransactionReceipt";
        JSONObject object = getResult(params, minbod);

        return object;
//
//        String from = object.getJSONObject("result").getString("from");
//        String to = object.getJSONObject("result").getString("to");
//        String blockNumber = object.getJSONObject("result").getString("blockNumber");
    }

    public JSONObject getResult(List<Object> params, String methodName) {

        JsonParam jsonParam = new JsonParam();
        jsonParam.setJsonrpc("2.0");
        jsonParam.setMethod(methodName);
        jsonParam.setParams(params);
        jsonParam.setId(67L);
        String param = JSONObject.toJSONString(jsonParam);
        List<String> nodes = inbConstants.getNodes();
        String result = null;
        for (String node : nodes) {
           result = HttpUtil.doPost("http://"+node, param);
           if(result != null){
               break;
           }
        }

        JSONObject object = (JSONObject) JSONObject.parse(result);
        return object;

    }


    public BlockChainRecord getBlockChain() {

        return this.dslContext.select(BLOCK_CHAIN.ID).from(BLOCK_CHAIN).fetchOneInto(BlockChainRecord.class);

    }

    /**
     * 根据区块高度获取区块信息
     *
     * @return
     */
    public JSONObject getBlockByNumber(Integer blockNumber) {
        List<Object> params = new ArrayList<>();
        params.add("0x" + Numeric.toHexStringNoPrefix(new BigInteger(blockNumber.toString())));
        params.add(true);
        JSONObject object = getResult(params, "inb_getBlockByNumber");
        return object;
    }

    /**
     * 根据区块hash获取区块信息
     *
     * @return
     */
    public JSONObject getBlockByHash(String blockHash) {
        List<Object> params = new ArrayList<>();
        params.add(blockHash);
        params.add(true);
        JSONObject object = getResult(params, "eth_getBlockByHash");
        return object;
    }

    /**
     * 获取最新高度
     *
     * @return
     */
    public BigInteger getLatestBlockNumber() {
        List<Object> params = new ArrayList<>();
        JSONObject object = getResult(params, "inb_blockNumber");
        BigInteger result = Numeric.decodeQuantity(object.getString("result"));
        return result;
    }

    /**
     * 获取不可逆块高度
     *
     * @return
     */
    public BigInteger getConfirmedBlockNumber() {
        List<Object> params = new ArrayList<>();
        JSONObject object = getResult(params, "inb_confirmedBlockNumber");
        BigInteger result = Numeric.decodeQuantity(object.getString("result"));
        return result;
    }

    /**
     * 获取账户信息
     * @param address
     * @return
     */
    public JSONObject getAccountInfo(String address) {
        List<Object> params = new ArrayList<>();
        params.add(address);
        JSONObject object = getResult(params, "inb_getAccountInfo");
        return object;
    }

    public JSONObject getTransInfo(String hash) {
        List<Object> params = new ArrayList<>();
        params.add(hash);
        JSONObject object = getResult(params, "inb_getTransactionByHash");
        return object;
    }


    /**
     * 获取代币信息
     * @param address
     * @return
     */
    public JSONObject getAccountTokenInfo(String address) {
        List<Object> params = new ArrayList<>();
        params.add(address);
        JSONObject object = getResult(params, "inb_getLightTokenAccountByAccountAddress");
        return object;
    }

    /**
     * 获取所有节点列表
     *
     * @return
     */
    public JSONObject getCandidateNodesInfo() {
        List<Object> params = new ArrayList<>();
        JSONObject object = getResult(params, "inb_getCandidateNodesInfo");
        return object;
    }

    /**
     * 获取超级节点列表
     *
     * @return
     */
    public JSONObject getSuperNodesInfo() {
        List<Object> params = new ArrayList<>();
        JSONObject object = getResult(params, "inb_getSuperNodesInfo");
        return object;
    }

    /**
     * 获取transLog
     *
     * @return
     */
    public List<JSONObject> getTransLog(String hash) {
        JSONObject object = getTransactionReceipt(hash);
        JSONArray logs = object.getJSONObject("result").getJSONArray("logs");
        List<JSONObject> objects = new ArrayList<>(logs.size());
        for (int i = 0; i < logs.size(); i++) {
            JSONObject jsonObject = (JSONObject) logs.get(i);
            objects.add(jsonObject);
        }
        return objects;
    }


}

package io.inbscan.chain;


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

    @Inject
    public InbChainService(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public void getTransactionReceipt(String hash){

        List<Object> params = new ArrayList<>();
        params.add(hash);
        String method = "eth_getTransactionReceipt";
        JSONObject object = getResult(params,method);


        String from = object.getJSONObject("result").getString("from");
        String to = object.getJSONObject("result").getString("to");
        String blockNumber = object.getJSONObject("result").getString("blockNumber");
    }

    public JSONObject getResult(List<Object> params, String methodName){

        JsonParam jsonParam = new JsonParam();
        jsonParam.setJsonrpc("2.0");
        jsonParam.setMethod(methodName);
        jsonParam.setParams(params);
        jsonParam.setId(67L);
        String param = JSONObject.toJSONString(jsonParam);
        String result = HttpUtil.doPost(InbConstants.URL, param);
        JSONObject object = (JSONObject)JSONObject.parse(result);
        return object;

    }


    public BlockChainRecord getBlockChain(){

       return this.dslContext.select(BLOCK_CHAIN.ID).from(BLOCK_CHAIN).fetchOneInto(BlockChainRecord.class);

    }


    /**
     * 获取不可逆块高度
     * @return
     */
    public BigInteger getConfirmedBlockNumber(){
        List<Object> params = new ArrayList<>();
        JSONObject object = getResult(params,"eth_confirmedBlockNumber");
        BigInteger result = Numeric.decodeQuantity(object.getString("result"));
        return result;
    }

    public JSONObject getAccountInfo(String address){
        List<Object> params = new ArrayList<>();
        params.add(address);
        JSONObject object = getResult(params,"eth_getAccountInfo");
        return object;
    }

    /**
     * 获取所有节点列表
     * @return
     */
    public JSONObject getCandidateNodesInfo(){
        List<Object> params = new ArrayList<>();
        JSONObject object = getResult(params,"eth_getCandidateNodesInfo");
        return object;
    }

    /**
     * 获取超级节点列表
     * @return
     */
    public JSONObject getSuperNodesInfo(){
        List<Object> params = new ArrayList<>();
        JSONObject object = getResult(params,"eth_getSuperNodesInfo");
        return object;
    }

}

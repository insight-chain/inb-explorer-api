package io.inbscan;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

public class HttpService {




//    public static JSONObject getResult(List<Object> params, String methodName){
//
//        JsonParam jsonParam = new JsonParam();
//        jsonParam.setJsonrpc("2.0");
//        jsonParam.setMethod(methodName);
//        jsonParam.setParams(params);
//        jsonParam.setId(67L);
//        String param = JSONObject.toJSONString(jsonParam);
//        String result = HttpUtil.doPost(InbConstants.URL, param);
//        JSONObject object = (JSONObject)JSONObject.parse(result);
//        return object;
//
//    }


    public static void main(String[] args) throws ParseException {

//        System.out.println(Numeric.decodeQuantity("0x8a0e"));
//
//        //获取不可逆块高度
//        List<Object> params = new ArrayList<>();
//        JSONObject object = getResult(params,"eth_confirmedBlockNumber");
//        System.out.println(object.getString("result"));
//        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
////        Integer a = Integer.parseInt("4112dceb305cf4b20",16);
//        BigInteger bigInteger = Numeric.decodeQuantity("0x5d19eac1");
//
//        System.out.println(bigInteger.toString());
//
//        String  d  = simpleDateFormat.format(new Date(Long.valueOf(bigInteger.toString()+"000")));
//        System.out.println(d);
//
////        String hex = .toString();
//        System.out.println(new String(Hex.decode("496e7465726e6574204e65656420426c6f636b636861696e2c20494e4220697320636f6d696e67206e6f772e")));


//        System.out.println(fromHexString("0x4112dceb305cf4b20"));
        //获取交易信息
//        String param = "{\"jsonrpc\":\"2.0\", \"method\":\"eth_getTransactionReceipt\",\"params\":[\"0x520d3264ec3b1b5f91ba0efc2b1a08e36b10949dab31aac67eca72e02ef883f7\"],\"id\":67}";
//        String result = doPost(InbConstants.URL, param);
//        JSONObject object = (JSONObject)JSONObject.parse(result);
//        String blockNumber = object.getJSONObject("result").getString("blockNumber");
//        System.out.println(object);


//        List<Object> params = new ArrayList<>();
//        params.add("0x64cf70fcdf06ceb18dafd15fc9807d905cdaa903d7c7111875ede92fb1a0ab4c");
//        JsonParam jsonParam = new JsonParam();
//        jsonParam.setJsonrpc("2.0");
//        jsonParam.setMethod("eth_getTransactionReceipt");
//        jsonParam.setParams(params);
//        jsonParam.setId(67L);
//        String param = JSONObject.toJSONString(jsonParam);
//        String result = HttpService.doPost("http://192.168.1.183:6003/", param);
//        System.out.println(result);

//
        //获取账号信息
//         String param = "{\"jsonrpc\":\"2.0\", \"method\":\"eth_getAccountInfo\",\"params\":[\"0x28352726a219b1c04194603f525ccdc03c6a8628\"],\"id\":67}";
//         String result = doPost(InbConstants.URL, param);
//         JSONObject object = (JSONObject)JSONObject.parse(result);
//         Integer nonce = Integer.valueOf(object.getJSONObject("result").get("Nonce").toString());
//         String mortgagteINB = object.getJSONObject("result").getJSONObject("Resources").getJSONObject("NET").get("MortgagteINB").toString();
//         String binwith =  object.getJSONObject("result").getJSONObject("Resources").getJSONObject("NET").get("Used").toString();
//         BigInteger balance  = object.getJSONObject("result").getBigInteger("Balance");
//         Double balance2 = balance.doubleValue();
//         System.out.println(balance);

//
//        String param1 = "{\"jsonrpc\":\"2.0\", \"method\":\"eth_getBlockByNumber\",\"params\":[\"0x187\",true],\"id\":67}";
//        String result1 = doPost("http://192.168.1.182:6001/", param1);
//        JSONObject object1 = (JSONObject)JSONObject.parse(result1);
//        BigInteger a = new BigInteger(object1.getJSONObject("result").get("reward").toString());
//        System.out.println("a"+a);

////        object1.getJSONObject("result");
//        List<Object> blockParams = new ArrayList<>();
//        blockParams.add("0x520d3264ec3b1b5f91ba0efc2b1a08e36b10949dab31aac67eca72e02ef883f7");
//        JsonParam jsonParamBlock = new JsonParam();
//        jsonParamBlock.setJsonrpc("2.0");
//        jsonParamBlock.setMethod("eth_getTransactionByHash");
//        jsonParamBlock.setParams(blockParams);
//        jsonParamBlock.setId(67L);
//        String blockParam = JSONObject.toJSONString(jsonParamBlock);
//        String block = HttpUtil.doPost(InbConstants.URL, blockParam);
//        System.out.println("a"+block);


//        double d = Double.parseDouble(object1.getJSONObject("result").get("reward").toString());
////        long s = 38051750380517503805l;
////        String s = String.valueOf(Math.pow(10,16));
//        double s = Math.pow(10,16);
//        double sfasf =d/s;
////        BigInteger b = new BigInteger(s);
////        System.out.println("10^16"+a.divide(b));
//        System.out.println(result1);
//        System.out.println();
//        System.out.println(receipt);
//
//
//        List<Object> params = new ArrayList<>();
//        params.add("0x5");
//        params.add(true);
//        JsonParam jsonParam = new JsonParam();
//        jsonParam.setJsonrpc("2.0");
//        jsonParam.setMethod("eth_getBlockByNumber");
//        jsonParam.setParams(params);
//        jsonParam.setId(67L);
//        String param3 = JSONObject.toJSONString(jsonParam);
//        String result3 = doPost(InbConstants.URL, param3);
//        System.out.println("result3"+result3);


//        List<Object> params = new ArrayList<>();
//        params.add("0x69b4f4fa05b4748d200c0aae5f9d56af6a1066652b483a3fb4426af0cc94e02c");
//        params.add(true);
//        JsonParam jsonParam = new JsonParam();
//        jsonParam.setJsonrpc("2.0");
//        jsonParam.setMethod("eth_getBlockByHash");
//        jsonParam.setParams(params);
//        jsonParam.setId(67L);
//        String param3 = JSONObject.toJSONString(jsonParam);
//        String result3 =HttpService.doPost("http://192.168.1.183:6003", param3);
//        JSONObject object = (JSONObject)JSONObject.parse(result3);
//        String timestamp = object.getJSONObject("result").getString("timestamp");
//        System.out.println("result3"+result3);


    }




}



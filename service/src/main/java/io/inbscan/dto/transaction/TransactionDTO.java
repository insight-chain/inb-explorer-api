package io.inbscan.dto.transaction;

import io.inbscan.model.tables.pojos.TransactionLog;
import jnr.ffi.Struct;

import java.util.List;

public class TransactionDTO {

    private Long blockId;

    /**
     * 区块号
     */
    private Long blockNumber;

    /**
     * 交易时间
     */
    private Long timestamp;

    /**
     * 交易hash
     */
    private String hash;

    /**
     * 区块hash
     */
    private String blockHash;

    /**
     * 发送地址
     */
    private String from;

    /**
     * 接收地址
     */
    private String to;

    /**
     * 交易数值
     */
    private double amount;

    /**
     * 数据
     */
    private String input;

    /**
     * 消耗net
     */
    private String bindwith;

    /**
     * 交易类型
     * 1转账
     * 2抵押
     * 3解抵押
     * 4投票
     */
    private Integer type;

    /**
     * 当前地址交易方向
     * 1为from
     * 2为to
     */
    private Integer direction;

    /**
     * 交易状态
     * 1成功
     * 2失败
     */
    private String status;

    private List<TransactionLog> transactionLog;

    //    /**
//     * 交易唯一值
//     */
//    private String nonce;

//    /**
//     * 交易指数
//     */
//    private String transactionIndex;



//    /**
//     * 交易时设置的gas值
//     */
//    private String gas;
//
//    /**
//     * 交易时的gasPrice
//     */
//    private String gasPrice;
//
//    /**
//     * 是否出错,0是正常
//     */
//    private String isError;
//
//    /**
//     * 交易接收状态
//     */
//    private String txreceipt_status;


//
//    /**
//     * 合约地址
//     */
//    private String contractAddress;
//
//    /**
//     * 累计使用的gas
//     */
//    private String cumulativeGasUsed;
//
//    /**
//     * 使用的gas值
//     */
//    private String gasUsed;
//
//    private String confirmations;


    public Long getBlockId() {
        return blockId;
    }

    public void setBlockId(Long blockId) {
        this.blockId = blockId;
    }

    public Long getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(Long blockNumber) {
        this.blockNumber = blockNumber;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getDirection() {
        return direction;
    }

    public void setDirection(Integer direction) {
        this.direction = direction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBindwith() {
        return bindwith;
    }

    public void setBindwith(String bindwith) {
        this.bindwith = bindwith;
    }

    public List<TransactionLog> getTransactionLog() {
        return transactionLog;
    }

    public void setTransactionLog(List<TransactionLog> transactionLog) {
        this.transactionLog = transactionLog;
    }
}

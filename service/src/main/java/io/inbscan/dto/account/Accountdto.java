package io.inbscan.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.inbscan.model.tables.pojos.TokenHolder;

import java.util.List;

public class Accountdto {


    @JsonIgnore
    private Long id;

    private String address;

    /**
     * 账户余额
     */
    private Double balance;

    private Integer nonce;

    /**
     * 锁仓数量
     */
    private Double regular;

    /**
     * 赎回
     */
    private Double redeemValue;

    /**
     * 赎回所在块高度
     */
    private Long redeemStartHeight;

    /**
     * 投票数量
     */
    private Long voteNumber;

    /**
     * 上次领取投票奖励块尕殴打
     */
    private Long lastReceiveVoteAwardHeight;

    /**
     * 资源相关
     * used 已使用资源
     * mortgage 抵押
     * usable 可使用资源
     * height 抵押所在块高度
     */
    private ResDTO res;

    /**
     * 锁仓
     *
     */
    private List<StoreDTO> store;

    /**
     * 代币
     */
    private List<TokenDTO> token;

    private Boolean bstop;

    private Integer resTotal;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    public ResDTO getRes() {
        return res;
    }

    public void setRes(ResDTO res) {
        this.res = res;
    }

    public Integer getNonce() {
        return nonce;
    }

    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }

    public Double getRegular() {
        return regular;
    }

    public void setRegular(Double regular) {
        this.regular = regular;
    }

    public Double getRedeemValue() {
        return redeemValue;
    }

    public void setRedeemValue(Double redeemValue) {
        this.redeemValue = redeemValue;
    }

    public Long getRedeemStartHeight() {
        return redeemStartHeight;
    }

    public void setRedeemStartHeight(Long redeemStartHeight) {
        this.redeemStartHeight = redeemStartHeight;
    }

    public Long getVoteNumber() {
        return voteNumber;
    }

    public void setVoteNumber(Long voteNumber) {
        this.voteNumber = voteNumber;
    }

    public Long getLastReceiveVoteAwardHeight() {
        return lastReceiveVoteAwardHeight;
    }

    public void setLastReceiveVoteAwardHeight(Long lastReceiveVoteAwardHeight) {
        this.lastReceiveVoteAwardHeight = lastReceiveVoteAwardHeight;
    }

    public List<StoreDTO> getStore() {
        return store;
    }

    public void setStore(List<StoreDTO> store) {
        this.store = store;
    }

    public List<TokenDTO> getToken() {
        return token;
    }

    public void setToken(List<TokenDTO> token) {
        this.token = token;
    }

    public Boolean getBstop() {
        return bstop;
    }

    public void setBstop(Boolean bstop) {
        this.bstop = bstop;
    }

    public Integer getResTotal() {
        return resTotal;
    }

    public void setResTotal(Integer resTotal) {
        this.resTotal = resTotal;
    }
}

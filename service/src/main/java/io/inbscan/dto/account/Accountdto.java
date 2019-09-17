package io.inbscan.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public class Accountdto {


    @JsonIgnore
    private Long id;

    private String address;

    /**
     * 账户余额
     */
    private Double balance;
    /**
     * 已使用net
     */
    private Double used;

    /**
     * 可使用net
     */
    private Long usable;

    /**
     * 已抵用INB
     */
    private Double mortgage;

    private Integer nonce;

    private Double regular;

    private Double redeemValue;

    private Long redeemStartHeight;

    private Long voteNumber;

    private Long lastReceiveVoteAwardTime;

    private List<StoreDTO> storeDTO;


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

    public Double getUsed() {
        return used;
    }

    public void setUsed(Double used) {
        this.used = used;
    }

    public Long getUsable() {
        return usable;
    }

    public void setUsable(Long usable) {
        this.usable = usable;
    }

    public Double getMortgage() {
        return mortgage;
    }

    public void setMortgage(Double mortgage) {
        this.mortgage = mortgage;
    }

    public Integer getNonce() {
        return nonce;
    }

    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }

    public List<StoreDTO> getStoreDTO() {
        return storeDTO;
    }

    public void setStoreDTO(List<StoreDTO> storeDTO) {
        this.storeDTO = storeDTO;
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

    public Long getLastReceiveVoteAwardTime() {
        return lastReceiveVoteAwardTime;
    }

    public void setLastReceiveVoteAwardTime(Long lastReceiveVoteAwardTime) {
        this.lastReceiveVoteAwardTime = lastReceiveVoteAwardTime;
    }
}

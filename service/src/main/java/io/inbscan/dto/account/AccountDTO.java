package io.inbscan.dto.account;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AccountDTO {


    @JsonIgnore
    private Long id;

    /**
     * 账户余额
     */
    private double balance;
    /**
     * 已使用net
     */
    private long used;

    /**
     * 可使用net
     */
    private long usableness;

    /**
     * 已抵用INB
     */
    private String mortgagte;

    private Integer nonce;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }


    public long getUsed() {
        return used;
    }

    public void setUsed(long used) {
        this.used = used;
    }

    public long getUsableness() {
        return usableness;
    }

    public void setUsableness(long usableness) {
        this.usableness = usableness;
    }

    public String getMortgagte() {
        return mortgagte;
    }

    public void setMortgagte(String mortgagte) {
        this.mortgagte = mortgagte;
    }

    public Integer getNonce() {
        return nonce;
    }

    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }
}

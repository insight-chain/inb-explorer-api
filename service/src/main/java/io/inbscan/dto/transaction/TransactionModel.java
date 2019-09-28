package io.inbscan.dto.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.inbscan.model.tables.pojos.TransactionLog;


import java.util.List;

/**
 * @author nicholas
 *
 */
public class TransactionModel {

	@JsonIgnore
	private long id;
	
	private String hash;
	
	private String from;

	private String to;
	
	private int type;
	
	private long block;

	private double amount;
	
	private Long timestamp;
	
	private boolean confirmed;
	
	private Object contract;

	private String bindwith;

	private String status;

	private String input;

	private List<TransactionLog> log;


	
	
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Object getContract() {
		return contract;
	}

	public void setContract(Object contract) {
		this.contract = contract;
	}

	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	
	public long getBlock() {
		return block;
	}

	public void setBlock(long block) {
		this.block = block;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	public int getType() {
		return this.type;
	}
	
	public int getTypeInt(){
		return this.type;
	}

	public void setType(int type) {
		this.type = type;
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

	public String getBindwith() {
		return bindwith;
	}

	public void setBindwith(String bindwith) {
		this.bindwith = bindwith;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getInput() {
		return input;
	}

	public void setInput(String input) {
		this.input = input;
	}

	public List<TransactionLog> getLog() {
		return log;
	}

	public void setLog(List<TransactionLog> log) {
		this.log = log;
	}
}

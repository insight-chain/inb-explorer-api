package io.inbscan.dto.transaction;

public class TransferModel {

	private String hash;

	private long blockId;

	private long blockNum;
	
	private String from;
	
	private String to;
	
	private double amount;
	
	private String token;

	private Long timestamp;

	private String bindwith;

	private Integer type;

	private String status;
	
	
	
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public long getBlockId() {
		return blockId;
	}

	public void setBlockId(long blockId) {
		this.blockId = blockId;
	}

	public Long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

//	public void setTimestamp(Timestamp timestamp) {
//		this.timestamp = timestamp;
//	}
//
//	public long getTimestamp() {
//		if (timestamp==null) {
//			return 0;
//		}
//		return timestamp.getTime()/1000;
//	}
	
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

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getBindwith() {
		return bindwith;
	}

	public void setBindwith(String bindwith) {
		this.bindwith = bindwith;
	}


	public long getBlockNum() {
		return blockNum;
	}

	public void setBlockNum(long blockNum) {
		this.blockNum = blockNum;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}

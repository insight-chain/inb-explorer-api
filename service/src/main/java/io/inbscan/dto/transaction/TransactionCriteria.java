package io.inbscan.dto.transaction;

import io.inbscan.dto.CommonCriteriaDTO;

import java.util.HashMap;
import java.util.Map;

public class TransactionCriteria extends CommonCriteriaDTO {
	
	private Integer block;

	private String hash;

	private String address;

	private String type;

	private String tokenAddress;

	private Integer transType;
	
	
	@Override
	public Map<String, String> params() {
		
		HashMap<String, String> map = new HashMap<>();
		
		if (block!=null) {
			map.put("block", String.valueOf(block));
		}
		
		return map;
	}


	public Integer getBlock() {
		return block;
	}


	public void setBlock(Integer block) {
		this.block = block;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTokenAddress() {
		return tokenAddress;
	}

	public void setTokenAddress(String tokenAddress) {
		this.tokenAddress = tokenAddress;
	}

	public Integer getTransType() {
		return transType;
	}

	public void setTransType(Integer transType) {
		this.transType = transType;
	}
}

package io.inbscan.dto.transaction;

import io.inbscan.dto.CommonCriteriaDTO;

import java.util.HashMap;
import java.util.Map;

public class TransactionCriteria extends CommonCriteriaDTO {
	
	private Integer block;

	private String hash;

	private String address;
	
	
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
}

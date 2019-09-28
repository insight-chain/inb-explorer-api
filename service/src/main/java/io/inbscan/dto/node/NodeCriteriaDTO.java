package io.inbscan.dto.node;

import io.inbscan.dto.CommonCriteriaDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class NodeCriteriaDTO extends CommonCriteriaDTO {
	
	private String country;
	
	private String ip;

	private String address;
	
	
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public Map<String, String> params() {

		HashMap<String, String> params = new HashMap<String, String>();
		
		if (StringUtils.isNotBlank(this.country)) {
			params.put("country", this.country);
		}
		
		if (StringUtils.isNotBlank(this.country)) {
			params.put("ip", this.ip);	
		}
		
		
		return params;
	}
	
	
}

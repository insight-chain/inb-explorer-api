package io.inbscan.dto;

import java.util.Map;

public abstract class CommonCriteriaDTO {
	
	private Integer limit;

	private Integer page;
	
	
	public int getOffSet(){
		
		if (getPage()==1){
			return 0;
		}else{
			return getLimit() * (getPage()-1);
		}
		

		
	}
	
	public Integer getLimit() {
		return limit;
	}
	
	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	
	
	public Integer getPage() {
		if (page==null || page==0) {
			return 1;
		}
		return page;
	}
	
	public void setPage(Integer page) {
		this.page = page;
	}

	public abstract Map<String, String> params();
	
}

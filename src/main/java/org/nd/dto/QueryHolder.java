package org.nd.dto;

import java.util.List;
import java.util.Map;

public class QueryHolder {
	
	Integer page;
	Integer size;
	String sortField;
	String sortOrder;
	String filter;
	String extract;
	Integer totalElement;
	
			
	public QueryHolder() {
		super();
	}

	
	public QueryHolder(Map<String, List<String>> queryMap) {

		try {
		    extract = queryMap.get("extract").get(0);
		} catch (Exception e) {}
		
	}
	
	

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getSortField() {
		return sortField;
	}
	public void setSortField(String sortField) {
		this.sortField = sortField;
	}
	public String getSortOrder() {
		return sortOrder;
	}
	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}
	public String getFilter() {
		return filter;
	}
	public void setFilter(String filter) {
		this.filter = filter;
	}

	public Integer getTotalElement() {
		return totalElement;
	}

	public void setTotalElement(Integer totalElement) {
		this.totalElement = totalElement;
	}

	public String getExtract() {
		return extract;
	}

	public void setExtract(String extract) {
		this.extract = extract;
	}


	@Override
	public String toString() {
	    return "QueryHolder [page=" + page + ", size=" + size + ", sortField=" + sortField + ", sortOrder="
		    + sortOrder + ", filter=" + filter + ", extract=" + extract + ", totalElement=" + totalElement
		    + "]";
	}



}

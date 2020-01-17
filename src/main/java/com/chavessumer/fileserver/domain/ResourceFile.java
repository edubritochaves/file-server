package com.chavessumer.fileserver.domain;

public class ResourceFile {

	private String context;
	private String url;
	private Boolean cache;
	
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public Boolean isCache() {
		return cache;
	}
	public void setCache(Boolean cache) {
		this.cache = cache;
	}
	
	
}

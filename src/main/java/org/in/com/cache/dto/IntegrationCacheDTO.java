package org.in.com.cache.dto;

public class IntegrationCacheDTO {
	private int id;
	private String code;
	private String integrationType;
	private String account;
	private String accessToken;
	private String accessURL;
	private String provider;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getIntegrationType() {
		return integrationType;
	}

	public void setIntegrationType(String integrationType) {
		this.integrationType = integrationType;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getAccessURL() {
		return accessURL;
	}

	public void setAccessURL(String accessURL) {
		this.accessURL = accessURL;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

}

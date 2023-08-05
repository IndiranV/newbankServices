package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.in.com.dto.MenuDTO;

public class AuthCacheDTO implements Serializable {
	private static final long serialVersionUID = 3582009960210752699L;
	private String authToken;
	private String apiToken;
	private String userCacheCode;
	private String nativeNamespaceCode;
	private String namespaceCode;
	private String deviceMediumCode;
	private String authenticationTypeCode;
	private List<MenuDTO> privilegesDTO;
	private int userCustomerId;
	private Map<String, String> additionalAttribute;

	public String getUserCacheCode() {
		return userCacheCode;
	}

	public void setUserCacheCode(String userCacheCode) {
		this.userCacheCode = userCacheCode;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public String getApiToken() {
		return apiToken;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public String getNativeNamespaceCode() {
		return nativeNamespaceCode;
	}

	public void setNativeNamespaceCode(String nativeNamespaceCode) {
		this.nativeNamespaceCode = nativeNamespaceCode;
	}

	public String getNamespaceCode() {
		return namespaceCode;
	}

	public void setNamespaceCode(String namespaceCode) {
		this.namespaceCode = namespaceCode;
	}

	public String getDeviceMediumCode() {
		return deviceMediumCode;
	}

	public void setDeviceMediumCode(String deviceMediumCode) {
		this.deviceMediumCode = deviceMediumCode;
	}

	public String getAuthenticationTypeCode() {
		return authenticationTypeCode;
	}

	public void setAuthenticationTypeCode(String authenticationTypeCode) {
		this.authenticationTypeCode = authenticationTypeCode;
	}

	public List<MenuDTO> getPrivileges() {
		return privilegesDTO;
	}

	public void setPrivilegesDTO(List<MenuDTO> privilegesDTO) {
		this.privilegesDTO = privilegesDTO;
	}

	public int getUserCustomerId() {
		return userCustomerId;
	}

	public void setUserCustomerId(int userCustomerId) {
		this.userCustomerId = userCustomerId;
	}

	public Map<String, String> getAdditionalAttribute() {
		return additionalAttribute;
	}

	public void setAdditionalAttribute(Map<String, String> additionalAttribute) {
		this.additionalAttribute = additionalAttribute;
	}

}

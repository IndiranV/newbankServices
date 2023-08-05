package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.List;

public class UserCacheDTO implements Serializable {
	private static final long serialVersionUID = 6522755882720296790L;
	private int id;
	private int activeFlag;
	private String code;
	private String username;
	private String email;
	private String mobile;
	private String name;
	private String lastname;
	private int groupId;
	private String paymentTypeCode;
	private String roleCode;
	private int organizationId;
	private String apiToken;
	private List<String> userTags;
	private String contactVerifiedFlag;
	private String passwordUpdatedAt;
	private String additionalAttribute;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getRoleCode() {
		return roleCode;
	}

	public void setRoleCode(String roleCode) {
		this.roleCode = roleCode;
	}

	public int getOrganizationId() {
		return organizationId;
	}

	public void setOrganizationId(int organizationId) {
		this.organizationId = organizationId;
	}

	public String getPaymentTypeCode() {
		return paymentTypeCode;
	}

	public void setPaymentTypeCode(String paymentTypeCode) {
		this.paymentTypeCode = paymentTypeCode;
	}

	public String getApiToken() {
		return apiToken;
	}

	public void setApiToken(String apiToken) {
		this.apiToken = apiToken;
	}

	public List<String> getUserTags() {
		return userTags;
	}

	public void setUserTags(List<String> userTags) {
		this.userTags = userTags;
	}

	public String getContactVerifiedFlag() {
		return contactVerifiedFlag;
	}

	public void setContactVerifiedFlag(String contactVerifiedFlag) {
		this.contactVerifiedFlag = contactVerifiedFlag;
	}

	public String getPasswordUpdatedAt() {
		return passwordUpdatedAt;
	}

	public void setPasswordUpdatedAt(String passwordUpdatedAt) {
		this.passwordUpdatedAt = passwordUpdatedAt;
	}

	public String getAdditionalAttribute() {
		return additionalAttribute;
	}

	public void setAdditionalAttribute(String additionalAttribute) {
		this.additionalAttribute = additionalAttribute;
	}

}

package org.in.com.cache.dto;

import java.io.Serializable;

public class MenuEventCacheDTO implements Serializable {
	private static final long serialVersionUID = 5584012635244023201L;
	private int id;
	private int activeFlag;
	private String code;
	private String name;
	private int permissionFlag;
	private int enabledFlag;
	private String operationCode;
	private String attr1Value;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getPermissionFlag() {
		return permissionFlag;
	}

	public void setPermissionFlag(int permissionFlag) {
		this.permissionFlag = permissionFlag;
	}

	public int getEnabledFlag() {
		return enabledFlag;
	}

	public void setEnabledFlag(int enabledFlag) {
		this.enabledFlag = enabledFlag;
	}

	public String getOperationCode() {
		return operationCode;
	}

	public void setOperationCode(String operationCode) {
		this.operationCode = operationCode;
	}

	public String getAttr1Value() {
		return attr1Value;
	}

	public void setAttr1Value(String attr1Value) {
		this.attr1Value = attr1Value;
	}

}

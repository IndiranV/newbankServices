package org.in.com.cache.dto;

import java.io.Serializable;

public class FareRuleCacheDTO implements Serializable {

	private static final long serialVersionUID = -3648468894209054793L;

	private int id;
	private String code;
	private String name;
	private int activeFlag;
	private String stateCode;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getStateCode() {
		return stateCode;
	}

	public void setStateCode(String stateCode) {
		this.stateCode = stateCode;
	}

}

package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.List;

public class CancellationTermsCacheDTO implements Serializable {
	private static final long serialVersionUID = -2255430749222792852L;
	private int id;
	private int activeFlag;
	private String code;
	private String name;
	private String policyGroupKey;
	private List<CancellationPolicyCacheDTO> policyList;

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

	public String getPolicyGroupKey() {
		return policyGroupKey;
	}

	public void setPolicyGroupKey(String policyGroupKey) {
		this.policyGroupKey = policyGroupKey;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public List<CancellationPolicyCacheDTO> getPolicyList() {
		return policyList;
	}

	public void setPolicyList(List<CancellationPolicyCacheDTO> policyList) {
		this.policyList = policyList;
	}

}

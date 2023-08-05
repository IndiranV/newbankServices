package org.in.com.cache.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;

public class DiscountSpecialCriteriaCacheDTO implements Serializable {

	private static final long serialVersionUID = 1L;
	private int id;
	private String code;
	private List<GroupDTO> userGroups;
	private List<ScheduleDTO> schedules;
	private BigDecimal maxAmount;
	private boolean percentageFlag;
	private int activeFlag;

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

	public List<GroupDTO> getUserGroups() {
		return userGroups;
	}

	public void setUserGroups(List<GroupDTO> userGroups) {
		this.userGroups = userGroups;
	}

	public List<ScheduleDTO> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<ScheduleDTO> schedules) {
		this.schedules = schedules;
	}

	public BigDecimal getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(BigDecimal maxAmount) {
		this.maxAmount = maxAmount;
	}

	public boolean isPercentageFlag() {
		return percentageFlag;
	}

	public void setPercentageFlag(boolean percentageFlag) {
		this.percentageFlag = percentageFlag;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

}

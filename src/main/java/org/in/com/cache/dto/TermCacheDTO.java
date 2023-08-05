package org.in.com.cache.dto;

import java.util.List;

import org.in.com.dto.ScheduleDTO;

public class TermCacheDTO {
	private int id;
	private String code;
	private String name;
	private int activeFlag;
	private int sequenceId;
	private List<String> tag;
	private List<ScheduleDTO> scheduleCode;
	private String transactionTypeCode;

	public String getTransactionTypeCode() {
		return transactionTypeCode;
	}

	public void setTransactionTypeCode(String transactionTypeCode) {
		this.transactionTypeCode = transactionTypeCode;
	}

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

	public int getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(int sequenceId) {
		this.sequenceId = sequenceId;
	}

	public List<String> getTag() {
		return tag;
	}

	public void setTag(List<String> tag) {
		this.tag = tag;
	}

	public List<ScheduleDTO> getScheduleCode() {
		return scheduleCode;
	}

	public void setScheduleCode(List<ScheduleDTO> scheduleCode) {
		this.scheduleCode = scheduleCode;
	}

}

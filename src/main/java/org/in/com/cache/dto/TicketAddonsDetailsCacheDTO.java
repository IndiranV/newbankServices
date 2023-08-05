package org.in.com.cache.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class TicketAddonsDetailsCacheDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private int refferenceId;
	private String refferenceCode;
	private String seatCode;
	private String addonsTypeCode;
	private BigDecimal value;

	public int getRefferenceId() {
		return refferenceId;
	}

	public void setRefferenceId(int refferenceId) {
		this.refferenceId = refferenceId;
	}

	public String getRefferenceCode() {
		return refferenceCode;
	}

	public void setRefferenceCode(String refferenceCode) {
		this.refferenceCode = refferenceCode;
	}

	public String getSeatCode() {
		return seatCode;
	}

	public void setSeatCode(String seatCode) {
		this.seatCode = seatCode;
	}

	public String getAddonsTypeCode() {
		return addonsTypeCode;
	}

	public void setAddonsTypeCode(String addonsTypeCode) {
		this.addonsTypeCode = addonsTypeCode;
	}

	public BigDecimal getValue() {
		return value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

}

package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.List;

public class BookingCacheDTO implements Serializable {
	private static final long serialVersionUID = -2463462794581341438L;
	private String code;
	private List<TicketCacheDTO> ticketCacheDTO;

	private String transactionDate;

	private String couponCode;

	// Payment Process
	private String paymentGatewayPartnerCode;
	private boolean paymentGatewayProcessFlag;
	private int transactionModeId;
	private String namespaceCode;

	public List<TicketCacheDTO> getTicketCacheDTO() {
		return ticketCacheDTO;
	}

	public void setTicketCacheDTO(List<TicketCacheDTO> ticketCacheDTO) {
		this.ticketCacheDTO = ticketCacheDTO;
	}

	public String getTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(String transactionDate) {
		this.transactionDate = transactionDate;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public String getPaymentGatewayPartnerCode() {
		return paymentGatewayPartnerCode;
	}

	public void setPaymentGatewayPartnerCode(String paymentGatewayPartnerCode) {
		this.paymentGatewayPartnerCode = paymentGatewayPartnerCode;
	}

	public boolean isPaymentGatewayProcessFlag() {
		return paymentGatewayProcessFlag;
	}

	public void setPaymentGatewayProcessFlag(boolean paymentGatewayProcessFlag) {
		this.paymentGatewayProcessFlag = paymentGatewayProcessFlag;
	}

	public int getTransactionModeId() {
		return transactionModeId;
	}

	public void setTransactionModeId(int transactionModeId) {
		this.transactionModeId = transactionModeId;
	}

	public String getNamespaceCode() {
		return namespaceCode;
	}

	public void setNamespaceCode(String namespaceCode) {
		this.namespaceCode = namespaceCode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}

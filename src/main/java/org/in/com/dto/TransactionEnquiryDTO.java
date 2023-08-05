package org.in.com.dto;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Data;

import org.in.com.exception.ErrorCode;

import com.google.gson.Gson;

@Data
public class TransactionEnquiryDTO {

	private String transactionCode;
	private String merchantReturnUrl;
	private PaymentGatewayCredentialsDTO gatewayCredentials;
	private BigDecimal amount;
	private String gatewayTransactionCode;
	private ErrorCode status;
	private String orderCode;
	private String responseRecevied;
	private String namesapceUrl;
	private String responseURL;

	/** Data from gateway will be bound to this object */
	private Map<String, String> gatewayReponse;

	public String toJSON() {
		Gson gson = new Gson();
		if (this != null) {
			gson.toJson(this);
		}
		return gson.toJson(this);
	}

}

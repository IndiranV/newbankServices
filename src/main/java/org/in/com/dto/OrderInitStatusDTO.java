package org.in.com.dto;

import java.util.Map;

import org.in.com.exception.ErrorCode;

import lombok.Data;

@Data
public class OrderInitStatusDTO {

	private String paymentRequestUrl;
	private String transactionCode;
	private String gatewayCode;
	private Map<String, String> gatewayInputDetails;
	private ErrorCode status;

}

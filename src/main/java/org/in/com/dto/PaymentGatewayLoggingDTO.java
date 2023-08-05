package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentGatewayLoggingDTO extends BaseDTO<PaymentGatewayLoggingDTO>{
	
	
	private String logData;
	
}

package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentGatewayProviderDTO extends BaseDTO<PaymentGatewayProviderDTO> {
	private String serviceName;
}

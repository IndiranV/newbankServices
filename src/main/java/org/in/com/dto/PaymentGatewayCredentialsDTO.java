package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentGatewayCredentialsDTO extends BaseDTO<PaymentPreTransactionDTO> {
	private PaymentGatewayProviderDTO gatewayProvider;
	private String accessCode;
	private String accessKey;
	private String attr1;
	private String propertiesFileName;
	private String pgReturnUrl;
	private String appReturnUrl;
	private String accountOwner;
}

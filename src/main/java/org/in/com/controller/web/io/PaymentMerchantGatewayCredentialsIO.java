package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentMerchantGatewayCredentialsIO extends BaseIO {
	private PaymentGatewayProviderIO gatewayProvider;
	private String returnUrl;
	private String accessCode;
	private String accessKey;
	private String attr1;
	private String propertiesFileName;
	private String accountOwner;
}

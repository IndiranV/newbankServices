package org.in.com.controller.pg.io;

import java.math.BigDecimal;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.PaymentGatewayProviderIO;
import org.in.com.controller.web.io.PaymentMerchantGatewayCredentialsIO;
import org.in.com.controller.web.io.UserIO;

import lombok.Data;

@Data
public class PaymentPreTransactionV2IO {
	private String code;
	private String orderCode;
	private String status;
	private UserIO user;
	private PaymentGatewayProviderIO gatewayProvider;
	private PaymentMerchantGatewayCredentialsIO gatewayCredentials;
	private BaseIO orderType;
	private BigDecimal amount;
	private BigDecimal serviceCharge;
	private String deviceMedium;
	private String transactionCode;
	private BaseIO gatewayTransactionType;
}

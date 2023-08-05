package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentGatewayPartnerIO extends BaseIO {
	private String apiProviderCode;
	private String offerNotes;
	private int precedence;
	private List<String> offerTerms;
	private BigDecimal serviceCharge;
	private PaymentModeIO paymentMode;
	private PaymentGatewayProviderIO gatewayProvider;

}

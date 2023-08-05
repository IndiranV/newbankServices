package org.in.com.controller.commerce.io;

import java.util.List;

import lombok.Data;

import org.in.com.controller.web.io.PaymentGatewayPartnerIO;

@Data
public class PaymentModeIO {
	private String code;
	private String name;
	private List<PaymentGatewayPartnerIO> paymentGatewayPartner;
}

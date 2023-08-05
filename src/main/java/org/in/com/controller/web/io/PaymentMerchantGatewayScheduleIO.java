package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentMerchantGatewayScheduleIO extends BaseIO {
	private String fromDate;
	private String toDate;
	private List<String> deviceMedium;
	private GroupIO group;
	private PaymentGatewayPartnerIO gatewayPartner;
	private BigDecimal serviceCharge;
	private BaseIO transactionType;
	private int precedence;
}

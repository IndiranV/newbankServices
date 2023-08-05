package org.in.com.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PendingOrderDTO extends BaseDTO<PendingOrderDTO> {
	private String orderCode;
	private String status;
	private BigDecimal amount;
	private BigDecimal serviceCharge;
	private PaymentGatewayPartnerDTO paymentGatewayPartner;
	private TicketDTO dto;
}

package org.in.com.controller.busbuddy.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripTransactionIO extends BaseIO {
	private String syncId;
	private float litres;
	private BigDecimal pricePerLitre;
	private BigDecimal totalAmount;
	private String billNumber;
	private float odometer;
	private BigDecimal amount;
	private BaseIO user;
	private String remarks;
	private String paymentContact;
	private String vendorContact;
}

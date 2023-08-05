package org.in.com.controller.pg.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class RefundIO {
	private String transactionCode;
	private String orderCode;
	private BigDecimal amount;
	private String orderType;

}

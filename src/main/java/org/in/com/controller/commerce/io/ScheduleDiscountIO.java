package org.in.com.controller.commerce.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleDiscountIO extends BaseIO {
	private BigDecimal discountValue;
	private int percentageFlag;
	private int advanceBookingDays;
	private int femaleDiscountFlag;
}
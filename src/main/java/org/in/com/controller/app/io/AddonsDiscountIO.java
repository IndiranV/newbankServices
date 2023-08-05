package org.in.com.controller.app.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddonsDiscountIO extends BaseIO {
	private double value;
	private double maxValue;
	private String message;
	private boolean percentageFlag;

}
package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserPaymentPreferencesIO extends BaseIO {
	private String preferencesType;
	private int travelDateFlag;
	private String frequencyMode;
	private String dayOfWeek;
	private int dayOfMonth;
	private String dayOfTime;
	private String emailAddress;
}

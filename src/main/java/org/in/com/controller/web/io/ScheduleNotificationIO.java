package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleNotificationIO extends BaseIO {
	private String mobileNumber;
	private int minutes;
	private ScheduleIO schedule;
}

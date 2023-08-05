package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleNotificationDTO extends BaseDTO<ScheduleNotificationDTO> {
	private String mobileNumber;
	private int minutes;
	private ScheduleDTO schedule;
}

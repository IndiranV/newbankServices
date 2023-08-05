package org.in.com.controller.busbuddy.io;

import lombok.Data;

@Data
public class ScheduleIO {
	private String code;
	private String name;
	private String serviceNumber;
	private String displayName;
	private BusIO bus;

}
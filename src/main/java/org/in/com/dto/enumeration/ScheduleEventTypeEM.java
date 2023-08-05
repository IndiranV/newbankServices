package org.in.com.dto.enumeration;

public enum ScheduleEventTypeEM {

	SCHEDULE("SCH", 1, "Schedule"), 
	SCHEDULE_STATION("SCHST", 2, "Schedule station"), 
	SCHEDULE_STATION_POINT("SCHSTP", 3, "Schedule station point"), 
	SCHEDULE_BUS("SCHB", 4, "Schedule bus"), 
	SCHEDULE_STAGE("SCHSG", 5, "Schedule stage fare"), 
	SCHEDULE_BUS_OVERRIDE("SCHBO", 6, "Schedule bus override"), 
	SCHEDULE_CANCELLATION_TERMS("SCHCT", 7, "Schedule cancellation terms"), 
	SCHEDULE_CONTROL("SCHC", 8, "Schedule control"), 
	SCHEDULE_DYANMIC_STAGE_FARE("SCHDSF", 9, "Schedule dynamic stage fare"), 
	SCHEDULE_FARE_AUTO_OVERRIDE("SCHFAO", 10, "Schedule fare auto override"), 
	SCHEDULE_SEAT_AUTO_RELEASE("SCHSAR", 11, "Schedule seat auto release"), 
	SCHEDULE_SEAT_FARE("SCHSF", 12, "Schedule seat fare"), 
	SCHEDULE_SEAT_PREFERENCES("SCHSP", 13, "Schedule seat preference"), 
	SCHEDULE_SEAT_VISIBILITY("SCHSV", 14, "Schedule seat visibility"), 
	SCHEDULE_TIME_OVERRIDE("SCHTO", 15, "Schedule time override");

	private final int id;
	private final String code;
	private final String name;

	private ScheduleEventTypeEM(String code, int id, String name) {
		this.code = code;
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static ScheduleEventTypeEM getScheduleEventTypEM(int id) {
		ScheduleEventTypeEM[] values = values();
		for (ScheduleEventTypeEM eventType : values) {
			if (eventType.getId() == id) {
				return eventType;
			}
		}
		return null;
	}

	public static ScheduleEventTypeEM getScheduleEventTypeEM(String Code) {
		ScheduleEventTypeEM[] values = values();
		for (ScheduleEventTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}

}

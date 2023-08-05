package org.in.com.dto.enumeration;

public enum SeatStatusEM {

	AVAILABLE_MALE("AM", 1, "Male"), 
	AVAILABLE_FEMALE("AF", 2, "Female"), 
	AVAILABLE_ALL("AL", 3, "Available for All"), 
	BOOKED("BO", 4, "Booked Seat"), 
	ALLOCATED_YOU("AY", 5, "Allocated for You"), 
	ALLOCATED_OTHER("AO",6, "Blocked,Allocated to Others"), 
	BLOCKED("BL", 7, "Blocked Seat"),
	TEMP_BLOCKED("TBL", 8, "Temp Blocked Seats"),
	PHONE_BLOCKED("PBL", 9, " Phone Blocked Seat"),
	UN_KNOWN("UK", 10, "Un Known,yet identified"),
	QUOTA_SEAT("QS", 11, "Quota Seat"),
	SOCIAL_DISTANCE_BLOCK("SDBL", 12, "Social Distancing Blocked");

	private final int id;
	private final String code;
	private final String description;

	private SeatStatusEM(String code, int id, String description) {
		this.code = code;
		this.id = id;
		this.description = description;
	}

	public Integer getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}

	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	public String toString() {
		return code + " : " + id;
	}

	public static SeatStatusEM getSeatStatusEM(int id) {
		SeatStatusEM[] values = values();
		for (SeatStatusEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static SeatStatusEM getSeatStatusEM(String Code) {
		SeatStatusEM[] values = values();
		for (SeatStatusEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}
}

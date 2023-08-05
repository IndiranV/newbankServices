package org.in.com.dto.enumeration;

public enum TripStatusEM {

	TRIP_NA("TPNA", 0, "Trip Not Available"),
	TRIP_OPEN("TPO", 1, "Trip Open"),
	TRIP_CLOSED("TPC", 2, "Trip Closed"),
	TRIP_YET_OPEN("TPY", 3, "Trip Yet Open"),
	TRIP_CANCELLED("TCA", 4, "Trip Cancelled");

	private final int id;
	private final String code;
	private final String name;

	private TripStatusEM(String code, int id, String name) {
		this.code = code;
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return code + " : " + id + ":" + name;
	}

	public static TripStatusEM getTripStatusEM(int id) {
		TripStatusEM[] values = values();
		for (TripStatusEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		System.out.println("Trip Status Not Found id: " + id);
		return null;
	}

	public static TripStatusEM getTripStatusEM(String Code) {
		TripStatusEM[] values = values();
		for (TripStatusEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		System.out.println("Trip Status Not Found Code: " + Code);
		return null;
	}
}

package org.in.com.dto.enumeration;

public enum TripActivitiesEM {

	SOCIAL_DISTANCING("SLDTG", 1, "Social Distancing"),
	DYNAMIC_PRICING("DPRICE", 2, "Dynamic Pricing"),
	VAN_PICKUP("VANPUP", 3, "Van Pickup");

	private final int id;
	private final String code;
	private final String name;

	private TripActivitiesEM(String code, int id, String name) {
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

	public static TripActivitiesEM getTripActivitiesEM(int id) {
		TripActivitiesEM[] values = values();
		for (TripActivitiesEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		System.out.println("Trip Status Not Found id: " + id);
		return null;
	}

	public static TripActivitiesEM getTripActivitiesEM(String Code) {
		TripActivitiesEM[] values = values();
		for (TripActivitiesEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		System.out.println("Trip Status Not Found Code: " + Code);
		return null;
	}
}

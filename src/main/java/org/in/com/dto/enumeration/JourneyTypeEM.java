package org.in.com.dto.enumeration;

public enum JourneyTypeEM {

	ONWARD_TRIP("OW", 1, "Onward Trip"),
	RETURN_TRIP("RT", 2, "Return Trip"),
	ALL_TRIP("AL", 3, "All Trip"),
	POSTPONE("PO", 4, "Post Pone"),
	PREPONE("PR", 5, "Pre Pone");

	private final int id;
	private final String code;
	private final String name;

	private JourneyTypeEM(String code, int id, String name) {
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

	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	public String toString() {
		return code + " : " + id;
	}

	public static JourneyTypeEM getJourneyTypeEM(int id) {
		JourneyTypeEM[] values = values();
		for (JourneyTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static JourneyTypeEM getJourneyTypeEM(String Code) {
		JourneyTypeEM[] values = values();
		for (JourneyTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}
}

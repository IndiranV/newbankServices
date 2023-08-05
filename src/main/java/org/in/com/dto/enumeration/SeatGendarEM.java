package org.in.com.dto.enumeration;

public enum SeatGendarEM {

	MALE("M", 1, "Male"), FEMALE("F", 2, "Female"), ALL("A", 3, "All");

	private final int id;
	private final String code;
	private final String name;

	private SeatGendarEM(String code, int id, String name) {
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

	public static SeatGendarEM getSeatGendarEM(int id) {
		SeatGendarEM[] values = values();
		for (SeatGendarEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static SeatGendarEM getSeatGendarEM(String Code) {
		SeatGendarEM[] values = values();
		for (SeatGendarEM gendarDTO : values) {
			if (gendarDTO.getCode().equalsIgnoreCase(Code)) {
				return gendarDTO;
			}
		}
		return FEMALE;
	}
}

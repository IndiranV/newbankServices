package org.in.com.dto.enumeration;

public enum SeatGenderRestrictionEM {

	ANY_GENDER("ANYGDR", 1, "Any Gender No Restriction"), 
	//Default
	SIMILAR_GENDER("SMRGDR", 2, "Similar Gender"),
	// Female Superior, Female can book male adjection seat
	FAMALE_SUPERIOR("FMESUP", 3, "Superior Female");

	private final int id;
	private final String code;
	private final String name;

	private SeatGenderRestrictionEM(String code, int id, String name) {
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

	public static SeatGenderRestrictionEM getSeatGendarRestrictionEM(int id) {
		SeatGenderRestrictionEM[] values = values();
		for (SeatGenderRestrictionEM restriction : values) {
			if (restriction.getId() == id) {
				return restriction;
			}
		}
		return SIMILAR_GENDER;
	}

	public static SeatGenderRestrictionEM getSeatGendarRestrictionEM(String Code) {
		SeatGenderRestrictionEM[] values = values();
		for (SeatGenderRestrictionEM restriction : values) {
			if (restriction.getCode().equalsIgnoreCase(Code)) {
				return restriction;
			}
		}
		return SIMILAR_GENDER;
	}
}

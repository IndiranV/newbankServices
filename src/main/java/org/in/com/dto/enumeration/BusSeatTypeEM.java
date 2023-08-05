package org.in.com.dto.enumeration;

public enum BusSeatTypeEM {

	ALL_BUS_SEAT_TYPE("ABST", 0, "All Seat Type", false),
	
	SEMI_SLEEPER("SS", 1, "Semi Sleeper", true),
	SLEEPER("SL", 2, "Sleeper", true),
	SEATER("ST", 3, "Seater", true),
	UPPER_SLEEPER("USL", 4, "Upper Sleeper", true),
	LOWER_SLEEPER("LSL", 5, "Lower Sleeper", true),
	PUSH_BACK("PB", 6, "Pushback", true),
	
	REST_ROOM("RRM", 7, "Rest Room", false),
	DINING_AREA("PTY", 8, "Pantry", false),
	FREE_SPACE("FRS", 9, "Free Space", false),
	SINGLE_UPPER_SLEEPER("SUSL", 10, "Single Upper Sleeper", true),
	SINGLE_LOWER_SLEEPER("SLSL", 11, "Single Lower Sleeper", true),
	
	SINGLE_SEMI_SLEEPER("SSS", 12, "Single Semi Sleeper", true),
	SINGLE_SEATER("SST", 13, "Single Seater", true);

	private final int id;
	private final String code;
	private final String name;
	private final boolean reservation;

	private BusSeatTypeEM(String code, int id, String name, boolean reservation) {
		this.code = code;
		this.id = id;
		this.name = name;
		this.reservation = reservation;
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

	public boolean isReservation() {
		return reservation;
	}

	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	public String toString() {
		return code + " : " + id;
	}

	public static BusSeatTypeEM getBusSeatTypeEM(int id) {
		BusSeatTypeEM[] values = values();
		for (BusSeatTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return ALL_BUS_SEAT_TYPE;
	}

	public static BusSeatTypeEM getBusSeatTypeEM(String Code) {
		BusSeatTypeEM[] values = values();
		for (BusSeatTypeEM seatType : values) {
			if (seatType.getCode().equalsIgnoreCase(Code)) {
				return seatType;
			}
		}
		return ALL_BUS_SEAT_TYPE;
	}
}

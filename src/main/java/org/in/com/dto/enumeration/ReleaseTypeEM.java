package org.in.com.dto.enumeration;

public enum ReleaseTypeEM {

	RELEASE_ACAT("ACAT", 1, "Release Allocation Seat"),
	RELEASE_HIDE("HIDE", 2, "Release Block Seat"),
	RELEASE_PHONE("RLSPHTCK", 3, "Release Phone booked tickets"),
	CONFIRM_PHONE("CNFPHTCK", 4, "Confirm Phone booked tickets");

	private final int id;
	private final String code;
	private final String name;

	private ReleaseTypeEM(String code, int id, String name) {
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
		return code + " : " + id + ": " + name;
	}

	public static ReleaseTypeEM getReleaseTypeEM(int id) {
		ReleaseTypeEM[] values = values();
		for (ReleaseTypeEM code : values) {
			if (code.getId() == id) {
				return code;
			}
		}
		return null;
	}

	public static ReleaseTypeEM getReleaseTypeEM(String code) {
		ReleaseTypeEM[] values = values();
		for (ReleaseTypeEM modeDTO : values) {
			if (modeDTO.getCode().equalsIgnoreCase(code)) {
				return modeDTO;
			}
		}
		return null;
	}

}

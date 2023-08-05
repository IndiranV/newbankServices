package org.in.com.dto.enumeration;

public enum SlabCalenderModeEM {
	FLEXI("FLEXI", 1, "Flexi Mode"), STRICT("STRICT", 2, "Strict Mode");

	private final int id;
	private final String code;
	private final String name;

	private SlabCalenderModeEM(String code, int id, String name) {
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

	public static SlabCalenderModeEM getSlabCalenderModeEM(int id) {
		SlabCalenderModeEM[] values = values();
		for (SlabCalenderModeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static SlabCalenderModeEM getSlabCalenderModeEM(String code) {
		SlabCalenderModeEM[] values = values();
		for (SlabCalenderModeEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return null;
	}
}

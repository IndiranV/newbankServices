package org.in.com.dto.enumeration;

public enum SlabModeEM {
	COUNT("STCNT", 1, "Seat Count"), AMOUNT("STAMT", 2, "Seat Amount");

	private final int id;
	private final String code;
	private final String name;

	private SlabModeEM(String code, int id, String name) {
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

	public static SlabModeEM getSlabModeEM(int id) {
		SlabModeEM[] values = values();
		for (SlabModeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static SlabModeEM getSlabModeEM(String code) {
		SlabModeEM[] values = values();
		for (SlabModeEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return null;
	}
}

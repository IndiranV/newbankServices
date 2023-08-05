package org.in.com.dto.enumeration;

public enum SlabCalenderTypeEM {
	DAY("DAY", 1, "By Day"), WEEK("WEEK", 2, "By Week"), MONTH("MONTH", 3, "By Month");

	private final int id;
	private final String code;
	private final String name;

	private SlabCalenderTypeEM(String code, int id, String name) {
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

	public static SlabCalenderTypeEM getSlabCalenderTypeEM(int id) {
		SlabCalenderTypeEM[] values = values();
		for (SlabCalenderTypeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static SlabCalenderTypeEM getSlabCalenderTypeEM(String code) {
		SlabCalenderTypeEM[] values = values();
		for (SlabCalenderTypeEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return null;
	}
}

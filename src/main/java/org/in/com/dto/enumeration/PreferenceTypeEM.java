package org.in.com.dto.enumeration;

public enum PreferenceTypeEM {

	AUTOMATIC("AUTO", 1, "Generate Automatic Voucher"), MANUAL("MANL", 2, "Manual Voucher");

	private final int id;
	private final String code;
	private final String name;

	private PreferenceTypeEM(String code, int id, String name) {
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

	public static PreferenceTypeEM getPreferenceTypeEM(int id) {
		PreferenceTypeEM[] values = values();
		for (PreferenceTypeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static PreferenceTypeEM getPreferenceTypeEM(String Code) {
		PreferenceTypeEM[] values = values();
		for (PreferenceTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}

}

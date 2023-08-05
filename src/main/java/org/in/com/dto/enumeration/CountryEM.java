package org.in.com.dto.enumeration;

public enum CountryEM {
	INDIA(1, "INDIA", "India"), NEPAL(2, "NEPAL", "Nepal");

	private final String code;
	private final int gstid;
	private final String name;

	private CountryEM(int gstid, String code, String name) {
		this.code = code;
		this.gstid = gstid;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public int getGstid() {
		return gstid;
	}

	public static CountryEM getStateEMGstID(int gstid) {
		CountryEM[] values = values();
		for (CountryEM errorCode : values) {
			if (errorCode.getGstid() == gstid) {
				return errorCode;
			}
		}
		return null;
	}

	public static CountryEM getGstStateEM(String code) {
		CountryEM[] values = values();
		for (CountryEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return null;
	}

}

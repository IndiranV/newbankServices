package org.in.com.dto.enumeration;

public enum NamespaceEM {

	BITS_ADMIN("bits", "Bits Admin"),
	TATTRAVELS("tattravels", "Thirumal Alagu Travels"),
	TRANZKING("tranzking", "Tranzking");

	private final String code;
	private final String name;

	private NamespaceEM(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return name;
	}

	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	public static NamespaceEM getNamespaceEM(String Code) {
		NamespaceEM[] values = values();
		for (NamespaceEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}
}

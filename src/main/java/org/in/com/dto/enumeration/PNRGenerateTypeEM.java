package org.in.com.dto.enumeration;

public enum PNRGenerateTypeEM {

	SEQUENCE_NUMBERIC("SEQNUM", 1, "Generate Sequence Numeric"), ENCODE_ALPHANUMERIC("ALPANU", 2, "Alpha Numberic Encode");

	private final int id;
	private final String code;
	private final String name;

	private PNRGenerateTypeEM(String code, int id, String name) {
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

	public static PNRGenerateTypeEM getPNRGenerateTypeEM(int id) {
		PNRGenerateTypeEM[] values = values();
		for (PNRGenerateTypeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return ENCODE_ALPHANUMERIC;
	}

	public static PNRGenerateTypeEM getPNRGenerateTypeEM(String Code) {
		PNRGenerateTypeEM[] values = values();
		for (PNRGenerateTypeEM generate : values) {
			if (generate.getCode().equalsIgnoreCase(Code)) {
				return generate;
			}
		}
		return ENCODE_ALPHANUMERIC;
	}

}

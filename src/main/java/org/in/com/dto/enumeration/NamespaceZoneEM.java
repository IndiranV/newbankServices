package org.in.com.dto.enumeration;

public enum NamespaceZoneEM {

	BITS(1, "bits", "http://app.ezeebits.com", "HXXJFEHP79Q69NZP"),
	BITS_REGION_2(2, "r2bits", "http://app.r2.ezeebits.com","FPSB2AWVQ8JZRKMRS"),
	BITS_REGION_3(3, "r3bits", "http://app.r3.ezeebits.com", "R39WCFY8HRNKY2V4S"),
	TAT_BITS(4, "tatbits", "http://app.tattravels.com", "5LXQMG55E75DLUWJ"),
	YBM_BITS(5, "ybmbits", "http://app.ybmtravels.in", "94MK3EZY2MLM97VE"),
	SBLT_BITS(6, "sbltbits", "http://app.sbltbus.com", "SEJ4326Q7RHXYZSH"),
	SVRT_BITS(7, "svrtbits", "http://app.srivenkataramanatravels.co.in", "BMMK94CAJR4UM4HY"),
	TRANZKING_BITS(8, "tranzkingbits", "http://app.tranzking.com","M2XMG8C49Y3MHPPR"),
	PARVEEN_BITS(9, "parveenbits", "http://app.parveentravels.in","HID8E8U5ZC6YE8NA"),
	DEV_BITS(10, "devbits", "http://dev-app.ezeebits.in","R1QLRDTHKGQ5DC0"),
	RAJESH_BITS(11, "rajeshbits", "http://app.rajeshbus.com","G4D00V8SN1W041R"),
	RMT_BITS(12, "rmtbits", "http://app.rathimeenatravels.in","R8MV7CXFRK9TWH27MR"),
	GOTOUR_BITS(13, "gotourbits", "http://app.gotourtravels.com","TW4FRM6CXHKV297R87");

	private final int id;
	private final String code;
	private final String domainUrl;
	private final String token;

	private NamespaceZoneEM(int id, String code, String domainUrl, String token) {
		this.code = code;
		this.id = id;
		this.domainUrl = domainUrl;
		this.token = token;
	}

	public Integer getId() {

		return id;
	}

	public String getCode() {
		return code;
	}

	public String getDomainURL() {
		return domainUrl;
	}

	public String getToken() {
		return token;
	}

	public String toString() {
		return code + " : " + id;
	}

	public static NamespaceZoneEM getNamespaceZoneEM(int id) {
		NamespaceZoneEM[] values = values();
		for (NamespaceZoneEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static NamespaceZoneEM getNamespaceZoneEM(String Code) {
		NamespaceZoneEM[] values = values();
		for (NamespaceZoneEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}
}

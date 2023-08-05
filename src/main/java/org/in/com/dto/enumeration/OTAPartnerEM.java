package org.in.com.dto.enumeration;

public enum OTAPartnerEM {

	API_USER_RB("APIRB", 1, "redbus"),
	API_USER_ABHIBUS("APIAB", 2, "abhibus"),
	API_USER_PAYTM("APIPT", 3, "paytm");
	

	private final int id;
	private final String code;
	private final String vendorKey;

	private OTAPartnerEM( String code, int id, String vendorKey) {
		this.id = id;
		this.code = code;
		this.vendorKey = vendorKey;
	}

	public static OTAPartnerEM getOtaPartnerEM(int id) {
		OTAPartnerEM[] values = values();
		for (OTAPartnerEM otaPartnerEM : values) {
			if (otaPartnerEM.getId() == id) {
				return otaPartnerEM;
			}
		}
		return null;
	}

	public static OTAPartnerEM getOtaPartnerEM(String Code) {
		OTAPartnerEM[] values = values();
		for (OTAPartnerEM otaPartnerEM : values) {
			if (otaPartnerEM.getCode().equalsIgnoreCase(Code)) {
				return otaPartnerEM;
			}
		}
		return null;
	}

	public int getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getVendorKey() {
		return vendorKey;
	}

}

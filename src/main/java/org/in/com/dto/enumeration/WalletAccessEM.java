package org.in.com.dto.enumeration;

public enum WalletAccessEM {
	DEMO("demobo", 1, "Demo Travels", "2MCXQKXFCV6CUCAT"),

	TRANZKING("tranzking", 2, "Tranzking Travels", "7MQEZNHFNKSVLS32"),

	YBM_TRAVELS("ybmtravels", 3, "YBM Travels", "RM5T3LG75AYERK7M"),

	SVRT_TRAVELS("srivenkataramana", 4, "Sri Venkataramana Travels", "KZ6YPEVDA38TJP3W"),

	TAT_TRAVELS("tattravels", 5, "TAT Travels", "T8Q22P1YNC63UHF9"),

	RAJESH_TRANSPORTS("rajeshtransports", 9, "Rajesh Transports", "NMA0WI6VGYXKOWO"),

	BITS("bits", 10, "Bits Admin", "4HMAIWU7E3DSO5G"),

	VKV_TRAVELS("vkvtravels", 11, "VKV Travels", "VKV2MI49NRvYXK0"),
	
	VIKRAMTRAVELS("vikramtravels", 12, "Vikram Travels", "VIKRAM1KM93KWRYXK0"),
	
	GOTOUR("gotour", 13, "Go Tour Travels And Holidays", "G0TOURK01KR3KWXYM9"),
	
	SIRI_TRAVELS("siritravels", 14, "Siri Tours and Travels", "SIRIBUSSH2PI7U91KM9");

	private final int id;
	private final String code;
	private final String name;
	private final String token;

	private WalletAccessEM(String code, int id, String name, String token) {
		this.code = code;
		this.id = id;
		this.name = name;
		this.token = token;
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

	public String getToken() {
		return token;
	}

	public static WalletAccessEM getWalletAccessEM(int id) {
		WalletAccessEM[] values = values();
		for (WalletAccessEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return null;
	}

	public static WalletAccessEM getWalletAccessEM(String code) {
		WalletAccessEM[] values = values();
		for (WalletAccessEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return null;
	}

}

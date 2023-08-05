package org.in.com.dto.enumeration;

public enum IntegrationTypeEM {

 	BITS(1, "BITS", "BITS"),
	CARGO(2, "CARGO", "Cargo"),
	TRACKBUS(3, "TRACKBUS", "TrackBus"),
	ORBIT(4, "ORBIT", "Orbit"),
	WALLET(5, "WALLET", "Customer Wallet"),
	COSTIV(6, "COSTIV", "Costiv"),
	TOURONE(7, "TOURONE", "Tour One"),
	ZINGPAY(8, "ZINGPAY", "Zing Pay"),
	VERTEX(9, "VERTEX", "Vertex"),
	DPE(10, "DPRICE", "Dynamic Price");
 	
 	private final int id;
	private final String code;
	private final String name;

	private IntegrationTypeEM(int id, String code, String name) {
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

	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	public String toString() {
		return code + " : " + id;
	}

	public static IntegrationTypeEM getIntegrationTypeEM(int id) {
		IntegrationTypeEM[] values = values();
		for (IntegrationTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static IntegrationTypeEM getIntegrationTypeEM(String Code) {
		IntegrationTypeEM[] values = values();
		for (IntegrationTypeEM typeEM : values) {
			if (typeEM.getCode().equalsIgnoreCase(Code)) {
				return typeEM;
			}
		}
		return null;
	}
}

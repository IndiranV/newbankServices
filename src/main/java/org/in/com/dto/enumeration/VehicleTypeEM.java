package org.in.com.dto.enumeration;

public enum VehicleTypeEM {
	BUS("BUS", 1, "Bus"),
	VAN("VAN", 2, "Van");

	private final int id;
	private final String code;
	private final String name;

	private VehicleTypeEM(String code, int id, String name) {
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

	public static VehicleTypeEM getVehicleTypeEM(int id) {
		VehicleTypeEM[] values = values();
		for (VehicleTypeEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return BUS;
	}

	public static VehicleTypeEM getVehicleTypeEM(String code) {
		VehicleTypeEM[] values = values();
		for (VehicleTypeEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return BUS;
	}

}

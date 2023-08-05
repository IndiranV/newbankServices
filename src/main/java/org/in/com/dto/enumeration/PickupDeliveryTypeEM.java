package org.in.com.dto.enumeration;

public enum PickupDeliveryTypeEM {

	DELIVERY_SELF_PICKUP("DYSE",1,"Self Pickup"),
	DELIVERY_DOOR("DYDO",2,"Door Delivery"),
	PICKUP_SELF("PUSE",3,"Self Pickup"),
	PICKUP_DOOR("PUDO",4,"Door Pickup"),
	PICKUP_DELIVERY_DOOR("PDDO",5,"Door Pickup and Delivery");
	
	private final int id;
	private final String code;
	private final String name;

	private PickupDeliveryTypeEM(String code, int id, String name) {
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

	public static PickupDeliveryTypeEM getDeliveryTypeEM(int id) {
		PickupDeliveryTypeEM[] values = values();
		for (PickupDeliveryTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return DELIVERY_SELF_PICKUP;
	}

	public static PickupDeliveryTypeEM getDeliveryTypeEM(String value) {
		PickupDeliveryTypeEM[] values = values();
		for (PickupDeliveryTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(value)) {
				return errorCode;
			}
		}
		return DELIVERY_SELF_PICKUP;
	}
}

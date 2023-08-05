package org.in.com.dto.enumeration;

public enum DynamicPriceProviderEM {

	NOT_AVAILABLE("NA", 0, "Not Available", "NA"), REDBUS("RDBUS", 1, "Redbus", "RedbusDynamicPricingImpl"), SCIATIVE("SCTIV", 2, "Sciative", "SciativeDynamicPricingImpl");

	private final int id;
	private final String code;
	private final String name;
	private final String impl;

	private DynamicPriceProviderEM(String code, int id, String name, String impl) {
		this.code = code;
		this.id = id;
		this.name = name;
		this.impl = impl;
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

	public String toString() {
		return code + " : " + id + ":" + name + ":" + impl;
	}

	public String getImpl() {
		return impl;
	}

	public static DynamicPriceProviderEM getDynamicPriceProviderEM(int id) {
		DynamicPriceProviderEM[] values = values();
		for (DynamicPriceProviderEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return DynamicPriceProviderEM.NOT_AVAILABLE;
	}

	public static DynamicPriceProviderEM getDynamicPriceProviderEM(String Code) {
		DynamicPriceProviderEM[] values = values();
		for (DynamicPriceProviderEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		System.out.println("Date Type Not Found: " + Code);
		return DynamicPriceProviderEM.NOT_AVAILABLE;
	}
}

package org.in.com.dto.enumeration;

import org.in.com.config.ApplicationConfig;

public enum ProductTypeEM {

	BITS("bits", 1, "bits", "ticket"), ACCOUNT("account", 2, "Account", "account"), CARGO("cargo", 3, "Cargo", "cargo");

	private final int id;
	private final String code;
	private final String name;
	private final String domainUrl;

	private ProductTypeEM(String code, int id, String name, String domainUrl) {
		this.code = code;
		this.id = id;
		this.name = name;
		this.domainUrl = domainUrl;
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

	public String getDomainUrl() {
		return domainUrl + "." + ApplicationConfig.getServerZoneUrl();
	}

	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	public String toString() {
		return code + " : " + id;
	}

	public static ProductTypeEM getProductTypeEM(int id) {
		ProductTypeEM[] values = values();
		for (ProductTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return BITS;
	}

	public static ProductTypeEM getProductTypeEM(String code) {
		ProductTypeEM[] values = values();
		for (ProductTypeEM seatType : values) {
			if (seatType.getCode().equalsIgnoreCase(code)) {
				return seatType;
			}
		}
		return BITS;
	}
}

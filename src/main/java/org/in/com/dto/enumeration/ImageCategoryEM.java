package org.in.com.dto.enumeration;

public enum ImageCategoryEM {

	PAYMENT_RECEIPT("RECPT", 1, "Payment Receipt"),
	CASHBOOK("CABO", 2, "Cashbook");

	private final int id;
	private final String code;
	private final String name;

	private ImageCategoryEM(String code, int id, String name) {
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

	public static ImageCategoryEM getImageCategoryEM(int id) {
		ImageCategoryEM[] values = values();
		for (ImageCategoryEM unit : values) {
			if (unit.getId() == id) {
				return unit;
			}
		}
		return null;
	}

	public static ImageCategoryEM getImageCategoryEM(String value) {
		ImageCategoryEM[] values = values();
		for (ImageCategoryEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(value)) {
				return errorCode;
			}
		}
		return null;
	}
}

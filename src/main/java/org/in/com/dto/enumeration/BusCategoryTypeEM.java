package org.in.com.dto.enumeration;

public enum BusCategoryTypeEM {

	LAYOUT_1X1("LT05", "1+1"), LAYOUT_2X2("LT01", "2+2"), LAYOUT_3X2("LT02", "3+2"), LAYOUT_2X1("LT03", "2+1"), LAYOUT_1X1X1("LT04", "1+1+1"), LAYOUT_OTHERS("LT99", ""),

	CHASIS_MULTIAXLE("CS01", "MultiAxle"), CHASIS_OTHERS("CS99", ""), CHASIS_SCANIA_MULTI_AXLE("CS02", "Scania Multi-Axle"), CHASIS_LEYLAND("CS03", "Leyland"), CHASIS_B11R_MULTI_AXLE("CS04", "B11R MultiAxle"), CHASIS_B9R_MULTI_AXLE("CS05", "B9R MultiAxle"), CHASIS_B11R_MULTI_AXLE_AUTO_TRANSMISSION("CS06", "B11R MultiAxle Auto Transmission"), CHASIS_B11R_MULTI_AXLE_ISHIFT("CS07", "B11R MultiAxle I-Shift"), CHASIS_CAPELLA("CS08", "Capella"), LUXURA("CS09", "Luxura"), CHASIS_ELECTRIC("CS10", "Electric"), CHASIS_PREMIUM_ELECTRIC("CS11", "Premium Electric"),

	MAKE_VOLVO("MK01", "Volvo"), MAKE_AIRBUS("MK02", "AirBus"), MAKE_KING_LONG("MK03", "King Long"), MAKE_ISUZU("MK04", "Isuzu"), MAKE_MERCEDEZ_BENZ("MK05", "Mercedez Benz"), MAKE_SCANIA("MK06", "Scania"), MAKE_BHARATBENZ("MK07", "BharatBenz"), MAKE_DECCAN_AUTO("MK08", "Deccan Auto"), MAKE_CORONA("MK09", "Corona"), MAKE_TATA("MK10", "TATA"), MAKE_TOYOTA("MK11", "Toyota"), MAKE_NISSAN("MK12", "Nissan"), MAKE_EICHER("MK13", "Eicher"), MAKE_OTHERS("MK99", ""),

	SEATTYPE_SEATER_SLEEPER("ST01", "Seater/Sleeper"), SEATTYPE_SEMI_SLEEPER("ST02", "Semi Sleeper"), SEATTYPE_SLEEPER("ST03", "Sleeper"), SEATTYPE_SEATER("ST04", "Seater"), SEATTYPE_PUSHBACK("ST05", "Pushback"), SEATTYPE_EXECUTIVE("ST06", "Executive"), SEATTYPE_SEMI_SLEEPER_SLEEPE("ST07", "Semi Sleeper/Sleeper"), SEATTYPE_PREMIUM_SLEEPER("ST08", "Premium Sleeper"), SEATTYPE_SINGLE_SEMI_SLEEPER("ST09", "Single Semi Sleeper"), SEATTYPE_SINGLE_SEATER("ST10", "Single Seater"),

	CLIMATE_CONTROL_AC("CC01", "A/C"), CLIMATE_CONTROL_NON_AC("CC02", "Non A/C");

	private final String code;
	private final String name;

	private BusCategoryTypeEM(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public static BusCategoryTypeEM getBusCategoryType(String Code) {
		BusCategoryTypeEM[] values = values();
		for (BusCategoryTypeEM seatType : values) {
			if (seatType.getCode().equalsIgnoreCase(Code)) {
				return seatType;
			}
		}
		return LAYOUT_OTHERS;
	}
}

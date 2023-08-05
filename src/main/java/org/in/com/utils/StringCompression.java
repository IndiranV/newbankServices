package org.in.com.utils;


public class StringCompression {

	public static String NumericCompression(String scheduleId) {
		while (scheduleId.contains(" ")) {
			scheduleId = scheduleId.replace(" ", "");
		}
		while (scheduleId.contains("0000000")) {
			scheduleId = scheduleId.replace("0000000", "XA");
		}
		while (scheduleId.contains("000000")) {
			scheduleId = scheduleId.replace("000000", "XB");
		}
		while (scheduleId.contains("00000")) {
			scheduleId = scheduleId.replace("00000", "XC");
		}
		while (scheduleId.contains("0000")) {
			scheduleId = scheduleId.replace("0000", "XD");
		}
		while (scheduleId.contains("1111")) {
			scheduleId = scheduleId.replace("1111", "XE");
		}
		while (scheduleId.contains("2222")) {
			scheduleId = scheduleId.replace("2222", "XF");
		}
		while (scheduleId.contains("3333")) {
			scheduleId = scheduleId.replace("3333", "XG");
		}
		while (scheduleId.contains("4444")) {
			scheduleId = scheduleId.replace("4444", "XH");
		}
		while (scheduleId.contains("5555")) {
			scheduleId = scheduleId.replace("5555", "XI");
		}
		while (scheduleId.contains("6666")) {
			scheduleId = scheduleId.replace("6666", "XJ");
		}
		while (scheduleId.contains("7777")) {
			scheduleId = scheduleId.replace("7777", "XK");
		}
		while (scheduleId.contains("8888")) {
			scheduleId = scheduleId.replace("8888", "XL");
		}
		while (scheduleId.contains("9999")) {
			scheduleId = scheduleId.replace("9999", "XM");
		}

		while (scheduleId.contains("0123")) {
			scheduleId = scheduleId.replace("0123", "XN");
		}
		while (scheduleId.contains("1234")) {
			scheduleId = scheduleId.replace("1234", "XO");
		}
		while (scheduleId.contains("2345")) {
			scheduleId = scheduleId.replace("2345", "XP");
		}
		while (scheduleId.contains("3456")) {
			scheduleId = scheduleId.replace("3456", "XQ");
		}
		while (scheduleId.contains("4567")) {
			scheduleId = scheduleId.replace("4567", "XR");
		}
		while (scheduleId.contains("5678")) {
			scheduleId = scheduleId.replace("5678", "XS");
		}
		while (scheduleId.contains("6789")) {
			scheduleId = scheduleId.replace("6789", "XT");
		}
		while (scheduleId.contains("7890")) {
			scheduleId = scheduleId.replace("7890", "XU");
		}
		while (scheduleId.contains("0987")) {
			scheduleId = scheduleId.replace("0987", "XV");
		}
		while (scheduleId.contains("9876")) {
			scheduleId = scheduleId.replace("9876", "XW");
		}
		while (scheduleId.contains("8765")) {
			scheduleId = scheduleId.replace("8765", "YA");
		}
		while (scheduleId.contains("7654")) {
			scheduleId = scheduleId.replace("7654", "YB");
		}
		while (scheduleId.contains("6543")) {
			scheduleId = scheduleId.replace("6543", "YC");
		}
		while (scheduleId.contains("5432")) {
			scheduleId = scheduleId.replace("5432", "YD");
		}
		while (scheduleId.contains("4321")) {
			scheduleId = scheduleId.replace("4321", "YE");
		}
		while (scheduleId.contains("3210")) {
			scheduleId = scheduleId.replace("3210", "YF");
		}

		while (scheduleId.contains("000")) {
			scheduleId = scheduleId.replace("000", "YG");
		}
		while (scheduleId.contains("111")) {
			scheduleId = scheduleId.replace("111", "YH");
		}
		while (scheduleId.contains("222")) {
			scheduleId = scheduleId.replace("222", "YI");
		}
		while (scheduleId.contains("333")) {
			scheduleId = scheduleId.replace("333", "YJ");
		}
		while (scheduleId.contains("444")) {
			scheduleId = scheduleId.replace("444", "YK");
		}
		while (scheduleId.contains("555")) {
			scheduleId = scheduleId.replace("555", "YL");
		}
		while (scheduleId.contains("666")) {
			scheduleId = scheduleId.replace("666", "YM");
		}
		while (scheduleId.contains("777")) {
			scheduleId = scheduleId.replace("777", "YN");
		}
		while (scheduleId.contains("888")) {
			scheduleId = scheduleId.replace("888", "YO");
		}
		while (scheduleId.contains("999")) {
			scheduleId = scheduleId.replace("999", "YP");
		}

		while (scheduleId.contains("012")) {
			scheduleId = scheduleId.replace("012", "YQ");
		}
		while (scheduleId.contains("123")) {
			scheduleId = scheduleId.replace("123", "YR");
		}
		while (scheduleId.contains("234")) {
			scheduleId = scheduleId.replace("234", "YS");
		}
		while (scheduleId.contains("345")) {
			scheduleId = scheduleId.replace("345", "YT");
		}
		while (scheduleId.contains("456")) {
			scheduleId = scheduleId.replace("456", "YU");
		}
		while (scheduleId.contains("567")) {
			scheduleId = scheduleId.replace("567", "YV");
		}
		while (scheduleId.contains("678")) {
			scheduleId = scheduleId.replace("678", "YW");
		}
		while (scheduleId.contains("789")) {
			scheduleId = scheduleId.replace("789", "ZA");
		}
		while (scheduleId.contains("890")) {
			scheduleId = scheduleId.replace("890", "ZB");
		}
		while (scheduleId.contains("098")) {
			scheduleId = scheduleId.replace("098", "ZC");
		}
		while (scheduleId.contains("987")) {
			scheduleId = scheduleId.replace("987", "ZD");
		}
		while (scheduleId.contains("876")) {
			scheduleId = scheduleId.replace("876", "ZE");
		}
		while (scheduleId.contains("765")) {
			scheduleId = scheduleId.replace("765", "ZF");
		}
		while (scheduleId.contains("654")) {
			scheduleId = scheduleId.replace("654", "ZG");
		}
		while (scheduleId.contains("543")) {
			scheduleId = scheduleId.replace("543", "ZH");
		}
		while (scheduleId.contains("432")) {
			scheduleId = scheduleId.replace("432", "ZI");
		}
		while (scheduleId.contains("321")) {
			scheduleId = scheduleId.replace("321", "ZJ");
		}
		while (scheduleId.contains("210")) {
			scheduleId = scheduleId.replace("210", "ZK");
		}

		while (scheduleId.contains("00")) {
			scheduleId = scheduleId.replace("00", "A");
		}
		while (scheduleId.contains("11")) {
			scheduleId = scheduleId.replace("11", "B");
		}
		while (scheduleId.contains("22")) {
			scheduleId = scheduleId.replace("22", "C");
		}
		while (scheduleId.contains("33")) {
			scheduleId = scheduleId.replace("33", "D");
		}
		while (scheduleId.contains("44")) {
			scheduleId = scheduleId.replace("44", "E");
		}
		while (scheduleId.contains("55")) {
			scheduleId = scheduleId.replace("55", "F");
		}
		while (scheduleId.contains("66")) {
			scheduleId = scheduleId.replace("66", "G");
		}
		while (scheduleId.contains("77")) {
			scheduleId = scheduleId.replace("77", "H");
		}
		while (scheduleId.contains("88")) {
			scheduleId = scheduleId.replace("88", "I");
		}
		while (scheduleId.contains("99")) {
			scheduleId = scheduleId.replace("99", "J");
		}
		while (scheduleId.contains("01")) {
			scheduleId = scheduleId.replace("01", "K");
		}
		while (scheduleId.contains("12")) {
			scheduleId = scheduleId.replace("12", "L");
		}
		while (scheduleId.contains("23")) {
			scheduleId = scheduleId.replace("23", "M");
		}
		while (scheduleId.contains("34")) {
			scheduleId = scheduleId.replace("34", "N");
		}
		while (scheduleId.contains("45")) {
			scheduleId = scheduleId.replace("45", "O");
		}
		while (scheduleId.contains("56")) {
			scheduleId = scheduleId.replace("56", "P");
		}
		while (scheduleId.contains("67")) {
			scheduleId = scheduleId.replace("67", "Q");
		}
		while (scheduleId.contains("78")) {
			scheduleId = scheduleId.replace("78", "R");
		}
		while (scheduleId.contains("89")) {
			scheduleId = scheduleId.replace("89", "S");
		}
		while (scheduleId.contains("90")) {
			scheduleId = scheduleId.replace("90", "T");
		}
		while (scheduleId.contains("98")) {
			scheduleId = scheduleId.replace("98", "U");
		}
		while (scheduleId.contains("87")) {
			scheduleId = scheduleId.replace("87", "V");
		}
		while (scheduleId.contains("76")) {
			scheduleId = scheduleId.replace("76", "W");
		}
		while (scheduleId.contains("65")) {
			scheduleId = scheduleId.replace("65", "ZL");
		}
		while (scheduleId.contains("54")) {
			scheduleId = scheduleId.replace("54", "ZM");
		}
		while (scheduleId.contains("43")) {
			scheduleId = scheduleId.replace("43", "ZN");
		}
		while (scheduleId.contains("32")) {
			scheduleId = scheduleId.replace("32", "ZO");
		}
		while (scheduleId.contains("21")) {
			scheduleId = scheduleId.replace("21", "ZP");
		}
		return scheduleId;
	}

}

package org.in.com.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenEncrypt {
	public static String encryptString(String token) {
		String passwordToken = null;
		try {
			if (StringUtil.isNotNull(token)) {
				token = StringUtil.trimAllSpaces(token);

				StringBuilder newToken = new StringBuilder();
				if (StringUtil.isNotNull(token)) {
					char[] resChars = token.toCharArray();
					for (Character resChar : resChars) {
						newToken.append((int) resChar);

					}
				}
				String numbers = null;
				/** Get all numeric values from a string **/
				Pattern nummberPattern = Pattern.compile("(\\d+)");
				Matcher matchs = nummberPattern.matcher(newToken.toString());
				while (matchs.find()) {
					String groupvalue = matchs.group();
					if (groupvalue.length() > 4) {
						groupvalue = "" + groupvalue.length() + sumStringNumber(groupvalue);
					}
					if (groupvalue.length() > 3) {
						groupvalue = "" + sumStringNumber(groupvalue);
					}

					if (numbers == null) {
						numbers = groupvalue;
					}
					else {
						numbers += groupvalue;
					}
				}
				if (numbers == null) {
					int counter = 0;
					int sum = 0;
					String terms = token.toLowerCase();
					for (int i = 0; i < terms.length(); i++) {
						if (terms.charAt(i) == 'a' || terms.charAt(i) == 'e' || terms.charAt(i) == 'i' || terms.charAt(i) == 'o' || terms.charAt(i) == 'u') {
							sum = sum + i;
							counter++;
						}
					}
					numbers = "" + counter + sum;
				}
				newToken.append(numbers);

				passwordToken = StringCompression.NumericCompression(newToken.toString());
			}
			else {
				System.out.println("Token is empty");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return passwordToken;
	}

	private static int sumStringNumber(String data) {
		int sum = 0;
		while (data.length() > 0) {
			sum += Integer.parseInt(data.substring(0, 1));
			data = data.substring(1, data.length());
		}
		return sum;
	}

	public static void main(String a[]) {
		System.out.println(encryptString("welcome123"));
	}
}
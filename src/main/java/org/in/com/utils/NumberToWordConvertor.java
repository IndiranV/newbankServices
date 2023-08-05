package org.in.com.utils;

import java.text.DecimalFormat;

public class NumberToWordConvertor {

	private static final String[] ones = { "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen" };
	private static final String[] tens = { "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety" };

	public static String convert(double number) {
		StringBuilder words = new StringBuilder();
		try {
			DecimalFormat decimalFormat = new DecimalFormat("#.##");
		
			String[] value = decimalFormat.format(number).split("\\.");
			int wholeValue = Integer.parseInt(value[0]);
			int decimalValue = value.length > 1 ? Integer.parseInt(value[1]) : 0;
			
			if (wholeValue > 0) {
				words.append(convertToWords(wholeValue));
			}
			if (decimalValue > 0) {
				words.append(" point ");
				words.append(convertToWords(decimalValue));
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return words.toString().trim();
	}

	private static String convertToWords(int number) {
		if (number < 20) {
			return ones[number];
		}
		if (number < 100) {
			return tens[number / 10] + " " + ones[number % 10];
		}
		if (number < 1000) {
			return ones[number / 100] + " hundred " + (number % 100 > 0 ? "and " : "") + convertToWords(number % 100);
		}
		if (number < 100000) {
			return convertToWords(number / 1000) + " thousand " + convertToWords(number % 1000);
		}
		if (number < 10000000) {
			return convertToWords(number / 100000) + " lakh " + convertToWords(number % 100000);
		}
		return convertToWords(number / 10000000) + " crore " + convertToWords(number % 10000000);
	}
	
}

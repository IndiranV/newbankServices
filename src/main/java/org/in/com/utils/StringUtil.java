package org.in.com.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.CancellationPolicyDTO;
import org.in.com.dto.CancellationTermDTO;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

public class StringUtil {
	private static final String MASK_CHAR = "x";
	final static Pattern MASKED_MOBILE_NUMBER_PATTERN = Pattern.compile("([0-9]{3}[x]{3}[0-9]{4})");

	public static boolean isNull(String text) {
		boolean flag = false;
		if (text == null || "".equals(text.trim()) || Text.NULL.equalsIgnoreCase(text) || "NA".equalsIgnoreCase(text) || Text.FALSE_STRING.equalsIgnoreCase(text)) {
			flag = true;
		}
		return flag;
	}

	public static boolean isNull(BigDecimal decimal) {
		boolean flag = false;
		if (decimal == null || decimal.compareTo(BigDecimal.ZERO) != 1) {
			flag = true;
		}
		return flag;
	}

	public static boolean isNull(Object object) {
		boolean flag = false;
		if (object == null) {
			flag = true;
		}
		return flag;
	}

	public static boolean isNotNull(BigDecimal decimal) {
		return !isNull(decimal);
	}

	public static boolean isNotNull(Object object) {
		return !isNull(object);
	}

	public static boolean isNotNull(String text) {
		return !isNull(text);
	}

	public static String isNull(String text, String defaultValue) {
		return isNull(text) ? defaultValue : text;
	}

	public BigDecimal isNull(BigDecimal decimal, BigDecimal defaultValue) {
		return isNull(decimal) ? defaultValue : decimal;
	}

	public static String trimAllSpaces(String strValues) {
		if (strValues.contains(" ")) {
			while (strValues.contains(" ")) {
				strValues = strValues.replace(" ", "");
			}
		}
		if (strValues.contains("  ")) {
			while (strValues.contains("  ")) {
				strValues = strValues.replace("  ", " ");
			}
		}
		return strValues.trim();
	}

	public static String trimDoubleSpaces(String strValues) {
		while (strValues.contains("  ")) {
			strValues = strValues.replace("  ", " ");
		}
		return strValues.trim();
	}

	public static String trimAllSpacesToUpperCase(String strValues) {
		if (strValues != null) {
			if (strValues.contains(" ")) {
				while (strValues.contains(" ")) {
					strValues = strValues.replace(" ", "");
				}
			}
			if (strValues.contains("  ")) {
				while (strValues.contains("  ")) {
					strValues = strValues.replace("  ", " ");
				}
			}
			return strValues.trim().toUpperCase();
		}
		return null;
	}

	public static boolean isNumeric(String StrValue) {
		boolean flag = false;
		try {
			Double.parseDouble(StrValue);
			flag = true;
		}
		catch (Exception e) {
			flag = false;
		}

		return flag;
	}

	public static boolean isBigDecimal(String StrValue) {
		boolean flag = false;
		try {
			new BigDecimal(StrValue);
			flag = true;
		}
		catch (Exception e) {
			flag = false;
		}

		return flag;
	}

	public static int getIntegerValue(String s) {
		int i = 0;
		try {
			if (isNotNull(s)) {
				i = Integer.parseInt(s);
			}
		}
		catch (Exception e) {
		}
		return i;
	}

	public static BigDecimal getBigDecimalValue(String s) {
		BigDecimal value = BigDecimal.ZERO;
		try {
			if (isNotNull(s)) {
				value = new BigDecimal(s);
			}
		}
		catch (Exception e) {
		}
		return value;
	}

	public static String getOrdinalFor(int value) {
		int hundredRemainder = value % 100;
		int tenRemainder = value % 10;
		if (hundredRemainder - tenRemainder == 10) {
			return "th";
		}

		switch (tenRemainder) {
			case 1:
				return "st";
			case 2:
				return "nd";
			case 3:
				return "rd";
			default:
				return "th";
		}
	}

	public static String getStackTrace(Throwable exception) {
		String trace = null;
		if (exception != null) {
			StringWriter strWriter = new StringWriter();
			PrintWriter writer = new PrintWriter(strWriter);
			try {
				exception.printStackTrace(writer);
				trace = strWriter.toString();
				strWriter.close();
				writer.close();
			}
			catch (Exception e) { /* ignore this exception */
			}
		}
		return trace;
	}

	public static boolean equals(String str1, String str2) {
		boolean flag = Text.TRUE;
		if (StringUtil.isNotNull(str1) && StringUtil.isNotNull(str2)) {
			if (!str1.equalsIgnoreCase(str2)) {
				flag = Text.FALSE;
			}
		}
		else {
			flag = Text.FALSE;
		}
		return flag;
	}

	public static String getTextBetweenTags(String InputText, String Tag1, String Tag2) {
		String Result;
		int index1 = InputText.indexOf(Tag1);
		int index2 = InputText.indexOf(Tag2);
		index1 = index1 + Tag1.length();
		Result = InputText.substring(index1, index2);
		return Result;

	}

	public static String substring(String inputText, int maxLength) {
		if (StringUtil.isNull(inputText)) {
			return "";
		}
		if (inputText.length() < maxLength) {
			return inputText.trim();
		}
		return inputText.substring(0, maxLength - 1).trim();
	}

	public static String subStringV2(String inputText, int maxminLength) {
		if (StringUtil.isNull(inputText) || inputText.length() < maxminLength) {
			inputText = inputText + RandomStringUtils.randomAlphanumeric(maxminLength - inputText.length()).toUpperCase();
		}
		return inputText.substring(0, maxminLength - 1).trim();
	}

	public static String _uGC(String l, String n, String s) {
		if (StringUtil.isNull(l) || StringUtil.isNull(n) || StringUtil.isNull(s)) {
			return "-";
		}
		int i, i2, i3;
		String c = "-";
		i = l.indexOf(n);
		i3 = n.indexOf("=") + 1;
		if (i > -1) {
			i2 = l.indexOf(s, i);
			if (i2 < 0) {
				i2 = l.length();
			}
			c = l.substring((i + i3), i2);
		}
		return c;
	}

	public static String replaceAll(String str, String org, String dest) {
		String regex = "\\$\\{" + org + "\\}";
		return str.replaceAll(regex, dest);
	}

	public static String replaceAll(String str, Properties props) {
		String value = str;
		Enumeration<Object> keyEnumrerator = props.keys();
		while (keyEnumrerator.hasMoreElements()) {
			Object org = keyEnumrerator.nextElement();
			if (org instanceof String) {
				String dest = props.getProperty((String) org);
				value = replaceAll(value, (String) org, dest);
			}
		}
		return value;
	}

	public static String removeSymbol(String response) {
		if (StringUtil.isNotNull(response)) {
			char[] resChars = response.toCharArray();
			for (Character resChar : resChars) {
				if (!(resChar >= 48 && resChar <= 57) && !(resChar >= 65 && resChar <= 90) && !(resChar >= 97 && resChar <= 122)) {
					response = response.replaceAll("\\" + resChar.toString(), Text.DOUBLE_QUOTE);
				}
			}
		}
		return response;
	}

	public static String removeSymbolWithSpace(String response) {
		if (StringUtil.isNotNull(response)) {
			char[] resChars = response.toCharArray();
			for (Character resChar : resChars) {
				if (resChar != 32 && !(resChar >= 48 && resChar <= 57) && !(resChar >= 65 && resChar <= 90) && !(resChar >= 97 && resChar <= 122)) {
					response = response.replaceAll("\\" + resChar.toString(), Text.DOUBLE_QUOTE);
				}
			}
		}
		return response;
	}

	public static String removeUnknownSymbol(String response) {
		if (StringUtil.isNotNull(response)) {
			char[] resChars = response.toCharArray();
			for (Character resChar : resChars) {
				if (!(resChar >= 48 && resChar <= 57) && !(resChar >= 65 && resChar <= 90) && !(resChar >= 97 && resChar <= 122) && resChar != 32 && !(resChar >= 40 && resChar <= 46)) {
					response = response.replaceAll("\\" + resChar.toString(), Text.DOUBLE_QUOTE);
				}
			}
		}
		return response;
	}

	/**
	 * @Desc This method returns true if the list empty. else it returns false
	 * @param list
	 * @return
	 * @throws Exception
	 */
	public static boolean checkEmptyList(List<?> list) throws Exception {
		if (list != null && !list.isEmpty()) {
			return true;
		}
		else {
			return false;
		}
	}

	public static boolean isValidEmailId(String email) {
		String stricterFilterString = "[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,15}";
		Pattern p = Pattern.compile(stricterFilterString);
		Matcher m = p.matcher(email);
		if (StringUtil.isNotNull(email) && m.matches()) {
			return true;
		}
		else {
			return false;
		}
	}

	public static String validateEmail(String email) {
		StringBuilder emailIds = new StringBuilder();
		if (StringUtil.isNotNull(email)) {
			List<String> emailIdList = Arrays.asList(email.split(Text.COMMA));
			for (String emailId : emailIdList) {
				if (StringUtil.isNotNull(emailId) && EmailUtil.isValid(emailId)) {
					emailIds.append(emailId);
					emailIds.append(Text.COMMA);
				}
			}
		}
		return emailIds.toString();
	}

	public static boolean isValidMobileNumber(String mobileNo) {
		boolean validMobNo = true;
		try {
			if (StringUtil.isNull(mobileNo)) {
				validMobNo = false;
			}
			else {
				double mobNo = Double.parseDouble(mobileNo);
				if (mobNo < 999999999 && mobileNo.length() > 11) {
					validMobNo = false;
				}
			}
		}
		catch (Exception e) {
			validMobNo = false;
		}
		return validMobNo;
	}

	public static boolean isValidMaskedMobileNumber(String mobileNumber) throws Exception {
		boolean validMobNo = true;
		try {
			Matcher matcher = MASKED_MOBILE_NUMBER_PATTERN.matcher(mobileNumber);
			if (!matcher.matches()) {
				return false;
			}
		}
		catch (Exception e) {
			validMobNo = false;
		}
		return validMobNo;
	}

	public static boolean isContains(String data, String find) {
		if (isNull(data)) {
			return false;
		}
		return data.contains("," + find + ",");
	}

	public static String generateToken(int count) {
		String token = RandomStringUtils.randomAlphanumeric(count);
		return token.toUpperCase();
	}

	public static String getCharForNumber(int i) {
		return i > 0 && i < 27 ? String.valueOf((char) (i + 'A' - 1)) : null;
	}

	@SuppressWarnings("unchecked")
	public static <T> T[] removeDuplicates(T[] arr) {
		return (T[]) new LinkedHashSet<T>(Arrays.asList(arr)).toArray();
	}

	public static boolean isValidJSON(String json) {
		boolean valid = false;
		try {
			if (StringUtil.isNotNull(json)) {
				JSONObject.fromObject(json);
				valid = true;
			}
		}
		catch (JSONException jpe) {
			valid = false;
		}
		catch (Exception ioe) {
			valid = false;
		}

		return valid;
	}

	public static String getMobileNumberMasking(String mobileNumber) {
		if (mobileNumber.length() < 10) {
			return mobileNumber;
		}
		final int start = 2;
		final int end = mobileNumber.length() - 3;
		final String overlay = StringUtils.repeat(MASK_CHAR, end - start);
		return StringUtils.overlay(mobileNumber, overlay, start, end);
	}

	public static boolean isMaskedMobileNumber(String mobileNumber) {
		if (mobileNumber.contains("xxx")) {
			return true;
		}
		return false;
	}

	public static boolean isValidDayOfWeek(String dayOfWeek) {
		boolean valid = false;
		try {
			if (StringUtil.isNotNull(dayOfWeek) && dayOfWeek.length() == 7) {
				valid = true;
			}
		}
		catch (JSONException jpe) {
			valid = false;
		}
		catch (Exception ioe) {
			valid = false;
		}

		return valid;
	}

	public static String getDayOfWeek(String dayOfWeek) {
		StringBuilder convertedDayOfWeek = new StringBuilder();
		if (isValidDayOfWeek(dayOfWeek)) {
			convertedDayOfWeek.append(dayOfWeek.charAt(0) == '1' ? "Sun," : Text.EMPTY);
			convertedDayOfWeek.append(dayOfWeek.charAt(1) == '1' ? "Mon," : Text.EMPTY);
			convertedDayOfWeek.append(dayOfWeek.charAt(2) == '1' ? "Tue," : Text.EMPTY);
			convertedDayOfWeek.append(dayOfWeek.charAt(3) == '1' ? "Wed," : Text.EMPTY);
			convertedDayOfWeek.append(dayOfWeek.charAt(4) == '1' ? "Thu," : Text.EMPTY);
			convertedDayOfWeek.append(dayOfWeek.charAt(5) == '1' ? "Fri," : Text.EMPTY);
			convertedDayOfWeek.append(dayOfWeek.charAt(6) == '1' ? "Sat" : Text.EMPTY);
		}
		return convertedDayOfWeek.toString();
	}

	public static boolean isValidUsername(String username) {
		Pattern pattern = Pattern.compile("([a-zA-Z0-9]+)");
		Matcher matcher = pattern.matcher(username);
		if (!matcher.matches()) {
			return false;
		}
		if (username.length() <= 5) {
			return false;
		}
		return true;
	}

	public static int getSMSCount(String content, String mobileNumber) {
		int numberCount = mobileNumber.split(Text.COMMA).length > 0 ? mobileNumber.split(Text.COMMA).length : 1;
		return (content.length() / 158) + ((content.length() % 158) > 0 ? 1 : 0) * numberCount;
	}
	public static int getWhatsappCount(String content, String mobileNumber) {
		int numberCount = mobileNumber.split(Text.COMMA).length > 0 ? mobileNumber.split(Text.COMMA).length : 1;
		return 1 * numberCount;
	}

	public static String split(String content, String splitter, int position) {
		String data = Text.EMPTY;
		if (StringUtil.isNotNull(content) && content.split("\\" + splitter).length > position) {
			data = content.split("\\" + splitter)[position];
		}
		return data;
	}

	public static String getRightPart(String text, int length) {
		String rightPart = text;
		if (length <= text.length()) {
			rightPart = StringUtils.right(text, length);
		}
		return rightPart;
	}

	public static String generateCancellationPolicyCode(CancellationTermDTO cancellationTerm, CancellationPolicyDTO cancellationPolicy) {
		return StringUtil.getRightPart(cancellationTerm.getCode(), Numeric.THREE_INT) + cancellationPolicy.getFromValue() + cancellationPolicy.getToValue() + Math.abs(cancellationPolicy.getDeductionValue().intValue()) + cancellationPolicy.getPercentageFlag() + cancellationPolicy.getPolicyPattern();
	}

	public static String composeRemarks(String oldRemarks, String newRemarks, int length) {
		String remarks = newRemarks;
		if (StringUtil.isNotNull(oldRemarks)) {
			remarks = oldRemarks + newRemarks;
		}

		if (remarks.length() > length) {
			remarks = remarks.substring(0, length);
		}
		return remarks;
	}

	// Important Note
	public static void commissionReverseCalculation(BigDecimal seatFare, BigDecimal serviceTax, BigDecimal commissionAmount) {
		BigDecimal serviceTaxAmount = commissionAmount.multiply(serviceTax).divide(Numeric.ONE_HUNDRED.add(serviceTax), 2);
		BigDecimal bookingCommissionAmount = serviceTaxAmount.multiply(Numeric.ONE_HUNDRED).divide(serviceTax, 2);
		BigDecimal bookingCommissionPercent = Numeric.ONE_HUNDRED.divide(seatFare, 2).multiply(bookingCommissionAmount);

		/**
		 * x = bookingCommissionPercent
		 * y = bookingCommissionAmount
		 * z = serviceTaxAmount
		 * seatFare = 1000
		 * serviceTax = 18
		 * commissionAmount = 118
		 * 
		 * (1000*x)/100 = y
		 * 10x = y @1
		 * 
		 * (y*18)/100 = z
		 * y = (100*z)/18 @2
		 * 
		 * y+z = 118 @3
		 * 
		 * (100*z)/18 + z = 118
		 * ((100+18)/18)z = 118
		 * z = 18 @3
		 * 
		 * y = (100 * 18) / 18
		 * y = 100 @4
		 * 
		 * 10x = 100
		 * x = 10 @5
		 * 
		 */
	}

	public static String getCoalesce(String value, String nullValue) {
		return StringUtil.isNotNull(value) ? value : Text.HYPHEN;
	}

	public static double similarity(String s1, String s2) {
		String longer = s1, shorter = s2;
		if (s1.length() < s2.length()) {
			longer = s2;
			shorter = s1;
		}
		int longerLength = longer.length();
		if (longerLength == 0) {
			return 1.0;
		}
		return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
	}

	private static int editDistance(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}

	public static void main(String a[]) {
		Pattern pattern = Pattern.compile("\\$\\{[a-zA-Z]+\\}");
		Matcher matcher = pattern.matcher("${originName} to ${destinationName}");
		while (matcher.find()) {
			System.out.println(matcher.group());
		}
	}
}

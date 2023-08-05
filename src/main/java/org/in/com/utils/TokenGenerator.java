package org.in.com.utils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;

public class TokenGenerator {
	public static Map<Integer, String> NumStrMap;
	public static AtomicInteger sequence = new AtomicInteger(-1);

	public static Integer getNextSequence() {
		if (sequence.get() > 999) {
			sequence.getAndSet(-1);
		}
		return sequence.incrementAndGet();
	}

	public static String generateResetToken() {
		return generateShaToken();
	}

	public static String generateShaToken() {
		String token = DigestUtils.sha1Hex(UUID.randomUUID().toString());
		return token;
	}

	public static String generateToken(int count) {
		String token = RandomStringUtils.randomAlphanumeric(count);
		return token;
	}

	public static String generateToken(int count, boolean upperCase) {
		return generateToken(count).toUpperCase();
	}

	public String getSessionTrackingId() {
		String uniqValue = null;
		try {
			// Initialize SecureRandom
			// This is a lengthy operation, to be done only upon
			// initialization of the application
			SecureRandom prng = SecureRandom.getInstance("SHA1PRNG", "SUN");
			// generate a random number
			String randomNum = new Integer(prng.nextInt()).toString();
			// get its digest
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] result = sha.digest(randomNum.getBytes());
			uniqValue = this.hexEncode(result);

		}
		catch (Exception ex) {
			System.err.println(ex);
		}
		return uniqValue.replace("-", "");
	}

	private String hexEncode(byte[] aInput) {
		StringBuilder result = new StringBuilder();
		char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		for (int idx = 0; idx < aInput.length; ++idx) {
			byte b = aInput[idx];
			result.append(digits[(b & 0xf0) >> 4]);
			result.append(digits[b & 0x0f]);
		}
		return result.toString();
	}

	public synchronized static String generateCode(String prefixCode) {
		int sequenceId = 0;
		try {
			sequenceId = getNextSequence();
			DateTime datetime = DateTime.now();
			prefixCode = prefixCode + getNumStrMap(datetime.getYear() % 1000) + getNumStrMap(datetime.getMonthOfYear()) + getNumStrMap(datetime.getDayOfMonth()) + getNumStrMap(datetime.getHourOfDay()) + getNumStrMap(datetime.getMinuteOfHour()) + getNumStrMap(datetime.getSecondOfMinute()) + getNumStrMap(sequenceId);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return prefixCode;
	}

	public synchronized static String generateCode(String prefixCode, int length) {
		try {
			DateTime datetime = DateTime.now();
			prefixCode = prefixCode + getNumStrMap(datetime.getYear() % 1000) + getNumStrMap(datetime.getMonthOfYear()) + getNumStrMap(datetime.getDayOfMonth()) + getNumStrMap(datetime.getHourOfDay()) + getNumStrMap(datetime.getMinuteOfHour()) + getNumStrMap(datetime.getSecondOfMinute()) + getNumStrMap(getNextSequence()) + datetime.getMillisOfSecond();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return StringUtil.subStringV2(prefixCode, length);
	}

	private static String getNumStrMap(int key) {
		if (NumStrMap == null || NumStrMap.isEmpty()) {
			initialize();
		}

		if (NumStrMap.get(key) != null) {
			return NumStrMap.get(key);
		}
		return String.valueOf(key);
	}

	public static void initialize() {
		NumStrMap = new HashMap<Integer, String>();
		NumStrMap.put(0, "0");
		NumStrMap.put(1, "1");
		NumStrMap.put(2, "2");
		NumStrMap.put(3, "3");
		NumStrMap.put(4, "4");
		NumStrMap.put(5, "5");
		NumStrMap.put(6, "6");
		NumStrMap.put(7, "7");
		NumStrMap.put(8, "8");
		NumStrMap.put(9, "9");
		NumStrMap.put(10, "A");
		NumStrMap.put(11, "B");
		NumStrMap.put(12, "C");
		NumStrMap.put(13, "D");
		NumStrMap.put(14, "E");
		NumStrMap.put(15, "F");
		NumStrMap.put(16, "G");
		NumStrMap.put(17, "H");
		NumStrMap.put(18, "I");
		NumStrMap.put(19, "J");
		NumStrMap.put(20, "K");
		NumStrMap.put(21, "L");
		NumStrMap.put(22, "M");
		NumStrMap.put(23, "N");
		NumStrMap.put(24, "O");
		NumStrMap.put(25, "P");
		NumStrMap.put(26, "Q");
		NumStrMap.put(27, "R");
		NumStrMap.put(28, "S");
		NumStrMap.put(29, "T");
		NumStrMap.put(30, "U");
		NumStrMap.put(31, "V");
		NumStrMap.put(32, "W");
		NumStrMap.put(33, "X");
		NumStrMap.put(34, "Y");
		NumStrMap.put(35, "Z");
	}

	public static String generateSequenceAlphabetic(int sequenceNumber) {
		String sequence = String.valueOf(sequenceNumber);
		if (sequenceNumber >= 1000000) {
			sequence = alphabeticSequence((sequenceNumber / 1000000) - 1) + String.format("%05d", sequenceNumber % 1000000);
		}
		return sequence;
	}

	private static String alphabeticSequence(int sequenceNumber) {
		return sequenceNumber < 0 ? "" : alphabeticSequence((sequenceNumber / 26) - 1) + (char) (65 + sequenceNumber % 26);
	}
}

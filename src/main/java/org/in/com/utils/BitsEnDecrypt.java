package org.in.com.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;

public class BitsEnDecrypt {

	private static final String UNICODE_FORMAT = "UTF8";
	public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
	private KeySpec myKeySpec;
	private SecretKeyFactory mySecretKeyFactory;
	private Cipher cipher;
	byte[] keyAsBytes;
	private String myEncryptionKey;
	private String myEncryptionScheme;
	SecretKey key;

	public BitsEnDecrypt() throws Exception {
		myEncryptionKey = "CloudSolutions092843EncryptionKey";
		myEncryptionScheme = DESEDE_ENCRYPTION_SCHEME;
		keyAsBytes = myEncryptionKey.getBytes(UNICODE_FORMAT);
		myKeySpec = new DESedeKeySpec(keyAsBytes);
		mySecretKeyFactory = SecretKeyFactory.getInstance(myEncryptionScheme);
		cipher = Cipher.getInstance(myEncryptionScheme);
		key = mySecretKeyFactory.generateSecret(myKeySpec);
	}

	/**
	 * Method To Encrypt The String
	 */
	public String encrypt(String unencryptedString) {
		String encryptedString = null;
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
			byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
			byte[] encryptedText = cipher.doFinal(plainText);
			encryptedString = Base64.getEncoder().encodeToString(encryptedText);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		encryptedString = encryptedString.replace("=", "()");
		encryptedString = encryptedString.replace("&", "[]");
		encryptedString = encryptedString.replace("+", "{}");
		return encryptedString;
	}

	/**
	 * Method To Decrypt An Ecrypted String
	 */
	public String decrypt(String encryptedString) {
		String decryptedText = "";
		if (encryptedString != null) {
			encryptedString = encryptedString.replace("()", "=");
			encryptedString = encryptedString.replace("[]", "&");
			encryptedString = encryptedString.replace("{}", "+");
			try {
				cipher.init(Cipher.DECRYPT_MODE, key);
				byte[] encryptedText = Base64.getDecoder().decode(encryptedString);
				byte[] plainText = cipher.doFinal(encryptedText);
				decryptedText = bytes2String(plainText);
			}
			catch (Exception e) {
				throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
				// e.printStackTrace();
			}
		}
		return decryptedText;
	}

	/**
	 * Returns String From An Array Of Bytes
	 */
	private String bytes2String(byte[] bytes) {
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			stringBuffer.append((char) bytes[i]);
		}
		return stringBuffer.toString();
	}

	public String getEncrypParams(String[] params) throws Exception {
		String strgEnpt = null;
		for (int i = 0; i < params.length; i++) {
			if (params[i] != null) {
				if (strgEnpt == null) {
					strgEnpt = params[i].trim();
				}
				else {
					strgEnpt += "@@" + params[i].trim();
				}
			}
		}
		return encrypt(strgEnpt);
	}

	public String[] getDecrypParams(String strgEnpt) throws Exception {
		String params = "";
		if (strgEnpt != null) {
			params = decrypt(strgEnpt);
		}
		return params.split("@@");
	}

	public static String getEncoder(String data) {
		Encoder theEncoder = Base64.getEncoder().withoutPadding();
		byte[] theArray = data.getBytes(StandardCharsets.UTF_8);
		String encoderdata = theEncoder.encodeToString(theArray);
		String encoderHash = getMD5Hash(encoderdata);
		StringBuilder encode = new StringBuilder();
		encode.append(encoderHash.substring(0, 16));
		encode.append(encoderdata);
		encode.append(encoderHash.substring(16, 32));
		return encode.toString();
	}

	public static String getBase64URLEncoder(String data) {
		return Base64.getUrlEncoder().encodeToString(data.getBytes(StandardCharsets.UTF_8));
	}

	public static String getBase64URLDecoder(String data) {
		byte[] decodedBytes = Base64.getUrlDecoder().decode(data);
		return new String(decodedBytes);
	}

	// Decoding a base64 encoded String in Java 8
	public static String getDecoder(String decoderdata) {
		String hash = decoderdata.substring(0, 16) + decoderdata.substring(decoderdata.length() - 16, decoderdata.length());
		String decodedata = decoderdata.substring(16, decoderdata.length() - 16);
		// Invalid Encoded String
		if (!getMD5Hash(decodedata).equalsIgnoreCase(hash)) {
			return null;
		}
		Decoder theDecoder = Base64.getDecoder();
		byte[] byteArray = theDecoder.decode(decodedata);
		return new String(byteArray, StandardCharsets.UTF_8);
	}

	private static String bytesToHex(byte[] hash) {
		return DatatypeConverter.printHexBinary(hash);
	}

	private static String getMD5Hash(String data) {
		String result = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] hash = digest.digest(data.getBytes("UTF-8"));
			return bytesToHex(hash); // make it printable
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	public static String getSHA256Hash(String data) {
		String result = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(data.getBytes("UTF-8"));
			return bytesToHex(hash); // make it printable
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}

	/**
	 * Testing The DESede Encryption And Decryption Technique
	 */
	public static void main(String args[]) throws Exception {
		BitsEnDecrypt myEncryptor = new BitsEnDecrypt();
		String params[] = { "EQTHRNTUTKMKIQ1B" };
		String enpt = myEncryptor.getEncrypParams(params);
		System.out.println(enpt);
		String dept[] = myEncryptor.getDecrypParams("mMTopP0SMCKSPQeGO1vE4Gk{}gFgs/qIP");
		// String dept[] =
		// myEncryptor.getDecrypParams("m7eLehy{}0VmlYITpVtcg5mk{}gFgs/qIP");
		for (int i = 0; i < dept.length; i++) {
			System.out.println(dept[i]);
		}
		System.out.println(BitsEnDecrypt.getBase64URLEncoder("2020-09-24 00:49:52"));
		System.out.println(BitsEnDecrypt.getBase64URLDecoder("MjAyMC0dwOS0yNCAwMDo0OTo1Mg"));
	}
}

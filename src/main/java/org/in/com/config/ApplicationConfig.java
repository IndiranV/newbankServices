package org.in.com.config;

import java.util.HashMap;
import java.util.Map;

import org.in.com.constants.Text;

public class ApplicationConfig {
	private final static String ZONE_CODE = "zonecode";
	private final static String ZONE_URL = "zoneurl";
	private final static String ZONE_USERNAME = "zoneusername";
	private final static String ZONE_SUPER_USER = "zonesuperuser";
	private final static String ZONE_TOKEN = "zoneapitoken";
	private final static String SERVER_ENV = "serverenv";
	private final static String CCAVENUE_ACCESS_CODE = "ccaaccesscode";
	private final static String CCAVENUE_SECRET_KEY = "ccasecretkey";
	private final static String S3_BUCKET_NAME = "s3_bucketname";
	private final static String S3_REGION = "s3_region";
	private final static String S3_ACCESS_KEY = "s3_accesskey";
	private final static String S3_SECRET_KEY = "s3_secretkey";
	private final static String MERCSERVICE_ACCESS_URL = "mercservice.access.url";
	private final static String MERCSERVICE_ACCESS_TOKEN = "mercservice.access.token";

	public static Map<String, String> CONFIGMAP = new HashMap<String, String>();

	public static String getServerZoneCode() {
		return CONFIGMAP.get(ZONE_CODE);
	}

	public static String getZoneUsername() {
		return CONFIGMAP.get(ZONE_USERNAME);
	}

	public static String getZoneSuperUser() {
		return CONFIGMAP.get(ZONE_SUPER_USER);
	}

	public static String getServerZoneUrl() {
		return CONFIGMAP.get(ZONE_URL);
	}

	public static String getZoneAPIToken() {
		return CONFIGMAP.get(ZONE_TOKEN);
	}

	public static String getCCAAccessCode(String accessCode) {
		return CONFIGMAP.get(getServerZoneCode() + Text.UNDER_SCORE + accessCode + Text.UNDER_SCORE + CCAVENUE_ACCESS_CODE);

	}

	public static String getCCASecretKey(String accessCode) {
		return CONFIGMAP.get(getServerZoneCode() + Text.UNDER_SCORE + accessCode + Text.UNDER_SCORE + CCAVENUE_SECRET_KEY);
	}

	public static String getServerEnv() {
		return CONFIGMAP.get(SERVER_ENV);
	}

	public static String getS3BucketName() {
		return CONFIGMAP.get(S3_BUCKET_NAME);
	}

	public static String getS3Region() {
		return CONFIGMAP.get(S3_REGION);
	}

	public static String getS3AccessKey() {
		return CONFIGMAP.get(S3_ACCESS_KEY);
	}

	public static String getS3SecretKey() {
		return CONFIGMAP.get(S3_SECRET_KEY);
	}

	public static String getMercServiceAccessurl() {
		return CONFIGMAP.get(MERCSERVICE_ACCESS_URL);
	}

	public static String getMercServiceAccessToken() {
		return CONFIGMAP.get(MERCSERVICE_ACCESS_TOKEN);
	}

}
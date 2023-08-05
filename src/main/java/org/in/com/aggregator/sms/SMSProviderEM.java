package org.in.com.aggregator.sms;

public enum SMSProviderEM {
	DEFAULT(1, "default",  ""),
	QIKBERRY(2, "qikberry", "QikberrySmsGatewayImpl"),
	QIKBERRYOTP(3, "qikberryotp", "QikberryOTPSmsGatewayImpl"),
	INFINI(4, "infini", "SolutionsInfiniImpl"),
	DIAL4SMS(5, "dial4sms", "SolutionsInfiniImpl"),
	SPARK_ALERTS(6, "spark", "SparkAlertsImpl"),
	PAY4SMS(7, "pay4sms", "Pay4SmsGatewayImpl"),
	AAKASHSMS(8, "aakashsms", "AakashSmsGatewayImpl"),
	KALEYRA(9, "kaleyrasms", "KaleyraSmsGatewayImpl");

	private final int id;
	private final String code;
 	private final String impl;

	private SMSProviderEM(int id, String code,   String impl) {
		this.code = code;
		this.id = id;
 		this.impl = impl;
	}

	public int getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public static SMSProviderEM getSMSProviderEM(int id) {
		SMSProviderEM[] values = values();
		for (SMSProviderEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return DEFAULT;
	}

	public static SMSProviderEM getSMSProviderEM(String code) {
		SMSProviderEM[] values = values();
		for (SMSProviderEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return DEFAULT;
	}

	public String getImpl() {
		return impl;
	}

}

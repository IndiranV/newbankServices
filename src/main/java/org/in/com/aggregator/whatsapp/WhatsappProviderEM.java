package org.in.com.aggregator.whatsapp;

public enum WhatsappProviderEM {
	NOT_AVAILABLE(0, "NA", "Not Available", ""),
 	WIZHCOMM(1, "WIZCOM", "Wizhcomm", "WizhcommServiceImpl"),
 	KALEYRA(2, "KLYAEB", "Kaleyra (Ezeebus)", "KaleyraServiceImpl"),
	QIKBERRY(3, "QIKBRY", "Qikberry", "QikberryServiceImpl"),
	MYOPERATOR(4, "MYOPTR", "MyOperator", "MyOperatorServiceImpl");

	private final int id;
	private final String code;
 	private final String name;
 	private final String impl;
 	
	private WhatsappProviderEM(int id, String code,   String name, String impl) {
		this.code = code;
		this.id = id;
 		this.name = name;
 		this.impl = impl;
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

	public String getImpl() {
		return impl;
	}

	public static WhatsappProviderEM getWhatsappProviderEM(int id) {
		WhatsappProviderEM[] values = values();
		for (WhatsappProviderEM errorCode : values) {
			if (errorCode.getId() == id) {
				return errorCode;
			}
		}
		return NOT_AVAILABLE;
	}

	public static WhatsappProviderEM getWhatsappProviderEM(String code) {
		WhatsappProviderEM[] values = values();
		for (WhatsappProviderEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return NOT_AVAILABLE;
	}
}

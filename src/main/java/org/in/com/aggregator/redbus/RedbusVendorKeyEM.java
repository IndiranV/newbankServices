package org.in.com.aggregator.redbus;

public enum RedbusVendorKeyEM {

	DEFAULT_FOUND("NA", "NA"),
	YBM_TRAVELS("ybmtravels", "b29d61abcdd66ec725dca4a6fb2f71f8"),
	RKT_TRAVELS("rkttravels", "2f692464976e44ee627fc4bc56396bee"),
	PARVEEN_TRAVELS("parveen", "ea0f9467aa5032227f0efec4ded6ca0c"),
	JAYALAKSHMI_TRAVELS("jayalakshmi", "9c11141394d26c0a817343e8624c9104"),
	TRANZKING("tranzking", "ab438da900c816e92b57392b4949969f"),
	MJT_TRAVELS("mjttravels", "823dfc9b0183e1b94883011e01412985"),
	VHB_TRAVELS("vhbtravels", "e503447f57953ac79a73061de33b98f8"),
	VKV_TRAVELS("vkvtravels", "ab7a33482e140d4a2b46d20e2d921841"),
	PSS_TRANSPORT("psstransport", "fc5317d7dddf2c3909f38d3c1aa6f083"),
	ASIAN_XPRESS("asianxpress", "af1c30311512c45cf9ed3d1c4a89cedf"),
	SBLT("sblt", "204fd9ccf9bbc75a117023dcbe6a9ddc"),
	GOTOUR("gotour", "f204eb80c4160c7112dde1865d566af6"),
	IRA_TRAVELS("iratravels", "24f85659a6b6a1c6977bee4eb1b849e5"),
	LNTBUS("lntbus", "66c60db7af4989b6306819a52461e0b0"),
	A1_TRAVELS("aonetravels", "d2cd4ee5373b7e288853e11b13712e71"),
	THIRUMALAIVASAN("thirumalaivasan", "0ae2c162ca20d756144dbac870ac0ecb"),
	PUNCHIRY("punchiry", "74db916f6c0e6bf982a92a1929b5d447"),
	JB_CONNECT("jbconnect", "caed5f58be552c493c9178f951480c88"),
	AAKASH_TRAVELSS("aakashtravelss", "a63900baedb334406a47cf1a2b31da42"),
	KMS_TRAVELS("kmstravels", "9ca8f30ba1f0126ae8ff72c9cf21672c"),
	SRI_KULAVILAKKAMMAN_TRAVELS("kulavilakku","eb97e921cc24433b10b0d95a6adb7bca"),
	RITHISH_TRAVELS("rithishtravels","88890ef3bdfea453e88e7cf31580f523"),
	BSPBUS("bspbus","98d19da989b91b10692a1e3d2df46f5b"),
	KRISH_BUS("krishbus","e815bf15571335e525311f209db3a0c5"),
	RAJESH_TRANSPORTS("rajeshtransports","02a91c72e06299d38640c0cd70d110a5"),
	VHBTRAVELS("vhbtravels","e503447f57953ac79a73061de33b98f8"),
	ASIANXPRESS("asianxpress","af1c30311512c45cf9ed3d1c4a89cedf"),
	SRIBALA_TRAVELS("sribalatravels","92c496adb58eca048b204092ad4fbb96");

	private final String namespaceCode;
	private final String vendorKey;

	private RedbusVendorKeyEM(String namespaceCode, String vendorKey) {
		this.namespaceCode = namespaceCode;
		this.vendorKey = vendorKey;
	}

	public static RedbusVendorKeyEM getRedbusVendorKeyEM(String Code) {
		RedbusVendorKeyEM[] values = values();
		for (RedbusVendorKeyEM errorCode : values) {
			if (errorCode.getNamespaceCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return DEFAULT_FOUND;
	}

	public String toString() {
		return namespaceCode + " : " + vendorKey;
	}

	public String getNamespaceCode() {
		return namespaceCode;
	}

	public String getVendorKey() {
		return vendorKey;
	}

}

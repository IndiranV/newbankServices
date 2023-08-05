package org.in.com.dto.enumeration;

public enum StateEM {
	JAMMU_AND_KASHMIR("STATE25", 1, "Jammu and Kashmir", CountryEM.INDIA),
	HIMACHAL_PRADESH("STATE17", 2, "Himachal Pradesh", CountryEM.INDIA),
	PUNJAB("STATE23", 3, "Punjab", CountryEM.INDIA),
	CHANDIGARH("STATE32", 4, "Chandigarh", CountryEM.INDIA),
	UTTARAKHAND("STATE28", 5, "Uttarakhand", CountryEM.INDIA),
	HARYANA("STATE5", 6, "Haryana", CountryEM.INDIA),
	DELHI("STATE24", 7, "Delhi", CountryEM.INDIA),
	RAJASTHAN("STATE14", 8, "Rajasthan", CountryEM.INDIA),
	UTTAR_PRADESH("STATE18", 9, "Uttar Pradesh", CountryEM.INDIA),
	BIHAR("STATE3", 10, "Bihar", CountryEM.INDIA),
	SIKKIM("STATE27", 11, "Sikkim", CountryEM.INDIA),
	ARUNACHAL_PRADESH("STATE1", 12, "Arunachal Pradesh", CountryEM.INDIA),
	NAGALAND("STATE10", 13, "Nagaland", CountryEM.INDIA),
	MANIPUR("STATE7", 14, "Manipur", CountryEM.INDIA),
	MIZORAM("STATE9", 15, "Mizoram", CountryEM.INDIA),
	TRIPURA("STATE26", 16, "Tripura", CountryEM.INDIA),
	MEGHLAYA("STATE22", 17, "Meghlaya", CountryEM.INDIA),
	ASSAM("STATE2", 18, "Assam", CountryEM.INDIA),
	WEST_BENGAL("STATE30", 19, "West Bengal", CountryEM.INDIA),
	JHARKHAND("STATE6", 20, "Jharkhand", CountryEM.INDIA),
	ODISHA("STATE15", 21, "Odisha", CountryEM.INDIA),
	CHATTISGARH("STATE16", 22, "Chattisgarh", CountryEM.INDIA),
	MADHYA_PRADESH("STATE20", 23, "Madhya Pradesh", CountryEM.INDIA),
	GUJARAT("STATE19", 24, "Gujarat", CountryEM.INDIA),
	DAMAN_AND_DIU("NA", 25, "Daman And DIU", CountryEM.INDIA),
	DADRA_AND_NAGAR_HAVELI("NA", 26, "Dadra and Nagar Haveli", CountryEM.INDIA),
	MAHARASHTRA("STATE13", 27, "Maharastra", CountryEM.INDIA),
	ANDHRA_PRADESH("STATE4", 28, "Andhra Pradesh", CountryEM.INDIA),
	KARNATAKA("STATE8", 29, "Karnataka", CountryEM.INDIA),
	GOA("STATE12", 30, "Goa", CountryEM.INDIA),
	KERALA("STATE21", 32, "Kerala", CountryEM.INDIA),
	TAMIL_NADU("STATE11", 33, "Tamilnadu", CountryEM.INDIA),
	PUDUCHERRY("STATE31", 34, "Puducheri", CountryEM.INDIA),
	TELANGANA("STATE29", 36, "Telangana", CountryEM.INDIA),
	ANDHRA_PRADESH_NEW("STATE4", 37, "Andhra Pradesh New", CountryEM.INDIA),
	LAKSHWADEEP("NA", 31, "Lakshwadeep", CountryEM.INDIA),
	ANDAMAN_AND_NICOBAR_ISLANDS("NA", 35, "Andaman And Nicobar Islands", CountryEM.INDIA),
	
	//Nepal
	KOSHI("NPAL01",01,"Koshi",CountryEM.NEPAL),
	MADHESH("NPAL02",02,"Madhesh",CountryEM.NEPAL),
	BAGMATI("NPAL03",03,"Bagmati",CountryEM.NEPAL),
	GANDAKI("NPAL04",04,"Gandaki",CountryEM.NEPAL),
	LUMBINI("NPAL05",05,"Lumbini",CountryEM.NEPAL),
	KARNALI("NPAL06",06,"Karnali",CountryEM.NEPAL),
	SUDURPASHCHIM("NPAL07",07,"Sudurpashchim",CountryEM.NEPAL);

	private final String code;
	private final int gstid;
	private final String name;
	private final CountryEM country;

	private StateEM(String code, int gstid, String name, CountryEM country) {
		this.code = code;
		this.gstid = gstid;
		this.name = name;
		this.country = country;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public int getGstid() {
		return gstid;
	}

	public CountryEM getCountry() {
		return country;
	}

	public static StateEM getStateEMGstID(int gstid) {
		StateEM[] values = values();
		for (StateEM errorCode : values) {
			if (errorCode.getGstid() == gstid) {
				return errorCode;
			}
		}
		return null;
	}

	public static StateEM getStateEM(String code) {
		StateEM[] values = values();
		for (StateEM modeEM : values) {
			if (modeEM.getCode().equalsIgnoreCase(code)) {
				return modeEM;
			}
		}
		return null;
	}

}

package org.in.com.aggregator.push;

import org.in.com.dto.enumeration.UserTagEM;

public enum PartnerEM {

	EZEEINFO(UserTagEM.API_USER_EZ, 1, "Ezeeinfo", "OrbitImpl"), 
	ABIBUS(UserTagEM.API_USER_AB, 2, "Abhibus", "AbhibusImpl"), 
	REDBUS(UserTagEM.API_USER_RB, 3, "Redbus", "RedbusImpl"), 
	PAYTM(UserTagEM.API_USER_PT, 4, "Paytm", "PaytmImpl");

	private final int id;
	private final UserTagEM userTag;
	private final String name;
	private final String impl;

	private PartnerEM(UserTagEM userTag, int id, String name, String impl) {
		this.userTag = userTag;
		this.id = id;
		this.name = name;
		this.impl = impl;
	}

	public Integer getId() {
		return id;
	}

	public UserTagEM getUserTag() {
		return userTag;
	}

	public String getName() {
		return name;
	}
	
	public String getImpl() {
		return impl;
	}

	public String toString() {
		return userTag.getCode() + " : " + id + ":" + name;
	}

	public static PartnerEM getPartnerEM(int id) {
		PartnerEM[] values = values();
		for (PartnerEM partnerEM : values) {
			if (partnerEM.getId() == id) {
				return partnerEM;
			}
		}
		return null;
	}

	public static PartnerEM getPartnerEM(String Code) {
		PartnerEM[] values = values();
		for (PartnerEM errorCode : values) {
			if (errorCode.getUserTag().getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}
}

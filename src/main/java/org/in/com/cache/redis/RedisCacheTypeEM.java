package org.in.com.cache.redis;

public enum RedisCacheTypeEM {

	TRIP_DETAILS("TRIP_DETAILS", 1440),
	ONE_DAY_CACHE("ONE_DAY_CACHE", 1440),
	TEN_DAY_CACHE("TEN_DAY_CACHE", 14400),
	TRIP_DATA_CACHE("TRIP_DATA_CACHE", 86400);

	private final String code;
	private final int timeToLive;

	private RedisCacheTypeEM(String code, int timeToLive) {
		this.code = code;
		this.timeToLive = timeToLive;
	}

	public String getCode() {
		return code;
	}

	public int getTimeToLive() {
		return timeToLive;
	}

	public String toString() {
		return code + " : " + timeToLive;
	}

}

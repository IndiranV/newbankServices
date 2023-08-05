package org.in.com.aggregator.fcm;

public enum FCMServerKeyEM {

	TRANZKING("tranzking", "AAAAnQRnG3k:APA91bFFqboqenI8zftknjAaXN7kzFDn2LYqiZa5NmTZWfrCTc0lvHVybZoEEbnNv7tmku2_6ojw3eKNiH1qB4ce44I2fK7p6Atv_LjbDSnn12vUWN_RaspxQUu4T5QWc-r-7GMKv7zp"),
	YBMTRAVELS("ybmtravels", "AAAAeI3A4Us:APA91bG92NakFIr9itIxwUkwbapflyBwTzRXq1uA8V9EZ1s7k-E6xvzUv82bpUVyPZyBXiSOXgod6b-0CzV2q_LFd9_w-R88veQV-8rCANmeWtSnoWiGkNqKVhZalKmhwJpikKrwQ9Ei"),
	TATTRAVELS("tattravels", "AAAAjCp5W-w:APA91bGvRXXmLoW5v_k4dtAOunwi8FPyjtsOH4jDQPA5lX5k6jnGGNOPcJ8wxZShENMvAKgLlI-eJxS0Kj0vFgOLOIIbF7clJtnkpxfTyWL6IxHW0YgOtmJl-FgDklmqNqIW--D87Twj"),
	PARVEENTRAVELS("parveen", "AAAAzIM8DVQ:APA91bHQuLJCV3VcDcHd9FL-bHI5yDaZgpXh_7HY1-OE3DEQCJ1S_b9yif0DPBDdWbgK0-bCb2Kt4e7hV2MsRJfSarNyYwjZk9fkyCxBtPOQSEHPZwWL_C8iZ-0uzuBW1gHuR8XPQB3f"),
	BUS_BUDDY("busbuddy", "AAAAwCFCwmo:APA91bFPp87hb4Lm-bb4P0WPdh3ZiCZ_Vof3LU2ZlTOHik45ZpouqeD5MM6hAWTAXzLTmW9HRmk9iNI16eB86PphRuw-r0VhrJyLMeqedYAf4lXYnFGsm2CN1LeqsR0_YvxzpMq_KrI-"),
	EZEEBOT("ezeebot", "AAAAZK0z2Co:APA91bFonWyfWnjF4Lc93FViHA-zXJQwCqBjBzv5CXw2HgogpR_Klqq8Q5I1zS7UG_JvbYIgIuitV16Q3s-cCFLLJ7Cnd2uXLydl2fi3dKOmCrLB8qj5Yo3r-jhTFWMWwKQod8Kc6lgN"),
	JAGANTRAVELS("jagantravels", "AAAAtWHKApo:APA91bFv3kdQ6VWNp0gvzXQCof8efH8qat1k5Okl2ECFYGGlNgZ301bo50R6iIr1AP5ItVoWUulaE2hHTC4kjPMJQ9VgBPfA7AyfjwPfQ5WyLjrcCRQdSxJLz8SZMMnj9FzLfZMX8ucb");

	private final String code;
	private final String key;

	private FCMServerKeyEM(String code, String key) {
		this.code = code;
		this.key = key;
	}

	public String getCode() {
		return code;
	}

	public String getKey() {
		return key;
	}

	public static FCMServerKeyEM getFCMServerKeyEM(String namespaceCode) {
		FCMServerKeyEM[] values = values();
		for (FCMServerKeyEM code : values) {
			if (code.getCode().equalsIgnoreCase(namespaceCode)) {
				return code;
			}
		}
		return null;
	}

}

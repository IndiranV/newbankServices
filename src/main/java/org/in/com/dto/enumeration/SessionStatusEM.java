package org.in.com.dto.enumeration;

public enum SessionStatusEM {

	SESSION_INACTIVE(0, "INACT", "In Active Session"),
	SESSION_ACTIVE(1, "ACT", "Active Session"),
	SESSION_END_BY_USER(2, "SEBU", "Session end by user"),
	SESSION_END_BY_SYSTEM(3, "SEBS", "Session end by System"),
	SESSION_TERMINATED_BY_USER(4, "SETU", "Session Terminated by user");

	private final int id;
	private final String code;
	private final String name;

	private SessionStatusEM(int id, String code, String name) {
		this.code = code;
		this.id = id;
		this.name = name;
	}

	public Integer getId() {

		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	public String toString() {
		return code + " : " + id;
	}

	public static SessionStatusEM getSessionStatusEMEM(int id) {
		SessionStatusEM[] values = values();
		for (SessionStatusEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return null;
	}

	public static SessionStatusEM getSessionStatusEMEM(String Code) {
		SessionStatusEM[] values = values();
		for (SessionStatusEM typeEM : values) {
			if (typeEM.getCode().equalsIgnoreCase(Code)) {
				return typeEM;
			}
		}
		return null;
	}
}

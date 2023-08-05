package org.in.com.dto.enumeration;

public enum ReleaseModeEM {

	RELEASE_SCHEDULE("SCH", 1, "Scchedule Based"), RELEASE_STAGE("STG", 2, "Stage Based");

	private final int id;
	private final String code;
	private final String name;

	private ReleaseModeEM(String code, int id, String name) {
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

	public String toString() {
		return code + " : " + id + ": " + name;
	}

	public static ReleaseModeEM getReleaseModeEM(int id) {
		ReleaseModeEM[] values = values();
		for (ReleaseModeEM code : values) {
			if (code.getId() == id) {
				return code;
			}
		}
		return null;
	}

	public static ReleaseModeEM getReleaseModeEM(String code) {
		ReleaseModeEM[] values = values();
		for (ReleaseModeEM modeDTO : values) {
			if (modeDTO.getCode().equalsIgnoreCase(code)) {
				return modeDTO;
			}
		}
		return null;
	}

}

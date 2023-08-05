package org.in.com.dto.enumeration;

public enum CalendarAnnouncementCategoryEM {
	
	PEAK_DAY("PEAK", 1, "Peak Day"), 
	HOLIDAY("HLDY", 2, "Holiday");

	private final int id;
	private final String code;
	private final String name;

	private CalendarAnnouncementCategoryEM(String code, int id, String name) {
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
		return code + " : " + id + ":" + name;
	}

	public static CalendarAnnouncementCategoryEM getCategoryEM(int id) {
		CalendarAnnouncementCategoryEM[] values = values();
		for (CalendarAnnouncementCategoryEM categoryDTO : values) {
			if (categoryDTO.getId() == id) {
				return categoryDTO;
			}
		}
		return null;
	}

	public static CalendarAnnouncementCategoryEM getCategoryEM(String Code) {
		CalendarAnnouncementCategoryEM[] values = values();
		for (CalendarAnnouncementCategoryEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return null;
	}
}

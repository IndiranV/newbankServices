package org.in.com.dto.enumeration;


public enum MediaTypeEM {

	AUDIO("AUDIO", 1, "Audio"), 
	TEXT("TEXT", 2, "Text"),
	IMAGE("IMAGE", 3, "Image");

	private final int id;
	private final String code;
	private final String name;

	private MediaTypeEM (String code, int id, String name) {
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

	public static MediaTypeEM getMediaTypeEM(int id) {
		MediaTypeEM[] values = values();
		for (MediaTypeEM messageType : values) {
			if (messageType.getId() == id) {
				return messageType;
			}
		}
		return null;
	}

	public static MediaTypeEM getMediaTypeEM(String Code) {
		MediaTypeEM[] values = values();
		for (MediaTypeEM messageType : values) {
			if (messageType.getCode().equalsIgnoreCase(Code)) {
				return messageType;
			}
		}
		return null;
	}
}

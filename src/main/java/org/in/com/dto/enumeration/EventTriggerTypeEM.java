package org.in.com.dto.enumeration;

public enum EventTriggerTypeEM {

	TICKET_PHONE_BOOK_AUTO_RELEASE("TAR", 1, "Phone Book auto release"),
	TICKET_PHONE_BOOK_AUTO_CONFIRM("PBTAC", 2, "Phone Book ticket auto confirm");

	private final int id;
	private final String code;
	private final String name;

	private EventTriggerTypeEM(String code, int id, String name) {
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

	public static EventTriggerTypeEM getEventTriggerTypeEM(int id) {
		EventTriggerTypeEM[] values = values();
		for (EventTriggerTypeEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		System.out.println("Event Trigger Type Not Found: " + id);
		return null;
	}

	public static EventTriggerTypeEM getEventTriggerTypeEM(String Code) {
		EventTriggerTypeEM[] values = values();
		for (EventTriggerTypeEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		System.out.println("Event Trigger Type Not Found: " + Code);
		return null;
	}
}

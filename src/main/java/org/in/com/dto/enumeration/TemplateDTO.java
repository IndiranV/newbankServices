package org.in.com.dto.enumeration;

public enum TemplateDTO {

	TICKET_PDF("TICKET-PDF", "Ticket PDF"),
	TICKET_CANCEL_EMAIL("CANCEL-EMAIL", "Cancel Ticket EMAIL"),
	TICKET_EMAIL("TICKET-EMAIL", "Ticket Email");

	private final String code;
	private final String name;
	private String template;

	private TemplateDTO(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public static TemplateDTO getTemplateDTO(String code) {
		TemplateDTO[] values = values();
		for (TemplateDTO modeDTO : values) {
			if (modeDTO.getCode().equalsIgnoreCase(code)) {
				return modeDTO;
			}
		}
		return null;
	}
}

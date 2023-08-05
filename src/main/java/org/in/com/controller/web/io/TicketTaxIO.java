package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketTaxIO extends BaseIO {
	private String gstin;
	private String tradeName;
	private String email;
}

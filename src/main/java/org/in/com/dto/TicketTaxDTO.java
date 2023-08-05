package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketTaxDTO extends BaseDTO<TicketTaxDTO> {
	private String gstin;
	private String tradeName;
	private String email;

}

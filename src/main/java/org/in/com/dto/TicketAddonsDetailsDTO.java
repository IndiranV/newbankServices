package org.in.com.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketAddonsDetailsDTO extends BaseDTO<TicketAddonsDetailsDTO> {
	private int ticketId;
	private int refferenceId;
	private String refferenceCode;
	private String seatCode;
	private TicketStatusEM ticketStatus;
	private AddonsTypeEM addonsType;
	private BigDecimal value;

	public int getTicketDetailsId(List<TicketDetailsDTO> list) {
		if (list != null && !list.isEmpty()) {
			for (TicketDetailsDTO dto : list) {
				if (dto.getSeatCode().equals(seatCode)) {
					return dto.getId();
				}
			}
		}
		return 0;
	}
}

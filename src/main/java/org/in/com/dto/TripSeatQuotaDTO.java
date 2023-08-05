package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Text;
import org.in.com.utils.StringUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripSeatQuotaDTO extends BaseDTO<TripSeatQuotaDTO> {
	private TripDTO trip;
	private UserDTO user;
	private StationDTO fromStation;
	private StationDTO toStation;
	private int relaseMinutes;
	private String remarks;
	private String updatedAt;
	private UserDTO updatedBy;
	private TicketDetailsDTO seatDetails;

	public String getRemark() {
		String remark = Text.EMPTY;
		if (StringUtil.isNotNull(remarks) && remarks.length() > 120) {
			remark = remarks.substring(0, 119);
		}
		else {
			remark = remarks;
		}
		return remark;
	}
}

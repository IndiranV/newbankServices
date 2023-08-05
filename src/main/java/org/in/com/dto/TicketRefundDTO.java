package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.RefundStatusEM;
import org.in.com.utils.DateUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketRefundDTO extends BaseDTO<TicketRefundDTO> {
	private String bookingCode;
	private String ticketCode;
	private String transactionCode;
	private StationDTO fromStation;
	private StationDTO toStation;
	private DateTime tripDate;
	private int travelMinutes;
	private String bookedAt;
	private String canncelledAt;
	private String passegerMobleNo;
	private String passegerEmailId;
	private BigDecimal totalRefundAmount;
	private int seatCount;
	private String remarks;
	private String travelTime;
	private RefundStatusEM refundStatus;

	public String getTripDateTime() {
		return DateUtil.addMinituesToDate(tripDate, travelMinutes).format("YYYY-MM-DD hh:mm:ss");
	}

}

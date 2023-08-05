package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.utils.StringUtil;

@Data
public class TicketExtraDTO {
	private int sequenceNumber;
	private int blockReleaseMinutes;
	private int phoneBookPaymentStatus;
	private String linkPay;
	private String offlineTicketCode;
	private DateTime releaseAt;
	private DateTime ticketAt;
	private BigDecimal netAmount;
	private BigDecimal acBusTax = BigDecimal.ZERO;
	private int ticketTransferMinutes;

	private int editBoardingPoint;
	private int editDroppingPoint;
	private int editPassengerDetails;
	private int editChangeSeat;
	private int editMobileNumber;
	private int ticketTransfer;
	private int ticketAfterTripTime;
	private TravelStatusEM travelStatus;
	private Map<String, String> additionalAttributes = new HashMap<String, String>();

	public boolean isExtraExists() {
		return sequenceNumber != 0 || blockReleaseMinutes != 0 || StringUtil.isNotNull(linkPay) || phoneBookPaymentStatus != 0 || editBoardingPoint != 0 || editDroppingPoint != 0 || editPassengerDetails != 0 || editChangeSeat != 0 || editMobileNumber != 0 || ticketTransfer != 0 || StringUtil.isNotNull(offlineTicketCode) ? true : false;
	}

	public String getTicketEditDetails() {
		StringBuilder ticketEditDetails = new StringBuilder();
		ticketEditDetails.append(editBoardingPoint).append(editDroppingPoint).append(editPassengerDetails).append(editChangeSeat).append(editMobileNumber).append(ticketTransfer);
		return ticketEditDetails.toString();
	}
}

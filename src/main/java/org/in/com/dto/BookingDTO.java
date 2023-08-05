package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.utils.StringUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookingDTO extends BaseDTO<BookingDTO> {
	private List<TicketDTO> ticketList;

	private String transactionDate;

	private String passengerMobile;
	private String passengerEmailId;
	private boolean phoneBookingFlag;
	private boolean roundTripFlag = false;
	private boolean bookAfterTripTimeFlag = false;
	private boolean freeServiceFlag = false;
	private String couponCode;
	private BigDecimal manualDiscountAmount;
	private BigDecimal agentServiceCharge;
	private String offlineUserCode;
	private String offlineDiscountCode;
	private Map<String, String> aggregate;
	private Map<String, String> additionalAttributes;
	// Payment Process

	private String paymentGatewayPartnerCode;
	private boolean paymentGatewayProcessFlag;
	private int updatedBy;
	private String updatedAt;
	private TransactionModeEM transactionMode;
	private NamespaceDTO namespace;

	public void setBookingDTO(BookingDTO bookingDTO) {
		if (bookingDTO != null) {
			setCode(bookingDTO.getCode());
			namespace = bookingDTO.getNamespace();
			paymentGatewayPartnerCode = bookingDTO.getPaymentGatewayPartnerCode();
			transactionMode = bookingDTO.getTransactionMode();
			roundTripFlag = bookingDTO.isRoundTripFlag();
			phoneBookingFlag = bookingDTO.isPhoneBookingFlag();
			paymentGatewayProcessFlag = bookingDTO.isPaymentGatewayProcessFlag();
			passengerEmailId = bookingDTO.getPassengerEmailId();
			passengerMobile = bookingDTO.getPassengerMobile();
			additionalAttributes = bookingDTO.getAdditionalAttributes();
		}
	}

	public TicketDTO getTicketDTO(JourneyTypeEM journeyType) {
		TicketDTO ticket = null;
		if (journeyType != null && ticketList != null && !ticketList.isEmpty()) {
			for (TicketDTO dto : ticketList) {
				if (journeyType.getId() == dto.getJourneyType().getId()) {
					ticket = dto;
				}
			}
		}
		return ticket;
	}

	public int getLookupId(TicketDTO ticket) {
		int lookupId = 0;
		for (TicketDTO dto : ticketList) {
			if (dto.getId() != ticket.getId()) {
				lookupId = dto.getId();
			}
		}
		return lookupId;
	}

	public void addTicketDTO(TicketDTO ticket) {
		if (ticketList == null) {
			ticketList = new ArrayList<TicketDTO>();
		}
		ticketList.add(ticket);
	}

	public DateTime getBlockingLiveTime() {
		DateTime dateTime = DateTime.now(TimeZone.getDefault());
		if (ticketList != null && !ticketList.isEmpty())
			for (TicketDTO ticket : ticketList) {
				if (ticket.getBlockingLiveTime() != null) {
					dateTime = ticket.getBlockingLiveTime();
				}
			}
		return dateTime;
	}

	public BigDecimal getTransactionAmount() {
		BigDecimal transactionAmount = BigDecimal.ZERO;
		for (TicketDTO ticket : ticketList) {
			for (TicketDetailsDTO ticketDetailsDTO : ticket.getTicketDetails()) {
				transactionAmount = transactionAmount.add(ticketDetailsDTO.getSeatFare());
				transactionAmount = transactionAmount.add(ticketDetailsDTO.getAcBusTax());
			}
			if (ticket.getTicketAddonsDetails() != null && !ticket.getTicketAddonsDetails().isEmpty()) {
				for (TicketAddonsDetailsDTO addonsDetailsDTO : ticket.getTicketAddonsDetails()) {
					if ("Cr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
						transactionAmount = transactionAmount.subtract(addonsDetailsDTO.getValue());
					}
					else if ("Dr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
						transactionAmount = transactionAmount.add(addonsDetailsDTO.getValue());
					}
				}
			}
		}
		return transactionAmount;
	}

	public BigDecimal getTotalSeatFare() {
		BigDecimal totalSeatFare = BigDecimal.ZERO;
		for (TicketDTO ticket : ticketList) {
			totalSeatFare = totalSeatFare.add(ticket.getTotalSeatFare());
		}
		return totalSeatFare;
	}

	public String getPassengerName() {
		if (ticketList != null && !ticketList.isEmpty()) {
			return ticketList.get(0).getTicketDetails().get(0).getPassengerName();
		}
		return "";
	}

	public int getTicketSeatCount() {
		int seatCount = 0;
		for (TicketDTO ticket : ticketList) {
			seatCount = seatCount + ticket.getTicketDetails().size();
		}
		return seatCount;
	}

	public UserDTO getTicketUserDTO() {
		for (TicketDTO ticket : ticketList) {
			return ticket.getTicketUser();
		}
		return null;
	}

	public BigDecimal getGoGreenAmount() {
		if (additionalAttributes != null && StringUtil.isNotNull(additionalAttributes.get("GO_GREEN"))) {
			return StringUtil.getBigDecimalValue(additionalAttributes.get("GO_GREEN"));
		}
		return BigDecimal.ZERO;
	}

	public BigDecimal getAddonTotalAmount() {
		BigDecimal transactionAmount = BigDecimal.ZERO;
		for (TicketDTO ticket : ticketList) {
			if (ticket.getTicketAddonsDetails() != null && !ticket.getTicketAddonsDetails().isEmpty()) {
				for (TicketAddonsDetailsDTO addonsDetailsDTO : ticket.getTicketAddonsDetails()) {
					if ("Cr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
						transactionAmount = transactionAmount.subtract(addonsDetailsDTO.getValue());
					}
					else if ("Dr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
						transactionAmount = transactionAmount.add(addonsDetailsDTO.getValue());
					}
				}
			}
		}
		return transactionAmount;
	}

	public String getJourneyType() {
		StringBuilder journeyType = new StringBuilder();
		if (ticketList != null && !ticketList.isEmpty()) {
			for (TicketDTO dto : ticketList) {
				journeyType.append(dto.getJourneyType().getName());
				journeyType.append(Text.SINGLE_SPACE);
			}
		}
		return journeyType.toString().trim();
	}
}

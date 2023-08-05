package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import com.google.gson.Gson;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketDTO extends BaseDTO<TicketDTO> {
	private String bookingCode;
	private DateTime tripDate;
	private int travelMinutes;
	private DateTime blockingLiveTime;
	private int reportingMinutes;
	private String namespaceCode;

	private String passengerMobile;
	private String passengerEmailId;
	private String serviceNo;
	private int otpNumber;
	private String alternateMobile;

	private JourneyTypeEM journeyType;
	private DeviceMediumEM deviceMedium;
	private String remarks;
	private String creditDebitFlag;

	private String pnrStartCode;

	private DateTime ticketAt;
	private DateTime updatedAt;

	private boolean overideFlag;
	private boolean myAccountFlag;
	private BigDecimal cancellationOverideRefundAmount;
	private boolean cancellationOveridePercentageFlag;
	private BigDecimal cancellationOverideValue;

	private TripDTO tripDTO;
	private UserDTO ticketUser;
	private UserDTO ticketForUser;
	private TransactionTypeEM transactionType;
	private TransactionModeEM transactionMode;
	private StationDTO fromStation;
	private StationDTO toStation;
	private StationPointDTO boardingPoint;
	private StationPointDTO droppingPoint;
	private TicketStatusEM ticketStatus;
	private List<TicketDetailsDTO> ticketDetails;
	private List<TicketAddonsDetailsDTO> ticketAddonsDetails;
	private CancellationTermDTO cancellationTerm;
	private NamespaceTaxDTO tax;

	private UserTransactionDTO userTransaction;
	private TicketTransactionDTO ticketXaction;
	private String relatedTicketCode;
	private int lookupId;
	private TicketExtraDTO ticketExtra;
	private ScheduleTicketTransferTermsDTO scheduleTicketTransferTerms;
	private String customerIdProof;
	private DateTime instantCancellationTill;

	public String toString() {
		return String.format("Ticket[id=%d, bookingCode='%s', code='%s']", getId(), getBookingCode(), getCode());
	}

	public String toJSON() {
		Gson gson = new Gson();
		if (this != null) {
			gson.toJson(this);
		}
		return gson.toJson(this);
	}

	public List<TicketDetailsDTO> getBookedTicketDetails(TicketStatusEM ticketStatus) {
		List<TicketDetailsDTO> list = new ArrayList<>();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			if (ticketDetailsDTO.getTicketStatus().getId() == ticketStatus.getId()) {
				list.add(ticketDetailsDTO);
			}
		}
		return list;
	}

	public String getPassengerGendar() {
		StringBuilder builder = new StringBuilder();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			if (!builder.toString().isEmpty()) {
				builder.append(",");
			}
			builder.append(ticketDetailsDTO.getSeatGendar().getCode());
		}
		return builder.toString();
	}

	public String getSeatNames() {
		StringBuilder builder = new StringBuilder();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			if (!builder.toString().isEmpty()) {
				builder.append(",");
			}
			builder.append(ticketDetailsDTO.getSeatName());
		}
		return builder.toString();
	}

	public String getSeatCodes() {
		StringBuilder builder = new StringBuilder();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			if (!builder.toString().isEmpty()) {
				builder.append(",");
			}
			builder.append(ticketDetailsDTO.getSeatCode());
		}
		return builder.toString();
	}

	public List<String> getSeatCodeList() {
		List<String> seatList = new ArrayList<String>();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			seatList.add(ticketDetailsDTO.getSeatCode());
		}
		return seatList;
	}

	public String getSeatCodeNames() {
		StringBuilder builder = new StringBuilder();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			if (!builder.toString().isEmpty()) {
				builder.append(Text.COMMA);
			}
			builder.append(ticketDetailsDTO.getSeatCode());
			builder.append(Text.HYPHEN);
			builder.append(ticketDetailsDTO.getSeatName());
			builder.append(Text.HYPHEN);
			builder.append(ticketDetailsDTO.getPassengerName());
			builder.append(Text.HYPHEN);
			builder.append(ticketDetailsDTO.getTravelStatus().getCode());
		}
		return builder.toString();
	}

	public String getPassengerNames() {
		StringBuilder builder = new StringBuilder();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			if (!builder.toString().isEmpty()) {
				builder.append(",");
			}
			builder.append(ticketDetailsDTO.getPassengerName());
		}
		return builder.toString();
	}

	public String getPassengerName() {
		StringBuilder builder = new StringBuilder();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			builder.append(ticketDetailsDTO.getPassengerName());
			break;
		}
		return builder.toString();
	}

	public BigDecimal getTotalSeatFare() {
		BigDecimal totalFare = BigDecimal.ZERO;
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			totalFare = totalFare.add(ticketDetailsDTO.getSeatFare());
		}
		return totalFare.setScale(2, RoundingMode.HALF_UP);
	}

	public int getAverageSeatFare() {
		BigDecimal totalFare = BigDecimal.ZERO;
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			totalFare = totalFare.add(ticketDetailsDTO.getSeatFare());
		}
		BigDecimal actualFare = totalFare.subtract(getAddonsValue());
		int averageSeatFare = Numeric.ZERO_INT;
		if (actualFare.compareTo(BigDecimal.ZERO) == 1) {
			averageSeatFare = totalFare.intValue() / ticketDetails.size();
		}
		return averageSeatFare;
	}

	public BigDecimal getTotalFare() {
		BigDecimal totalFare = BigDecimal.ZERO;
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			totalFare = totalFare.add(ticketDetailsDTO.getSeatFare().add(ticketDetailsDTO.getAcBusTax()));
		}
		return totalFare.setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal getActualBookedSeatFare(TicketDetailsDTO ticketDetailsDTO) {
		BigDecimal actualBookedSeatFare = ticketDetailsDTO.getSeatFare().add(ticketDetailsDTO.getAcBusTax());
		if (ticketAddonsDetails != null && !ticketAddonsDetails.isEmpty()) {
			for (TicketAddonsDetailsDTO addonsDetailsDTO : ticketAddonsDetails) {
				if (AddonsTypeEM.TRANSFER_PREVIOUS_TICKET_AMOUNT.getId() == addonsDetailsDTO.getAddonsType().getId()) {
					continue;
				}
				if (addonsDetailsDTO.getSeatCode().equals(ticketDetailsDTO.getSeatCode())) {
					if ("Cr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
						actualBookedSeatFare = actualBookedSeatFare.subtract(addonsDetailsDTO.getValue());
					}
					else if ("Dr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
						actualBookedSeatFare = actualBookedSeatFare.add(addonsDetailsDTO.getValue());
					}
				}
			}
		}
		return actualBookedSeatFare;
	}

	public BigDecimal getTicketFareWithAddons() {
		BigDecimal totalFare = BigDecimal.ZERO;
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			totalFare = totalFare.add(ticketDetailsDTO.getSeatFare().add(ticketDetailsDTO.getAcBusTax()));
		}
		if (ticketAddonsDetails != null && !ticketAddonsDetails.isEmpty()) {
			for (TicketAddonsDetailsDTO addonsDetailsDTO : ticketAddonsDetails) {
				if (AddonsTypeEM.TRANSFER_PREVIOUS_TICKET_AMOUNT.getId() == addonsDetailsDTO.getAddonsType().getId() || AddonsTypeEM.PG_SERVICE_CHARGE.getId() == addonsDetailsDTO.getAddonsType().getId()) {
					continue;
				}
				if ("Cr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
					totalFare = totalFare.subtract(addonsDetailsDTO.getValue());
				}
				else if ("Dr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
					totalFare = totalFare.add(addonsDetailsDTO.getValue());
				}
				else if ("NA".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
					totalFare = totalFare.add(addonsDetailsDTO.getValue());
				}
			}
		}
		return totalFare.setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal getAcBusTax() {
		BigDecimal totalAcBusTax = BigDecimal.ZERO;
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			totalAcBusTax = totalAcBusTax.add(ticketDetailsDTO.getAcBusTax());
		}
		return totalAcBusTax.setScale(2, RoundingMode.HALF_UP);
	}

	public List<BigDecimal> getSeatFareUniqueList() {
		Set<BigDecimal> hashsetList = new HashSet<BigDecimal>();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			hashsetList.add(ticketDetailsDTO.getSeatFare());
		}
		return new ArrayList<BigDecimal>(hashsetList);
	}

	public BigDecimal getAddonsValue() {
		BigDecimal totalValue = BigDecimal.ZERO;
		if (ticketAddonsDetails != null && !ticketAddonsDetails.isEmpty()) {
			for (TicketAddonsDetailsDTO addonsDetailsDTO : ticketAddonsDetails) {
				if (!addonsDetailsDTO.getAddonsType().isRefundable()) {
					continue;
				}
				if ("Cr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
					totalValue = totalValue.add(addonsDetailsDTO.getValue());
				}
				else if ("Dr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
					totalValue = totalValue.subtract(addonsDetailsDTO.getValue());
				}
			}
		}
		return totalValue.setScale(2, RoundingMode.HALF_UP);
	}

	// Go Green
	public BigDecimal getDebitAddonsValue() {
		BigDecimal totalValue = BigDecimal.ZERO;
		if (ticketAddonsDetails != null && !ticketAddonsDetails.isEmpty()) {
			for (TicketAddonsDetailsDTO addonsDetailsDTO : ticketAddonsDetails) {
				if (addonsDetailsDTO.getAddonsType().isRefundable() || addonsDetailsDTO.getAddonsType().getId() == AddonsTypeEM.TRANSFER_PREVIOUS_TICKET_AMOUNT.getId()) {
					continue;
				}
				totalValue = totalValue.add(addonsDetailsDTO.getValue());
			}
		}
		return totalValue.setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal getCancellationCharges() {
		BigDecimal cancellationCharges = BigDecimal.ZERO;
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			cancellationCharges = cancellationCharges.add(ticketDetailsDTO.getCancellationCharges());
		}
		return cancellationCharges.setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal getTotalCancellationChargeTaxAmount() {
		BigDecimal cancellationChargeTaxAmount = BigDecimal.ZERO;
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			cancellationChargeTaxAmount = cancellationChargeTaxAmount.add(ticketDetailsDTO.getCancellationChargeTax());
		}
		return cancellationChargeTaxAmount.setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal getRefundAmount() {
		BigDecimal refundAmount = BigDecimal.ZERO;
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			refundAmount = refundAmount.add(ticketDetailsDTO.getRefundAmount()).add(ticketDetailsDTO.getAcBusTax()).subtract(getAddonsValue(ticketDetailsDTO));
		}
		return refundAmount.setScale(2, RoundingMode.HALF_UP);
	}

	public BigDecimal getAddonsValue(TicketDetailsDTO ticketDetailsDTO) {
		BigDecimal totalValue = BigDecimal.ZERO;
		if (ticketAddonsDetails != null && !ticketAddonsDetails.isEmpty()) {
			for (TicketAddonsDetailsDTO addonsDetailsDTO : ticketAddonsDetails) {
				if (!addonsDetailsDTO.getAddonsType().isRefundable()) {
					continue;
				}
				if (addonsDetailsDTO.getSeatCode().equals(ticketDetailsDTO.getSeatCode())) {
					if ("Cr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
						totalValue = totalValue.add(addonsDetailsDTO.getValue());
					}
					else if ("Dr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
						totalValue = totalValue.subtract(addonsDetailsDTO.getValue());
					}
				}
			}
		}
		return totalValue.setScale(2, RoundingMode.HALF_UP);
	}

	public String getTripTime() {
		return DateUtil.addMinituesToDate(tripDate, travelMinutes).format("hh12:mm a", Locale.forLanguageTag("en_IN"));
	}

	public DateTime getTripDateTime() {
		return DateUtil.addMinituesToDate(tripDate, travelMinutes);
	}

	public String getReportingTime() {
		return DateUtil.addMinituesToDate(tripDate, reportingMinutes).format("hh12:mm a", Locale.forLanguageTag("en_IN"));
	}

	public DateTime getBoardingPointDateTime() {
		return DateUtil.addMinituesToDate(tripDate, boardingPoint.getMinitues());
	}

	public DateTime getDroppingPointDateTime() {
		return DateUtil.addMinituesToDate(tripDate, droppingPoint.getMinitues());
	}

	public String getTicketEvent() {
		StringBuilder builder = new StringBuilder();
		if (StringUtil.isNotNull(relatedTicketCode)) {
			builder.append(relatedTicketCode);
		}
		if (builder.length() != 0) {
			builder.append("-");
		}
		builder.append(passengerMobile);

		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			if (builder.length() != 0) {
				builder.append(", ");
			}
			builder.append(ticketDetailsDTO.getSeatName() + "-" + ticketDetailsDTO.getTicketStatus() != null ? ticketDetailsDTO.getTicketStatus().getCode() : "NA");
		}
		return builder.toString();
	}

	public Map<String, String> getSeatTravelStatus() {
		Map<String, String> travelStatuMap = new HashMap<String, String>();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDetails) {
			travelStatuMap.put(ticketDetailsDTO.getSeatCode(), ticketDetailsDTO.getTravelStatus().getCode());
		}
		return travelStatuMap;
	}

	public boolean isContainAddonWalletTransaction() {
		for (TicketAddonsDetailsDTO ticketAddonsDetailsDTO : ticketAddonsDetails) {
			if (AddonsTypeEM.WALLET_REDEEM.getId() == ticketAddonsDetailsDTO.getAddonsType().getId() || AddonsTypeEM.WALLET_COUPON.getId() == ticketAddonsDetailsDTO.getAddonsType().getId()) {
				return true;
			}
		}
		return false;
	}

	public BigDecimal getCgst() {
		BigDecimal cgst = BigDecimal.ZERO;
		if (tax != null) {
			cgst = getTotalSeatFare().subtract(getAddonsValue()).multiply(tax.getCgstValue()).divide(Numeric.ONE_HUNDRED);
		}
		return cgst.setScale(1, RoundingMode.HALF_UP);
	}

	public BigDecimal getSgst() {
		BigDecimal sgst = BigDecimal.ZERO;
		if (tax != null) {
			sgst = getTotalSeatFare().subtract(getAddonsValue()).multiply(tax.getSgstValue()).divide(Numeric.ONE_HUNDRED);
		}
		return sgst.setScale(1, RoundingMode.HALF_UP);
	}

	public int getReleaseMinutes() {
		return ticketExtra != null ? ticketExtra.getBlockReleaseMinutes() : Numeric.ZERO_INT;
	}

	public TicketAddonsDetailsDTO getTicketCouponAddon(AddonsTypeEM addonsType) {
		TicketAddonsDetailsDTO addonDetails = null;
		if (ticketAddonsDetails != null) {
			for (TicketAddonsDetailsDTO ticketAddonsDetailsDTO : ticketAddonsDetails) {
				if (addonsType.getId() != ticketAddonsDetailsDTO.getAddonsType().getId()) {
					continue;
				}
				addonDetails = ticketAddonsDetailsDTO;
				break;
			}
		}
		return addonDetails;
	}
}

package org.in.com.controller.api_v3.io;

import java.math.BigDecimal;
import java.util.List;

import com.google.gson.Gson;

import lombok.Data;


@Data
public class TicketIO {
	private String code;
	private StationIO fromStation;
	private StationIO toStation;
	private String travelDate;
	private String reportingTime;
	private String transactionDate;
	private String passegerMobleNo;
	private String passegerEmailId;
	private BigDecimal totalFare;
	private BigDecimal totalRefundAmount;
	private BigDecimal cancellationCharge;
	private String serviceNo;
	private String remarks;
	private String JourneyType;
	private String tripCode;
	private String tripStageCode;
	private String ticketCode;
	private String gatewayPaymentCode;
	private String ticketAt;
	private String travelTime;
	private String deviceMedium;
	private OperatorIO operator;
	private TicketStatusIO ticketStatus;
	private TransactionModeIO transactionMode;
	private List<TicketDetailsIO> ticketDetails;
	private CancellationTermIO cancellationTerms;
	private List<UserTransactionIO> usertransaction;
	private List<TicketTransactionIO> ticketTransaction;
	
	public String toJSON() {
		Gson gson = new Gson();
		if (this != null) {
			gson.toJson(this);
		}
		return gson.toJson(this);
	}
}

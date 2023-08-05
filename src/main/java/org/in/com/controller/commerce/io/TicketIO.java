package org.in.com.controller.commerce.io;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.CancellationTermIO;
import org.in.com.controller.web.io.NamespaceTaxIO;
import org.in.com.controller.web.io.PaymentGatewayPartnerIO;
import org.in.com.controller.web.io.TicketExtraIO;
import org.in.com.controller.web.io.UserIO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketIO extends BaseIO {
	private String bookingCode;
	private StationIO fromStation;
	private StationIO toStation;
	private String tripDate;
	private String travelDate;
	private String blockingLiveTime;
	private String reportingTime;
	private String transactionDate;
	private String passegerMobleNo;
	private String passegerEmailId;
	private BusIO bus;
	private BigDecimal totalFare;
	private String totalRefundAmount;
	private boolean phoneBookingFlag;
	private String serviceNo;
	private String remarks;
	private String JourneyType;
	private String tripCode;
	private String tripName;
	private String tripTime;
	private String scheduleName;
	private String tripStageCode;
	private String ticketCode;
	private String gatewayPaymentCode;
	private PaymentGatewayPartnerIO paymentGatewayPartner;
	private String ticketAt;
	private UserIO user;
	private String travelTime;
	private String deviceMedium;
	private OperatorIO operator;
	private TicketStatusIO ticketStatus;
	private TransactionModeIO transactionMode;
	private List<TicketDetailsIO> ticketDetails;
	private List<TicketAddonsDetailsIO> ticketAddonsDetails;
	private CancellationTermIO cancellationTerms;
	private List<UserTransactionIO> usertransaction;
	private List<TicketTransactionIO> ticketTransaction;
	private String relatedTicketCode;
	private NamespaceTaxIO tax;
	private TicketExtraIO ticketExtra;
	private Map<String, String> additionalAttributes;
}

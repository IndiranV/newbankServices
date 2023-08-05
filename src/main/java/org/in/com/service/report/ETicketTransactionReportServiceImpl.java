package org.in.com.service.report;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.PaymentGatewayScheduleDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.PaymentMerchantGatewayScheduleService;
import org.in.com.service.ReportQueryService;
import org.in.com.service.TicketService;
import org.in.com.service.pg.PaymentRequestService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class ETicketTransactionReportServiceImpl implements ETicketTransactionReportService {
	@Autowired
	ReportQueryService reportQueryService;
	@Autowired
	TicketService ticketService;
	@Autowired
	PaymentRequestService paymentRequestService;
	@Autowired
	PaymentMerchantGatewayScheduleService gatewayScheduleService;

	@Override
	public List<Map<String, String>> getETicketTransactions(AuthDTO authDTO, ReportQueryDTO reportQueryDTO, String fromDate, String toDate, int travelDateFlag) {

		// fetch Only Account over Type = EZEE
		List<PaymentGatewayScheduleDTO> list = gatewayScheduleService.getVertexScheduledPaymentGateway(authDTO, new DateTime(fromDate), new DateTime(toDate));

		if (list.size() == 0) {
			throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
		}

		List<DBQueryParamDTO> params = new ArrayList<>();
		if (reportQueryDTO.getQuery().contains(":namespaceId")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("namespaceId");
			paramDTO.setValue(String.valueOf(authDTO.getNamespace().getId()));
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":fromDate")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("fromDate");
			paramDTO.setValue(fromDate);
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":toDate")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("toDate");
			paramDTO.setValue(toDate);
			params.add(paramDTO);
		}
		if (reportQueryDTO.getQuery().contains(":travelDateFlag")) {
			DBQueryParamDTO paramDTO = new DBQueryParamDTO();
			paramDTO.setParamName("travelDateFlag");
			paramDTO.setValue(String.valueOf(travelDateFlag));
			params.add(paramDTO);
		}

		List<Map<String, ?>> results = reportQueryService.getQueryResultsMap(authDTO, reportQueryDTO, params);

		List<Map<String, String>> finalResults = new ArrayList<>();
		for (Map<String, ?> resultMap : results) {
			int lookupTicketId = StringUtil.getIntegerValue(String.valueOf(resultMap.get("lookup_id")));
			int ticketId = StringUtil.getIntegerValue(String.valueOf(resultMap.get("ticket_id")));

			Map<String, String> ticketMap = (Map<String, String>) resultMap;
			ticketMap.put("reference_code", Text.NA);
			ticketMap.remove("lookup_id");
			ticketMap.remove("ticket_id");

			if (TicketStatusEM.TICKET_TRANSFERRED.getCode().equals(String.valueOf(resultMap.get("ticket_status_code"))) && (JourneyTypeEM.POSTPONE.getCode().equals(String.valueOf(resultMap.get("journey_type"))) || JourneyTypeEM.PREPONE.getCode().equals(String.valueOf(resultMap.get("journey_type"))))) {
				ticketMap.put("ticket_amount", Numeric.ZERO);
				ticketMap.put("ac_bus_tax", Numeric.ZERO);
				ticketMap.put("charge_tax_amount", Numeric.ZERO);
				ticketMap.put("addons_amount", Numeric.ZERO);
				ticketMap.put("transfer_charge_amount", Numeric.ZERO);
				ticketMap.put("previous_ticket_amount", Numeric.ZERO);
				ticketMap.put("gateway_transaction_amount", Numeric.ZERO);
				ticketMap.put("transaction_amount", Numeric.ZERO);
				ticketMap.put("commission_amount", Numeric.ZERO);
				ticketMap.put("revoke_commission_amount", Numeric.ZERO);
				ticketMap.put("cancellation_charges", Numeric.ZERO);
				ticketMap.put("cancel_tds_deduction", Numeric.ZERO);
				ticketMap.put("refund_amount", Numeric.ZERO);
				ticketMap.put("revoke_commission_amount", Numeric.ZERO);
				ticketMap.put("cancel_commission", Numeric.ZERO);
				ticketMap.put("refund_amount", Numeric.ZERO);
				ticketMap.put("cancellation_charges", Numeric.ZERO);
				ticketMap.put("tcs_deduction", Numeric.ZERO);
				ticketMap.put("tds_deduction", Numeric.ZERO);
				finalResults.add(ticketMap);
				continue;
			}

			TicketDTO ticketDTO = null;
			if (lookupTicketId != 0) {
				ticketDTO = new TicketDTO();
				ticketDTO.setId(lookupTicketId);

				ticketService.getTicketStatus(authDTO, ticketDTO);

				ticketMap.put("reference_code", ticketDTO.getCode());
			}

			if (travelDateFlag == 0) {
				BigDecimal transactionAmount = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("transaction_amount")));
				BigDecimal refundAmount = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("refund_amount")));
				BigDecimal returnRefundAmount = BigDecimal.ZERO;

				if (JourneyTypeEM.RETURN_TRIP.getCode().equals(resultMap.get("journey_type"))) {
					transactionAmount = transactionAmount.add(ticketDTO.getTicketFareWithAddons());

					List<PaymentGatewayTransactionDTO> paymentGatewayTransactions = paymentRequestService.getPaymentGatewayTransaction(authDTO, ticketDTO.getBookingCode());
					for (PaymentGatewayTransactionDTO paymentGatewayTransactionDTO : paymentGatewayTransactions) {
						if (paymentGatewayTransactionDTO.getTransactionType().getId() == TransactionTypeEM.TICKETS_CANCEL.getId()) {
							returnRefundAmount = returnRefundAmount.add(paymentGatewayTransactionDTO.getAmount());
						}
					}
				}
				ticketMap.put("gateway_transaction_amount", String.valueOf(transactionAmount.subtract(refundAmount.add(returnRefundAmount))));

				if (TicketStatusEM.TICKET_TRANSFERRED.getCode().equals(resultMap.get("ticket_status_code"))) {
					TicketDTO repoTicket = new TicketDTO();
					repoTicket.setLookupId(ticketId);

					ticketService.findTicketByLookupId(authDTO, repoTicket);
					ticketService.getTicketStatus(authDTO, repoTicket);

					repoTicket.setTicketXaction(new TicketTransactionDTO());
					repoTicket.getTicketXaction().setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
					ticketService.getTicketTransaction(authDTO, repoTicket);

					BigDecimal transactionAmount1 = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("transaction_amount")));
					if (repoTicket.getTicketXaction().getTransactionAmount() != null && repoTicket.getTicketXaction().getTransactionAmount().compareTo(transactionAmount1) > 0) {
						ticketMap.put("ticket_amount", String.valueOf(repoTicket.getTotalSeatFare()));
						ticketMap.put("ac_bus_tax", String.valueOf(repoTicket.getAcBusTax()));
						ticketMap.put("addons_amount", String.valueOf(repoTicket.getAddonsValue()));
						ticketMap.put("transaction_amount", String.valueOf(repoTicket.getTicketXaction().getTransactionAmount()));
						ticketMap.put("commission_amount", String.valueOf(repoTicket.getTicketXaction().getCommissionAmount()));
					}

					TicketAddonsDetailsDTO previousTicketAddon = repoTicket.getTicketCouponAddon(AddonsTypeEM.TRANSFER_PREVIOUS_TICKET_AMOUNT);
					TicketAddonsDetailsDTO transferChargeAddon = repoTicket.getTicketCouponAddon(AddonsTypeEM.TICKET_TRANSFER_CHARGE);

					ticketMap.put("previous_ticket_amount", previousTicketAddon != null ? String.valueOf(previousTicketAddon.getValue()) : Numeric.ZERO);
					ticketMap.put("transfer_charge_amount", transferChargeAddon != null ? String.valueOf(transferChargeAddon.getValue()) : Numeric.ZERO);
					ticketMap.put("reference_code", repoTicket.getCode());
				}
			}
			else if (travelDateFlag == 1) {
				BigDecimal transactionAmount = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("transaction_amount")));
				BigDecimal refundAmount = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("refund_amount")));
				BigDecimal returnRefundAmount = BigDecimal.ZERO;

				if (JourneyTypeEM.RETURN_TRIP.getCode().equals(resultMap.get("journey_type"))) {
					transactionAmount = transactionAmount.add(ticketDTO.getTicketFareWithAddons());
					List<PaymentGatewayTransactionDTO> paymentGatewayTransactions = paymentRequestService.getPaymentGatewayTransaction(authDTO, ticketDTO.getBookingCode());
					for (PaymentGatewayTransactionDTO paymentGatewayTransactionDTO : paymentGatewayTransactions) {
						if (paymentGatewayTransactionDTO.getTransactionType().getId() == TransactionTypeEM.TICKETS_CANCEL.getId()) {
							returnRefundAmount = returnRefundAmount.add(paymentGatewayTransactionDTO.getAmount());
						}
					}
				}
				else if (JourneyTypeEM.POSTPONE.getCode().equals(resultMap.get("journey_type")) || JourneyTypeEM.PREPONE.getCode().equals(resultMap.get("journey_type"))) {
					ticketDTO.setBookingCode(String.valueOf(resultMap.get("booking_code")));
					List<PaymentGatewayTransactionDTO> paymentGatewayTransactions = paymentRequestService.getPaymentGatewayTransaction(authDTO, ticketDTO.getBookingCode());

					for (PaymentGatewayTransactionDTO paymentGatewayTransactionDTO : paymentGatewayTransactions) {
						if (paymentGatewayTransactionDTO.getTransactionType().getId() == TransactionTypeEM.TICKETS_BOOKING.getId()) {
							transactionAmount = transactionAmount.add(paymentGatewayTransactionDTO.getAmount());
						}
						else if (paymentGatewayTransactionDTO.getTransactionType().getId() == TransactionTypeEM.TICKETS_CANCEL.getId()) {
							returnRefundAmount = returnRefundAmount.add(paymentGatewayTransactionDTO.getAmount());
						}
					}

					BigDecimal actualTicketAmount = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("actual_ticket_amount"))).add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ac_bus_tax")))).subtract(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("addons_amount"))));
					BigDecimal previousTicketAmount = StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("previous_ticket_amount")));
					if (actualTicketAmount.compareTo(previousTicketAmount) != 0) {
						ticketMap.put("ticket_amount", String.valueOf(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("ticket_amount"))).add(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("previous_ticket_amount")))).subtract(StringUtil.getBigDecimalValue(String.valueOf(resultMap.get("addons_amount"))))));
					}
				}
				ticketMap.put("gateway_transaction_amount", String.valueOf(transactionAmount.subtract(refundAmount.add(returnRefundAmount))));
			}

			finalResults.add(ticketMap);
		}
		return finalResults;
	}
}

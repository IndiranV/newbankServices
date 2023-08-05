package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.PendingOrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TicketDTO;

public interface PendingOrderService extends BaseService<PendingOrderDTO> {

	public TicketDTO confirmPendingOrder(AuthDTO authDTO, TicketDTO ticketDTO);

	public PendingOrderDTO cancelPendingOrder(AuthDTO authDTO, PendingOrderDTO pendingOrderDTO);

	public List<BookingDTO> getInprogreeTicketTransaction(AuthDTO authDTO);

	public PaymentTransactionDTO rechargeConfirmOrder(AuthDTO authDTO, String orderCode);

	public void processPendingOrderAutoRefund();

	public void refundOrder(AuthDTO authDTO, RefundDTO refundDTO);

	public List<Map<String, String>> getPaymentGatewayAnalyticsReport(AuthDTO authDTO, String fromDate, String toDate);

}

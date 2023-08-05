package org.in.com.service.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.aggregator.payment.PGInterface;
import org.in.com.aggregator.payment.impl.GatewayFactory;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ConnectDAO;
import org.in.com.dao.PaymentGatewayPreTransactionDAO;
import org.in.com.dao.PaymentGatewayProviderDAO;
import org.in.com.dao.PaymentGatewayTransactionDAO;
import org.in.com.dao.PaymentMerchantGatewayCredentialsDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.PaymentGatewayCredentialsDTO;
import org.in.com.dto.PaymentGatewayProviderDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.PaymentPreTransactionDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.RefundStatusEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NamespaceService;
import org.in.com.service.PaymentOrderStatusService;
import org.in.com.service.TicketService;
import org.in.com.service.UserTransactionService;
import org.in.com.service.pg.PaymentRefundService;
import org.in.com.service.pg.PaymentRequestService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Cleanup;

@Service
public class PaymentOrderStatusImpl implements PaymentOrderStatusService {
	@Autowired
	NamespaceService namespaceService;
	@Autowired
	TicketService ticketService;
	@Autowired
	PaymentRefundService refundService;
	@Autowired
	UserTransactionService transactionService;
	@Autowired
	PaymentRequestService paymentRequetService;

	@Override
	public Map<String, String> getOrderStatus(AuthDTO authDTO, String orderCode, String namespaceCode) {
		OrderDTO orderDTO = new OrderDTO();
		orderDTO.setOrderCode(orderCode);
		NamespaceDTO namespace = namespaceService.getNamespace(namespaceCode);
		if (namespace.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
		}
		PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();

		PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO();
		preTransactionDTO.setOrderCode(orderDTO.getOrderCode());

		paymentPreTransactionDAO.getPreTransactionForTicket(namespace, preTransactionDTO);
		if (preTransactionDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS, " Payment Gateway Credentials not found");
		}
		orderDTO.setTransactionCode(preTransactionDTO.getCode());
		orderDTO.setGatewayTransactionCode(preTransactionDTO.getGatewayTransactionCode());

		PaymentMerchantGatewayCredentialsDAO merchantGatewayDetails = new PaymentMerchantGatewayCredentialsDAO();
		PaymentGatewayCredentialsDTO gatewayCredentials = merchantGatewayDetails.getNamespacePGCredentials(namespace, preTransactionDTO.getGatewayProvider().getId());
		orderDTO.setGatewayCredentials(gatewayCredentials);
		if (gatewayCredentials.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS, " Payment Gateway Credentials not found");
		}

		PaymentGatewayProviderDAO gatewayProviderDAO = new PaymentGatewayProviderDAO();
		PaymentGatewayProviderDTO provider = gatewayProviderDAO.getPGDetails(preTransactionDTO.getGatewayProvider().getId());

		if (StringUtil.isNull(provider.getServiceName())) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS, " Payment Gateway Provider not found");
		}
		PGInterface gatewayInstance = GatewayFactory.returnPgInstance(provider.getServiceName());
		Map<String, String> orderStatusMap = gatewayInstance.verifyPaymentOrder(authDTO, orderDTO);
		return orderStatusMap;
	}

	@Override
	public List<PaymentGatewayTransactionDTO> getPaymentGatewayTransactions(AuthDTO authDTO, TicketDTO ticketDTO) {
		PaymentGatewayTransactionDAO paymentGatewayTransactionDAO = new PaymentGatewayTransactionDAO();
		PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();
		PaymentGatewayProviderDAO gatewayProviderDAO = new PaymentGatewayProviderDAO();
		ticketService.getTicketStatus(authDTO, ticketDTO);
		if (ticketDTO.getLookupId() != 0 && (JourneyTypeEM.PREPONE.getId() == ticketDTO.getJourneyType().getId() || JourneyTypeEM.POSTPONE.getId() == ticketDTO.getJourneyType().getId())) {
			TicketDTO ticket = new TicketDTO();
			ticket.setId(ticketDTO.getLookupId());
			ticketDTO = ticketService.getTicketStatus(authDTO, ticket);
		}

		List<PaymentGatewayTransactionDTO> list = paymentGatewayTransactionDAO.getPaymentGatewayTransaction(authDTO, ticketDTO.getBookingCode());
		for (PaymentGatewayTransactionDTO paymentGatewayTransactionDTO : list) {
			PaymentGatewayProviderDTO gatewayProvider = gatewayProviderDAO.getPGDetails(paymentGatewayTransactionDTO.getGatewayProvider().getId());
			paymentGatewayTransactionDTO.setGatewayProvider(gatewayProvider);
			
			PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO();
			preTransactionDTO.setOrderCode(ticketDTO.getBookingCode());
			paymentPreTransactionDAO.getPreTransactionForTicket(authDTO, preTransactionDTO);
			paymentGatewayTransactionDTO.setStatus(preTransactionDTO.getStatus());
		}
		return list;
	}
	
	public Map<String, String> verifyOrderStatus(AuthDTO authDTO, TicketDTO ticketDTO) {
		Map<String, String> orderStatusMap = new HashMap<String, String>();
		ticketService.getTicketStatus(authDTO, ticketDTO);
		if (ticketDTO.getLookupId() != 0 && (JourneyTypeEM.PREPONE.getId() == ticketDTO.getJourneyType().getId() || JourneyTypeEM.POSTPONE.getId() == ticketDTO.getJourneyType().getId())) {
			TicketDTO ticket = new TicketDTO();
			ticket.setId(ticketDTO.getLookupId());
			ticket = ticketService.getTicketStatus(authDTO, ticket);
			ticketDTO.setBookingCode(ticket.getBookingCode());
		}
		orderStatusMap = getOrderStatus(authDTO, ticketDTO.getBookingCode(), authDTO.getNamespace().getCode());

		return orderStatusMap;
	}

	public Map<String, String> updateTransactionStatus(AuthDTO authDTO, TicketDTO ticketDTO) {
		Map<String, String> orderStatusMap = new HashMap<String, String>();
		try {
			ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getLookupId() != 0 && (JourneyTypeEM.PREPONE.getId() == ticketDTO.getJourneyType().getId() || JourneyTypeEM.POSTPONE.getId() == ticketDTO.getJourneyType().getId())) {
				TicketDTO ticket = new TicketDTO();
				ticket.setId(ticketDTO.getLookupId());
				ticket = ticketService.getTicketStatus(authDTO, ticket);
				ticketDTO.setBookingCode(ticket.getBookingCode());
			}
			OrderDTO orderDTO = new OrderDTO();
			orderDTO.setOrderCode(ticketDTO.getBookingCode());

			PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO(); 
			preTransactionDTO.setOrderCode(ticketDTO.getBookingCode());

			PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();
			paymentPreTransactionDAO.getPreTransactionForTicket(authDTO, preTransactionDTO);
			if (preTransactionDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}

			PaymentPreTransactionDTO preTransaction = new PaymentPreTransactionDTO();
			preTransaction.setCode(preTransactionDTO.getCode());
			preTransaction.setStatus(preTransactionDTO.getStatus());

			orderDTO.setTransactionCode(preTransactionDTO.getCode());
			orderDTO.setGatewayTransactionCode(preTransactionDTO.getGatewayTransactionCode());

			PaymentMerchantGatewayCredentialsDAO merchantGatewayDetails = new PaymentMerchantGatewayCredentialsDAO();
			PaymentGatewayCredentialsDTO gatewayCredentials = merchantGatewayDetails.getNamespacePGCredentials(authDTO.getNamespace(), preTransactionDTO.getGatewayProvider().getId());
			orderDTO.setGatewayCredentials(gatewayCredentials);
			if (gatewayCredentials.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS, " Payment Gateway Credentials not found");
			}

			PaymentGatewayProviderDAO gatewayProviderDAO = new PaymentGatewayProviderDAO();
			PaymentGatewayProviderDTO provider = gatewayProviderDAO.getPGDetails(preTransactionDTO.getGatewayProvider().getId());

			if (StringUtil.isNull(provider.getServiceName())) {
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS, " Payment Gateway Provider not found");
			}
			PGInterface gatewayInstance = GatewayFactory.returnPgInstance(provider.getServiceName());
			orderStatusMap = gatewayInstance.verifyPaymentOrder(authDTO, orderDTO);

			if (orderStatusMap == null) {
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}

			if ("success".equalsIgnoreCase(orderStatusMap.get("orderStatus")) || "Captured".equalsIgnoreCase(orderStatusMap.get("orderStatus"))) {
				preTransaction.setStatus(PaymentGatewayStatusEM.SUCCESS);
			}
			else if ("Bounced".equalsIgnoreCase(orderStatusMap.get("orderStatus")) || "failedbybank".equalsIgnoreCase(orderStatusMap.get("orderStatus").replace(Text.SINGLE_SPACE, Text.EMPTY)) || "Dropped".equalsIgnoreCase(orderStatusMap.get("orderStatus"))) {
				preTransaction.setStatus(PaymentGatewayStatusEM.FAILURE);
			}
			else if ("cancelledbyuser".equalsIgnoreCase(orderStatusMap.get("orderStatus").replace(Text.SINGLE_SPACE, Text.EMPTY))) {
				preTransaction.setStatus(PaymentGatewayStatusEM.ORDER_CANCELLED);
			}

			if (preTransactionDTO.getStatus().getId() != preTransaction.getStatus().getId()) {
				paymentPreTransactionDAO.updateStatusV3(authDTO, preTransaction);
			}
			if (preTransaction.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId()) {
				savePaymentGatewayTransaction(authDTO, preTransactionDTO);
			}
		}
		catch (ServiceException e) {
			e.printStackTrace();
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		return orderStatusMap;
	}

	private PaymentGatewayTransactionDTO savePaymentGatewayTransaction(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDTO) {
		PaymentGatewayTransactionDTO transactionEntity = new PaymentGatewayTransactionDTO();
		try {
			transactionEntity.setCode(preTransactionDTO.getCode());
			transactionEntity.setUser(preTransactionDTO.getUser());
			transactionEntity.setDeviceMedium(preTransactionDTO.getDeviceMedium());
			transactionEntity.setGatewayPartner(preTransactionDTO.getGatewayPartner());
			transactionEntity.setGatewayProvider(preTransactionDTO.getGatewayProvider());
			transactionEntity.setGatewayCredentials(preTransactionDTO.getGatewayCredentials());
			transactionEntity.setAmount(preTransactionDTO.getAmount());
			transactionEntity.setTransactionType(PaymentGatewayTransactionTypeEM.PAYMENT);
			transactionEntity.setOrderCode(preTransactionDTO.getOrderCode());
			transactionEntity.setStatus(PaymentGatewayStatusEM.SUCCESS);

			PaymentGatewayTransactionDAO gatewayTransaction = new PaymentGatewayTransactionDAO();
			gatewayTransaction.checkAndInsert(authDTO, transactionEntity);
		}
		catch (Exception e) {
			System.out.println("Payment Gateway Transaction Exist!");
		}
		return transactionEntity;
	}

	public void paymentRefund(AuthDTO authDTO, PaymentPreTransactionDTO preTransaction) {
		TicketDTO ticketDTO = new TicketDTO();
		TicketDTO ticket = new TicketDTO();
		ticketDTO.setCode(preTransaction.getOrderCode());
		ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
		if (ticketDTO.getLookupId() != 0 && (JourneyTypeEM.PREPONE.getId() == ticketDTO.getJourneyType().getId() || JourneyTypeEM.POSTPONE.getId() == ticketDTO.getJourneyType().getId())) {
			ticket.setId(ticketDTO.getLookupId());
			ticket = ticketService.getTicketStatus(authDTO, ticket);
			ticketDTO.setBookingCode(ticket.getBookingCode());
		}

		BigDecimal bookingAmount = BigDecimal.ZERO;
		List<PaymentGatewayTransactionDTO> paymentGatewayTransactionList = paymentRequetService.getPaymentGatewayTransaction(authDTO, ticketDTO.getBookingCode());
		for (PaymentGatewayTransactionDTO paymentGatewayTransaction : paymentGatewayTransactionList) {
			if (paymentGatewayTransaction.getTransactionType().getId() == PaymentGatewayTransactionTypeEM.PAYMENT.getId()) {
				bookingAmount = bookingAmount.add(paymentGatewayTransaction.getAmount());
			}
			else {
				bookingAmount = bookingAmount.subtract(paymentGatewayTransaction.getAmount());
			}
		}

		if (bookingAmount.compareTo(BigDecimal.ZERO) == 0) {
			throw new ServiceException(ErrorCode.MISMATCH_IN_TRANSACTION_AMOUNT);
		}
		BigDecimal ticketAmount = BigDecimal.ZERO;
		if (ticket.getId() != Numeric.ZERO_INT) {
			ticketAmount = bookingAmount.subtract(ticket.getTotalFare());
		}
		else {
			ticketAmount = bookingAmount.subtract(ticketDTO.getTotalFare());
		}

		if (preTransaction.getAmount().compareTo(bookingAmount) <= 0 && (ticketAmount.compareTo(BigDecimal.ZERO) == 0 || ticketAmount.compareTo(preTransaction.getAmount()) <= 0)) {
			TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
			ticketTransactionDTO.setTransactionAmount(preTransaction.getAmount());
			ticketTransactionDTO.setAcBusTax(BigDecimal.ZERO);
			ticketTransactionDTO.setTdsTax(BigDecimal.ZERO);
			ticketTransactionDTO.setExtraCommissionAmount(BigDecimal.ZERO);
			ticketTransactionDTO.setCommissionAmount(BigDecimal.ZERO);
			ticketTransactionDTO.setAddonsAmount(BigDecimal.ZERO);
			ticketTransactionDTO.setTransactionMode(TransactionModeEM.PAYMENT_PAYMENT_GATEWAY);
			ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
			ticketTransactionDTO.setRemarks(StringUtil.substring(ticketDTO.getRemarks(), 120));

			ticketTransactionDTO.setCancelTdsTax(BigDecimal.ZERO);
			ticketTransactionDTO.setCancellationChargeTax(BigDecimal.ZERO);
			ticketTransactionDTO.setCancellationCommissionAmount(BigDecimal.ZERO);
			ticketTransactionDTO.setRefundAmount(preTransaction.getAmount());
			ticketTransactionDTO.setRefundStatus(RefundStatusEM.INITIAL);
			ticketDTO.setTicketXaction(ticketTransactionDTO);
			
			if (ticket.getId() != Numeric.ZERO_INT && StringUtil.isNotNull(ticket.getBookingCode())) {
				ticketDTO.setBookingCode(ticket.getBookingCode());
			}
			saveTransaction(authDTO, ticketDTO);

			doAutoRefundProcess(authDTO, ticketDTO);
		}
		else {
			throw new ServiceException(ErrorCode.MISMATCH_IN_TRANSACTION_AMOUNT);
		}
	}

	private void saveTransaction(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);
				ticketService.saveTicketTransaction(connection, authDTO, ticketDTO);
				ticketService.saveTicketCancelTransaction(connection, authDTO, ticketDTO);

				if (ticketDTO.getUserTransaction() != null && ticketDTO.getTicketUser().getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId()) {
					ticketDTO.getUserTransaction().setRefferenceId(ticketDTO.getTicketXaction().getId());
					transactionService.SaveUserTransaction(connection, authDTO, ticketDTO.getTicketUser(), ticketDTO.getUserTransaction());
				}
			}
			catch (ServiceException e) {
				connection.rollback();
				throw e;
			}
			catch (Exception e) {
				e.printStackTrace();
				connection.rollback();
				throw e;
			}
			finally {
				connection.commit();
				connection.setAutoCommit(true);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void doAutoRefundProcess(AuthDTO authDTO, TicketDTO repositoryTicketDTO) {
		RefundDTO refundDTO = new RefundDTO();
		refundDTO.setAmount(repositoryTicketDTO.getTicketXaction().getRefundAmount());
		refundDTO.setOrderCode(repositoryTicketDTO.getBookingCode());
		refundDTO.setOrderTransactionCode(repositoryTicketDTO.getTicketXaction().getCode());
		refundDTO.setOrderType(OrderTypeEM.TICKET);
		refundService.doRefund(authDTO, refundDTO);
	}
}

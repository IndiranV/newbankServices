package org.in.com.service.pg;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.payment.PGInterface;
import org.in.com.aggregator.payment.impl.GatewayFactory;
import org.in.com.cache.PaymentCache;
import org.in.com.constants.Text;
import org.in.com.dao.PaymentGatewayLoggingDAO;
import org.in.com.dao.PaymentGatewayPreTransactionDAO;
import org.in.com.dao.PaymentGatewayTransactionDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.PaymentPreTransactionDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.dto.enumeration.PaymentOrderEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.PaymentResponseException;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.ConfirmSeatsService;
import org.in.com.service.TicketService;
import org.in.com.service.impl.BaseImpl;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentResponseServiceImpl extends BaseImpl implements PaymentResponseService {

	private static final Logger responselogger = LoggerFactory.getLogger("org.in.com.controller.pgtrace");

	@Autowired
	AuthService authService;
	@Autowired
	TicketService ticketService;
	@Autowired
	EmailService emailService;
	@Autowired
	ConfirmSeatsService confirmSeatsService;

	public TransactionEnquiryDTO handlePayment(String transactionCode, Map<String, String> fields) {

		responselogger.info("pg response handling started - {} fields {}", transactionCode, fields);
		TransactionEnquiryDTO transactionEnquiry = new TransactionEnquiryDTO();
		PaymentPreTransactionDTO preTransactionDTO = null;
		try {
			// Get transaction authDTO from Cache using transaction ID

			if (StringUtils.isBlank(transactionCode)) {
				throw new PaymentResponseException(ErrorCode.INVALID_TRANSACTION_ID);
			}

			PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();

			preTransactionDTO = paymentPreTransactionDAO.isTransactionExists(transactionCode, PaymentGatewayTransactionTypeEM.PAYMENT);

			preTransactionDTO.setStatus(PaymentGatewayStatusEM.ORDER_PG_RESPONSE);

			paymentPreTransactionDAO.updateStatus(preTransactionDTO);

			PaymentCache paymentCache = new PaymentCache();
			OrderDTO transactionDetails = paymentCache.getTransactionDetails(transactionCode);

			if (transactionDetails == null) {
				throw new PaymentResponseException(ErrorCode.INVALID_TRANSACTION_ID);
			}

			AuthDTO authDTO = getAuthDTO(transactionDetails);

			PGInterface gatewayInstance = GatewayFactory.returnPgInstance(transactionDetails.getGatewayProvider().getServiceName());

			if (gatewayInstance == null) {
				throw new PaymentResponseException(ErrorCode.NO_GATEWAY_FOUND);
			}

			// transactionEnquiry.setMerchantReturnUrl(credentialsEntity.getMerchant().getReturnUrl());
			transactionEnquiry.setGatewayReponse(fields);
			transactionEnquiry.setTransactionCode(transactionCode);
			transactionEnquiry.setGatewayCredentials(transactionDetails.getGatewayCredentials());
			transactionEnquiry.setResponseURL(transactionDetails.getResponseUrl());
			transactionEnquiry.setOrderCode(transactionDetails.getOrderCode());
			transactionEnquiry.setGatewayTransactionCode(transactionDetails.getGatewayTransactionCode());
			try {
				// Validate Order Status based on response
				gatewayInstance.transactionVerify(authDTO, transactionEnquiry);

				// M2M Verifications
				Map<String, String> responseMap = gatewayInstance.verifyPaymentOrder(authDTO, transactionDetails);
				// Send Email
				sendPaymentOrderVerificationFailureEmail(authDTO, transactionEnquiry, transactionDetails, responseMap);

				// Update payment order status
				if (responseMap != null && StringUtil.isNotNull(responseMap.get("paymentOrderStatus")) && !PaymentOrderEM.SUCCESS.getCode().equals(responseMap.get("paymentOrderStatus"))) {
					transactionEnquiry.setStatus(ErrorCode.PAYMENT_DECLINED);
				}

				if (transactionEnquiry.getStatus() == ErrorCode.SUCCESS && transactionDetails.getGatewayProvider().getServiceName().equals("KhaltiPaymentServiceImpl")) {
					transactionEnquiry.setAmount(transactionDetails.getAmount());
				}
			}
			catch (PaymentResponseException e) {
				responselogger.error("{} {}", transactionCode, e);
				/**
				 * TODO After putting the details in cache and validating you
				 * need to send mail or some admin page notification
				 * for this order id to check whether this is fraudlent or valid
				 * order id
				 */
				/**
				 * After enquiring with gateway if it is failing to connect with
				 * gateway in that case
				 * we will try validate with some checksum logic and return as
				 * successful transaction
				 */

				gatewayInstance.internalVerfication(authDTO, transactionEnquiry);
				if (transactionEnquiry.getStatus() != ErrorCode.SUCCESS) {
					throw new PaymentResponseException(transactionEnquiry.getStatus());
				}
			}
			// If there is any mismatch in transaction amount we asked payment
			// gateway to debit
			// that is considered as fraudlent transaction
			if (preTransactionDTO.getAmount().compareTo(transactionEnquiry.getAmount()) != 0) {
				throw new PaymentResponseException(ErrorCode.MISMATCH_IN_TRANSACTION_AMOUNT);
			}

			if (transactionEnquiry.getStatus() == ErrorCode.SUCCESS) {
				PaymentGatewayTransactionDTO transactionEntity = new PaymentGatewayTransactionDTO();
				transactionEntity.setCode(preTransactionDTO.getCode());
				transactionEntity.setUser(preTransactionDTO.getUser());
				transactionEntity.setDeviceMedium(preTransactionDTO.getDeviceMedium());
				transactionEntity.setGatewayPartner(preTransactionDTO.getGatewayPartner());
				transactionEntity.setGatewayCredentials(transactionDetails.getGatewayCredentials());
				transactionEntity.setGatewayProvider(transactionDetails.getGatewayProvider());
				transactionEntity.setGatewayTransactionCode(transactionEnquiry.getGatewayTransactionCode());
				transactionEntity.setAmount(preTransactionDTO.getAmount());
				transactionEntity.setServiceCharge(preTransactionDTO.getServiceCharge());
				transactionEntity.setTransactionType(PaymentGatewayTransactionTypeEM.PAYMENT);
				transactionEntity.setOrderCode(transactionDetails.getOrderCode());
				PaymentGatewayTransactionDAO gatewayTransaction = new PaymentGatewayTransactionDAO();
				gatewayTransaction.checkAndInsert(authDTO, transactionEntity);
				transactionEnquiry.setOrderCode(transactionDetails.getOrderCode());

				transactionDetails.setStatus(transactionEnquiry.getStatus().getCode());

				paymentCache.putTransactionDetailsInCache(transactionDetails);

				// Update pre transaction status
				preTransactionDTO.setStatus(PaymentGatewayStatusEM.SUCCESS);
				preTransactionDTO.setFailureErrorCode(PaymentGatewayStatusEM.SUCCESS.getCode());
				paymentPreTransactionDAO.updateStatus(preTransactionDTO);

				// Confirm Ticket On the flow
				if (transactionDetails.getOrderType().getId() == OrderTypeEM.TICKET.getId() && StringUtil.isNull(transactionDetails.getUdf4())) {
					BookingDTO bookingDTO = confirmSeatsService.confirmBooking(authDTO, transactionCode, TransactionModeEM.PAYMENT_PAYMENT_GATEWAY.getCode(), null, null);
					responselogger.info("PR Auto confirm: {}", bookingDTO.getCode());
				}
				else if (transactionDetails.getOrderType().getId() == OrderTypeEM.TICKET.getId() && StringUtil.isNotNull(transactionDetails.getUdf4()) && !JourneyTypeEM.POSTPONE.getCode().equals(transactionDetails.getUdf4()) && !JourneyTypeEM.PREPONE.getCode().equals(transactionDetails.getUdf4())) {
					BookingDTO bookingDTO = confirmSeatsService.confirmBooking(authDTO, transactionCode, TransactionModeEM.PAYMENT_PAYMENT_GATEWAY.getCode(), null, null);
					responselogger.info("PR Auto confirm: {}", bookingDTO.getCode());
				}
			}

			responselogger.info("pg response handling ended - {}", transactionCode);

		}
		catch (ServiceException e) {
			if (!e.getErrorCode().getCode().equals(ErrorCode.TRANSACTION_ALREADY_SUCCESS.getCode())) {
				throw e;
			}
		}
		catch (PaymentResponseException e) {
			transactionEnquiry.setStatus(e.getError());
			responselogger.error("{} {}", transactionCode, e);
		}
		catch (Exception e) {
			transactionEnquiry.setStatus(ErrorCode.UNDEFINE_EXCEPTION);
			responselogger.error("{} {}", transactionCode, e);
		}
		finally {
			if (transactionEnquiry.getStatus() != ErrorCode.SUCCESS) {
				if (preTransactionDTO != null) {
					PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();
					try {
						paymentPreTransactionDAO.updateStatus(preTransactionDTO);
					}
					catch (Exception e) {
						responselogger.error("{} {}", transactionCode, e);
					}
				}
				else {
					responselogger.warn("This transaction id - {} has some problem look into this ", transactionCode);
				}
			}
			PaymentGatewayLoggingDAO loggingDAO = new PaymentGatewayLoggingDAO();
			loggingDAO.pgresponselogging(transactionEnquiry);

		}
		return transactionEnquiry;
	}

	private AuthDTO getAuthDTO(OrderDTO orderDTO) {
		AuthDTO authDTO = null;
		try {
			authDTO = authService.getAuthDTO(orderDTO.getAuthToken());
		}
		catch (ServiceException e) {
			if (e.getErrorCode().getCode().equals(ErrorCode.USER_INVALID_AUTH_TOKEN.getCode())) {
				System.out.printf("ERR00P9: %s-%s-%s-%s", orderDTO.getAmount(), orderDTO.getAuthToken(), orderDTO.getOrderCode(), orderDTO.getGatewayTransactionCode());
			}
		}
		if (authDTO == null) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(orderDTO.getOrderCode());
			authDTO = authService.getGuestAuthendtication(getNamespaceAuthDTO(ticketDTO).getNamespaceCode(), ticketDTO.getDeviceMedium());
		}
		if (authDTO == null) {
			System.out.printf("ERR00P10: %s-%s-%s-%s", orderDTO.getAmount(), orderDTO.getAuthToken(), orderDTO.getOrderCode(), orderDTO.getGatewayTransactionCode());
		}
		return authDTO;
	}

	private void sendPaymentOrderVerificationFailureEmail(AuthDTO authDTO, TransactionEnquiryDTO transactionEnquiry, OrderDTO transactionDetails, Map<String, String> responseMap) {
		boolean isTransactionEnquirySuccess = transactionEnquiry.getStatus() == ErrorCode.SUCCESS ? Text.TRUE : Text.FALSE;
		boolean isPaymentOrderSuccess = Text.FALSE;
		if (responseMap != null && StringUtil.isNotNull(responseMap.get("paymentOrderStatus")) && PaymentOrderEM.SUCCESS.getCode().equals(responseMap.get("paymentOrderStatus"))) {
			isPaymentOrderSuccess = Text.TRUE;
		}

		if (isTransactionEnquirySuccess != isPaymentOrderSuccess) {
			emailService.sendPaymentOrderVerificationFailureEmail(authDTO, transactionDetails, responseMap);
		}
	}

	@Override
	public AuthDTO validateInternalOrderStatus(String orderCode) {
		AuthDTO authDTO = null;
		try {
			PaymentCache paymentCache = new PaymentCache();
			OrderDTO transactionDetails = paymentCache.getTransactionDetails(orderCode);

			if (transactionDetails == null || (StringUtil.isNotNull(transactionDetails.getStatus()) && ErrorCode.SUCCESS.getCode().equals(transactionDetails.getStatus()))) {
				throw new PaymentResponseException(ErrorCode.INVALID_TRANSACTION_ID);
			}
			authDTO = getAuthDTO(transactionDetails);

			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(orderCode);
			ticketService.getTicketStatus(authDTO, ticketDTO);

			// Validate ticket available if block time over
			if (DateUtil.NOW().gteq(DateUtil.addMinituesToDate(ticketDTO.getTicketAt(), authDTO.getNamespace().getProfile().getSeatBlockTime()))) {
				// Send SMS
				emailService.sendS2SFailureBookingEmail(authDTO, ticketDTO);
				throw new ServiceException(ErrorCode.SELECTED_SEAT_BLOCK_TIME_OVER, "Internal callback failed!");
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			System.out.println(orderCode);
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return authDTO;
	}

	// Call back activity M2M
	public TransactionEnquiryDTO internalTransactionEnquiry(String transactionCode, Map<String, String> fields) {
		responselogger.info("pg response handling callback started - {}", transactionCode);
		TransactionEnquiryDTO transactionEnquiry = new TransactionEnquiryDTO();
		try {
			if (StringUtils.isBlank(transactionCode)) {
				throw new PaymentResponseException(ErrorCode.INVALID_TRANSACTION_ID);
			}
			PaymentCache paymentCache = new PaymentCache();
			OrderDTO transactionDetails = paymentCache.getTransactionDetails(transactionCode);

			if (transactionDetails == null || TransactionTypeEM.TICKETS_BOOKING.getId() != transactionDetails.getOrderType().getId()) {
				throw new PaymentResponseException(ErrorCode.INVALID_TRANSACTION_ID);
			}

			AuthDTO authDTO = getAuthDTO(transactionDetails);

			PGInterface gatewayInstance = GatewayFactory.returnPgInstance(transactionDetails.getGatewayProvider().getServiceName());

			if (gatewayInstance == null) {
				throw new PaymentResponseException(ErrorCode.NO_GATEWAY_FOUND);
			}

			transactionEnquiry.setGatewayReponse(fields);
			transactionEnquiry.setTransactionCode(transactionCode);
			transactionEnquiry.setGatewayCredentials(transactionDetails.getGatewayCredentials());
			transactionEnquiry.setResponseURL(transactionDetails.getResponseUrl());
			transactionEnquiry.setOrderCode(transactionDetails.getOrderCode());
			transactionEnquiry.setGatewayTransactionCode(transactionDetails.getGatewayTransactionCode());
			try {
				gatewayInstance.transactionVerify(authDTO, transactionEnquiry);
				if (transactionDetails.getGatewayCredentials() != null) {
					Map<String, String> responseMap = gatewayInstance.verifyPaymentOrder(authDTO, transactionDetails);

					if (responseMap != null && StringUtil.isNotNull(responseMap.get("paymentOrderStatus")) && !PaymentOrderEM.SUCCESS.getCode().equals(responseMap.get("paymentOrderStatus"))) {
						transactionEnquiry.setStatus(ErrorCode.PAYMENT_DECLINED);
					}
				}
			}
			catch (PaymentResponseException e) {
				responselogger.error("{} {}", transactionCode, e);
				gatewayInstance.internalVerfication(authDTO, transactionEnquiry);
			}
			responselogger.info("pg response handling callback ended 1 - {}", transactionCode);

		}
		catch (ServiceException e) {
			if (!e.getErrorCode().getCode().equals(ErrorCode.TRANSACTION_ALREADY_SUCCESS.getCode())) {
				transactionEnquiry.setStatus(ErrorCode.TRANSACTION_ALREADY_SUCCESS);
			}
			responselogger.error("{} {}", transactionCode, e);
		}
		catch (PaymentResponseException e) {
			transactionEnquiry.setStatus(e.getError());
			responselogger.error("{} {}", transactionCode, e);
		}
		catch (Exception e) {
			transactionEnquiry.setStatus(ErrorCode.UNDEFINE_EXCEPTION);
			responselogger.error("{} {}", transactionCode, e);
		}
		return transactionEnquiry;
	}
}
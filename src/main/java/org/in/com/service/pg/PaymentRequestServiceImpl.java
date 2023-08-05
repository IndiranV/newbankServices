package org.in.com.service.pg;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.in.com.aggregator.payment.PGInterface;
import org.in.com.aggregator.payment.impl.GatewayFactory;
import org.in.com.cache.PaymentCache;
import org.in.com.cache.TicketCache;
import org.in.com.config.GatewayConfig;
import org.in.com.constants.Numeric;
import org.in.com.dao.PaymentGatewayLoggingDAO;
import org.in.com.dao.PaymentGatewayPreTransactionDAO;
import org.in.com.dao.PaymentGatewayTransactionDAO;
import org.in.com.dao.PaymentMerchantGatewayCredentialsDAO;
import org.in.com.dao.PaymentMerchantGatewayScheduleDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.OrderInitRequestDTO;
import org.in.com.dto.OrderInitStatusDTO;
import org.in.com.dto.PaymentGatewayCredentialsDTO;
import org.in.com.dto.PaymentGatewayScheduleDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.PaymentPreTransactionDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.PaymentRequestException;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @Desc
 *       This service class takes the control of
 *       initPayment - Server to Server Order details are shared to before
 *       redirecting end user to bank
 * 
 */
@Service
public class PaymentRequestServiceImpl implements PaymentRequestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentRequestServiceImpl.class);

	@Override
	public OrderInitStatusDTO handlePgService(AuthDTO authDTO, OrderInitRequestDTO orderInitDetails) {

		OrderInitStatusDTO orderStatus = new OrderInitStatusDTO();
		try {
			LOGGER.info("payment initialization started {} {} {} {}", authDTO.getNamespace().getId(), orderInitDetails.getTransactionCode(), orderInitDetails.getPartnerCode(), orderInitDetails.getAmount());

			PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();

			/** Getting active gateway for current date */

			PaymentMerchantGatewayScheduleDAO gatewayScheduleDAO = new PaymentMerchantGatewayScheduleDAO();
			PaymentGatewayScheduleDTO gatewayScheduleDTO = gatewayScheduleDAO.getPaymentGatewayForNamespace(authDTO, orderInitDetails);

			if (gatewayScheduleDTO == null || gatewayScheduleDTO.getGatewayPartner() == null || gatewayScheduleDTO.getGatewayPartner().getGatewayProvider() == null) {
				throw new PaymentRequestException(ErrorCode.NO_GATEWAY_FOUND);
			}

			// Get amount details and validate transaction attempts
			PaymentPreTransactionDTO preTransactionDetailsDTO = makeTransaction(authDTO, orderInitDetails, gatewayScheduleDTO);
			if (preTransactionDetailsDTO == null) {
				LOGGER.warn("This transaction has been already intiated - name space id -{0} || transaction id - {1}", authDTO.getNamespace().getId(), orderInitDetails.getTransactionCode());
				throw new ServiceException(ErrorCode.TRANSACTION_ALREADY_INITIATED);
			}

			PaymentMerchantGatewayCredentialsDAO merchantGatewayDetails = new PaymentMerchantGatewayCredentialsDAO();
			PaymentGatewayCredentialsDTO gatewayCredentials = merchantGatewayDetails.getNamespacePGCredentials(authDTO.getNamespace(), gatewayScheduleDTO.getGatewayPartner().getGatewayProvider().getId());

			if (gatewayCredentials == null) {
				throw new PaymentRequestException(ErrorCode.NO_GATEWAY_FOUND);
			}
			preTransactionDetailsDTO.setUser(authDTO.getUser());
			preTransactionDetailsDTO.setGatewayPartner(gatewayScheduleDTO.getGatewayPartner());
			preTransactionDetailsDTO.setGatewayCredentials(gatewayCredentials);
			preTransactionDetailsDTO.setGatewayProvider(gatewayScheduleDTO.getGatewayPartner().getGatewayProvider());
			preTransactionDetailsDTO.setTransactionType(PaymentGatewayTransactionTypeEM.PAYMENT);
			preTransactionDetailsDTO.setOrderType(orderInitDetails.getOrderType());
			preTransactionDetailsDTO.setFailureErrorCode(PaymentGatewayStatusEM.ORDER_INITIATED.getCode());

			paymentPreTransactionDAO.insert(authDTO, preTransactionDetailsDTO);

			OrderDTO order = new OrderDTO();
			order.setAmount(orderInitDetails.getAmount().setScale(0, RoundingMode.HALF_UP));
			order.setTransactionCode(orderInitDetails.getTransactionCode());
			order.setOrderType(orderInitDetails.getOrderType());
			order.setGatewayPartnerCode(gatewayScheduleDTO.getGatewayPartner().getApiProviderCode());
			order.setGatewayCredentials(gatewayCredentials);
			order.setGatewayProvider(gatewayScheduleDTO.getGatewayPartner().getGatewayProvider());
			order.setResponseUrl(orderInitDetails.getResponseUrl());
			order.setOrderCode(orderInitDetails.getOrderCode());
			order.setAuthToken(authDTO.getAuthToken());
			order.setFirstName(orderInitDetails.getFirstName());
			order.setLastName(orderInitDetails.getLastName());
			order.setAddress1(orderInitDetails.getAddress1());
			order.setAddress2(orderInitDetails.getAddress2());
			order.setMobile(orderInitDetails.getMobile());
			order.setEmail(orderInitDetails.getEmail());
			order.setCity(orderInitDetails.getCity());
			order.setState(orderInitDetails.getState());
			order.setUdf1(orderInitDetails.getUdf1());
			order.setUdf2(orderInitDetails.getUdf2());
			order.setUdf3(orderInitDetails.getUdf3());
			order.setUdf4(orderInitDetails.getUdf4());
			order.setUdf5(orderInitDetails.getUdf5());

			pgHandling(authDTO, order);

			if (StringUtil.isNotNull(order.getGatewayTransactionCode())) {
				preTransactionDetailsDTO.setGatewayTransactionCode(order.getGatewayTransactionCode());
				paymentPreTransactionDAO.updateGatewayTransactionCode(authDTO, preTransactionDetailsDTO);
			}
			orderStatus.setGatewayInputDetails(order.getGatewayFormParam());
			orderStatus.setGatewayCode(gatewayScheduleDTO.getGatewayPartner().getGatewayProvider().getCode());
			orderStatus.setPaymentRequestUrl(order.getGatewayUrl());
			orderStatus.setTransactionCode(preTransactionDetailsDTO.getCode());

			orderStatus.setStatus(ErrorCode.SUCCESS);

			// Operator website redirection url forming logic
			String namespaceResponseUrl = order.getResponseUrl();
			if (StringUtil.isNull(namespaceResponseUrl)) {
				namespaceResponseUrl = authDTO.getDomainUrl();
			}

			order.setResponseUrl(namespaceResponseUrl);
			// Add transaction to cache
			PaymentCache paymentCache = new PaymentCache();
			paymentCache.putTransactionDetailsInCache(order);

			LOGGER.info("payment initialization ended {} {}", authDTO.getNamespace().getId(), orderInitDetails.getTransactionCode());
		}
		catch (ServiceException e) {
			LOGGER.error(orderInitDetails.getTransactionCode(), e);
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			LOGGER.error(orderInitDetails.getTransactionCode(), e);
			throw new ServiceException(e.getMessage());
		}
		return orderStatus;

	}

	private PaymentPreTransactionDTO makeTransaction(AuthDTO authDTO, OrderInitRequestDTO orderInitDetails, PaymentGatewayScheduleDTO gatewayScheduleDTO) {

		// Like API,we get from DB/API/Cache
		if (orderInitDetails.getOrderType().getId() == OrderTypeEM.TICKET.getId()) {
			TicketCache ticketCache = new TicketCache();
			BookingDTO bookingDTO = ticketCache.getBookingDTO(authDTO, orderInitDetails.getOrderCode());
			if (bookingDTO == null) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_ID);
			}

			TicketDTO ticketDTO = bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP);
			if (ticketDTO == null) {
				ticketDTO = bookingDTO.getTicketList().get(0);
			}
			orderInitDetails.setAmount(bookingDTO.getTransactionAmount().setScale(0, RoundingMode.HALF_UP));
			orderInitDetails.setEmail(ticketDTO.getPassengerEmailId());
			orderInitDetails.setMobile(ticketDTO.getPassengerMobile());
		}
		else if (orderInitDetails.getOrderType().getId() == OrderTypeEM.RECHARGE.getId()) {
			// TODO: check with DB to get Amount
			// testing propose we hard amount
			orderInitDetails.setAmount(orderInitDetails.getAmount());
		}

		BigDecimal serviceCharge = BigDecimal.ZERO;
		if (gatewayScheduleDTO.getServiceCharge().compareTo(BigDecimal.ZERO) == 1) {
			serviceCharge = orderInitDetails.getAmount().multiply(gatewayScheduleDTO.getServiceCharge()).divide(Numeric.ONE_HUNDRED);
			orderInitDetails.setAmount(orderInitDetails.getAmount().add(serviceCharge).setScale(0, RoundingMode.HALF_UP));
		}

		PaymentGatewayPreTransactionDAO preTransactionDAO = new PaymentGatewayPreTransactionDAO();
		PaymentPreTransactionDTO preTransactionDetailsDTO = preTransactionDAO.CheckAndMakeTransaction(authDTO, orderInitDetails);

		// TODO:Update to Reference if tried payment gateway transaction
		preTransactionDetailsDTO.setServiceCharge(serviceCharge);
		preTransactionDetailsDTO.setAmount(orderInitDetails.getAmount());
		preTransactionDetailsDTO.setStatus(PaymentGatewayStatusEM.ORDER_INITIATED);
		orderInitDetails.setTransactionCode(preTransactionDetailsDTO.getCode());
		return preTransactionDetailsDTO;
	}

	private void pgHandling(AuthDTO authDTO, OrderDTO order) throws Exception {

		try {
			PGInterface gatewayInstance = GatewayFactory.returnPgInstance(order.getGatewayProvider().getServiceName());
			if (gatewayInstance == null) {
				throw new PaymentRequestException(ErrorCode.NO_GATEWAY_FOUND);
			}
			String gatewayReturnURL = order.getGatewayCredentials().getPgReturnUrl();
			if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.WEB_USER.getId()) {
				gatewayReturnURL = gatewayReturnURL.replace("www", "m");
			}
			if (order.getGatewayProvider().getServiceName().equals("KhaltiPaymentServiceImpl")) {
				order.setReturnUrl(gatewayReturnURL + GatewayConfig.COMMON_RETURN_PATH);
			}
			else {
				order.setReturnUrl(gatewayReturnURL + GatewayConfig.COMMON_RETURN_PATH + "?eTransactionCode=" + order.getTransactionCode());
			}
			/** Gateway implementation is invoked */
			gatewayInstance.packPaymentRequest(authDTO, order);

		}
		catch (Exception e) {
			throw e;
		}
		finally {
			PaymentGatewayLoggingDAO loggingDAO = new PaymentGatewayLoggingDAO();
			loggingDAO.pgresponselogging(order);
		}
	}

	@Override
	public boolean CheckPaymentStatus(AuthDTO authDTO, String referenceCode) {
		PaymentGatewayTransactionDAO transactionDAO = new PaymentGatewayTransactionDAO();
		return transactionDAO.getTransactionStatus(authDTO, referenceCode);
	}

	@Override
	public PaymentGatewayTransactionDTO getPaymentGatewayTransactionAmount(AuthDTO authDTO, String orderCode, PaymentGatewayTransactionTypeEM transactionTypeEM) {
		PaymentGatewayTransactionDAO transactionDAO = new PaymentGatewayTransactionDAO();
		return transactionDAO.getPaymentGatewayTransaction(authDTO, orderCode, transactionTypeEM);
	}

	@Override
	public List<PaymentGatewayTransactionDTO> getPaymentGatewayTransaction(AuthDTO authDTO, String orderCode) {
		PaymentGatewayTransactionDAO transactionDAO = new PaymentGatewayTransactionDAO();
		return transactionDAO.getPaymentGatewayTransaction(authDTO, orderCode);
	}
}

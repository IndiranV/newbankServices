package org.in.com.service.pg;

import java.math.BigDecimal;

import org.in.com.aggregator.payment.PGInterface;
import org.in.com.aggregator.payment.impl.GatewayFactory;
import org.in.com.dao.PaymentGatewayLoggingDAO;
import org.in.com.dao.PaymentGatewayPreTransactionDAO;
import org.in.com.dao.PaymentGatewayProviderDAO;
import org.in.com.dao.PaymentGatewayTransactionDAO;
import org.in.com.dao.PaymentMerchantGatewayCredentialsDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.PaymentGatewayCredentialsDTO;
import org.in.com.dto.PaymentGatewayProviderDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.PaymentPreTransactionDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TicketRefundDTO;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.dto.enumeration.RefundStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.TicketService;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class PaymentRefundServiceImpl implements PaymentRefundService {

	private static final Logger LOGGER = LoggerFactory.getLogger("org.in.com.service.pg.PaymentRefundServiceImpl");
	@Autowired
	TicketService ticketService;

	@Async
	public void doRefund(AuthDTO authDTO, RefundDTO refund) {
		LOGGER.info("PG Auto Refund Init " + refund.getOrderCode() + refund.getOrderTransactionCode() + refund.getAmount());
		try {
			PaymentGatewayTransactionDAO paymentGatewayTransactionDAO = new PaymentGatewayTransactionDAO();

			PaymentGatewayTransactionDTO transactionDetails = paymentGatewayTransactionDAO.getPaymentGatewayTransaction(authDTO, refund.getOrderCode(), PaymentGatewayTransactionTypeEM.PAYMENT);
			if (transactionDetails == null) {
				throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID);
			}
			if (transactionDetails.getAmount().compareTo(refund.getAmount()) == -1) {
				throw new ServiceException(ErrorCode.INVALID_REFUND_AMOUNT);

			}
			PaymentGatewayProviderDAO gatewayProviderDAO = new PaymentGatewayProviderDAO();
			PaymentGatewayProviderDTO gatewayProviderDTO = gatewayProviderDAO.getPGDetails(transactionDetails.getGatewayProvider().getId());

			PaymentMerchantGatewayCredentialsDAO merchantGatewayDetails = new PaymentMerchantGatewayCredentialsDAO();
			PaymentGatewayCredentialsDTO gatewayCredentials = merchantGatewayDetails.getNamespacePGCredentials(authDTO.getNamespace(), transactionDetails.getGatewayProvider().getId());

			if (gatewayCredentials == null || gatewayCredentials.getAccessCode() == null) {
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}
			PGInterface gatewayInstance = GatewayFactory.returnPgInstance(gatewayProviderDTO.getServiceName());
			refund.setGatewayTransactionCode(transactionDetails.getGatewayTransactionCode());
			refund.setGatewayCredentials(gatewayCredentials);
			refund.setTransactionCode(transactionDetails.getCode());
			try {
				if (refund.getAmount().compareTo(BigDecimal.ZERO) == 1) {
					gatewayInstance.refund(authDTO, refund);
				}
				else if (refund.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
					refund.setStatus(PaymentGatewayStatusEM.SUCCESS);
				}
			}
			catch (ServiceException e) {
				refund.setErrorCode(e.getErrorCode());
				LOGGER.error(refund.getTransactionCode(), e);
				LOGGER.error("PG Auto Refund Fail " + refund.getOrderCode() + refund.getOrderTransactionCode() + refund.getAmount());
			}
			finally {
				if (refund.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId()) {
					PaymentGatewayTransactionDTO transactionEntity = new PaymentGatewayTransactionDTO();
					// TODO need to change this model mapper after fixing this
					// id mapping error
					transactionEntity.setCode(TokenGenerator.generateCode("ARF"));
					transactionEntity.setGatewayTransactionCode(refund.getGatewayTransactionCode());
					transactionEntity.setUser(transactionDetails.getUser());
					transactionEntity.setDeviceMedium(authDTO.getDeviceMedium());
					transactionEntity.setOrderCode(refund.getOrderCode());
					transactionEntity.setGatewayPartner(transactionDetails.getGatewayPartner());
					transactionEntity.setAmount(refund.getAmount());
					// transactionEntity.setServiceCharge();
					transactionEntity.setTransactionType(PaymentGatewayTransactionTypeEM.REFUND);
					transactionEntity.setGatewayCredentials(transactionDetails.getGatewayCredentials());
					transactionEntity.setGatewayProvider(transactionDetails.getGatewayProvider());
					PaymentGatewayTransactionDAO gatewayTransaction = new PaymentGatewayTransactionDAO();
					gatewayTransaction.insert(authDTO, transactionEntity);

					// Update ticket Transaction refund status
					if (refund.getOrderType().getId() == OrderTypeEM.TICKET.getId() && StringUtil.isNotNull(refund.getOrderTransactionCode())) {
						TicketRefundDTO refundDTO = new TicketRefundDTO();
						refundDTO.setTransactionCode(refund.getOrderTransactionCode());
						refundDTO.setRefundStatus(RefundStatusEM.REQUEST_TO_BANK);
						ticketService.updateRefundTicket(authDTO, refundDTO);
					}
				}
				else {
					PaymentPreTransactionDTO preTransactionDetailsDTO = new PaymentPreTransactionDTO();
					preTransactionDetailsDTO.setCode(transactionDetails.getCode());
					preTransactionDetailsDTO.setOrderCode(refund.getOrderCode());
					preTransactionDetailsDTO.setUser(authDTO.getUser());
					preTransactionDetailsDTO.setGatewayTransactionCode(transactionDetails.getGatewayTransactionCode());
					preTransactionDetailsDTO.setAmount(refund.getAmount());
					preTransactionDetailsDTO.setStatus(PaymentGatewayStatusEM.FAILURE);
					preTransactionDetailsDTO.setGatewayPartner(transactionDetails.getGatewayPartner());
					preTransactionDetailsDTO.setGatewayCredentials(transactionDetails.getGatewayCredentials());
					preTransactionDetailsDTO.setGatewayProvider(transactionDetails.getGatewayProvider());
					preTransactionDetailsDTO.setTransactionType(PaymentGatewayTransactionTypeEM.REFUND);
					preTransactionDetailsDTO.setOrderType(refund.getOrderType());
					preTransactionDetailsDTO.setFailureErrorCode(refund.getStatus().getCode());
					// TODO : Change this automatically creating and updating
					// date logic
					PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();
					paymentPreTransactionDAO.insert(authDTO, preTransactionDetailsDTO);
				}
			}

		}
		catch (ServiceException e) {
			refund.setErrorCode(e.getErrorCode());
			LOGGER.error(refund.getTransactionCode(), e);
		}
		catch (Exception e) {
			refund.setErrorCode(ErrorCode.PAYMENT_DECLINED);
			LOGGER.error(refund.getTransactionCode(), e);
		}
		finally {
			// TODO use some resuable method
			try {
				StringBuilder logging = new StringBuilder();
				logging.append("Request sent=").append(refund.getAmount()).append(",").append(refund.getTransactionCode()).append("||Response Rcvd=").append(refund.getResponseRecevied());

				PaymentGatewayLoggingDAO loggingDAO = new PaymentGatewayLoggingDAO();
				loggingDAO.pgRefundLogging(refund);

			}
			catch (Exception e) {
				LOGGER.error(refund.getTransactionCode(), e);
			}
		}
	}
}

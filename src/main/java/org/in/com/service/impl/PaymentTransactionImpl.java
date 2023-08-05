package org.in.com.service.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.sms.SMSService;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.UserCache;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ConnectDAO;
import org.in.com.dao.PaymentTransactionDAO;
import org.in.com.dao.TicketTransactionDAO;
import org.in.com.dao.UserTransactionDAO;
import org.in.com.dto.AuditDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.PaymentReceiptDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.PaymentAcknowledgeEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.dto.enumeration.PaymentReceiptTypeEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.PaymentTransactionService;
import org.in.com.service.SectorService;
import org.in.com.service.TicketService;
import org.in.com.service.UserService;
import org.in.com.service.UserTransactionService;
import org.in.com.service.pg.PaymentRequestService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import lombok.Cleanup;
import net.sf.ehcache.Element;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class PaymentTransactionImpl extends UserCache implements PaymentTransactionService {
	@Autowired
	UserService userService;
	@Autowired
	UserTransactionService userTransactionService;
	@Autowired
	PaymentRequestService paymentRequestService;
	@Autowired
	EmailService emailService;
	@Autowired
	SMSService smsService;
	@Autowired
	TicketService ticketService;
	@Autowired
	SectorService sectorService;
	private static final Logger logger = LoggerFactory.getLogger(PaymentTransactionImpl.class);

	@Override
	public void rechargeTransaction(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		if (!authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		UserDTO userDTO = userService.getUser(authDTO, paymentTransactionDTO.getUser());
		UserDTO paymentHandledByUser = userService.getUser(authDTO, paymentTransactionDTO.getPaymentHandledByUser());
		if (userDTO == null || paymentHandledByUser == null || userDTO.getId() == 0 || paymentHandledByUser.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_USER_CODE);
		}
		paymentTransactionDTO.setUser(userDTO);
		paymentTransactionDTO.setPaymentHandledByUser(paymentHandledByUser);
		PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
		paymentTransactionDAO.SaveRechargeTransaction(authDTO, paymentTransactionDTO);

		// Recharge auto approval flow
		if (authDTO.getNamespace().getProfile().isRechargeAutoApprovalFlag()) {
			paymentTransactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED);
			acknowledgeTransaction(authDTO, paymentTransactionDTO);
		}
		sendPaymentNotification(authDTO, paymentTransactionDTO, null);

	}

	@Override
	public void acknowledgeTransaction(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		PaymentTransactionDTO repoPaymentTransaction = new PaymentTransactionDTO();
		repoPaymentTransaction.setCode(paymentTransactionDTO.getCode());

		PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
		paymentTransactionDAO.getPaymentTransaction(authDTO, repoPaymentTransaction);
		if (repoPaymentTransaction.getId() == 0 || repoPaymentTransaction.getLookupId() != 0) {
			throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID);
		}

		if (paymentTransactionDTO.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED.getId() || paymentTransactionDTO.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID.getId()) {
			// Validate Fresh Transaction
			validateFreshTransaction(paymentTransactionDTO.getCode());

			if (!authDTO.getNamespace().getProfile().isRechargeAutoApprovalFlag() && repoPaymentTransaction.getPaymentAcknowledge().getId() != PaymentAcknowledgeEM.PAYMENT_PAID.getId() && repoPaymentTransaction.getPaymentAcknowledge().getId() != PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID.getId()) {
				throw new ServiceException(ErrorCode.TRANSACTION_INVALID_ACK);
			}
			if (paymentTransactionDTO.getTransactionAmount() != null && paymentTransactionDTO.getTransactionAmount().compareTo(repoPaymentTransaction.getTransactionAmount()) == 1) {
				throw new ServiceException(ErrorCode.MISMATCH_IN_TRANSACTION_AMOUNT);
			}
			if (paymentTransactionDTO.getTransactionAmount() != null && paymentTransactionDTO.getTransactionAmount().compareTo(repoPaymentTransaction.getTransactionAmount()) == 0 && repoPaymentTransaction.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID.getId()) {
				throw new ServiceException(ErrorCode.MISMATCH_IN_TRANSACTION_AMOUNT);
			}

			repoPaymentTransaction.setUser(getUserDTOById(authDTO, repoPaymentTransaction.getUser()));
			repoPaymentTransaction.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED);
			repoPaymentTransaction.setRemarks(repoPaymentTransaction.getRemarks() + Text.SINGLE_SPACE + paymentTransactionDTO.getRemarks());

			// Update User Balance
			UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
			userTransactionDTO.setTransactionAmount(repoPaymentTransaction.getTransactionAmount());
			userTransactionDTO.setTransactionMode(repoPaymentTransaction.getTransactionMode());
			userTransactionDTO.setTransactionType(repoPaymentTransaction.getTransactionType());
			userTransactionDTO.setTdsTax(BigDecimal.ZERO);

			// Credit
			if (repoPaymentTransaction.getTransactionType().getCreditDebitFlag().equals(Text.CREDIT)) {
				userTransactionDTO.setCreditAmount(repoPaymentTransaction.getTransactionAmount());
				userTransactionDTO.setDebitAmount(BigDecimal.ZERO);
			}
			// Debit
			if (repoPaymentTransaction.getTransactionType().getCreditDebitFlag().equals(Text.DEDIT)) {
				userTransactionDTO.setCreditAmount(BigDecimal.ZERO);
				userTransactionDTO.setDebitAmount(repoPaymentTransaction.getTransactionAmount());
			}

			if (paymentTransactionDTO.getTransactionAmount() != null && paymentTransactionDTO.getTransactionAmount().compareTo(repoPaymentTransaction.getTransactionAmount()) == -1) {
				PaymentTransactionDTO paymentPartialTransaction = acknowledgePaymentVoucherPartialTransaction(authDTO, repoPaymentTransaction, paymentTransactionDTO, userTransactionDTO);
				repoPaymentTransaction.setPaymentAcknowledge(paymentPartialTransaction.getPaymentAcknowledge());
				repoPaymentTransaction.setUserTransaction(userTransactionDTO);

				paymentTransactionDAO.getAcknowledgedPaymentTransactionUpdate(authDTO, repoPaymentTransaction);
			}
			else if (paymentTransactionDTO.getTransactionAmount() == null || paymentTransactionDTO.getTransactionAmount().compareTo(repoPaymentTransaction.getTransactionAmount()) == 0) {
				userTransactionDTO.setRefferenceCode(repoPaymentTransaction.getCode());
				userTransactionDTO.setRefferenceId(repoPaymentTransaction.getId());
				userTransactionService.SaveUserTransaction(authDTO, repoPaymentTransaction.getUser(), userTransactionDTO);
				repoPaymentTransaction.setUserTransaction(userTransactionDTO);

				paymentTransactionDAO.getAcknowledgedPaymentTransactionUpdate(authDTO, repoPaymentTransaction);
			}
		}
		else if (paymentTransactionDTO.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PAYMENT_REJECT.getId()) {
			if (repoPaymentTransaction.getPaymentAcknowledge().getId() != PaymentAcknowledgeEM.PAYMENT_INITIATED.getId()) {
				throw new ServiceException(ErrorCode.TRANSACTION_ALREADY_ACK);
			}
			List<PaymentTransactionDTO> partialPaymentTransactionList = paymentTransactionDAO.getPartialPaymentTransaction(authDTO, repoPaymentTransaction);
			BigDecimal totalTransactionAmount = BigDecimal.ZERO;
			for (PaymentTransactionDTO paymentTransaction : partialPaymentTransactionList) {
				totalTransactionAmount = totalTransactionAmount.add((paymentTransaction.getTransactionAmount()));
			}

			if (!partialPaymentTransactionList.isEmpty() && totalTransactionAmount.compareTo(repoPaymentTransaction.getTransactionAmount()) == -1) {
				throw new ServiceException(ErrorCode.TRANSACTION_ALREADY_INITIATED);
			}

			UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
			repoPaymentTransaction.setUserTransaction(userTransactionDTO);
			repoPaymentTransaction.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_REJECT);
			repoPaymentTransaction.setRemarks(repoPaymentTransaction.getRemarks() + Text.SINGLE_SPACE + paymentTransactionDTO.getRemarks());
			paymentTransactionDAO.getAcknowledgedPaymentTransactionUpdate(authDTO, repoPaymentTransaction);
			// Clear related ticket transaction
			if (repoPaymentTransaction.getTransactionType().getId() == TransactionTypeEM.PAYMENT_VOUCHER.getId()) {
				clearGeneratePaymentVoucher(authDTO, repoPaymentTransaction);
			}
		}
		else if (paymentTransactionDTO.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PAYMENT_PAID.getId()) {
			if (repoPaymentTransaction.getPaymentAcknowledge().getId() != PaymentAcknowledgeEM.PAYMENT_INITIATED.getId()) {
				throw new ServiceException(ErrorCode.TRANSACTION_INVALID_ACK);
			}
			UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
			repoPaymentTransaction.setUserTransaction(userTransactionDTO);
			repoPaymentTransaction.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_PAID);
			repoPaymentTransaction.setRemarks(repoPaymentTransaction.getRemarks() + Text.SINGLE_SPACE + paymentTransactionDTO.getRemarks());
			paymentTransactionDAO.getAcknowledgedPaymentTransactionUpdate(authDTO, repoPaymentTransaction);
		}

		sendPaymentNotification(authDTO, paymentTransactionDTO, repoPaymentTransaction);
	}

	public static synchronized void validateFreshTransaction(String transactionCode) {
		if (EhcacheManager.getFreshTransactionEhCache().get(transactionCode) == null) {
			Element element = new Element(transactionCode, transactionCode);
			EhcacheManager.getFreshTransactionEhCache().put(element);
		}
		else {
			throw new ServiceException(ErrorCode.PARALLEL_SAME_TRANSACTION_OCCUR);
		}
	}

	private PaymentTransactionDTO acknowledgePaymentVoucherPartialTransaction(AuthDTO authDTO, PaymentTransactionDTO repoPaymentTransaction, PaymentTransactionDTO paymentTransactionDTO, UserTransactionDTO userTransactionDTO) {
		PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
		List<PaymentTransactionDTO> partialPaymentTransactionList = paymentTransactionDAO.getPartialPaymentTransaction(authDTO, repoPaymentTransaction);

		BigDecimal totalTransactionAmount = BigDecimal.ZERO;
		for (PaymentTransactionDTO paymentTransaction : partialPaymentTransactionList) {
			totalTransactionAmount = totalTransactionAmount.add((paymentTransaction.getTransactionAmount()));
		}

		PaymentTransactionDTO paymentPartialTransaction = new PaymentTransactionDTO();
		paymentPartialTransaction.setCode(repoPaymentTransaction.getPaymentTransactionCode());
		paymentPartialTransaction.setUser(repoPaymentTransaction.getUser());
		paymentPartialTransaction.setTransactionAmount(paymentTransactionDTO.getTransactionAmount());
		paymentPartialTransaction.setCommissionAmount(BigDecimal.ZERO);
		paymentPartialTransaction.setAcBusTax(BigDecimal.ZERO);
		paymentPartialTransaction.setTdsTax(BigDecimal.ZERO);
		paymentPartialTransaction.setPaymentHandledByUser(authDTO.getUser());
		paymentPartialTransaction.setTransactionType(repoPaymentTransaction.getTransactionType());
		paymentPartialTransaction.setTransactionMode(paymentTransactionDTO.getTransactionMode());
		paymentPartialTransaction.setAmountReceivedDate(paymentTransactionDTO.getAmountReceivedDate());
		paymentPartialTransaction.setRemarks(paymentTransactionDTO.getRemarks());

		if (totalTransactionAmount.add(paymentTransactionDTO.getTransactionAmount()).compareTo(repoPaymentTransaction.getTransactionAmount()) == 1) {
			throw new ServiceException(ErrorCode.MISMATCH_IN_TRANSACTION_AMOUNT);
		}
		else if (totalTransactionAmount.add(paymentTransactionDTO.getTransactionAmount()).compareTo(repoPaymentTransaction.getTransactionAmount()) == -1) {
			paymentPartialTransaction.setPaymentAcknowledge(PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID);
		}
		else if (totalTransactionAmount.add(paymentTransactionDTO.getTransactionAmount()).compareTo(repoPaymentTransaction.getTransactionAmount()) == 0) {
			paymentPartialTransaction.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED);
		}
		paymentPartialTransaction.setLookupId(repoPaymentTransaction.getId());

		userTransactionService.generatePaymentVoucherV2(authDTO, paymentPartialTransaction);

		userTransactionDTO.setTransactionAmount(paymentTransactionDTO.getTransactionAmount());
		userTransactionDTO.setRefferenceId(paymentPartialTransaction.getId());
		userTransactionDTO.setRefferenceCode(paymentPartialTransaction.getCode());
		// Credit
		if (repoPaymentTransaction.getTransactionType().getCreditDebitFlag().equals(Text.CREDIT)) {
			userTransactionDTO.setCreditAmount(paymentTransactionDTO.getTransactionAmount());
			userTransactionDTO.setDebitAmount(BigDecimal.ZERO);
		}
		// Debit
		if (repoPaymentTransaction.getTransactionType().getCreditDebitFlag().equals(Text.DEDIT)) {
			userTransactionDTO.setCreditAmount(BigDecimal.ZERO);
			userTransactionDTO.setDebitAmount(paymentTransactionDTO.getTransactionAmount());
		}
		userTransactionService.SaveUserTransaction(authDTO, repoPaymentTransaction.getUser(), userTransactionDTO);
		paymentPartialTransaction.setUserTransaction(userTransactionDTO);

		paymentTransactionDAO.getAcknowledgedPaymentTransactionUpdate(authDTO, paymentPartialTransaction);
		return paymentPartialTransaction;
	}

	@Override
	public void getUnAcknowledgeTransaction(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
		paymentTransactionDAO.getALLUnAcknowledgedPaymentTransaction(authDTO, paymentTransactionDTO);

		SectorDTO sector = sectorService.getActiveUserSectorOrganization(authDTO);
		for (Iterator<PaymentTransactionDTO> iterator = paymentTransactionDTO.getList().iterator(); iterator.hasNext();) {
			PaymentTransactionDTO transactionDTO = iterator.next();
			transactionDTO.setUser(getUserDTOById(authDTO, transactionDTO.getUser()));

			// Apply Sector User filter
			if (sector.getActiveFlag() == Numeric.ONE_INT && BitsUtil.isOrganizationExists(sector.getOrganization(), transactionDTO.getUser().getOrganization()) == null) {
				iterator.remove();
				continue;
			}

			transactionDTO.setPaymentHandledByUser(getUserDTOById(authDTO, transactionDTO.getPaymentHandledByUser()));

			for (PaymentTransactionDTO partialPaymentDTO : transactionDTO.getPartialPaymentPaidList()) {
				partialPaymentDTO.setUser(getUserDTOById(authDTO, partialPaymentDTO.getUser()));
				partialPaymentDTO.setPaymentHandledByUser(getUserDTOById(authDTO, partialPaymentDTO.getPaymentHandledByUser()));
			}
		}
	}

	@Override
	public PaymentTransactionDTO getPaymentTransactionHistory(AuthDTO authDTO, UserDTO userDTO, PaymentTransactionDTO transactionDTO, DateTime fromDate, DateTime toDate) {
		PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
		return paymentTransactionDAO.getPaymentTransactionHistory(authDTO, userDTO, transactionDTO, fromDate, toDate);
	}

	@Override
	public PaymentTransactionDTO getPaymentTransaction(AuthDTO authDTO, PaymentTransactionDTO transactionDTO) {
		PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
		transactionDTO = paymentTransactionDAO.getPaymentTransactionV2(authDTO, transactionDTO);
		transactionDTO.setUser(getUserDTOById(authDTO, transactionDTO.getUser()));
		transactionDTO.setPaymentHandledByUser(getUserDTOById(authDTO, transactionDTO.getPaymentHandledByUser()));

		for (PaymentTransactionDTO partialPaymentDTO : transactionDTO.getPartialPaymentPaidList()) {
			partialPaymentDTO.setUser(getUserDTOById(authDTO, partialPaymentDTO.getUser()));
			partialPaymentDTO.setPaymentHandledByUser(getUserDTOById(authDTO, partialPaymentDTO.getPaymentHandledByUser()));
		}
		return transactionDTO;
	}

	@Override
	public PaymentTransactionDTO getPaymentTransactionByTicket(AuthDTO authDTO, TicketDTO ticketDTO) {
		ticketService.getTicketStatus(authDTO, ticketDTO);

		TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
		ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
		ticketDTO.setTicketXaction(ticketTransactionDTO);

		ticketService.getTicketTransaction(authDTO, ticketDTO);

		PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
		if (ticketDTO.getTicketXaction() != null && ticketDTO.getTicketXaction().getPaymentTrans() != null && ticketDTO.getTicketXaction().getPaymentTrans().getId() != Numeric.ZERO_INT) {
			paymentTransactionDTO = getPaymentTransaction(authDTO, ticketDTO.getTicketXaction().getPaymentTrans());
		}
		return paymentTransactionDTO;
	}

	@Override
	public synchronized PaymentTransactionDTO rechargeGatewayTransaction(AuthDTO authDTO, String orderCode) {
		PaymentGatewayTransactionDTO transactionDTO = paymentRequestService.getPaymentGatewayTransactionAmount(authDTO, orderCode, PaymentGatewayTransactionTypeEM.PAYMENT);

		PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
		PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
		paymentTransactionDTO.setCode(orderCode);
		paymentTransactionDAO.getPaymentTransaction(authDTO, paymentTransactionDTO);

		if (StringUtil.isNotNull(transactionDTO.getCode()) && paymentTransactionDTO.getId() == 0) {
			paymentTransactionDTO.setTransactionAmount(transactionDTO.getAmount().subtract(transactionDTO.getServiceCharge()));
			paymentTransactionDTO.setTransactionType(TransactionTypeEM.RECHARGE);
			paymentTransactionDTO.setTransactionMode(TransactionModeEM.PAYMENT_PAYMENT_GATEWAY);
			paymentTransactionDTO.setCommissionAmount(BigDecimal.ZERO);
			paymentTransactionDTO.setAmountReceivedDate(DateTime.now().toString(DateUtil.JODA_DATE_FORMATE));
			paymentTransactionDTO.setPaymentHandledByUser(authDTO.getUser());
			paymentTransactionDTO.setUser(authDTO.getUser());
			paymentTransactionDTO.setTdsTax(BigDecimal.ZERO);
			paymentTransactionDTO.setAcBusTax(BigDecimal.ZERO);
			paymentTransactionDTO.setRemarks("Online Recharge: Rs" + transactionDTO.getAmount() + " include PG charge:Rs." + transactionDTO.getServiceCharge());
			paymentTransactionDAO.SaveRechargeTransaction(authDTO, paymentTransactionDTO);

			// Update User Balance
			UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
			userTransactionDTO.setTransactionAmount(paymentTransactionDTO.getTransactionAmount());
			userTransactionDTO.setCreditAmount(paymentTransactionDTO.getTransactionAmount());
			userTransactionDTO.setDebitAmount(BigDecimal.ZERO);
			userTransactionDTO.setTdsTax(BigDecimal.ZERO);

			userTransactionDTO.setTransactionMode(paymentTransactionDTO.getTransactionMode());
			userTransactionDTO.setTransactionType(paymentTransactionDTO.getTransactionType());
			userTransactionDTO.setRefferenceCode(paymentTransactionDTO.getCode());
			userTransactionDTO.setRefferenceId(paymentTransactionDTO.getId());
			userTransactionService.SaveUserTransaction(authDTO, paymentTransactionDTO.getUser(), userTransactionDTO);

			paymentTransactionDTO.setUserTransaction(userTransactionDTO);
			paymentTransactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED);
			paymentTransactionDAO.getAcknowledgedPaymentTransactionUpdate(authDTO, paymentTransactionDTO);

			smsService.sendRechargeSMS(authDTO, paymentTransactionDTO);
		}
		else {
			throw new ServiceException(ErrorCode.TRANSACTION_FAIL_BALANCE_ISSUES);
		}
		return paymentTransactionDTO;
	}

	private void clearGeneratePaymentVoucher(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);
				TicketTransactionDAO ticketTransactionDao = new TicketTransactionDAO();
				List<TicketTransactionDTO> transactionList = ticketTransactionDao.getTicketTransactionFindPaymentId(authDTO, paymentTransactionDTO);
				paymentTransactionDTO.setId(0);
				for (TicketTransactionDTO transactionDTO : transactionList) {
					paymentTransactionDTO.setTransactionCode(transactionDTO.getCode());
					ticketTransactionDao.updatePaymentTransactionId(connection, authDTO, paymentTransactionDTO);
				}
			}
			catch (Exception e) {
				connection.rollback();
				throw e;
			}
			finally {
				connection.commit();
				connection.setAutoCommit(true);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	@Override
	public List<PaymentTransactionDTO> verifyTicketTransaction(AuthDTO authDTO, List<PaymentTransactionDTO> paymentTransactionList) {
		TicketTransactionDAO ticketTransactionDAO = new TicketTransactionDAO();
		for (PaymentTransactionDTO paymentTransactionDTO : paymentTransactionList) {
			BigDecimal transactionAmount = paymentTransactionDTO.getTransactionAmount();
			TicketTransactionDTO ticketTransactionDTO = ticketTransactionDAO.getTicketTransaction(authDTO, paymentTransactionDTO);
			if (ticketTransactionDTO != null) {
				BigDecimal totalAmount = (ticketTransactionDTO.getTransactionAmount().subtract(ticketTransactionDTO.getAddonsAmount()));
				if (ticketTransactionDTO.getPaymentTrans().getId() == Numeric.ZERO_INT && totalAmount.compareTo(transactionAmount) != 0) {
					paymentTransactionDTO.setRemarks("MISMATCH");
				}
				else if (ticketTransactionDTO.getPaymentTrans().getId() == Numeric.ZERO_INT && totalAmount.compareTo(transactionAmount) == 0) {
					paymentTransactionDTO.setRemarks("YET");
				}
				else if (ticketTransactionDTO.getPaymentTrans().getId() != Numeric.ZERO_INT) {
					paymentTransactionDTO.setRemarks("PAID");
				}
			}
			else {
				paymentTransactionDTO.setRemarks("INVALID");
			}
		}
		return paymentTransactionList;
	}

	@Override
	public List<PaymentTransactionDTO> updateTicketTransaction(AuthDTO authDTO, List<PaymentTransactionDTO> paymentTransactionList) {
		List<TicketTransactionDTO> mismatchTransactionList = new ArrayList<TicketTransactionDTO>();
		List<PaymentTransactionDTO> transactionList = new ArrayList<PaymentTransactionDTO>();

		TicketTransactionDAO ticketTransactionDAO = new TicketTransactionDAO();
		for (PaymentTransactionDTO paymentTransactionDTO : paymentTransactionList) {
			TicketTransactionDTO ticketTransactionDTO = ticketTransactionDAO.getTicketTransaction(authDTO, paymentTransactionDTO);
			if (ticketTransactionDTO != null) {
				BigDecimal totalAmount = ticketTransactionDTO.getTransactionAmount();
				if (ticketTransactionDTO.getPaymentTrans().getId() == Numeric.ZERO_INT && totalAmount.compareTo(paymentTransactionDTO.getTransactionAmount()) == 0) {
					paymentTransactionDTO.setId(-2);
					paymentTransactionDTO.setRemarks("SUCCESS");
					transactionList.add(paymentTransactionDTO);
				}
				else if (ticketTransactionDTO.getPaymentTrans().getId() == Numeric.ZERO_INT && totalAmount.compareTo(paymentTransactionDTO.getTransactionAmount()) != 0) {
					ticketTransactionDTO.setRemarks("MISMATCH");
					paymentTransactionDTO.setRemarks("MISMATCH");
					mismatchTransactionList.add(ticketTransactionDTO);
				}
				else if (ticketTransactionDTO.getPaymentTrans().getId() != Numeric.ZERO_INT) {
					ticketTransactionDTO.setRemarks("PAID");
					paymentTransactionDTO.setRemarks("PAID");
					mismatchTransactionList.add(ticketTransactionDTO);
				}
			}
		}
		if (!transactionList.isEmpty()) {
			ticketTransactionDAO.updatePaymentTransactionId(authDTO, transactionList);
		}
		if (!mismatchTransactionList.isEmpty()) {
			emailService.sendMismatchTransactionEmail(authDTO, mismatchTransactionList);
		}
		return paymentTransactionList;
	}

	@Async
	public void sendPaymentNotification(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO, PaymentTransactionDTO repoPaymentTransaction) {
		if (paymentTransactionDTO.getTransactionType() == null && repoPaymentTransaction != null) {
			paymentTransactionDTO.setTransactionType(repoPaymentTransaction.getTransactionType());
		}
		if (paymentTransactionDTO.getUser() == null && repoPaymentTransaction != null) {
			paymentTransactionDTO.setUser(getUserDTOById(authDTO, repoPaymentTransaction.getUser()));
		}
		if (paymentTransactionDTO.getTransactionAmount() == null && repoPaymentTransaction != null) {
			paymentTransactionDTO.setTransactionAmount(repoPaymentTransaction.getTransactionAmount());
		}
		if (paymentTransactionDTO.getPaymentAcknowledge() == null || PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID.getId() == paymentTransactionDTO.getPaymentAcknowledge().getId()) {
			paymentTransactionDTO.setPaymentAcknowledge(paymentTransactionDTO.getPaymentAcknowledge() == null ? PaymentAcknowledgeEM.PAYMENT_INITIATED : repoPaymentTransaction.getPaymentAcknowledge());
		}
		if (paymentTransactionDTO.getUser() != null && paymentTransactionDTO.getUser().getMobileVerifiedFlag() == Numeric.ONE_INT) {
			smsService.sendPaymentSMS(authDTO, paymentTransactionDTO);
		}
	}

	public List<PaymentReceiptDTO> getPaymentReceipts(AuthDTO authDTO, UserDTO user, String fromDate, String toDate, PaymentAcknowledgeEM paymentAcknowledge, UserRoleEM userRole) {
		if (StringUtil.isNotNull(user.getCode())) {
			user = userService.getUser(authDTO, user);
		}
		SectorDTO sector = null;
		if (StringUtil.isNull(user.getCode())) {
			sector = sectorService.getActiveUserSectorOrganization(authDTO);
		}
		PaymentTransactionDAO transactionDAO = new PaymentTransactionDAO();
		List<PaymentReceiptDTO> paymentReceipts = transactionDAO.getPaymentReceipts(authDTO, user, fromDate, toDate, paymentAcknowledge);
		for (Iterator<PaymentReceiptDTO> iterator = paymentReceipts.iterator(); iterator.hasNext();) {
			PaymentReceiptDTO paymentReceiptDTO = iterator.next();

			paymentReceiptDTO.setUser(userService.getUser(authDTO, paymentReceiptDTO.getUser()));
			if (userRole != null && userRole.getId() != paymentReceiptDTO.getUser().getUserRole().getId()) {
				iterator.remove();
				continue;
			}
			// Apply Sector User filter
			if (sector != null && sector.getActiveFlag() == Numeric.ONE_INT && BitsUtil.isOrganizationExists(sector.getOrganization(), paymentReceiptDTO.getUser().getOrganization()) == null) {
				iterator.remove();
				continue;
			}

			userService.getUserV2(authDTO, paymentReceiptDTO.getUpdatedBy());

			if (paymentReceiptDTO.getAuditLog() != null) {
				for (AuditDTO auditDTO : paymentReceiptDTO.getAuditLog()) {
					userService.getUserV2(authDTO, auditDTO.getUser());
				}
			}

			for (PaymentTransactionDTO transactionDTO : paymentReceiptDTO.getPaymentTransactions()) {
				transactionDAO.getPaymentTransactionsWithPartialPayment(authDTO, transactionDTO);
				transactionDTO.setUser(userService.getUser(authDTO, transactionDTO.getUser()));
				transactionDTO.setPaymentHandledByUser(userService.getUser(authDTO, transactionDTO.getPaymentHandledByUser()));
				for (PaymentTransactionDTO partialPransactionDTO : transactionDTO.getPartialPaymentPaidList()) {
					partialPransactionDTO.setUser(userService.getUser(authDTO, partialPransactionDTO.getUser()));
					partialPransactionDTO.setPaymentHandledByUser(userService.getUser(authDTO, partialPransactionDTO.getPaymentHandledByUser()));
				}

				if (transactionDTO.getUserTransaction() != null && transactionDTO.getUserTransaction().getId() != 0) {
					userTransactionService.getUserTransaction(authDTO, transactionDTO.getUserTransaction());
				}
			}

			// Sorting
			Collections.sort(paymentReceiptDTO.getPaymentTransactions(), new Comparator<PaymentTransactionDTO>() {
				@Override
				public int compare(PaymentTransactionDTO t1, PaymentTransactionDTO t2) {
					return new CompareToBuilder().append(t2.getTransactionDate(), t1.getTransactionDate()).toComparison();
				}
			});

			PaymentTransactionDTO transactionDTO = Iterables.getLast(paymentReceiptDTO.getPaymentTransactions(), null);
			if (transactionDTO != null && transactionDTO.getUserTransaction() != null) {
				paymentReceiptDTO.setOpeningBalance(transactionDTO.getUserTransaction().getClosingBalanceAmount().subtract(transactionDTO.getUserTransaction().getTransactionAmount()));
				paymentReceiptDTO.setClosingBalance(transactionDTO.getUserTransaction().getClosingBalanceAmount());
			}
		}
		return paymentReceipts;
	}

	@Override
	public void savePaymentReceipt(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO) {
		try {
			paymentReceiptDTO.setUser(userService.getUser(authDTO, paymentReceiptDTO.getUser()));

			if (paymentReceiptDTO.getUser().getPaymentType().getId() != PaymentTypeEM.PAYMENT_POST_PAID.getId()) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED, "Only Post Paid Users Allowed!");
			}

			if (paymentReceiptDTO.getPaymentAcknowledge().getId() != PaymentAcknowledgeEM.PAYMENT_INITIATED.getId() && authDTO.getNamespace().getProfile().getPaymentReceiptAcknowledgeProcess() == 1) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED, "Payment approval process should be followed!");
			}
			if (StringUtil.isNull(paymentReceiptDTO.getCode())) {
				String code = TokenGenerator.generateCode("PRC");
				paymentReceiptDTO.setCode(code);
			}
			PaymentTransactionDAO transactionDAO = new PaymentTransactionDAO();
			transactionDAO.generatePaymentReceipt(authDTO, paymentReceiptDTO);

			if (paymentReceiptDTO.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED.getId() && authDTO.getNamespace().getProfile().getPaymentReceiptAcknowledgeProcess() == 0) {
				processReceiptsVoucherPaymentAcknowledge(authDTO, paymentReceiptDTO.getUser());
			}
		}
		catch (ServiceException e) {
			e.printStackTrace();
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	@Override
	public void updatePaymentReceipt(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO) {
		try {
			PaymentTransactionDAO transactionDAO = new PaymentTransactionDAO();
			convertRemarks(authDTO, paymentReceiptDTO);

			transactionDAO.updatePaymentReceiptV2(authDTO, paymentReceiptDTO);

			if (paymentReceiptDTO.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED.getId()) {
				transactionDAO.getPaymentReceipt(authDTO, paymentReceiptDTO);

				paymentReceiptDTO.setUser(userService.getUser(authDTO, paymentReceiptDTO.getUser()));

				processReceiptsVoucherPaymentAcknowledge(authDTO, paymentReceiptDTO.getUser());
			}
		}
		catch (ServiceException e) {
			e.printStackTrace();
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	private void convertRemarks(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO) {
		PaymentReceiptDTO repoPayment = new PaymentReceiptDTO();
		repoPayment.setCode(paymentReceiptDTO.getCode());

		PaymentTransactionDAO transactionDAO = new PaymentTransactionDAO();
		transactionDAO.getPaymentReceipt(authDTO, repoPayment);

		String remarks = Text.EMPTY;

		JSONObject audit = new JSONObject();
		audit.put("e", "Payment Status Changed " + repoPayment.getPaymentAcknowledge().getName() + " to " + paymentReceiptDTO.getPaymentAcknowledge().getName());
		audit.put("u", authDTO.getUser().getId());
		audit.put("t", DateUtil.convertDateTime(DateUtil.NOW()));

		if (StringUtil.isValidJSON(repoPayment.getRemarks())) {
			JSONObject remarksJSON = JSONObject.fromObject(repoPayment.getRemarks());
			remarksJSON.getJSONArray("a").add(audit);
			remarksJSON.put("r", remarksJSON.getString("r") + Text.COMMA + paymentReceiptDTO.getRemarks());
			remarks = remarksJSON.toString();
		}
		else {
			JSONObject remarksJSON = new JSONObject();
			JSONArray audits = new JSONArray();
			audits.add(audit);
			remarksJSON.put("r", repoPayment.getRemarks() + Text.COMMA + paymentReceiptDTO.getRemarks());
			remarksJSON.put("a", audits);

			remarks = remarksJSON.toString();
		}

		paymentReceiptDTO.setRemarks(remarks);
	}

	private List<PaymentReceiptDTO> processReceiptsVoucherPaymentAcknowledge(AuthDTO authDTO, UserDTO user) {
		List<PaymentReceiptDTO> savePaymentReceipts = new ArrayList<>();
		try {
			PaymentTransactionDAO transactionDAO = new PaymentTransactionDAO();

			List<PaymentReceiptDTO> repoPaymentReceipts = transactionDAO.getBalancePaymentReceipt(authDTO, user);

			List<PaymentTransactionDTO> paymentTransactionList = getUnAcknowledgePaymentTransaction(authDTO, null, user);

			List<PaymentTransactionDTO> paymentTransactions = new ArrayList<>();
			for (Iterator<PaymentTransactionDTO> iterator = paymentTransactionList.iterator(); iterator.hasNext();) {
				PaymentTransactionDTO paymentTransactionDTO = iterator.next();
				if (paymentTransactionDTO.getTransactionType().getId() != TransactionTypeEM.PAYMENT_VOUCHER.getId()) {
					iterator.remove();
					continue;
				}
				if (paymentTransactionDTO.getPaymentAcknowledge().getId() != PaymentAcknowledgeEM.PAYMENT_PAID.getId() && paymentTransactionDTO.getPaymentAcknowledge().getId() != PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID.getId()) {
					iterator.remove();
					continue;
				}
				if (paymentTransactionDTO.getUser().getId() != user.getId()) {
					iterator.remove();
					continue;
				}
				paymentTransactions.add(paymentTransactionDTO);
			}

			// Sorting
			Collections.sort(repoPaymentReceipts, new Comparator<PaymentReceiptDTO>() {
				@Override
				public int compare(PaymentReceiptDTO t1, PaymentReceiptDTO t2) {
					return new CompareToBuilder().append(t1.getTransactionDate(), t2.getTransactionDate()).toComparison();
				}
			});

			// Sorting
			Collections.sort(paymentTransactions, new Comparator<PaymentTransactionDTO>() {
				@Override
				public int compare(PaymentTransactionDTO t1, PaymentTransactionDTO t2) {
					return new CompareToBuilder().append(t1.getTransactionDate(), t2.getTransactionDate()).toComparison();
				}
			});

			BigDecimal transactionAmount = BigDecimal.ZERO;
			BigDecimal totalPaidPartialAmount = BigDecimal.ZERO;

			List<PaymentTransactionDTO> savePaymentTransactions = new ArrayList<>();
			for (PaymentTransactionDTO transactionDTO : paymentTransactions) {
				transactionDTO.setUser(userService.getUser(authDTO, transactionDTO.getUser()));

				totalPaidPartialAmount = BigDecimal.ZERO;
				List<PaymentTransactionDTO> partialPaymentTransactionList = transactionDAO.getPartialPaymentTransaction(authDTO, transactionDTO);
				for (PaymentTransactionDTO partialPaymentTransaction : partialPaymentTransactionList) {
					totalPaidPartialAmount = totalPaidPartialAmount.add(partialPaymentTransaction.getTransactionAmount());
				}

				transactionAmount = transactionDTO.getTransactionAmount().subtract(totalPaidPartialAmount);

				if (repoPaymentReceipts.isEmpty()) {
					break;
				}

				for (Iterator<PaymentReceiptDTO> iterator = repoPaymentReceipts.iterator(); iterator.hasNext();) {
					PaymentReceiptDTO paymentReceipt = iterator.next();

					if (transactionAmount.compareTo(BigDecimal.ZERO) < 0 && paymentReceipt.getPaymentReceiptType().getId() != PaymentReceiptTypeEM.PAYMENT.getId()) {
						continue;
					}
					if (paymentReceipt.getBalanceAmount().compareTo(BigDecimal.ZERO) == 0) {
						iterator.remove();
						continue;
					}

					if (transactionAmount.compareTo(BigDecimal.ZERO) < 0 && paymentReceipt.getPaymentReceiptType().getId() == PaymentReceiptTypeEM.PAYMENT.getId() && transactionAmount.abs().compareTo(paymentReceipt.getBalanceAmount()) <= 0) {
						transactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED);
						paymentReceipt.setBalanceAmount(paymentReceipt.getBalanceAmount().subtract(transactionAmount.abs()));
						transactionDTO.setTransactionAmount(transactionAmount);
						if (transactionDTO.getTransactionAmount().compareTo(BigDecimal.ZERO) == 0) {
							break;
						}
						paymentReceipt.getPaymentTransactions().add(transactionDTO);

						savePaymentTransactions.add(transactionDTO);
						savePaymentReceipts.add(paymentReceipt);
						break;
					}
					else if (transactionAmount.compareTo(BigDecimal.ZERO) < 0 && paymentReceipt.getPaymentReceiptType().getId() == PaymentReceiptTypeEM.PAYMENT.getId() && transactionAmount.abs().compareTo(paymentReceipt.getBalanceAmount()) >= 0) {
						PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
						BeanUtils.copyProperties(paymentTransactionDTO, transactionDTO);
						paymentTransactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID);
						paymentTransactionDTO.setTransactionAmount(paymentReceipt.getBalanceAmount().multiply(BigDecimal.valueOf(-1)));

						if (paymentTransactionDTO.getTransactionAmount().compareTo(BigDecimal.ZERO) == 0) {
							break;
						}

						transactionAmount = transactionAmount.abs().subtract(paymentReceipt.getBalanceAmount());

						paymentReceipt.setBalanceAmount(BigDecimal.ZERO);
						paymentReceipt.getPaymentTransactions().add(transactionDTO);

						savePaymentTransactions.add(paymentTransactionDTO);
						savePaymentReceipts.add(paymentReceipt);
					}
					else if (paymentReceipt.getPaymentReceiptType().getId() == PaymentReceiptTypeEM.COLLECTION.getId() && transactionAmount.compareTo(paymentReceipt.getBalanceAmount()) < 0) {
						transactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED);
						paymentReceipt.setBalanceAmount(paymentReceipt.getBalanceAmount().subtract(transactionAmount));
						transactionDTO.setTransactionAmount(transactionAmount);
						if (transactionDTO.getTransactionAmount().compareTo(BigDecimal.ZERO) == 0) {
							break;
						}
						paymentReceipt.getPaymentTransactions().add(transactionDTO);

						savePaymentTransactions.add(transactionDTO);
						savePaymentReceipts.add(paymentReceipt);
						break;
					}
					else if (paymentReceipt.getPaymentReceiptType().getId() == PaymentReceiptTypeEM.COLLECTION.getId() && transactionAmount.compareTo(paymentReceipt.getBalanceAmount()) > 0) {
						PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
						BeanUtils.copyProperties(paymentTransactionDTO, transactionDTO);
						paymentTransactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID);
						paymentTransactionDTO.setTransactionAmount(paymentReceipt.getBalanceAmount());

						if (paymentTransactionDTO.getTransactionAmount().compareTo(BigDecimal.ZERO) == 0) {
							break;
						}

						transactionAmount = transactionAmount.subtract(paymentReceipt.getBalanceAmount());

						paymentReceipt.setBalanceAmount(BigDecimal.ZERO);
						paymentReceipt.getPaymentTransactions().add(transactionDTO);

						savePaymentTransactions.add(paymentTransactionDTO);
						savePaymentReceipts.add(paymentReceipt);
					}
					else {
						iterator.remove();
						continue;
					}
				}
			}

			for (PaymentTransactionDTO paymentTransactionDTO : savePaymentTransactions) {
				acknowledgePaymentVoucherTransaction(authDTO, paymentTransactionDTO);
			}
			for (PaymentReceiptDTO paymentReceipt : savePaymentReceipts) {
				transactionDAO.updatePaymentReceipt(authDTO, paymentReceipt);
			}
		}
		catch (ServiceException e) {
			e.printStackTrace();
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return savePaymentReceipts;
	}

	public void acknowledgePaymentVoucherTransaction(AuthDTO authDTO, PaymentTransactionDTO userPayment) {
		PaymentTransactionDTO repoPayment = new PaymentTransactionDTO();
		repoPayment.setCode(userPayment.getCode());
		PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
		paymentTransactionDAO.getPaymentTransaction(authDTO, repoPayment);

		repoPayment.setUser(getUserDTOById(authDTO, repoPayment.getUser()));
		if (repoPayment.getId() == 0 || repoPayment.getLookupId() != 0) {
			throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID);
		}
		if (repoPayment.getUser().getUserRole().getId() != UserRoleEM.USER_ROLE.getId()) {
			throw new ServiceException(ErrorCode.MISMATCH_ROLE);
		}
		if (userPayment.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED.getId() || userPayment.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID.getId()) {
			if (repoPayment.getPaymentAcknowledge().getId() != PaymentAcknowledgeEM.PAYMENT_PAID.getId() && repoPayment.getPaymentAcknowledge().getId() != PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID.getId()) {
				throw new ServiceException(ErrorCode.TRANSACTION_INVALID_ACK);
			}
			if (userPayment.getTransactionAmount().compareTo(repoPayment.getTransactionAmount()) == 1) {
				throw new ServiceException(ErrorCode.MISMATCH_IN_TRANSACTION_AMOUNT);
			}
			if (userPayment.getTransactionAmount().compareTo(repoPayment.getTransactionAmount()) == 0 && repoPayment.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID.getId()) {
				throw new ServiceException(ErrorCode.MISMATCH_IN_TRANSACTION_AMOUNT);
			}
			UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
			userTransactionDTO.setRefferenceId(repoPayment.getId());
			userTransactionDTO.setRefferenceCode(repoPayment.getCode());
			repoPayment.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED);
			repoPayment.setRemarks(userPayment.getRemarks());

			if (userPayment.getTransactionAmount().compareTo(repoPayment.getTransactionAmount()) == -1) {
				PaymentTransactionDTO paymentPartialTransaction = acknowledgePaymentVoucherPartialTransaction(authDTO, repoPayment, userPayment);
				userTransactionDTO.setRefferenceId(paymentPartialTransaction.getId());
				userTransactionDTO.setRefferenceCode(paymentPartialTransaction.getCode());
				repoPayment.setPaymentAcknowledge(paymentPartialTransaction.getPaymentAcknowledge());
			}

			// Update User Balance
			userTransactionDTO.setTransactionAmount(userPayment.getTransactionAmount());
			userTransactionDTO.setCreditAmount(userPayment.getTransactionAmount());
			userTransactionDTO.setDebitAmount(BigDecimal.ZERO);
			userTransactionDTO.setTdsTax(BigDecimal.ZERO);
			userTransactionDTO.setTransactionMode(repoPayment.getTransactionMode());
			userTransactionDTO.setTransactionType(repoPayment.getTransactionType());
			userTransactionService.SaveUserTransaction(authDTO, repoPayment.getUser(), userTransactionDTO);

			repoPayment.setUserTransaction(userTransactionDTO);
			userPayment.setUserTransaction(userTransactionDTO);
			paymentTransactionDAO.getAcknowledgedPaymentTransactionUpdate(authDTO, repoPayment);
		}
		else if (userPayment.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PAYMENT_REJECT.getId()) {
			List<PaymentTransactionDTO> partialPaymentTransactionList = paymentTransactionDAO.getPartialPaymentTransaction(authDTO, repoPayment);
			BigDecimal totalTransactionAmount = BigDecimal.ZERO;
			for (PaymentTransactionDTO paymentTransaction : partialPaymentTransactionList) {
				totalTransactionAmount = totalTransactionAmount.add((paymentTransaction.getTransactionAmount()));
			}

			if (!partialPaymentTransactionList.isEmpty() && totalTransactionAmount.compareTo(repoPayment.getTransactionAmount()) == -1) {
				throw new ServiceException(ErrorCode.TRANSACTION_ALREADY_INITIATED);
			}

			userPayment.setId(repoPayment.getId());
			userPayment.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_REJECT);
			userPayment.setRemarks(repoPayment.getRemarks());
			paymentTransactionDAO.getAcknowledgedPaymentTransactionUpdate(authDTO, userPayment);
			// Clear related cargo transaction
			if (userPayment.getTransactionType().getId() == TransactionTypeEM.PAYMENT_VOUCHER.getId()) {
				clearGeneratePaymentVoucher(authDTO, userPayment);
			}
		}
		else if (userPayment.getPaymentAcknowledge().getId() == PaymentAcknowledgeEM.PAYMENT_PAID.getId()) {
			if (repoPayment.getPaymentAcknowledge().getId() != PaymentAcknowledgeEM.PAYMENT_INITIATED.getId()) {
				throw new ServiceException(ErrorCode.TRANSACTION_INVALID_ACK);
			}
			repoPayment.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_PAID);
			repoPayment.setRemarks(repoPayment.getRemarks() + Text.SINGLE_SPACE + userPayment.getRemarks());
			paymentTransactionDAO.getAcknowledgedPaymentTransactionUpdate(authDTO, repoPayment);

			processReceiptsVoucherPaymentAcknowledge(authDTO, repoPayment.getUser());
		}
	}

	@Override
	public void getPaymentReceipt(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO) {
		PaymentTransactionDAO paymentReceiptDAO = new PaymentTransactionDAO();
		paymentReceiptDAO.getPaymentReceipt(authDTO, paymentReceiptDTO);
	}

	private void processUserTransaction(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO) {
		getPaymentReceipt(authDTO, paymentReceiptDTO);
		getUserDTO(authDTO, paymentReceiptDTO.getUser());
		UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
		userTransactionDTO.setRefferenceId(paymentReceiptDTO.getId());
		userTransactionDTO.setRefferenceCode(paymentReceiptDTO.getCode());
		userTransactionDTO.setTransactionAmount(paymentReceiptDTO.getTransactionAmount());
		if (paymentReceiptDTO.getPaymentReceiptType().getId() == PaymentReceiptTypeEM.COLLECTION.getId()) {
			userTransactionDTO.setCreditAmount(paymentReceiptDTO.getTransactionAmount());
			userTransactionDTO.setDebitAmount(BigDecimal.ZERO);
		}
		else if (paymentReceiptDTO.getPaymentReceiptType().getId() == PaymentReceiptTypeEM.PAYMENT.getId()) {
			userTransactionDTO.setCreditAmount(BigDecimal.ZERO);
			userTransactionDTO.setDebitAmount(paymentReceiptDTO.getTransactionAmount());
		}
		userTransactionDTO.setTdsTax(BigDecimal.ZERO);
		userTransactionDTO.setTransactionMode(paymentReceiptDTO.getTransactionMode());
		userTransactionDTO.setTransactionType(TransactionTypeEM.PAYMENT_RECEIPT);
		userTransactionService.SaveUserTransaction(authDTO, paymentReceiptDTO.getUser(), userTransactionDTO);
	}

	public List<PaymentTransactionDTO> getUnAcknowledgePaymentTransaction(AuthDTO authDTO, OrganizationDTO organizationDTO, UserDTO userDTO) {
		if (organizationDTO != null) {
			organizationDTO = getOrganizationByCode(authDTO, organizationDTO);
		}
		if (userDTO != null) {
			userDTO = userService.getUser(authDTO, userDTO);
		}
		PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
		List<PaymentTransactionDTO> paymentTransactionList = paymentTransactionDAO.getALLUnAcknowledgedPaymentTransaction(authDTO, organizationDTO, userDTO);
		for (PaymentTransactionDTO transactionDTO : paymentTransactionList) {
			UserDTO transactionUserDTO = getUserDTOById(authDTO, transactionDTO.getUser());
			transactionUserDTO.setOrganization(getOrganizationDTObyId(authDTO, transactionUserDTO.getOrganization()));
			transactionDTO.setPaymentHandledByUser(getUserDTOById(authDTO, transactionDTO.getPaymentHandledByUser()));

			for (PaymentTransactionDTO partialPaymentDTO : transactionDTO.getPartialPaymentPaidList()) {
				partialPaymentDTO.setUser(getUserDTOById(authDTO, partialPaymentDTO.getUser()));
				partialPaymentDTO.setPaymentHandledByUser(getUserDTOById(authDTO, partialPaymentDTO.getPaymentHandledByUser()));
			}
		}
		return paymentTransactionList;
	}

	private PaymentTransactionDTO acknowledgePaymentVoucherPartialTransaction(AuthDTO authDTO, PaymentTransactionDTO repoPayment, PaymentTransactionDTO userPayment) {
		PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
		List<PaymentTransactionDTO> partialPaymentTransactionList = paymentTransactionDAO.getPartialPaymentTransaction(authDTO, repoPayment);

		BigDecimal totalTransactionAmount = BigDecimal.ZERO;
		for (PaymentTransactionDTO paymentTransaction : partialPaymentTransactionList) {
			totalTransactionAmount = totalTransactionAmount.add((paymentTransaction.getTransactionAmount()));
		}

		PaymentTransactionDTO paymentPartialTransaction = new PaymentTransactionDTO();
		paymentPartialTransaction.setCode(paymentPartialTransaction.getPaymentTransactionCode());
		paymentPartialTransaction.setUser(repoPayment.getUser());
		paymentPartialTransaction.setTransactionAmount(userPayment.getTransactionAmount());
		paymentPartialTransaction.setPaymentHandledByUser(authDTO.getUser());
		paymentPartialTransaction.setTransactionType(repoPayment.getTransactionType());
		paymentPartialTransaction.setTransactionMode(userPayment.getTransactionMode());
		paymentPartialTransaction.setAmountReceivedDate(userPayment.getAmountReceivedDate());
		paymentPartialTransaction.setRemarks(userPayment.getRemarks());

		if (totalTransactionAmount.add(userPayment.getTransactionAmount()).compareTo(repoPayment.getTransactionAmount()) == 1) {
			throw new ServiceException(ErrorCode.MISMATCH_IN_TRANSACTION_AMOUNT);
		}
		else if (totalTransactionAmount.add(userPayment.getTransactionAmount()).compareTo(repoPayment.getTransactionAmount()) == -1) {
			paymentPartialTransaction.setPaymentAcknowledge(PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID);
		}
		else if (totalTransactionAmount.add(userPayment.getTransactionAmount()).compareTo(repoPayment.getTransactionAmount()) == 0) {
			paymentPartialTransaction.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED);
		}
		paymentPartialTransaction.setLookupId(repoPayment.getId());

		UserTransactionDAO dao = new UserTransactionDAO();
		dao.generatePaymentVoucher(authDTO, paymentPartialTransaction);
		return paymentPartialTransaction;
	}

	@Override
	public void updatePaymentReceiptImageDetails(AuthDTO authDTO, String referenceCode, String imageDetailsIds) {
		PaymentTransactionDAO paymentReceiptDAO = new PaymentTransactionDAO();
		paymentReceiptDAO.updatePaymentReceiptImageDetails(authDTO, referenceCode, imageDetailsIds);
	}

}

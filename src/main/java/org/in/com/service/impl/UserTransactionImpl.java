package org.in.com.service.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLTransactionRollbackException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.slack.SlackService;
import org.in.com.cache.CacheCentral;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ConnectDAO;
import org.in.com.dao.TicketTransactionDAO;
import org.in.com.dao.UserTransactionDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.PaymentVoucherDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.PaymentAcknowledgeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.GroupService;
import org.in.com.service.PaymentTransactionService;
import org.in.com.service.SearchService;
import org.in.com.service.SectorService;
import org.in.com.service.StationPointService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.service.UserTransactionService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import lombok.Cleanup;

@Service
public class UserTransactionImpl extends CacheCentral implements UserTransactionService {
	@Autowired
	PaymentTransactionService paymentTransactionService;
	@Autowired
	SearchService searchService;
	@Autowired
	TripService tripService;
	@Autowired
	StationPointService stationPointService;
	@Autowired
	SlackService slack;
	@Autowired
	GroupService groupService;
	@Autowired
	UserService userService;
	@Autowired
	EmailService emailService;
	@Autowired
	SectorService sectorService;

	public void SaveUserTransaction(Connection connection, AuthDTO authDTO, UserDTO userDTO, UserTransactionDTO userTransactionDTO) {
		// Balance update only for User
		if (userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && userTransactionDTO != null) {
			UserTransactionDAO.SaveUserTransaction(connection, authDTO, userDTO, userTransactionDTO);
		}
	}

	public UserTransactionDTO getTransactionHistory(AuthDTO authDTO, UserDTO userDTO, DateTime fromDate, DateTime toDate) {
		UserTransactionDAO dao = new UserTransactionDAO();
		UserTransactionDTO userTransactionDTO = dao.getTransactionHistory(authDTO, userDTO, fromDate, toDate);
		for (UserTransactionDTO transactionDTO : userTransactionDTO.getList()) {
			transactionDTO.getUser().setGroup(groupService.getGroup(authDTO, transactionDTO.getUser().getGroup()));
		}
		return userTransactionDTO;
	}

	@Override
	public List<PaymentVoucherDTO> getPaymentVoucherUnPaid(AuthDTO authDTO, String userCode, DateTime fromDate, DateTime toDate, Boolean useTravelDate, String schedule) {
		UserTransactionDAO dao = new UserTransactionDAO();
		String organizationIds = Text.NA;
		if (StringUtil.isNull(userCode)) {
			SectorDTO sector = sectorService.getActiveUserSectorOrganization(authDTO);
			if (sector.getActiveFlag() == Numeric.ONE_INT) {
				organizationIds = sector.getOrganizationIds();
			}
		}
		List<PaymentVoucherDTO> pvList = dao.getPaymentVoucherUnPaid(authDTO, userCode, fromDate, toDate, useTravelDate, schedule, organizationIds);
		return pvList;
	}

	@Override
	public List<PaymentVoucherDTO> getGeneratedPaymentVoucherDetails(AuthDTO authDTO, String paymentCode) {
		UserTransactionDAO dao = new UserTransactionDAO();
		List<PaymentVoucherDTO> pvList = dao.getGeneratedPaymentVoucherDetails(authDTO, paymentCode);
		return pvList;
	}

	@Override
	public PaymentTransactionDTO generatePaymentVoucher(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO, String ticketTransactionCode) {

		TicketTransactionDAO transactionDAO = new TicketTransactionDAO();
		String code = paymentTransactionDTO.getPaymentTransactionCode();
		paymentTransactionDTO.setCode(code);
		BigDecimal totalCreditAmount = BigDecimal.ZERO;
		BigDecimal totalAcBusTaxAmount = BigDecimal.ZERO;
		BigDecimal totalCommissionAmount = BigDecimal.ZERO;
		UserDTO transactionUserDTO = getUserDTO(authDTO, paymentTransactionDTO.getUser());

		for (String transactionCode : ticketTransactionCode.split(Text.COMMA)) {
			if (StringUtil.isNull(transactionCode)) {
				continue;
			}
			TicketTransactionDTO transactionDTO = transactionDAO.getTicketTransactionDetails(authDTO, transactionCode, transactionUserDTO);
			if (transactionDTO == null || transactionDTO.getPaymentTrans().getId() != 0) {
				throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID);
			}
			if (transactionDTO.getTransactionType().getId() == TransactionTypeEM.TICKETS_BOOKING.getId()) {
				totalCreditAmount = totalCreditAmount.add((transactionDTO.getTransactionAmount()).subtract(transactionDTO.getCommissionAmount()));
				totalAcBusTaxAmount = totalAcBusTaxAmount.add(transactionDTO.getAcBusTax());
				totalCommissionAmount = totalCommissionAmount.add(transactionDTO.getCommissionAmount());
			}
			else if (transactionDTO.getTransactionType().getId() == TransactionTypeEM.TICKETS_CANCEL.getId()) {
				transactionDAO.getTicketCancelTransactionDetails(authDTO, transactionDTO, transactionUserDTO);
				totalCreditAmount = totalCreditAmount.subtract(transactionDTO.getRefundAmount()).add(transactionDTO.getCancellationChargeCommissionAmount());
				totalAcBusTaxAmount = totalAcBusTaxAmount.subtract(transactionDTO.getAcBusTax());
				totalCommissionAmount = totalCommissionAmount.subtract(transactionDTO.getCancellationChargeAmount());
			}
		}
		paymentTransactionDTO.setTransactionAmount(totalCreditAmount);
		paymentTransactionDTO.setAcBusTax(totalAcBusTaxAmount);
		paymentTransactionDTO.setCommissionAmount(totalCommissionAmount);
		paymentTransactionDTO.setPaymentHandledByUser(getUserDTO(authDTO, paymentTransactionDTO.getPaymentHandledByUser()));
		// Save Generated Voucher
		SaveGeneratePaymentVoucher(authDTO, paymentTransactionDTO, ticketTransactionCode);
		paymentTransactionService.sendPaymentNotification(authDTO, paymentTransactionDTO, null);
		return paymentTransactionDTO;
	}

	@Override
	public PaymentTransactionDTO generatePaymentVoucherV2(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		SaveGeneratePaymentVoucher(authDTO, paymentTransactionDTO, Text.EMPTY);
		return paymentTransactionDTO;
	}

	@Override
	public void SaveUserTransaction(AuthDTO authDTO, UserDTO userDTO, UserTransactionDTO userTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			SaveUserTransaction(connection, authDTO, userDTO, userTransactionDTO);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

	}

	private void SaveGeneratePaymentVoucher(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO, String ticketTransactionCode) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				UserTransactionDAO dao = new UserTransactionDAO();
				connection.setAutoCommit(false);
				dao.generatePaymentVoucher(connection, authDTO, paymentTransactionDTO);
				TicketTransactionDAO ticketTransactionDao = new TicketTransactionDAO();
				for (String transactionCode : ticketTransactionCode.split(Text.COMMA)) {
					paymentTransactionDTO.setTransactionCode(transactionCode);
					ticketTransactionDao.updatePaymentTransactionId(connection, authDTO, paymentTransactionDTO);
				}
			}
			catch (SQLTransactionRollbackException e) {
				slack.sendAlert(authDTO, paymentTransactionDTO.getCode() + " DL01- " + " -  Deadlock found when trying to get lock; try restarting transaction");

				e.printStackTrace();
				connection.rollback();
				throw e;
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
	public void updateUserBalance(AuthDTO authDTO, List<String> paymentCodes) {
		UserTransactionDAO userTransactionDAO = new UserTransactionDAO();
		for (String paymentCode : paymentCodes) {
			UserDTO userDTO = new UserDTO();
			List<PaymentVoucherDTO> list = userTransactionDAO.getGeneratedPaymentVoucherDetails(authDTO, paymentCode);

			BigDecimal bookingAmount = BigDecimal.ZERO;
			BigDecimal cancellationAmount = BigDecimal.ZERO;
			BigDecimal creditAmount = BigDecimal.ZERO;
			BigDecimal cancellationCharge = BigDecimal.ZERO;
			// Overall Booking and Cancellation
			for (PaymentVoucherDTO paymentVoucher : list) {
				if (paymentVoucher.getTransactionType().getId() == TransactionTypeEM.TICKETS_BOOKING.getId()) {
					bookingAmount = bookingAmount.add(paymentVoucher.getNetAmount());
				}
				else if (paymentVoucher.getTransactionType().getId() == TransactionTypeEM.TICKETS_CANCEL.getId()) {
					creditAmount = creditAmount.add(paymentVoucher.getTicketAmount().subtract(paymentVoucher.getRefundAmount()));
					cancellationCharge = cancellationCharge.add(paymentVoucher.getCancellationChargeAmount());
				}
				userDTO.setCode(paymentVoucher.getUser().getCode());
			}
			cancellationAmount = creditAmount.add(creditAmount);

			PaymentTransactionDTO transactionDTO = new PaymentTransactionDTO();
			transactionDTO.setAmountReceivedDate(DateUtil.NOW().format("YYYY-MM-DD"));
			transactionDTO.setRemarks("");
			transactionDTO.setTransactionAmount(bookingAmount.subtract(cancellationAmount));
			transactionDTO.setTransactionMode(TransactionModeEM.PAYMENT_CASH);
			transactionDTO.setTransactionType(TransactionTypeEM.RECHARGE);
			transactionDTO.setUser(userDTO);
			transactionDTO.setCommissionAmount(BigDecimal.ZERO);
			transactionDTO.setAcBusTax(BigDecimal.ZERO);
			transactionDTO.setTdsTax(BigDecimal.ZERO);
			transactionDTO.setPaymentHandledByUser(authDTO.getUser());
			paymentTransactionService.rechargeTransaction(authDTO, transactionDTO);
			// Update Payment status as Paid
			if (transactionDTO.getId() > 0) {
				transactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_PAID);

				// Update User Balance
				UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
				userTransactionDTO.setTransactionAmount(transactionDTO.getTransactionAmount());
				// Credit
				userTransactionDTO.setCreditAmount(transactionDTO.getTransactionAmount());
				userTransactionDTO.setDebitAmount(BigDecimal.ZERO);
				userTransactionDTO.setTdsTax(BigDecimal.ZERO);
				userTransactionDTO.setTransactionMode(transactionDTO.getTransactionMode());
				userTransactionDTO.setTransactionType(transactionDTO.getTransactionType());
				userTransactionDTO.setRefferenceCode(transactionDTO.getCode());
				userTransactionDTO.setRefferenceId(transactionDTO.getId());
				UserTransactionImpl userTransactionImpl = new UserTransactionImpl();
				userTransactionImpl.SaveUserTransaction(authDTO, transactionDTO.getUser(), userTransactionDTO);
				// Update Payment Transaction to Paid
				if (userTransactionDTO.getId() > 0) {
					transactionDTO.setUserTransaction(userTransactionDTO);
					// paymentTransactionService.getAcknowledgedPaymentTransactionUpdate(authDTO,
					// transactionDTO);
				}
			}
		}

	}

	@Override
	public void creditUserBoardingCommission(AuthDTO authDTO, SearchDTO searchDTO) {
		// List of Trips
		List<TripDTO> tripList = searchService.getAllTrips(authDTO, searchDTO);

		for (TripDTO tripDTO : tripList) {

			Map<String, List<TripChartDetailsDTO>> userTicketDetailsMap = new HashMap<String, List<TripChartDetailsDTO>>();
			// Trip wise Ticket
			TripChartDTO tripChartDTO = tripService.getTripChart(authDTO, tripDTO);

			if (tripChartDTO == null || tripChartDTO.getTicketDetailsList().isEmpty()) {
				continue;
			}

			for (TripChartDetailsDTO ticketDetails : tripChartDTO.getTicketDetailsList()) {
				if (ticketDetails.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetails.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					ticketDetails.setBoardingPoint(getStationPointDTObyId(authDTO, ticketDetails.getBoardingPoint()));
					ticketDetails.setUser(getUserDTOById(authDTO, ticketDetails.getUser()));
					ticketDetails.getUser().setGroup(groupService.getGroup(authDTO, ticketDetails.getUser().getGroup()));

					// User wise Ticket
					if (userTicketDetailsMap.get(ticketDetails.getUser().getCode()) != null) {
						List<TripChartDetailsDTO> ticketSeats = userTicketDetailsMap.get(ticketDetails.getUser().getCode());
						ticketSeats.add(ticketDetails);
						userTicketDetailsMap.put(ticketDetails.getUser().getCode(), ticketSeats);
					}
					else {
						List<TripChartDetailsDTO> ticketSeats = new ArrayList<TripChartDetailsDTO>();
						ticketSeats.add(ticketDetails);
						userTicketDetailsMap.put(ticketDetails.getUser().getCode(), ticketSeats);
					}
				}
			}

			// User boarding commission
			Map<String, UserTransactionDTO> userTransactionMap = new HashMap<String, UserTransactionDTO>();
			for (Entry<String, List<TripChartDetailsDTO>> userStationPointMapEntry : userTicketDetailsMap.entrySet()) {
				BigDecimal totalBoardingCommissionAmount = BigDecimal.ZERO;

				String key = userStationPointMapEntry.getKey();
				List<TripChartDetailsDTO> chartDetailsList = userStationPointMapEntry.getValue();

				UserDTO userDTO = new UserDTO();
				userDTO.setCode(key);
				userDTO = getUserDTO(authDTO, userDTO);
				GroupDTO groupDTO = groupService.getGroup(authDTO, userDTO.getGroup());
				userDTO.setGroup(groupDTO);

				// User wise Station Points
				Map<String, Map<String, StationPointDTO>> usertationPointMap = stationPointService.getUserSpecificStationPointV2(authDTO, userDTO, new StationDTO());
				if (usertationPointMap.isEmpty()) {
					continue;
				}

				// Calculation
				for (Iterator<TripChartDetailsDTO> iterator = chartDetailsList.iterator(); iterator.hasNext();) {
					TripChartDetailsDTO tripChartDetailsDTO = iterator.next();
					
					Map<String, StationPointDTO> dataMap = (Map<String, StationPointDTO>) usertationPointMap.get(tripChartDetailsDTO.getBoardingPoint().getCode());
					if (dataMap == null) {
						continue;
					}
					StationPointDTO stationPoint = null;
					if (dataMap.get(tripChartDetailsDTO.getUser().getGroup().getCode()) != null) {
						stationPoint = dataMap.get(tripChartDetailsDTO.getUser().getGroup().getCode());
					} 
					else if (dataMap.get("ALL") != null) {
						stationPoint = dataMap.get("ALL");
					}
					if (stationPoint == null) {
						continue;
					}
					if (tripChartDetailsDTO.getFromStation().getId() == stationPoint.getStation().getId() && tripChartDetailsDTO.getBoardingPoint().getId() == stationPoint.getId()) {
						totalBoardingCommissionAmount = totalBoardingCommissionAmount.add(stationPoint.getBoardingCommission());
					}
				}
				// User Transaction
				if (totalBoardingCommissionAmount.compareTo(BigDecimal.ZERO) == 1) {
					if (userTransactionMap.get(userDTO.getCode()) != null) {
						UserTransactionDTO userTransaction = userTransactionMap.get(userDTO.getCode());
						BigDecimal amount = userTransaction.getTransactionAmount();
						userTransaction.setUser(userDTO);
						userTransaction.setTransactionAmount(totalBoardingCommissionAmount.add(amount));
						userTransactionMap.put(userDTO.getCode(), userTransaction);
					}
					else {
						UserTransactionDTO userTransaction = new UserTransactionDTO();
						userTransaction.setUser(userDTO);
						userTransaction.setTransactionAmount(totalBoardingCommissionAmount);
						userTransactionMap.put(userDTO.getCode(), userTransaction);
					}
				}
			}

			// Update User Transaction
			List<UserTransactionDTO> userTransactionList = new ArrayList<UserTransactionDTO>(userTransactionMap.values());
			for (UserTransactionDTO userTransactionDTO : userTransactionList) {
				PaymentTransactionDTO transactionDTO = new PaymentTransactionDTO();
				transactionDTO.setAmountReceivedDate(DateUtil.NOW().format("YYYY-MM-DD"));
				transactionDTO.setRemarks("User Boarding Point Commission");
				transactionDTO.setTransactionAmount(userTransactionDTO.getTransactionAmount());
				transactionDTO.setTransactionMode(TransactionModeEM.PAYMENT_CASH);
				transactionDTO.setTransactionType(TransactionTypeEM.RECHARGE);
				transactionDTO.setUser(userTransactionDTO.getUser());
				transactionDTO.setCommissionAmount(BigDecimal.ZERO);
				transactionDTO.setAcBusTax(BigDecimal.ZERO);
				transactionDTO.setTdsTax(BigDecimal.ZERO);
				transactionDTO.setPaymentHandledByUser(authDTO.getUser());
				paymentTransactionService.rechargeTransaction(authDTO, transactionDTO);
			}
		}
	}

	@Override
	public List<UserTransactionDTO> validateUserBalanceMismatch(AuthDTO authDTO, DateTime fromDate, DateTime toDate) {
		UserTransactionDAO transactionDAO = new UserTransactionDAO();
		Map<Integer, List<UserTransactionDTO>> transactionMap = transactionDAO.getUserTransaction(authDTO, fromDate, toDate);

		List<UserTransactionDTO> transactions = new ArrayList<UserTransactionDTO>();
		for (Entry<Integer, List<UserTransactionDTO>> entry : transactionMap.entrySet()) {
			List<UserTransactionDTO> transactionList = entry.getValue();

			Comparator<UserTransactionDTO> comp = new BeanComparator("id");
			Collections.sort(transactionList, comp);

			UserTransactionDTO transaction = Iterables.getFirst(transactionList, null);
			BigDecimal openingBalance = transaction.getClosingBalanceAmount();

			for (UserTransactionDTO transactionDTO : transactionList) {
				if (transaction.getId() == transactionDTO.getId()) {
					continue;
				}

				openingBalance = openingBalance.add(transactionDTO.getTransactionAmount());
				if (openingBalance.intValue() == transactionDTO.getClosingBalanceAmount().intValue() || Math.abs(openingBalance.subtract(transactionDTO.getClosingBalanceAmount()).intValue()) <= 4) {
					continue;
				}
				transactions.add(transactionDTO);
				openingBalance = transactionDTO.getClosingBalanceAmount();
				break;
			}
		}

		// send E-mail
		if (!transactions.isEmpty()) {
			emailService.sendTransactionEmail(authDTO, transactions);
		}
		return transactions;
	}

	@Override
	public void updateUserBalanceMismatch(AuthDTO authDTO, UserTransactionDTO userTransactionDTO) {
		userTransactionDTO.setUser(userService.getUser(authDTO, userTransactionDTO.getUser()));

		UserTransactionDAO transactionDAO = new UserTransactionDAO();
		List<UserTransactionDTO> transactionList = transactionDAO.getUserTransactions(authDTO, userTransactionDTO);

		// Sorting
		Comparator<UserTransactionDTO> comp = new BeanComparator("id");
		Collections.sort(transactionList, comp);

		UserTransactionDTO transaction = Iterables.getFirst(transactionList, null);
		BigDecimal openingBalance = transaction.getClosingBalanceAmount();

		List<UserTransactionDTO> mismatchList = new ArrayList<UserTransactionDTO>();
		for (UserTransactionDTO transactionDTO : transactionList) {
			if (transaction.getId() == transactionDTO.getId()) {
				continue;
			}
			openingBalance = openingBalance.add(transactionDTO.getTransactionAmount());
			if (openingBalance.compareTo(transactionDTO.getClosingBalanceAmount()) == 0) {
				continue;
			}
			transactionDTO.setClosingBalanceAmount(openingBalance);
			openingBalance = transactionDTO.getClosingBalanceAmount();
			mismatchList.add(transactionDTO);
		}

		// Update Balance
		if (!mismatchList.isEmpty()) {
			transactionDAO.updateClosingBalance(authDTO, mismatchList);
		}
	}

	@Override
	public void getUserTransaction(AuthDTO authDTO, UserTransactionDTO userTransactionDTO) {
		UserTransactionDAO dao = new UserTransactionDAO();
		dao.getUserTransaction(authDTO, userTransactionDTO);
	}
}

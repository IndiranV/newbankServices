package org.in.com.controller.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.WordUtils;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Numeric;
import org.in.com.controller.commerce.io.PaymentModeIO;
import org.in.com.controller.web.io.AcknowledgeStatusIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.OrganizationIO;
import org.in.com.controller.web.io.PaymentGatewayPartnerIO;
import org.in.com.controller.web.io.PaymentTransactionIO;
import org.in.com.controller.web.io.PaymentTypeIO;
import org.in.com.controller.web.io.PaymentVoucherIO;
import org.in.com.controller.web.io.RechargeOrderIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.TransactionModeIO;
import org.in.com.controller.web.io.TransactionTypeIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.controller.web.io.UserTransactionIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.OrderInitRequestDTO;
import org.in.com.dto.OrderInitStatusDTO;
import org.in.com.dto.PaymentGatewayPartnerDTO;
import org.in.com.dto.PaymentGatewayScheduleDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.PaymentVoucherDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentAcknowledgeEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.MenuService;
import org.in.com.service.PaymentMerchantGatewayScheduleService;
import org.in.com.service.PaymentTransactionService;
import org.in.com.service.PendingOrderService;
import org.in.com.service.UserService;
import org.in.com.service.UserTransactionService;
import org.in.com.service.pg.PaymentRequestService;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/user/transaction")
public class UserTransactionController extends BaseController {
	public static Map<String, Integer> ConcurrentRequests = new ConcurrentHashMap<String, Integer>();

	@Autowired
	UserService userService;
	@Autowired
	UserTransactionService transactionService;
	@Autowired
	PendingOrderService pendingOrderService;
	@Autowired
	PaymentTransactionService paymentTransactionService;
	@Autowired
	PaymentRequestService paymentRequestService;
	@Autowired
	PaymentMerchantGatewayScheduleService gatewayScheduleService;
	@Autowired
	MenuService menuService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<UserTransactionIO>> getUserTransaction(@PathVariable("authtoken") String authtoken, String fromDate, String toDate, String userCode) throws Exception {
		List<UserTransactionIO> transactionIOList = new ArrayList<UserTransactionIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (StringUtil.isNull(fromDate) || StringUtil.isNull(toDate)) {
				throw new ServiceException();
			}
			if (StringUtil.isNull(userCode)) {
				throw new ServiceException(ErrorCode.INVALID_USER_CODE);
			}
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userCode);
			DateTime fromDateTime = new DateTime(fromDate);
			DateTime toDateTime = new DateTime(toDate);
			UserTransactionDTO userTransactionDTO = transactionService.getTransactionHistory(authDTO, userDTO, fromDateTime, toDateTime);
			for (UserTransactionDTO transactionDTO : userTransactionDTO.getList()) {
				UserTransactionIO userTransactionIO = new UserTransactionIO();
				TransactionTypeIO transactionTypeIO = new TransactionTypeIO();
				transactionTypeIO.setName(transactionDTO.getTransactionType().getName());
				transactionTypeIO.setCreditDebitFlag(transactionDTO.getTransactionType().getCreditDebitFlag());
				transactionTypeIO.setCode(transactionDTO.getTransactionType().getCode());
				userTransactionIO.setTransactionType(transactionTypeIO);
				userTransactionIO.setRefferenceCode(transactionDTO.getRefferenceCode());
				if (transactionDTO.getTransactionMode() != null) {
					TransactionModeIO transactionModeIO = new TransactionModeIO();
					transactionModeIO.setCode(transactionDTO.getTransactionMode().getCode());
					transactionModeIO.setName(transactionDTO.getTransactionMode().getName());
					userTransactionIO.setTransactionMode(transactionModeIO);
				}
				userTransactionIO.setCreditAmount(transactionDTO.getCreditAmount());
				userTransactionIO.setDebitAmount(transactionDTO.getDebitAmount());
				userTransactionIO.setTransactionAmount(transactionDTO.getTransactionAmount());
				userTransactionIO.setClosingBalance(transactionDTO.getClosingBalanceAmount());
				userTransactionIO.setTdsTax(transactionDTO.getTdsTax());
				userTransactionIO.setTransactionDate(transactionDTO.getTransactionDate());
				// User
				UserIO userIO = new UserIO();
				userIO.setCode(transactionDTO.getUser().getCode());
				userIO.setName(transactionDTO.getUser().getName());
				userTransactionIO.setUser(userIO);

				transactionIOList.add(userTransactionIO);
			}

		}
		return ResponseIO.success(transactionIOList);
	}

	@RequestMapping(value = "/retrive/{transactionTypeCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<PaymentTransactionIO>> getUserRechargeTransaction(@PathVariable("authtoken") String authtoken, @PathVariable("transactionTypeCode") String transactionTypeCode, String fromDate, String toDate, String userCode) throws Exception {
		List<PaymentTransactionIO> transactionIOList = new ArrayList<PaymentTransactionIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (StringUtil.isNull(fromDate) || StringUtil.isNull(toDate)) {
				throw new ServiceException();
			}
			UserDTO userDTO = new UserDTO();
			DateTime fromDateTime = new DateTime(fromDate);
			DateTime toDateTime = new DateTime(toDate);
			userDTO.setCode(userCode);
			PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
			paymentTransactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(transactionTypeCode));
			paymentTransactionDTO = paymentTransactionService.getPaymentTransactionHistory(authDTO, userDTO, paymentTransactionDTO, fromDateTime, toDateTime);
			for (PaymentTransactionDTO transactionDTO : paymentTransactionDTO.getList()) {
				PaymentTransactionIO transactionIO = new PaymentTransactionIO();

				transactionIO.setCode(transactionDTO.getCode());
				transactionIO.setRemarks(transactionDTO.getRemarks());
				transactionIO.setAmountReceivedDate(transactionDTO.getAmountReceivedDate());
				transactionIO.setName(transactionDTO.getName());
				transactionIO.setTransactionAmount(transactionDTO.getTransactionAmount());
				transactionIO.setTransactionDate(transactionDTO.getTransactionDate());
				// Transaction Mode
				TransactionModeIO modeIO = new TransactionModeIO();
				modeIO.setCode(transactionDTO.getTransactionMode().getCode());
				modeIO.setName(transactionDTO.getTransactionMode().getName());
				transactionIO.setTransactionMode(modeIO);
				// Transaction Type
				TransactionTypeIO typeIO = new TransactionTypeIO();
				typeIO.setCode(transactionDTO.getTransactionType().getCode());
				typeIO.setName(transactionDTO.getTransactionType().getName());
				transactionIO.setTransactionType(typeIO);
				// Ack status
				AcknowledgeStatusIO acknowledgeStatus = new AcknowledgeStatusIO();
				acknowledgeStatus.setCode(transactionDTO.getPaymentAcknowledge().getCode());
				acknowledgeStatus.setName(transactionDTO.getPaymentAcknowledge().getName());
				transactionIO.setAcknowledgeStatus(acknowledgeStatus);
				// User
				UserIO userIO = new UserIO();
				userIO.setCode(transactionDTO.getUser().getCode());
				userIO.setName(transactionDTO.getUser().getName());
				transactionIO.setUser(userIO);
				// Payment Handle User
				UserIO handlerUserIO = new UserIO();
				handlerUserIO.setCode(transactionDTO.getPaymentHandledByUser().getCode());
				handlerUserIO.setName(transactionDTO.getPaymentHandledByUser().getName());
				transactionIO.setPaymentHandledBy(handlerUserIO);
				transactionIOList.add(transactionIO);
			}

		}
		return ResponseIO.success(transactionIOList);
	}

	@RequestMapping(value = "/payment/{paymentCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<PaymentTransactionIO> getUserPaymentTransaction(@PathVariable("authtoken") String authtoken, @PathVariable("paymentCode") String paymentCode) throws Exception {
		PaymentTransactionIO transactionIO = new PaymentTransactionIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
			paymentTransactionDTO.setCode(paymentCode);
			paymentTransactionDTO = paymentTransactionService.getPaymentTransaction(authDTO, paymentTransactionDTO);
			if (paymentTransactionDTO.getId() != 0) {
				transactionIO.setCode(paymentTransactionDTO.getCode());
				transactionIO.setRemarks(paymentTransactionDTO.getRemarks());
				transactionIO.setAmountReceivedDate(paymentTransactionDTO.getAmountReceivedDate());
				transactionIO.setName(paymentTransactionDTO.getName());
				transactionIO.setTransactionAmount(paymentTransactionDTO.getTransactionAmount());
				transactionIO.setTransactionDate(paymentTransactionDTO.getTransactionDate());
				// Transaction Mode
				TransactionModeIO modeIO = new TransactionModeIO();
				modeIO.setCode(paymentTransactionDTO.getTransactionMode().getCode());
				modeIO.setName(paymentTransactionDTO.getTransactionMode().getName());
				transactionIO.setTransactionMode(modeIO);
				// Transaction Type
				TransactionTypeIO typeIO = new TransactionTypeIO();
				typeIO.setCode(paymentTransactionDTO.getTransactionType().getCode());
				typeIO.setName(paymentTransactionDTO.getTransactionType().getName());
				transactionIO.setTransactionType(typeIO);
				// Ack status
				AcknowledgeStatusIO acknowledgeStatus = new AcknowledgeStatusIO();
				acknowledgeStatus.setCode(paymentTransactionDTO.getPaymentAcknowledge().getCode());
				acknowledgeStatus.setName(paymentTransactionDTO.getPaymentAcknowledge().getName());
				transactionIO.setAcknowledgeStatus(acknowledgeStatus);
				// User
				UserIO userIO = new UserIO();
				userIO.setCode(paymentTransactionDTO.getUser().getCode());
				userIO.setName(paymentTransactionDTO.getUser().getName());
				transactionIO.setUser(userIO);
				// Payment Handle User
				UserIO handlerUserIO = new UserIO();
				handlerUserIO.setCode(paymentTransactionDTO.getPaymentHandledByUser().getCode());
				handlerUserIO.setName(paymentTransactionDTO.getPaymentHandledByUser().getName());
				transactionIO.setPaymentHandledBy(handlerUserIO);

				List<PaymentTransactionIO> partialPaymentList = new ArrayList<PaymentTransactionIO>();
				for (PaymentTransactionDTO partialPaymentTransaction : paymentTransactionDTO.getPartialPaymentPaidList()) {
					PaymentTransactionIO partialPayment = new PaymentTransactionIO();
					partialPayment.setCode(partialPaymentTransaction.getCode());
					partialPayment.setRemarks(partialPaymentTransaction.getRemarks());
					partialPayment.setAmountReceivedDate(partialPaymentTransaction.getAmountReceivedDate());
					partialPayment.setName(partialPaymentTransaction.getName());
					partialPayment.setTransactionAmount(partialPaymentTransaction.getTransactionAmount());
					partialPayment.setTransactionDate(partialPaymentTransaction.getTransactionDate());
					// Transaction Mode
					TransactionModeIO transactionMode = new TransactionModeIO();
					transactionMode.setCode(partialPaymentTransaction.getTransactionMode().getCode());
					transactionMode.setName(partialPaymentTransaction.getTransactionMode().getName());
					partialPayment.setTransactionMode(transactionMode);
					// Transaction Type
					TransactionTypeIO transactionType = new TransactionTypeIO();
					transactionType.setCode(partialPaymentTransaction.getTransactionType().getCode());
					transactionType.setName(partialPaymentTransaction.getTransactionType().getName());
					partialPayment.setTransactionType(transactionType);
					// Ack status
					AcknowledgeStatusIO acknowledgeStatusIO = new AcknowledgeStatusIO();
					acknowledgeStatusIO.setCode(partialPaymentTransaction.getPaymentAcknowledge().getCode());
					acknowledgeStatusIO.setName(partialPaymentTransaction.getPaymentAcknowledge().getName());
					partialPayment.setAcknowledgeStatus(acknowledgeStatusIO);
					// User
					UserIO user = new UserIO();
					user.setCode(partialPaymentTransaction.getUser().getCode());
					user.setName(partialPaymentTransaction.getUser().getName());
					partialPayment.setUser(user);
					// Payment Handle User
					UserIO handlerUser = new UserIO();
					handlerUser.setCode(partialPaymentTransaction.getPaymentHandledByUser().getCode());
					handlerUser.setName(partialPaymentTransaction.getPaymentHandledByUser().getName());
					partialPayment.setPaymentHandledBy(handlerUser);

					partialPaymentList.add(partialPayment);
				}
				transactionIO.setPartialPaymentList(partialPaymentList);
			}
		}
		return ResponseIO.success(transactionIO);
	}

	@RequestMapping(value = "/payment/ticket/{ticketCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<PaymentTransactionIO> getUserPaymentTransactionByTicket(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		PaymentTransactionIO transactionIO = new PaymentTransactionIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);

			PaymentTransactionDTO paymentTransactionDTO = paymentTransactionService.getPaymentTransactionByTicket(authDTO, ticketDTO);
			if (paymentTransactionDTO.getId() != 0) {
				transactionIO.setCode(paymentTransactionDTO.getCode());
				transactionIO.setRemarks(paymentTransactionDTO.getRemarks());
				transactionIO.setAmountReceivedDate(paymentTransactionDTO.getAmountReceivedDate());
				transactionIO.setName(paymentTransactionDTO.getName());
				transactionIO.setTransactionAmount(paymentTransactionDTO.getTransactionAmount());
				transactionIO.setTransactionDate(paymentTransactionDTO.getTransactionDate());
				// Transaction Mode
				TransactionModeIO modeIO = new TransactionModeIO();
				modeIO.setCode(paymentTransactionDTO.getTransactionMode().getCode());
				modeIO.setName(paymentTransactionDTO.getTransactionMode().getName());
				transactionIO.setTransactionMode(modeIO);
				// Transaction Type
				TransactionTypeIO typeIO = new TransactionTypeIO();
				typeIO.setCode(paymentTransactionDTO.getTransactionType().getCode());
				typeIO.setName(paymentTransactionDTO.getTransactionType().getName());
				transactionIO.setTransactionType(typeIO);
				// Ack status
				AcknowledgeStatusIO acknowledgeStatus = new AcknowledgeStatusIO();
				acknowledgeStatus.setCode(paymentTransactionDTO.getPaymentAcknowledge().getCode());
				acknowledgeStatus.setName(paymentTransactionDTO.getPaymentAcknowledge().getName());
				transactionIO.setAcknowledgeStatus(acknowledgeStatus);
				// User
				UserIO userIO = new UserIO();
				userIO.setCode(paymentTransactionDTO.getUser().getCode());
				userIO.setName(paymentTransactionDTO.getUser().getName());
				transactionIO.setUser(userIO);
				// Payment Handle User
				UserIO handlerUserIO = new UserIO();
				handlerUserIO.setCode(paymentTransactionDTO.getPaymentHandledByUser().getCode());
				handlerUserIO.setName(paymentTransactionDTO.getPaymentHandledByUser().getName());
				transactionIO.setPaymentHandledBy(handlerUserIO);

				List<PaymentTransactionIO> partialPaymentList = new ArrayList<PaymentTransactionIO>();
				for (PaymentTransactionDTO partialPaymentTransaction : paymentTransactionDTO.getPartialPaymentPaidList()) {
					PaymentTransactionIO partialPayment = new PaymentTransactionIO();
					partialPayment.setCode(partialPaymentTransaction.getCode());
					partialPayment.setRemarks(partialPaymentTransaction.getRemarks());
					partialPayment.setAmountReceivedDate(partialPaymentTransaction.getAmountReceivedDate());
					partialPayment.setName(partialPaymentTransaction.getName());
					partialPayment.setTransactionAmount(partialPaymentTransaction.getTransactionAmount());
					partialPayment.setTransactionDate(partialPaymentTransaction.getTransactionDate());
					// Transaction Mode
					TransactionModeIO transactionMode = new TransactionModeIO();
					transactionMode.setCode(partialPaymentTransaction.getTransactionMode().getCode());
					transactionMode.setName(partialPaymentTransaction.getTransactionMode().getName());
					partialPayment.setTransactionMode(transactionMode);
					// Transaction Type
					TransactionTypeIO transactionType = new TransactionTypeIO();
					transactionType.setCode(partialPaymentTransaction.getTransactionType().getCode());
					transactionType.setName(partialPaymentTransaction.getTransactionType().getName());
					partialPayment.setTransactionType(transactionType);
					// Ack status
					AcknowledgeStatusIO acknowledgeStatusIO = new AcknowledgeStatusIO();
					acknowledgeStatusIO.setCode(partialPaymentTransaction.getPaymentAcknowledge().getCode());
					acknowledgeStatusIO.setName(partialPaymentTransaction.getPaymentAcknowledge().getName());
					partialPayment.setAcknowledgeStatus(acknowledgeStatusIO);
					// User
					UserIO user = new UserIO();
					user.setCode(partialPaymentTransaction.getUser().getCode());
					user.setName(partialPaymentTransaction.getUser().getName());
					partialPayment.setUser(user);
					// Payment Handle User
					UserIO handlerUser = new UserIO();
					handlerUser.setCode(partialPaymentTransaction.getPaymentHandledByUser().getCode());
					handlerUser.setName(partialPaymentTransaction.getPaymentHandledByUser().getName());
					partialPayment.setPaymentHandledBy(handlerUser);

					partialPaymentList.add(partialPayment);
				}
				transactionIO.setPartialPaymentList(partialPaymentList);
			}
		}
		return ResponseIO.success(transactionIO);
	}

	@RequestMapping(value = "/payment/gateway/options", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<PaymentModeIO>> getActivePaymentGateway(@PathVariable("authtoken") String authtoken) throws Exception {
		List<PaymentModeIO> paymentModeList = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<PaymentGatewayScheduleDTO> list = gatewayScheduleService.getActiveSchedulePaymentGateway(authDTO, OrderTypeEM.RECHARGE);
			if (!list.isEmpty()) {
				Map<String, List<PaymentGatewayPartnerIO>> mapList = new HashMap<>();
				Map<String, PaymentGatewayPartnerDTO> modeMAP = new HashMap<>();
				for (PaymentGatewayScheduleDTO scheduleDTO : list) {
					if (mapList.get(scheduleDTO.getGatewayPartner().getPaymentMode().getCode()) == null) {
						mapList.put(scheduleDTO.getGatewayPartner().getPaymentMode().getCode(), new ArrayList<PaymentGatewayPartnerIO>());
					}
					PaymentGatewayPartnerIO partnerIO = new PaymentGatewayPartnerIO();
					partnerIO.setCode(scheduleDTO.getGatewayPartner().getCode());
					partnerIO.setName(scheduleDTO.getGatewayPartner().getName());
					partnerIO.setOfferNotes(scheduleDTO.getGatewayPartner().getOfferNotes());
					partnerIO.setOfferTerms(scheduleDTO.getGatewayPartner().getOfferTerms());
					partnerIO.setServiceCharge(scheduleDTO.getServiceCharge());

					List<PaymentGatewayPartnerIO> partnerList = mapList.get(scheduleDTO.getGatewayPartner().getPaymentMode().getCode());
					partnerList.add(partnerIO);
					mapList.put(scheduleDTO.getGatewayPartner().getPaymentMode().getCode(), partnerList);
					modeMAP.put(scheduleDTO.getGatewayPartner().getPaymentMode().getCode(), scheduleDTO.getGatewayPartner());
				}
				for (String mapKey : mapList.keySet()) {
					PaymentModeIO modeIO = new PaymentModeIO();
					modeIO.setCode(modeMAP.get(mapKey).getPaymentMode().getCode());
					modeIO.setName(modeMAP.get(mapKey).getPaymentMode().getName());
					modeIO.setPaymentGatewayPartner(mapList.get(mapKey));
					paymentModeList.add(modeIO);
				}
			}
		}
		return ResponseIO.success(paymentModeList);
	}

	@RequestMapping(value = "/recharge/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<PaymentTransactionIO> rechargeTransaction(@PathVariable("authtoken") String authtoken, @RequestBody PaymentTransactionIO transaction) throws Exception {
		PaymentTransactionIO paymentTransactionIO = new PaymentTransactionIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			PaymentTransactionDTO transactionDTO = new PaymentTransactionDTO();
			transactionDTO.setAmountReceivedDate(transaction.getAmountReceivedDate());
			transactionDTO.setRemarks(transaction.getRemarks());
			transactionDTO.setTransactionAmount(transaction.getTransactionAmount());
			transactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(transaction.getTransactionMode().getCode()));
			transactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(transaction.getTransactionType().getCode()));
			UserDTO userDTO = new UserDTO();
			UserDTO paymentHandleBy = new UserDTO();
			userDTO.setCode(transaction.getUser().getCode());
			paymentHandleBy.setCode(transaction.getPaymentHandledBy().getCode());
			transactionDTO.setUser(userDTO);
			transactionDTO.setCommissionAmount(BigDecimal.ZERO);
			transactionDTO.setAcBusTax(BigDecimal.ZERO);
			transactionDTO.setTdsTax(BigDecimal.ZERO);
			transactionDTO.setPaymentHandledByUser(paymentHandleBy);
			paymentTransactionService.rechargeTransaction(authDTO, transactionDTO);
			paymentTransactionIO.setCode(transactionDTO.getCode());
			paymentTransactionIO.setTransactionAmount(transactionDTO.getTransactionAmount());
		}

		return ResponseIO.success(paymentTransactionIO);
	}

	@RequestMapping(value = "/recharge/gateway/initiate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<RechargeOrderIO> requestGatewayRecharge(@PathVariable("authtoken") String authtoken, String responseUrl, BigDecimal transactionAmount, String gatewayCode) throws Exception {
		RechargeOrderIO rechargeOrderIO = new RechargeOrderIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			OrderInitRequestDTO paymentRequest = new OrderInitRequestDTO();
			paymentRequest.setAmount(transactionAmount.setScale(0, RoundingMode.HALF_UP));
			paymentRequest.setFirstName(WordUtils.capitalize(authDTO.getUser().getName()));
			paymentRequest.setLastName(StringUtil.removeSymbol(authDTO.getUser().getLastname()));
			paymentRequest.setPartnerCode(gatewayCode);
			paymentRequest.setResponseUrl(responseUrl);
			paymentRequest.setOrderCode(TokenGenerator.generateCode("ORG"));
			paymentRequest.setOrderType(OrderTypeEM.RECHARGE);
			paymentRequest.setAddress1(authDTO.getUser().getName());
			paymentRequest.setMobile(authDTO.getUser().getMobile());
			paymentRequest.setEmail(authDTO.getUser().getEmail());
			paymentRequest.setUdf1("Online Recharge " + authDTO.getUser().getName());
			paymentRequest.setUdf2(authDTO.getNamespaceCode());
			paymentRequest.setUdf3(authDTO.getDeviceMedium().getCode());
			paymentRequest.setUdf4(authDTO.getUser().getUsername());
			paymentRequest.setUdf5(ApplicationConfig.getServerZoneCode());
			OrderInitStatusDTO orderInitStatusDTO = paymentRequestService.handlePgService(authDTO, paymentRequest);
			rechargeOrderIO.setTransactionCode(orderInitStatusDTO.getTransactionCode());
			rechargeOrderIO.setPaymentRequestUrl(orderInitStatusDTO.getPaymentRequestUrl());
			rechargeOrderIO.setGatewayInputDetails(orderInitStatusDTO.getGatewayInputDetails());
		}

		return ResponseIO.success(rechargeOrderIO);
	}

	@RequestMapping(value = "/recharge/gateway/progress", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<RechargeOrderIO> progressGatewayRecharge(@PathVariable("authtoken") String authtoken) throws Exception {
		RechargeOrderIO rechargeOrderIO = new RechargeOrderIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			// pendingOrderService.get
		}

		return ResponseIO.success(rechargeOrderIO);
	}

	@RequestMapping(value = "/recharge/gateway/validate/{orderCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<PaymentTransactionIO> rechargeGatewayTransaction(@PathVariable("authtoken") String authtoken, @PathVariable("orderCode") String orderCode) throws Exception {
		PaymentTransactionIO paymentTransactionIO = new PaymentTransactionIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			PaymentTransactionDTO transactionDTO = paymentTransactionService.rechargeGatewayTransaction(authDTO, orderCode);
			paymentTransactionIO.setCode(transactionDTO.getCode());
			paymentTransactionIO.setTransactionAmount(transactionDTO.getTransactionAmount());
		}
		return ResponseIO.success(paymentTransactionIO);
	}

	@RequestMapping(value = "/recharge/pending/order/{orderCode}/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<PaymentTransactionIO> rechargeConfirmOrder(@PathVariable("authtoken") String authtoken, @PathVariable("orderCode") String orderCode) throws Exception {
		PaymentTransactionIO paymentTransactionIO = new PaymentTransactionIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			PaymentTransactionDTO transactionDTO = pendingOrderService.rechargeConfirmOrder(authDTO, orderCode);
			paymentTransactionIO.setCode(transactionDTO.getCode());
			paymentTransactionIO.setTransactionAmount(transactionDTO.getTransactionAmount());
			paymentTransactionIO.setActiveFlag(transactionDTO.getActiveFlag());
		}
		return ResponseIO.success(paymentTransactionIO);
	}

	@RequestMapping(value = "/acknowledge/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<PaymentTransactionIO> UpdateAcknowledgeTransaction(@PathVariable("authtoken") String authtoken, String paymentTransactionCode, String paymentAcknowledgeCode, String remarks) throws Exception {
		PaymentTransactionIO paymentTransactionIO = new PaymentTransactionIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			PaymentTransactionDTO transactionDTO = new PaymentTransactionDTO();
			transactionDTO.setCode(paymentTransactionCode);
			transactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(paymentAcknowledgeCode));
			transactionDTO.setRemarks(remarks);
			paymentTransactionService.acknowledgeTransaction(authDTO, transactionDTO);
			paymentTransactionIO.setCode(transactionDTO.getCode());
			paymentTransactionIO.setTransactionAmount(transactionDTO.getTransactionAmount());
		}
		return ResponseIO.success(paymentTransactionIO);
	}

	@RequestMapping(value = "/acknowledge/update/v2", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<PaymentTransactionIO> updateAcknowledgeTransactionV2(@PathVariable("authtoken") String authtoken, @RequestBody PaymentTransactionIO paymentTransaction) throws Exception {
		PaymentTransactionIO paymentTransactionIO = new PaymentTransactionIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			PaymentTransactionDTO transactionDTO = new PaymentTransactionDTO();
			transactionDTO.setCode(paymentTransaction.getCode());
			transactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(paymentTransaction.getAcknowledgeStatus().getCode()));
			transactionDTO.setRemarks(paymentTransaction.getRemarks());

			if (PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID.getId() == transactionDTO.getPaymentAcknowledge().getId() || PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED.getId() == transactionDTO.getPaymentAcknowledge().getId()) {
				transactionDTO.setTransactionAmount(paymentTransaction.getTransactionAmount());
				transactionDTO.setAmountReceivedDate(paymentTransaction.getAmountReceivedDate());
				transactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(paymentTransaction.getTransactionMode().getCode()));
			}

			paymentTransactionService.acknowledgeTransaction(authDTO, transactionDTO);
			paymentTransactionIO.setCode(transactionDTO.getCode());
			paymentTransactionIO.setTransactionAmount(transactionDTO.getTransactionAmount());
		}
		return ResponseIO.success(paymentTransactionIO);
	}

	@RequestMapping(value = "/acknowledge", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<PaymentTransactionIO>> getAcknowledgeTransaction(@PathVariable("authtoken") String authtoken) throws Exception {
		List<PaymentTransactionIO> TransactionIOList = new ArrayList<PaymentTransactionIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			PaymentTransactionDTO transactionDTO = new PaymentTransactionDTO();
			transactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_INITIATED);
			paymentTransactionService.getUnAcknowledgeTransaction(authDTO, transactionDTO);
			PaymentTransactionDTO transactionPaidDTO = new PaymentTransactionDTO();
			transactionPaidDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_PAID);
			paymentTransactionService.getUnAcknowledgeTransaction(authDTO, transactionPaidDTO);
			transactionDTO.getList().addAll(transactionPaidDTO.getList());
			PaymentTransactionDTO transactionPartialPaidDTO = new PaymentTransactionDTO();
			transactionPartialPaidDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID);
			paymentTransactionService.getUnAcknowledgeTransaction(authDTO, transactionPartialPaidDTO);
			transactionDTO.getList().addAll(transactionPartialPaidDTO.getList());
			for (PaymentTransactionDTO dto : transactionDTO.getList()) {
				PaymentTransactionIO paymentTransactionIO = new PaymentTransactionIO();
				paymentTransactionIO.setCode(dto.getCode());
				paymentTransactionIO.setTransactionAmount(dto.getTransactionAmount());
				paymentTransactionIO.setTransactionDate(dto.getTransactionDate());
				paymentTransactionIO.setAmountReceivedDate(dto.getAmountReceivedDate());

				TransactionModeIO transactionModeIO = new TransactionModeIO();
				transactionModeIO.setCode(dto.getTransactionMode().getCode());
				transactionModeIO.setName(dto.getTransactionMode().getName());
				paymentTransactionIO.setTransactionMode(transactionModeIO);

				TransactionTypeIO transactionTypeIO = new TransactionTypeIO();
				transactionTypeIO.setCode(dto.getTransactionType().getCode());
				transactionTypeIO.setName(dto.getTransactionType().getName());
				paymentTransactionIO.setTransactionType(transactionTypeIO);

				AcknowledgeStatusIO acknowledgeStatus = new AcknowledgeStatusIO();
				acknowledgeStatus.setCode(dto.getPaymentAcknowledge().getCode());
				acknowledgeStatus.setName(dto.getPaymentAcknowledge().getName());
				paymentTransactionIO.setAcknowledgeStatus(acknowledgeStatus);

				UserIO userIO = new UserIO();
				userIO.setCode(dto.getUser().getCode());
				userIO.setName(dto.getUser().getName());
				userIO.setLastname(dto.getUser().getLastname());
				userIO.setUsername(dto.getUser().getUsername());
				paymentTransactionIO.setUser(userIO);
				UserIO user = new UserIO();
				user.setCode(dto.getPaymentHandledByUser().getCode());
				user.setName(dto.getPaymentHandledByUser().getName());
				user.setLastname(dto.getPaymentHandledByUser().getLastname());
				user.setUsername(dto.getPaymentHandledByUser().getUsername());
				paymentTransactionIO.setPaymentHandledBy(user);

				paymentTransactionIO.setRemarks(dto.getRemarks());

				List<PaymentTransactionIO> partialPaymentList = new ArrayList<PaymentTransactionIO>();
				for (PaymentTransactionDTO partialPaymentTransaction : dto.getPartialPaymentPaidList()) {
					PaymentTransactionIO partialPayment = new PaymentTransactionIO();
					partialPayment.setCode(partialPaymentTransaction.getCode());
					partialPayment.setRemarks(partialPaymentTransaction.getRemarks());
					partialPayment.setAmountReceivedDate(partialPaymentTransaction.getAmountReceivedDate());
					partialPayment.setName(partialPaymentTransaction.getName());
					partialPayment.setTransactionAmount(partialPaymentTransaction.getTransactionAmount());
					partialPayment.setTransactionDate(partialPaymentTransaction.getTransactionDate());
					// Transaction Mode
					TransactionModeIO transactionMode = new TransactionModeIO();
					transactionMode.setCode(partialPaymentTransaction.getTransactionMode().getCode());
					transactionMode.setName(partialPaymentTransaction.getTransactionMode().getName());
					partialPayment.setTransactionMode(transactionMode);
					// Transaction Type
					TransactionTypeIO transactionType = new TransactionTypeIO();
					transactionType.setCode(partialPaymentTransaction.getTransactionType().getCode());
					transactionType.setName(partialPaymentTransaction.getTransactionType().getName());
					partialPayment.setTransactionType(transactionType);
					// Ack status
					AcknowledgeStatusIO acknowledgeStatusIO = new AcknowledgeStatusIO();
					acknowledgeStatusIO.setCode(partialPaymentTransaction.getPaymentAcknowledge().getCode());
					acknowledgeStatusIO.setName(partialPaymentTransaction.getPaymentAcknowledge().getName());
					partialPayment.setAcknowledgeStatus(acknowledgeStatusIO);
					// User
					UserIO partialUser = new UserIO();
					partialUser.setCode(partialPaymentTransaction.getUser().getCode());
					partialUser.setName(partialPaymentTransaction.getUser().getName());
					partialPayment.setUser(partialUser);
					// Payment Handle User
					UserIO handlerUser = new UserIO();
					handlerUser.setCode(partialPaymentTransaction.getPaymentHandledByUser().getCode());
					handlerUser.setName(partialPaymentTransaction.getPaymentHandledByUser().getName());
					partialPayment.setPaymentHandledBy(handlerUser);

					partialPaymentList.add(partialPayment);
				}
				paymentTransactionIO.setPartialPaymentList(partialPaymentList);
				TransactionIOList.add(paymentTransactionIO);
			}
		}
		return ResponseIO.success(TransactionIOList);
	}

	@RequestMapping(value = "/mode", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TransactionModeIO>> getTransactionMode(@PathVariable("authtoken") String authtoken) throws Exception {
		List<TransactionModeIO> list = new ArrayList<TransactionModeIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TransactionModeIO modeIO = new TransactionModeIO();
			modeIO.setCode(TransactionModeEM.PAYMENT_CASH.getCode());
			modeIO.setName(TransactionModeEM.PAYMENT_CASH.getName());
			list.add(modeIO);
			modeIO = new TransactionModeIO();
			modeIO.setCode(TransactionModeEM.PAYMENT_CHEQUE.getCode());
			modeIO.setName(TransactionModeEM.PAYMENT_CHEQUE.getName());
			list.add(modeIO);
			modeIO = new TransactionModeIO();
			modeIO.setCode(TransactionModeEM.PAYMENT_CREDIT_CARD.getCode());
			modeIO.setName(TransactionModeEM.PAYMENT_CREDIT_CARD.getName());
			list.add(modeIO);
			modeIO = new TransactionModeIO();
			modeIO.setCode(TransactionModeEM.PAYMENT_NBK.getCode());
			modeIO.setName(TransactionModeEM.PAYMENT_NBK.getName());
			list.add(modeIO);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/type", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TransactionTypeIO>> getTransactionType(@PathVariable("authtoken") String authtoken) throws Exception {
		List<TransactionTypeIO> list = new ArrayList<TransactionTypeIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TransactionTypeIO modeIO = new TransactionTypeIO();
			modeIO.setCode(TransactionTypeEM.PAYMENT_VOUCHER.getCode());
			modeIO.setName(TransactionTypeEM.PAYMENT_VOUCHER.getName());
			list.add(modeIO);
			modeIO = new TransactionTypeIO();
			modeIO.setCode(TransactionTypeEM.RECHARGE.getCode());
			modeIO.setName(TransactionTypeEM.RECHARGE.getName());
			list.add(modeIO);
			modeIO = new TransactionTypeIO();
			modeIO.setCode(TransactionTypeEM.REVOKE_RECEIPT.getCode());
			modeIO.setName(TransactionTypeEM.REVOKE_RECEIPT.getName());
			list.add(modeIO);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/voucher/unpaid", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<PaymentVoucherIO>> getPaymentVoucherUnPaid(@PathVariable("authtoken") String authtoken, String userCode, String fromDate, String toDate, Boolean useTravelDate, String scheduleCode) throws Exception {
		List<PaymentVoucherIO> pvList = new ArrayList<PaymentVoucherIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (StringUtil.isNull(fromDate) || StringUtil.isNull(toDate)) {
				throw new ServiceException(ErrorCode.INVALID_DATE);
			}
			DateTime fromDateTime = new DateTime(fromDate);
			DateTime toDateTime = new DateTime(toDate);
			List<PaymentVoucherDTO> listDTO = transactionService.getPaymentVoucherUnPaid(authDTO, userCode, fromDateTime, toDateTime, useTravelDate, scheduleCode);
			for (PaymentVoucherDTO voucherDTO : listDTO) {
				PaymentVoucherIO voucher = new PaymentVoucherIO();

				voucher.setTransactionCode(voucherDTO.getTransactionCode());
				voucher.setTicketCode(voucherDTO.getTicketCode());

				UserIO user = new UserIO();
				user.setCode(voucherDTO.getUser().getCode());
				user.setName(voucherDTO.getUser().getName());

				GroupIO group = new GroupIO();
				group.setName(voucherDTO.getUser().getGroup().getName());

				user.setGroup(group);
				voucher.setUser(user);

				voucher.setTransactionCode(voucherDTO.getTransactionCode());
				voucher.setTransactionDate(voucherDTO.getTransactionDate());
				voucher.setTravelDate(voucherDTO.getTravelDate());
				voucher.setSeatNames(voucherDTO.getSeatNames());
				voucher.setScheduleNames(voucherDTO.getScheduleNames());
				voucher.setTripCode(voucherDTO.getTripCode());
				voucher.setSeatCount(voucherDTO.getSeatCount());
				voucher.setTransactionAmount(voucherDTO.getTransactionAmount());
				voucher.setTicketAmount(voucherDTO.getTicketAmount());
				voucher.setServiceTax(voucherDTO.getServiceTax());
				voucher.setCommissionAmount(voucherDTO.getCommissionAmount());
				voucher.setAddonsAmount(voucherDTO.getAddonsAmount());
				voucher.setExtraCommissionAmount(voucherDTO.getExtraCommissionAmount());

				voucher.setRefundAmount(voucherDTO.getRefundAmount());
				voucher.setCancellationChargeAmount(voucherDTO.getCancellationChargeAmount());
				voucher.setCancellationChargeCommissionAmount(voucherDTO.getCancellationChargeCommissionAmount());
				voucher.setRevokeCancelCommissionAmount(voucherDTO.getRevokeCancelCommissionAmount());
				voucher.setNetAmount(voucherDTO.getNetAmount());

				// Transaction Type
				TransactionTypeIO transactionTypeIO = new TransactionTypeIO();
				transactionTypeIO.setCode(voucherDTO.getTransactionType().getCode());
				transactionTypeIO.setName(voucherDTO.getTransactionType().getName());
				voucher.setTransactionType(transactionTypeIO);

				StationIO FromStationIO = new StationIO();
				FromStationIO.setName(voucherDTO.getFromStation().getName());
				StationIO toStationIO = new StationIO();
				toStationIO.setName(voucherDTO.getToStation().getName());
				voucher.setFromStation(FromStationIO);
				voucher.setToStation(toStationIO);

				pvList.add(voucher);
			}
		}
		return ResponseIO.success(pvList);
	}

	@RequestMapping(value = "/voucher/generate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<PaymentTransactionIO> generatePaymentVoucher(@PathVariable("authtoken") String authtoken, @RequestBody PaymentTransactionIO transaction) throws Exception {
		checkConcurrentRequests(transaction.getUser().getCode() + "_" + transaction.getPaymentHandledBy().getCode() + "_" + transaction.getAmountReceivedDate());
		PaymentTransactionIO transactionIO = new PaymentTransactionIO();
		try {
			AuthDTO authDTO = authService.getAuthDTO(authtoken);
			if (authDTO != null) {
				if (StringUtil.isNull(transaction.getTransactionCodes()) || transaction.getTransactionCodes().split(",").length == Numeric.ZERO_INT) {
					throw new ServiceException(ErrorCode.INVALID_CODE);
				}

				PaymentTransactionDTO transactionDTO = new PaymentTransactionDTO();
				transactionDTO.setAmountReceivedDate(transaction.getAmountReceivedDate());
				transactionDTO.setRemarks(transaction.getRemarks());
				transactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(transaction.getTransactionMode().getCode()));
				transactionDTO.setTransactionType(TransactionTypeEM.PAYMENT_VOUCHER);
				UserDTO userDTO = new UserDTO();
				UserDTO paymentHandleBy = new UserDTO();
				userDTO.setCode(transaction.getUser().getCode());
				paymentHandleBy.setCode(transaction.getPaymentHandledBy().getCode());
				transactionDTO.setUser(userDTO);
				transactionDTO.setCommissionAmount(BigDecimal.ZERO);
				transactionDTO.setAcBusTax(BigDecimal.ZERO);
				transactionDTO.setTdsTax(BigDecimal.ZERO);
				transactionDTO.setPaymentHandledByUser(paymentHandleBy);

				transactionService.generatePaymentVoucher(authDTO, transactionDTO, transaction.getTransactionCodes());

				transactionIO.setTransactionAmount(transactionDTO.getTransactionAmount());
				transactionIO.setCode(transactionDTO.getCode());
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			releaseConcurrentRequests(transaction.getUser().getCode() + "_" + transaction.getPaymentHandledBy().getCode() + "_" + transaction.getAmountReceivedDate());
		}
		return ResponseIO.success(transactionIO);
	}

	@RequestMapping(value = "/voucher/{paymentCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<PaymentVoucherIO>> getPaymentVoucherDetails(@PathVariable("authtoken") String authtoken, @PathVariable("paymentCode") String paymentCode) throws Exception {
		List<PaymentVoucherIO> pvList = new ArrayList<PaymentVoucherIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (StringUtil.isNull(paymentCode)) {
				throw new ServiceException(ErrorCode.INVALID_CODE);
			}
			List<PaymentVoucherDTO> listDTO = transactionService.getGeneratedPaymentVoucherDetails(authDTO, paymentCode);
			for (PaymentVoucherDTO item : listDTO) {
				PaymentVoucherIO pvIO = new PaymentVoucherIO();
				pvIO.setCommissionAmount(item.getCommissionAmount());
				pvIO.setServiceTax(item.getServiceTax());
				pvIO.setSeatCount(item.getSeatCount());
				pvIO.setTicketAmount(item.getTicketAmount());
				pvIO.setAddonsAmount(item.getAddonsAmount());
				pvIO.setTicketCode(item.getTicketCode());
				pvIO.setTransactionCode(item.getTransactionCode());
				pvIO.setTransactionDate(item.getTransactionDate());
				pvIO.setTravelDate(item.getTravelDate());
				pvIO.setSeatNames(item.getSeatNames());
				// Transaction Type
				TransactionTypeIO transactionTypeIO = new TransactionTypeIO();
				transactionTypeIO.setCode(item.getTransactionType().getCode());
				transactionTypeIO.setName(item.getTransactionType().getName());
				pvIO.setTransactionType(transactionTypeIO);

				// Cancellation details
				pvIO.setCancellationChargeAmount(item.getCancellationChargeAmount());
				pvIO.setCancellationChargeCommissionAmount(item.getCancellationChargeCommissionAmount());
				pvIO.setRefundAmount(item.getRefundAmount());

				StationIO FromStationIO = new StationIO();
				FromStationIO.setCode(item.getFromStation().getCode());
				FromStationIO.setName(item.getFromStation().getName());
				StationIO toStationIO = new StationIO();
				toStationIO.setCode(item.getToStation().getCode());
				toStationIO.setName(item.getToStation().getName());
				pvIO.setFromStation(FromStationIO);
				pvIO.setToStation(toStationIO);

				pvList.add(pvIO);
			}
		}
		return ResponseIO.success(pvList);
	}

	@RequestMapping(value = "/voucher/acknowledge/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<PaymentTransactionIO> updateAcknowledgeTransactionV3(@PathVariable("authtoken") String authtoken, @RequestBody PaymentTransactionIO cargoPaymentTransaction) throws Exception {
		PaymentTransactionIO paymentTransactionIO = new PaymentTransactionIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			PaymentTransactionDTO transactionDTO = new PaymentTransactionDTO();
			transactionDTO.setCode(cargoPaymentTransaction.getCode());
			transactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(cargoPaymentTransaction.getAcknowledgeStatus().getCode()));
			transactionDTO.setTransactionType(TransactionTypeEM.CARGO_PAYMENT_VOUCHER);
			transactionDTO.setRemarks(cargoPaymentTransaction.getRemarks());

			if (PaymentAcknowledgeEM.PARTIAL_PAYMENT_PAID.getId() == transactionDTO.getPaymentAcknowledge().getId() || PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED.getId() == transactionDTO.getPaymentAcknowledge().getId()) {
				transactionDTO.setTransactionAmount(cargoPaymentTransaction.getTransactionAmount());
				transactionDTO.setAmountReceivedDate(cargoPaymentTransaction.getAmountReceivedDate());
				transactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(cargoPaymentTransaction.getTransactionMode().getCode()));

				UserDTO paymentHandleBy = new UserDTO();
				paymentHandleBy.setCode(cargoPaymentTransaction.getPaymentHandledBy().getCode());
				transactionDTO.setPaymentHandledByUser(paymentHandleBy);
			}

			paymentTransactionService.acknowledgePaymentVoucherTransaction(authDTO, transactionDTO);
			paymentTransactionIO.setCode(transactionDTO.getCode());
			paymentTransactionIO.setTransactionAmount(transactionDTO.getTransactionAmount());
		}
		return ResponseIO.success(paymentTransactionIO);
	}

	@RequestMapping(value = "/recharge/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<UserIO>> getRechargeUsers(@PathVariable("authtoken") String authtoken) throws Exception {
		List<UserIO> user = new ArrayList<UserIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<UserDTO> list = userService.getAll(authDTO);
			for (UserDTO userDTO : list) {
				if (userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && userDTO.getPaymentType().getId() == PaymentTypeEM.PAYMENT_PRE_PAID.getId()) {
					UserIO userio = new UserIO();
					GroupIO groupIO = new GroupIO();
					OrganizationIO organizationIO = new OrganizationIO();
					userio.setUsername(userDTO.getUsername());
					userio.setEmail(userDTO.getEmail());
					userio.setCode(userDTO.getCode());
					userio.setName(userDTO.getName());
					userio.setLastname(userDTO.getLastname());
					userio.setActiveFlag(userDTO.getActiveFlag());

					PaymentTypeIO paymentType = new PaymentTypeIO();
					paymentType.setCode(userDTO.getPaymentType().getCode());
					paymentType.setName(userDTO.getPaymentType().getName());
					userio.setPaymentType(paymentType);

					groupIO.setCode(userDTO.getGroup() != null ? userDTO.getGroup().getCode() : null);
					groupIO.setName(userDTO.getGroup() != null ? userDTO.getGroup().getName() : null);
					groupIO.setDecription(userDTO.getGroup() != null ? userDTO.getGroup().getDecription() : null);
					organizationIO.setCode(userDTO.getOrganization() != null ? userDTO.getOrganization().getCode() : null);
					organizationIO.setName(userDTO.getOrganization() != null ? userDTO.getOrganization().getName() : null);
					userio.setOrganization(organizationIO);
					userio.setGroup(groupIO);
					user.add(userio);
				}
			}

		}
		return ResponseIO.success(user);
	}

	@RequestMapping(value = "/voucher/user", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<UserIO>> getVoucherUsers(@PathVariable("authtoken") String authtoken) throws Exception {
		List<UserIO> user = new ArrayList<UserIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<UserDTO> list = userService.getAll(authDTO);
			for (UserDTO userDTO : list) {
				if (userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && userDTO.getPaymentType().getId() == PaymentTypeEM.PAYMENT_POST_PAID.getId()) {
					UserIO userio = new UserIO();
					GroupIO groupIO = new GroupIO();
					OrganizationIO organizationIO = new OrganizationIO();
					userio.setUsername(userDTO.getUsername());
					userio.setEmail(userDTO.getEmail());
					userio.setCode(userDTO.getCode());
					userio.setName(userDTO.getName());
					userio.setLastname(userDTO.getLastname());
					userio.setActiveFlag(userDTO.getActiveFlag());

					PaymentTypeIO paymentType = new PaymentTypeIO();
					paymentType.setCode(userDTO.getPaymentType().getCode());
					paymentType.setName(userDTO.getPaymentType().getName());
					userio.setPaymentType(paymentType);

					groupIO.setCode(userDTO.getGroup() != null ? userDTO.getGroup().getCode() : null);
					groupIO.setName(userDTO.getGroup() != null ? userDTO.getGroup().getName() : null);
					groupIO.setDecription(userDTO.getGroup() != null ? userDTO.getGroup().getDecription() : null);
					organizationIO.setCode(userDTO.getOrganization() != null ? userDTO.getOrganization().getCode() : null);
					organizationIO.setName(userDTO.getOrganization() != null ? userDTO.getOrganization().getName() : null);
					userio.setOrganization(organizationIO);
					userio.setGroup(groupIO);
					user.add(userio);
				}
			}
		}
		return ResponseIO.success(user);
	}

	@RequestMapping(value = "/voucher/user/auto", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<UserIO>> getVoucherAutoGenerateUsers(@PathVariable("authtoken") String authtoken) throws Exception {
		List<UserIO> user = new ArrayList<UserIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			// operation_code ='VOC-AUTO-GENET'
			List<UserDTO> list = userService.getAll(authDTO);
			for (UserDTO userDTO : list) {
				if (userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
					List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
					Eventlist.add(MenuEventEM.TICKET_PAYMENT_VOUCHER);
					List<MenuDTO> privilegesDTOlist = menuService.getUserPrivileges(authDTO, userDTO);
					MenuEventDTO MinsMenuEventDTO = getPrivilegeV3(privilegesDTOlist, Eventlist);

					if (MinsMenuEventDTO == null || MinsMenuEventDTO.getEnabledFlag() != Numeric.ONE_INT) {
						continue;
					}
					UserIO userio = new UserIO();
					userio.setUsername(userDTO.getUsername());
					userio.setEmail(userDTO.getEmail());
					userio.setCode(userDTO.getCode());
					userio.setName(userDTO.getName());
					userio.setLastname(userDTO.getLastname());
					userio.setActiveFlag(userDTO.getActiveFlag());

					PaymentTypeIO paymentType = new PaymentTypeIO();
					paymentType.setCode(userDTO.getPaymentType().getCode());
					paymentType.setName(userDTO.getPaymentType().getName());
					userio.setPaymentType(paymentType);

					user.add(userio);
				}
			}
		}
		return ResponseIO.success(user);
	}

	@RequestMapping(value = "/credit/update/{transactioncode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateUserBalance(@PathVariable("authtoken") String authtoken, @PathVariable("transactioncode") String transactioncode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<String> transactioncodes = Arrays.asList(transactioncode.split(","));
			transactionService.updateUserBalance(authDTO, transactioncodes);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/boarding/commission/credit/job/{travelDate}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> creditUserBoardingCommission(@PathVariable("authtoken") String authtoken, @PathVariable("travelDate") String travelDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(new hirondelle.date4j.DateTime(travelDate));
			transactionService.creditUserBoardingCommission(authDTO, searchDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{userCode}/balance/{referenceId}/apply", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> validateUserBalance(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode, @PathVariable("referenceId") int referenceId) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		UserDTO userDTO = new UserDTO();
		userDTO.setCode(userCode);

		UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
		userTransactionDTO.setId(referenceId);
		userTransactionDTO.setUser(userDTO);

		transactionService.updateUserBalanceMismatch(authDTO, userTransactionDTO);
		return ResponseIO.success();
	}

	public static synchronized boolean releaseConcurrentRequests(String key) {
		if (ConcurrentRequests.get(key) != null) {
			if (ConcurrentRequests.get(key) > 0) {
				ConcurrentRequests.put(key, ConcurrentRequests.get(key) - 1);
			}
		}
		return true;
	}

	public static synchronized boolean checkConcurrentRequests(String key) {
		if (ConcurrentRequests.get(key) != null && ConcurrentRequests.get(key) > 0) {
			throw new ServiceException(ErrorCode.REACHED_MAX_CONCURRENT_REQUESTS);
		}
		if (ConcurrentRequests.get(key) != null) {
			ConcurrentRequests.put(key, ConcurrentRequests.get(key) + 1);
		}
		else {
			ConcurrentRequests.put(key, 1);
		}
		return true;
	}

}

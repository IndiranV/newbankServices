package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.AcknowledgeStatusIO;
import org.in.com.controller.web.io.AuditIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.OrganizationIO;
import org.in.com.controller.web.io.PaymentReceiptIO;
import org.in.com.controller.web.io.PaymentTransactionIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.TransactionModeIO;
import org.in.com.controller.web.io.TransactionTypeIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuditDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.PaymentReceiptDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.PaymentAcknowledgeEM;
import org.in.com.dto.enumeration.PaymentReceiptTypeEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.service.PaymentTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/payment")
public class PaymentTransactionController extends BaseController {

	@Autowired
	PaymentTransactionService paymentTransactionService;

	@RequestMapping(value = "/receipts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<PaymentReceiptIO>> getPaymentReceipts(@PathVariable("authtoken") String authtoken, @RequestParam(required = true) String fromDate, @RequestParam(required = false) String toDate, @RequestParam(required = false) String userCode, @RequestParam(required = false) String paymentAcknowledgeStatus, @RequestParam(required = false) String userRoleCode) throws Exception {
		List<PaymentReceiptIO> paymentReceipts = new ArrayList<PaymentReceiptIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserDTO user = new UserDTO();
		user.setCode(userCode);

		List<PaymentReceiptDTO> list = paymentTransactionService.getPaymentReceipts(authDTO, user, fromDate, toDate, PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(paymentAcknowledgeStatus), UserRoleEM.getUserRoleEM(userRoleCode));
		for (PaymentReceiptDTO paymentReceiptDTO : list) {
			PaymentReceiptIO paymentReceiptIO = new PaymentReceiptIO();
			paymentReceiptIO.setCode(paymentReceiptDTO.getCode());
			paymentReceiptIO.setTransactionAmount(paymentReceiptDTO.getTransactionAmount());
			paymentReceiptIO.setBalanceAmount(paymentReceiptDTO.getBalanceAmount());
			paymentReceiptIO.setTransactionDate(paymentReceiptDTO.getTransactionDate());
			paymentReceiptIO.setAmountReceivedDate(paymentReceiptDTO.getAmountReceivedDate());
			paymentReceiptIO.setOpeningBalance(paymentReceiptDTO.getOpeningBalance());
			paymentReceiptIO.setClosingBalance(paymentReceiptDTO.getClosingBalance());

			BaseIO transactionModeIO = new BaseIO();
			transactionModeIO.setCode(paymentReceiptDTO.getTransactionMode().getCode());
			transactionModeIO.setName(paymentReceiptDTO.getTransactionMode().getName());
			paymentReceiptIO.setTransactionMode(transactionModeIO);

			UserIO userIO = new UserIO();
			userIO.setCode(paymentReceiptDTO.getUser().getCode());
			userIO.setName(paymentReceiptDTO.getUser().getName());
			userIO.setLastname(paymentReceiptDTO.getUser().getLastname());
			userIO.setUsername(paymentReceiptDTO.getUser().getUsername());

			OrganizationDTO organizationDTO = paymentReceiptDTO.getUser().getOrganization();
			if (organizationDTO != null) {
				OrganizationIO userOrganization = new OrganizationIO();
				userOrganization.setCode(organizationDTO.getCode());
				userOrganization.setName(organizationDTO.getName());
				userIO.setOrganization(userOrganization);
			}
			paymentReceiptIO.setUser(userIO);

			AcknowledgeStatusIO acknowledgeStatus = new AcknowledgeStatusIO();
			if (paymentReceiptDTO.getPaymentAcknowledge() != null) {
				acknowledgeStatus.setCode(paymentReceiptDTO.getPaymentAcknowledge().getCode());
				acknowledgeStatus.setName(paymentReceiptDTO.getPaymentAcknowledge().getName());
			}
			paymentReceiptIO.setPaymentAcknowledgeStatus(acknowledgeStatus);

			BaseIO paymentReceiptType = new BaseIO();
			if (paymentReceiptDTO.getPaymentReceiptType() != null) {
				paymentReceiptType.setCode(paymentReceiptDTO.getPaymentReceiptType().getCode());
				paymentReceiptType.setName(paymentReceiptDTO.getPaymentReceiptType().getName());
			}
			paymentReceiptIO.setPaymentReceiptType(paymentReceiptType);
			paymentReceiptIO.setImage(paymentReceiptDTO.getImageDetails().size());

			UserIO updatedBy = new UserIO();
			updatedBy.setCode(paymentReceiptDTO.getUpdatedBy().getCode());
			updatedBy.setName(paymentReceiptDTO.getUpdatedBy().getName());
			paymentReceiptIO.setUpdatedBy(updatedBy);

			paymentReceiptIO.setRemarks(paymentReceiptDTO.getRemarks());

			List<AuditIO> audits = new ArrayList<>();
			if (paymentReceiptDTO.getAuditLog() != null) {
				for (AuditDTO auditDTO : paymentReceiptDTO.getAuditLog()) {
					AuditIO auditIO = new AuditIO();

					auditIO.setEvent(auditDTO.getEvent());
					auditIO.setUpdatedAt(auditDTO.getUpdatedAt());

					UserIO updatedUser = new UserIO();
					updatedUser.setCode(auditDTO.getUser().getCode());
					updatedUser.setName(auditDTO.getUser().getName());
					auditIO.setUser(updatedUser);

					audits.add(auditIO);
				}
			}
			paymentReceiptIO.setAuditLog(audits);

			List<PaymentTransactionIO> paymentTransactionList = new ArrayList<PaymentTransactionIO>();
			for (PaymentTransactionDTO transactionDTO : paymentReceiptDTO.getPaymentTransactions()) {
				PaymentTransactionIO paymentTransaction = new PaymentTransactionIO();
				paymentTransaction.setCode(transactionDTO.getCode());
				paymentTransaction.setTransactionAmount(transactionDTO.getTransactionAmount());
				paymentTransaction.setTransactionDate(transactionDTO.getTransactionDate());
				paymentTransaction.setAmountReceivedDate(transactionDTO.getAmountReceivedDate());

				TransactionModeIO transactionMode = new TransactionModeIO();
				transactionMode.setCode(transactionDTO.getTransactionMode().getCode());
				transactionMode.setName(transactionDTO.getTransactionMode().getName());
				paymentTransaction.setTransactionMode(transactionMode);

				TransactionTypeIO transactionTypeO = new TransactionTypeIO();
				transactionTypeO.setCode(transactionDTO.getTransactionType().getCode());
				transactionTypeO.setName(transactionDTO.getTransactionType().getName());
				paymentTransaction.setTransactionType(transactionTypeO);

				AcknowledgeStatusIO acknowledgeStatusIO = new AcknowledgeStatusIO();
				acknowledgeStatusIO.setCode(transactionDTO.getPaymentAcknowledge().getCode());
				acknowledgeStatusIO.setName(transactionDTO.getPaymentAcknowledge().getName());
				paymentTransaction.setAcknowledgeStatus(acknowledgeStatusIO);

				UserIO user1 = new UserIO();
				user1.setCode(transactionDTO.getUser().getCode());
				user1.setName(transactionDTO.getUser().getName());
				user1.setLastname(transactionDTO.getUser().getLastname());
				user1.setUsername(transactionDTO.getUser().getUsername());
				paymentTransaction.setUser(user1);

				UserIO paymentHandled = new UserIO();
				if (transactionDTO.getPaymentHandledByUser() != null) {
					paymentHandled.setCode(transactionDTO.getPaymentHandledByUser().getCode());
					paymentHandled.setName(transactionDTO.getPaymentHandledByUser().getName());
					paymentHandled.setLastname(transactionDTO.getPaymentHandledByUser().getLastname());
					paymentHandled.setUsername(transactionDTO.getPaymentHandledByUser().getUsername());
				}
				paymentTransaction.setPaymentHandledBy(paymentHandled);

				paymentTransaction.setRemarks(transactionDTO.getRemarks());

				List<PaymentTransactionIO> partialPaymentTransactionList = new ArrayList<PaymentTransactionIO>();
				for (PaymentTransactionDTO partialTransactionDTO : transactionDTO.getPartialPaymentPaidList()) {
					PaymentTransactionIO partialPaymentTransaction = new PaymentTransactionIO();
					partialPaymentTransaction.setCode(partialTransactionDTO.getCode());
					partialPaymentTransaction.setTransactionAmount(partialTransactionDTO.getTransactionAmount());
					partialPaymentTransaction.setTransactionDate(partialTransactionDTO.getTransactionDate());
					partialPaymentTransaction.setAmountReceivedDate(partialTransactionDTO.getAmountReceivedDate());

					TransactionModeIO partialTransactionMode = new TransactionModeIO();
					partialTransactionMode.setCode(partialTransactionDTO.getTransactionMode().getCode());
					partialTransactionMode.setName(partialTransactionDTO.getTransactionMode().getName());
					partialPaymentTransaction.setTransactionMode(partialTransactionMode);

					TransactionTypeIO transactionTypeIO = new TransactionTypeIO();
					transactionTypeIO.setCode(partialTransactionDTO.getTransactionType().getCode());
					transactionTypeIO.setName(partialTransactionDTO.getTransactionType().getName());
					partialPaymentTransaction.setTransactionType(transactionTypeIO);

					AcknowledgeStatusIO partialAcknowledgeStatus = new AcknowledgeStatusIO();
					partialAcknowledgeStatus.setCode(partialTransactionDTO.getPaymentAcknowledge().getCode());
					partialAcknowledgeStatus.setName(partialTransactionDTO.getPaymentAcknowledge().getName());
					partialPaymentTransaction.setAcknowledgeStatus(partialAcknowledgeStatus);

					UserIO partilalUser = new UserIO();
					partilalUser.setCode(partialTransactionDTO.getUser().getCode());
					partilalUser.setName(partialTransactionDTO.getUser().getName());
					partilalUser.setLastname(partialTransactionDTO.getUser().getLastname());
					partilalUser.setUsername(partialTransactionDTO.getUser().getUsername());
					partialPaymentTransaction.setUser(partilalUser);

					UserIO paymentHandledIO = new UserIO();
					paymentHandledIO.setCode(partialTransactionDTO.getPaymentHandledByUser().getCode());
					paymentHandledIO.setName(partialTransactionDTO.getPaymentHandledByUser().getName());
					paymentHandledIO.setLastname(partialTransactionDTO.getPaymentHandledByUser().getLastname());
					paymentHandledIO.setUsername(partialTransactionDTO.getPaymentHandledByUser().getUsername());
					partialPaymentTransaction.setPaymentHandledBy(paymentHandledIO);

					partialPaymentTransaction.setRemarks(partialTransactionDTO.getRemarks());
					partialPaymentTransactionList.add(partialPaymentTransaction);
				}
				paymentTransaction.setPartialPaymentList(partialPaymentTransactionList);

				paymentTransactionList.add(paymentTransaction);
			}
			paymentReceiptIO.setPaymentTransactions(paymentTransactionList);

			paymentReceipts.add(paymentReceiptIO);
		}
		return ResponseIO.success(paymentReceipts);
	}

	@RequestMapping(value = "/receipt/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> savePaymentReceipt(@PathVariable("authtoken") String authtoken, @RequestBody PaymentReceiptIO paymentReceiptIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		PaymentReceiptDTO paymentReceiptDTO = new PaymentReceiptDTO();
		paymentReceiptDTO.setTransactionAmount(paymentReceiptIO.getTransactionAmount());
		paymentReceiptDTO.setBalanceAmount(paymentReceiptIO.getTransactionAmount());
		paymentReceiptDTO.setTransactionDate(paymentReceiptIO.getTransactionDate());
		paymentReceiptDTO.setAmountReceivedDate(paymentReceiptIO.getAmountReceivedDate());
		paymentReceiptDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(paymentReceiptIO.getTransactionMode().getCode()));
		paymentReceiptDTO.setPaymentAcknowledge(paymentReceiptIO.getPaymentAcknowledgeStatus() != null ? PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(paymentReceiptIO.getPaymentAcknowledgeStatus().getCode()) : PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED);
		paymentReceiptDTO.setPaymentReceiptType(paymentReceiptIO.getPaymentReceiptType() != null ? PaymentReceiptTypeEM.getPaymentReceiptType(paymentReceiptIO.getPaymentReceiptType().getCode()) : PaymentReceiptTypeEM.COLLECTION);
		paymentReceiptDTO.setRemarks(paymentReceiptIO.getRemarks());

		UserDTO user = new UserDTO();
		user.setCode(paymentReceiptIO.getUser().getCode());
		paymentReceiptDTO.setUser(user);

		paymentTransactionService.savePaymentReceipt(authDTO, paymentReceiptDTO);
		BaseIO baseIO = new BaseIO();
		baseIO.setCode(paymentReceiptDTO.getCode());
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/receipt/status/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updatePaymentReceiptStatus(@PathVariable("authtoken") String authtoken, @RequestBody PaymentReceiptIO paymentReceiptIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		PaymentReceiptDTO paymentReceiptDTO = new PaymentReceiptDTO();
		paymentReceiptDTO.setCode(paymentReceiptIO.getCode());
		paymentReceiptDTO.setPaymentAcknowledge(paymentReceiptIO.getPaymentAcknowledgeStatus() != null ? PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(paymentReceiptIO.getPaymentAcknowledgeStatus().getCode()) : PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED);
		paymentReceiptDTO.setRemarks(paymentReceiptIO.getRemarks());

		paymentTransactionService.updatePaymentReceipt(authDTO, paymentReceiptDTO);

		BaseIO baseIO = new BaseIO();
		baseIO.setCode(paymentReceiptDTO.getCode());
		return ResponseIO.success(baseIO);
	}

}

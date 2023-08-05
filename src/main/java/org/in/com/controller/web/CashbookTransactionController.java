package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.CashbookTransactionIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CashbookTransactionDTO;
import org.in.com.dto.CashbookTypeDTO;
import org.in.com.dto.CashbookVendorDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.CashbookAckStatusEM;
import org.in.com.dto.enumeration.CashbookCategoryEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.CashbookTransactionService;
import org.in.com.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/cashbook/transaction")
public class CashbookTransactionController extends BaseController {
	@Autowired
	CashbookTransactionService cashbookTransactionService;

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<CashbookTransactionIO> updateCashbookTransaction(@PathVariable("authtoken") String authtoken, @RequestBody CashbookTransactionIO cashbookTransaction) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		CashbookTransactionIO cashbookTransactionIO = new CashbookTransactionIO();
		CashbookTransactionDTO cashbookTransactionDTO = new CashbookTransactionDTO();
		cashbookTransactionDTO.setCode(cashbookTransaction.getCode());
		cashbookTransactionDTO.setTransactionDate(DateUtil.getDateTime(cashbookTransaction.getTransactionDate()));
		cashbookTransactionDTO.setCashbookCategory(CashbookCategoryEM.getCashbookCategoryEM(cashbookTransaction.getCashbookCategory().getCode()));
		cashbookTransactionDTO.setReferenceCode(cashbookTransaction.getReferenceCode());

		CashbookTypeDTO cashbookType = new CashbookTypeDTO();
		cashbookType.setCode(cashbookTransaction.getCashbookType().getCode());
		cashbookTransactionDTO.setCashbookType(cashbookType);

		cashbookTransactionDTO.setTransactionType(cashbookTransaction.getTransactionType());
		cashbookTransactionDTO.setAmount(cashbookTransaction.getAmount());

		UserDTO user = new UserDTO();
		user.setCode(cashbookTransaction.getUser().getCode());
		cashbookTransactionDTO.setUser(user);

		CashbookVendorDTO cashbookVendor = new CashbookVendorDTO();
		cashbookVendor.setCode(cashbookTransaction.getCashbookVendor().getCode());
		cashbookTransactionDTO.setCashbookVendor(cashbookVendor);

		cashbookTransactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(cashbookTransaction.getTransactionMode().getCode()));
		cashbookTransactionDTO.setAcknowledgeStatus(CashbookAckStatusEM.getCashbookAckStatusEM(cashbookTransaction.getAcknowledgeStatus().getCode()));
		cashbookTransactionDTO.setPaymentStatusFlag(cashbookTransaction.getPaymentStatusFlag());
		cashbookTransactionDTO.setRemarks(cashbookTransaction.getRemarks());
		cashbookTransactionDTO.setActiveFlag(cashbookTransaction.getActiveFlag());

		cashbookTransactionService.updateCashBookTransaction(authDTO, cashbookTransactionDTO);

		cashbookTransactionIO.setCode(cashbookTransactionDTO.getCode());
		cashbookTransactionIO.setActiveFlag(cashbookTransactionDTO.getActiveFlag());
		return ResponseIO.success(cashbookTransactionIO);
	}

	@RequestMapping(value = "/update/status", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateCashbookTransactionStatus(@PathVariable("authtoken") String authtoken, @RequestBody CashbookTransactionIO cashbookTransaction) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<CashbookTransactionDTO> cashbookTransactions = new ArrayList<CashbookTransactionDTO>();
		CashbookAckStatusEM cashbookAckStatusEM = CashbookAckStatusEM.getCashbookAckStatusEM(cashbookTransaction.getAcknowledgeStatus().getCode());
		if (cashbookAckStatusEM == null) {
			throw new ServiceException(ErrorCode.INVALID_CODE, "Invalid Acknowledge Status Code!");
		}
		for (String code : cashbookTransaction.getCode().split(",")) {
			CashbookTransactionDTO cashbookTransactionDTO = new CashbookTransactionDTO();
			cashbookTransactionDTO.setCode(code);
			cashbookTransactionDTO.setAcknowledgeStatus(cashbookAckStatusEM);
			cashbookTransactions.add(cashbookTransactionDTO);
		}
		cashbookTransactionService.updateCashbookTransactionStatus(authDTO, cashbookTransactions, cashbookAckStatusEM);
		return ResponseIO.success();
	}

}

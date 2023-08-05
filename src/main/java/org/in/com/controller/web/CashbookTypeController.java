package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.CashbookTypeIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CashbookTypeDTO;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.service.CashbookTypeService;
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
@RequestMapping("/{authtoken}/cashbook/type")
public class CashbookTypeController extends BaseController {
	@Autowired
	CashbookTypeService cashbookTypeService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<CashbookTypeIO>> getCashbookTypes(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<CashbookTypeIO> cashbookTypes = new ArrayList<CashbookTypeIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<CashbookTypeDTO> list = cashbookTypeService.getAll(authDTO);

			for (CashbookTypeDTO bookTypeDTO : list) {
				if (activeFlag != -1 && activeFlag != bookTypeDTO.getActiveFlag()) {
					continue;
				}
				CashbookTypeIO cashbookTypeIO = new CashbookTypeIO();
				cashbookTypeIO.setCode(bookTypeDTO.getCode());
				cashbookTypeIO.setName(bookTypeDTO.getName());
				cashbookTypeIO.setTransactionType(bookTypeDTO.getTransactionType());

				BaseIO transactionMode = new BaseIO();
				transactionMode.setCode(bookTypeDTO.getTransactionMode().getCode());
				transactionMode.setName(bookTypeDTO.getTransactionMode().getName());
				cashbookTypeIO.setTransactionMode(transactionMode);

				cashbookTypeIO.setActiveFlag(bookTypeDTO.getActiveFlag());
				cashbookTypes.add(cashbookTypeIO);
			}

		}
		return ResponseIO.success(cashbookTypes);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<CashbookTypeIO> updateCashbookType(@PathVariable("authtoken") String authtoken, @RequestBody CashbookTypeIO cashbookTypeIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		CashbookTypeIO cashbookType = new CashbookTypeIO();
		if (authDTO != null) {
			CashbookTypeDTO cashbookTypeDTO = new CashbookTypeDTO();
			cashbookTypeDTO.setCode(cashbookTypeIO.getCode());
			cashbookTypeDTO.setName(cashbookTypeIO.getName());
			cashbookTypeDTO.setTransactionType(cashbookTypeIO.getTransactionType());
			cashbookTypeDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(cashbookTypeIO.getTransactionMode().getCode()));
			cashbookTypeDTO.setActiveFlag(cashbookTypeIO.getActiveFlag());

			cashbookTypeService.Update(authDTO, cashbookTypeDTO);

			cashbookType.setCode(cashbookTypeDTO.getCode());
			cashbookType.setActiveFlag(cashbookTypeDTO.getActiveFlag());
		}
		return ResponseIO.success(cashbookType);
	}

}

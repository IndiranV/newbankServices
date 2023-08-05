package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.CashbookVendorIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CashbookVendorDTO;
import org.in.com.service.CashbookVendorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/cashbook/vendor")
public class CashbookVendorController extends BaseController {
	@Autowired
	CashbookVendorService cashbookVendorService;

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<CashbookVendorIO> updateCashbookVendor(@PathVariable("authtoken") String authtoken, @RequestBody CashbookVendorIO bookVendorIO) throws Exception {
		CashbookVendorIO cashbookVendor = new CashbookVendorIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		CashbookVendorDTO customerVendorDTO = new CashbookVendorDTO();
		customerVendorDTO.setCode(bookVendorIO.getCode());
		customerVendorDTO.setName(bookVendorIO.getName());
		customerVendorDTO.setMobileNumber(bookVendorIO.getMobileNumber());
		customerVendorDTO.setAddress(bookVendorIO.getAddress());
		customerVendorDTO.setEmail(bookVendorIO.getEmail());
		customerVendorDTO.setBankDetails(bookVendorIO.getBankDetails());
		customerVendorDTO.setActiveFlag(bookVendorIO.getActiveFlag());
		cashbookVendorService.Update(authDTO, customerVendorDTO);
		cashbookVendor.setCode(customerVendorDTO.getCode());
		cashbookVendor.setActiveFlag(customerVendorDTO.getActiveFlag());
		return ResponseIO.success(cashbookVendor);
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<CashbookVendorIO>> getCashbookVendors(@PathVariable("authtoken") String authtoken) throws Exception {
		List<CashbookVendorIO> customerList = new ArrayList<CashbookVendorIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<CashbookVendorDTO> list = cashbookVendorService.getAll(authDTO);

		for (CashbookVendorDTO cashbookVendorDTO : list) {
			CashbookVendorIO cashbookVendorIO = new CashbookVendorIO();
			cashbookVendorIO.setCode(cashbookVendorDTO.getCode());
			cashbookVendorIO.setName(cashbookVendorDTO.getName());
			cashbookVendorIO.setMobileNumber(cashbookVendorDTO.getMobileNumber());
			cashbookVendorIO.setAddress(cashbookVendorDTO.getAddress());
			cashbookVendorIO.setEmail(cashbookVendorDTO.getEmail());
			cashbookVendorIO.setBankDetails(cashbookVendorDTO.getBankDetails());
			cashbookVendorIO.setActiveFlag(cashbookVendorDTO.getActiveFlag());
			customerList.add(cashbookVendorIO);
		}
		return ResponseIO.success(customerList);
	}
}

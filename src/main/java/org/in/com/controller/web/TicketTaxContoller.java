package org.in.com.controller.web;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.NamespaceTaxIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.controller.web.io.TicketTaxIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketTaxDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.TicketTaxService;
import org.in.com.utils.GSTINValidator;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/ticket/tax")
public class TicketTaxContoller extends BaseController {
	@Autowired
	TicketTaxService ticketTaxService;

	@RequestMapping(value = "/{ticketCode}/tax/details", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TicketTaxIO> getTicketTaxDetails(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		TicketTaxIO ticketTax = new TicketTaxIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);

		TicketTaxDTO ticketTaxDTO = ticketTaxService.getTicketTax(authDTO, ticketDTO);
		ticketTax.setTradeName(ticketTaxDTO.getTradeName());
		ticketTax.setGstin(ticketTaxDTO.getGstin());
		ticketTax.setEmail(ticketTaxDTO.getEmail());
		ticketTax.setActiveFlag(ticketTaxDTO.getActiveFlag());

		return ResponseIO.success(ticketTax);
	}

	@RequestMapping(value = "/{ticketCode}/tax/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateTicketTax(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, @RequestBody TicketTaxIO ticketTaxIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (StringUtil.isNull(ticketTaxIO.getGstin()) || !GSTINValidator.validGSTIN(ticketTaxIO.getGstin())) {
			throw new ServiceException(ErrorCode.INVALID_GSTIN);
		}

		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);

		TicketTaxDTO ticketTaxDTO = new TicketTaxDTO();
		ticketTaxDTO.setTradeName(ticketTaxIO.getTradeName());
		ticketTaxDTO.setGstin(ticketTaxIO.getGstin());
		ticketTaxDTO.setEmail(ticketTaxIO.getEmail());

		ticketTaxService.updateTicketTax(authDTO, ticketDTO, ticketTaxDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{gstin}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> getTax(@PathVariable("authtoken") String authtoken, @PathVariable("gstin") String gstin) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		NamespaceTaxDTO taxDTO = ticketTaxService.getTaxByGstin(authDTO, gstin);

		BaseIO state = new BaseIO();
		state.setCode(taxDTO.getState().getCode());
		state.setName(taxDTO.getState().getName());
		state.setActiveFlag(taxDTO.getActiveFlag());
		return ResponseIO.success(state);
	}

}

package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.NamespaceTaxIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.enumeration.ProductTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NamespaceTaxService;
import org.in.com.utils.StringUtil;
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
@RequestMapping("/{authtoken}/tax")
public class NamespaceTaxController extends BaseController {
	@Autowired
	NamespaceTaxService taxService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<NamespaceTaxIO>> getAllTax(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "1") int activeFlag) throws Exception {
		List<NamespaceTaxIO> list = new ArrayList<NamespaceTaxIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<NamespaceTaxDTO> taxList = taxService.getAll(authDTO);
			for (NamespaceTaxDTO taxDTO : taxList) {
				if (activeFlag != -1 && activeFlag != taxDTO.getActiveFlag()) {
					continue;
				}
				NamespaceTaxIO tax = new NamespaceTaxIO();
				tax.setCode(taxDTO.getCode());
				tax.setName(taxDTO.getName());
				tax.setTradeName(taxDTO.getTradeName());
				tax.setGstin(taxDTO.getGstin());
				tax.setCgstValue(taxDTO.getCgstValue());
				tax.setSgstValue(taxDTO.getSgstValue());
				tax.setUgstValue(taxDTO.getUgstValue());
				tax.setIgstValue(taxDTO.getIgstValue());
				tax.setSacNumber(taxDTO.getSacNumber());

				StateIO state = new StateIO();
				state.setCode(taxDTO.getState().getCode());
				state.setName(taxDTO.getState().getName());
				tax.setState(state);

				BaseIO productType = new BaseIO();
				productType.setCode(taxDTO.getProductType().getCode());
				productType.setName(taxDTO.getProductType().getName());
				tax.setProductType(productType);

				tax.setActiveFlag(taxDTO.getActiveFlag());
				list.add(tax);
			}
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/{code}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<NamespaceTaxIO> getTax(@PathVariable("authtoken") String authtoken, @PathVariable("code") String code) throws Exception {
		NamespaceTaxIO tax = new NamespaceTaxIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			NamespaceTaxDTO taxDTO = new NamespaceTaxDTO();
			taxDTO.setCode(code);
			taxDTO = taxService.getTax(authDTO, taxDTO);
			tax.setCode(taxDTO.getCode());
			tax.setName(taxDTO.getName());
			tax.setTradeName(taxDTO.getTradeName());
			tax.setGstin(taxDTO.getGstin());
			tax.setCgstValue(taxDTO.getCgstValue());
			tax.setSgstValue(taxDTO.getSgstValue());
			tax.setUgstValue(taxDTO.getUgstValue());
			tax.setIgstValue(taxDTO.getIgstValue());
			tax.setSacNumber(taxDTO.getSacNumber());

			StateIO state = new StateIO();
			state.setCode(taxDTO.getState() != null ? taxDTO.getState().getCode() : null);
			state.setName(taxDTO.getState() != null ? taxDTO.getState().getName() : null);
			tax.setState(state);

			BaseIO productType = new BaseIO();
			productType.setCode(taxDTO.getProductType().getCode());
			productType.setName(taxDTO.getProductType().getName());
			tax.setProductType(productType);

			tax.setActiveFlag(taxDTO.getActiveFlag());
		}
		return ResponseIO.success(tax);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<NamespaceTaxIO> getUpdateUID(@PathVariable("authtoken") String authtoken, @RequestBody NamespaceTaxIO tax) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (tax.getActiveFlag() == 1 & (StringUtil.isNull(tax.getName()) || StringUtil.isNull(tax.getTradeName()) || StringUtil.isNull(tax.getGstin()) || tax.getState() == null || StringUtil.isNull(tax.getState().getCode()))) {
				throw new ServiceException(ErrorCode.INVALID_GSTIN, "Required data not Found");
			}
			NamespaceTaxDTO taxDTO = new NamespaceTaxDTO();
			taxDTO.setCode(tax.getCode());
			taxDTO.setName(tax.getName());
			taxDTO.setTradeName(tax.getTradeName());
			taxDTO.setGstin(tax.getGstin());
			taxDTO.setCgstValue(tax.getCgstValue());
			taxDTO.setSgstValue(tax.getSgstValue());
			taxDTO.setUgstValue(tax.getUgstValue());
			taxDTO.setIgstValue(tax.getIgstValue());
			taxDTO.setSacNumber(tax.getSacNumber());

			StateDTO stateDTO = new StateDTO();
			stateDTO.setCode(tax.getState().getCode());
			taxDTO.setState(stateDTO);

			taxDTO.setProductType(ProductTypeEM.getProductTypeEM(tax.getProductType() != null ? tax.getProductType().getCode() : Text.EMPTY));
			taxDTO.setActiveFlag(tax.getActiveFlag());
			taxService.Update(authDTO, taxDTO);
			tax.setCode(taxDTO.getCode());
			tax.setActiveFlag(taxDTO.getActiveFlag());
		}
		return ResponseIO.success(tax);
	}
}

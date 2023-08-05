package org.in.com.controller.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.CancellationPolicyIO;
import org.in.com.controller.web.io.CancellationTermIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CancellationPolicyDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.CancellationTermsService;
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
@RequestMapping("/{authtoken}/cancellations")
public class CancellationController extends BaseController {
	@Autowired
	CancellationTermsService termsService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<CancellationTermIO>> getCancellationTerms(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<CancellationTermIO> termIOs = new ArrayList<CancellationTermIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<CancellationTermDTO> list = (List<CancellationTermDTO>) termsService.getAll(authDTO);
			for (CancellationTermDTO dto : list) {
				if (activeFlag != -1 && activeFlag != dto.getActiveFlag()) {
					continue;
				}
				CancellationTermIO termIO = new CancellationTermIO();
				termIO.setName(dto.getName());
				termIO.setCode(dto.getCode());
				termIO.setActiveFlag(dto.getActiveFlag());
				List<CancellationPolicyIO> policyIOs = new ArrayList<CancellationPolicyIO>();
				for (CancellationPolicyDTO policyDTO : dto.getPolicyList()) {
					CancellationPolicyIO policyIO = new CancellationPolicyIO();
					policyIO.setFromValue(policyDTO.getFromValue());
					policyIO.setToValue(policyDTO.getToValue());
					policyIO.setDeductionAmount(policyDTO.getDeductionValue());
					policyIO.setPercentageFlag(policyDTO.getPercentageFlag());
					policyIO.setPolicyPattern(policyDTO.getPolicyPattern());
					policyIOs.add(policyIO);
				}
				termIO.setPolicyList(policyIOs);
				termIOs.add(termIO);
			}
		}
		return ResponseIO.success(termIOs);
	}

	@RequestMapping(value = "/{cancellationtermsCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<CancellationTermIO>> getCancellationTerms(@PathVariable("authtoken") String authtoken, @PathVariable("cancellationtermsCode") String cancellationtermsCode) throws Exception {
		List<CancellationTermIO> termIOs = new ArrayList<CancellationTermIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			CancellationTermDTO cancellationTermDTO = new CancellationTermDTO();
			List<CancellationTermDTO> list = (List<CancellationTermDTO>) termsService.get(authDTO, cancellationTermDTO);
			for (CancellationTermDTO dto : list) {
				CancellationTermIO termIO = new CancellationTermIO();
				termIO.setName(dto.getName());
				termIO.setCode(dto.getCode());
				termIO.setActiveFlag(dto.getActiveFlag());
				List<CancellationPolicyIO> policyIOs = new ArrayList<CancellationPolicyIO>();
				for (CancellationPolicyDTO policyDTO : dto.getPolicyList()) {
					CancellationPolicyIO policyIO = new CancellationPolicyIO();
					policyIO.setFromValue(policyDTO.getFromValue());
					policyIO.setToValue(policyDTO.getToValue());
					policyIO.setDeductionAmount(policyDTO.getDeductionValue());
					policyIO.setPercentageFlag(policyDTO.getPercentageFlag());
					policyIO.setPolicyPattern(policyDTO.getPolicyPattern());
					policyIOs.add(policyIO);
				}
				termIO.setPolicyList(policyIOs);
				termIOs.add(termIO);
			}
		}
		return ResponseIO.success(termIOs);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<CancellationTermIO> updateCancellationTermsUID(@PathVariable("authtoken") String authtoken, @RequestBody CancellationTermIO termIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		CancellationTermIO termIO2 = new CancellationTermIO();
		if (authDTO != null) {
			CancellationTermDTO termDTO = new CancellationTermDTO();
			List<CancellationPolicyDTO> policyList = new ArrayList<>();
			termDTO.setCode(termIO.getCode());
			termDTO.setName(termIO.getName());
			termDTO.setActiveFlag(termIO.getActiveFlag());

			if (termIO.getActiveFlag() == 1 && termIO.getPolicyList() != null) {
				if (termIO.getPolicyList().size() < 3) {
					throw new ServiceException(ErrorCode.MIN_THREE_POLICY_ALLOWED);
				}
				int previousPolicyToValue = 0;
				String previousPolicyPattern = null;
				for (CancellationPolicyIO policyIO : termIO.getPolicyList()) {
					CancellationPolicyDTO policyDTO = new CancellationPolicyDTO();
					policyDTO.setFromValue(policyIO.getFromValue());
					policyDTO.setToValue(policyIO.getToValue());
					policyDTO.setDeductionValue(policyIO.getDeductionAmount() == null ? BigDecimal.ZERO : policyIO.getDeductionAmount());
					policyDTO.setPercentageFlag(policyIO.getPercentageFlag());
					policyDTO.setPolicyPattern(policyIO.getPolicyPattern());

					if (previousPolicyToValue != 0 && StringUtil.isNotNull(previousPolicyPattern) && previousPolicyToValue != policyDTO.getFromValue()) {
						throw new ServiceException(ErrorCode.POLICY_TIME_CONTINUITY_MISMATCHED);
					}
					// if Deduction Value -1, that's no cancellation, percentage flag should be 1
					if (policyDTO.getDeductionValue().intValue() == -1) {
						policyDTO.setPercentageFlag(1);
					}
					previousPolicyToValue = policyDTO.getToValue();
					previousPolicyPattern = policyDTO.getPolicyPattern();
					policyList.add(policyDTO);
				}
			}
			termDTO.setPolicyList(policyList);
			termsService.Update(authDTO, termDTO);
			termIO2.setCode(termDTO.getCode());
			termIO2.setActiveFlag(termDTO.getActiveFlag());
		}
		return ResponseIO.success(termIO2);
	}

}

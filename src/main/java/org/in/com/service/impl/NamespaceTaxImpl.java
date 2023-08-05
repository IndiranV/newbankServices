package org.in.com.service.impl;

import java.util.List;

import org.in.com.cache.TaxCache;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Numeric;
import org.in.com.dao.NamespaceTaxDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.StateDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.NamespaceTaxService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.GSTINValidator;
import org.springframework.stereotype.Service;

@Service
public class NamespaceTaxImpl extends TaxCache implements NamespaceTaxService {

	@Override
	public List<NamespaceTaxDTO> get(AuthDTO authDTO, NamespaceTaxDTO dto) {
		return null;
	}

	@Override
	public List<NamespaceTaxDTO> getAll(AuthDTO authDTO) {
		NamespaceTaxDAO taxDAO = new NamespaceTaxDAO();
		return taxDAO.getAll(authDTO);
	}

	@Override
	public NamespaceTaxDTO Update(AuthDTO authDTO, NamespaceTaxDTO taxDTO) {
		if (!authDTO.getNativeNamespaceCode().equals(ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		NamespaceTaxDAO taxDAO = new NamespaceTaxDAO();
		if (taxDTO.getActiveFlag() == 1 && !GSTINValidator.validGSTIN(taxDTO.getGstin())) {
			throw new ServiceException(ErrorCode.INVALID_GSTIN);
		}
		// check same tax is exist 
		if (taxDTO.getActiveFlag() == 1) {
			validateNamespaceTax(authDTO, taxDTO);
		}
		// Refresh cache
		removegetNamespaceTaxDTO(authDTO, taxDTO);
		return taxDAO.Update(authDTO, taxDTO);
	}

	@Override
	public NamespaceTaxDTO getTax(AuthDTO authDTO, NamespaceTaxDTO taxDTO) {
		NamespaceTaxDAO taxDAO = new NamespaceTaxDAO();
		return taxDAO.getTax(authDTO, taxDTO);
	}

	@Override
	public NamespaceTaxDTO getTaxbyId(AuthDTO authDTO, NamespaceTaxDTO taxDTO) {
		if (taxDTO.getId() != 0) {
			taxDTO = getNamespaceTaxbyId(authDTO, taxDTO);
		}
		return taxDTO;
	}

	public NamespaceTaxDTO getTaxbyState(AuthDTO authDTO, StateDTO stateDTO) {
		List<NamespaceTaxDTO> taxList = getNamespaceTaxbyStateCode(authDTO, stateDTO);
		return taxList.isEmpty() ? new NamespaceTaxDTO() : taxList.get(Numeric.ZERO_INT);
	}

	public NamespaceTaxDTO getTaxbyStateV2(AuthDTO authDTO, NamespaceTaxDTO tax, StateDTO state) {
		List<NamespaceTaxDTO> taxList = getNamespaceTaxbyStateCode(authDTO, state);
		return BitsUtil.getNamespaceTax(taxList, tax);
	}

	private void validateNamespaceTax(AuthDTO authDTO, NamespaceTaxDTO taxDTO) {
		NamespaceTaxDAO taxDAO = new NamespaceTaxDAO();
		boolean isFound = taxDAO.checkNamespaceTaxFound(authDTO, taxDTO.getState(), taxDTO.getProductType());
		if (isFound) {
			throw new ServiceException(ErrorCode.SAME_GST_IS_EXIST);
		}
	}
}

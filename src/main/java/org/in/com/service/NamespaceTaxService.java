package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.StateDTO;

public interface NamespaceTaxService extends BaseService<NamespaceTaxDTO> {
	public NamespaceTaxDTO getTax(AuthDTO authDTO, NamespaceTaxDTO taxDTO);

	public NamespaceTaxDTO getTaxbyId(AuthDTO authDTO, NamespaceTaxDTO taxDTO);

	public NamespaceTaxDTO getTaxbyState(AuthDTO authDTO, StateDTO stateDTO);

	public NamespaceTaxDTO getTaxbyStateV2(AuthDTO authDTO, NamespaceTaxDTO tax, StateDTO state);

}

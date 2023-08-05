package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.CashbookTypeDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CashbookTypeDTO;
import org.in.com.service.CashbookTypeService;
import org.springframework.stereotype.Service;

@Service
public class CashbookTypeServiceImpl implements CashbookTypeService {

	@Override
	public List<CashbookTypeDTO> get(AuthDTO authDTO, CashbookTypeDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<CashbookTypeDTO> getAll(AuthDTO authDTO) {
		CashbookTypeDAO dao = new CashbookTypeDAO();
		return dao.getCashbookTypes(authDTO);
	}

	@Override
	public CashbookTypeDTO Update(AuthDTO authDTO, CashbookTypeDTO dto) {
		CashbookTypeDAO dao = new CashbookTypeDAO();
		dao.updateCashbookType(authDTO, dto);
		return dto;
	}
}

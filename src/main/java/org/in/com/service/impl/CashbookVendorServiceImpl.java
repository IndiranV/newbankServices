package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.CashbookVendorDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CashbookVendorDTO;
import org.in.com.service.CashbookVendorService;
import org.springframework.stereotype.Service;

@Service
public class CashbookVendorServiceImpl implements CashbookVendorService {

	@Override
	public List<CashbookVendorDTO> get(AuthDTO authDTO, CashbookVendorDTO dto) {
		CashbookVendorDAO cashbookVendorDAO = new CashbookVendorDAO();
		cashbookVendorDAO.getCashbookVendor(authDTO, dto);
		return null;
	}

	@Override
	public List<CashbookVendorDTO> getAll(AuthDTO authDTO) {
		CashbookVendorDAO cashbookVendorDAO = new CashbookVendorDAO();
		return cashbookVendorDAO.getCashbookVendors(authDTO);
	}

	@Override
	public CashbookVendorDTO Update(AuthDTO authDTO, CashbookVendorDTO dto) {
		CashbookVendorDAO cashbookVendorDAO = new CashbookVendorDAO();
		cashbookVendorDAO.updateCashbookVendor(authDTO, dto);
		return dto;
	}

}

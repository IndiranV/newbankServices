package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.PaymentModeDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.PaymentModeDTO;
import org.in.com.service.PaymentModeService;
import org.springframework.stereotype.Service;

@Service
public class PaymentModeImpl implements PaymentModeService {

	
	public List<PaymentModeDTO> get(AuthDTO authDTO, PaymentModeDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}

	
	public List<PaymentModeDTO> getAll(AuthDTO authDTO) {
		PaymentModeDAO dao = new PaymentModeDAO();
		return dao.getAllPaymentMode();
	}

	
	public PaymentModeDTO Update(AuthDTO authDTO, PaymentModeDTO dto) {
		PaymentModeDAO dao = new PaymentModeDAO();
		return dao.getPaymentModeUpdate(authDTO, dto);
	}

}

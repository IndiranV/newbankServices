package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.PaymentGatewayProviderDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.PaymentGatewayProviderDTO;
import org.in.com.service.PaymentGatewayProviderService;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayProviderImpl implements PaymentGatewayProviderService {

	public List<PaymentGatewayProviderDTO> get(AuthDTO authDTO, PaymentGatewayProviderDTO pgModeDTO) {
		return null;
	}

	public List<PaymentGatewayProviderDTO> getAll(AuthDTO authDTO) {
		PaymentGatewayProviderDAO dao = new PaymentGatewayProviderDAO();
		return dao.getAllPgProvider();
	}

	public PaymentGatewayProviderDTO Update(AuthDTO authDTO, PaymentGatewayProviderDTO pgModeDTO) {
		PaymentGatewayProviderDAO dao = new PaymentGatewayProviderDAO();
		return dao.getPgProviderUpdate(authDTO, pgModeDTO);
	}
}

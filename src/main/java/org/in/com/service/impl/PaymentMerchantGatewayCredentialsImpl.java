package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.PaymentMerchantGatewayCredentialsDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.PaymentGatewayCredentialsDTO;
import org.in.com.service.PaymentMerchantGatewayCredentialsService;
import org.springframework.stereotype.Service;

@Service
public class PaymentMerchantGatewayCredentialsImpl implements PaymentMerchantGatewayCredentialsService {

	public List<PaymentGatewayCredentialsDTO> get(AuthDTO authDTO, PaymentGatewayCredentialsDTO dto) {
		PaymentMerchantGatewayCredentialsDAO dao = new PaymentMerchantGatewayCredentialsDAO();
		return dao.getPgCredentials(authDTO, dto);
	}

	public List<PaymentGatewayCredentialsDTO> getAll(AuthDTO authDTO) {
		PaymentMerchantGatewayCredentialsDAO dao = new PaymentMerchantGatewayCredentialsDAO();
		return dao.getallPgCredentials(authDTO);
	}

	public PaymentGatewayCredentialsDTO Update(AuthDTO authDTO, PaymentGatewayCredentialsDTO dto) {
		PaymentMerchantGatewayCredentialsDAO dao = new PaymentMerchantGatewayCredentialsDAO();
		return dao.updatePgMerchantCredentails(authDTO, dto);
	}

}

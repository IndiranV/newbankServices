package org.in.com.service.impl;

import java.util.List;

import org.in.com.dao.PaymentGatewayPartnerDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.PaymentGatewayPartnerDTO;
import org.in.com.service.PaymentGatewayPartnerService;
import org.springframework.stereotype.Service;

@Service
public class PaymentGatewayPartnerImpl implements PaymentGatewayPartnerService {

	public List<PaymentGatewayPartnerDTO> get(AuthDTO authDTO, PaymentGatewayPartnerDTO gatewayPartnerDTO) {
		return null;
	}

	public List<PaymentGatewayPartnerDTO> getAll(AuthDTO authDTO) {
		PaymentGatewayPartnerDAO dao = new PaymentGatewayPartnerDAO();
		return dao.getAllPgPartner(authDTO);
	}

	public PaymentGatewayPartnerDTO Update(AuthDTO authDTO, PaymentGatewayPartnerDTO dto) {
		PaymentGatewayPartnerDAO dao = new PaymentGatewayPartnerDAO();
		dao.getPgPartnerUpdate(authDTO, dto);
		return dto;
	}

}

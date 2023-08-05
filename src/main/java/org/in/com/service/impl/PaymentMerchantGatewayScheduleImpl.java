package org.in.com.service.impl;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import org.in.com.dao.PaymentMerchantGatewayScheduleDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderInitRequestDTO;
import org.in.com.dto.PaymentGatewayScheduleDTO;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.service.PaymentMerchantGatewayScheduleService;
import org.in.com.utils.BitsUtil;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class PaymentMerchantGatewayScheduleImpl implements PaymentMerchantGatewayScheduleService {

	public List<PaymentGatewayScheduleDTO> get(AuthDTO authDTO, PaymentGatewayScheduleDTO PaymentMerchantGatewayScheduleDTO) {
		return null;
	}

	public List<PaymentGatewayScheduleDTO> getAll(AuthDTO authDTO) {
		PaymentMerchantGatewayScheduleDAO dao = new PaymentMerchantGatewayScheduleDAO();
		return dao.getAllPgMerchantSchedule(authDTO);
	}

	public PaymentGatewayScheduleDTO Update(AuthDTO authDTO, PaymentGatewayScheduleDTO dto) {
		PaymentMerchantGatewayScheduleDAO dao = new PaymentMerchantGatewayScheduleDAO();
		if (dto.getServiceCharge() == null) {
			dto.setServiceCharge(BigDecimal.ZERO);
		}
		return dao.getPgModeUpdate(authDTO, dto);
	}

	public List<PaymentGatewayScheduleDTO> getActiveSchedulePaymentGateway(AuthDTO authDTO, OrderTypeEM orderType) {
		PaymentMerchantGatewayScheduleDAO dao = new PaymentMerchantGatewayScheduleDAO();
		List<PaymentGatewayScheduleDTO> list = dao.getActiveSchedulePaymentGateway(authDTO, orderType);
		for (Iterator<PaymentGatewayScheduleDTO> iterator = list.iterator(); iterator.hasNext();) {
			PaymentGatewayScheduleDTO paymentGateway = iterator.next();
			if (BitsUtil.isDeviceMediumExists(paymentGateway.getDeviceMedium(), authDTO.getDeviceMedium()) == null) {
				iterator.remove();
				continue;
			}
		}
		return list;
	}

	public List<PaymentGatewayScheduleDTO> getVertexScheduledPaymentGateway(AuthDTO authDTO, DateTime fromDate, DateTime toDate) {
		PaymentMerchantGatewayScheduleDAO dao = new PaymentMerchantGatewayScheduleDAO();
		List<PaymentGatewayScheduleDTO> list = dao.getVertexScheduledPaymentGateway(authDTO, fromDate, toDate);
		return list;
	}

	@Override
	public PaymentGatewayScheduleDTO getPaymentGatewayForNamespace(AuthDTO authDTO, OrderInitRequestDTO orderInitRequestDTO) throws Exception {
		PaymentMerchantGatewayScheduleDAO dao = new PaymentMerchantGatewayScheduleDAO();
		return dao.getPaymentGatewayForNamespace(authDTO, orderInitRequestDTO);
	}
}

package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderInitRequestDTO;
import org.in.com.dto.PaymentGatewayScheduleDTO;
import org.in.com.dto.enumeration.OrderTypeEM;

import hirondelle.date4j.DateTime;

public interface PaymentMerchantGatewayScheduleService extends BaseService<PaymentGatewayScheduleDTO> {
	public List<PaymentGatewayScheduleDTO> getActiveSchedulePaymentGateway(AuthDTO authDTO, OrderTypeEM orderType);

	public List<PaymentGatewayScheduleDTO> getVertexScheduledPaymentGateway(AuthDTO authDTO, DateTime fromDate, DateTime toDate);
	
	public PaymentGatewayScheduleDTO getPaymentGatewayForNamespace(AuthDTO authDTO, OrderInitRequestDTO orderInitRequestDTO) throws Exception;
}

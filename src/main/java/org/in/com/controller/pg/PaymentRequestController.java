package org.in.com.controller.pg;

import java.math.RoundingMode;

import org.in.com.cache.PaymentCache;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Text;
import org.in.com.controller.commerce.io.OrderIO;
import org.in.com.controller.web.BaseController;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.OrderInitRequestDTO;
import org.in.com.dto.OrderInitStatusDTO;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.pg.PaymentRequestService;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/commerce/payment")
public class PaymentRequestController extends BaseController {
	// Direct Payment Request for make transaction after ticket blocked or retry
	// the payment process alone
	private static final Logger requestlogger = LoggerFactory.getLogger("org.in.com.controller.pgtrace");

	@Autowired
	PaymentRequestService paymentRequestService;

	@RequestMapping(value = "/request", method = { RequestMethod.GET, RequestMethod.POST }, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public OrderIO initPayment(@PathVariable("authtoken") String authtoken, String orderCode, String orderType, String transactionCode, String paymentGatewayCode, String responseURL) {
		OrderIO transactionOrderIO = new OrderIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			OrderInitRequestDTO paymentRequestDTO = null;
			if (StringUtil.isNotNull(orderCode) || StringUtil.isNotNull(transactionCode)) {

				paymentRequestDTO = new OrderInitRequestDTO();
				// will get from API interface based on transactionTypeDTO
				// for testing use TransactionTypeDTO.RECHARGE, get Rs.50
				paymentRequestDTO.setTransactionCode(transactionCode);
				paymentRequestDTO.setPartnerCode(paymentGatewayCode);
				paymentRequestDTO.setFirstName(authDTO.getUser().getName());
				paymentRequestDTO.setLastName(authDTO.getUser().getLastname());
				paymentRequestDTO.setOrderCode(orderCode);
				paymentRequestDTO.setResponseUrl(responseURL);
				paymentRequestDTO.setOrderType(OrderTypeEM.getOrderTypeEM(orderType));

			}
			if (StringUtil.isNotNull(transactionCode)) {
				PaymentCache paymentCache = new PaymentCache();
				OrderDTO orderDTO = paymentCache.getTransactionDetails(transactionCode);
				if (orderDTO == null) {
					throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID);
				}
				paymentRequestDTO = new OrderInitRequestDTO();
				paymentRequestDTO.setAmount(orderDTO.getAmount().setScale(0, RoundingMode.HALF_UP));
				paymentRequestDTO.setOrderCode(orderDTO.getOrderCode());
				paymentRequestDTO.setOrderType(orderDTO.getOrderType());
				paymentRequestDTO.setTransactionCode(orderDTO.getTransactionCode());
				paymentRequestDTO.setResponseUrl(orderDTO.getResponseUrl());
				paymentRequestDTO.setPartnerCode(paymentGatewayCode);
				paymentRequestDTO.setFirstName(orderDTO.getFirstName());
				paymentRequestDTO.setLastName(orderDTO.getLastName());
				paymentRequestDTO.setMobile(orderDTO.getMobile());
				paymentRequestDTO.setEmail(orderDTO.getEmail());
				paymentRequestDTO.setUdf1(authDTO.getNamespace().getName() + Text.SINGLE_SPACE + authDTO.getUser().getName());
				paymentRequestDTO.setUdf2(authDTO.getNamespaceCode());
				paymentRequestDTO.setUdf3(authDTO.getDeviceMedium().getCode());
				paymentRequestDTO.setUdf4(OrderTypeEM.getOrderTypeEM(orderType).getName());
				paymentRequestDTO.setUdf5(ApplicationConfig.getServerZoneCode());
			}
			if (paymentRequestDTO == null) {
				throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID);
			}
			// Invoke the payment gateway process
			OrderInitStatusDTO orderInitStatusDTO = paymentRequestService.handlePgService(authDTO, paymentRequestDTO);
			transactionOrderIO.setTransactionCode(orderInitStatusDTO.getTransactionCode());
			transactionOrderIO.setPaymentRequestUrl(orderInitStatusDTO.getPaymentRequestUrl());
			transactionOrderIO.setGatewayInputDetails(orderInitStatusDTO.getGatewayInputDetails());

			requestlogger.info(orderInitStatusDTO.getGatewayInputDetails().toString());
		}
		return transactionOrderIO;
	}
}

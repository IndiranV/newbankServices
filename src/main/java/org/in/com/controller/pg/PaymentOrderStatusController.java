package org.in.com.controller.pg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Text;
import org.in.com.controller.pg.io.PaymentPreTransactionV2IO;
import org.in.com.controller.pg.io.RefundIO;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.PaymentGatewayProviderIO;
import org.in.com.controller.web.io.PaymentTransactionIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.PaymentPreTransactionDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.PaymentOrderStatusService;
import org.in.com.service.PaymentTransactionService;
import org.in.com.service.PendingOrderService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/payment")
public class PaymentOrderStatusController extends BaseController {
	@Autowired
	PaymentOrderStatusService paymentOrderStatusService;
	@Autowired
	PendingOrderService pendingOrderService;
	@Autowired
	PaymentTransactionService paymentTransactionService;

	@RequestMapping(value = "/order/status/{orderCode}/{namespaceCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, String>> getOrderStatus(@PathVariable("authtoken") String authtoken, @PathVariable("orderCode") String orderCode, @PathVariable("namespaceCode") String namespaceCode) {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		Map<String, String> response = new HashMap<String, String>();
		if (authDTO != null) {
			response = paymentOrderStatusService.getOrderStatus(authDTO, orderCode, namespaceCode);
		}
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/transaction/refund", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> refundOrder(@PathVariable("authtoken") String authtoken, @RequestBody RefundIO refund) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			RefundDTO refundDTO = new RefundDTO();
			refundDTO.setTransactionCode(refund.getTransactionCode());
			refundDTO.setOrderCode(refund.getOrderCode());
			refundDTO.setOrderType(OrderTypeEM.getOrderTypeEM(refund.getOrderType()));
			refundDTO.setAmount(refund.getAmount());
			pendingOrderService.refundOrder(authDTO, refundDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/gateway/transaction/{ticketCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<PaymentPreTransactionV2IO>> getPaymentGatewayTransactions(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		List<PaymentPreTransactionV2IO> paymentPreTransactions = new ArrayList<PaymentPreTransactionV2IO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);

		List<PaymentGatewayTransactionDTO> list = paymentOrderStatusService.getPaymentGatewayTransactions(authDTO, ticketDTO);
		for (PaymentGatewayTransactionDTO transactionDTO : list) {
			PaymentPreTransactionV2IO paymentPreTransactionIO = new PaymentPreTransactionV2IO();
			paymentPreTransactionIO.setCode(transactionDTO.getCode());
			paymentPreTransactionIO.setOrderCode(transactionDTO.getOrderCode());

			PaymentGatewayProviderIO gatewayProviderIO = new PaymentGatewayProviderIO();
			gatewayProviderIO.setCode(transactionDTO.getGatewayProvider().getCode());
			gatewayProviderIO.setName(transactionDTO.getGatewayProvider().getName());
			gatewayProviderIO.setServiceName(transactionDTO.getGatewayProvider().getServiceName());
			paymentPreTransactionIO.setGatewayProvider(gatewayProviderIO);

			paymentPreTransactionIO.setDeviceMedium(transactionDTO.getDeviceMedium().getCode());
			paymentPreTransactionIO.setAmount(transactionDTO.getAmount());
			paymentPreTransactionIO.setServiceCharge(transactionDTO.getServiceCharge());
			paymentPreTransactionIO.setStatus(transactionDTO.getStatus() != null ? transactionDTO.getStatus().getCode() : Text.NA);

			BaseIO gatewayTransactionType = new BaseIO();
			gatewayTransactionType.setCode(transactionDTO.getTransactionType().getCode());
			gatewayTransactionType.setName(transactionDTO.getTransactionType().getName());
			paymentPreTransactionIO.setGatewayTransactionType(gatewayTransactionType);

			paymentPreTransactionIO.setTransactionCode(transactionDTO.getGatewayTransactionCode());
			paymentPreTransactions.add(paymentPreTransactionIO);
		}
		return ResponseIO.success(paymentPreTransactions);
	}

	@RequestMapping(value = "/gateway/transaction/{ticketCode}/status/verify", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<Map<String, String>> verifyOrderStatus(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		Map<String, String> response = new HashMap<String, String>();

		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);

		response = paymentOrderStatusService.verifyOrderStatus(authDTO, ticketDTO);
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/gateway/transaction/{ticketCode}/status/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<Map<String, String>> updateTransactionStatus(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		Map<String, String> response = new HashMap<String, String>();

		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);

		response = paymentOrderStatusService.updateTransactionStatus(authDTO, ticketDTO);
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/gateway/transaction/refund", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> refundGatewayTransaction(@PathVariable("authtoken") String authtoken, @RequestBody PaymentPreTransactionV2IO paymentPreTransaction) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (StringUtil.isNull(paymentPreTransaction.getOrderCode())) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
		if (paymentPreTransaction.getAmount() == null || paymentPreTransaction.getAmount().intValue() == 0) {
			throw new ServiceException(ErrorCode.INVALID_REFUND_AMOUNT);
		}
		PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO();
		preTransactionDTO.setOrderCode(paymentPreTransaction.getOrderCode());
		preTransactionDTO.setAmount(paymentPreTransaction.getAmount());
		paymentOrderStatusService.paymentRefund(authDTO, preTransactionDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/eticket/status/verify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<PaymentTransactionIO>> verifyTicketTransaction(@PathVariable("authtoken") String authtoken, @RequestBody List<PaymentTransactionIO> paymentTransactionList) throws Exception {
		List<PaymentTransactionIO> paymentTransactionIOList = new ArrayList<PaymentTransactionIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<PaymentTransactionDTO> paymentTransactionDTOList = new ArrayList<PaymentTransactionDTO>();
		for (PaymentTransactionIO paymentTransactionIO : paymentTransactionList) {
			if (StringUtil.isNull(paymentTransactionIO.getCode())) {
				continue;
			}
			PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
			paymentTransactionDTO.setCode(paymentTransactionIO.getCode());
			paymentTransactionDTO.setTransactionAmount(paymentTransactionIO.getTransactionAmount());
			paymentTransactionDTOList.add(paymentTransactionDTO);
		}

		paymentTransactionDTOList = paymentTransactionService.verifyTicketTransaction(authDTO, paymentTransactionDTOList);
		for (PaymentTransactionDTO paymentTransactionDTO : paymentTransactionDTOList) {
			PaymentTransactionIO paymentTransactionIO = new PaymentTransactionIO();
			paymentTransactionIO.setCode(paymentTransactionDTO.getCode());
			paymentTransactionIO.setTransactionAmount(paymentTransactionDTO.getTransactionAmount());
			paymentTransactionIO.setRemarks(paymentTransactionDTO.getRemarks());
			paymentTransactionIOList.add(paymentTransactionIO);
		}
		return ResponseIO.success(paymentTransactionIOList);
	}

	@RequestMapping(value = "/eticket/transaction/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<PaymentTransactionIO>> updateTicketTransaction(@PathVariable("authtoken") String authtoken, @RequestBody List<PaymentTransactionIO> paymentTransactionList) throws Exception {
		List<PaymentTransactionIO> paymentTransactionIOList = new ArrayList<PaymentTransactionIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<PaymentTransactionDTO> paymentTransactionDTOList = new ArrayList<PaymentTransactionDTO>();
		for (PaymentTransactionIO paymentTransactionIO : paymentTransactionList) {
			if (StringUtil.isNull(paymentTransactionIO.getCode())) {
				continue;
			}
			PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
			paymentTransactionDTO.setCode(paymentTransactionIO.getCode());
			paymentTransactionDTO.setTransactionAmount(paymentTransactionIO.getTransactionAmount());
			paymentTransactionDTOList.add(paymentTransactionDTO);
		}

		List<PaymentTransactionDTO> list = paymentTransactionService.updateTicketTransaction(authDTO, paymentTransactionDTOList);
		for (PaymentTransactionDTO paymentTransaction : list) {
			PaymentTransactionIO paymentTransactionIO = new PaymentTransactionIO();
			paymentTransactionIO.setCode(paymentTransaction.getCode());
			paymentTransactionIO.setTransactionAmount(paymentTransaction.getTransactionAmount());
			paymentTransactionIO.setRemarks(paymentTransaction.getRemarks());
			paymentTransactionIOList.add(paymentTransactionIO);
		}
		return ResponseIO.success(paymentTransactionIOList);
	}

	@RequestMapping(value = "/gateway/analytics/report", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, String>>> getPaymentGatewayAnalyticsReport(@PathVariable("authtoken") String authtoken, @RequestParam(required = true) String fromDate, @RequestParam(required = true) String toDate) {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (!DateUtil.isValidDate(fromDate) || !DateUtil.isValidDate(toDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		List<Map<String, String>> responses = pendingOrderService.getPaymentGatewayAnalyticsReport(authDTO, DateUtil.convertDateTime(DateUtil.getDateTime(fromDate).getStartOfDay()), DateUtil.convertDateTime(DateUtil.getDateTime(toDate).getEndOfDay()));
		return ResponseIO.success(responses);
	}

}

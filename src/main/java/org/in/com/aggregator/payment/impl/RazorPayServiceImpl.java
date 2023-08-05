package org.in.com.aggregator.payment.impl;

import java.util.HashMap;
import java.util.Map;

import org.in.com.aggregator.payment.PGInterface;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.PaymentGatewayCredentialsDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentOrderEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.PaymentResponseException;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.Refund;

public class RazorPayServiceImpl implements PGInterface {
	private static final Logger responselogger = LoggerFactory.getLogger("org.in.com.controller.pgtrace");
	private static String TRANSACTION_ID_SEPARATOR = "__";

	public void packPaymentRequest(AuthDTO authDTO, OrderDTO order) {
		try {
			JSONObject orderRequest = new JSONObject();
			orderRequest.put("amount", order.getAmount().multiply(Numeric.ONE_HUNDRED));
			orderRequest.put("currency", "INR");
			orderRequest.put("payment_capture", 1);
			orderRequest.put("receipt", order.getOrderCode());
			orderRequest.put("partial_payment", Text.FALSE);

			JSONObject notes = new JSONObject();
			notes.put("merchant_order_id", order.getOrderCode());
			notes.put("orderType", order.getOrderType().getName());
			notes.put("mobile", order.getMobile());
			notes.put("email", order.getEmail());
			notes.put("ns", authDTO.getNamespace().getName());
			notes.put("deviceMedium", authDTO.getDeviceMedium().getName());
			notes.put("name", order.getFirstName());
			notes.put("udf1", order.getUdf1());
			notes.put("udf2", order.getUdf2());
			notes.put("udf3", order.getUdf3());
			notes.put("udf4", order.getUdf4());
			notes.put("udf5", order.getUdf5());
			notes.put("gatewayProvider", "RazorPay");
			orderRequest.put("notes", notes);

			RazorpayClient razorpay = new RazorpayClient(order.getGatewayCredentials().getAccessCode(), order.getGatewayCredentials().getAccessKey());
			Order orderResponseJson = razorpay.Orders.create(orderRequest);
			JSONObject orderResponse = orderResponseJson.toJson();

			order.setResponseRecevied(orderResponse.toString());
			if (orderResponse != null && orderResponse.has("id") && orderResponse.has("status") && "created".equals(orderResponse.get("status"))) {
				Map<String, String> requestMap = new HashMap<String, String>();
				requestMap.put("key", order.getGatewayCredentials().getAccessCode());
				requestMap.put("amount", String.valueOf(order.getAmount()));
				requestMap.put("receipt", order.getOrderCode());
				requestMap.put("currency", "INR");
				requestMap.put("name", order.getFirstName());
				requestMap.put("description", order.getOrderType().getName());
				requestMap.put("order_id", orderResponse.getString("id"));
				requestMap.put("prefill.name", order.getFirstName());
				requestMap.put("prefill.email", order.getEmail());
				requestMap.put("prefill.contact", order.getMobile());
				requestMap.put("notes.udf1", order.getUdf1());
				requestMap.put("notes.udf2", order.getUdf2());
				requestMap.put("notes.udf3", order.getUdf3());
				requestMap.put("notes.udf4", order.getUdf4());
				requestMap.put("notes.udf5", order.getUdf5());
				requestMap.put("notes.phone", order.getMobile());
				requestMap.put("notes.surl", order.getReturnUrl());
				requestMap.put("notes.furl", order.getReturnUrl());
				requestMap.put("notes.orderCode", order.getOrderCode());
				requestMap.put("notes.orderType", order.getOrderType().getName());
				requestMap.put("notes.ns", authDTO.getNamespace().getName());
				requestMap.put("notes.deviceMedium", authDTO.getDeviceMedium().getName());
				requestMap.put("callback_url", order.getReturnUrl());

				order.setGatewayFormParam(requestMap);
				order.setGatewayTransactionCode(orderResponse.getString("id"));
			}
			else {
				throw new Exception();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			responselogger.info("PPR: " + order.getTransactionCode() + Text.SINGLE_SPACE + order.getGatewayFormParam());
		}
	}

	@Override
	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception {
		try {
			RazorpayClient razorpay = new RazorpayClient(enquiryStatus.getGatewayCredentials().getAccessCode(), enquiryStatus.getGatewayCredentials().getAccessKey());
			Order orderResponseJson = razorpay.Orders.fetch(enquiryStatus.getGatewayTransactionCode());
			JSONObject orderResponse = orderResponseJson.toJson();

			if (orderResponse != null && orderResponse.has("status") && "paid".equals(orderResponse.getString("status"))) {
				enquiryStatus.setStatus(ErrorCode.SUCCESS);
				enquiryStatus.setAmount(StringUtil.getBigDecimalValue(orderResponse.get("amount").toString()).divide(Numeric.ONE_HUNDRED));

				StringBuilder transactionCode = new StringBuilder();
				transactionCode.append(enquiryStatus.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR)[0]);
				transactionCode.append(TRANSACTION_ID_SEPARATOR);
				transactionCode.append(enquiryStatus.getGatewayReponse().get("razorpay_payment_id"));
				enquiryStatus.setGatewayTransactionCode(transactionCode.toString());
			}
			else if (orderResponse != null && orderResponse.has("status") && "created".equals(orderResponse.getString("status"))) {
				enquiryStatus.setStatus(ErrorCode.SUCCESS);
				enquiryStatus.setAmount(StringUtil.getBigDecimalValue(orderResponse.get("amount").toString()).divide(Numeric.ONE_HUNDRED));

				JSONObject captureRequest = new JSONObject();
				captureRequest.put("amount", enquiryStatus.getAmount().multiply(Numeric.ONE_HUNDRED));
				captureRequest.put("currency", "INR");

				Payment payment = razorpay.Payments.capture(enquiryStatus.getGatewayTransactionCode(), captureRequest);
				if (payment != null && payment.has("id") && "paid".equals(payment.get("status"))) {
					enquiryStatus.setStatus(ErrorCode.SUCCESS);
					StringBuilder transactionCode = new StringBuilder();
					transactionCode.append(enquiryStatus.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR)[0]);
					transactionCode.append(TRANSACTION_ID_SEPARATOR);
					transactionCode.append(payment.get("id").toString());
					enquiryStatus.setGatewayTransactionCode(transactionCode.toString());
				}
				else {
					enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
				}
			}
			else {
				enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
			}
			responselogger.info("transactionEnquiry : " + authDTO.getNamespaceCode() + " " + enquiryStatus.getOrderCode() + " " + enquiryStatus.getGatewayTransactionCode() + " " + orderResponse);
		}
		catch (Exception e) {
			responselogger.error("transactionEnquiry : " + authDTO.getNamespaceCode() + "  " + enquiryStatus.getOrderCode() + "  " + enquiryStatus.getGatewayTransactionCode());
			e.printStackTrace();
		}
		finally {
			if (enquiryStatus.getStatus() == null) {
				enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
			}
			responselogger.info(enquiryStatus.getOrderCode() + " XactionCheck: " + enquiryStatus.getGatewayReponse() + " Status:" + enquiryStatus.getStatus().getCode());
		}

	}

	public void refund(AuthDTO authDTO, RefundDTO refund) {
		JSONObject jsonResponse = null;
		try {
			String paymentCaptureTransactionId = Text.NA;

			if (refund.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR).length == 2) {
				paymentCaptureTransactionId = refund.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR)[1];
			}
			else if (refund.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR).length != 2) {
				OrderDTO orderDTO = new OrderDTO();
				orderDTO.setGatewayCredentials(refund.getGatewayCredentials());
				orderDTO.setOrderCode(refund.getOrderCode());
				orderDTO.setGatewayTransactionCode(refund.getGatewayTransactionCode());
				Map<String, String> captureDetails = verifyPaymentOrder(authDTO, orderDTO);
				paymentCaptureTransactionId = captureDetails.get("paymentCaptureId");
			}
			if (StringUtil.isNull(paymentCaptureTransactionId)) {
				throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID, "Payment Capture Gateway Id not found");
			}

			RazorpayClient razorpay = new RazorpayClient(refund.getGatewayCredentials().getAccessCode(), refund.getGatewayCredentials().getAccessKey());

			JSONObject refundRequest = new JSONObject();
			refundRequest.put("amount", refund.getAmount().multiply(Numeric.ONE_HUNDRED));
			refundRequest.put("speed", "normal");
			refundRequest.put("receipt", refund.getOrderCode());

			Refund orderResponseJson = razorpay.Payments.refund(paymentCaptureTransactionId, refundRequest);
			JSONObject orderResponse = orderResponseJson.toJson();
			if (orderResponse != null && ("pending".equals(orderResponse.getString("status")) || "processed".equals(orderResponse.getString("status")))) {
				refund.setStatus(PaymentGatewayStatusEM.SUCCESS);
				refund.setGatewayTransactionCode(orderResponse.getString("id"));
			}
			else {
				refund.setStatus(PaymentGatewayStatusEM.FAILURE);
			}
			refund.setResponseRecevied(" Request : " + refundRequest + " Response: " + jsonResponse);
		}
		catch (Exception e) {
			e.printStackTrace();
			refund.setResponseRecevied(e.getMessage());
		}
		finally {
			responselogger.info("Refund 02: " + refund.getGatewayTransactionCode() + Text.SINGLE_SPACE + refund.getAmount() + Text.SINGLE_SPACE + refund.getTransactionCode() + " Response :" + jsonResponse);
		}
	}

	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			String paymentCaptureTransactionId = Text.NA;

			if (orderDTO.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR).length == 2) {
				paymentCaptureTransactionId = orderDTO.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR)[1];
			}
			else if (orderDTO.getGatewayTransactionCode().split(TRANSACTION_ID_SEPARATOR).length != 2) {
				paymentCaptureTransactionId = orderDTO.getGatewayTransactionCode();
			}

			RazorpayClient razorpay = new RazorpayClient(orderDTO.getGatewayCredentials().getAccessCode(), orderDTO.getGatewayCredentials().getAccessKey());
			Order orderResponseJson = razorpay.Orders.fetch(paymentCaptureTransactionId);
			if (orderResponseJson != null && orderResponseJson.has("status")) {
				JSONObject orderResponse = orderResponseJson.toJson();
				responseMap.put("referenceNumber", orderResponse.has("id") ? orderResponse.getString("id") : "");
				responseMap.put("orderNumber", orderResponse.has("receipt") ? orderResponse.getString("receipt") : "");
				responseMap.put("orderAmount", orderResponse.has("amount") ? StringUtil.getBigDecimalValue(orderResponse.get("amount").toString()).divide(Numeric.ONE_HUNDRED).toString() : "");
				responseMap.put("dateTime", orderResponse.has("created_at") ? DateUtil.getDateTimeByInstant(Long.valueOf(orderResponse.get("created_at").toString())) : "");
				responseMap.put("orderCapturedAmount", orderResponse.has("amount") ? StringUtil.getBigDecimalValue(orderResponse.get("amount").toString()).divide(Numeric.ONE_HUNDRED).toString() : "");
				responseMap.put("paymentCaptureId", orderResponse.has("id") ? orderResponse.getString("id") : "");
				if (orderResponse.has("notes") && orderResponse.get("notes") != null) {
					JSONObject notes = orderResponse.getJSONObject("notes");
					responseMap.put("name", notes.has("name") ? notes.getString("name") : "");
					responseMap.put("orderNumber", notes.has("merchant_order_id") ? notes.getString("merchant_order_id") : "");
				}
				responseMap.put("orderStatus", orderResponse.has("status") ? orderResponse.getString("status") : "");
				responseMap.put("status", orderResponse.has("status") ? orderResponse.getString("status") : "");
				responseMap.put("netDebitAmount", orderResponse.has("amount") ? StringUtil.getBigDecimalValue(orderResponse.get("amount").toString()).divide(Numeric.ONE_HUNDRED).toString() : "");

				if ("paid".equalsIgnoreCase(responseMap.get("orderStatus"))) {
					responseMap.put("paymentOrderStatus", PaymentOrderEM.SUCCESS.getCode());
				}
				else {
					responseMap.put("paymentOrderStatus", PaymentOrderEM.PAYMENT_DECLINED.getCode());
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			responseMap.put("Gateway Provider", "RazorPay");
		}
		return responseMap;

	}

	@Override
	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception {
	}

	public static void main(String a[]) {
		try {
			AuthDTO authDTO = new AuthDTO();
			authDTO.setNamespaceCode("bits");
			authDTO.setDeviceMedium(DeviceMediumEM.WEB_USER);

			OrderDTO order = new OrderDTO();
			order.setGatewayCredentials(new PaymentGatewayCredentialsDTO());
			order.getGatewayCredentials().setAccessCode("rzp_test_TDCReZ2s8ERT79");
			order.getGatewayCredentials().setAccessKey("PzOdPAOIkMtYqWq6KbrVzYla");

			order.setOrderCode("ORDER001");
			order.setOrderType(OrderTypeEM.TICKET);
			order.setAmount(Numeric.ONE_HUNDRED);
			order.setEmail("arun@ezeeinfo.in");
			order.setFirstName("Arun");
			order.setMobile("9159931750");
			order.setUdf1("udf1");
			order.setUdf2("udf2");
			order.setUdf3("udf3");
			order.setUdf4("udf4");
			order.setUdf5("udf5");
			order.setGatewayTransactionCode("order_H6cm4e8pHsW2jn");

			RazorPayServiceImpl rpi = new RazorPayServiceImpl();
			rpi.verifyPaymentOrder(authDTO, order);
			System.out.println(order.getResponseRecevied());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package org.in.com.controller.pg;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.in.com.aggregator.bits.BitsService;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.PaymentCache;
import org.in.com.config.ApplicationConfig;
import org.in.com.config.GatewayConfig;
import org.in.com.constants.Numeric;
import org.in.com.controller.commerce.io.ResponseIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.in.com.dto.enumeration.PaymentOrderEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.service.ConfirmSeatsService;
import org.in.com.service.pg.PaymentResponseService;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.ehcache.Element;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/commerce/payment")
public class PaymentResponseController {

	@Autowired
	PaymentResponseService paymenService;
	@Autowired
	ConfirmSeatsService confirmSeatsService;
	@Autowired
	BitsService bitsService;

	private static final Logger logger = LoggerFactory.getLogger(PaymentResponseController.class);
	private static final Logger responselogger = LoggerFactory.getLogger("org.in.com.controller.pgtrace");

	@RequestMapping(value = "/response", method = { RequestMethod.GET, RequestMethod.POST })
	public void paymentResponseHandler(HttpServletRequest request, HttpServletResponse response, ModelMap model) {

		String transactionCode = null;
		TransactionEnquiryDTO transactionEnquiryDTO = null;
		try {
			// CCAvenue will give order id in Order_Id parameter
			transactionCode = request.getParameter("Order_Id");
			if (StringUtil.isNull(transactionCode)) {
				transactionCode = request.getParameter("eTransactionCode");
			}
			if (StringUtil.isNull(transactionCode)) {
				transactionCode = request.getParameter("orderNo");
			}
			// Paytm
			if (StringUtil.isNull(transactionCode)) {
				transactionCode = request.getParameter("ORDERID");
			}
			// Atom
			if (StringUtil.isNull(transactionCode)) {
				transactionCode = request.getParameter("mer_txn");
			}
			// Khalti
			if(StringUtil.isNull(transactionCode)) {
				transactionCode = request.getParameter("purchase_order_id");
			}
			// techprocess transaction id retrieved
			if (StringUtil.isNull(transactionCode)) {
				String message = request.getParameter("msg");
				if (!StringUtils.isBlank(message) && message.contains("|")) {
					String req[] = message.split("\\|");
					if (req.length > 1) {
						transactionCode = req[1];
					}
				}
			}

			Map<String, String> responseParam = new HashMap<String, String>();

			for (Enumeration<String> en = request.getParameterNames(); en.hasMoreElements();) {
				String fieldName = en.nextElement();
				String fieldValue = request.getParameter(fieldName);
				responseParam.put(fieldName, fieldValue);
			}
			responselogger.info("transactionCode: " + transactionCode + "-" + request.getRequestURL() + "-" + responseParam.toString());

			PaymentCache paymentCache = new PaymentCache();
			OrderDTO transactionDetails = paymentCache.getTransactionDetails(transactionCode);

			if (transactionDetails != null && TransactionTypeEM.TICKETS_BOOKING.getId() == transactionDetails.getOrderType().getId() && StringUtil.isNotNull(transactionDetails.getStatus()) && !PaymentOrderEM.SUCCESS.getCode().equals(transactionDetails.getStatus())) {
				transactionEnquiryDTO = paymenService.internalTransactionEnquiry(transactionCode, responseParam);
			}
			else if (transactionDetails != null && TransactionTypeEM.TICKETS_BOOKING.getId() == transactionDetails.getOrderType().getId() && (StringUtil.isNull(transactionDetails.getStatus()) || !PaymentOrderEM.SUCCESS.getCode().equals(transactionDetails.getStatus()))) {
				transactionEnquiryDTO = paymenService.handlePayment(transactionCode, responseParam);
			}
			else if (transactionDetails != null && TransactionTypeEM.TICKETS_BOOKING.getId() != transactionDetails.getOrderType().getId()) {
				transactionEnquiryDTO = paymenService.handlePayment(transactionCode, responseParam);
			}

			if (transactionEnquiryDTO != null) {
				model.addAttribute("paymentresponse", transactionEnquiryDTO);
				response.sendRedirect(transactionEnquiryDTO.getResponseURL() + "?transactionCode=" + transactionEnquiryDTO.getTransactionCode() + "&orderCode=" + transactionEnquiryDTO.getOrderCode() + "&status=" + transactionEnquiryDTO.getStatus().getMessage());
			}
			else if (transactionEnquiryDTO == null) {
				response.getOutputStream().print("400 - Bad Request, transaction details not found");
			}
		}
		catch (ServiceException e) {
			responselogger.error(transactionCode + " : ", e.getErrorCode());
		}
		catch (Exception e) {
			responselogger.error(transactionCode + " : ", e);
		}
		finally {
			if (transactionEnquiryDTO != null) {
				responselogger.info("Response Redirect : " + transactionCode + "-" + transactionEnquiryDTO.toJSON());
			}
		}
		// return "paymentStatus";
	}

	@RequestMapping(value = "/hdfcreciever", method = { RequestMethod.GET, RequestMethod.POST })
	public void hdfcPostPayment(HttpServletRequest request, HttpServletResponse response, String env) throws IOException {

		StringBuilder responseStr = new StringBuilder();
		Map<String, String> responseMap = new HashMap<String, String>();
		for (Enumeration<?> en = request.getParameterNames(); en.hasMoreElements();) {
			try {
				String fieldName = (String) en.nextElement();
				String fieldValue = request.getParameter(fieldName);
				responseStr.append(fieldName).append("=").append(fieldValue).append("&");
				responseMap.put(fieldName, fieldValue);

			}
			catch (Exception e) {
				logger.error("Error in forming hdfc query string from hdfc response", e);
			}
		}
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (StringUtils.isBlank(ipAddress)) {
			ipAddress = request.getRemoteAddr();
		}
		responseMap.put("remoteIp", ipAddress);
		responseStr.append("remoteIp").append("=").append(ipAddress);

		String transactionId = request.getParameter("trackid");

		Element element = EhcacheManager.getHdfcCache().get(transactionId);
		if (element == null) {
			EhcacheManager.getHdfcCache().put(new Element(transactionId, responseMap));
		}
		else {
			logger.error("object found in ehcache -" + transactionId);
		}
		try {

			String redirectUrl = GatewayConfig.BASE_RETURN_URL + GatewayConfig.COMMON_RETURN_PATH + "?eTransactionCode=" + transactionId;

			logger.info(redirectUrl + "-Parameters->" + responseMap.toString());
			response.getOutputStream().println("REDIRECT=" + redirectUrl);

		}
		catch (Exception e) {
			logger.error(transactionId, e);
		}
	}

	@RequestMapping(value = "/status/notification", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseIO<BaseIO> serverPaymentResponseHandler(HttpServletRequest request) {
		String transactionCode = null;
		try {
			Map<String, String> responseParam = new HashMap<String, String>();
			for (Enumeration<String> en = request.getParameterNames(); en.hasMoreElements();) {
				String fieldName = en.nextElement();
				String fieldValue = request.getParameter(fieldName);
				responseParam.put(fieldName, fieldValue);
				if (StringUtil.isNull(transactionCode)) {
					transactionCode = request.getParameter("txnid");
				}
			}
			responselogger.info("Notificaiton: " + "transactionCode: " + transactionCode + "-" + responseParam.toString());
			responselogger.info("Notificaiton IP: " + request.getHeader("X-FORWARDED-FOR") + request.getRemoteAddr());

			// Forward request to Availability Namespace Zone
			if (StringUtil.isNotNull(responseParam.get("udf5")) && !responseParam.get("udf5").equals(ApplicationConfig.getServerZoneCode()) && NamespaceZoneEM.getNamespaceZoneEM(responseParam.get("udf5")) != null) {
				bitsService.serverPaymentResponseHandler(NamespaceZoneEM.getNamespaceZoneEM(responseParam.get("udf5")), responseParam);
			}
			else if (StringUtil.isNotNull(responseParam.get("udf5")) && NamespaceZoneEM.getNamespaceZoneEM(responseParam.get("udf5")) != null && responseParam.get("udf5").equals(ApplicationConfig.getServerZoneCode())) {

				PaymentCache paymentCache = new PaymentCache();
				OrderDTO transactionDetails = paymentCache.getTransactionDetails(transactionCode);

				if (transactionDetails != null && TransactionTypeEM.TICKETS_BOOKING.getId() == transactionDetails.getOrderType().getId() && (StringUtil.isNull(transactionDetails.getStatus()) || !PaymentOrderEM.SUCCESS.getCode().equals(transactionDetails.getStatus()))) {
					// Order Validation
					AuthDTO authDTO = paymenService.validateInternalOrderStatus(transactionCode);

					// Process Payment
					paymenService.handlePayment(transactionCode, responseParam);

					if ((StringUtil.isNull(transactionDetails.getUdf4())) || (StringUtil.isNotNull(transactionDetails.getUdf4()) && !JourneyTypeEM.POSTPONE.getCode().equals(transactionDetails.getUdf4()) && !JourneyTypeEM.PREPONE.getCode().equals(transactionDetails.getUdf4()))) {
						BookingDTO bookingDTO = confirmSeatsService.confirmBooking(authDTO, transactionCode, TransactionModeEM.PAYMENT_PAYMENT_GATEWAY.getCode(), null, null);
						System.out.println("Auto confirm " + bookingDTO.getCode());
					}
				}
			}
		}
		catch (ServiceException e) {
			responselogger.error(transactionCode + " : ", e.getErrorCode());
		}
		catch (Exception e) {
			responselogger.error(transactionCode + " : ", e);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/status/notification/razorpay", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public ResponseIO<BaseIO> razorpayServerPaymentResponseHandler(@RequestBody JSONObject data) {
		responselogger.info("", data.toString());
		String transactionCode = null;
		try {
			String event = String.valueOf(data.get("event"));
			JSONArray contains = data.getJSONArray("contains");
			if ("order.paid".equals(event) && contains.size() == Numeric.TWO_INT && contains.contains("payment") && contains.contains("order") && data.has("payload") && data.getJSONObject("payload").has("payment")) {
				JSONObject paymentJSON = null;
				NamespaceZoneEM namespaceZone = null;
				if (data.getJSONObject("payload").has("payment") && data.getJSONObject("payload").getJSONObject("payment").has("entity")) {
					paymentJSON = data.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity");
					if (paymentJSON.has("notes") && paymentJSON.getJSONObject("notes") != null) {
						namespaceZone = NamespaceZoneEM.getNamespaceZoneEM(String.valueOf(paymentJSON.getJSONObject("notes").get("udf5")));
					}
				}

				if (namespaceZone != null && !namespaceZone.getCode().equals(ApplicationConfig.getServerZoneCode())) {
					bitsService.razorpayServerPaymentResponseHandler(namespaceZone, data);
				}
				else if (namespaceZone != null) {
					JSONObject orderJSON = null;
					if (data.getJSONObject("payload").has("order") && data.getJSONObject("payload").getJSONObject("order").has("entity")) {
						orderJSON = data.getJSONObject("payload").getJSONObject("order").getJSONObject("entity");
						transactionCode = String.valueOf(orderJSON.get("receipt"));
					}
					if (paymentJSON != null && orderJSON != null && paymentJSON.has("status") && "captured".equals(String.valueOf(paymentJSON.get("status"))) && orderJSON.has("receipt") && StringUtil.isNotNull(transactionCode)) {
						responselogger.info("Notificaiton: " + "transactionCode: " + transactionCode + "-" + data.toString());

						if (namespaceZone.getCode().equals(ApplicationConfig.getServerZoneCode())) {
							PaymentCache paymentCache = new PaymentCache();
							OrderDTO transactionDetails = paymentCache.getTransactionDetails(transactionCode);

							if (transactionDetails != null && TransactionTypeEM.TICKETS_BOOKING.getId() == transactionDetails.getOrderType().getId() && (StringUtil.isNull(transactionDetails.getStatus()) || !PaymentOrderEM.SUCCESS.getCode().equals(transactionDetails.getStatus()))) {
								/** Order Validation */
								AuthDTO authDTO = paymenService.validateInternalOrderStatus(transactionCode);

								/** Process Payment */
								Map<String, String> responseParam = new HashMap<String, String>();
								responseParam.put("razorpay_payment_id", String.valueOf(orderJSON.get("id")));
								responseParam.put("amount", String.valueOf(StringUtil.getBigDecimalValue(String.valueOf(paymentJSON.get("amount_paid"))).divide(Numeric.ONE_HUNDRED)));
								responseParam.put("receipt", String.valueOf(orderJSON.get("receipt")));
								responseParam.put("currency", "INR");
								responseParam.put("status", String.valueOf(orderJSON.get("status")));
								responseParam.put("order_id", String.valueOf(orderJSON.get("id")));
								JSONObject noteJSON = orderJSON.getJSONObject("notes");
								responseParam.put("name", String.valueOf(noteJSON.get("name")));
								responseParam.put("description", String.valueOf(noteJSON.get("orderType")));
								responseParam.put("prefill.name", String.valueOf(noteJSON.get("name")));
								responseParam.put("prefill.email", String.valueOf(noteJSON.get("email")));
								responseParam.put("prefill.contact", String.valueOf(noteJSON.get("mobile")));
								responseParam.put("notes.udf1", String.valueOf(noteJSON.get("udf1")));
								responseParam.put("notes.udf2", String.valueOf(noteJSON.get("udf2")));
								responseParam.put("notes.udf3", String.valueOf(noteJSON.get("udf3")));
								responseParam.put("notes.udf4", String.valueOf(noteJSON.get("udf4")));
								responseParam.put("notes.udf5", String.valueOf(noteJSON.get("udf5")));
								responseParam.put("notes.phone", String.valueOf(noteJSON.get("mobile")));
								responseParam.put("notes.orderCode", String.valueOf(noteJSON.get("merchant_order_id")));
								responseParam.put("notes.orderType", String.valueOf(noteJSON.get("orderType")));
								responseParam.put("notes.ns", String.valueOf(noteJSON.get("ns")));
								responseParam.put("notes.deviceMedium", String.valueOf(noteJSON.get("udf3")));
								paymenService.handlePayment(transactionCode, responseParam);

								if ((StringUtil.isNull(transactionDetails.getUdf4())) || (StringUtil.isNotNull(transactionDetails.getUdf4()) && !JourneyTypeEM.POSTPONE.getCode().equals(transactionDetails.getUdf4()) && !JourneyTypeEM.PREPONE.getCode().equals(transactionDetails.getUdf4()))) {
									BookingDTO bookingDTO = confirmSeatsService.confirmBooking(authDTO, transactionCode, TransactionModeEM.PAYMENT_PAYMENT_GATEWAY.getCode(), null, null);
									System.out.println("Auto confirm " + bookingDTO.getCode());
								}
							}
						}
					}
				}
			}
		}
		catch (ServiceException e) {
			responselogger.error(transactionCode + " : ", e.getErrorCode());
		}
		catch (Exception e) {
			responselogger.error(transactionCode + " : ", e);
		}
		return ResponseIO.success();
	}
}

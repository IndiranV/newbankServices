package org.in.com.aggregator.payment.impl;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.in.com.aggregator.payment.PGInterface;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.dto.enumeration.PaymentOrderEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.PaymentResponseException;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.JsonBuilder;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import com.google.gson.JsonObject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

import net.sf.json.JSONObject;

public class KhaltiPaymentServiceImpl implements PGInterface {

	private static final Logger logger = LoggerFactory.getLogger("org.in.com.controller.pgtrace");
	private static final String KHALTI_BASE_URL = "https://khalti.com/api/v2";
	private static final String INITIALIZE_PAYMENT = "/epayment/initiate/";
	private static final String VERIFY_PAYMENT = "/epayment/lookup/";

	@Override
	public void packPaymentRequest(AuthDTO authDTO, OrderDTO order) throws Exception {
		TreeMap<String, String> paytmReqParam = new TreeMap<String, String>();
		JSONObject responseData = null;
		try {
			JsonBuilder transactionJson = new JsonBuilder()
					.add("return_url", order.getReturnUrl())
					.add("website_url", "https://" + authDTO.getNamespace().getProfile().getDomainURL())
					.add("amount", getPaisaValue(order.getAmount()))
					.add("purchase_order_id", order.getTransactionCode())
					.add("purchase_order_name", order.getOrderType().getName())
					.add("customer_info", new JsonBuilder()
							.add("name", order.getFirstName())
							.add("email", order.getEmail())
							.add("phone", order.getMobile()).toJson());
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("Authorization", order.getGatewayCredentials().getAccessKey());

			responseData = httpPOST(KHALTI_BASE_URL + INITIALIZE_PAYMENT, headers, transactionJson.toJson());
			order.setResponseRecevied(responseData.toString());
			if (StringUtil.isNotNull(responseData) && !responseData.has("error_key") && !responseData.has("status_code")) {
				String gatewayTransactionCode = responseData.has("pidx") ? responseData.getString("pidx") : null;
				String return_url = responseData.has("payment_url") ? responseData.getString("payment_url") : null;
				order.setGatewayUrl(return_url);
				order.setGatewayTransactionCode(gatewayTransactionCode);
				paytmReqParam.put("paymentRedirectMethod", "GET");
				paytmReqParam.put("pidx",gatewayTransactionCode);
				order.setGatewayFormParam(paytmReqParam);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			logger.info("packPaymentRequest PNR: " + order.getTransactionCode() + Text.SINGLE_SPACE + order.getGatewayFormParam() + Text.SINGLE_SPACE + responseData);
		}
	}

	private JSONObject httpPOST(String URL, Map<String, String> headers, JsonObject data) throws Exception {
		Client client = Client.create(getTLSConfig(new DefaultClientConfig()));

		WebResource webResource = client.resource(URL);
		WebResource.Builder builder = (WebResource.Builder) webResource.accept(new String[] { "application/json" }).type("application/json");
		for (Entry<String, String> param : headers.entrySet()) {
			builder.header(param.getKey(), param.getValue());
		}

		ClientResponse response = builder.post(ClientResponse.class, data.toString());
		String json = (String) response.getEntity(String.class);
		JSONObject jsonObject = null;
		if (StringUtil.isNotNull(json)) {
			jsonObject = JSONObject.fromObject(json);
		}
		return jsonObject;
	}

	private ClientConfig getTLSConfig(DefaultClientConfig config) throws Exception {
		try {
			TrustManager[] trustAllCerts = { new X509TrustManager() {
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };
			SSLContext sc = SSLContext.getInstance("TLSv1.2");
			sc.init(null, trustAllCerts, new SecureRandom());
			config.getProperties().put("com.sun.jersey.client.impl.urlconnection.httpsProperties", new HTTPSProperties(new HostnameVerifier() {

				public boolean verify(String s, SSLSession sslSession) {
					return true;
				}
			}, sc));
		}
		catch (Exception e) {
			throw e;
		}
		return config;
	}

	private String getPaisaValue(BigDecimal amount) {
		BigDecimal rupees = amount;
		BigDecimal paisa = rupees.multiply(new BigDecimal(100));
		return String.valueOf(paisa);
	}

	@Override
	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception {
         enquiryStatus.setStatus(ErrorCode.SUCCESS);
	}

	@Override
	public void refund(AuthDTO authDTO, RefundDTO refund) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			String json = null;
			String url = KHALTI_BASE_URL + VERIFY_PAYMENT;
			JSONObject object = new JSONObject();
			object.put("pidx", orderDTO.getGatewayTransactionCode());
			HttpServiceClient client = new HttpServiceClient();
			Header[] headers = { new BasicHeader("Authorization", orderDTO.getGatewayCredentials().getAccessKey()), new BasicHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) };
			json = client.postSSLV2(url, object.toString(), headers);

			if (StringUtil.isNotNull(json) && !json.contains("error_key") && !json.contains("status_code")) {
				JSONObject jsonResponse = JSONObject.fromObject(json);
				if (jsonResponse != null) {
					responseMap.put("pidx", jsonResponse.has("pidx") ? jsonResponse.getString("pidx") : Text.NA);
					responseMap.put("total_amount", jsonResponse.has("total_amount") ? jsonResponse.getString("total_amount") : Text.NA);
					responseMap.put("status", jsonResponse.has("status") ? jsonResponse.getString("status") : Text.NA);
					responseMap.put("transaction_id", jsonResponse.has("transaction_id") ? jsonResponse.getString("transaction_id") : Text.NA);
					responseMap.put("fee", jsonResponse.has("fee") ? jsonResponse.getString("fee") : Text.NA);
					responseMap.put("refunded", jsonResponse.has("refunded") ? jsonResponse.getString("refunded") : Text.NA);

					if ("Completed".equalsIgnoreCase(responseMap.get("status"))) {
						responseMap.put("paymentOrderStatus", PaymentOrderEM.SUCCESS.getCode());
					}
					else if ("Pending".equalsIgnoreCase(responseMap.get("status")) || "Refunded".equalsIgnoreCase(responseMap.get("status")) || "Expired".equalsIgnoreCase(responseMap.get("status"))) {
						responseMap.put("paymentOrderStatus", PaymentOrderEM.PAYMENT_DECLINED.getCode());
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			responseMap.put("Gateway Provider", "Khalti");
		}
		return responseMap;
	}
}

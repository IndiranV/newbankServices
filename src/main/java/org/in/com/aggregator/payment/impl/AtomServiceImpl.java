package org.in.com.aggregator.payment.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.in.com.aggregator.payment.PGInterface;
import org.in.com.config.GatewayConfig;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class AtomServiceImpl implements PGInterface {
	private static final Logger logger = LoggerFactory.getLogger("org.in.com.controller.pgtrace");

	@Override
	public void packPaymentRequest(AuthDTO authDTO, OrderDTO order) throws Exception {
		Map<String, String> fields = new HashMap<String, String>();
		try {
			Map<String, String> gatewayFormParam = new HashMap<String, String>();
			String returnUrl = order.getGatewayCredentials().getPgReturnUrl() + GatewayConfig.COMMON_RETURN_PATH;
			String getTokenURL = "https://payment.atomtech.in/paynetz/epi/fts" + "?login=" + order.getGatewayCredentials().getAccessCode() + "&pass=" + order.getGatewayCredentials().getAccessKey() + "&ttype=NBFundTransfer&prodid=" + order.getGatewayCredentials().getAttr1() + "&amt=" + order.getAmount() + "&txncurr=INR&txnscamt=0&clientcode=007&txnid=" + order.getTransactionCode() + "&date=" + DateUtil.currentDateAndTime() + "&custacc=1011464110604503&udf1=" + order.getFirstName() + "&udf2=" + order.getEmail() + "&udf3=" + order.getMobile() + "&udf4=Chennai&ru=" + returnUrl;
			getTokenURL = getTokenURL.replaceAll(" ", "%20");
			getTokenURL = getTokenURL.replaceAll("\\{", "%7A");
			getTokenURL = getTokenURL.replaceAll("\\}", "%7D");
			Client client = new Client();
			client.setReadTimeout(10000);
			client.setConnectTimeout(10000);
			WebResource webResource = client.resource(getTokenURL);
			ClientResponse response = webResource.accept(MediaType.APPLICATION_ATOM_XML).post(ClientResponse.class);
			String respString = response.getEntity(String.class);
			logger.info(order.getOrderCode() + " Request: " + webResource.toString());
			logger.info(order.getOrderCode() + " Response: " + respString);
			fields.put("LOGIN =", getTokenURL);
			fields.put("ATOM API=", webResource.toString());
			fields.put("ATOM API=", respString);
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = new ByteArrayInputStream(respString.getBytes());
			Document dom = db.parse(is);
			org.w3c.dom.Element docEle = dom.getDocumentElement();
			String tempTxnId = docEle.getElementsByTagName("param").item(1).getNodeValue();
			String token = docEle.getElementsByTagName("param").item(2).getNodeValue();
			String sendTokenURL = "https://payment.atomtech.in/paynetz/epi/fts" + "?ttype=NBFundTransfer&tempTxnId=" + tempTxnId + "&token=" + token + "&txnStage=1";
			webResource = client.resource(sendTokenURL);
			gatewayFormParam.put("Redirect_Url", order.getReturnUrl());
			order.setGatewayUrl(sendTokenURL);
			order.setGatewayFormParam(gatewayFormParam);
			String logData = order.getGatewayCredentials().getAccessCode() + "," + order.getGatewayCredentials().getAccessKey() + "," + order.getGatewayCredentials().getAttr1() + "," + "," + order.getAmount() + "," + order.getTransactionCode();
			fields.put("ATOM =", logData);
			order.setResponseRecevied(fields.toString());
		}
		catch (Exception e) {
			String logData = "Error occured in ATOM first request for PNR : " + order.getGatewayTransactionCode();
			System.out.println(logData);
			fields.put("ATOM =", logData);
			order.setResponseRecevied(fields.toString() + "ERROR - " + e.getMessage());
		}

	}

	@Override
	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws Exception {
		String status = (String) enquiryStatus.getGatewayReponse().get("f_code");
		enquiryStatus.setGatewayTransactionCode((String) enquiryStatus.getGatewayReponse().get("mmp_txn"));
		enquiryStatus.setGatewayTransactionCode((String) enquiryStatus.getGatewayReponse().get("mer_txn"));
		enquiryStatus.setAmount(new BigDecimal(enquiryStatus.getGatewayReponse().get("amt")));
		enquiryStatus.setResponseRecevied(status + ", " + enquiryStatus.getGatewayReponse().toString());
		if ("Ok".equalsIgnoreCase(status)) {
			enquiryStatus.setStatus(ErrorCode.SUCCESS);
		}
		else {
			enquiryStatus.setStatus(ErrorCode.PAYMENT_DECLINED);
		}

	}

	@Override
	public void refund(AuthDTO authDTO, RefundDTO refund) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO) {
		// TODO Auto-generated method stub
		return null;
	}

}

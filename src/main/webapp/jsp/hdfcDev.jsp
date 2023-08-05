<%@page import="org.in.com.config.GatewayConfig"%>
<%@page import="org.slf4j.LoggerFactory"%>
<%@page import="org.slf4j.Logger"%>
<%@page import="org.in.com.cache.EhcacheManager"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="net.sf.ehcache.Element"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.Enumeration"%>
<%@page import="java.util.Map"%>
<%
	Logger logger = LoggerFactory.getLogger("hdfcDev");
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
			e.printStackTrace();
		}
	}
	String ipAddress = request.getHeader("X-FORWARDED-FOR");
	responseStr.append("remoteIp").append("=").append(ipAddress);

	String bookingId = request.getParameter("trackid");
	String transactionId = request.getParameter("trackid");
	responseStr.append("eTransactionCode").append("=").append(transactionId);
	Element element = EhcacheManager.getHdfcCache().get(transactionId);
	if (element == null) {
		EhcacheManager.getHdfcCache().put(new Element(transactionId, responseMap));
	}
	else {
		logger.error("Transaction already exists -" + transactionId);
	}

	logger.info("-Parameters" + responseMap.toString());

	if (GatewayConfig.DEV_ENIORNMENT) {
		response.sendRedirect("Redirect=http://localhost:8080/busservices/commerce/payment/response?eTransactionCode=" + transactionId);
	}
	else {
		out.println("Redirect=http://localhost:8080/busservices/jsp/hdfcDev.jsp?" + responseStr);
	}
%>
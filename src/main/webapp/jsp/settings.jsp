
<%@page import="org.in.com.controller.api_v3.ApiV3Controller"%>
<%@page import="org.in.com.controller.api_v3.ApiGPSV3Controller"%>

<%
	ApiGPSV3Controller.ConcurrentRequests.clear();
	ApiV3Controller.ConcurrentRequests.clear();
%>
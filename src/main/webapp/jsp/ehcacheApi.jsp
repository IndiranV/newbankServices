<%@page import="net.sf.ehcache.Element"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="org.in.com.controller.api_v2.ApiV2Controller"%>
<%@page import="org.in.com.utils.StringUtil"%>
<%@page import="net.sf.ehcache.Cache"%>
<%@page import="org.in.com.cache.EhcacheManager"%>

<%
	String event = (String) request.getParameter("cachename");
	if (StringUtil.isNotNull(event)) {
		Cache cache = EhcacheManager.getCacheManager().getCache(event);
		if (cache != null) {
			cache.removeAll();
			out.println("ok");
		}
	}
%>

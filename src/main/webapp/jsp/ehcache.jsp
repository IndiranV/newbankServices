<%@page import="com.google.gson.Gson"%>
<%@page import="org.in.com.controller.api_v3.ApiGPSV3Controller"%>
<%@page import="org.in.com.controller.api_v3.ApiV3Controller"%>
<%@page import="org.joda.time.DateTime"%>
<%@page import="net.sf.ehcache.Element"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="org.in.com.controller.api_v2.ApiV2Controller"%>
<%@page import="org.in.com.utils.StringUtil"%>
<%@page import="net.sf.ehcache.Cache"%>
<%@page import="org.in.com.cache.EhcacheManager"%>
<html>
<script language="javascript" type="text/javascript"
	src="/<%=pageContext.getServletContext().getServletContextName()%>/js/jquery-1.8.0.min.js"></script>
<body>

	<%
		String event = (String) request.getParameter("cachename");
		if (StringUtil.isNotNull(event)) {
			Cache cache = EhcacheManager.getCacheManager().getCache(event);
			if (cache != null) {
				cache.removeAll();
				out.println("<div> " + event + " has been cleared </div>");
			}
		}
		String cacheManager = (String) request.getParameter("cacheManager");
		String cacheKey = (String) request.getParameter("cacheKey");
		if (StringUtil.isNotNull(cacheManager) && StringUtil.isNotNull(cacheKey)) {
			Cache cache = EhcacheManager.getCacheManager().getCache(cacheManager);
			if (cache != null && cache.get(cacheKey) != null) {
				cache.remove(cacheKey);
				out.println("<div> " + cacheKey + " has been cleared </div>");
			}
		}
	%>

	<table align="center" border="1px">
		<%
			String cachelist = (String) request.getParameter("cachelist");
			if (StringUtil.isNull(cachelist)) {
				String[] nameList = EhcacheManager.getCacheManager().getCacheNames();
				long totalSize = 0;
				int totalCount = 0;

				for (int count = 0; count < nameList.length; count++) {
					Cache cacheAttributes = EhcacheManager.getCacheManager().getCache(nameList[count]);
					out.print("<tr>");
					if (cacheAttributes != null) {
						out.println("<td>" + nameList[count] + "</td><td>" + cacheAttributes.getKeys().size() + "</td><td>" + cacheAttributes.calculateInMemorySize() / 1024 + " kb </td> <td><a href=/busservices/jsp/ehcache.jsp?cachename=" + nameList[count] + ">" + "Clear</a></td><td><a href=/busservices/jsp/ehcache.jsp?viewkeys=" + nameList[count] + ">" + "Keys</a></td>");
						totalCount = totalCount+cacheAttributes.getKeys().size();
						totalSize = totalSize+ cacheAttributes.calculateInMemorySize();
					}
					else {
						out.println("<td>" + nameList[count] + "</td><td>0</td>");
					}
					out.print("</tr>");
				}
				out.print("<tr style='background-color:gold;'>");
				out.println("<td> Total </td><td>" + totalCount + "</td><td>" + totalSize/1024 + " kb</td>");
				out.print("</tr>");

				if (ApiV2Controller.ConcurrentRequests != null) {
					for (Entry<String, Integer> e : ApiV2Controller.ConcurrentRequests.entrySet()) {
						if (e.getValue() > 0) {
							out.print("<tr>");
							out.println("<td>" + e.getKey() + "</td><td>" + e.getValue() + "</td>");
							out.print("</tr>");
						}
					}
				}
				/**
				if (ApiV3Controller.ConcurrentRequests != null) {
					out.print("<tr>");
					out.println("<td>-------V3---------</td><td> ----------- </td>");
					out.print("</tr>");
					for (Entry<String, Integer> e : ApiV3Controller.ConcurrentRequests.entrySet()) {
						out.print("<tr>");
						out.println("<td>" + e.getKey() + "</td><td>" + e.getValue() + "</td>");
						out.print("</tr>");
					}
				}
				if (ApiGPSV3Controller.ConcurrentRequests != null) {
					ApiGPSV3Controller.ConcurrentRequests.clear();
					out.print("<tr>");
					out.println("<td>------GPS-------</td><td> -----V3------ </td>");
					out.print("</tr>");
					for (Entry<String, Integer> e : ApiGPSV3Controller.ConcurrentRequests.entrySet()) {
						out.print("<tr>");
						out.println("<td>" + e.getKey() + "</td><td>" + e.getValue() + "</td>");
						out.print("</tr>");
					}
				}
				*/
			}
		%>
	</table>
	<br>
	<%
		String viewkeys = (String) request.getParameter("viewkeys");
		if (StringUtil.isNotNull(viewkeys)) {
			Cache cache = EhcacheManager.getCacheManager().getCache(viewkeys);
			out.print("<table align='center' border='1px'>");
			for (Object key : cache.getKeys()) {
				Element element = cache.get(key);
				if (element != null) {
					out.print("<tr>");
					out.println("<td>" + element.getObjectKey() + "</td><td>" + element.getSerializedSize() + "</td><td>" + new DateTime(element.getCreationTime()).toString() + "</td><td>" + new DateTime(element.getExpirationTime()).toString() + "</td><td><a href=/busservices/jsp/ehcache.jsp?cacheManager=" + cache.getName() + "&cacheKey=" + element.getObjectKey() + ">" + "Clear</a></td>" + "</td><td><a href=/busservices/jsp/ehcache.jsp?viewDataCache=" + cache.getName() + "&viewDataKey=" + element.getObjectKey() + ">" + "View</a></td>");
					out.print("</tr>");
					Gson gson = new Gson();
					gson.toJson(element.getObjectValue());
				}
			}
			out.print("</table>");
		}
	%>
	<br>
	<%
		String viewDataCache = (String) request.getParameter("viewDataCache");
		String viewDataKey = (String) request.getParameter("viewDataKey");
		if (StringUtil.isNotNull(viewDataCache) && StringUtil.isNotNull(viewDataKey)) {
			Cache cache = EhcacheManager.getCacheManager().getCache(viewDataCache);
			out.print("<table align='center' border='1px'>");
			Element element = cache.get(viewDataKey);
			if (element != null) {
				Gson gson = new Gson();
				out.print("<tr>");
				out.println("<td>" + element.getObjectKey() + "</td><td>" + gson.toJson(element.getObjectValue()));
				out.print("</tr>");
			}
			out.print("</table>");
		}
	%>
</body>

<style>
.tblshw {
	width: 20%;
	word-wrap: break-word;
}
</style>

<script type="text/javascript">
	$(function() {
		$("#seradd").click(function() {
			var pathKey = $('input:radio[name=spath]:checked').val();
			var URLlink = "/jsp/ehcache.jsp?cachename=" + pathKey;
			var value = $.get(URLlink, function(data) {
			}).success(function() {
			});
		});
	});
</script>

</html>
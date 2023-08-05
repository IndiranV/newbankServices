<%@page import="java.util.*"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Provider Page</title>
</head>
<body>
	<br />
	<br />
	<h2 align=center>Dummy Payment Netbanking Page</h2>
	<br />
	<FORM METHOD=POST ACTION="<%=request.getParameter("Redirect_Url")%>">
		<TABLE border=1 align=center>
			<%
				Enumeration en = request.getParameterNames();

				// enumerate through the keys and extract the values 
				// from the keys! 
				StringBuffer buffer = new StringBuffer();
				while (en.hasMoreElements()) {
					String parameterName = (String) en.nextElement();
					String parameterValue = request.getParameter(parameterName);
					out.println("<tr><td> " + parameterName + " : </td><td>" + parameterValue + "</td></tr>");
					buffer.append("<input type='hidden' name='"+parameterName+"'  value='"+parameterValue+"' />");
				}
			%>
			<TR>
				<TD>Payment Status :</TD>
				<TD><INPUT TYPE="radio" NAME="status" value="success">
					Success&nbsp;&nbsp; <INPUT TYPE="radio" NAME="status"
					value="failure"> Failure</TD>
			</TR>
			<TR>
				<TD colspan=2 align=center>&nbsp;</TD>
			</TR>
			<TR>
				<TD colspan=2 align=center><INPUT TYPE="submit" value="Submit"></TD>
			</TR>
		</TABLE>

<% out.println(buffer.toString()); %>
	</FORM>
</body>
</html>
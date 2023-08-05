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
				/*
				ServletContext context = getServletContext();
				RequestDispatcher dispatcher = context.getRequestDispatcher(request.getParameter("Redirect_Url"));
				
				dispatcher.forward(request, response);
				 */

				Enumeration en = request.getParameterNames();

				// enumerate through the keys and extract the values 
				// from the keys! 
				while (en.hasMoreElements()) {
					String parameterName = (String) en.nextElement();
					String parameterValue = request.getParameter(parameterName);
					out.println("<input type='hidden' name="+parameterName+"  value="+parameterValue+" />");
					out.println("<TR><TD>" + parameterName + " : </TD><TD>" + parameterValue + "</TD></TR>");
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


	</FORM>
</body>
</html>
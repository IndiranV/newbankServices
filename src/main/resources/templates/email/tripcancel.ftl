      Hi Team, <br /><br />                    
	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; ${tripCode!"-"} - Trip cancelled for ${bookingId!"-"}.<br /><br />
Route : ${fromStation!"-"} - ${toStation!"-"}<br /><br />
<#if discount?has_content>
Discount : (-) Rs. ${discount!"-"}<br />
</#if>
Refund Amount : Rs. ${refund!"-"}

<br />
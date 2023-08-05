<html>
<body>
Dear ${passengerName!"-"},<br/>
<br/>
<br/>
Your ticket has been cancelled successfully.<br/>
We have initiated the process to refund Rs.${refund!"-"} to you and it will credited to your account in approximately 5 -7 bank working days.<br/>
<br/>
<b>Ticket details:</b>
<br/>
PNR : ${pnr!"-"}<br/>
From: ${originName!"-"}<br/>
To: ${destinationName!"-"}<br/>
Date of Journey: ${travelDate!"-"}<br/>
Bus Type: ${busType!"-"}<br/>
Boarding Point : ${boardingPointName!"-"}<br/>
Boarding Time: ${boardingPointTime!"-"}<br/>
Seat number: ${seats!"-"}<br/>
<br/>
<br/>
<b>Refund calculation:</b><br/>
Amount paid: Rs. ${paidAmount!"-"}<br/>
Cancellation Charges: (-) Rs. ${cancellationCharges!"-"}<br/>
<#if discount?has_content>
Discount: (-) Rs. ${discount!"-"}<br/>
</#if>
Final refund amount: Rs. ${refund!"-"}<br/>
<br/>

Best regards,<br/>
${travelsName!"-"}<br/>
${website!"-"}
</body>
</html>

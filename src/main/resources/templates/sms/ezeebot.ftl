<#switch notificationType>
	<#case "AFTDP">
New ticket booked in ${serviceNo!"-"}

PNR : ${pnr!"-"}
Route : ${route!"-"}
Seat : ${seats!"-"}
Mobile : <a href='tel:${mobile!"-"}'> ${mobile!"-"} </a>
Boarding : ${boardingPoint!"-"}
Boarding Time : ${boardingTime!"-"}
Booked By : ${bookedBy!"-"}
Driver Number : <a href='tel:${driverMobile!"-"}'> ${driverMobile!"-"} </a>
	<#break>
	<#case "TCKNB">
Not boarded in ${serviceNo!"-"}, Open the seats for new booking.

PNR : ${pnr!"-"}
Seat : ${seats!"-"}
Mobile : <a href='tel:${mobile!"-"}'> ${mobile!"-"} </a>
Booked By : ${bookedBy!"-"}
Boarding : ${boardingPoint!"-"}
Boarding Time : ${boardingTime!"-"}
	<#break>
	<#case "NOTTRVL">
${pnr!"-"}, ${seats!"-"} updated as not travelled.

Seat(s) opened for re-booking. 
	<#break>
	<#case "SALES">
${time!"-"}

${header!"-"}

<b>Bookings : </b>
${booking!"-"}

<#list content as cont>
${cont!"-"}
</#list>


<#list summary as summ>
${summ!"-"}
</#list>


<#if cancel?has_content>
<b> Cancels : </b> 
${cancel!"-"} 
</#if>

<a href='${domainUrl!"-"}'> ${domainUrl!"-"} </a>
	<#break>
	<#case "FLRTCK">
Failure / Dropout Ticket - ${travelDate!"-"}

Route : ${route!"-"}
Service # : ${serviceNo!"-"}
Bus Type : ${busType!"-"}

No Of Seats :  ${seatCount!"-"}
Seats :  ${seats!"-"}
Boarding : ${boardingPoint!"-"}
Boarding Time : ${boardingTime!"-"}
By : ${updatedBy!"-"}

Customer : ${customerDetails!"-"}
Customer Mobile : <a href='tel:${customerMobile!"-"}'> ${customerMobile!"-"} </a>
	<#break>
	<#case "PBCNL">
	<#case "MYTCK_PBCNL">
${pnr!"-"} cancelled by ${updatedBy!"-"}.

Route : ${route!"-"}
Service # : ${serviceNo!"-"}
DOJ : ${travelDate!"-"}
#Seats : ${seats!"-"}
Fare : ${fare!"-"}
Booked By : ${bookedBy!"-"}
	<#break>
	<#case "TCKCNL">
	<#case "MYTCK_TCKCNL">
${pnr!"-"} cancelled by ${cancelledBy!"-"}.
Route : ${route!"-"}
Service # : ${serviceNo!"-"}
DOJ : ${travelDate!"-"}
#Seats : ${seats!"-"}
Fare : ${fare!"-"}
Passenger Number : ${mobileNumber!"-"}
Cancel Charges : ${charges!"-"}
Booked By : ${bookedBy!"-"}
	<#break>
	<#case "FRCNG">
Fare changed for the service ${serviceNo!"-"} , ${tripDate!"-"} by ${updatedBy!"-"}

<#list fareDetails as details>
${details!"-"}
</#list>
	<#break>
	<#case "SRVUP">
	<#if status == "CLOSE">
	${serviceNo!"-"} which has the ${tripDate!"-"} is <b>closed</b> by ${updatedBy!"-"}.
	
	<#elseif status == "CANCEL">
	${serviceNo!"-"} which has the ${tripDate!"-"} is <b>cancelled</b> by ${updatedBy!"-"}.
	
	${seatCount!"-"} seats booked
	
	<#elseif status == "OPEN">
	<b>Booking opened</b> a service ${serviceNo!"-"} which has the <b>DOJ is ${tripDate!"-"}</b> by ${updatedBy!"-"}.
	</#if>
	<#break>
	<#case "VHNTASN">
Vehicle not assigned for the service ${serviceNo!"-"}, unable to send tracking SMS to passengers.
	<#break>
	<#case "TRPSMS">
${type!"-"}  notification sent for the trip ${serviceNo!"-"}, ${travelDate!"-"}.

No Of PNR : ${pnrCount!"-"}
<#if time?has_content> 
Time : ${time!"-"}
</#if>
<#if stationPoint?has_content>
Station Point: ${stationPoint!"-"}
</#if>

Sent by ${updatedBy!"-"}
	<#break>
	<#case "FRTCK">
Service First ticket booked in ${serviceNo!"-"}

PNR : ${pnr!"-"}
Seat : ${seats!"-"}
By : ${bookedUser!"-"}
Boarding : ${boardingPoint!"-"}
	<#break>
	<#case "ADVBO">
Advance ticket booked for ${travelDate!"-"} by ${updatedBy!"-"}

Route : ${route!"-"}
Service # : ${serviceNo!"-"}
Fare : ${fare!"-"}
	<#break>
	<#case "TCKBL">
New Ticket blocked for ${travelDate!"-"} by ${updatedBy!"-"}

PNR : ${pnr!"-"} 
Seat : ${seats!"-"}
Route : ${route!"-"}
Service # : ${serviceNo!"-"}
Fare : ${fare!"-"}
	<#break>
	<#case "TCKCNF">
	<#case "MYTCK_TCKCNF">
New Ticket Booked for ${travelDate!"-"} by ${updatedBy!"-"}

PNR : ${pnr!"-"}
Seat : ${seats!"-"}
Route : ${route!"-"}
Service # : ${serviceNo!"-"}
Fare : ${fare!"-"}
${passenger!"-"} ${mobile!"-"}
	<#break>
	<#case "NEWSCH">
New schedule created by ${updatedBy!"-"}.

Service #: ${serviceNo!"-"}
DOJ : ${fromDate!"-"} To ${toDate!"-"}
Bus Type : ${busType!"-"} 
	<#break>
	<#case "SCHEDT">
${header!"-"}.
 
${changes!"-"}
By : ${updatedBy!"-"}
	<#break>
	<#case "NEWUSR">
New user created with the name ${userFirstName!"-"} under the group ${groupName!"-"} 

By ${updatedBy!"-"}
	<#break>
	<#case "USRLGN">
	<#case "ALUSRLN">
User ${userFirstName!"-"} logged in
	<#break>
	<#case "DUPLGN">
User ${userFirstName!"-"} duplicate logged in
	<#break>
	<#case "RSTPWD">
User ${userFirstName!"-"}'s password changed by ${updatedBy!"-"}.
	<#break>
	<#case "USRDLT">
User ${userFirstName!"-"} is deleted under the group ${groupName!"-"} 

By ${updatedBy!"-"}
	<#break>
	<#case "OTPLGN">
OTP for your login is ${otpNumber!"-"}, valid 10 minutes.
	<#break>
	<#case "STVSB">
${activityType!"-"}	
	
Seat name : ${seats!"-"}	
<#if users?has_content>
${users!"-"}  
</#if>
<#if groups?has_content> 	
${groups!"-"} 
</#if>
Release : ${minutes!"-"}
By : ${updatedBy!"-"}
	<#break>
	<#case "OCCSTS">
Today Trip - ${tripCount!"-"}

${tripCountDetails!"-"}

<#list tripDetails as details>
${details!"-"}
</#list>

	<#default>
	<#break>
	
<#case "CUSFEB">
${title!"-"}

Mobile - ${mobile!"-"}
Email - ${email!"-"}
Feedback - ${comments!"-"}

<#break>

<#case "STEDT">
PNR : ${pnr!"-"}
Route : ${route!"-"}
Travel Date : ${travelDate!"-"}

Seat Status : ${seatStatus!"-"}

Updated by : ${updatedBy!"-"}
Updated at : ${updatedAt!"-"}
<#break>

<#case "VHCASG">
Trip Name : ${tripName!"-"}
Service No. : ${serviceNumber!"-"}
Vehicle No. : ${vehicleNumber!"-"}
Driver Name : ${driverName!"-"}
Driver No. : ${driverNumber!"-"}

Updated by : ${updatedBy!"-"}
updatedAt : ${updatedAt!"-"}
<#break>

<#case "RESHL">
FIRST TICKET:

PNR : ${oldPnr!"-"}
Fare : ${oldFare!"-"}
DOJ : ${oldDoj!"-"}
Route : ${route!"-"}
Booked By : ${bookedBy!"-"}

RESCHDULED TICKET:

PNR : ${pnr!"-"}
Fare : ${fare!"-"}
DOJ : ${doj!"-"}
Reschedule Charges : ${rescheduleCharges!"-"}
Transfered By : ${transferedBy!"-"}
Transfered At : ${transferedAt!"-"}
<#break>

<#case "TRSTS">
${title!"-"}

<#list travelStatus as status>
${status!"-"}
</#list>
</#switch>

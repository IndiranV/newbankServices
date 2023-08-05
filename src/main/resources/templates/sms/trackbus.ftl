<#switch namespaceCode>
	<#case "tattravels">
Dear ${passengerName!"-"},
Your travel from ${fromStationName!"-"} to ${toStationName!"-"}
is going to start at ${travelDatetime!"-"}.

please reach your boarding point before 15 minutes.

<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Bus No:${busNumber!"-"}
Contact No:${busContact!"-"}
<#if mapurl?has_content>
Board locate: ${mapurl!"-"}
</#if>

<#if trackBus?has_content>
Track Bus: ${trackBus!"-"}
</#if>

${domainUrl!"-"}
	<#break>
	<#case "muthumari">
Dear ${passengerName!"-"},
Your travel from ${fromStationName!"-"} to ${toStationName!"-"}
is going to start at ${travelDatetime!"-"}.

please reach your boarding point before 15 minutes.

<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Bus No: ${busNumber!"-"}
Contact No: ${busContact!"-"}

<#if trackBus??>
Track Bus: ${trackBus!"-"}
</#if>

Log on ${domainUrl!"-"} to get flat 10% offer
	<#break>
	<#case "royalvoyage">
Dear ${passengerName!"-"},
Your travel from ${fromStationName!"-"} to ${toStationName!"-"}
is going to start at ${travelDatetime!"-"}.

please reach your boarding point before 15 minutes.

<#if seatName?has_content>
Seat : ${seatName!"-"}
</#if>
<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Bus No: ${busNumber!"-"}
Contact No: ${busContact!"-"}
<#if mapurl?has_content>
Boarding locate: ${mapurl!"-"}
</#if>

<#if trackBus??>
Track Bus: ${trackBus!"-"}
</#if>
Alcoholic beverages not allowed

${domainUrl!"-"}
	<#break>
	<#case "ksmbus">
Dear ${passengerName!"-"},
Your travel from ${fromStationName!"-"} to ${toStationName!"-"}
is going to start at ${travelDatetime!"-"}.

please reach your boarding point before 15 minutes.

<#if seatName?has_content>
Seat : ${seatName!"-"}
</#if>
<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Bus No: ${busNumber!"-"}
Contact No: ${busContact!"-"}
<#if mapurl?has_content>
Boarding locate: ${mapurl!"-"}
</#if>

<#if trackBus??>
Track Bus: ${trackBus!"-"}
</#if>
Please wear a mask, carry your own blankets and ensure your temperature is normal in line with Covid Safety guidelines.

We have maintained high standards of sanitization for your safety because peace of mind is the best form of luxury we can give you.

${domainUrl!"-"}
	<#break>
	<#case "rajeshtransports">
Dear ${passengerName!"-"},
Your travel from ${fromStationName!"-"} to ${toStationName!"-"}
is going to start at ${travelDatetime!"-"}.

please reach your boarding point before 15 minutes.

<#if seatName?has_content>
Seat : ${seatName!"-"}
</#if>
<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Bus No: ${busNumber!"-"}
Contact No: ${busContact!"-"}
<#if mapurl?has_content>
Boarding locate: ${mapurl!"-"}
</#if>

<#if trackBus??>
Track Bus: ${trackBus!"-"}
</#if>
Please wear a mask, carry your own blankets and ensure your temperature is normal in line with Covid Safety guidelines.

We have maintained high standards of sanitization for your safety because peace of mind is the best form of luxury we can give you.

${domainUrl!"-"}
	<#break>
	<#default>
Dear ${passengerName!"-"},
Your travel from ${fromStationName!"-"} to ${toStationName!"-"}
is going to start at ${travelDatetime!"-"}.

<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Bus No: ${busNumber!"-"}
Driver No: ${busContact!"-"}

<#if mapurl?has_content>
Boarding locate: ${mapurl!"-"}
</#if>

<#if trackBus??>
Track Bus: ${trackBus!"-"}
</#if>

${domainUrl!"-"}
	<#break>
</#switch>
<#switch namespaceCode>
	<#case "smrtravels">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}

Seat : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>

Boarding : ${boarding!"-"} at : ${time!"-"}

Your ticket confirmed and you have to pay while boarding
	<#break>
	<#case "maduraiharsha">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}

Seat : ${seats!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}

Your ticket confirmed and you have to pay while boarding
	<#break>
	<#case "joytravels">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}

Seat : ${seats!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}

Your ticket confirmed and you have to pay while boarding
	<#break>
	<#case "thangamtravels">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}

Seat : ${seats!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}

Your ticket confirmed and you have to pay while boarding
	<#break>
	<#case "tattravels">
Thanks for using ${travelsName!"-"}

You have to pay ticket amount while boarding

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}
Bus Type : ${busType!"-"}

Seat : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>

<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${boardingAddress!"-"}

Contact : ${contact!"-"}

<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

Your ticket is phone booked, confirm your ticket before 3 hrs of departure, failure on this will lead to cancellation of ticket.
	<#break>
	<#case "psktravels">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}

Seat : ${seats!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${boardingAddress!"-"}

Your ticket confirmed and you have to pay while boarding
	<#break>
	<#case "rkgktstravels">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}

Seat : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${boardingAddress!"-"}

Your ticket confirmed and you have to pay while boarding
	<#break>
	<#case "sornatravels">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}

Seat : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${boardingAddress!"-"}

Your ticket confirmed and you have to pay while boarding
	<#break>
	<#case "srivenkataramana">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}

Seat : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${boardingAddress!"-"}

Your ticket is under phone blocked and you have to pay while boarding
	<#break>
	<#case "surajtravels">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}

Seat : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${boardingAddress!"-"}

Your ticket is under phone blocked and you have to pay while boarding
	<#break>
	<#case "ashokatravels">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}

Seat : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}

Your ticket is under phone blocked and you have to pay while boarding
	<#break>
	<#case "jrtravels">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}

Seat : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${boardingAddress!"-"}

Your ticket is under phone blocked please confirm or pay at the time of boarding
	<#break>
	<#case "lokeswari">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}

Seat : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}

Your ticket confirmed and you have to pay while boarding
	<#break>
<#case "neeveetravels">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}
<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>

Seat : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}

Your ticket confirmed and you have to pay while boarding

*Extra luggage and boxes will be charged as per size
	<#break>
<#case "sgstravels">
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}
<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>

Seat : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}

Your ticket confirmed and you have to pay while boarding

*Extra luggage and boxes will be charged as per size
	<#break>
	<#default>
Thanks for using ${travelsName!"-"}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}
<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>

Seat : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Contact : ${boardingContact!"-"}

Your ticket confirmed and you have to pay while boarding 
<#if linkpay?has_content>
or you can pay using this link ${linkpay!"-"}
</#if>
	<#break>
</#switch>
<#switch namespaceCode>
	<#case "mgmt">
		PNR# ${pnr!"-"}, From: ${originName!"-"} to ${destinationName!"-"},on ${travelDate!"-"} ${departureTime!"-"},Name :  ${travelsName!"-"},Seat # ${seats!"-"}-${gendar!"-"},your ticket has been confirmed.
	<#break>
	<#case "test">
		PNR# ${pnr!"-"}, From: ${originName!"-"} to ${destinationName!"-"},on ${travelDate!"-"} ${departureTime!"-"},Name :  ${travelsName!"-"},Seat # ${seats!"-"}-${gendar!"-"},your ticket has been confirmed.
	<#break>
	<#case "tattravels">
Thanks for using ${travelsName}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}
Bus Type : ${busType!"-"}

<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Seat : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>

Boarding : ${boarding!"-"} at : ${time!"-"}
Boarding Address: ${boardingAddress!"-"},
Contact : ${boardingContact!"-"}

<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

www.tattravels.com

Mobile App 
http://goo.gl/Ove8xJ
	<#break>
	<#case "srivenkataramana">
Thanks for using ${travelsName}

PNR : ${pnr!"-"}
${originName!"-"} - ${destinationName!"-"}
On : ${travelDate}
Bus Type : ${busType!"-"}

<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Seat : ${seats!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

Download our APP now http://bit.ly/2Z0HASc

${namespaceURL!"-"}
	<#break>
	<#case "skylinetravels">
Hi ${name!"-"}
Thanks for using ${travelsName}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate}

Seat(s) : ${seats!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Contact : ${contact!"-"}

www.skylinekerala.com
	<#break>
	<#case "maduraiharsha">
Hi ${name!"-"}
Thanks for using ${travelsName}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate}

Seat(s) : ${seats!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Contact : ${contact!"-"}

www.maduraiharshatravels.com
	<#break>
	<#case "deepthitravels">
Hi ${name!"-"}
Thanks for using ${travelsName}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate}

Seat(s) : ${seats!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Contact : ${contact!"-"}

<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

${namespaceURL!"-"}
	<#break>
	<#case "gprholidays">
Hi ${name!"-"}
Thanks for using ${travelsName}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate}

Seat(s) : ${seats!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Contact : ${contact!"-"}

<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

${namespaceURL!"-"}
	<#break>
	<#case "lokeswari">
Hi ${name!"-"}
Thanks for using ${travelsName}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate}

Seat(s) : ${seats!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Contact : ${contact!"-"}

<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

${namespaceURL!"-"}
	<#break>
	<#case "thangamtravels">
Hi ${name!"-"}
Thanks for using ${travelsName}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate}

Seat(s) : ${seats!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Contact : ${contact!"-"}

www.thangamtravels.in
	<#break>	
	<#case "noonetravels">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Seat(s) : ${seats!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

Happy journey.
	<#break>
	<#case "ramyatravels">
Thanks for choosing ${travelsName!"-"}.
PNR: ${pnr!"-"},
${originName!"-"} to ${destinationName!"-"}, 
Travel Date: ${travelDate!"-"},
Seat no: ${seats!"-"},

Boarding point: ${boarding!"-"},
Boarding Date: ${boardingDate!"-"},
Boarding Time: ${time!"-"},
Boarding Address: ${boardingAddress!"-"},
<#if landmark?has_content>
Landmark: ${landmark!"-"},
</#if>

Contact: ${boardingContact!"-"}
Contact Person: ${boardingContactName!"-"}

Wish you a safe and happy journey.
	<#break>
	<#case "tranzking">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Seat(s) : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

<#if boardingLandmark?has_content>
Landmark: ${boardingLandmark!"-"},
</#if>

${namespaceURL!"-"}
	<#break>
	<#case "evacaybus">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${boardingDate!"-"}

Seat(s) : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

<#if boardingLandmark?has_content>
Landmark: ${boardingLandmark!"-"},
</#if>
<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

${namespaceURL!"-"}

*Carton Boxes and other bigger luggages will not be allowed in our Buses.
	<#break>
	<#case "ashwintravels">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${boardingDate!"-"}

Seat(s) : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

<#if boardingLandmark?has_content>
Landmark: ${boardingLandmark!"-"},
</#if>
<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

please reach your boarding point before 15 minutes.

${namespaceURL!"-"}
	<#break>
	<#case "arthitravels">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Seat(s) : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

${namespaceURL!"-"}

Download our Mobile APP http://goo.gl/eyN5b3
	<#break>
	<#case "rkgktstravels">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Seat(s) : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

${namespaceURL!"-"}
	<#break>
	<#case "arlbus">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Seat(s) : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

${namespaceURL!"-"}
	<#break>
	<#case "surajtravels">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Seat(s) : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

${namespaceURL!"-"}
	<#break>
	<#case "sornatravels">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Seat(s) : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

${namespaceURL!"-"}
	<#break>
<#case "ashokatravels">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Seat(s) : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

${namespaceURL!"-"}
	<#break>
<#case "suryatravelsap">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Seat(s) : ${seats!"-"}
Bus Type : ${busType!"-"}
<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}
<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

${namespaceURL!"-"}
	<#break>
<#case "vikramtravels">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Seat(s) : ${seats!"-"}
Bus Type : ${busType!"-"}
<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}
<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

${namespaceURL!"-"}
	<#break>
<#case "newpooja">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Seat(s) : ${seats!"-"}
Bus Type : ${busType!"-"}
<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}
<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

${namespaceURL!"-"}
	<#break>
<#case "amarnath">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Seat(s) : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>
Bus Type : ${busType!"-"}
<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}
<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

Luggage at your own risk

${namespaceURL!"-"}
	<#break>
<#case "neeveetravels">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Seat(s) : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}
<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

${namespaceURL!"-"}

*Extra luggage and boxes will be charged as per size
	<#break>
<#case "sgstravels">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Seat(s) : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

${namespaceURL!"-"}

*Extra luggage and boxes will be charged as per size
	<#break>
<#case "srkttravels">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Seat(s) : ${seats!"-"}
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}
<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

${namespaceURL!"-"}
	<#break>
<#case "royalvoyage">
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Seat(s) : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}

${namespaceURL!"-"}
	<#break>
	<#default>
Hi ${name!"-"}
Thanks for using ${travelsName!"-"}

PNR# : ${pnr!"-"}
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>
Seat(s) : ${seats!"-"}
<#if fare != "0.00">
Fare : ${fare!"-"}
</#if>
Bus Type : ${busType!"-"}

Boarding : ${boarding!"-"} at : ${time!"-"}
Address : ${contact!"-"}
<#if mapurl?has_content>
Map : ${mapurl!"-"}
</#if>

${namespaceURL!"-"}
	<#break>
</#switch>
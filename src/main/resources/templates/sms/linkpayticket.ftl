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

${notes!"-"}
<#if paymentLink?has_content>
Payment link ${paymentLink!"-"}
</#if>
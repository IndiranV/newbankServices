${ticketStatus!"-"}

PNR : ${pnr!"-"}
Seats : ${seats!"-"}
<#if serviceNumber?has_content>
Service #: ${serviceNumber!"-"}
</#if>

${originName!"-"} - ${destinationName!"-"}
DOJ: ${travelDate!"-"}
Fare: ${fare!"-"}

${bookedBy!"-"}
${bookedDate!"-"} ${time!"-"}
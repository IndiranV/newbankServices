New Ticket <#if vehicleNumber?has_content>- ${vehicleNumber!"-"} </#if>
<#if serviceNumber?has_content>
[${serviceNumber!"-"}]
</#if>

${pnr!"-"}
${seats!"-"}
${fare!"-"}

${passengerName!"-"} - ${mobileNumber!"-"}

${originName!"-"} - ${destinationName!"-"}
${boardingName!"-"}
${boardingDate!"-"} ${time!"-"}

${bookedBy!"-"}
${bookedAtDate!"-"} ${bookedAtTime!"-"}
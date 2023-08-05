<#switch namespaceCode>
	<#case "tattravels1">
PNR#: ${pnr!"-"}, Seat : ${seats!"-"} has been Cancelled,
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

www.tattravels.com
	<#break>
	<#case "srivenkataramana">
Dear ${name!"-"},

PNR#: ${pnr!"-"}, Seat : ${seats!"-"} has been Cancelled with refund amount ${refund!"-"}

${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}

Thanks for using ${travelsName!"-"}
	<#break>
	<#default>
PNR#: ${pnr!"-"} Cancelled Successfully,
${originName!"-"} to ${destinationName!"-"}
On : ${travelDate!"-"}
Seat#: ${seats!"-"}
Thanks for using ${travelsName!"-"}
	<#break>
</#switch>
<#switch namespaceCode>
	<#case "tattravels">
Dear ${passengerName!"-"},

PNR ${ticketCode!"-"}, 

Your boarding is changed to ${stationPointName!"-"} at ${stationPointTime!"-"} due to ${reason!"-"}.

Please call ${supportNumber!"-"} for more details. 

${travelsName!"-"}
	<#break>
	<#default>
Dear ${passengerName!"-"},

PNR ${ticketCode!"-"}, 

Your boarding is changed to ${stationPointName!"-"} at ${stationPointTime!"-"} due to ${reason!"-"}.

Please call ${supportNumber!"-"} for more details. 

${travelsName!"-"}
	<#break>
</#switch>
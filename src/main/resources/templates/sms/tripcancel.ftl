<#switch namespaceCode>
	<#case "tattravels">
PNR ${ticketCode!"-"},

Your Trip ${fromStation!"-"} - ${toStation!"-"} is cancelled due to ${reason!"-"}.

Please call ${supportNumber!"-"} for more details. 

Sorry for the inconvenience

${travelsName!"-"}
	<#break>
	<#default>
PNR ${ticketCode!"-"},

Your Trip ${fromStation!"-"} - ${toStation!"-"} is cancelled due to ${reason!"-"}.

Please call ${supportNumber!"-"} for more details. 

${travelsName!"-"}
	<#break>
</#switch>
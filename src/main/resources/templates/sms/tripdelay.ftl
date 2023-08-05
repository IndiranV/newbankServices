<#switch namespaceCode>
	<#case "tattravels">
Dear ${passengerName!"-"}, 

Due to ${reason!"-"}, your bus will be delayed by ${delayTime!"-"}

Please call ${supportNumber!"-"} for more details. 

${travelsName!"-"}
	<#break>
	<#default>
Dear ${passengerName!"-"}, 

Due to ${reason!"-"}, your bus will be delayed by ${delayTime!"-"}

Please call ${supportNumber!"-"} for more details. 

${travelsName!"-"}
	<#break>
</#switch>
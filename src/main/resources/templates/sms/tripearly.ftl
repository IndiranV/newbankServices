<#switch namespaceCode>
	<#case "tattravels">
Dear ${passengerName!"-"}, 

Due to ${reason!"-"}, your bus will be started ${earlyTime!"-"} early from your actual departure time.

Please call ${supportNumber!"-"} for more details. 

${travelsName!"-"}
	<#break>
	<#default>
Dear ${passengerName!"-"}, 

Due to ${reason!"-"}, your bus will be started ${earlyTime!"-"} early from your actual departure time.

Please call ${supportNumber!"-"} for more details. 

${travelsName!"-"}
	<#break>
</#switch>
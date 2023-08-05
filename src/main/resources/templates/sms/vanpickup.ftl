<#switch namespacecode>
	<#case "goldentravels">
Dear ${passengerName!"-"},
Your pickup bus will arrive at ${travelDatetime!"-"}.

Bus No: ${vanNumber!"-"}
Driver Name: ${supportName!"-"}
Contact No: ${vanContact!"-"}

Track Bus: ${trackBus!"-"}

${domainUrl!"-"}
	<#break>

	<#default>
Dear ${passengerName!"-"},
Your pickup van will arrive at ${travelDatetime!"-"}.

Van No: ${vanNumber!"-"}
Driver Name: ${supportName!"-"}
Contact No: ${vanContact!"-"}

Track Van: ${trackBus!"-"}

${domainUrl!"-"}
	<#break>
</#switch>
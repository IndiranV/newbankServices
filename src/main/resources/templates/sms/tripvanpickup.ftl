<#switch namespaceCode>
	<#case "goldentravels">
Dear ${passengerName!"-"},

Your pickup Bus details

Bus No: ${vanNumber!"-"}
Driver Name: ${supportName!"-"}
Contact No: ${vanContact!"-"}
	<#break>
	<#default>
Dear ${passengerName!"-"},

Your pickup van details

Van No: ${vanNumber!"-"}
Driver Name: ${supportName!"-"}
Contact No: ${vanContact!"-"}
	<#break>
</#switch>
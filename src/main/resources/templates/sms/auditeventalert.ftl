<#switch eventType>
	<#case "SCHEVE">
		Schedule ${serviceNumber} is updated by ${handledBy} at ${dateTime}.
		${domainUrl!}
	<#break>
	<#case "SCHFREVE">
		${fareOverrideType} ${seatFare} is updated for ${serviceNumber} in between ${dateRange} by ${handledBy} at ${dateTime}.
		${domainUrl!}
	<#break>
	<#case "USREVE">
		User ${name} is updated by ${handledBy} at ${dateTime}.
		${domainUrl!}
	<#break>
	<#case "USPAYEVE">
		RS.${transactionAmount} User payment is updated for ${name} by ${handledBy} at ${dateTime}.
		${domainUrl!}
	<#break>
	<#case "TRPEVE">
		Trip ${code} is updated by ${handledBy} at ${dateTime}.
		${domainUrl!}
	<#break>
	<#case "TCKBOEVE">
		Ticket ${ticketCode} is booked in ${serviceNo} on ${travelDate} via ${deviceMedium} by ${handledBy} at ${dateTime}.
		${domainUrl!}
	<#break>
	<#case "TCKCAEVE">
		Ticket ${ticketCode} is cancelled in ${serviceNo} on ${travelDate} via ${deviceMedium} by ${handledBy} at ${dateTime}.
		${domainUrl!}
	<#break>
	<#case "BUSEVE">
		Bus ${name} is updated by ${handledBy} at ${dateTime}.
		${domainUrl!}
	<#break>
	<#case "DISEVE">
		Discount ${discount} is updated for ${forUserGroup} by ${handledBy} at ${dateTime}.
		${domainUrl!}
	<#break>
</#switch>
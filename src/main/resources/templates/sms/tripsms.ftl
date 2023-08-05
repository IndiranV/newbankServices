<#switch namespacecode>
	<#case "mgmt">
		PNR# ${pnr!"-"}, From: ${originName!"-"} to ${destinationName!"-"},on ${travelsDate!"-"} ${departureTime!"-"},Name :  ${travelsName!"-"},Seat # ${seats!"-"}-${gendar!"-"},your ticket has been confirmed.
	<#break>

	<#default>
	Dear ${TNAME!"-"},
	Your Bus ${FROM!"-"} To ${TO!"-"} is reaching ${BOARDINGPT!"-"} at ${BRTIME!"-"}.
	BusNo:${BUSNO!"-"}.
	Bus contact:${CONTACT!"-"}.
	
	${MSG!"-"}
	<#break>
</#switch>
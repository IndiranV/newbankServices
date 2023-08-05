<#switch namespaceCode>
	<#case "tattravels1">
${OTPNumber!"-"} is your NETSECURE code to cancel your Transaction is valid for 30 mins

Do not share with anyone. 

${domainUrl!"-"}
	<#break>
	<#default>
${OTPNumber!"-"} is your OTP to your transaction, valid for 30 minutes

${domainUrl!"-"}
	<#break>
</#switch>
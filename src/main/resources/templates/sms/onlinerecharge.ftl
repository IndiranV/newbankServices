<#switch namespaceCode>
	<#case "tattravels">
${firstName!"-"}, Rs.${amount!"-"} is successfully recharged to your account.

${namespaceName!"-"}
	<#break>
	<#default>
${firstName!"-"}, Rs.${amount!"-"} is successfully recharged to your account.

${namespaceName!"-"}
	<#break>
</#switch>
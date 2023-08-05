<#switch namespaceCode>
	<#case "tranzking">
		<#if devicemedium?has_content && devicemedium != "APP">
RS.500/- plus exciting offers exclusively for you in Tranzking mobile app..! Hurry. Register today.

https://m.tranzking.com/app

For more offers 9696360000
		<#else>
RS.500/- plus exciting offers exclusively for you in Tranzking mobile app..! Hurry. Register today.

https://m.tranzking.com/app

For more offers 9696360000
		</#if>
	<#break>
	<#case "ashwintravels">
Thanks for travelling with Ashwin Roadways

Please leave us your valuable feedback ${feedbackUrl!"-"} Or Call us 9841565555

${domainUrl!"-"}
	<#break>
	<#case "vkvtravels">
Thanks for travelling with VKV Travels

Please leave us your valuable feedback ${feedbackUrl!"-"} Or Call us 9840964716

${domainUrl!"-"}
	<#break>
	<#case "poornatravels">
Dear Customer

We are glad you chose to travel with Poorna Travels. Complaint or Compliment, we'd love to hear from you.

Call us 9962910074 or ${feedbackUrl!"-"}

${domainUrl!"-"}
	<#break>
	<#case "srisrinivasa">
Dear Customer

We are glad you chose to travel with Sri Srinivasa Travels. Complaint or Compliment, we'd love to hear from you.

Call us 96559 20296, 98424 20296 or ${feedbackUrl!"-"}

${domainUrl!"-"}
	<#break>
	<#case "wintravels">
Thanks for travelling with Win Tours and Travels

Please leave us your valuable feedback ${feedbackUrl!"-"} Or Call us 9840309993

${domainUrl!"-"}
	<#break>
	<#case "sriharish">
Thanks for travelling with Sri Harish Travels

Please leave us your valuable feedback ${feedbackUrl!"-"} Or Call us 9940397966

${domainUrl!"-"}
	<#break>
	<#case "veekaytravels">
Thank You for choosing Veekay Travels

Please leave us your feedback ${feedbackUrl!"-"} Or r Call us 7540002495 to serve you better.

${domainUrl!"-"}
	<#break>
	<#case "gokultravels">
Thanks for journey with Gokul Travels

Please leave us your feedback ${feedbackUrl!"-"} Or Call us 7825884422 / 12 to serve you better.

${domainUrl!"-"}
	<#break>
	<#default>
Thanks for journey with ${namespaceName!"-"}

Please leave us your feedback ${feedbackUrl!"-"} Or Call us 7825884422 / 12 to serve you better.

${domainUrl!"-"}
	<#break>
</#switch>
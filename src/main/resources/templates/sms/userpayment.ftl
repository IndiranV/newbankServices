${firstName!"-"}, ${transactionTypeName!"-"} Rs.${amount!"-"} is<#if paymentStatusCode == "INITD" || paymentStatusCode == "RJECT"> ${paymentStatusName!"-"}. <#elseif paymentStatusCode == "ACKED" || paymentStatusCode == "PAID" || paymentStatusCode == "PAPAID"> ${paymentStatusName!"-"} successfully.</#if>

${namespaceName!"-"}

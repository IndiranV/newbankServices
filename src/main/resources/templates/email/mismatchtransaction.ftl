<html>
	<p>Hi Team,
		<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Transaction of ${travelsName!"-"} Ticket Transaction mismatch details.
	</p>
	<br/><br/>
	<table align="center" cellspacing="2" cellpadding="1" width="100%" style="font-size:11.5px; border:0.4px dashed lightgrey;">
		<tr bgcolor='#F5F2F2'>
			<th style="border-right:1px solid grey;">Transaction Code</th>
			<th style="border-right:1px solid grey;">Transaction Mode</th>
			<th style="border-right:1px solid grey;">Transaction Type</th>
			<th style="border-right:1px solid grey;">Transaction Amount</th>
			<th style="border-right:1px solid grey;">Commission Amount</th>
			<th style="border-right:1px solid grey;">TDS Tax</th>
			<th style="border-right:1px solid grey;">Ac Bus Tax</th>
			<th style="border-right:1px solid grey;">Addon Amount</th>
			<th style="border-right:1px solid grey;">Status</th>
		</tr>
		<#list transactionDetails as transaction>
		<tr>
			<td>${transaction.code!"-"}</td>
			<td>${transaction.transactionMode!"-"}</td>
			<td>${transaction.transactionType!"-"}</td>
			<td>${transaction.transactionAmount!"-"}</td>
			<td>${transaction.commissionAmount!"-"}</td>
			<td>${transaction.tdsTax!"-"}</td>
			<td>${transaction.acBusTax!"-"}</td>
			<td>${transaction.addonAmount!"-"}</td>
			<td>${transaction.status!"-"}</td>
		</tr>
		</#list>
	</table>
</html>
<html>
	<p>Hi Team,
		<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; Transaction of ${travelsName!"-"} closing balance mismatch details.
	</p>
	<br/><br/>
	<table align="center" cellspacing="2" cellpadding="1" width="100%" style="font-size:11.5px; border:0.4px dashed lightgrey;">
		<tr bgcolor='#F5F2F2'>
			<th style="border-right:1px solid grey;">PNR</th>
			<th style="border-right:1px solid grey;">User Code</th>
			<th style="border-right:1px solid grey;">User Name</th>
			<th style="border-right:1px solid grey;">Transaction Date</th>
			<th style="border-right:1px solid grey;">Transaction Mode</th>
			<th style="border-right:1px solid grey;">Transaction Type</th>
			<th style="border-right:1px solid grey;">Transaction Amount</th>
			<th style="border-right:1px solid grey;">Commission Amount</th>
			<th style="border-right:1px solid grey;">TDS Tax</th>
			<th style="border-right:1px solid grey;">Credit Amount</th>
			<th style="border-right:1px solid grey;">Debit Amount</th>
			<th>Closing Balance</th>
		</tr>
		<#list transactionDetails as transaction>
		<tr>
			<td>${transaction.pnr!"-"}</td>
			<td>${transaction.userCode!"-"}</td>
			<td>${transaction.userName!"-"}</td>
			<td>${transaction.transactionDate!"-"}</td>
			<td>${transaction.transactionMode!"-"}</td>
			<td>${transaction.transactionType!"-"}</td>
			<td>${transaction.transactionAmount!"-"}</td>
			<td>${transaction.commissionAmount!"-"}</td>
			<td>${transaction.tdsTax!"-"}</td>
			<td>${transaction.creditAmount!"-"}</td>
			<td>${transaction.debitAmount!"-"}</td>
			<td>${transaction.closingBalance!"-"}</td>
		</tr>
		</#list>
	</table>
</html>
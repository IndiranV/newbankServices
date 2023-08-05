<html><table><tr><td>Dear ${name!"-"},
</td></tr>
<tr><td> &nbsp;&nbsp;&nbsp;Thanks for using our Online Bus Ticket Services through ${website!"-"}. Your e-ticket has been booked  and the details are indicated below. </td></tr>
<tr><td><br><br><fieldset> <legend>Travels Information:</legend>
<table><tr><th>Origin </th><td>:</td>&nbsp;&nbsp;&nbsp;<td>${origin!"-"}</td></tr>
<tr><th>Destianation </th><td>:</td>&nbsp;&nbsp;&nbsp;<td>${destination!"-"}</td></tr>
<tr><th>Travel date </th><td>:</td>&nbsp;&nbsp;&nbsp;<td>${travelDate!"-"}</td></tr>
<tr><th>Travel time </th><td>:</td>&nbsp;&nbsp;&nbsp;<td>${travelTime!"-"}</td></tr>
<tr><th>Boarding Time </th><td>:</td>&nbsp;&nbsp;&nbsp;<td>${boardingPointTime!"-"}</td></tr>
<tr><th>Boarding point </th><td>:</td>&nbsp;&nbsp;&nbsp;<td>${boardingPoint!"-"}</td></tr>
<tr><th>Contact No </th><td>:</td>&nbsp;&nbsp;&nbsp;<td>${contact!"-"}</td></tr>
<tr><th>Total Amount </th><td>:</td>&nbsp;&nbsp;&nbsp;<td>${fare!"-"}</td></tr></table>
</fieldset></td></tr>
<tr><td> <br><br><fieldset> <legend>Passenger Information:</legend>
<table><tr align='center'><th>Name</th><th>Seat No</th><th>Gender</th><th>Age</th></tr>

<#list ticketDetails as passenger>
	<tr>
	<td width='35%' height='24' align='left'>${passenger.passengerName!"-"}</td>
	<td width='15%' height='24' align='center' >${passenger.seatName!"-"}</td>
	<td width='5%' height='24' align='center'>${passenger.gender!"-"}</td>
	<td width='5%' height='24' align='center' >${passenger.passengerAge!"-"}</td>

	</tr>
</#list>
</table>
</fieldset></td></tr>
<tr><td>Please click on the following URL to access and print your e ticket</td></tr>
<tr><td> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="${ticketurl!"-"}"> ${ticketCode!"-"}</td></tr>
<tr><td><br><br><br>&nbsp;&nbsp;&nbsp;Regards</td></tr>
<tr><td>&nbsp;&nbsp;&nbsp;${operator!"-"}</td></tr>
</table><html>
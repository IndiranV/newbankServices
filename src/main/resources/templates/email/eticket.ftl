<#setting locale="en_IN">
<table width='100%' border='0' cellspacing='0' cellpadding='0' bgcolor='white'>
<tr>
  <td align='right' valign='top' style='border-top: 1px dotted gray; font:normal 10px/20px arial; color:gray;'> Can't see the Mail or Picture? Select 'Always display images' or <a href='${messageInBrowserURL!"-"}'>view this message in your browser. </a> </td>
</tr>
</table>
<table width='100%' border='0' cellpadding='0' cellspacing='0' bgcolor='white'>
  <tr>
    <td bgcolor='gray' style='padding:7px;  background:LightGray ;'><table width='100%' border='0' cellspacing='0' cellpadding='0' bgcolor='white' style='     box-shadow: 0 0 3px rgba(0, 0, 0, 0.4);'>
        <tr>
        <table width='100%' bgcolor='lightgray' border='0' ><tr>
		<td  height="55" width='50%' valign="left"  bgcolor="lightgray" > <a href="http://www.ticketgoose.com/" target="_blank" style="text-decoration:none; outline:none; color:black; padding:5px;" title="www.ticketgoose.com"> <img src="${logoURL!"-"}" alt="Ticketgoose Logo" height="70px" width="152px"> </a></td>
		<td  height="55" width='50%'  valign="right"  bgcolor="lightgray" style="font:35px 'Times New Roman'; color:black; text-align:right; border-bottom:1px solid silver;" ><a href="http://www.ticketgoose.com/" target="_blank" style="text-decoration:none; outline:none; color:black; padding:5px;" title="www.ticketgoose.com">www.<span style="color:green;">ticket</span><span style="color:orange;">goose</span>.com</a></td>
        </tr></table>
        </tr>
        <tr>
          <td height='35' align='left' valign='middle' style='color: green;  font: bold 11pt arial;  border-bottom:1px solid green; padding-left:3px; '> Reservation Ticket</td>
        </tr>
        <tr>
          <td><table width='100%' border='0' cellpadding='3' cellspacing='0' bgcolor='white'>
              <tr>
                <td width='30%' height='18' style=' color: green;  font-size: 12px; font-weight: bold; font-family: Verdana;'>Ticket PNR No </td>
                <td width='2%' height='10' align='center' style=' font:bold 12px  Verdana; '>:</td>
                <td width='69%' height='10' style=' font:normal 12px  Verdana;'>${ticketPNR!"-"}</td>
              </tr>
              <tr>
                <td width='30%' height='18' style=' color: green;  font:bold 12px  Verdana;'>Travels Name</td>
                <td width='2%' height='18' align='center' style=' font:bold 12px  Verdana; '>:</td>
                <td width='69%' height='18' style=' font:normal 12px  Verdana;'>${travelsName!"-"}</td>
              </tr>
              <tr>
                <td width='30%' height='18' style=' color: green;  font-size: 12px; font-weight: bold; font-family: Verdana;'>Ticket Booking</td>
                <td width='2%' height='18' align='center' style=' font:bold 12px  Verdana;'>:</td>
                <td width='69%' height='18' style=' font:normal 12px  Verdana; text-transform:capitalize;'>${fromStationName!"-"} - ${toStationName!"-"}</td>
              </tr>
              <tr>
                <td width='30%' height='18' style=' color: green;  font-size: 12px; font-weight: bold; font-family: Verdana;'>Boarding Point Name </td>
                <td width='2%' height='18' align='center' style=' font:bold 12px  Verdana;'>:</td>
                <td width='69%' height='18' style=' font:normal 12px  Verdana;'>${boardingPointName!"-"}</td>
              </tr>
              <tr>
                <td width='30%' height='18' style=' color: green;  font-size: 12px; font-weight: bold; font-family: Verdana;'>Travel Date Time </td>
                <td width='2%' height='18' align='center' style=' font:bold 12px  Verdana;'>:</td>
                <td width='69%' height='18' style=' font:normal 12px  Verdana;'>${travelDateTime!"-"}</td>
              </tr>
              <tr>
                <td width='30%' height='18' style=' color: green;  font-size: 12px; font-weight: bold; font-family: Verdana;'>Travels Contact No </td>
                <td width='2%' height='18' align='center' style=' font:bold 12px  Verdana;'>:</td>
                <td width='69%' height='18' style=' font:normal 12px  Verdana;'>${travelsContact!"-"}</td>
              </tr>
              <tr>
                <td width='30%' height='18' bordercolor='0' style=' color: green;font-size: 12px;font-weight: bold; font-family: Verdana;'>Fare </td>
                <td width='2%' height='18' align='center' style=' font:bold 12px  Verdana;'>:</td>
                <td width='69%' height='18' style=' font:normal 12px  Verdana;'>${fare!"-"}</td>
              </tr>
              <tr>
                <td height='10' colspan='3' bordercolor='0'><hr style=' border-top: 1px dotted gray;'/></td>
              </tr>
            </table></td>
        </tr>
        <tr>
          <td align='left' valign='top'><table width='100%' border='0' cellpadding='3' cellspacing='0' bgcolor='white' style=' font:normal 12px  Verdana;'>
              <tr>
                <td width='35%' height='24' align='left' style='color: green; font-weight:bold;'>Passenger Name</td>
                <td width='15%' height='24' align='center' style='color: green; font-weight:bold;' >Seat No.</td>
                <td width='5%' height='24' align='center' style='color: green; font-weight:bold;'>Sex</td>
                <td width='5%' height='24' align='center' style='color: green; font-weight:bold;' >Age</td>
                <td width='15%' height='24' align='center' style='color: green; font-weight:bold;'>Mobile</td>
                <td width='25%' height='24' align='center' style='color: green; font-weight:bold;'>Seat Info </td>
              </tr>
		<#list passengerList as passenger>
              <tr>
                <td width='35%' height='24' align='left'>${passenger.name!"-"}</td>
                <td width='15%' height='24' align='center' >${passenger.seatNumber!"-"}</td>
                <td width='5%' height='24' align='center'>${passenger.sex!"-"}</td>
                <td width='5%' height='24' align='center' >${passenger.age!"-"}</td>
                <td width='15%' height='24' align='center'>${passenger.mobile!"-"}</td>
                <td width='25%' height='24' align='center'>${passenger.seatInfo!"-"}</td>
              </tr>
		</#list>

             </table></td>
        </tr>
        <tr>
          <td><table width='100%' border='0' cellpadding='3' cellspacing='0' bgcolor='white' style='color: black; font:normal 12px  Verdana;'>
              <tr>
                <td height='20' align='left'  style='color: green;  font: bold 11pt arial;  border-bottom:1px solid green; '>Cancellation terms (Charges per seat)</td>
                </tr>
              <tr>
                <td><div style='line-height:12px;'>
			<table>
			<#if statics["com.ticketgoose.helper.ObjectUtils"].isCollectionObject(cancellationTermsList)>
			      <tr>
			        <td width='55%' height='24' align='left' style='font-weight:bold;'></td>
			        <td width='15%' height='24' align='center' style='font-weight:bold;' >Deduction</td>
			        <td width='15%' height='24' align='center' style='font-weight:bold;'>Refund</td>
			        <td width='15%' height='24' align='center' style='font-weight:bold;' >Charges</td>
			      </tr>
			      <#list cancellationTermsList as cancellationTerms>
			          <tr>
			            <td width='55%' height='24' align='left'>${cancellationTerms.description!"-"}</td>
			            <td width='15%' height='24' align='center'>
			            <#if cancellationTerms.deductionAmount?? && (cancellationTerms.deductionAmount > 0) >
			            ${cancellationTerms.deductionAmount?string.currency}/-
			            <#else>
			            -
			            </#if>
			            </td>
			            <td width='15%' height='24' align='center'>
			            <#if cancellationTerms.refundAmount?? && (cancellationTerms.refundAmount > 0) >
			            ${cancellationTerms.refundAmount?string.currency}/-
			            <#else>
			            -
			            </#if>
			            </td>
			            <td width='15%' height='24' align='center'>
			            <#if cancellationTerms.value?? && (cancellationTerms.value > 0) >
			            ${cancellationTerms.value?string}${cancellationTerms.percentage?string("%","/-")}
			            <#else>
			            -
			            </#if>
			            </td>
			          </tr>
			      </#list>
			<#else>
				${cancellationTermsList!"-"}
			</#if>
			</table>
                  </div></td>
              </tr>
              <tr>
                <td height='20'> </td>
              </tr>
              <tr>
                <td height='20' style='color: green; font: bold 11pt arial; border-bottom:1px solid green; '>Terms and Condition: </td>
              </tr>
              <tr>
                <td><div style='line-height:22px;'>
                    <ol style='margin:0; padding:0;'>
                      <li style='list-style:inside decimal;'>Please ensure that Operator PNR is filled, otherwise the ticket is not valid</li>
                      <li style='list-style:inside decimal;'>The passenger is required to furnish a printout of the e-ticket and carry a valid photo  identity proof while traveling which must be produced on request</li>
                      <li style='list-style:inside decimal;'>The Ticket booked is not transferable</li>
                      <li style='list-style:inside decimal;'>Bus Operator reserves rights to change the seat while boarding, if adjacent seat is female</li>
                      <li style='list-style:inside decimal;'>All other conditions specific to a Bus Operator are applicable</li>
                    </ol>
                  </div></td>
              </tr>
            </table></td>
        </tr>
      </table></td>
  </tr>
</table>
<br/>
	24 * 7 Customer Support:<br/>
	088 80 80 80 80<br/>

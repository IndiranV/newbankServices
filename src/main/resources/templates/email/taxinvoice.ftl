<html>
  <center>
    <h2 style='font-family: Helvetica; border: 1px solid; border-radius: 10px; width:250px; padding-top: 4px; padding-bottom: 4px;' align='center'>TAX INVOICE</h2>
  </center>
    <table border='0' align='center' width='70%' cellspacing='0' cellpadding='3' bgcolor='white' style='min-width: 850px; border-color: #F5F2F2; font-family: Calibri;'>
        <tr style='height: 25px;'>
          <td colspan='4' width='35%'><b>Invoice To</b></td> 
          <td width='15%' align='left'><b>${namespaceTradeName!"-"}</b></td>
          <td width='10%' colspan='3' align='left'></td>
        </tr>
        <tr style='height: 10px;'>
          <td width='10%' colspan='4'><b>${customerTradeName!"-"}</b></td>
          <td width='25%' align='left'><b>GSTIN: </b>${namespaceGstin!"-"}</td>
          <td colspan='3' style='min-width: 140px;'></td>
        </tr>
        <tr style='height: 10px;'>
          <td width='10%' colspan='4'><b>GSTIN: </b>${customerGstin!"-"}</td>
          <td width='25%' colspan='1' align='left'><b>Invoice No</td>
          <td colspan='3' style='min-width: 140px;'><b>Date</b></td>
        </tr>
        <tr style='height: 10px;'>
          <td width='10%' colspan='4'><b>SAC Code: </b>${sacCode!"-"}</td>
          <td width='25%' colspan='1' align='left'>${ticketCode!"-"}</td>
          <td colspan='3' style='min-width: 140px;'>${date!"-"}</td>
        </tr>
  </table></br>

  <#setting number_format=",##0.00">
  <#setting locale="en_US">
  
  <table border='1' align='center' width='70%' cellspacing='0' cellpadding='6' bgcolor='white' style='min-width: 850px; border-color: #F5F2F2; font-family: Calibri;'>
        <tr style='height: 35px;'>
          <th bgcolor='#F3F2F2' colspan='1' width='5%' align='left'><b>S.No</b></th> 
          <th bgcolor='#F3F2F2' width='30%' align='left'><b>DESCRIPTION</b></th>
          <th bgcolor='#F3F2F2' width='10%' colspan='0' align='left'><b>SEATS</b></th>
          <th bgcolor='#F3F2F2' width='10%' colspan='0' align='right'><b>PRICE</b></th>
          <th bgcolor='#F3F2F2' width='10%' colspan='0' align='right'><b>TOTAL</b></th>
        </tr>
        <tr style='height: 55px;'>
          <td colspan='1' style='vertical-align: top';>1</td> 
          <td width='30%' align='left' style='vertical-align: top';>${description!"-"}</td>
          <td width='10%' colspan='0' align='left' style='vertical-align: top';>${seatCount?string("0")!"-"}</td>
          <td width='10%' colspan='0' align='right' style='vertical-align: top';>${taxableValue!"-"}</td>
          <td width='10%' colspan='0' align='right' style='vertical-align: top';>${taxableValue!"-"}</td>
        </tr>
  </table></br>
  
  <table border='0' align='center' cellspacing='0' cellpadding='3' width='70%' style='min-width: 900px; margin-top: 10px; border-color: #F5F2F2; font-family: Calibri;'>
    <tr height='25px'>
      <td width='60%'><b>Amount in words : </b></td>
      <td width='15%'></td>
      <td width='15%'></td>
    </tr>
    <tr height='10px'>
      <td width='60%'>${totalInWords!"-"}</td>
      <td width='15%'></td>
      <td width='15%'></td>
    </tr>
    <tr height='10px'>
      <td width='60%'></td>
      <td width='15%' align='right'>Sub Total :</td>
      <td width='15%' align='right'>${taxableValue!"-"}</td>
    </tr>
    <tr  height='10px'>
      <td width='60%'></td>
      <td width='15%' align='right'>Discount :</td>
      <td width='15%' align='right'>${discount!"-"}</td>
    </tr>
     <tr height='10px'>
      <td width='60%'></td>
      <td width='15%' align='right'>CGST&nbsp;<span style='font-weight: normal;'>(${cgstPercentage!"-"}%) :</span></td>
      <td width='15%' align='right'>${cgst!"-"}</td>
    </tr>
     <tr height='10px'>
      <td width='60%'></td>
      <td width='15%' align='right'>SGST&nbsp;<span style='font-weight: normal;'>(${sgstPercentage!"-"}%) :</span></td>
      <td width='15%' align='right'>${sgst!"-"}</td>
    </tr>
    <tr height='10px'>
      <td width='70%'></td>
      <td width='15%' align='right'><b>Invoice Total :</b></td>
      <td width='15%' align='right'><b>${total!"-"}</b></td>
    </tr>
  </table> 
</html>
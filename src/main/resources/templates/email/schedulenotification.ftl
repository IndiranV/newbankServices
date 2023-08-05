<html>
<table width="780" border="0" cellspacing="0" cellpadding="0" align="center" style="border:1px solid #ccc; font-family:arial">
  <tbody>
    <tr>
      <td align="left" valign="top" style="background:#368EE0"><table width="94%" border="0" cellspacing="0" cellpadding="0" align="center">
          <tbody>
            <tr>
              <td align="left" valign="top" style="font-size:20px;color:#fff;padding:20px 0;padding-bottom:5px;line-height:15px">Schedule Update Notification</td>
            </tr>
            <tr>
              <td align="left" valign="top" style="font-size:12px;color:#fff;padding:20px 0;line-height:15px">${headerContent!"-"}</td>
            </tr>
          </tbody>
        </table></td>
    </tr>
    <tr>
      <td align="left" valign="top" style="padding-bottom:20px;background:#eff2f6"></td>
    </tr>
    
    <tr>
      <td align="left" valign="top" style="background:#eff2f6; height:350px"><table width="740" border="0" cellspacing="0" cellpadding="0" align="center" >
          <tbody>
            <tr>
              <td align="left" valign="top"><table width="100%" border="0" cellspacing="0" cellpadding="0" align="center" style="padding-top:20px" bgcolor="#FFFFFF">
                  <tbody>
                    <tr>
                      <td align="left" valign="top" style="padding-bottom:20px"><table width="94%" border="0" cellspacing="0" cellpadding="0" align="center" bgcolor="#FFFFFF"  >
                          <tbody>
                          <tr>
                          	<td width="20%" align="left" valign="top" style="font-size:13px;color:#373737;padding:15px 8px 8px 8px;border:1px solid #aaa"><strong>Details</strong></td>
                            <td width="20%" align="left" valign="top" style="font-size:13px;color:#373737;padding:15px 8px 8px 8px;border:1px solid #aaa"><strong>Old Data</strong></td>
                            <td width="20%" align="left" valign="top" style="font-size:13px;color:#373737;padding:15px 8px 8px 8px;border:1px solid #aaa"><strong>New Data</strong></td>
                            <td width="20%" align="left" valign="top" style="font-size:13px;color:#373737;padding:15px 8px 8px 8px;border:1px solid #aaa"><strong>Changed By</strong></td>
                          </tr>
                          <#list logDetails as change>
	                          <tr>
	                          	<td width="20%" align="left" valign="top" style="font-size:13px;color:#373737;padding:15px 8px 8px 8px;border:1px solid #aaa"><strong>${change.keyword!"-"}</strong></td>
	                            <td width="20%" align="left" valign="top" style="font-size:13px;color:#373737;padding:15px 8px 8px 8px;border:1px solid #aaa">${change.oldContent!"-"}</td>
	                            <td width="20%" align="left" valign="top" style="font-size:13px;color:#373737;padding:15px 8px 8px 8px;border:1px solid #aaa">${change.newContent!"-"}</td>
	                            <td width="20%" align="left" valign="top" style="font-size:13px;color:#373737;padding:15px 8px 8px 8px;border:1px solid #aaa">${change.updatedBy!"-"}<br>${change.updatedAt!"-"}</td>
	                          </tr>
                          </#list>
                          </tbody>
                        </table></td>
                    </tr>
                  </tbody>
                </table></td>
            </tr>
          </tbody>
        </table></td>
    </tr>
    <tr>
      <td align="left" valign="top" style="background:#eff2f6">&nbsp;</td>
    </tr>
    <tr>
      <td align="left" valign="top" style="background:#368EE0"><table width="94%" border="0" cellspacing="0" cellpadding="0" align="center" >
          <tbody>
            <tr>
              <td align="left" valign="top" style="font-size:11px;color:#fff;padding:20px 0; padding-bottom:0px;line-height:15px"> Its an system generated notification, no need to reply. </td>
            </tr>
            <tr>
              <td align="left" valign="top" style="font-size:11px;color:#fff;padding:20px 0;line-height:15px"> Kind note, if you are not the intended recipient of this email, please do drop us a mail at <a href="mailto:ezeebus@ezeeinfosolutions.com" style="color:#ffffff" target="_blank">ezeebus@ezeeinfosolutions.com</a>. 
                We will ensure immediate updation at our end. </td>
            </tr>
          </tbody>
        </table></td>
    </tr>
  </tbody>
</table>
</html>
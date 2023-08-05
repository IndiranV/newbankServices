<?xml version="1.0" encoding="UTF-8"?>
<api>
    <dlrurl>${deliveryUrl!"-"}</dlrurl>
    <unicode>0</unicode>
    <flash>0</flash>
    <sender>${senderCode!"-"}</sender>
    <message>${content!"-"}</message>
        <#list mobileNumber?split(",") as mob>
			<sms>
            	<to>${mob}</to>
        	</sms>
		</#list>
</api>
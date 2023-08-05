<html>
   <body>
    ${travelsName!"-"} of ${fileName!"-"} 
    <br/><br/>
    	<#if url?has_content>
    		<a href="${url!"-"}">Download</a>
    	</#if>
    </body>
</html>
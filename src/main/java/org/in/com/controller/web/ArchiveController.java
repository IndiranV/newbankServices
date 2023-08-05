package org.in.com.controller.web;

import hirondelle.date4j.DateTime;

import java.util.List;
import java.util.Map;

import org.in.com.constants.Text;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{zoneCode}/{token}/archive/dynamic")
public class ArchiveController extends BaseController {
	@Autowired
	ArchiveService archiveService;

	@RequestMapping(value = "/drill/{tableName}/{fromDate}/{toDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, List<Map<String, ?>>>> getDynamicDrillReport(@PathVariable("zoneCode") String zoneCode, @PathVariable("token") String token, @PathVariable("tableName") String tableName, @PathVariable("fromDate") String fromDate, @PathVariable("toDate") String toDate) {
		// Zone Token Validation
		validateToken(zoneCode, token);

		DateTime fromDateTime = new DateTime(fromDate).getStartOfDay();
		DateTime toDateTime = new DateTime(toDate).getEndOfDay();
		Map<String, List<Map<String, ?>>> list = archiveService.getArchiveReport(tableName, fromDateTime.format(Text.DATE_TIME_DATE4J), toDateTime.format(Text.DATE_TIME_DATE4J));
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/drill/master/{tableName}/{fromDate}/{toDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, List<Map<String, ?>>>> getDynamicMasterDrillReport(@PathVariable("zoneCode") String zoneCode, @PathVariable("token") String token, @PathVariable("tableName") String tableName, @PathVariable("fromDate") String fromDate, @PathVariable("toDate") String toDate) {
		// Zone Token Validation
		validateToken(zoneCode, token);

		DateTime fromDateTime = new DateTime(fromDate).getStartOfDay();
		DateTime toDateTime = new DateTime(toDate).getEndOfDay();
		Map<String, List<Map<String, ?>>> list = archiveService.getMasterForDrill(tableName, fromDateTime.format(Text.DATE_TIME_DATE4J), toDateTime.format(Text.DATE_TIME_DATE4J));
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/drill/ticket/transaction/{fromDate}/{toDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, List<Map<String, ?>>>> getBitsTicketTransaction(@PathVariable("zoneCode") String zoneCode, @PathVariable("token") String token, @PathVariable("fromDate") String fromDate, @PathVariable("toDate") String toDate) {
		// Zone Token Validation
		validateToken(zoneCode, token);

		DateTime fromDateTime = new DateTime(fromDate).getStartOfDay();
		DateTime toDateTime = new DateTime(toDate).getEndOfDay();
		Map<String, List<Map<String, ?>>> list = archiveService.getBitsTicketTransaction(fromDateTime, toDateTime);
		return ResponseIO.success(list);
	}

	private void validateToken(String zoneCode, String token) {
		NamespaceZoneEM namespaceZone = NamespaceZoneEM.getNamespaceZoneEM(zoneCode);
		if (namespaceZone == null || !namespaceZone.getToken().equals(token)) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
	}
}

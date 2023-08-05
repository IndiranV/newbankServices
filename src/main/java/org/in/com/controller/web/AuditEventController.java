package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.AuditEventIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuditEventDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.enumeration.AuditEventTypeEM;
import org.in.com.service.AuditEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/namespace/event")
public class AuditEventController extends BaseController {
	@Autowired
	AuditEventService namespaceEventService;

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AuditEventIO> getUpdate(@PathVariable("authtoken") String authtoken, @RequestBody AuditEventIO namespaceEvent) throws Exception {
		AuditEventIO event = new AuditEventIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			AuditEventDTO dto = new AuditEventDTO();
			dto.setCode(namespaceEvent.getCode());
			dto.setActiveFlag(namespaceEvent.getActiveFlag());
			dto.setEmailId(namespaceEvent.getEmailId());
			dto.setMobileNumber(namespaceEvent.getMobileNumber());
			dto.setNamespaceEventType(namespaceEvent.getNamespaceEventType() != null ? AuditEventTypeEM.getNamespaceEventTypeEM(namespaceEvent.getNamespaceEventType().getCode()) : null);
			namespaceEventService.Update(authDTO, dto);
			event.setCode(dto.getCode());
			event.setActiveFlag(dto.getActiveFlag());
		}
		return ResponseIO.success(event);
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<AuditEventIO>> getNamespaceEvent(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<AuditEventIO> events = new ArrayList<AuditEventIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<AuditEventDTO> list = namespaceEventService.getAll(authDTO);
			for (AuditEventDTO dto : list) {
				if (activeFlag != -1 && activeFlag != dto.getActiveFlag()) {
					continue;
				}
				AuditEventIO event = new AuditEventIO();
				event.setCode(dto.getCode());
				event.setActiveFlag(dto.getActiveFlag());
				event.setEmailId(dto.getEmailId());
				event.setMobileNumber(dto.getMobileNumber());

				BaseIO namespaceEvent = new BaseIO();
				namespaceEvent.setCode(dto.getNamespaceEventType().getCode());
				namespaceEvent.setName(dto.getNamespaceEventType().getDescription());
				event.setNamespaceEventType(namespaceEvent);

				events.add(event);
			}
		}
		return ResponseIO.success(events);
	}

	@RequestMapping(value = "/type", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BaseIO>> getNamespaceEventType(@PathVariable("authtoken") String authtoken) throws Exception {
		List<BaseIO> namespaceEventType = new ArrayList<BaseIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<AuditEventTypeEM> list = namespaceEventService.getAllNamespaceEvent(authDTO);
			for (AuditEventTypeEM namespaceEventAlertType : list) {
				BaseIO namespaceEvent = new BaseIO();
				namespaceEvent.setCode(namespaceEventAlertType.getCode());
				namespaceEvent.setName(namespaceEventAlertType.getDescription());
				namespaceEventType.add(namespaceEvent);
			}
		}
		return ResponseIO.success(namespaceEventType);
	}
}

package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.ScheduleTagIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleTagDTO;
import org.in.com.service.ScheduleTagService;
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
@RequestMapping(value = "/{authtoken}/schedule/tag")
public class ScheduleTagController extends BaseController {

	@Autowired
	ScheduleTagService scheduleTagService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ScheduleTagIO>> getAllScheduleTag(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) {
		List<ScheduleTagIO> list = new ArrayList<ScheduleTagIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<ScheduleTagDTO> scheduleTaglist = scheduleTagService.getAll(authDTO);
		for (ScheduleTagDTO scheduleTag : scheduleTaglist) {
			if (activeFlag != -1 && activeFlag != scheduleTag.getActiveFlag()) {
				continue;
			}
			ScheduleTagIO scheduleTagIO = new ScheduleTagIO();
			scheduleTagIO.setCode(scheduleTag.getCode());
			scheduleTagIO.setName(scheduleTag.getName());
			scheduleTagIO.setActiveFlag(scheduleTag.getActiveFlag());
			list.add(scheduleTagIO);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/{tagCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ScheduleTagIO> getScheduleTag(@PathVariable("authtoken") String authtoken, @PathVariable("tagCode") String tagCode) {
		ScheduleTagIO scheduleTagIO = new ScheduleTagIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleTagDTO scheduleTagDTO = new ScheduleTagDTO();
		scheduleTagDTO.setCode(tagCode);
		
		scheduleTagService.get(authDTO, scheduleTagDTO);
		scheduleTagIO.setCode(scheduleTagDTO.getCode());
		scheduleTagIO.setName(scheduleTagDTO.getName());
		scheduleTagIO.setActiveFlag(scheduleTagDTO.getActiveFlag());
		return ResponseIO.success(scheduleTagIO);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ScheduleTagIO> updateScheduleTag(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleTagIO scheduleTag) {
		ScheduleTagIO scheduleTagIO = new ScheduleTagIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		ScheduleTagDTO scheduleTagDTO = new ScheduleTagDTO();
		scheduleTagDTO.setCode(scheduleTag.getCode());
		scheduleTagDTO.setName(scheduleTag.getName());
		scheduleTagDTO.setActiveFlag(scheduleTag.getActiveFlag());
		scheduleTagService.Update(authDTO, scheduleTagDTO);

		scheduleTagIO.setCode(scheduleTagDTO.getCode());
		scheduleTagIO.setActiveFlag(scheduleTagDTO.getActiveFlag());
		return ResponseIO.success(scheduleTagIO);
	}
}

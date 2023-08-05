package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.RoleIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.UserDTO;
import org.in.com.service.GroupService;
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
@RequestMapping("/{authtoken}/group")
public class GroupController extends BaseController {
	@Autowired
	GroupService groupService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<GroupIO>> getUserGroups(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<GroupIO> groupIOs = new ArrayList<GroupIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<GroupDTO> list = (List<GroupDTO>) groupService.getAll(authDTO);
		// Sorting
		Comparator<GroupDTO> comp = new BeanComparator("name");
		Collections.sort(list, comp);

		for (GroupDTO dto : list) {
			if (activeFlag != -1 && activeFlag != dto.getActiveFlag()) {
				continue;
			}
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(dto.getCode());
			groupIO.setActiveFlag(dto.getActiveFlag());
			groupIO.setDecription(dto.getDecription());
			groupIO.setName(dto.getName());
			groupIO.setColor(dto.getColor());
			groupIO.setUserCount(dto.getUserCount());
			groupIO.setLevel(dto.getLevel());
			RoleIO role = new RoleIO();
			role.setCode(dto.getRole().getCode());
			role.setName(dto.getRole().getCode());
			groupIO.setRole(role);
			groupIOs.add(groupIO);
		}
		return ResponseIO.success(groupIOs);
	}

	@RequestMapping(value = "/active", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<GroupIO>> getActiveGroups(@PathVariable("authtoken") String authtoken) throws Exception {
		List<GroupIO> groupIOs = new ArrayList<GroupIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<GroupDTO> list = (List<GroupDTO>) groupService.getActiveGroup(authDTO);
		// Sorting
		Comparator<GroupDTO> comp = new BeanComparator("name");
		Collections.sort(list, comp);

		for (GroupDTO dto : list) {
			GroupIO groupIO = new GroupIO();
			groupIO.setCode(dto.getCode());
			groupIO.setActiveFlag(dto.getActiveFlag());
			groupIO.setLevel(dto.getLevel());
			groupIO.setDecription(dto.getDecription());
			groupIO.setColor(dto.getColor());
			groupIO.setName(dto.getName());
			RoleIO role = new RoleIO();
			role.setCode(dto.getRole().getCode());
			role.setName(dto.getRole().getCode());
			groupIO.setRole(role);
			groupIOs.add(groupIO);
		}
		return ResponseIO.success(groupIOs);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<GroupIO> getUpdateUID(@PathVariable("authtoken") String authtoken, @RequestBody GroupIO groupIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		GroupDTO groupDTO = new GroupDTO();
		groupDTO.setCode(groupIO.getCode());
		groupDTO.setName(groupIO.getName());
		groupDTO.setLevel(groupIO.getLevel());
		groupDTO.setDecription(groupIO.getDecription());
		groupDTO.setActiveFlag(groupIO.getActiveFlag());
		groupDTO.setColor(groupIO.getColor());
		groupService.Update(authDTO, groupDTO);
		GroupIO userGroupIO = new GroupIO();
		userGroupIO.setCode(groupDTO.getCode());
		userGroupIO.setActiveFlag(groupDTO.getActiveFlag());
		return ResponseIO.success(userGroupIO);
	}

	@RequestMapping(value = "/{groupcode}/{usercode}/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> MapUserGroup(@PathVariable("authtoken") String authtoken, @PathVariable("groupcode") String groupcode, @PathVariable("usercode") String usercode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			GroupDTO groupDTO = new GroupDTO();
			groupDTO.setCode(groupcode);
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(usercode);
			groupService.Update(authDTO, groupDTO, userDTO);
		}

		return ResponseIO.success();
	}

}

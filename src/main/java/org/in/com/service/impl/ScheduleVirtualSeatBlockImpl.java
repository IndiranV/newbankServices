package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.ScheduleCache;
import org.in.com.dao.ScheduleVirtualSeatBlockDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleVirtualSeatBlockDTO;
import org.in.com.exception.ServiceException;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleVirtualSeatBlockService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleVirtualSeatBlockImpl extends ScheduleCache implements ScheduleVirtualSeatBlockService {
	@Autowired
	GroupService groupService;

	@Override
	public void updateScheduleVirtualSeatBlock(AuthDTO authDTO, ScheduleVirtualSeatBlockDTO scheduleVirtualSeatBlock) {
		ScheduleVirtualSeatBlockDAO seatBlockDAO = new ScheduleVirtualSeatBlockDAO();
		// Schedule
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		StringBuilder scheduleCodes = new StringBuilder();
		if (scheduleVirtualSeatBlock.getScheduleList().isEmpty()) {
			scheduleCodes.append("NA");
		}
		else {
			for (ScheduleDTO schedule : scheduleVirtualSeatBlock.getScheduleList()) {
				scheduleCodes.append(schedule.getCode());
				scheduleCodes.append(",");
			}
		}
		scheduleDTO.setCode(scheduleCodes.toString());
		scheduleVirtualSeatBlock.setSchedule(scheduleDTO);
		// User Group
		GroupDTO groupDTO = new GroupDTO();
		StringBuilder groupCodes = new StringBuilder();
		if (scheduleVirtualSeatBlock.getUserGroupList().isEmpty()) {
			groupCodes.append("NA");
		}
		else {
			for (GroupDTO group : scheduleVirtualSeatBlock.getUserGroupList()) {
				groupCodes.append(group.getCode());
				groupCodes.append(",");
			}
		}
		groupDTO.setCode(groupCodes.toString());
		scheduleVirtualSeatBlock.setGroup(groupDTO);

		seatBlockDAO.updateScheduleVirtualSeatBlock(authDTO, scheduleVirtualSeatBlock);
		// remove from cache
		if (StringUtil.isNotNull(scheduleVirtualSeatBlock.getCode())) {
			removeScheduleVirtualSeatBlock(authDTO);
		}
	}

	@Override
	public List<ScheduleVirtualSeatBlockDTO> getScheduleVirtualSeatBlock(AuthDTO authDTO) {
		ScheduleVirtualSeatBlockDAO seatBlockDAO = new ScheduleVirtualSeatBlockDAO();
		List<ScheduleVirtualSeatBlockDTO> list = seatBlockDAO.getScheduleVirtualSeatBlock(authDTO);
		for (ScheduleVirtualSeatBlockDTO scheduleVirtualSeatBlock : list) {
			// Schedule
			List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
			for (String scheduleCode : scheduleVirtualSeatBlock.getSchedule().getCode().split(",")) {
				if (StringUtil.isNull(scheduleCode.trim())) {
					continue;
				}
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(scheduleCode);
				try {
					scheduleDTO = getScheduleDTO(authDTO, scheduleDTO);
				}
				catch (ServiceException e) {
				}
				scheduleList.add(scheduleDTO);
			}
			scheduleVirtualSeatBlock.setScheduleList(scheduleList);
			// User Group
			List<GroupDTO> groupList = new ArrayList<GroupDTO>();
			for (String groupCode : scheduleVirtualSeatBlock.getGroup().getCode().split(",")) {
				if (StringUtil.isNull(groupCode.trim())) {
					continue;
				}
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(groupCode);
				try {
					groupDTO = groupService.getGroup(authDTO, groupDTO);
				}
				catch (ServiceException e) {
				}
				groupList.add(groupDTO);
			}
			scheduleVirtualSeatBlock.setUserGroupList(groupList);
		}
		return list;
	}

}

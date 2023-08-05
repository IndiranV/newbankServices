package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.in.com.cache.DiscountSpecialCriteriaCache;
import org.in.com.dao.DiscountSpecialCriteriaDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.DiscountSpecialCriteriaDTO;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleService;
import org.in.com.service.DiscountSpecialCriteriaService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscountSpecialCriteriaImpl extends DiscountSpecialCriteriaCache implements DiscountSpecialCriteriaService {
	private static String SPECIAL_DISCOUNT_KEY = "ALL_SPECIAL_DISCOUNT_KEY";

	@Autowired
	GroupService groupService;

	@Autowired
	ScheduleService scheduleService;

	@Autowired
	DiscountSpecialCriteriaDAO discountSpecialCriteriaDAO;

	@Override
	public List<DiscountSpecialCriteriaDTO> getAllSpecialDiscountCriteria(AuthDTO authDTO) {
		List<DiscountSpecialCriteriaDTO> list = discountSpecialCriteriaDAO.getAllSpecialDiscountCriteria(authDTO);
		for (DiscountSpecialCriteriaDTO discount : list) {
			for (GroupDTO group : discount.getUserGroups()) {
				groupService.getGroup(authDTO, group);
			}
			for (ScheduleDTO schedule : discount.getSchedules()) {
				scheduleService.getSchedule(authDTO, schedule);
			}
		}
		return list;
	}

	@Override
	public DiscountSpecialCriteriaDTO updateSpecialDiscountCriteria(AuthDTO authDTO, DiscountSpecialCriteriaDTO discount) {

		List<GroupDTO> userGroupList = new ArrayList<>();
		for (GroupDTO userGroup : discount.getUserGroups()) {
			GroupDTO group = groupService.getGroup(authDTO, userGroup);
			userGroupList.add(group);
		}
		discount.setUserGroups(userGroupList);
		List<ScheduleDTO> scheduleList = new ArrayList<>();
		for (ScheduleDTO schedule : discount.getSchedules()) {
			ScheduleDTO schedule1 = scheduleService.getSchedule(authDTO, schedule);
			scheduleList.add(schedule1);
		}
		discount.setSchedules(scheduleList);
		discountSpecialCriteriaDAO.updateSpecialDiscountCriteria(authDTO, discount);
		removeSpecialDiscountCriteria(authDTO);
		return discount;
	}

	@Override
	public DiscountSpecialCriteriaDTO getSpecialDiscountCriteriaByCode(AuthDTO authDTO, DiscountSpecialCriteriaDTO discount) {
		DiscountSpecialCriteriaDTO dto = null;
		if (StringUtil.isNotNull(discount.getCode())) {
			dto = discountSpecialCriteriaDAO.getSpecialDiscountCriteriaByCode(authDTO, discount);
			for (GroupDTO group : dto.getUserGroups()) {
				groupService.getGroup(authDTO, group);
			}
			for (ScheduleDTO schedule : dto.getSchedules()) {
				scheduleService.getSchedule(authDTO, schedule);
			}
		}
		return dto;
	}

	@Override
	public List<DiscountSpecialCriteriaDTO> getSpecialDiscountCriteria(AuthDTO authDTO, ScheduleDTO schedule) {
		List<DiscountSpecialCriteriaDTO> discountList = getSpecialDiscountCriteria(authDTO);
		if (discountList == null) {
			discountList = discountSpecialCriteriaDAO.getAllSpecialDiscountCriteria(authDTO);
			putSpecialDiscountCriteria(authDTO, discountList);
		}
		ScheduleDTO scheduleDTO = scheduleService.getSchedule(authDTO, schedule);
		for (Iterator<DiscountSpecialCriteriaDTO> iterator = discountList.iterator(); iterator.hasNext();) {
			DiscountSpecialCriteriaDTO discount = iterator.next();
			if ((BitsUtil.isScheduleExists(discount.getSchedules(), scheduleDTO) == null && !discount.getSchedules().isEmpty()) || (BitsUtil.isGroupExists(discount.getUserGroups(), authDTO.getGroup()) == null && !discount.getUserGroups().isEmpty())) {
				iterator.remove();
				continue;
			}
		}
		return discountList;
	}

}

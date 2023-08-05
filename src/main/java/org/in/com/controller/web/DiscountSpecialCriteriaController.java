package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.DiscountSpecialCriteriaIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.DiscountSpecialCriteriaDTO;
import org.in.com.service.DiscountSpecialCriteriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/addons")
public class DiscountSpecialCriteriaController extends BaseController {

	@Autowired
	DiscountSpecialCriteriaService discountSpecialCriteriaService;
	@RequestMapping(value = "/discount/special", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<DiscountSpecialCriteriaIO>> getAllSpecialDiscountCriteria(@PathVariable("authtoken") String authtoken) throws Exception {
		List<DiscountSpecialCriteriaIO> discountList = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
			List<DiscountSpecialCriteriaDTO> list = discountSpecialCriteriaService.getAllSpecialDiscountCriteria(authDTO);
			for (DiscountSpecialCriteriaDTO discount : list) {
				DiscountSpecialCriteriaIO discountIO = new DiscountSpecialCriteriaIO();
				discountIO.setCode(discount.getCode());
				List<GroupIO> groupList = new ArrayList<>();
				for (GroupDTO userGroup : discount.getUserGroups()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(userGroup.getCode());
					groupIO.setName(userGroup.getName());
					groupList.add(groupIO);
				}
				discountIO.setUserGroups(groupList);
				List<ScheduleIO> scheduleList = new ArrayList<>();
				for (ScheduleDTO schedule : discount.getSchedules()) {
					ScheduleIO scheduleIO = new ScheduleIO();
					scheduleIO.setCode(schedule.getCode());
					scheduleIO.setName(schedule.getName());
					scheduleIO.setServiceNumber(schedule.getServiceNumber());
					scheduleList.add(scheduleIO);
				}
				discountIO.setSchedules(scheduleList);
				discountIO.setMaxAmount(discount.getMaxAmount());
				discountIO.setPercentageFlag(discount.isPercentageFlag());
				discountIO.setActiveFlag(discount.getActiveFlag());
				discountList.add(discountIO);
			}
		return ResponseIO.success(discountList);

	}

	@RequestMapping(value = "/discount/special/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<DiscountSpecialCriteriaIO> updateSpecialDiscountCriteria(@PathVariable("authtoken") String authtoken, @RequestBody DiscountSpecialCriteriaIO discountSpecialCriteriaIO) {
		DiscountSpecialCriteriaIO discountIO = new DiscountSpecialCriteriaIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
			DiscountSpecialCriteriaDTO discount = new DiscountSpecialCriteriaDTO();
			discount.setCode(discountSpecialCriteriaIO.getCode());
			List<GroupDTO> groupList = new ArrayList<>();
			if (discountSpecialCriteriaIO.getUserGroups() != null) {
				for (GroupIO groupIO : discountSpecialCriteriaIO.getUserGroups()) {
					GroupDTO group = new GroupDTO();
					group.setCode(groupIO.getCode());
					groupList.add(group);
				}
			}
			discount.setUserGroups(groupList);
			List<ScheduleDTO> scheduleList = new ArrayList<>();
			if (discountSpecialCriteriaIO.getSchedules() != null) {
				for (ScheduleIO scheduleIO : discountSpecialCriteriaIO.getSchedules()) {
					ScheduleDTO schedule = new ScheduleDTO();
					schedule.setCode(scheduleIO.getCode());
					scheduleList.add(schedule);
				}
			}
			discount.setSchedules(scheduleList);
			discount.setMaxAmount(discountSpecialCriteriaIO.getMaxAmount());
			discount.setPercentageFlag(discountSpecialCriteriaIO.isPercentageFlag());
			discount.setActiveFlag(discountSpecialCriteriaIO.getActiveFlag());
			discountSpecialCriteriaService.updateSpecialDiscountCriteria(authDTO, discount);
			if (discount.getCode() != null) {
				discountIO.setCode(discount.getCode());
				discountIO.setActiveFlag(discount.getActiveFlag());
			}
		return ResponseIO.success(discountIO);
	}

	@RequestMapping(value = "/discount/special/{criteriaCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<DiscountSpecialCriteriaIO> getSpecialDiscountCriteriaByCode(@PathVariable("authtoken") String authtoken, @PathVariable("criteriaCode") String criteriaCode) {
		DiscountSpecialCriteriaIO discountIO = new DiscountSpecialCriteriaIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
			DiscountSpecialCriteriaDTO discount = new DiscountSpecialCriteriaDTO();
			discount.setCode(criteriaCode);
			DiscountSpecialCriteriaDTO criteria = discountSpecialCriteriaService.getSpecialDiscountCriteriaByCode(authDTO, discount);
			if (criteria != null) {
				discountIO.setCode(criteria.getCode());
				List<GroupIO> groupList = new ArrayList<>();
				for (GroupDTO group : criteria.getUserGroups()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(group.getCode());
					groupIO.setName(group.getName());
					groupList.add(groupIO);
				}
				discountIO.setUserGroups(groupList);
				List<ScheduleIO> scheduleList = new ArrayList<>();
				for (ScheduleDTO scheduleDTO : criteria.getSchedules()) {
					ScheduleIO scheduleIO = new ScheduleIO();
					scheduleIO.setCode(scheduleDTO.getCode());
					scheduleIO.setName(scheduleDTO.getName());
					scheduleIO.setServiceNumber(scheduleDTO.getServiceNumber());
					scheduleList.add(scheduleIO);
				}
				discountIO.setSchedules(scheduleList);
				discountIO.setMaxAmount(criteria.getMaxAmount());
				discountIO.setPercentageFlag(criteria.isPercentageFlag());
				discountIO.setActiveFlag(criteria.getActiveFlag());
			}
		return ResponseIO.success(discountIO);
	}
}

package org.in.com.controller.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.AuditIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.CommissionIO;
import org.in.com.controller.web.io.ExtraCommissionIO;
import org.in.com.controller.web.io.ExtraCommissionSlabIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CommissionDTO;
import org.in.com.dto.ExtraCommissionDTO;
import org.in.com.dto.ExtraCommissionSlabDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.CommissionTypeEM;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.SlabCalenderModeEM;
import org.in.com.dto.enumeration.SlabCalenderTypeEM;
import org.in.com.dto.enumeration.SlabModeEM;
import org.in.com.service.CommissionService;
import org.in.com.utils.StringUtil;
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
@RequestMapping("/{authtoken}/commission")
public class CommissionController extends BaseController {
	@Autowired
	CommissionService commissionService;

	@RequestMapping(value = "/{userCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<CommissionIO>> getUserCommisson(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode) throws Exception {
		List<CommissionIO> commission = new ArrayList<CommissionIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		UserDTO userDTO = new UserDTO();
		userDTO.setCode(userCode);
		List<CommissionDTO> list = (List<CommissionDTO>) commissionService.getAllCommission(authDTO, userDTO);
		for (CommissionDTO commissionDTO : list) {
			CommissionIO commissionio = new CommissionIO();
			commissionio.setCode(commissionDTO.getCode());
			commissionio.setValue(commissionDTO.getCommissionValue());
			commissionio.setServiceTax(commissionDTO.getServiceTax());
			commissionio.setActiveFlag(commissionDTO.getActiveFlag());
			if (commissionDTO.getCommissionValueType() != null) {
				BaseIO commissionType = new BaseIO();
				commissionType.setCode(commissionDTO.getCommissionValueType().getCode());
				commissionType.setName(commissionDTO.getCommissionValueType().getName());
				commissionio.setValueType(commissionType);
			}
			commissionio.setCommissionType(commissionDTO.getCommissionType().getCode());
			commissionio.setCreditLimit(commissionDTO.getCreditlimit());
			commissionio.setCreatedDateTime(commissionDTO.getCreatedDateTime());
			
			AuditIO auditIO = new AuditIO();
			if (commissionDTO.getAudit() != null && commissionDTO.getAudit().getUser() != null) {
				UserIO updatedBy = new UserIO();
				updatedBy.setCode(commissionDTO.getAudit().getUser().getCode());
				updatedBy.setName(commissionDTO.getAudit().getUser().getName());
				auditIO.setUser(updatedBy);
			}
			commissionio.setAudit(auditIO);
			commission.add(commissionio);
		}
		return ResponseIO.success(commission);
	}

	@RequestMapping(value = "/{userCode}/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<CommissionIO> updateCommissionUID(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode, @RequestBody CommissionIO commissionIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			CommissionDTO commissionDTO = new CommissionDTO();
			commissionDTO.setCode(commissionIO.getCode());
			commissionDTO.setActiveFlag(commissionIO.getActiveFlag());
			commissionDTO.setCommissionValueType(FareTypeEM.getFareTypeEM(commissionIO.getValueType().getCode()));
			commissionDTO.setCreditlimit(commissionIO.getCreditLimit() != null ? commissionIO.getCreditLimit() : BigDecimal.ZERO);
			commissionDTO.setCommissionValue(commissionIO.getValue() != null ? commissionIO.getValue() : BigDecimal.ZERO);
			commissionDTO.setServiceTax(commissionIO.getServiceTax() != null ? commissionIO.getServiceTax() : BigDecimal.ZERO);
			commissionDTO.setCommissionType(CommissionTypeEM.getCommissionTypeEM(commissionIO.getCommissionType()));
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userCode);
			commissionService.updateCommission(authDTO, userDTO, commissionDTO);
			commissionIO.setCode(commissionDTO.getCode());
			commissionIO.setActiveFlag(commissionDTO.getActiveFlag());
		}
		return ResponseIO.success(commissionIO);

	}

	@RequestMapping(value = "/extra/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ExtraCommissionIO> updateUserExtraCommissionUID(@PathVariable("authtoken") String authtoken, @RequestBody ExtraCommissionIO commissionIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ExtraCommissionDTO commissionDTO = new ExtraCommissionDTO();
			commissionDTO.setCode(commissionIO.getCode());
			commissionDTO.setName(commissionIO.getName());
			commissionDTO.setCommissionValue(commissionIO.getCommissionValue() != null ? commissionIO.getCommissionValue() : BigDecimal.ZERO);
			commissionDTO.setActiveFlag(commissionIO.getActiveFlag());
			if (commissionIO.getActiveFlag() == 1) {
				commissionDTO.setCommissionValueType(FareTypeEM.getFareTypeEM(commissionIO.getCommissionValueType() != null ? commissionIO.getCommissionValueType().getCode() : null));
				commissionDTO.setActiveFrom(commissionIO.getActiveFrom());
				commissionDTO.setActiveTo(commissionIO.getActiveTo());
				commissionDTO.setDayOfWeek(commissionIO.getDayOfWeek());
				commissionDTO.setDateType(DateTypeEM.getDateTypeEM(commissionIO.getDateType()));
				commissionDTO.setRefferenceType(commissionIO.getRoleType());
				commissionDTO.setLookupCode(commissionIO.getLookupCode());
				commissionDTO.setOverrideCommissionFlag(commissionIO.getOverrideCommissionFlag());

				ExtraCommissionSlabDTO commissionSlabDTO = new ExtraCommissionSlabDTO();
				commissionSlabDTO.setCode(commissionIO.getCommissionSlab() != null ? commissionIO.getCommissionSlab().getCode() : null);
				commissionDTO.setCommissionSlab(commissionSlabDTO);

				commissionDTO.setMaxCommissionLimit(commissionIO.getMaxCommissionLimit() != null ? commissionIO.getMaxCommissionLimit() : BigDecimal.ZERO);
				commissionDTO.setMinTicketFare(commissionIO.getMinTicketFare() != null ? commissionIO.getMinTicketFare() : BigDecimal.ZERO);
				commissionDTO.setMaxExtraCommissionAmount(commissionIO.getMaxExtraCommissionAmount() != null ? commissionIO.getMaxExtraCommissionAmount() : BigDecimal.ZERO);
				commissionDTO.setMinSeatCount(commissionIO.getMinSeatCount());
				if ("UR".equals(commissionIO.getRoleType()) && commissionIO.getUser() != null) {
					List<UserDTO> userList = new ArrayList<>();
					for (BaseIO user : commissionIO.getUser()) {
						if (StringUtil.isNull(user.getCode())) {
							continue;
						}
						UserDTO userDTO = new UserDTO();
						userDTO.setCode(user.getCode());
						userList.add(userDTO);
					}
					commissionDTO.setUser(userList);
				}
				else if ("GR".equals(commissionIO.getRoleType()) && commissionIO.getGroup() != null) {
					List<GroupDTO> groupList = new ArrayList<>();
					for (BaseIO group : commissionIO.getGroup()) {
						if (StringUtil.isNull(group.getCode())) {
							continue;
						}
						GroupDTO groupDTO = new GroupDTO();
						groupDTO.setCode(group.getCode());
						groupList.add(groupDTO);
					}
					commissionDTO.setGroup(groupList);
				}
				if (commissionIO.getSchedule() != null && !commissionIO.getSchedule().isEmpty()) {
					List<ScheduleDTO> scheduleList = new ArrayList<>();
					for (ScheduleIO schedule : commissionIO.getSchedule()) {
						ScheduleDTO scheduleDTO = new ScheduleDTO();
						scheduleDTO.setCode(schedule.getCode());
						scheduleList.add(scheduleDTO);
					}
					commissionDTO.setScheduleList(scheduleList);
				}
				if (commissionIO.getRouteCode() != null && !commissionIO.getRouteCode().isEmpty()) {
					List<RouteDTO> routeList = new ArrayList<>();
					for (String code : commissionIO.getRouteCode()) {
						RouteDTO routeDTO = new RouteDTO();
						routeDTO.setCode(code);
						routeList.add(routeDTO);
					}
					commissionDTO.setRouteList(routeList);
				}
			}
			commissionService.UpdateExtraCommission(authDTO, commissionDTO);
			commissionIO.setCode(commissionDTO.getCode());
			commissionIO.setActiveFlag(commissionDTO.getActiveFlag());
		}
		return ResponseIO.success(commissionIO);

	}

	@RequestMapping(value = "/extra", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ExtraCommissionIO>> getAllUserExtraCommissionUID(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<ExtraCommissionIO> list = new ArrayList<ExtraCommissionIO>();
		if (authDTO != null) {
			List<ExtraCommissionDTO> commissionList = commissionService.getAllExtraCommission(authDTO);
			if (!commissionList.isEmpty()) {
				for (ExtraCommissionDTO commissionDTO : commissionList) {
					if (activeFlag != -1 && activeFlag != commissionDTO.getActiveFlag()) {
						continue;
					}
					ExtraCommissionIO commissionIO = new ExtraCommissionIO();
					commissionIO.setCode(commissionDTO.getCode());
					commissionIO.setName(commissionDTO.getName());
					commissionIO.setActiveFrom(commissionDTO.getActiveFrom());
					commissionIO.setActiveTo(commissionDTO.getActiveTo());
					commissionIO.setDayOfWeek(commissionDTO.getDayOfWeek());
					commissionIO.setDateType(commissionDTO.getDateType().getCode());
					commissionIO.setCommissionValue(commissionDTO.getCommissionValue());
					commissionIO.setOverrideCommissionFlag(commissionDTO.getOverrideCommissionFlag());

					BaseIO commissionType = new BaseIO();
					commissionType.setCode(commissionDTO.getCommissionValueType().getCode());
					commissionType.setName(commissionDTO.getCommissionValueType().getName());
					commissionIO.setCommissionValueType(commissionType);
					List<ScheduleIO> scheduleList = new ArrayList<>();
					if (!commissionDTO.getScheduleList().isEmpty()) {
						for (ScheduleDTO dto : commissionDTO.getScheduleList()) {
							ScheduleIO schedule = new ScheduleIO();
							schedule.setCode(dto.getCode());
							schedule.setName(dto.getName());
							scheduleList.add(schedule);
						}
					}
					commissionIO.setSchedule(scheduleList);
					List<String> routeList = new ArrayList<>();
					if (commissionDTO.getRouteList() != null && !commissionDTO.getRouteList().isEmpty()) {
						for (RouteDTO routeDTO : commissionDTO.getRouteList()) {
							routeList.add(routeDTO.getCode());
						}
					}
					commissionIO.setRouteCode(routeList);
					if (commissionDTO.getCommissionSlab() != null) {
						ExtraCommissionSlabIO commissionSlab = new ExtraCommissionSlabIO();
						commissionSlab.setCode(commissionDTO.getCommissionSlab().getCode());
						commissionIO.setCommissionSlab(commissionSlab);
					}

					commissionIO.setMaxCommissionLimit(commissionDTO.getMaxCommissionLimit());
					commissionIO.setMinTicketFare(commissionDTO.getMinTicketFare());
					commissionIO.setMaxExtraCommissionAmount(commissionDTO.getMaxExtraCommissionAmount());
					commissionIO.setMinSeatCount(commissionDTO.getMinSeatCount());
					commissionIO.setRoleType(commissionDTO.getRefferenceType());
					if (commissionDTO.getRefferenceType().equals("GR")) {
						List<BaseIO> groupList = new ArrayList<>();
						if (commissionDTO.getGroup() != null) {
							for (GroupDTO group : commissionDTO.getGroup()) {
								BaseIO groupIO = new BaseIO();
								groupIO.setCode(group.getCode());
								groupIO.setName(group.getName());
								groupList.add(groupIO);
							}
						}
						commissionIO.setGroup(groupList);
					}
					if (commissionDTO.getRefferenceType().equals("UR")) {
						List<BaseIO> userList = new ArrayList<>();
						if (commissionDTO.getUser() != null) {
							for (UserDTO user : commissionDTO.getUser()) {
								BaseIO userIO = new BaseIO();
								userIO.setCode(user.getCode());
								userIO.setName(user.getName());
								userList.add(userIO);
							}
						}
						commissionIO.setUser(userList);
					}
					// Exceptions
					if (!commissionDTO.getOverrideList().isEmpty()) {
						List<ExtraCommissionIO> overridelist = new ArrayList<ExtraCommissionIO>();
						for (ExtraCommissionDTO overrideDTO : commissionDTO.getOverrideList()) {
							ExtraCommissionIO overrideIO = new ExtraCommissionIO();
							overrideIO.setCode(overrideDTO.getCode());
							overrideIO.setName(overrideDTO.getName());
							overrideIO.setActiveFrom(overrideDTO.getActiveFrom());
							overrideIO.setActiveTo(overrideDTO.getActiveTo());
							overrideIO.setDayOfWeek(overrideDTO.getDayOfWeek());
							overridelist.add(overrideIO);
						}
						commissionIO.setOverrideList(overridelist);
					}
					list.add(commissionIO);
				}
			}
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/extra/{commissionCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ExtraCommissionIO> getAllUserExtraCommissionUID(@PathVariable("authtoken") String authtoken, @PathVariable("commissionCode") String commissionCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ExtraCommissionIO commissionIO = new ExtraCommissionIO();
		if (authDTO != null) {
			ExtraCommissionDTO commissionDTO = new ExtraCommissionDTO();
			commissionDTO.setCode(commissionCode);
			commissionDTO = commissionService.getExtraCommission(authDTO, commissionDTO);
			commissionIO.setCode(commissionDTO.getCode());
			commissionIO.setName(commissionDTO.getName());
			commissionIO.setActiveFrom(commissionDTO.getActiveFrom());
			commissionIO.setActiveTo(commissionDTO.getActiveTo());
			commissionIO.setDayOfWeek(commissionDTO.getDayOfWeek());
			commissionIO.setDateType(commissionDTO.getDateType().getCode());
			commissionIO.setCommissionValue(commissionDTO.getCommissionValue());
			commissionIO.setOverrideCommissionFlag(commissionDTO.getOverrideCommissionFlag());

			BaseIO commissionType = new BaseIO();
			commissionType.setCode(commissionDTO.getCommissionValueType().getCode());
			commissionType.setName(commissionDTO.getCommissionValueType().getName());
			commissionIO.setCommissionValueType(commissionType);

			if (!commissionDTO.getScheduleList().isEmpty()) {
				List<ScheduleIO> scheduleList = new ArrayList<>();
				for (ScheduleDTO dto : commissionDTO.getScheduleList()) {
					ScheduleIO schedule = new ScheduleIO();
					schedule.setCode(dto.getCode());
					schedule.setName(dto.getName());
				}
				commissionIO.setSchedule(scheduleList);
			}
			if (commissionIO.getRouteCode() != null && !commissionIO.getRouteCode().isEmpty()) {
				List<RouteDTO> routeList = new ArrayList<>();
				for (String code : commissionIO.getRouteCode()) {
					RouteDTO routeDTO = new RouteDTO();
					routeDTO.setCode(code);
					routeList.add(routeDTO);
				}
				commissionDTO.setRouteList(routeList);
			}
			if (commissionDTO.getCommissionSlab() != null) {
				ExtraCommissionSlabIO commissionSlab = new ExtraCommissionSlabIO();
				commissionSlab.setCode(commissionDTO.getCommissionSlab().getCode());
				commissionIO.setCommissionSlab(commissionSlab);
			}
			commissionIO.setMaxCommissionLimit(commissionDTO.getMaxCommissionLimit());
			commissionIO.setMinTicketFare(commissionDTO.getMinTicketFare());
			commissionIO.setMaxExtraCommissionAmount(commissionDTO.getMaxExtraCommissionAmount());
			commissionIO.setMinSeatCount(commissionDTO.getMinSeatCount());
			commissionIO.setRoleType(commissionDTO.getRefferenceType());
			if (commissionDTO.getGroup() != null) {
				List<BaseIO> groupList = new ArrayList<>();
				for (GroupDTO group : commissionDTO.getGroup()) {
					BaseIO groupIO = new BaseIO();
					groupIO.setCode(group.getCode());
					groupIO.setName(group.getName());
					groupList.add(groupIO);
				}
				commissionIO.setGroup(groupList);
			}
			if (commissionDTO.getUser() != null) {
				List<BaseIO> userList = new ArrayList<>();
				for (UserDTO user : commissionDTO.getUser()) {
					BaseIO userIO = new BaseIO();
					userIO.setCode(user.getCode());
					userIO.setName(user.getName());
					userList.add(userIO);
				}
				commissionIO.setUser(userList);
			}
			// Exceptions
			if (!commissionDTO.getOverrideList().isEmpty()) {
				List<ExtraCommissionIO> overridelist = new ArrayList<ExtraCommissionIO>();
				for (ExtraCommissionDTO overrideDTO : commissionDTO.getOverrideList()) {
					ExtraCommissionIO overrideIO = new ExtraCommissionIO();
					overrideIO.setCode(overrideDTO.getCode());
					overrideIO.setName(overrideDTO.getName());
					overrideIO.setActiveFrom(overrideDTO.getActiveFrom());
					overrideIO.setActiveTo(overrideDTO.getActiveTo());
					overrideIO.setDayOfWeek(overrideDTO.getDayOfWeek());
					overridelist.add(overrideIO);
				}
				commissionIO.setOverrideList(overridelist);
			}
		}
		return ResponseIO.success(commissionIO);
	}

	@RequestMapping(value = "/extra/slab", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ExtraCommissionSlabIO>> getAllSlab(@PathVariable("authtoken") String authtoken) throws Exception {
		List<ExtraCommissionSlabIO> slabList = new ArrayList<ExtraCommissionSlabIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<ExtraCommissionSlabDTO> list = commissionService.getAllExtraCommissionSlab(authDTO);
			for (ExtraCommissionSlabDTO extraSlabDTO : list) {
				ExtraCommissionSlabIO slabIO = new ExtraCommissionSlabIO();
				slabIO.setCode(extraSlabDTO.getCode());
				slabIO.setName(extraSlabDTO.getName());
				slabIO.setCode(extraSlabDTO.getCode());
				BaseIO calenderType = new BaseIO();
				calenderType.setCode(extraSlabDTO.getSlabCalenderType().getCode());
				calenderType.setName(extraSlabDTO.getSlabCalenderType().getName());
				slabIO.setSlabCalenderType(calenderType);
				BaseIO calenderMode = new BaseIO();
				calenderMode.setCode(extraSlabDTO.getSlabCalenderMode().getCode());
				calenderMode.setName(extraSlabDTO.getSlabCalenderMode().getName());
				slabIO.setSlabCalenderMode(calenderMode);
				BaseIO slabMode = new BaseIO();
				slabMode.setCode(extraSlabDTO.getSlabMode().getCode());
				slabMode.setName(extraSlabDTO.getSlabMode().getName());
				slabIO.setSlabMode(slabMode);
				slabIO.setSlabFromValue(extraSlabDTO.getSlabFromValue());
				slabIO.setSlabToValue(extraSlabDTO.getSlabToValue());
				slabIO.setActiveFlag(extraSlabDTO.getActiveFlag());
				slabIO.setActiveFlag(extraSlabDTO.getActiveFlag());
				slabList.add(slabIO);
			}
		}
		return ResponseIO.success(slabList);
	}

	@RequestMapping(value = "/extra/slab/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ExtraCommissionSlabIO> updateExtraCommissionSlabDetails(@PathVariable("authtoken") String authtoken, @RequestBody ExtraCommissionSlabIO slabIO) throws Exception {
		ExtraCommissionSlabIO slabDetails = new ExtraCommissionSlabIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ExtraCommissionSlabDTO commissionSlabDTO = new ExtraCommissionSlabDTO();
			commissionSlabDTO.setCode(slabIO.getCode());
			commissionSlabDTO.setName(slabIO.getName());
			if (slabIO.getActiveFlag() == 1) {
				commissionSlabDTO.setSlabCalenderType(SlabCalenderTypeEM.getSlabCalenderTypeEM(slabIO.getSlabCalenderType().getCode()));
				commissionSlabDTO.setSlabCalenderMode(SlabCalenderModeEM.getSlabCalenderModeEM(slabIO.getSlabCalenderMode().getCode()));
				commissionSlabDTO.setSlabMode(SlabModeEM.getSlabModeEM(slabIO.getSlabMode().getCode()));
			}
			commissionSlabDTO.setSlabFromValue(slabIO.getSlabFromValue());
			commissionSlabDTO.setSlabToValue(slabIO.getSlabToValue());
			commissionSlabDTO.setActiveFlag(slabIO.getActiveFlag());
			commissionService.updateExtraCommissionSlabDetails(authDTO, commissionSlabDTO);
			slabDetails.setCode(commissionSlabDTO.getCode());
			slabDetails.setActiveFlag(commissionSlabDTO.getActiveFlag());
		}
		return ResponseIO.success(slabDetails);
	}

}

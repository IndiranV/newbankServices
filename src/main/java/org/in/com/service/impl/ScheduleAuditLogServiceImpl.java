package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.CacheCentral;
import org.in.com.cache.ScheduleCache;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.BusVehicleVanPickupDAO;
import org.in.com.dao.ScheduleAuditDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleAuditLogDTO;
import org.in.com.dto.ScheduleCategoryDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.enumeration.ScheduleEventTypeEM;
import org.in.com.service.ScheduleAuditLogService;
import org.in.com.service.ScheduleCategoryService;
import org.in.com.service.StateService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleAuditLogServiceImpl extends CacheCentral implements ScheduleAuditLogService {

	@Autowired
	ScheduleCategoryService categoryService;
	@Autowired
	StateService stateService;

	public void updateScheduleAudit(AuthDTO authDTO, ScheduleDTO schedule) {
		try {
			List<ScheduleAuditLogDTO> scheduleAuditList = new ArrayList<ScheduleAuditLogDTO>();
			ScheduleAuditDAO scheduleAuditDAO = new ScheduleAuditDAO();
			ScheduleAuditLogDTO scheduleAuditLogDTO = new ScheduleAuditLogDTO();

			ScheduleCache scheduleCache = new ScheduleCache();
			ScheduleDTO existSchedule = null;
			if (StringUtil.isNotNull(schedule.getCode())) {
				existSchedule = new ScheduleDTO();
				existSchedule.setCode(schedule.getCode());
				existSchedule = scheduleCache.getScheduleDTO(authDTO, existSchedule);
			}

			StringBuilder scheduleAudit = new StringBuilder();
			if (existSchedule != null && existSchedule.getActiveFlag() == Numeric.ONE_INT && schedule.getActiveFlag() == Numeric.ONE_INT) {
				if (StringUtil.isNotNull(schedule.getName()) && !schedule.getName().equals(existSchedule.getName())) {
					scheduleAudit.append("Name ");
					scheduleAudit.append(existSchedule.getName());
					scheduleAudit.append(" changed to ");
					scheduleAudit.append(schedule.getName() + Text.SINGLE_SPACE);
					scheduleAudit.append("\n");
				}
				if ((StringUtil.isNotNull(schedule.getActiveFrom()) && !schedule.getActiveFrom().equals(existSchedule.getActiveFrom())) || (StringUtil.isNotNull(schedule.getActiveTo()) && !schedule.getActiveTo().equals(existSchedule.getActiveTo()))) {
					scheduleAudit.append(existSchedule.getActiveFrom());
					scheduleAudit.append("|");
					scheduleAudit.append(existSchedule.getActiveTo());
					scheduleAudit.append(" changed to ");
					scheduleAudit.append(schedule.getActiveFrom());
					scheduleAudit.append("|");
					scheduleAudit.append(schedule.getActiveTo() + Text.SINGLE_SPACE);
					scheduleAudit.append("\n");
				}
				if (StringUtil.isNotNull(schedule.getServiceNumber()) || StringUtil.isNotNull(existSchedule.getServiceNumber())) {
					if ((StringUtil.isNotNull(schedule.getServiceNumber()) && StringUtil.isNotNull(existSchedule.getServiceNumber())) && !schedule.getServiceNumber().equals(existSchedule.getServiceNumber())) {
						scheduleAudit.append("Service No ");
						scheduleAudit.append(StringUtil.isNotNull(existSchedule.getServiceNumber()) ? existSchedule.getServiceNumber() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("changed");
						scheduleAudit.append(StringUtil.isNotNull(schedule.getServiceNumber()) ? " to " + schedule.getServiceNumber() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
					else if (StringUtil.isNotNull(schedule.getServiceNumber()) && StringUtil.isNull(existSchedule.getServiceNumber())) {
						scheduleAudit.append("Service No Added ");
						scheduleAudit.append(StringUtil.isNotNull(schedule.getServiceNumber()) ? schedule.getServiceNumber() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
					else if (StringUtil.isNotNull(existSchedule.getServiceNumber()) && StringUtil.isNull(schedule.getServiceNumber())) {
						scheduleAudit.append("Service No Removed ");
						scheduleAudit.append(StringUtil.isNotNull(existSchedule.getServiceNumber()) ? existSchedule.getServiceNumber() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
				}
				if ((StringUtil.isNotNull(schedule.getDisplayName()) || StringUtil.isNotNull(existSchedule.getDisplayName()))) {
					if (StringUtil.isNotNull(schedule.getDisplayName()) && StringUtil.isNotNull(existSchedule.getDisplayName()) && !schedule.getDisplayName().equals(existSchedule.getDisplayName())) {
						scheduleAudit.append("Display Name ");
						scheduleAudit.append(StringUtil.isNotNull(existSchedule.getDisplayName()) ? existSchedule.getDisplayName() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("changed");
						scheduleAudit.append(StringUtil.isNotNull(schedule.getDisplayName()) ? " to " + schedule.getDisplayName() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
					else if (StringUtil.isNotNull(schedule.getDisplayName()) && StringUtil.isNull(existSchedule.getDisplayName())) {
						scheduleAudit.append("Display Name Added ");
						scheduleAudit.append(StringUtil.isNotNull(schedule.getDisplayName()) ? schedule.getDisplayName() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
					else if (StringUtil.isNotNull(existSchedule.getDisplayName()) && StringUtil.isNull(schedule.getDisplayName())) {
						scheduleAudit.append("Display Name Removed ");
						scheduleAudit.append(StringUtil.isNotNull(existSchedule.getDisplayName()) ? existSchedule.getDisplayName() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
				}
				if ((StringUtil.isNotNull(schedule.getApiDisplayName()) || StringUtil.isNotNull(existSchedule.getApiDisplayName()))) {
					if ((StringUtil.isNotNull(schedule.getApiDisplayName()) && StringUtil.isNotNull(existSchedule.getApiDisplayName())) && !schedule.getApiDisplayName().equals(existSchedule.getApiDisplayName())) {
						scheduleAudit.append("API Display Name ");
						scheduleAudit.append(StringUtil.isNotNull(existSchedule.getApiDisplayName()) ? existSchedule.getApiDisplayName() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("changed");
						scheduleAudit.append(StringUtil.isNotNull(schedule.getApiDisplayName()) ? " to " + schedule.getApiDisplayName() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
					else if ((StringUtil.isNotNull(schedule.getApiDisplayName()) && StringUtil.isNull(existSchedule.getApiDisplayName()))) {
						scheduleAudit.append("API Display Name Added ");
						scheduleAudit.append(StringUtil.isNotNull(schedule.getApiDisplayName()) ? schedule.getApiDisplayName() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
					else if ((StringUtil.isNotNull(existSchedule.getApiDisplayName()) && StringUtil.isNull(schedule.getApiDisplayName()))) {
						scheduleAudit.append("API Display Name Removed ");
						scheduleAudit.append(StringUtil.isNotNull(existSchedule.getApiDisplayName()) ? existSchedule.getApiDisplayName() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
				}
				if (StringUtil.isNotNull(schedule.getPnrStartCode()) || StringUtil.isNotNull(existSchedule.getPnrStartCode())) {
					if (StringUtil.isNotNull(schedule.getPnrStartCode()) && StringUtil.isNotNull(existSchedule.getPnrStartCode()) && !schedule.getPnrStartCode().equals(existSchedule.getPnrStartCode())) {
						scheduleAudit.append("PNR code ");
						scheduleAudit.append(StringUtil.isNotNull(existSchedule.getPnrStartCode()) ? existSchedule.getPnrStartCode() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("changed");
						scheduleAudit.append(StringUtil.isNotNull(schedule.getPnrStartCode()) ? " to " + schedule.getPnrStartCode() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
					else if (StringUtil.isNotNull(schedule.getPnrStartCode()) && StringUtil.isNull(existSchedule.getPnrStartCode())) {
						scheduleAudit.append("PNR code Added ");
						scheduleAudit.append(StringUtil.isNotNull(schedule.getPnrStartCode()) ? schedule.getPnrStartCode() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
					else if (StringUtil.isNotNull(existSchedule.getPnrStartCode()) && StringUtil.isNull(schedule.getPnrStartCode())) {
						scheduleAudit.append("PNR code Removed ");
						scheduleAudit.append(StringUtil.isNotNull(existSchedule.getPnrStartCode()) ? existSchedule.getPnrStartCode() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
				}
				if ((StringUtil.isNotNull(schedule.getDayOfWeek()) || StringUtil.isNotNull(existSchedule.getDayOfWeek()))) {
					if (StringUtil.isNotNull(schedule.getDayOfWeek()) && StringUtil.isNotNull(existSchedule.getDayOfWeek()) && !schedule.getDayOfWeek().equals(existSchedule.getDayOfWeek())) {
						scheduleAudit.append("Day of week ");
						scheduleAudit.append(StringUtil.isNotNull(existSchedule.getDayOfWeek()) ? StringUtil.getDayOfWeek(existSchedule.getDayOfWeek()) + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("changed");
						scheduleAudit.append(StringUtil.isNotNull(schedule.getDayOfWeek()) ? " to " + StringUtil.getDayOfWeek(schedule.getDayOfWeek()) + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
					else if (StringUtil.isNotNull(schedule.getDayOfWeek()) && StringUtil.isNull(existSchedule.getDayOfWeek())) {
						scheduleAudit.append("Day of week Added ");
						scheduleAudit.append(StringUtil.isNotNull(schedule.getDayOfWeek()) ? StringUtil.getDayOfWeek(schedule.getDayOfWeek()) + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
					else if (StringUtil.isNotNull(existSchedule.getDayOfWeek()) && StringUtil.isNull(schedule.getDayOfWeek())) {
						scheduleAudit.append("Day of week Removed ");
						scheduleAudit.append(StringUtil.isNotNull(existSchedule.getDayOfWeek()) ? StringUtil.getDayOfWeek(existSchedule.getDayOfWeek()) + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleAudit.append("\n");
					}
				}
				if (schedule.getCategory() != null || existSchedule.getCategory() != null) {
					schedule.setCategory(categoryService.getCategory(authDTO, schedule.getCategory()));
					existSchedule.setCategory(existSchedule.getCategory() != null ? categoryService.getCategory(authDTO, existSchedule.getCategory()) : new ScheduleCategoryDTO());
					if ((schedule.getCategory().getId() != 0 || existSchedule.getCategory().getId() != 0) && schedule.getCategory().getId() != existSchedule.getCategory().getId()) {
						if (schedule.getCategory().getId() != 0 && existSchedule.getCategory().getId() != 0 && schedule.getCategory().getId() != existSchedule.getCategory().getId()) {
							scheduleAudit.append("Category ");
							scheduleAudit.append(StringUtil.isNotNull(existSchedule.getCategory().getName()) ? existSchedule.getCategory().getName() + Text.SINGLE_SPACE : Text.EMPTY);
							scheduleAudit.append("changed");
							scheduleAudit.append(StringUtil.isNotNull(schedule.getCategory().getName()) ? " to " + schedule.getCategory().getName() : Text.EMPTY);
						}
						else if (schedule.getCategory().getId() != 0 && existSchedule.getCategory().getId() == 0) {
							scheduleAudit.append("Category ");
							scheduleAudit.append(StringUtil.isNotNull(schedule.getCategory().getName()) ? schedule.getCategory().getName() + Text.SINGLE_SPACE : Text.EMPTY);
							scheduleAudit.append("Added");
						}
						else if (existSchedule.getCategory().getId() != 0 && schedule.getCategory().getId() == 0) {
							scheduleAudit.append("Category ");
							scheduleAudit.append(StringUtil.isNotNull(existSchedule.getCategory().getName()) ? existSchedule.getCategory().getName() + Text.SINGLE_SPACE : Text.EMPTY);
							scheduleAudit.append("removed");
						}
					}
				}
				if (scheduleAudit.length() > 0) {
					scheduleAudit.insert(0, schedule.getName() + " - ");
				}
			}
			else if (schedule.getActiveFlag() == Numeric.TWO_INT && existSchedule != null) {
				scheduleAudit.append(StringUtil.isNotNull(existSchedule.getName()) ? existSchedule.getName() : Text.EMPTY);
			}
			if (scheduleAudit.length() == 0 && schedule.getActiveFlag() == Numeric.TWO_INT) {
				scheduleAudit.append(Text.HYPHEN);
			}
			if (scheduleAudit.length() > 0) {
				String eventName = getEvent(Text.FALSE, schedule.getLookupCode(), schedule.getActiveFlag(), ScheduleEventTypeEM.SCHEDULE.getName());
				scheduleAuditLogDTO.setEvent(eventName);

				scheduleAuditLogDTO.setCode(schedule.getCode());
				scheduleAuditLogDTO.setScheduleCode(StringUtil.isNotNull(schedule.getLookupCode()) ? schedule.getLookupCode() : schedule.getCode());
				scheduleAuditLogDTO.setEventType(ScheduleEventTypeEM.SCHEDULE);
				scheduleAuditLogDTO.setTableName("schedule");
				scheduleAuditLogDTO.setLog(scheduleAudit.toString());
				scheduleAuditLogDTO.setActiveFlag(schedule.getActiveFlag());
				scheduleAuditList.add(scheduleAuditLogDTO);

				scheduleAuditDAO.addScheduleAudit(authDTO, scheduleAuditList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateScheduleStationAudit(AuthDTO authDTO, ScheduleStationDTO scheduleStation) {
		try {
			List<ScheduleAuditLogDTO> scheduleAuditList = new ArrayList<ScheduleAuditLogDTO>();
			ScheduleAuditDAO scheduleAuditDAO = new ScheduleAuditDAO();
			StringBuilder scheduleStationAudit = new StringBuilder();
			ScheduleStationDTO existStatation = null;
			ScheduleAuditLogDTO scheduleAuditLog = new ScheduleAuditLogDTO();

			if (scheduleStation.getActiveFlag() != Numeric.FIVE_INT) {
				ScheduleCache schedulCache = new ScheduleCache();
				ScheduleDTO scheduleDTO = schedulCache.getScheduleDTO(authDTO, scheduleStation.getSchedule());

				List<ScheduleStationDTO> stationList = schedulCache.getScheduleStationDTO(authDTO, scheduleDTO);
				for (ScheduleStationDTO ScheduleStation : stationList) {
					if (scheduleStation.getCode().equals(ScheduleStation.getCode())) {
						existStatation = ScheduleStation;
						break;
					}
					for (ScheduleStationDTO overrideScheuleStation : ScheduleStation.getOverrideList()) {
						if (scheduleStation.getCode().equals(overrideScheuleStation.getCode())) {
							existStatation = overrideScheuleStation;
							break;
						}
					}
					if (existStatation != null) {
						break;
					}
				}
			}
			if (existStatation != null && scheduleStation.getActiveFlag() == Numeric.ONE_INT) {
				existStatation.setStation(getStationDTObyId(existStatation.getStation()));
				if (StringUtil.isNotNull(scheduleStation.getStation().getCode()) && StringUtil.isNotNull(existStatation.getStation().getCode()) && !scheduleStation.getStation().getCode().equals(existStatation.getStation().getCode())) {
					scheduleStationAudit.append("Station ");
					scheduleStationAudit.append(existStatation.getStation().getName());
					scheduleStationAudit.append(" changed to ");
					scheduleStationAudit.append(scheduleStation.getStation().getName() + Text.SINGLE_SPACE);
					scheduleStationAudit.append("\n");
				}
				if ((StringUtil.isNotNull(scheduleStation.getActiveFrom()) && StringUtil.isNotNull(scheduleStation.getActiveTo())) || (StringUtil.isNotNull(existStatation.getActiveFrom()) && StringUtil.isNotNull(existStatation.getActiveTo()))) {
					if ((StringUtil.isNotNull(scheduleStation.getActiveFrom()) && StringUtil.isNotNull(existStatation.getActiveFrom()) && !scheduleStation.getActiveFrom().trim().equals(existStatation.getActiveFrom().trim())) || (StringUtil.isNotNull(scheduleStation.getActiveTo()) && StringUtil.isNotNull(existStatation.getActiveTo()) && !scheduleStation.getActiveTo().trim().equals(existStatation.getActiveTo().trim()))) {
						scheduleStationAudit.append(existStatation.getActiveFrom());
						scheduleStationAudit.append("|");
						scheduleStationAudit.append(existStatation.getActiveTo());
						scheduleStationAudit.append(" changed to ");
						scheduleStationAudit.append(scheduleStation.getActiveFrom().trim());
						scheduleStationAudit.append("|");
						scheduleStationAudit.append(scheduleStation.getActiveTo().trim() + Text.SINGLE_SPACE);
						scheduleStationAudit.append("\n");
					}
					else if (((StringUtil.isNotNull(scheduleStation.getActiveFrom()) && StringUtil.isNotNull(scheduleStation.getActiveTo())) || (StringUtil.isNotNull(existStatation.getActiveFrom()) && StringUtil.isNotNull(existStatation.getActiveTo())))) {
						if (StringUtil.isNotNull(scheduleStation.getActiveFrom()) && StringUtil.isNotNull(scheduleStation.getActiveTo()) && StringUtil.isNull(existStatation.getActiveFrom()) && StringUtil.isNull(existStatation.getActiveTo())) {
							scheduleStationAudit.append("Added ");
							scheduleStationAudit.append(scheduleStation.getActiveFrom().trim());
							scheduleStationAudit.append("|");
							scheduleStationAudit.append(scheduleStation.getActiveTo().trim() + Text.SINGLE_SPACE);
							scheduleStationAudit.append("\n");
						}
						else if (StringUtil.isNotNull(existStatation.getActiveFrom()) && StringUtil.isNotNull(existStatation.getActiveTo()) && StringUtil.isNull(scheduleStation.getActiveFrom()) && StringUtil.isNull(scheduleStation.getActiveTo())) {
							scheduleStationAudit.append("Removed ");
							scheduleStationAudit.append(existStatation.getActiveFrom().trim());
							scheduleStationAudit.append("|");
							scheduleStationAudit.append(existStatation.getActiveTo().trim() + Text.SINGLE_SPACE);
							scheduleStationAudit.append("\n");
						}
					}
				}
				if ((StringUtil.isNotNull(scheduleStation.getDayOfWeek()) || StringUtil.isNotNull(existStatation.getDayOfWeek()))) {
					if (StringUtil.isNotNull(scheduleStation.getDayOfWeek()) && StringUtil.isNotNull(existStatation.getDayOfWeek()) && !scheduleStation.getDayOfWeek().equals(existStatation.getDayOfWeek())) {
						scheduleStationAudit.append("Day of week ");
						scheduleStationAudit.append(StringUtil.isNotNull(existStatation.getDayOfWeek()) ? StringUtil.getDayOfWeek(existStatation.getDayOfWeek()) + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStationAudit.append("changed");
						scheduleStationAudit.append(StringUtil.isNotNull(scheduleStation.getDayOfWeek()) ? " to " + StringUtil.getDayOfWeek(scheduleStation.getDayOfWeek()) + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStationAudit.append("\n");
					}
					else if (StringUtil.isNotNull(scheduleStation.getDayOfWeek()) && StringUtil.isNull(existStatation.getDayOfWeek())) {
						scheduleStationAudit.append("Day of Week ");
						scheduleStationAudit.append(StringUtil.isNotNull(scheduleStation.getDayOfWeek()) ? StringUtil.getDayOfWeek(scheduleStation.getDayOfWeek()) + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStationAudit.append("Added ");
						scheduleStationAudit.append("\n");
					}
					else if (StringUtil.isNotNull(existStatation.getDayOfWeek()) && StringUtil.isNull(scheduleStation.getDayOfWeek())) {
						scheduleStationAudit.append("Day of Week ");
						scheduleStationAudit.append(StringUtil.isNotNull(existStatation.getDayOfWeek()) ? StringUtil.getDayOfWeek(existStatation.getDayOfWeek()) + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStationAudit.append("removed ");
						scheduleStationAudit.append("\n");
					}
				}
				if (scheduleStation.getMinitues() != existStatation.getMinitues()) {
					scheduleStationAudit.append("Station Time ");
					scheduleStationAudit.append(DateUtil.getMinutesToTime(existStatation.getMinitues()));
					scheduleStationAudit.append(" changed to ");
					scheduleStationAudit.append(DateUtil.getMinutesToTime(scheduleStation.getMinitues()) + Text.SINGLE_SPACE);
					scheduleStationAudit.append("\n");
				}
				if (StringUtil.isNotNull(scheduleStation.getMobileNumber()) || StringUtil.isNotNull(existStatation.getMobileNumber())) {
					if (StringUtil.isNotNull(scheduleStation.getMobileNumber()) && StringUtil.isNotNull(existStatation.getMobileNumber()) && !scheduleStation.getMobileNumber().equals(existStatation.getMobileNumber())) {
						scheduleStationAudit.append("Mobile No ");
						scheduleStationAudit.append(StringUtil.isNotNull(existStatation.getMobileNumber()) ? existStatation.getMobileNumber() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStationAudit.append("changed");
						scheduleStationAudit.append(StringUtil.isNotNull(scheduleStation.getMobileNumber()) ? " to " + scheduleStation.getMobileNumber() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStationAudit.append("\n");
					}
					else if (StringUtil.isNotNull(scheduleStation.getMobileNumber()) && StringUtil.isNull(existStatation.getMobileNumber())) {
						scheduleStationAudit.append("Mobile No ");
						scheduleStationAudit.append(StringUtil.isNotNull(scheduleStation.getMobileNumber()) ? scheduleStation.getMobileNumber() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStationAudit.append("Added ");
						scheduleStationAudit.append("\n");
					}
					else if (StringUtil.isNotNull(existStatation.getMobileNumber()) && StringUtil.isNull(scheduleStation.getMobileNumber())) {
						scheduleStationAudit.append("Mobile No ");
						scheduleStationAudit.append(StringUtil.isNotNull(existStatation.getMobileNumber()) ? existStatation.getMobileNumber() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStationAudit.append("removed");
						scheduleStationAudit.append("\n");
					}
				}
				if (scheduleStationAudit.length() > 0) {
					scheduleStationAudit.insert(0, scheduleStation.getStation().getName() + (scheduleStation.getMinitues() == -1 ? " Exception" : Text.EMPTY) + " - ");
				}
			}
			else if (scheduleStation.getActiveFlag() == Numeric.TWO_INT) {
				scheduleStationAudit.append(scheduleStation.getStation().getName() + (scheduleStation.getMinitues() == -1 ? " Exception" : Text.EMPTY));
			}
			if (scheduleStationAudit.length() == 0 && scheduleStation.getActiveFlag() == Numeric.TWO_INT) {
				scheduleStationAudit.append(Text.HYPHEN);
			}

			if (scheduleStationAudit.length() > 0) {
				String eventName = getEvent(Text.FALSE, scheduleStation.getLookupCode(), scheduleStation.getActiveFlag(), ScheduleEventTypeEM.SCHEDULE_STATION.getName());
				scheduleAuditLog.setEvent(eventName);

				scheduleAuditLog.setCode(StringUtil.isNotNull(scheduleStation.getCode()) ? scheduleStation.getCode() : scheduleStation.getCode());
				scheduleAuditLog.setScheduleCode(StringUtil.isNotNull(scheduleStation.getSchedule().getCode()) ? scheduleStation.getSchedule().getCode() : Text.NA);
				scheduleAuditLog.setEventType(ScheduleEventTypeEM.SCHEDULE_STATION);
				scheduleAuditLog.setTableName("schedule_station");
				scheduleAuditLog.setLog(scheduleStationAudit.toString());
				scheduleAuditLog.setActiveFlag(scheduleStation.getActiveFlag());
				scheduleAuditList.add(scheduleAuditLog);

				scheduleAuditDAO.addScheduleAudit(authDTO, scheduleAuditList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updateScheduleStationPointAudit(AuthDTO authDTO, ScheduleStationPointDTO scheduleStationPoint) {
		try {
			List<ScheduleAuditLogDTO> scheduleAuditList = new ArrayList<ScheduleAuditLogDTO>();
			ScheduleAuditDAO scheduleAuditDAO = new ScheduleAuditDAO();
			BusVehicleVanPickupDAO vanPickupDAO = new BusVehicleVanPickupDAO();
			ScheduleAuditLogDTO scheduleAuditLog = new ScheduleAuditLogDTO();
			StringBuilder scheduleStageFare = new StringBuilder();

			ScheduleCache schedulCache = new ScheduleCache();
			List<ScheduleStationPointDTO> scheduleStationPointList = schedulCache.getScheduleStationPointDTO(authDTO, scheduleStationPoint.getSchedule());
			ScheduleStationPointDTO existStationPoint = null;
			for (ScheduleStationPointDTO stationPointDTO : scheduleStationPointList) {
				if (scheduleStationPoint.getCode().equals(stationPointDTO.getCode())) {
					existStationPoint = stationPointDTO;
					break;
				}
				for (ScheduleStationPointDTO overrideStationPoint : stationPointDTO.getOverrideList()) {
					if (scheduleStationPoint.getCode().equals(overrideStationPoint.getCode())) {
						existStationPoint = overrideStationPoint;
						break;
					}
				}
				if (existStationPoint != null) {
					break;
				}
			}
			if (existStationPoint != null && scheduleStationPoint.getActiveFlag() == Numeric.ONE_INT) {
				getStationPointDTObyId(authDTO, scheduleStationPoint.getStationPoint());
				if (scheduleStationPoint.getStationPoint() != null && existStationPoint.getStationPoint() != null) {
					getStationPointDTObyId(authDTO, existStationPoint.getStationPoint());
					if (!scheduleStationPoint.getStationPoint().getCode().equals(existStationPoint.getStationPoint().getCode())) {
						scheduleStageFare.append("Station Point ");
						scheduleStageFare.append(StringUtil.isNotNull(existStationPoint.getStationPoint().getName()) ? existStationPoint.getStationPoint().getName() : Text.EMPTY);
						scheduleStageFare.append(" changed to ");
						scheduleStageFare.append(StringUtil.isNotNull(scheduleStationPoint.getStationPoint().getName()) ? scheduleStationPoint.getStationPoint().getName() + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStageFare.append("\n");
					}
				}
				if ((scheduleStationPoint.getBusVehicleVanPickup() != null && StringUtil.isNotNull(scheduleStationPoint.getBusVehicleVanPickup().getCode())) || (existStationPoint.getBusVehicleVanPickup() != null && existStationPoint.getBusVehicleVanPickup().getId() != Numeric.ZERO_INT)) {
					if (!scheduleStationPoint.getBusVehicleVanPickup().getCode().equals(existStationPoint.getBusVehicleVanPickup().getCode())) {
						scheduleStageFare.append("Van Route ");
						if (existStationPoint.getBusVehicleVanPickup() != null && existStationPoint.getBusVehicleVanPickup().getId() != Numeric.ZERO_INT) {
							vanPickupDAO.getBusVehicleVanPickup(authDTO, existStationPoint.getBusVehicleVanPickup());
							scheduleStageFare.append(StringUtil.isNotNull(existStationPoint.getBusVehicleVanPickup().getName()) ? existStationPoint.getBusVehicleVanPickup().getName() + Text.SINGLE_SPACE : Text.EMPTY);
						}
						scheduleStageFare.append("changed");
						if (scheduleStationPoint.getBusVehicleVanPickup() != null && StringUtil.isNotNull(scheduleStationPoint.getBusVehicleVanPickup().getCode())) {
							vanPickupDAO.getBusVehicleVanPickup(authDTO, scheduleStationPoint.getBusVehicleVanPickup());
							scheduleStageFare.append(StringUtil.isNotNull(scheduleStationPoint.getBusVehicleVanPickup().getName()) ? " to " + scheduleStationPoint.getBusVehicleVanPickup().getName() + Text.SINGLE_SPACE : Text.EMPTY);
						}
						scheduleStageFare.append("\n");
					}
				}
				if ((scheduleStationPoint.getMinitues() != 0 || existStationPoint.getMinitues() != 0) && scheduleStationPoint.getMinitues() != existStationPoint.getMinitues()) {
					scheduleStageFare.append("Time ");
					scheduleStageFare.append(existStationPoint.getMinitues());
					scheduleStageFare.append(" changed to ");
					scheduleStageFare.append(scheduleStationPoint.getMinitues() + Text.SINGLE_SPACE);
					scheduleStageFare.append("\n");
				}
				if (StringUtil.isNotNull(scheduleStationPoint.getActiveFrom()) || StringUtil.isNotNull(existStationPoint.getActiveFrom())) {
					if ((StringUtil.isNotNull(scheduleStationPoint.getActiveFrom()) && StringUtil.isNotNull(existStationPoint.getActiveFrom()) && !scheduleStationPoint.getActiveFrom().trim().equals(existStationPoint.getActiveFrom().trim())) || (StringUtil.isNotNull(scheduleStationPoint.getActiveTo()) && StringUtil.isNotNull(existStationPoint.getActiveTo()) && !scheduleStationPoint.getActiveTo().trim().equals(existStationPoint.getActiveTo().trim()))) {
						scheduleStageFare.append(existStationPoint.getActiveFrom().trim());
						scheduleStageFare.append(" | ");
						scheduleStageFare.append(existStationPoint.getActiveTo().trim());
						scheduleStageFare.append(" changed ");
						scheduleStageFare.append(scheduleStationPoint.getActiveFrom().trim());
						scheduleStageFare.append(" | ");
						scheduleStageFare.append(scheduleStationPoint.getActiveTo().trim() + Text.SINGLE_SPACE);
						scheduleStageFare.append("\n");
					}
					else if (StringUtil.isNotNull(scheduleStationPoint.getActiveFrom()) && StringUtil.isNull(existStationPoint.getActiveFrom())) {
						scheduleStageFare.append("Added ");
						scheduleStageFare.append(scheduleStationPoint.getActiveFrom().trim());
						scheduleStageFare.append("|");
						scheduleStageFare.append(scheduleStationPoint.getActiveTo().trim() + Text.SINGLE_SPACE);
						scheduleStageFare.append("\n");
					}
				}
				if (StringUtil.isNotNull(scheduleStationPoint.getDayOfWeek()) || StringUtil.isNotNull(existStationPoint.getDayOfWeek())) {
					if (StringUtil.isNotNull(scheduleStationPoint.getDayOfWeek()) && StringUtil.isNotNull(existStationPoint.getDayOfWeek()) && !scheduleStationPoint.getDayOfWeek().equals(existStationPoint.getDayOfWeek())) {
						scheduleStageFare.append("Day of Week ");
						scheduleStageFare.append(StringUtil.isNotNull(existStationPoint.getDayOfWeek()) ? StringUtil.getDayOfWeek(existStationPoint.getDayOfWeek()) + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStageFare.append("changed");
						scheduleStageFare.append(StringUtil.isNotNull(scheduleStationPoint.getDayOfWeek()) ? " to " + StringUtil.getDayOfWeek(scheduleStationPoint.getDayOfWeek()) + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStageFare.append("\n");
					}
					else if (StringUtil.isNotNull(scheduleStationPoint.getDayOfWeek()) && StringUtil.isNull(existStationPoint.getDayOfWeek())) {
						scheduleStageFare.append("Day of Week ");
						scheduleStageFare.append(StringUtil.isNotNull(scheduleStationPoint.getDayOfWeek()) ? StringUtil.getDayOfWeek(scheduleStationPoint.getDayOfWeek()) + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStageFare.append("Added ");
					}
					else if (StringUtil.isNotNull(existStationPoint.getDayOfWeek()) && StringUtil.isNull(scheduleStationPoint.getDayOfWeek())) {
						scheduleStageFare.append("Day of Week ");
						scheduleStageFare.append(StringUtil.isNotNull(existStationPoint.getDayOfWeek()) ? StringUtil.getDayOfWeek(existStationPoint.getDayOfWeek()) + Text.SINGLE_SPACE : Text.EMPTY);
						scheduleStageFare.append("Removed ");
					}
				}
				if ((StringUtil.isNotNull(scheduleStationPoint.getBoardingDroppingFlag()) && StringUtil.isNotNull(existStationPoint.getBoardingDroppingFlag())) && !scheduleStationPoint.getBoardingDroppingFlag().equals(existStationPoint.getBoardingDroppingFlag())) {
					scheduleStageFare.append("Boarding/Dropping: ");
					scheduleStageFare.append(StringUtil.isNotNull(existStationPoint.getBoardingDroppingFlag()) ? existStationPoint.getBoardingDroppingFlag().equals("11") ? "Boarding and Dropping" : existStationPoint.getBoardingDroppingFlag().equals("10") ? "Boarding" : "Dropping" : Text.EMPTY);
					scheduleStageFare.append(" changed to ");
					scheduleStageFare.append(StringUtil.isNotNull(scheduleStationPoint.getBoardingDroppingFlag()) ? scheduleStationPoint.getBoardingDroppingFlag().equals("11") ? "Boarding and Dropping" : scheduleStationPoint.getBoardingDroppingFlag().equals("10") ? "Boarding" : "Dropping" : Text.EMPTY);
					scheduleStageFare.append("\n");
				}
				if (scheduleStageFare.length() > 0) {
					scheduleStageFare.insert(0, scheduleStationPoint.getStationPoint().getName() + (scheduleStationPoint.getMinitues() == -1 ? " Exception" : Text.EMPTY) + " - ");
				}
			}
			else if (scheduleStationPoint.getActiveFlag() == Numeric.TWO_INT && existStationPoint != null) {
				getStationPointDTObyId(authDTO, existStationPoint.getStationPoint());
				scheduleStageFare.append(existStationPoint.getStationPoint().getName() + (existStationPoint.getMinitues() == -1 ? " Exception" : Text.EMPTY));
			}
			if (scheduleStageFare.length() == 0 && scheduleStationPoint.getActiveFlag() == Numeric.TWO_INT) {
				scheduleStageFare.append(Text.HYPHEN);
			}
			if (scheduleStageFare.length() > 0) {
				String eventName = getEvent(false, scheduleStationPoint.getLookupCode(), scheduleStationPoint.getActiveFlag(), ScheduleEventTypeEM.SCHEDULE_STATION_POINT.getName());
				scheduleAuditLog.setEvent(eventName);

				scheduleAuditLog.setCode(scheduleStationPoint.getCode());
				scheduleAuditLog.setScheduleCode(StringUtil.isNotNull(scheduleStationPoint.getSchedule().getCode()) ? scheduleStationPoint.getSchedule().getCode() : Text.NA);
				scheduleAuditLog.setEventType(ScheduleEventTypeEM.SCHEDULE_STATION_POINT);
				scheduleAuditLog.setTableName("schedule_station_point");
				scheduleAuditLog.setLog(scheduleStageFare.toString());
				scheduleAuditLog.setActiveFlag(scheduleStationPoint.getActiveFlag());
				scheduleAuditList.add(scheduleAuditLog);

				scheduleAuditDAO.addScheduleAudit(authDTO, scheduleAuditList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getEvent(boolean isNewData, String lookupCode, int activeFlag, String event) {
		String eventName = "";
		if (StringUtil.isNull(lookupCode)) {
			if (isNewData && activeFlag == Numeric.ONE_INT) {
				eventName = event + " created";
			}
			else if (!isNewData && activeFlag == Numeric.ONE_INT) {
				eventName = event + " updated";
			}
			else if (!isNewData && activeFlag == Numeric.TWO_INT) {
				eventName = event + " deleted";
			}
			else if (!isNewData && activeFlag == Numeric.FIVE_INT) {
				eventName = event + Text.SINGLE_SPACE + "sequence changed";
			}
		}
		else if (StringUtil.isNotNull(lookupCode)) {
			if (isNewData && activeFlag == Numeric.ONE_INT) {
				eventName = event + Text.SINGLE_SPACE + "exception/override created";
			}
			else if (!isNewData && activeFlag == Numeric.ONE_INT) {
				eventName = event + Text.SINGLE_SPACE + "exception/override updated";
			}
			else if (!isNewData && activeFlag != Numeric.ONE_INT) {
				eventName = event + Text.SINGLE_SPACE + "exception/override deleted";
			}
		}
		return eventName;
	}
}

package org.in.com.service.impl;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.ehcache.Element;

import org.in.com.cache.CancellationTermsCache;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatTypeFareDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleCancellationTermDTO;
import org.in.com.dto.ScheduleCategoryDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.service.BusBreakevenService;
import org.in.com.service.BusService;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleActivityService;
import org.in.com.service.ScheduleCategoryService;
import org.in.com.service.StationService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleActivityServiceImpl extends ScheduleCache implements ScheduleActivityService {
	@Autowired
	BusService busService;
	@Autowired
	ScheduleCategoryService categoryService;
	@Autowired
	GroupService groupService;
	@Autowired
	StationService stationService;
	@Autowired
	BusBreakevenService busBreakevenService;

	@Override
	public boolean scheduleActivity(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		boolean notifyStages = false;
		try {
			List<Map<String, String>> scheduleActivityList = new ArrayList<>();
			String header = generateHeader(authDTO, scheduleDTO);

			// Schedule Log
			// New Schedule Name
			if (StringUtil.isNull(scheduleDTO.getCode())) {
				StringBuilder keyword = new StringBuilder();
				StringBuilder newContent = new StringBuilder();
				newContent.append(StringUtil.isNotNull(scheduleDTO.getName()) ? scheduleDTO.getName() + Text.COMMA : Text.EMPTY);
				newContent.append(StringUtil.isNotNull(scheduleDTO.getServiceNumber()) ? "Service Number:" + scheduleDTO.getServiceNumber() + Text.COMMA : Text.EMPTY);
				if (StringUtil.isNull(scheduleDTO.getLookupCode())) {
					keyword.append("Schedule has been created");
				}
				else if (StringUtil.isNotNull(scheduleDTO.getLookupCode())) {
					keyword.append("Schedule exception has been created");
				}

				newContent.append(StringUtil.isNotNull(scheduleDTO.getDisplayName()) ? "Display Name:" + scheduleDTO.getDisplayName() + Text.COMMA : Text.EMPTY);
				newContent.append(StringUtil.isNotNull(scheduleDTO.getPnrStartCode()) ? "PNR Start Code" + scheduleDTO.getPnrStartCode() + Text.COMMA : Text.EMPTY);
				newContent.append(StringUtil.isNotNull(scheduleDTO.getApiDisplayName()) ? "API Display Name:" + scheduleDTO.getApiDisplayName() + Text.COMMA : Text.EMPTY);
				newContent.append(StringUtil.isNotNull(scheduleDTO.getActiveFrom()) ? "Active From:" + new DateTime(scheduleDTO.getActiveFrom()).format("DD/MM/YYYY") + Text.COMMA : Text.EMPTY);
				newContent.append(StringUtil.isNotNull(scheduleDTO.getActiveTo()) ? "Active To:" + new DateTime(scheduleDTO.getActiveTo()).format("DD/MM/YYYY") + Text.COMMA : Text.EMPTY);
				newContent.append(StringUtil.isNotNull(scheduleDTO.getDayOfWeek()) ? "DayOfWeek:" + StringUtil.getDayOfWeek(scheduleDTO.getDayOfWeek()) + Text.COMMA : Text.EMPTY);

				Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header.toString(), keyword.toString(), Text.HYPHEN, newContent.toString());
				scheduleActivityList.add(scheduleActivityMap);
			}
			else if (StringUtil.isNotNull(scheduleDTO.getCode())) {
				if (StringUtil.isNull(scheduleDTO.getLookupCode()) && scheduleDTO.getActiveFlag() != 1) {
					StringBuilder keyword = new StringBuilder();
					keyword.append("Schedule has been deleted");

					StringBuilder oldContent = new StringBuilder();
					oldContent.append(scheduleDTO.getName());

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), Text.HYPHEN);
					scheduleActivityList.add(scheduleActivityMap);
				}
				else if (StringUtil.isNotNull(scheduleDTO.getLookupCode()) && scheduleDTO.getActiveFlag() != 1) {
					StringBuilder keyword = new StringBuilder();
					keyword.append("Schedule exception has been deleted");

					StringBuilder oldContent = new StringBuilder();
					oldContent.append(scheduleDTO.getName());

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), Text.HYPHEN);
					scheduleActivityList.add(scheduleActivityMap);
				}
				else if (scheduleDTO.getActiveFlag() == 1) {
					// Previous Schedule
					ScheduleDTO previousSchedule = new ScheduleDTO();
					previousSchedule.setCode(scheduleDTO.getCode());
					getScheduleDTO(authDTO, previousSchedule);

					// Schedule Name
					if ((previousSchedule == null || StringUtil.isNull(previousSchedule.getName())) && StringUtil.isNotNull(scheduleDTO.getName())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule name has been created");

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleDTO.getName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					else if (previousSchedule != null && !previousSchedule.getName().equals(scheduleDTO.getName())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule name has been changed");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(previousSchedule.getName());

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleDTO.getName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					// Service Number
					if (previousSchedule == null || StringUtil.isNull(previousSchedule.getServiceNumber())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Service number has been created");

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleDTO.getServiceNumber());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					else if (previousSchedule != null && StringUtil.isNotNull(previousSchedule.getServiceNumber()) && !previousSchedule.getServiceNumber().equals(scheduleDTO.getServiceNumber())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Service number has been changed");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(previousSchedule.getServiceNumber());

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleDTO.getServiceNumber());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}

					// Display Name
					if ((previousSchedule == null || StringUtil.isNull(previousSchedule.getDisplayName())) && StringUtil.isNotNull(scheduleDTO.getDisplayName())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Display Name created");

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleDTO.getDisplayName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					else if (previousSchedule != null && !previousSchedule.getDisplayName().equals(scheduleDTO.getDisplayName())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Display Name changed");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(previousSchedule.getDisplayName());

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleDTO.getDisplayName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					// PNR Start Code
					if ((previousSchedule == null || StringUtil.isNull(previousSchedule.getPnrStartCode())) && StringUtil.isNotNull(scheduleDTO.getPnrStartCode())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule PNR Start Code created");

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleDTO.getPnrStartCode());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					else if (previousSchedule != null && !previousSchedule.getPnrStartCode().equals(scheduleDTO.getPnrStartCode())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule PNR Start Code changed");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(previousSchedule.getPnrStartCode());

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleDTO.getPnrStartCode());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					// API Display Name
					if ((previousSchedule == null || StringUtil.isNull(previousSchedule.getApiDisplayName())) && StringUtil.isNotNull(scheduleDTO.getApiDisplayName())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Api Display Name created");

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleDTO.getApiDisplayName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);

					}
					else if (previousSchedule != null && !previousSchedule.getApiDisplayName().equals(scheduleDTO.getApiDisplayName())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Api Display Name changed");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(previousSchedule.getApiDisplayName());

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleDTO.getApiDisplayName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);

					}
					// Active From
					if ((previousSchedule == null || StringUtil.isNull(previousSchedule.getActiveFrom())) && StringUtil.isNotNull(scheduleDTO.getActiveFrom())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Active From created");

						StringBuilder newContent = new StringBuilder();
						newContent.append(new DateTime(scheduleDTO.getActiveFrom()).format("DD/MM/YYYY"));

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					else if (previousSchedule != null && !previousSchedule.getActiveFrom().equals(scheduleDTO.getActiveFrom())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Active From changed");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(new DateTime(previousSchedule.getActiveFrom()).format("DD/MM/YYYY"));

						StringBuilder newContent = new StringBuilder();
						newContent.append(new DateTime(scheduleDTO.getActiveFrom()).format("DD/MM/YYYY"));

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
						
						notifyStages = true;
					}
					// Active To
					if ((previousSchedule == null || StringUtil.isNull(previousSchedule.getActiveTo())) && StringUtil.isNotNull(scheduleDTO.getActiveTo())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Active To created");

						StringBuilder newContent = new StringBuilder();
						newContent.append(new DateTime(scheduleDTO.getActiveTo()).format("DD/MM/YYYY"));

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					else if (previousSchedule != null && !previousSchedule.getActiveTo().equals(scheduleDTO.getActiveTo())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Active To changed");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(new DateTime(previousSchedule.getActiveTo()).format("DD/MM/YYYY"));

						StringBuilder newContent = new StringBuilder();
						newContent.append(new DateTime(scheduleDTO.getActiveTo()).format("DD/MM/YYYY"));

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
						
						notifyStages = true;
					}
					// Day Of Week
					if ((previousSchedule == null || StringUtil.isNull(previousSchedule.getDayOfWeek())) && StringUtil.isNotNull(scheduleDTO.getDayOfWeek())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Day Of Week created");

						StringBuilder newContent = new StringBuilder();
						newContent.append(StringUtil.getDayOfWeek(scheduleDTO.getDayOfWeek()));

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					else if (previousSchedule != null && !previousSchedule.getDayOfWeek().equals(scheduleDTO.getDayOfWeek())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Day Of Week changed");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(StringUtil.getDayOfWeek(previousSchedule.getDayOfWeek()));

						StringBuilder newContent = new StringBuilder();
						newContent.append(StringUtil.getDayOfWeek(scheduleDTO.getDayOfWeek()));

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
						
						notifyStages = true;
					}
					// Category
					if ((previousSchedule == null || previousSchedule.getCategory() == null) && scheduleDTO.getCategory() != null && StringUtil.isNotNull(scheduleDTO.getCategory().getCode())) {
						List<ScheduleCategoryDTO> categoryList = categoryService.getAll(authDTO);
						ScheduleCategoryDTO scheduleCategory = null;
						for (ScheduleCategoryDTO scheduleCategoryDTO : categoryList) {
							if (scheduleDTO.getCategory().getCode().equals(scheduleCategoryDTO.getCode())) {
								scheduleCategory = scheduleCategoryDTO;
								break;
							}
						}

						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Category created");

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleCategory.getName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					else if (previousSchedule != null && scheduleDTO.getCategory() != null && previousSchedule.getCategory() != null && StringUtil.isNotNull(previousSchedule.getCategory().getCode()) && !previousSchedule.getCategory().getCode().equals(scheduleDTO.getCategory().getCode())) {
						List<ScheduleCategoryDTO> categoryList = categoryService.getAll(authDTO);
						ScheduleCategoryDTO previousCategory = null;
						ScheduleCategoryDTO scheduleCategory = null;
						for (ScheduleCategoryDTO scheduleCategoryDTO : categoryList) {
							if (scheduleDTO.getCategory().getCode().equals(scheduleCategoryDTO.getCode())) {
								scheduleCategory = scheduleCategoryDTO;
							}
							if (previousSchedule.getCategory().getCode().equals(scheduleCategoryDTO.getCode())) {
								previousCategory = scheduleCategoryDTO;
							}
							if (scheduleCategory != null && previousCategory != null) {
								break;
							}
						}
						StringBuilder keyword = new StringBuilder();
						keyword.append("Schedule Category changed");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(previousSchedule.getCategory().getName());

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleDTO.getCategory().getName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
				}
			}
			// store Cache
			if (!scheduleActivityList.isEmpty()) {
				putScheduleActivityCache(scheduleDTO, scheduleActivityList);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return notifyStages;
	}

	@Override
	public void scheduleStationActivity(AuthDTO authDTO, ScheduleStationDTO scheduleStationDTO) {
		try {
			// Schedule Activity Log
			List<Map<String, String>> scheduleActivityList = new ArrayList<>();

			// Previous Schedule
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleStationDTO.getSchedule().getCode());
			getScheduleDTO(authDTO, scheduleDTO);

			String header = generateHeader(authDTO, scheduleDTO);

			// New Schedule Name
			if (StringUtil.isNull(scheduleStationDTO.getCode())) {
				StringBuilder keyword = new StringBuilder();
				if (StringUtil.isNotNull(scheduleStationDTO.getLookupCode()) && scheduleStationDTO.getMinitues() == -1) {
					keyword.append("Station exception has been created");
				}
				else if (StringUtil.isNotNull(scheduleStationDTO.getLookupCode()) && scheduleStationDTO.getMinitues() != -1) {
					keyword.append("Station override has been created");
				}
				else if (StringUtil.isNull(scheduleStationDTO.getLookupCode())) {
					keyword.append("Station has been created");
				}

				StringBuilder newContent = new StringBuilder();
				newContent.append(scheduleStationDTO.getStation().getName());
				newContent.append(Text.SINGLE_SPACE);
				newContent.append(getTime(scheduleStationDTO.getMinitues()));

				Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
				scheduleActivityList.add(scheduleActivityMap);
			}
			else if (StringUtil.isNotNull(scheduleStationDTO.getCode())) {
				List<ScheduleStationDTO> stationDTOList = getScheduleStationDTO(authDTO, scheduleDTO);
				ScheduleStationDTO previousScheduleStation = null;
				for (ScheduleStationDTO scheduleStation : stationDTOList) {
					if (scheduleStation.getCode().equals(scheduleStationDTO.getCode())) {
						previousScheduleStation = scheduleStation;
						previousScheduleStation.setStation(stationService.getStation(previousScheduleStation.getStation()));
						break;
					}
				}
				if (scheduleStationDTO.getActiveFlag() != 1) {
					StringBuilder keyword = new StringBuilder();
					if (StringUtil.isNotNull(scheduleStationDTO.getLookupCode()) && scheduleStationDTO.getMinitues() == -1) {
						keyword.append("Station exception has been deleted");
					}
					else if (StringUtil.isNotNull(scheduleStationDTO.getLookupCode()) && scheduleStationDTO.getMinitues() != -1) {
						keyword.append("Station override has been deleted");
					}
					else if (StringUtil.isNull(scheduleStationDTO.getLookupCode())) {
						keyword.append("Station has been deleted");
					}

					StringBuilder oldContent = new StringBuilder();
					oldContent.append(scheduleStationDTO.getStation().getName());

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), Text.HYPHEN);
					scheduleActivityList.add(scheduleActivityMap);

				}
				else if (scheduleStationDTO.getActiveFlag() == 1) {
					// Minutes
					if (StringUtil.isNull(scheduleStationDTO.getLookupCode())) {
						if (previousScheduleStation == null || previousScheduleStation.getMinitues() != scheduleStationDTO.getMinitues()) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStation == null || previousScheduleStation.getMinitues() == Numeric.ZERO_INT ? "Station Minutes has been created" : "Station Minutes has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStation != null && previousScheduleStation.getStation() != null) {
								oldContent.append(previousScheduleStation.getStation().getName());
								oldContent.append(Text.SINGLE_SPACE);
								oldContent.append(getTime(previousScheduleStation.getMinitues()));
							}
							else if (previousScheduleStation == null || previousScheduleStation.getStation() == null) {
								oldContent.append(Text.HYPHEN);
							}

							StringBuilder newContent = new StringBuilder();
							newContent.append(scheduleStationDTO.getStation().getName());
							newContent.append(Text.SINGLE_SPACE);
							newContent.append(getTime(scheduleStationDTO.getMinitues()));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Station Sequence
						if (previousScheduleStation == null || previousScheduleStation.getStationSequence() != scheduleStationDTO.getStationSequence()) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStation == null || previousScheduleStation.getStationSequence() == Numeric.ZERO_INT ? "Station Sequence has been created" : "Station Sequence has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStation != null && previousScheduleStation.getStation() != null) {
								oldContent.append(previousScheduleStation.getStation().getName());
								oldContent.append(" Sequence is ");
								oldContent.append(previousScheduleStation.getStationSequence());
							}
							else if (previousScheduleStation == null || previousScheduleStation.getStation() == null) {
								oldContent.append(Text.HYPHEN);
							}

							StringBuilder newContent = new StringBuilder();
							newContent.append(scheduleStationDTO.getStation().getName());
							newContent.append(" Sequence is ");
							newContent.append(scheduleStationDTO.getStationSequence());

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Active From
						if ((previousScheduleStation == null || StringUtil.isNull(previousScheduleStation.getActiveFrom())) && StringUtil.isNotNull(scheduleStationDTO.getActiveFrom())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append("Station Active From has been created");

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(scheduleStationDTO.getStation().getName())) {
								newContent.append(scheduleStationDTO.getStation().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(new DateTime(scheduleStationDTO.getActiveFrom()).format("DD/MM/YYYY"));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						else if (previousScheduleStation != null && scheduleStationDTO != null && StringUtil.isNotNull(previousScheduleStation.getActiveFrom()) && StringUtil.isNotNull(scheduleStationDTO.getActiveFrom()) && !previousScheduleStation.getActiveFrom().equals(scheduleStationDTO.getActiveFrom())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append("Station Active To has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(previousScheduleStation.getStation().getName())) {
								oldContent.append(previousScheduleStation.getStation().getName());
								oldContent.append(Text.COMMA);
							}
							oldContent.append(new DateTime(previousScheduleStation.getActiveFrom()).format("DD/MM/YYYY"));

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(scheduleStationDTO.getStation().getName())) {
								newContent.append(scheduleStationDTO.getStation().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(new DateTime(scheduleStationDTO.getActiveFrom()).format("DD/MM/YYYY"));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Active To
						if ((previousScheduleStation == null || StringUtil.isNull(previousScheduleStation.getActiveTo())) && StringUtil.isNotNull(scheduleStationDTO.getActiveTo())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append("Station Active To has been created");

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(scheduleStationDTO.getStation().getName())) {
								newContent.append(scheduleStationDTO.getStation().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(new DateTime(scheduleStationDTO.getActiveTo()).format("DD/MM/YYYY"));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						else if (previousScheduleStation != null && StringUtil.isNotNull(previousScheduleStation.getActiveTo()) && StringUtil.isNotNull(scheduleStationDTO.getActiveTo()) && !previousScheduleStation.getActiveTo().equals(scheduleStationDTO.getActiveTo())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append("Station Active To has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(previousScheduleStation.getStation().getName())) {
								oldContent.append(previousScheduleStation.getStation().getName());
								oldContent.append(Text.COMMA);
							}
							oldContent.append(new DateTime(previousScheduleStation.getActiveTo()).format("DD/MM/YYYY"));

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(scheduleStationDTO.getStation().getName())) {
								newContent.append(scheduleStationDTO.getStation().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(new DateTime(scheduleStationDTO.getActiveTo()).format("DD/MM/YYYY"));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Day Of Week
						if ((previousScheduleStation == null || StringUtil.isNull(previousScheduleStation.getDayOfWeek())) && StringUtil.isNotNull(scheduleStationDTO.getDayOfWeek())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append("Station Day Of Week has been created");

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(scheduleStationDTO.getStation().getName())) {
								newContent.append(scheduleStationDTO.getStation().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(StringUtil.getDayOfWeek(scheduleStationDTO.getDayOfWeek()));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						else if (previousScheduleStation != null && StringUtil.isNotNull(previousScheduleStation.getDayOfWeek()) && StringUtil.isNotNull(scheduleStationDTO.getDayOfWeek()) && !previousScheduleStation.getDayOfWeek().equals(scheduleStationDTO.getDayOfWeek())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append("Station Day Of Week has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(previousScheduleStation.getStation().getName())) {
								oldContent.append(previousScheduleStation.getStation().getName());
								oldContent.append(Text.COMMA);
							}
							oldContent.append(StringUtil.getDayOfWeek(previousScheduleStation.getDayOfWeek()));

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(scheduleStationDTO.getStation().getName())) {
								newContent.append(scheduleStationDTO.getStation().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(StringUtil.getDayOfWeek(scheduleStationDTO.getDayOfWeek()));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
					}
					else if (StringUtil.isNotNull(scheduleStationDTO.getLookupCode())) {
						if (previousScheduleStation == null) {
							for (ScheduleStationDTO scheduleStation : stationDTOList) {
								if (scheduleStation.getOverrideList() != null) {
									for (ScheduleStationDTO overrideScheduleStation : scheduleStation.getOverrideList()) {
										if (overrideScheduleStation.getCode().equals(scheduleStationDTO.getCode())) {
											previousScheduleStation = overrideScheduleStation;
											previousScheduleStation.setStation(stationService.getStation(previousScheduleStation.getStation()));
											break;
										}
									}
									if (previousScheduleStation != null) {
										break;
									}
								}
							}
						}

						if (previousScheduleStation == null || previousScheduleStation.getMinitues() != scheduleStationDTO.getMinitues()) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStation == null || previousScheduleStation.getMinitues() == Numeric.ZERO_INT ? "Station Minutes override has been created" : "Station Minutes override has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStation != null && previousScheduleStation.getStation() != null) {
								oldContent.append(previousScheduleStation.getStation().getName());
								oldContent.append(Text.SINGLE_SPACE);
								oldContent.append(getTime(previousScheduleStation.getMinitues()));
							}
							else if (previousScheduleStation == null || previousScheduleStation.getStation() == null) {
								oldContent.append(Text.HYPHEN);
							}

							StringBuilder newContent = new StringBuilder();
							newContent.append(scheduleStationDTO.getStation().getName());
							newContent.append(Text.SINGLE_SPACE);
							newContent.append(getTime(scheduleStationDTO.getMinitues()));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Active From
						if ((previousScheduleStation == null || StringUtil.isNull(previousScheduleStation.getActiveFrom())) && StringUtil.isNotNull(scheduleStationDTO.getActiveFrom())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append("Station ActiveFrom override has been created");

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(scheduleStationDTO.getStation().getName())) {
								newContent.append(scheduleStationDTO.getStation().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(new DateTime(scheduleStationDTO.getActiveFrom()).format("DD/MM/YYYY"));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						else if (previousScheduleStation != null && StringUtil.isNotNull(previousScheduleStation.getActiveFrom()) && StringUtil.isNotNull(scheduleStationDTO.getActiveFrom()) && !previousScheduleStation.getActiveFrom().equals(scheduleStationDTO.getActiveFrom())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append("Station ActiveFrom override has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(previousScheduleStation.getStation().getName())) {
								oldContent.append(previousScheduleStation.getStation().getName());
								oldContent.append(Text.COMMA);
							}
							oldContent.append(new DateTime(previousScheduleStation.getActiveFrom()).format("DD/MM/YYYY"));

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(scheduleStationDTO.getStation().getName())) {
								newContent.append(scheduleStationDTO.getStation().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(new DateTime(scheduleStationDTO.getActiveFrom()).format("DD/MM/YYYY"));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Active To
						if ((previousScheduleStation == null || StringUtil.isNull(previousScheduleStation.getActiveTo())) && StringUtil.isNotNull(scheduleStationDTO.getActiveTo())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append("Station ActiveTo override has been created");

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(scheduleStationDTO.getStation().getName())) {
								newContent.append(scheduleStationDTO.getStation().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(new DateTime(scheduleStationDTO.getActiveTo()).format("DD/MM/YYYY"));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						else if (previousScheduleStation != null && StringUtil.isNotNull(previousScheduleStation.getActiveTo()) && StringUtil.isNotNull(scheduleStationDTO.getActiveTo()) && !previousScheduleStation.getActiveTo().equals(scheduleStationDTO.getActiveTo())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append("Station ActiveTo override has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(previousScheduleStation.getStation().getName())) {
								oldContent.append(previousScheduleStation.getStation().getName());
								oldContent.append(Text.COMMA);
							}
							oldContent.append(new DateTime(previousScheduleStation.getActiveTo()).format("DD/MM/YYYY"));

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(scheduleStationDTO.getStation().getName())) {
								newContent.append(scheduleStationDTO.getStation().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(new DateTime(scheduleStationDTO.getActiveTo()).format("DD/MM/YYYY"));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Day Of Week
						if ((previousScheduleStation == null || StringUtil.isNull(previousScheduleStation.getDayOfWeek())) && StringUtil.isNotNull(scheduleStationDTO.getDayOfWeek())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append("Station DayOfWeek override has been created");

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(scheduleStationDTO.getStation().getName())) {
								newContent.append(scheduleStationDTO.getStation().getName());
								newContent.append(Text.HYPHEN);
							}
							newContent.append(StringUtil.getDayOfWeek(scheduleStationDTO.getDayOfWeek()));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						else if (previousScheduleStation != null && StringUtil.isNotNull(previousScheduleStation.getDayOfWeek()) && StringUtil.isNotNull(scheduleStationDTO.getDayOfWeek()) && !previousScheduleStation.getDayOfWeek().equals(scheduleStationDTO.getDayOfWeek())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append("Station DayOfWeek override has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(previousScheduleStation.getStation().getName())) {
								oldContent.append(previousScheduleStation.getStation().getName());
								oldContent.append(Text.HYPHEN);
							}
							oldContent.append(StringUtil.getDayOfWeek(previousScheduleStation.getDayOfWeek()));

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationDTO != null && scheduleStationDTO.getStation() != null && StringUtil.isNotNull(scheduleStationDTO.getStation().getName())) {
								newContent.append(scheduleStationDTO.getStation().getName());
								newContent.append(Text.HYPHEN);
							}
							newContent.append(StringUtil.getDayOfWeek(scheduleStationDTO.getDayOfWeek()));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
					}
				}
			}
			// store Cache
			if (!scheduleActivityList.isEmpty()) {
				putScheduleActivityCache(scheduleStationDTO.getSchedule(), scheduleActivityList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void scheduleStationPointActivity(AuthDTO authDTO, ScheduleStationPointDTO scheduleStationPointDTO) {
		try {

			// Schedule Activity Log
			List<Map<String, String>> scheduleActivityList = new ArrayList<>();
			scheduleStationPointDTO.setStation(stationService.getStation(scheduleStationPointDTO.getStation()));
			if (scheduleStationPointDTO.getStationPoint() != null && scheduleStationPointDTO.getStationPoint().getId() != 0) {
				scheduleStationPointDTO.setStationPoint(getStationPointDTObyId(authDTO, scheduleStationPointDTO.getStationPoint()));
			}
			else if (scheduleStationPointDTO.getStationPoint() != null && StringUtil.isNotNull(scheduleStationPointDTO.getStationPoint().getCode())) {
				scheduleStationPointDTO.setStationPoint(getStationPointDTO(authDTO, scheduleStationPointDTO.getStationPoint()));
			}

			// Previous Schedule
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleStationPointDTO.getSchedule().getCode());
			getScheduleDTO(authDTO, scheduleDTO);

			String header = generateHeader(authDTO, scheduleDTO);

			// New Schedule Name
			if (StringUtil.isNull(scheduleStationPointDTO.getCode())) {
				StringBuilder keyword = new StringBuilder();
				keyword.append(StringUtil.isNull(scheduleStationPointDTO.getLookupCode()) ? "Station Point has been created" : "Station Point exception/override has been created");

				StringBuilder newContent = new StringBuilder();
				newContent.append(scheduleStationPointDTO.getStationPoint().getName());
				newContent.append(Text.SINGLE_SPACE);
				newContent.append(scheduleStationPointDTO.getMinitues());
				newContent.append(" Minutes");

				Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
				scheduleActivityList.add(scheduleActivityMap);
			}
			else if (StringUtil.isNotNull(scheduleStationPointDTO.getCode())) {

				List<ScheduleStationPointDTO> stationPointList = getScheduleStationPointDTO(authDTO, scheduleDTO);
				ScheduleStationPointDTO previousScheduleStationPoint = null;
				for (ScheduleStationPointDTO scheduleStationPoint : stationPointList) {
					if (scheduleStationPoint.getCode().equals(scheduleStationPointDTO.getCode())) {
						previousScheduleStationPoint = scheduleStationPoint;
						previousScheduleStationPoint.setStation(stationService.getStation(previousScheduleStationPoint.getStation()));
						previousScheduleStationPoint.setStationPoint(getStationPointDTObyId(authDTO, previousScheduleStationPoint.getStationPoint()));
						break;
					}
				}

				// Boarding Reporting Minutes
				if (StringUtil.isNull(scheduleStationPointDTO.getLookupCode())) {
					if (scheduleStationPointDTO.getActiveFlag() != 1) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Station point has been deleted");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(scheduleStationPointDTO.getStationPoint().getName());
						oldContent.append(Text.SINGLE_SPACE);
						oldContent.append(scheduleStationPointDTO.getMinitues());
						oldContent.append(" Minutes");

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), Text.HYPHEN);
						scheduleActivityList.add(scheduleActivityMap);
					}
					else if (scheduleStationPointDTO.getActiveFlag() == 1) {
						if (previousScheduleStationPoint == null || previousScheduleStationPoint.getMinitues() != scheduleStationPointDTO.getMinitues()) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStationPoint == null || previousScheduleStationPoint.getMinitues() == Numeric.ZERO_INT ? "Station Point Minutes has been created" : "Station Point Minutes has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStationPoint != null && previousScheduleStationPoint.getStationPoint() != null) {
								oldContent.append(scheduleStationPointDTO.getStationPoint().getName());
								oldContent.append(Text.SINGLE_SPACE);
								oldContent.append(previousScheduleStationPoint.getMinitues());
								oldContent.append(" Minutes");
							}
							else if (previousScheduleStationPoint == null || previousScheduleStationPoint.getStationPoint() == null) {
								oldContent.append(Text.HYPHEN);
							}

							StringBuilder newContent = new StringBuilder();
							newContent.append(scheduleStationPointDTO.getStationPoint().getName());
							newContent.append(Text.SINGLE_SPACE);
							newContent.append(scheduleStationPointDTO.getMinitues());
							newContent.append(" Minutes");

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Boarding Flag
						if (previousScheduleStationPoint == null || previousScheduleStationPoint.getBoardingFlag() != scheduleStationPointDTO.getBoardingFlag()) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStationPoint == null || previousScheduleStationPoint.getBoardingFlag() == Numeric.ZERO_INT ? "Station Point Boarding Flag has been created" : "Station Point Boarding Flag has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStationPoint != null && previousScheduleStationPoint.getStationPoint() != null) {
								oldContent.append(scheduleStationPointDTO.getStationPoint().getName());
								oldContent.append(" boarding ");
								oldContent.append(previousScheduleStationPoint.getBoardingFlag() == Numeric.ZERO_INT ? "disabled" : "enabled");
							}
							else if (previousScheduleStationPoint == null || previousScheduleStationPoint.getStationPoint() == null) {
								oldContent.append(Text.HYPHEN);
							}
							StringBuilder newContent = new StringBuilder();
							newContent.append(scheduleStationPointDTO.getStationPoint().getName());
							newContent.append(" boarding ");
							newContent.append(scheduleStationPointDTO.getBoardingFlag() == Numeric.ZERO_INT ? "disabled" : "enabled");

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Dropping Flag
						if (previousScheduleStationPoint == null || previousScheduleStationPoint.getDroppingFlag() != scheduleStationPointDTO.getDroppingFlag()) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStationPoint == null || previousScheduleStationPoint.getDroppingFlag() == Numeric.ZERO_INT ? "Station Point Dropping Flag has been created" : "Station Point Dropping Flag has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStationPoint != null && previousScheduleStationPoint.getStationPoint() != null) {
								oldContent.append(scheduleStationPointDTO.getStationPoint().getName());
								oldContent.append(" dropping ");
								oldContent.append(previousScheduleStationPoint.getDroppingFlag() == Numeric.ZERO_INT ? "disabled" : "enabled");
							}
							else if (previousScheduleStationPoint == null || previousScheduleStationPoint.getStationPoint() == null) {
								oldContent.append(Text.HYPHEN);
							}

							StringBuilder newContent = new StringBuilder();
							newContent.append(scheduleStationPointDTO.getStationPoint().getName());
							newContent.append(" dropping ");
							newContent.append(scheduleStationPointDTO.getDroppingFlag() == Numeric.ZERO_INT ? "disabled" : "enabled");

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Active From
						if ((previousScheduleStationPoint == null || StringUtil.isNull(previousScheduleStationPoint.getActiveFrom())) || (StringUtil.isNotNull(previousScheduleStationPoint.getActiveFrom()) && StringUtil.isNotNull(scheduleStationPointDTO.getActiveFrom()) && !previousScheduleStationPoint.getActiveFrom().equals(scheduleStationPointDTO.getActiveFrom()))) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStationPoint == null || StringUtil.isNull(previousScheduleStationPoint.getActiveFrom()) ? "Station Point ActiveFrom has been created" : "Station Point ActiveFrom has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStationPoint != null && previousScheduleStationPoint.getStationPoint() != null && StringUtil.isNotNull(previousScheduleStationPoint.getStationPoint().getName())) {
								oldContent.append(previousScheduleStationPoint.getStationPoint().getName());
								oldContent.append(Text.COMMA);
							}
							oldContent.append(previousScheduleStationPoint != null && StringUtil.isNotNull(previousScheduleStationPoint.getActiveFrom()) ? new DateTime(previousScheduleStationPoint.getActiveFrom()).format("DD/MM/YYYY") : Text.HYPHEN);

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationPointDTO != null && scheduleStationPointDTO.getStationPoint() != null && StringUtil.isNotNull(scheduleStationPointDTO.getStationPoint().getName())) {
								newContent.append(previousScheduleStationPoint.getStationPoint().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(new DateTime(scheduleStationPointDTO.getActiveFrom()).format("DD/MM/YYYY"));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Active To
						if ((previousScheduleStationPoint == null || StringUtil.isNull(previousScheduleStationPoint.getActiveTo())) || (StringUtil.isNotNull(previousScheduleStationPoint.getActiveTo()) && StringUtil.isNotNull(scheduleStationPointDTO.getActiveTo()) && !previousScheduleStationPoint.getActiveTo().equals(scheduleStationPointDTO.getActiveTo()))) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStationPoint == null || StringUtil.isNull(previousScheduleStationPoint.getActiveTo()) ? "Station Point ActiveTo has been created" : "Station Point ActiveTo has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStationPoint != null && previousScheduleStationPoint.getStationPoint() != null && StringUtil.isNotNull(previousScheduleStationPoint.getStationPoint().getName())) {
								oldContent.append(previousScheduleStationPoint.getStationPoint().getName());
								oldContent.append(Text.COMMA);
							}
							oldContent.append(previousScheduleStationPoint != null && StringUtil.isNotNull(previousScheduleStationPoint.getActiveTo()) ? new DateTime(previousScheduleStationPoint.getActiveTo()).format("DD/MM/YYYY") : Text.HYPHEN);

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationPointDTO != null && scheduleStationPointDTO.getStationPoint() != null && StringUtil.isNotNull(scheduleStationPointDTO.getStationPoint().getName())) {
								newContent.append(scheduleStationPointDTO.getStationPoint().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(new DateTime(scheduleStationPointDTO.getActiveTo()).format("DD/MM/YYYY"));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Day Of Week
						if ((previousScheduleStationPoint == null || StringUtil.isNull(previousScheduleStationPoint.getDayOfWeek())) || (StringUtil.isNotNull(previousScheduleStationPoint.getDayOfWeek()) && StringUtil.isNotNull(scheduleStationPointDTO.getDayOfWeek()) && !previousScheduleStationPoint.getDayOfWeek().equals(scheduleStationPointDTO.getDayOfWeek()))) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStationPoint == null || StringUtil.isNull(previousScheduleStationPoint.getDayOfWeek()) ? "Station Point DayOfWeek has been created" : "Station Point DayOfWeek has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStationPoint != null && previousScheduleStationPoint.getStationPoint() != null && StringUtil.isNotNull(previousScheduleStationPoint.getStationPoint().getName())) {
								oldContent.append(previousScheduleStationPoint.getStationPoint().getName());
								oldContent.append(Text.HYPHEN);
							}
							oldContent.append(previousScheduleStationPoint != null && StringUtil.isNotNull(previousScheduleStationPoint.getDayOfWeek()) ? StringUtil.getDayOfWeek(previousScheduleStationPoint.getDayOfWeek()) : Text.HYPHEN);

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationPointDTO != null && scheduleStationPointDTO.getStationPoint() != null && StringUtil.isNotNull(scheduleStationPointDTO.getStationPoint().getName())) {
								newContent.append(scheduleStationPointDTO.getStationPoint().getName());
								newContent.append(Text.HYPHEN);
							}
							newContent.append(StringUtil.getDayOfWeek(scheduleStationPointDTO.getDayOfWeek()));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
					}
				}
				else if (StringUtil.isNotNull(scheduleStationPointDTO.getLookupCode())) {
					if (scheduleStationPointDTO.getActiveFlag() != 1) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Station point override/exception has been deleted");

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleStationPointDTO.getStationPoint().getName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);

					}
					else if (scheduleStationPointDTO.getActiveFlag() == 1) {
						if (previousScheduleStationPoint == null) {
							for (ScheduleStationPointDTO scheduleStationPoint : stationPointList) {
								if (scheduleStationPoint.getOverrideList() != null) {
									for (ScheduleStationPointDTO overrideScheduleStationPoint : scheduleStationPoint.getOverrideList()) {
										if (overrideScheduleStationPoint.getCode().equals(scheduleStationPointDTO.getCode())) {
											previousScheduleStationPoint = overrideScheduleStationPoint;
											previousScheduleStationPoint.setStation(stationService.getStation(previousScheduleStationPoint.getStation()));
											previousScheduleStationPoint.setStationPoint(getStationPointDTObyId(authDTO, previousScheduleStationPoint.getStationPoint()));
											break;
										}
									}
								}
							}
						}
						if (previousScheduleStationPoint == null || previousScheduleStationPoint.getMinitues() != scheduleStationPointDTO.getMinitues()) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStationPoint == null || previousScheduleStationPoint.getMinitues() == Numeric.ZERO_INT ? "Station Point Minutes override has been created" : "Station Point Minutes override has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStationPoint != null && previousScheduleStationPoint.getStationPoint() != null) {
								oldContent.append(previousScheduleStationPoint.getStationPoint().getName());
								oldContent.append(Text.SINGLE_SPACE);
								oldContent.append(previousScheduleStationPoint.getMinitues());
								oldContent.append(" Minutes");
							}
							else if (previousScheduleStationPoint == null || previousScheduleStationPoint.getStationPoint() == null) {
								oldContent.append(Text.HYPHEN);
							}

							StringBuilder newContent = new StringBuilder();
							newContent.append(scheduleStationPointDTO.getStationPoint().getName());
							newContent.append(Text.SINGLE_SPACE);
							newContent.append(scheduleStationPointDTO.getMinitues());
							newContent.append(" Minutes");

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Active From
						if ((previousScheduleStationPoint == null || StringUtil.isNull(previousScheduleStationPoint.getActiveFrom())) || (StringUtil.isNotNull(previousScheduleStationPoint.getActiveFrom()) && StringUtil.isNotNull(scheduleStationPointDTO.getActiveFrom()) && !previousScheduleStationPoint.getActiveFrom().equals(scheduleStationPointDTO.getActiveFrom()))) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStationPoint == null || StringUtil.isNull(previousScheduleStationPoint.getActiveFrom()) ? "Station Point ActiveFrom override has been created" : "Station Point ActiveFrom override has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStationPoint != null && previousScheduleStationPoint.getStationPoint() != null && StringUtil.isNotNull(previousScheduleStationPoint.getStationPoint().getName())) {
								oldContent.append(previousScheduleStationPoint.getStationPoint().getName());
								oldContent.append(Text.COMMA);
							}
							oldContent.append(previousScheduleStationPoint != null && StringUtil.isNotNull(previousScheduleStationPoint.getActiveFrom()) ? new DateTime(previousScheduleStationPoint.getActiveFrom()).format("DD/MM/YYYY") : Text.HYPHEN);

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationPointDTO != null && scheduleStationPointDTO.getStationPoint() != null && StringUtil.isNotNull(scheduleStationPointDTO.getStationPoint().getName())) {
								newContent.append(scheduleStationPointDTO.getStationPoint().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(new DateTime(scheduleStationPointDTO.getActiveFrom()).format("DD/MM/YYYY"));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Active To
						if ((previousScheduleStationPoint == null || StringUtil.isNull(previousScheduleStationPoint.getActiveTo())) || (StringUtil.isNotNull(previousScheduleStationPoint.getActiveTo()) && StringUtil.isNotNull(scheduleStationPointDTO.getActiveTo()) && !previousScheduleStationPoint.getActiveTo().equals(scheduleStationPointDTO.getActiveTo()))) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStationPoint == null || StringUtil.isNull(previousScheduleStationPoint.getActiveTo()) ? "Station Point ActiveTo override has been created" : "Station Point ActiveTo override has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStationPoint != null && previousScheduleStationPoint.getStationPoint() != null && StringUtil.isNotNull(previousScheduleStationPoint.getStationPoint().getName())) {
								oldContent.append(previousScheduleStationPoint.getStationPoint().getName());
								oldContent.append(Text.COMMA);
							}
							oldContent.append(previousScheduleStationPoint != null && StringUtil.isNotNull(previousScheduleStationPoint.getActiveTo()) ? new DateTime(previousScheduleStationPoint.getActiveTo()).format("DD/MM/YYYY") : Text.HYPHEN);

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationPointDTO != null && scheduleStationPointDTO.getStationPoint() != null && StringUtil.isNotNull(scheduleStationPointDTO.getStationPoint().getName())) {
								newContent.append(scheduleStationPointDTO.getStationPoint().getName());
								newContent.append(Text.COMMA);
							}
							newContent.append(new DateTime(scheduleStationPointDTO.getActiveTo()).format("DD/MM/YYYY"));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Day Of Week
						if ((previousScheduleStationPoint == null || StringUtil.isNull(previousScheduleStationPoint.getDayOfWeek())) || (StringUtil.isNotNull(previousScheduleStationPoint.getDayOfWeek()) && StringUtil.isNotNull(scheduleStationPointDTO.getDayOfWeek()) && !previousScheduleStationPoint.getDayOfWeek().equals(scheduleStationPointDTO.getDayOfWeek()))) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStationPoint == null || StringUtil.isNull(previousScheduleStationPoint.getDayOfWeek()) ? "Station Point DayOfWeek override has been created" : "Station Point DayOfWeek override has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStationPoint != null && previousScheduleStationPoint.getStationPoint() != null && StringUtil.isNotNull(previousScheduleStationPoint.getStationPoint().getName())) {
								oldContent.append(previousScheduleStationPoint.getStationPoint().getName());
								oldContent.append(Text.HYPHEN);
							}
							oldContent.append(previousScheduleStationPoint != null && StringUtil.isNotNull(previousScheduleStationPoint.getDayOfWeek()) ? StringUtil.getDayOfWeek(previousScheduleStationPoint.getDayOfWeek()) : Text.HYPHEN);

							StringBuilder newContent = new StringBuilder();
							if (scheduleStationPointDTO != null && scheduleStationPointDTO.getStationPoint() != null && StringUtil.isNotNull(scheduleStationPointDTO.getStationPoint().getName())) {
								newContent.append(scheduleStationPointDTO.getStationPoint().getName());
								newContent.append(Text.HYPHEN);
							}
							newContent.append(StringUtil.getDayOfWeek(scheduleStationPointDTO.getDayOfWeek()));

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Boarding Flag
						if (previousScheduleStationPoint == null || (previousScheduleStationPoint != null && scheduleStationPointDTO != null && previousScheduleStationPoint.getBoardingFlag() != scheduleStationPointDTO.getBoardingFlag())) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStationPoint == null || previousScheduleStationPoint.getBoardingFlag() == Numeric.ZERO_INT ? "Station Point Boarding Flag override has been created" : "Station Point Boarding Flag override has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStationPoint != null && previousScheduleStationPoint.getStationPoint() != null) {
								oldContent.append(scheduleStationPointDTO.getStationPoint().getName());
								oldContent.append(" boarding ");
								oldContent.append(previousScheduleStationPoint.getBoardingFlag() == Numeric.ZERO_INT ? "disabled" : "enabled");
							}
							else if (previousScheduleStationPoint == null || previousScheduleStationPoint.getStationPoint() == null) {
								oldContent.append(Text.HYPHEN);
							}

							StringBuilder newContent = new StringBuilder();
							newContent.append(scheduleStationPointDTO.getStationPoint().getName());
							newContent.append(" boarding ");
							newContent.append(scheduleStationPointDTO.getBoardingFlag() == Numeric.ZERO_INT ? "disabled" : "enabled");

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
						// Dropping Flag
						if (previousScheduleStationPoint == null || previousScheduleStationPoint.getDroppingFlag() != scheduleStationPointDTO.getDroppingFlag()) {
							StringBuilder keyword = new StringBuilder();
							keyword.append(previousScheduleStationPoint == null || previousScheduleStationPoint.getDroppingFlag() == Numeric.ZERO_INT ? "Station Point Dropping Flag override has been created" : "Station Point Dropping Flag override has been changed");

							StringBuilder oldContent = new StringBuilder();
							if (previousScheduleStationPoint != null && previousScheduleStationPoint.getStationPoint() != null) {
								oldContent.append(scheduleStationPointDTO.getStationPoint().getName());
								oldContent.append(" dropping ");
								oldContent.append(previousScheduleStationPoint.getDroppingFlag() == Numeric.ZERO_INT ? "disabled" : "enabled");
							}
							else if (previousScheduleStationPoint == null || previousScheduleStationPoint.getStationPoint() == null) {
								oldContent.append(Text.HYPHEN);
							}

							StringBuilder newContent = new StringBuilder();
							newContent.append(scheduleStationPointDTO.getStationPoint().getName());
							newContent.append(" dropping ");
							newContent.append(scheduleStationPointDTO.getDroppingFlag() == Numeric.ZERO_INT ? "disabled" : "enabled");

							Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
							scheduleActivityList.add(scheduleActivityMap);
						}
					}
				}
			}
			// store Cache
			if (!scheduleActivityList.isEmpty()) {
				putScheduleActivityCache(scheduleStationPointDTO.getSchedule(), scheduleActivityList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void scheduleBusActivity(AuthDTO authDTO, ScheduleBusDTO scheduleBusDTO) {
		try {

			// Schedule Activity Log
			List<Map<String, String>> scheduleActivityList = new ArrayList<>();
			// Previous Schedule
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleBusDTO.getSchedule().getCode());
			getScheduleDTO(authDTO, scheduleDTO);
			getBusDTO(authDTO, scheduleBusDTO.getBus());
			String busType = busService.getBusCategoryByCode(scheduleBusDTO.getBus().getCategoryCode());
			if (scheduleBusDTO.getTax() != null && scheduleBusDTO.getTax().getId() > 0) {
				scheduleBusDTO.setTax(getNamespaceTaxbyId(authDTO, scheduleBusDTO.getTax()));
			}
			else if (scheduleBusDTO.getTax() != null && StringUtil.isNotNull(scheduleBusDTO.getTax().getCode())) {
				scheduleBusDTO.setTax(getNamespaceTaxDTO(authDTO, scheduleBusDTO.getTax()));
			}
			String header = generateHeader(authDTO, scheduleDTO);

			// New Schedule Name
			if (StringUtil.isNull(scheduleBusDTO.getCode())) {
				StringBuilder keyword = new StringBuilder();
				keyword.append("Schedule bus has been created");

				StringBuilder newContent = new StringBuilder();
				newContent.append(scheduleBusDTO.getBus().getName());

				if (scheduleBusDTO.getTax() != null) {
					newContent.append(StringUtil.isNotNull(scheduleBusDTO.getTax().getName()) ? "Tax: " + scheduleBusDTO.getTax().getName() : Text.EMPTY);
				}
				newContent.append("Distance: " + scheduleBusDTO.getDistance());

				if (scheduleBusDTO.getBreakevenSettings() != null) {
					newContent.append(StringUtil.isNotNull(scheduleBusDTO.getBreakevenSettings().getName()) ? "Breakeven:" + scheduleBusDTO.getBreakevenSettings().getName() : Text.EMPTY);
				}

				Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
				scheduleActivityList.add(scheduleActivityMap);
			}
			else if (StringUtil.isNotNull(scheduleBusDTO.getCode())) {
				ScheduleBusDTO previousScheduleBus = getScheduleBusDTO(authDTO, scheduleDTO);
				previousScheduleBus.setBus(getBusDTObyId(authDTO, previousScheduleBus.getBus()));
				String previousBusType = busService.getBusCategoryByCode(previousScheduleBus.getBus().getCategoryCode());

				if (previousScheduleBus != null && previousScheduleBus.getTax() != null && previousScheduleBus.getTax().getId() > 0) {
					previousScheduleBus.setTax(getNamespaceTaxbyId(authDTO, previousScheduleBus.getTax()));
				}
				else if (previousScheduleBus != null && previousScheduleBus.getTax() != null && StringUtil.isNotNull(previousScheduleBus.getTax().getCode())) {
					previousScheduleBus.setTax(getNamespaceTaxDTO(authDTO, previousScheduleBus.getTax()));
				}

				if (previousScheduleBus.getBreakevenSettings() != null && previousScheduleBus.getBreakevenSettings().getId() != 0) {
					busBreakevenService.getBreakeven(authDTO, previousScheduleBus.getBreakevenSettings());
				}

				if (scheduleBusDTO.getBus() != null && previousScheduleBus.getBus() != null && !previousScheduleBus.getBus().getCode().equals(scheduleBusDTO.getBus().getCode())) {
					StringBuilder keyword = new StringBuilder();
					keyword.append("Schedule bus has been changed");

					StringBuilder oldContent = new StringBuilder();
					oldContent.append(previousBusType);

					StringBuilder newContent = new StringBuilder();
					newContent.append(busType);

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
					scheduleActivityList.add(scheduleActivityMap);
				}
				// Amenities
				if (previousScheduleBus.getAmentiesList() != null && !previousScheduleBus.getAmentiesList().isEmpty() && scheduleBusDTO.getAmentiesList() != null && !scheduleBusDTO.getAmentiesList().isEmpty()) {
					StringBuilder keyword = new StringBuilder();
					keyword.append("Schedule bus amenities has been changed");

					StringBuilder oldContent = new StringBuilder();
					Map<String, AmenitiesDTO> validateMap = new HashMap<>();
					for (AmenitiesDTO previousAmenities : previousScheduleBus.getAmentiesList()) {
						oldContent.append(getAmenitiesDTO(previousAmenities.getCode()).getName());
						oldContent.append(Text.COMMA);
						validateMap.put(previousAmenities.getCode(), previousAmenities);
					}

					boolean monitorRequired = Text.FALSE;
					StringBuilder newContent = new StringBuilder();
					for (AmenitiesDTO amenities : scheduleBusDTO.getAmentiesList()) {
						newContent.append(getAmenitiesDTO(amenities.getCode()).getName());
						newContent.append(Text.COMMA);
						if (validateMap.get(amenities.getCode()) == null) {
							monitorRequired = Text.TRUE;
						}
					}

					if (monitorRequired) {
						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
				}
				else if ((previousScheduleBus.getAmentiesList() == null || previousScheduleBus.getAmentiesList().isEmpty()) && !scheduleBusDTO.getAmentiesList().isEmpty()) {
					StringBuilder keyword = new StringBuilder();
					keyword.append("Schedule bus amenities has been created");

					StringBuilder newContent = new StringBuilder();
					for (AmenitiesDTO amenities : scheduleBusDTO.getAmentiesList()) {
						newContent.append(amenities.getName());
						newContent.append(Text.COMMA);
					}

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
					scheduleActivityList.add(scheduleActivityMap);
				}

				// Tax
				if ((previousScheduleBus == null || previousScheduleBus.getTax() == null || previousScheduleBus.getTax().getId() == 0) && scheduleBusDTO.getTax() != null && scheduleBusDTO.getTax().getId() > 0) {
					StringBuilder keyword = new StringBuilder();
					keyword.append("Schedule Bus Tax created");

					StringBuilder newContent = new StringBuilder();
					newContent.append(scheduleBusDTO.getTax().getName());

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
					scheduleActivityList.add(scheduleActivityMap);
				}
				else if (previousScheduleBus != null && scheduleBusDTO.getTax() != null && scheduleBusDTO.getTax().getId() != 0 && previousScheduleBus.getTax() != null && previousScheduleBus.getTax().getId() != 0 && previousScheduleBus.getTax().getId() != scheduleBusDTO.getTax().getId()) {
					StringBuilder keyword = new StringBuilder();
					keyword.append("Schedule Bus Tax changed");

					StringBuilder oldContent = new StringBuilder();
					oldContent.append(previousScheduleBus.getTax().getTradeName());

					StringBuilder newContent = new StringBuilder();
					newContent.append(scheduleBusDTO.getTax().getTradeName());

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
					scheduleActivityList.add(scheduleActivityMap);
				}
				if (previousScheduleBus != null && scheduleBusDTO.getDistance() != 0 && previousScheduleBus.getDistance() == 0) {
					StringBuilder keyword = new StringBuilder();
					keyword.append("Schedule Bus Distance created");

					StringBuilder newContent = new StringBuilder();
					newContent.append(scheduleBusDTO.getDistance());

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
					scheduleActivityList.add(scheduleActivityMap);
				}
				else if (previousScheduleBus != null && previousScheduleBus.getDistance() != 0 && scheduleBusDTO.getDistance() != previousScheduleBus.getDistance()) {
					StringBuilder keyword = new StringBuilder();
					keyword.append("Schedule Bus Distance changed");

					StringBuilder oldContent = new StringBuilder();
					oldContent.append(previousScheduleBus.getDistance());

					StringBuilder newContent = new StringBuilder();
					newContent.append(scheduleBusDTO.getDistance());

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
					scheduleActivityList.add(scheduleActivityMap);
				}
				// Breakeven
				if (previousScheduleBus != null && scheduleBusDTO.getBreakevenSettings() != null && scheduleBusDTO.getBreakevenSettings().getId() != 0 && previousScheduleBus.getBreakevenSettings().getId() == 0) {
					StringBuilder keyword = new StringBuilder();
					keyword.append("Schedule Bus breakeven created");

					StringBuilder newContent = new StringBuilder();
					newContent.append(scheduleBusDTO.getBreakevenSettings().getName());

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
					scheduleActivityList.add(scheduleActivityMap);
				}
				else if (previousScheduleBus != null && scheduleBusDTO.getBreakevenSettings() != null && scheduleBusDTO.getBreakevenSettings().getId() != 0 && previousScheduleBus.getBreakevenSettings().getId() != 0 && scheduleBusDTO.getBreakevenSettings().getId() != previousScheduleBus.getBreakevenSettings().getId()) {
					StringBuilder keyword = new StringBuilder();
					keyword.append("Schedule Bus breakeven changed");

					StringBuilder oldContent = new StringBuilder();
					oldContent.append(previousScheduleBus.getBreakevenSettings().getName());

					StringBuilder newContent = new StringBuilder();
					newContent.append(scheduleBusDTO.getBreakevenSettings().getName());

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
					scheduleActivityList.add(scheduleActivityMap);
				}
			}
			// store Cache
			if (!scheduleActivityList.isEmpty()) {
				putScheduleActivityCache(scheduleBusDTO.getSchedule(), scheduleActivityList);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void scheduleStageActivity(AuthDTO authDTO, ScheduleStageDTO scheduleStageDTO) {
		try {

			// Schedule Activity Log
			List<Map<String, String>> scheduleActivityList = new ArrayList<>();
			scheduleStageDTO.setFromStation(stationService.getStation(scheduleStageDTO.getFromStation()));
			scheduleStageDTO.setToStation(stationService.getStation(scheduleStageDTO.getToStation()));

			// Previous Schedule
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleStageDTO.getSchedule().getCode());
			getScheduleDTO(authDTO, scheduleDTO);

			String header = generateHeader(authDTO, scheduleDTO);

			// New Schedule Name
			if (StringUtil.isNull(scheduleStageDTO.getCode())) {
				StringBuilder keyword = new StringBuilder();
				keyword.append(StringUtil.isNull(scheduleStageDTO.getLookupCode()) ? "Route has been created" : "Route override has been created");
				if (scheduleStageDTO.getGroup() != null && StringUtil.isNotNull(scheduleStageDTO.getGroup().getCode())) {
					scheduleStageDTO.setGroup(groupService.getGroup(authDTO, scheduleStageDTO.getGroup()));
					keyword.append(Text.SINGLE_SPACE).append("for group").append(Text.SINGLE_SPACE).append(scheduleStageDTO.getGroup().getName());
				}

				StringBuilder newContent = new StringBuilder();
				newContent.append(scheduleStageDTO.getFromStation().getName());
				newContent.append(Text.HYPHEN);
				newContent.append(scheduleStageDTO.getToStation().getName());
				newContent.append("(");
				if (scheduleStageDTO.getBusSeatTypeFare() != null) {
					for (BusSeatTypeFareDTO busSeatTypeFare : scheduleStageDTO.getBusSeatTypeFare()) {
						newContent.append(busSeatTypeFare.getBusSeatType().getName());
						newContent.append(Text.COLON);
						newContent.append(busSeatTypeFare.getFare());
						newContent.append(Text.COMMA);
					}
				}
				else if (scheduleStageDTO.getFare() != 0) {
					newContent.append(scheduleStageDTO.getBusSeatType().getName());
					newContent.append(Text.COMMA);
					newContent.append(Text.SINGLE_SPACE);
					newContent.append(scheduleStageDTO.getFare());
				}
				
				
				newContent.append(")");

				Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
				scheduleActivityList.add(scheduleActivityMap);
			}
			else if (StringUtil.isNotNull(scheduleStageDTO.getCode())) {
				if (scheduleStageDTO.getActiveFlag() != 1) {
					StringBuilder keyword = new StringBuilder();
					keyword.append("Route has been deleted");

					StringBuilder oldContent = new StringBuilder();
					oldContent.append(scheduleStageDTO.getFromStation().getName());
					oldContent.append(Text.HYPHEN);
					oldContent.append(scheduleStageDTO.getToStation().getName());
					oldContent.append(Text.SINGLE_SPACE);
					if (scheduleStageDTO.getBusSeatTypeFare() != null && !scheduleStageDTO.getBusSeatTypeFare().isEmpty()) {
						for (BusSeatTypeFareDTO seatTypeFareDTO : scheduleStageDTO.getBusSeatTypeFare()) {
							oldContent.append(seatTypeFareDTO.getBusSeatType().getName());
							oldContent.append(Text.COLON);
							oldContent.append(seatTypeFareDTO.getFare());
							oldContent.append(Text.COMMA);
						}
					}
					else if (scheduleStageDTO.getFare() != 0) {
						oldContent.append(scheduleStageDTO.getBusSeatType().getName());
						oldContent.append(Text.COLON);
						oldContent.append(scheduleStageDTO.getFare());
					}

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), Text.HYPHEN);
					scheduleActivityList.add(scheduleActivityMap);

				}
				else if (scheduleStageDTO.getActiveFlag() == 1) {
					List<ScheduleStageDTO> previousScheduleStageList = getScheduleStageDTO(authDTO, scheduleDTO);
					ScheduleStageDTO previousScheduleStage = null;
					for (ScheduleStageDTO scheduleStage : previousScheduleStageList) {
						if (scheduleStage != null && scheduleStageDTO != null && StringUtil.isNotNull(scheduleStage.getCode()) && StringUtil.isNotNull(scheduleStageDTO.getCode()) && scheduleStage.getCode().equals(scheduleStageDTO.getCode())) {
							previousScheduleStage = scheduleStage;
							previousScheduleStage.setFromStation(stationService.getStation(previousScheduleStage.getFromStation()));
							previousScheduleStage.setToStation(stationService.getStation(previousScheduleStage.getToStation()));
							break;
						}
					}

					// Route
					if (previousScheduleStage != null && previousScheduleStage.getFromStation() != null && scheduleStageDTO.getFromStation() != null && previousScheduleStage.getFromStation().getId() != scheduleStageDTO.getFromStation().getId()) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("From Station has been changed");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(previousScheduleStage.getFromStation().getName());

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleStageDTO.getFromStation().getName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					else if ((previousScheduleStage == null || previousScheduleStage.getFromStation() == null || previousScheduleStage.getFromStation().getId() == Numeric.ZERO_INT) && scheduleStageDTO.getFromStation() != null) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("From Station has been created");

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleStageDTO.getFromStation().getName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					if (previousScheduleStage != null && previousScheduleStage.getToStation() != null && scheduleStageDTO.getToStation() != null && previousScheduleStage.getToStation().getId() != scheduleStageDTO.getToStation().getId()) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("To Station has been changed");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(previousScheduleStage.getToStation().getName());

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleStageDTO.getToStation().getName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					else if (previousScheduleStage == null || previousScheduleStage.getToStation() == null || (scheduleStageDTO.getToStation() != null && previousScheduleStage.getToStation().getId() == Numeric.ZERO_INT)) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("To Station has been created");

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleStageDTO.getToStation().getName());

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}

					// Group
					StringBuilder oldGroupContent = null;
					StringBuilder newGroupContent = null;
					if (previousScheduleStage != null && scheduleStageDTO != null && previousScheduleStage.getGroup() == null && scheduleStageDTO.getGroup() != null) {
						newGroupContent = new StringBuilder();

						if (scheduleStageDTO.getGroup() == null || StringUtil.isNull(scheduleStageDTO.getGroup().getCode())) {
							newGroupContent.append(Text.HYPHEN);
						}
						else if (scheduleStageDTO.getGroup() != null) {
							newGroupContent.append(scheduleStageDTO.getGroup().getName());
						}
					}
					else if (previousScheduleStage != null && scheduleStageDTO != null && previousScheduleStage.getGroup() != null && scheduleStageDTO.getGroup() == null) {
						oldGroupContent = new StringBuilder();
						if (previousScheduleStage.getGroup() == null || StringUtil.isNull(previousScheduleStage.getGroup().getCode())) {
							oldGroupContent.append(Text.HYPHEN);
						}
						else if (previousScheduleStage.getGroup() != null) {
							oldGroupContent.append(previousScheduleStage.getGroup().getName());
						}
					}
					else if (previousScheduleStage != null && scheduleStageDTO != null && scheduleStageDTO.getGroup() != null && previousScheduleStage.getGroup() != null && StringUtil.isNotNull(previousScheduleStage.getGroup().getCode()) && StringUtil.isNotNull(scheduleStageDTO.getGroup().getCode()) && !previousScheduleStage.getGroup().getCode().equals(scheduleStageDTO.getGroup().getCode())) {
						oldGroupContent = new StringBuilder();
						newGroupContent = new StringBuilder();
						if (StringUtil.isNull(previousScheduleStage.getGroup().getName())) {
							oldGroupContent.append(Text.HYPHEN);
						}
						else if (StringUtil.isNotNull(previousScheduleStage.getGroup().getName())) {
							oldGroupContent.append(previousScheduleStage.getGroup().getName());
						}

						if (StringUtil.isNull(scheduleStageDTO.getGroup().getName())) {
							newGroupContent.append(Text.HYPHEN);
						}
						else if (StringUtil.isNotNull(scheduleStageDTO.getGroup().getName())) {
							newGroupContent.append(scheduleStageDTO.getGroup().getName());
						}
					}
					// Fare
					if (oldGroupContent != null || newGroupContent != null || (previousScheduleStage != null && previousScheduleStage.getFare() != scheduleStageDTO.getFare())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append(scheduleStageDTO.getFromStation().getName());
						keyword.append(Text.HYPHEN);
						keyword.append(scheduleStageDTO.getToStation().getName());
						keyword.append(" fare has been changed");

						StringBuilder oldContent = new StringBuilder();
						if (previousScheduleStage.getBusSeatType() != null) {
							oldContent.append(oldGroupContent == null ? Text.EMPTY : "Group " + oldGroupContent + Text.COMMA);
							oldContent.append(StringUtil.isNotNull(previousScheduleStage.getBusSeatType().getName()) ? previousScheduleStage.getBusSeatType().getName() + Text.COLON : Text.EMPTY);
							oldContent.append(previousScheduleStage.getFare());
						}

						StringBuilder newContent = new StringBuilder();
						if (scheduleStageDTO.getBusSeatType() != null) {
							newContent.append(newGroupContent == null ? Text.EMPTY : "Group " + newGroupContent + Text.SINGLE_SPACE);
							newContent.append(StringUtil.isNotNull(scheduleStageDTO.getBusSeatType().getName()) ? scheduleStageDTO.getBusSeatType().getName() + Text.COLON : Text.EMPTY);
							newContent.append(scheduleStageDTO.getFare());
						}

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					if (oldGroupContent != null || newGroupContent != null || (previousScheduleStage != null && previousScheduleStage.getBusSeatTypeFare() != null && !previousScheduleStage.getBusSeatTypeFare().isEmpty() && scheduleStageDTO.getBusSeatTypeFare() != null && !scheduleStageDTO.getBusSeatTypeFare().isEmpty())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append(scheduleStageDTO.getFromStation().getName());
						keyword.append(Text.HYPHEN);
						keyword.append(scheduleStageDTO.getToStation().getName());
						keyword.append(" fare has been changed");

						StringBuilder oldContent = new StringBuilder();
						oldContent.append(oldGroupContent == null ? Text.EMPTY : "Group " + oldGroupContent + Text.COMMA);
						for (BusSeatTypeFareDTO busSeatTypeFare : previousScheduleStage.getBusSeatTypeFare()) {
							oldContent.append(StringUtil.isNotNull(busSeatTypeFare.getBusSeatType().getName()) ? busSeatTypeFare.getBusSeatType().getName() + Text.COLON : Text.EMPTY);
							oldContent.append(previousScheduleStage.getFare());
							oldContent.append(Text.COMMA);
						}

						StringBuilder newContent = new StringBuilder();
						newContent.append(newGroupContent == null ? Text.EMPTY : "Group " + newGroupContent + Text.SINGLE_SPACE);
						for (BusSeatTypeFareDTO busSeatTypeFare : scheduleStageDTO.getBusSeatTypeFare()) {
							newContent.append(StringUtil.isNotNull(busSeatTypeFare.getBusSeatType().getName()) ? busSeatTypeFare.getBusSeatType().getName() + Text.COLON : Text.EMPTY);
							newContent.append(scheduleStageDTO.getFare());
							newContent.append(Text.COMMA);
						}

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
				}
			}

			// store Cache
			if (!scheduleActivityList.isEmpty()) {
				putScheduleActivityCache(scheduleStageDTO.getSchedule(), scheduleActivityList);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void scheduleCancellationTermsActivity(AuthDTO authDTO, ScheduleCancellationTermDTO cancellationTerm) {
		try {
			// Schedule Activity Log
			List<Map<String, String>> scheduleActivityList = new ArrayList<>();
			ScheduleDTO scheduleDTO = null;
			for (ScheduleCancellationTermDTO scheduleCancellationTermDTO : cancellationTerm.getList()) {
				if (scheduleCancellationTermDTO.getActiveFlag() != 1) {
					scheduleDTO = scheduleCancellationTermDTO.getSchedule();
					getScheduleDTO(authDTO, scheduleDTO);

					String header = generateHeader(authDTO, scheduleDTO);

					StringBuilder keyword = new StringBuilder();
					keyword.append(StringUtil.isNull(scheduleCancellationTermDTO.getLookupCode()) ? "Cancellation Policy deleted" : "Cancellation Policy Override deleted");

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, Text.HYPHEN);
					scheduleActivityList.add(scheduleActivityMap);
				}
				else if (scheduleCancellationTermDTO.getActiveFlag() == 1) {
					CancellationTermsCache cancellationTermsCache = new CancellationTermsCache();
					scheduleCancellationTermDTO.setCancellationTerm(cancellationTermsCache.getCancellationTermDTO(authDTO, scheduleCancellationTermDTO.getCancellationTerm()));
					getScheduleDTO(authDTO, scheduleCancellationTermDTO.getSchedule());
					if (scheduleDTO == null) {
						scheduleDTO = scheduleCancellationTermDTO.getSchedule();
					}
					if (scheduleCancellationTermDTO.getGroup() != null && StringUtil.isNotNull(scheduleCancellationTermDTO.getGroup().getCode())) {
						scheduleCancellationTermDTO.setGroup(groupService.getGroup(authDTO, scheduleCancellationTermDTO.getGroup()));
					}

					String header = generateHeader(authDTO, scheduleDTO);

					if (StringUtil.isNull(scheduleCancellationTermDTO.getCode())) {
						StringBuilder keyword = new StringBuilder();
						keyword.append(StringUtil.isNull(scheduleCancellationTermDTO.getLookupCode()) ? "Cancellation Policy created" : "Cancellation Policy Override created");

						StringBuilder newContent = new StringBuilder();
						newContent.append(scheduleCancellationTermDTO.getCancellationTerm().getName());
						newContent.append(StringUtil.isNotNull(scheduleCancellationTermDTO.getActiveFrom()) ? new DateTime(scheduleCancellationTermDTO.getActiveFrom()).format("DD/MM/YYYY") + Text.COMMA : Text.EMPTY);
						newContent.append(StringUtil.isNotNull(scheduleCancellationTermDTO.getActiveTo()) ? new DateTime(scheduleCancellationTermDTO.getActiveTo()).format("DD/MM/YYYY") + Text.COMMA : Text.EMPTY);
						newContent.append(StringUtil.isNotNull(scheduleCancellationTermDTO.getDayOfWeek()) ? StringUtil.getDayOfWeek(scheduleCancellationTermDTO.getDayOfWeek()) + Text.COMMA : Text.EMPTY);
						if (scheduleCancellationTermDTO.getGroup() != null && StringUtil.isNotNull(scheduleCancellationTermDTO.getGroup().getCode())) {
							newContent.append(Text.COMMA);
							newContent.append("Group ");
							newContent.append(scheduleCancellationTermDTO.getGroup().getName());
						}

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);
					}
					else if (StringUtil.isNotNull(scheduleCancellationTermDTO.getCode()) && scheduleCancellationTermDTO.getActiveFlag() != Numeric.ONE_INT) {
						StringBuilder keyword = new StringBuilder();
						keyword.append("Cancellation Policy");

						StringBuilder newContent = new StringBuilder();
						if (scheduleCancellationTermDTO.getActiveFlag() == Numeric.ZERO_INT) {
							keyword.append(" has been disabled ");
							newContent.append(Text.HYPHEN);
						}
						else if (scheduleCancellationTermDTO.getActiveFlag() == Numeric.ONE_INT) {
							keyword.append(" has been changed ");
							newContent.append(scheduleCancellationTermDTO.getCancellationTerm().getName());
							newContent.append(StringUtil.isNotNull(scheduleCancellationTermDTO.getActiveFrom()) ? new DateTime(scheduleCancellationTermDTO.getActiveFrom()).format("DD/MM/YYYY") + Text.COMMA : Text.EMPTY);
							newContent.append(StringUtil.isNotNull(scheduleCancellationTermDTO.getActiveTo()) ? new DateTime(scheduleCancellationTermDTO.getActiveTo()).format("DD/MM/YYYY") + Text.COMMA : Text.EMPTY);
							newContent.append(StringUtil.isNotNull(scheduleCancellationTermDTO.getDayOfWeek()) ? StringUtil.getDayOfWeek(scheduleCancellationTermDTO.getDayOfWeek()) + Text.COMMA : Text.EMPTY);
							if (scheduleCancellationTermDTO.getGroup() != null && StringUtil.isNotNull(scheduleCancellationTermDTO.getGroup().getCode())) {
								newContent.append(Text.COMMA);
								newContent.append("Group ");
								newContent.append(scheduleCancellationTermDTO.getGroup().getName());
							}
						}
						else if (scheduleCancellationTermDTO.getActiveFlag() == Numeric.TWO_INT) {
							keyword.append(" has been deleted ");
							newContent.append(Text.HYPHEN);
						}

						Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
						scheduleActivityList.add(scheduleActivityMap);

					}
				}
			}
			// store Cache
			if (!scheduleActivityList.isEmpty()) {
				putScheduleActivityCache(scheduleDTO, scheduleActivityList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void scheduleBookingControlActivity(AuthDTO authDTO, ScheduleControlDTO scheduleControlDTO) {
		try {
			// Schedule Activity Log
			List<Map<String, String>> scheduleActivityList = new ArrayList<>();
			if (scheduleControlDTO.getGroup() != null && scheduleControlDTO.getGroup().getId() != 0) {
				scheduleControlDTO.setGroup(groupService.getGroup(authDTO, scheduleControlDTO.getGroup()));
			}
			else if (scheduleControlDTO.getGroup() != null && StringUtil.isNotNull(scheduleControlDTO.getGroup().getCode())) {
				scheduleControlDTO.setGroup(groupService.getGroup(authDTO, scheduleControlDTO.getGroup()));
			}
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleControlDTO.getSchedule().getCode());
			getScheduleDTO(authDTO, scheduleDTO);

			String header = generateHeader(authDTO, scheduleDTO);

			if (StringUtil.isNull(scheduleControlDTO.getCode())) {
				StringBuilder keyword = new StringBuilder();
				keyword.append(StringUtil.isNull(scheduleControlDTO.getLookupCode()) ? "Booking Control created" : "Booking Control Override created");

				StringBuilder newContent = new StringBuilder();
				newContent.append(scheduleControlDTO.getFromStation() != null ? "Stage :" : Text.EMPTY);
				newContent.append(scheduleControlDTO.getFromStation() != null ? scheduleControlDTO.getFromStation().getName() + "-" : Text.EMPTY);
				newContent.append(scheduleControlDTO.getToStation() != null ? scheduleControlDTO.getToStation().getName() : Text.EMPTY);
				newContent.append(Text.COMMA);
				newContent.append(StringUtil.isNotNull(scheduleControlDTO.getDayOfWeek()) ? "DayOfWeek:" : Text.EMPTY);
				newContent.append(StringUtil.isNotNull(scheduleControlDTO.getDayOfWeek()) ? StringUtil.getDayOfWeek(scheduleControlDTO.getDayOfWeek()) + Text.COMMA : Text.EMPTY);
				newContent.append("Booking ");
				newContent.append(scheduleControlDTO.getAllowBookingFlag() == 1 ? "Open" : "Close");
				newContent.append(Text.COMMA);
				newContent.append("Open :");
				newContent.append(getTimeV2(scheduleControlDTO.getOpenMinitues()));
				newContent.append(Text.COMMA);
				newContent.append("Close :");
				newContent.append(getTimeV2(scheduleControlDTO.getCloseMinitues()));
				newContent.append(scheduleControlDTO.getGroup() != null && StringUtil.isNotNull(scheduleControlDTO.getGroup().getCode()) ? "Group:" + Text.COMMA : Text.EMPTY);
				newContent.append(scheduleControlDTO.getGroup() != null && StringUtil.isNotNull(scheduleControlDTO.getGroup().getCode()) ? scheduleControlDTO.getGroup().getName() : Text.EMPTY);

				Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), Text.HYPHEN, newContent.toString());
				scheduleActivityList.add(scheduleActivityMap);
			}
			else {
				StringBuilder keyword = new StringBuilder();
				if (StringUtil.isNotNull(scheduleControlDTO.getCode()) && StringUtil.isNull(scheduleControlDTO.getLookupCode())) {
					keyword.append("Booking Control ");
				}
				else if (StringUtil.isNotNull(scheduleControlDTO.getCode()) && StringUtil.isNotNull(scheduleControlDTO.getLookupCode())) {
					keyword.append("Booking Control Override ");

				}
				List<ScheduleControlDTO> scheduleControlCacheList = getScheduleControlDTO(authDTO, scheduleDTO);

				ScheduleControlDTO previousScheduleControl = null;
				for (ScheduleControlDTO scheduleControl : scheduleControlCacheList) {
					if (scheduleControlDTO.getCode().equals(scheduleControl.getCode())) {
						previousScheduleControl = scheduleControl;
						if (previousScheduleControl.getGroup() != null && previousScheduleControl.getGroup().getId() != 0) {
							previousScheduleControl.setGroup(groupService.getGroup(authDTO, previousScheduleControl.getGroup()));
						}
						else if (previousScheduleControl.getGroup() != null && StringUtil.isNotNull(previousScheduleControl.getGroup().getCode())) {
							previousScheduleControl.setGroup(groupService.getGroup(authDTO, previousScheduleControl.getGroup()));
						}
						if (previousScheduleControl.getFromStation() != null) {
							previousScheduleControl.setFromStation(stationService.getStation(previousScheduleControl.getFromStation()));
						}
						if (previousScheduleControl.getToStation() != null) {
							previousScheduleControl.setToStation(stationService.getStation(previousScheduleControl.getToStation()));
						}
						break;
					}
				}

				StringBuilder newContent = new StringBuilder();
				StringBuilder oldContent = new StringBuilder();
				if (scheduleControlDTO.getActiveFlag() == Numeric.ZERO_INT) {
					keyword.append(" has been disabled ");
					newContent.append(Text.HYPHEN);
					oldContent.append(Text.HYPHEN);
				}
				else if (scheduleControlDTO.getActiveFlag() == Numeric.TWO_INT) {
					keyword.append(" has been deleted ");
					newContent.append(Text.HYPHEN);
					oldContent.append(Text.HYPHEN);
				}
				else if (scheduleControlDTO.getActiveFlag() == Numeric.ONE_INT) {
					if (previousScheduleControl == null) {
						for (ScheduleControlDTO scheduleControl : scheduleControlCacheList) {
							if (scheduleControl.getOverrideList() != null) {
								for (ScheduleControlDTO overrideScheduleControl : scheduleControl.getOverrideList()) {
									if (overrideScheduleControl.getCode().equals(scheduleControlDTO.getCode())) {
										previousScheduleControl = overrideScheduleControl;
										if (previousScheduleControl.getGroup() != null) {
											previousScheduleControl.setGroup(groupService.getGroup(authDTO, previousScheduleControl.getGroup()));
										}
										if (previousScheduleControl.getFromStation() != null) {
											previousScheduleControl.setFromStation(stationService.getStation(previousScheduleControl.getFromStation()));
										}
										if (previousScheduleControl.getToStation() != null) {
											previousScheduleControl.setToStation(stationService.getStation(previousScheduleControl.getToStation()));
										}
										break;
									}
								}
							}
						}
					}

					keyword.append(" has been changed ");

					// Group
					StringBuilder oldGroupContent = null;
					StringBuilder newGroupContent = null;
					if ((previousScheduleControl == null || previousScheduleControl.getGroup() == null) && (scheduleControlDTO != null && scheduleControlDTO.getGroup() != null)) {
						newGroupContent = new StringBuilder();

						if (scheduleControlDTO.getGroup() == null || StringUtil.isNull(scheduleControlDTO.getGroup().getCode())) {
							newGroupContent.append(Text.HYPHEN);
						}
						else if (scheduleControlDTO.getGroup() != null) {
							newGroupContent.append(scheduleControlDTO.getGroup().getName());
						}
					}
					else if ((previousScheduleControl != null && previousScheduleControl.getGroup() != null) && (scheduleControlDTO == null || scheduleControlDTO.getGroup() == null)) {
						oldGroupContent = new StringBuilder();
						if (previousScheduleControl.getGroup() == null || StringUtil.isNull(previousScheduleControl.getGroup().getCode())) {
							oldGroupContent.append(Text.HYPHEN);
						}
						else if (previousScheduleControl.getGroup() != null) {
							oldGroupContent.append(previousScheduleControl.getGroup().getName());
						}
					}
					else if (previousScheduleControl != null && previousScheduleControl.getGroup() != null && scheduleControlDTO.getGroup() != null && !previousScheduleControl.getGroup().getCode().equals(scheduleControlDTO.getGroup().getCode())) {
						oldGroupContent = new StringBuilder();
						newGroupContent = new StringBuilder();
						if (previousScheduleControl.getGroup() == null || StringUtil.isNull(previousScheduleControl.getGroup().getCode())) {
							oldGroupContent.append(Text.HYPHEN);
						}
						else if (previousScheduleControl.getGroup() != null) {
							oldGroupContent.append(previousScheduleControl.getGroup().getName());
						}

						if (scheduleControlDTO.getGroup() == null || StringUtil.isNull(scheduleControlDTO.getGroup().getCode())) {
							newGroupContent.append(Text.HYPHEN);
						}
						else if (scheduleControlDTO.getGroup() != null) {
							newGroupContent.append(scheduleControlDTO.getGroup().getName());
						}
					}

					StringBuilder oldFromStationContent = null;
					StringBuilder newFromStationContent = null;
					if (previousScheduleControl != null && previousScheduleControl.getFromStation() == null && scheduleControlDTO.getFromStation() != null) {
						newFromStationContent = new StringBuilder();
						if (scheduleControlDTO.getFromStation() == null || StringUtil.isNull(scheduleControlDTO.getFromStation().getCode())) {
							newFromStationContent.append(Text.HYPHEN);
						}
						else if (scheduleControlDTO.getFromStation() != null) {
							newFromStationContent.append(scheduleControlDTO.getFromStation().getName());
						}
					}
					else if (previousScheduleControl != null && previousScheduleControl.getFromStation() != null && scheduleControlDTO.getFromStation() == null) {
						oldFromStationContent = new StringBuilder();
						if (previousScheduleControl.getFromStation() == null || StringUtil.isNull(previousScheduleControl.getFromStation().getCode())) {
							oldFromStationContent.append(Text.HYPHEN);
						}
						else if (previousScheduleControl.getFromStation() != null) {
							oldFromStationContent.append(previousScheduleControl.getFromStation().getName());
						}
					}
					else if (previousScheduleControl != null && previousScheduleControl.getFromStation() != null && scheduleControlDTO.getFromStation() != null && !previousScheduleControl.getFromStation().getCode().equals(scheduleControlDTO.getFromStation().getCode())) {
						oldFromStationContent = new StringBuilder();
						newFromStationContent = new StringBuilder();
						if (previousScheduleControl.getFromStation() == null || StringUtil.isNull(previousScheduleControl.getFromStation().getCode())) {
							oldFromStationContent.append(Text.HYPHEN);
						}
						else if (previousScheduleControl.getFromStation() != null) {
							oldFromStationContent.append(previousScheduleControl.getFromStation().getName());
						}

						if (scheduleControlDTO.getFromStation() == null || StringUtil.isNull(scheduleControlDTO.getFromStation().getCode())) {
							newFromStationContent.append(Text.HYPHEN);
						}
						else if (scheduleControlDTO.getFromStation() != null) {
							newFromStationContent.append(scheduleControlDTO.getFromStation().getName());
						}
					}

					StringBuilder oldToStationContent = null;
					StringBuilder newToStationContent = null;
					if (previousScheduleControl != null && previousScheduleControl.getToStation() == null && scheduleControlDTO.getToStation() != null) {
						newToStationContent = new StringBuilder();
						if (scheduleControlDTO.getToStation() == null || StringUtil.isNull(scheduleControlDTO.getToStation().getCode())) {
							newToStationContent.append(Text.HYPHEN);
						}
						else if (scheduleControlDTO.getToStation() != null) {
							newToStationContent.append(scheduleControlDTO.getToStation().getName());
						}
					}
					else if (previousScheduleControl != null && previousScheduleControl.getToStation() != null && scheduleControlDTO.getToStation() == null) {
						oldToStationContent = new StringBuilder();
						if (previousScheduleControl.getToStation() == null || StringUtil.isNull(previousScheduleControl.getToStation().getCode())) {
							oldToStationContent.append(Text.HYPHEN);
						}
						else if (previousScheduleControl.getToStation() != null) {
							oldToStationContent.append(previousScheduleControl.getToStation().getName());
						}
					}
					else if (previousScheduleControl != null && previousScheduleControl.getToStation() != null && scheduleControlDTO.getToStation() != null && !previousScheduleControl.getToStation().getCode().equals(scheduleControlDTO.getToStation().getCode())) {
						oldToStationContent = new StringBuilder();
						newToStationContent = new StringBuilder();
						if (previousScheduleControl.getToStation() == null || StringUtil.isNull(previousScheduleControl.getToStation().getCode())) {
							oldToStationContent.append(Text.HYPHEN);
						}
						else if (previousScheduleControl.getToStation() != null) {
							oldToStationContent.append(previousScheduleControl.getToStation().getName());
						}

						if (scheduleControlDTO.getToStation() == null || StringUtil.isNull(scheduleControlDTO.getToStation().getCode())) {
							newToStationContent.append(Text.HYPHEN);
						}
						else if (scheduleControlDTO.getToStation() != null) {
							newToStationContent.append(scheduleControlDTO.getToStation().getName());
						}
					}

					StringBuilder oldOpenMinContent = null;
					StringBuilder newOpenMinContent = null;
					if (previousScheduleControl != null && previousScheduleControl.getOpenMinitues() != scheduleControlDTO.getOpenMinitues()) {
						oldOpenMinContent = new StringBuilder();
						newOpenMinContent = new StringBuilder();
						oldOpenMinContent.append(getTimeV2(previousScheduleControl.getOpenMinitues()));
						newOpenMinContent.append(getTimeV2(scheduleControlDTO.getOpenMinitues()));
					}
					StringBuilder oldCloseMinContent = null;
					StringBuilder newCloseMinContent = null;
					if (previousScheduleControl != null && previousScheduleControl.getCloseMinitues() != scheduleControlDTO.getCloseMinitues()) {
						oldCloseMinContent = new StringBuilder();
						newCloseMinContent = new StringBuilder();
						oldCloseMinContent.append(getTimeV2(previousScheduleControl.getCloseMinitues()));
						newCloseMinContent.append(getTimeV2(scheduleControlDTO.getCloseMinitues()));
					}
					StringBuilder oldAllowBookingContent = null;
					StringBuilder newAllowBookingContent = null;
					if (previousScheduleControl != null && previousScheduleControl.getAllowBookingFlag() != scheduleControlDTO.getAllowBookingFlag()) {
						oldAllowBookingContent = new StringBuilder();
						newAllowBookingContent = new StringBuilder();
						oldAllowBookingContent.append(previousScheduleControl.getAllowBookingFlag());
						newAllowBookingContent.append(scheduleControlDTO.getAllowBookingFlag());
					}

					// Stage
					// Old Content
					if (previousScheduleControl != null && previousScheduleControl.getGroup() != null && previousScheduleControl.getFromStation() != null && previousScheduleControl.getToStation() != null && (oldFromStationContent != null || oldToStationContent != null)) {
						oldContent.append("From ");
						oldContent.append(previousScheduleControl.getFromStation().getName());
						oldContent.append(Text.HYPHEN);
						oldContent.append(previousScheduleControl.getToStation().getName());
						oldContent.append(Text.COMMA);
					}
					if (previousScheduleControl != null && oldGroupContent != null && StringUtil.isNotNull(previousScheduleControl.getGroup().getName())) {
						oldContent.append("Group: ");
						oldContent.append(previousScheduleControl.getGroup().getName());
						oldContent.append(Text.COMMA);
					}
					if (previousScheduleControl != null && oldOpenMinContent != null) {
						oldContent.append("Open : ");
						oldContent.append(getTimeV2(previousScheduleControl.getOpenMinitues()));
						oldContent.append(Text.COMMA);
					}
					if (previousScheduleControl != null && oldCloseMinContent != null) {
						oldContent.append("Close : ");
						oldContent.append(getTimeV2(previousScheduleControl.getCloseMinitues()));
						oldContent.append(Text.COMMA);
					}
					if (previousScheduleControl != null && oldAllowBookingContent != null) {
						oldContent.append("Booking ");
						oldContent.append(previousScheduleControl.getAllowBookingFlag() == Numeric.ONE_INT ? " Opened" : " Closed");
					}
					// New Content
					if (scheduleControlDTO.getGroup() != null && scheduleControlDTO.getFromStation() != null && scheduleControlDTO.getToStation() != null && (newFromStationContent != null || newToStationContent != null)) {
						newContent.append("From ");
						newContent.append(scheduleControlDTO.getFromStation().getName());
						newContent.append(Text.HYPHEN);
						newContent.append(scheduleControlDTO.getToStation().getName());
						newContent.append(Text.COMMA);
					}
					if (newGroupContent != null) {
						newContent.append("Group: ");
						newContent.append(scheduleControlDTO.getGroup().getName());
						newContent.append(Text.COMMA);
					}
					if (newOpenMinContent != null) {
						newContent.append("Open : ");
						newContent.append(getTimeV2(scheduleControlDTO.getOpenMinitues()));
						newContent.append(Text.COMMA);
					}
					if (newCloseMinContent != null) {
						newContent.append("Close : ");
						newContent.append(getTimeV2(scheduleControlDTO.getCloseMinitues()));
						newContent.append(Text.COMMA);
					}
					if (newAllowBookingContent != null) {
						newContent.append("Booking ");
						newContent.append(scheduleControlDTO.getAllowBookingFlag() == Numeric.ONE_INT ? " Open" : " Close");
					}

					Map<String, String> scheduleActivityMap = putScheduleHistory(authDTO, header, keyword.toString(), oldContent.toString(), newContent.toString());
					scheduleActivityList.add(scheduleActivityMap);
				}
			}
			// store Cache
			if (!scheduleActivityList.isEmpty()) {
				putScheduleActivityCache(scheduleControlDTO.getSchedule(), scheduleActivityList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Map<String, String> putScheduleHistory(AuthDTO authDTO, String header, String keyword, String oldContent, String newContent) {
		Map<String, String> scheduleActivityMap = new HashMap<>();
		scheduleActivityMap.put("namespaceCode", authDTO.getNamespaceCode());
		scheduleActivityMap.put("header", StringUtil.isNotNull(header) ? header : Text.HYPHEN);
		scheduleActivityMap.put("keyword", StringUtil.isNotNull(keyword) ? keyword : Text.HYPHEN);
		scheduleActivityMap.put("oldContent", StringUtil.isNotNull(oldContent) ? oldContent : Text.HYPHEN);
		scheduleActivityMap.put("newContent", StringUtil.isNotNull(newContent) ? newContent : Text.HYPHEN);
		scheduleActivityMap.put("updatedBy", authDTO.getUser().getName());
		scheduleActivityMap.put("updatedAt", DateUtil.NOW().format("MMM DD WWW, hh12:mm a", Locale.forLanguageTag("en_IN")));
		return scheduleActivityMap;
	}

	private String generateHeader(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleDTO schedule = new ScheduleDTO();
		schedule.setCode(scheduleDTO.getCode());
		schedule.setName(scheduleDTO.getName());
		schedule.setServiceNumber(scheduleDTO.getServiceNumber());
		if (StringUtil.isNull(scheduleDTO.getCode())) {
			schedule.setCode(scheduleDTO.getLookupCode());
			schedule = getScheduleDTO(authDTO, schedule);
		}
		StringBuilder header = new StringBuilder();
		header.append(StringUtil.isNotNull(scheduleDTO.getName()) ? schedule.getName() + Text.COMMA + Text.SINGLE_SPACE : Text.EMPTY);
		header.append(StringUtil.isNotNull(schedule.getServiceNumber()) ? schedule.getServiceNumber() + Text.COMMA + Text.SINGLE_SPACE : Text.EMPTY);
		header.append(schedule.getCode());
		return header.toString();
	}

	private String getTime(int minutes) {
		String time = DateUtil.addMinituesToDate(DateUtil.NOW().getStartOfDay(), minutes).format("hh12:mm a", Locale.forLanguageTag("en_IN"));
		if (minutes > 1440 && minutes <= 2880) {
			time = time + " Next Day ";
		}
		else if (minutes > 2880) {
			time = time + " Third Day ";
		}
		return time;
	}

	private String getTimeV2(int minutes) {
		int day = minutes / 24 / 60;
		int hour = minutes / 60 % 24;
		int min = minutes % 60;

		StringBuilder timeConverion = new StringBuilder();
		timeConverion.append(day > 1 ? day + " days" : day + " day");
		timeConverion.append(Text.COLON);
		timeConverion.append(hour > 1 ? hour + " hours" : hour + " hour");
		timeConverion.append(Text.COLON);
		timeConverion.append(min > 1 ? min + " minutes" : min + " minute");
		return timeConverion.toString();
	}

	private void putScheduleActivityCache(ScheduleDTO scheduleDTO, List<Map<String, String>> scheduleActivityList) {
		String scheduleCode = scheduleDTO.getCode();
		if (StringUtil.isNull(scheduleCode)) {
			scheduleCode = scheduleDTO.getLookupCode();
		}
		Element element = EhcacheManager.getScheduleActivityCache().get(scheduleCode);
		if (element != null) {
			List<Map<String, String>> scheduleActivityCacheList = (List<Map<String, String>>) element.getObjectValue();
			scheduleActivityList.addAll(scheduleActivityCacheList);
		}
		element = new Element(scheduleCode, scheduleActivityList);
		EhcacheManager.getScheduleActivityCache().put(element);
	}
}

package org.in.com.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.utils.StringUtil;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleDTO extends BaseDTO<ScheduleDTO> {
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String serviceNumber;
	private String displayName;
	private String apiDisplayName;
	private String pnrStartCode;
	private String preRequrities;
	// private BigDecimal acBusTax = BigDecimal.ZERO;
	private String lookupCode;
	private DateTime tripDate;
	private List<ScheduleDTO> overrideList = new ArrayList<ScheduleDTO>();
	private NamespaceTaxDTO tax;

	private List<ScheduleStageDTO> scheduleStageList;
	private List<ScheduleStageDTO> otherSscheduleStageList;
	private List<StageDTO> stageList;
	private List<ScheduleControlDTO> controlList;
	private List<ScheduleStationDTO> stationList;
	private List<ScheduleStationPointDTO> stationPointList;
	private ScheduleBusDTO scheduleBus;
	private ScheduleDiscountDTO scheduleDiscount;
	private CancellationTermDTO cancellationTerm;
	private List<ScheduleSeatVisibilityDTO> seatVisibilityList;
	private List<ScheduleSeatPreferenceDTO> seatPreferenceList;
	private List<ScheduleSeatAutoReleaseDTO> seatAutoReleaseList;
	private List<ScheduleTimeOverrideDTO> timeOverrideList;
	private List<ScheduleFareAutoOverrideDTO> fareAutoOverrideList;
	private List<ScheduleSeatFareDTO> seatFareList;
	private List<TravelStopsDTO> travelStopsList;
	private ScheduleCategoryDTO category;
	private ScheduleDynamicStageFareDetailsDTO dynamicStageFare;
	private ScheduleTicketTransferTermsDTO ticketTransferTerms;
	private List<TripDTO> tripList;

	private boolean debugEnabled;
	private List<ScheduleTagDTO> scheduleTagList;
	private float distance;
	private List<SectorDTO> sectorList;
	private Map<String, String> additionalAttributes = new HashMap<String, String>();

	public Map<String, BusSeatTypeEM> getUniqueStageBusType() {
		Map<String, BusSeatTypeEM> typeMap = new HashMap<String, BusSeatTypeEM>();
		for (ScheduleStageDTO scheduleStageDTO : scheduleStageList) {
			typeMap.put(scheduleStageDTO.getBusSeatType().getCode(), scheduleStageDTO.getBusSeatType());
		}
		return typeMap;
	}

	public Map<String, BusSeatTypeEM> getUniqueStageBusType(List<ScheduleStageDTO> scheduleStageList) {
		Map<String, BusSeatTypeEM> typeMap = new HashMap<String, BusSeatTypeEM>();
		for (ScheduleStageDTO scheduleStageDTO : scheduleStageList) {
			typeMap.put(scheduleStageDTO.getBusSeatType().getCode(), scheduleStageDTO.getBusSeatType());
		}
		return typeMap;
	}

	public String getScheduleTagIds() {
		StringBuilder scheduleTagIds = new StringBuilder();
		String tags = Text.EMPTY;
		if (scheduleTagList != null) {
			for (ScheduleTagDTO scheduleTag : scheduleTagList) {
				if (scheduleTag.getId() == 0) {
					continue;
				}
				scheduleTagIds.append(scheduleTag.getId());
				scheduleTagIds.append(Text.COMMA);
			}
			tags = scheduleTagIds.toString();
		}
		if (StringUtil.isNull(tags)) {
			tags = Text.NA;
		}
		return tags;
	}

	public String getSectorIds() {
		StringBuilder sectorIds = new StringBuilder();
		String sector = Text.EMPTY;
		if (sectorList != null) {
			for (SectorDTO sectorDTO : sectorList) {
				if (sectorDTO.getId() == 0) {
					continue;
				}
				sectorIds.append(sectorDTO.getId());
				sectorIds.append(Text.COMMA);
			}
			sector = sectorIds.toString();
		}
		if (StringUtil.isNull(sector)) {
			sector = Text.NA;
		}
		return sector;

	}

}

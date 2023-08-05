package org.in.com.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import com.google.common.collect.Iterables;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleBusOverrideDTO extends BaseDTO<ScheduleBusOverrideDTO> {
	private ScheduleDTO schedule;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private BusDTO bus;
	private String lookupCode;
	private NamespaceTaxDTO tax;
	private List<String> tripDates;
	private List<ScheduleBusOverrideDTO> overrideList = new ArrayList<ScheduleBusOverrideDTO>();

	public String getTripDatesToString() {
		StringBuilder tripdates = new StringBuilder();
		if (tripDates != null && !tripDates.isEmpty()) {
			for (String tripDate : tripDates) {
				if (StringUtil.isNotNull(tripdates.toString())) {
					tripdates.append(Text.COMMA);
				}
				tripdates.append(tripDate);
			}
		}
		return tripdates.toString();
	}

	public List<DateTime> getTripDateTimes() {
		List<DateTime> tripDateTimes = new ArrayList<>();
		if (DateUtil.isValidDate(activeFrom) && DateUtil.isValidDate(activeTo)) {
			tripDateTimes = DateUtil.getDateListV3(DateUtil.getDateTime(activeFrom), DateUtil.getDateTime(activeTo), dayOfWeek);
		}
		else if (tripDates != null && !tripDates.isEmpty()) {
			for (String tripDate : tripDates) {
				if (DateUtil.isValidDate(tripDate)) {
					tripDateTimes.add(DateUtil.getDateTime(tripDate));
				}
			}
		}
		Collections.sort(tripDateTimes);
		return tripDateTimes;
	}

	public DateTime getActiveFromDateTime() {
		DateTime dateTime = DateUtil.getDateTime(activeFrom);
		if (!DateUtil.isValidDate(activeFrom)) {
			List<DateTime> tripDateTimes = getTripDateTimes();
			Collections.sort(tripDateTimes);
			dateTime = Iterables.getFirst(tripDateTimes, null);
		}
		return dateTime;
	}

	public DateTime getActiveToDateTime() {
		DateTime dateTime = DateUtil.getDateTime(activeTo);
		if (!DateUtil.isValidDate(activeTo)) {
			List<DateTime> tripDateTimes = getTripDateTimes();
			Collections.sort(tripDateTimes);
			dateTime = Iterables.getLast(tripDateTimes, null);
		}
		return dateTime;
	}
}

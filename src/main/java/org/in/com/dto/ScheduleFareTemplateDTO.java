package org.in.com.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Text;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleFareTemplateDTO extends BaseDTO<ScheduleFareTemplateDTO> {
	private BusDTO bus;
	private List<RouteDTO> stageFare;
	private AuditDTO audit;
	private List<DateTime> tripDates;

	public Map<String, RouteDTO> getRouteFare() {
		Map<String, RouteDTO> routeMap = new HashMap<String, RouteDTO>();
		for (RouteDTO route : stageFare) {
			routeMap.put(route.getFromStation().getId() + Text.UNDER_SCORE + route.getToStation().getId(), route);
		}
		return routeMap;
	}
}

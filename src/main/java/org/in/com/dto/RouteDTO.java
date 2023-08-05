package org.in.com.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RouteDTO extends BaseDTO<RouteDTO> {
	private StationDTO fromStation;
	private StationDTO toStation;
	private int topRouteFlag;
	private int bookingCount;
	private int minFare;
	private int maxFare;
	private List<StageFareDTO> stageFare;
	
	public Map<String, BigDecimal> getMinMaxFare() {
		BigDecimal minFare = null;
		BigDecimal maxFare = null;
		Map<String, BigDecimal> minmaxFare = new HashMap<String, BigDecimal>();
		for (StageFareDTO fare : stageFare) {
			if (minFare == null || minFare.compareTo(fare.getFare()) == 1) {
				minFare = fare.getFare();
			}
			if (maxFare == null || maxFare.compareTo(fare.getFare()) == 0) {
				maxFare = fare.getFare();
			}
		}
		minmaxFare.put("minFare", minFare);
		minmaxFare.put("maxFare", maxFare);
		return minmaxFare;
	}
}

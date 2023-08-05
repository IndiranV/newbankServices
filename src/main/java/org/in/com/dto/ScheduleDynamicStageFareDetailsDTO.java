package org.in.com.dto;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.dto.enumeration.DynamicPriceProviderEM;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleDynamicStageFareDetailsDTO extends BaseDTO<ScheduleDynamicStageFareDetailsDTO> {
	private BigDecimal minFare;
	private BigDecimal maxFare;
	private StationDTO fromStation;
	private StationDTO toStation;
	private List<BusSeatLayoutDTO> seatFare;
	private DateTime tripDate;
	private DynamicPriceProviderEM dynamicPriceProvider;

	public Map<String, BigDecimal> getMinMaxFare() {
		BigDecimal minFare = null;
		BigDecimal maxFare = null;
		Map<String, BigDecimal> minmaxFare = new HashMap<String, BigDecimal>();
		for (BusSeatLayoutDTO layout : seatFare) {
			if (minFare == null || minFare.compareTo(layout.getFare()) == 1) {
				minFare = layout.getFare();
			}
			if (maxFare == null || maxFare.compareTo(layout.getFare()) == 0) {
				maxFare = layout.getFare();
			}
		}
		minmaxFare.put("minFare", minFare);
		minmaxFare.put("maxFare", maxFare);
		return minmaxFare;
	}
}
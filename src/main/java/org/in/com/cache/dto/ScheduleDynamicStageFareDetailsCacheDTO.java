package org.in.com.cache.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ScheduleDynamicStageFareDetailsCacheDTO implements Serializable {
	private static final long serialVersionUID = -96110725857274157L;
	private BigDecimal minFare;
	private BigDecimal maxFare;
	private int fromStationId;
	private int toStationId;
	List<String> seatNameFare;
	private String tripDate;

	// private List<ScheduleDynamicStageTripFareDetailsCacheDTO> tripFareList;;
}
package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripPickupVanChartDTO extends BaseDTO<TripPickupVanChartDTO> {
	private TripVanInfoDTO tripVanInfo;
	private List<TripChartDetailsDTO> ticketDetails = new ArrayList<>();
}

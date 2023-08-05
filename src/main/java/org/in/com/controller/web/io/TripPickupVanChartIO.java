package org.in.com.controller.web.io;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripPickupVanChartIO extends BaseIO {
	private TripVanInfoIO tripVanInfo;
	private List<TripChartDetailsIO> ticketDetails = new ArrayList<>();
}

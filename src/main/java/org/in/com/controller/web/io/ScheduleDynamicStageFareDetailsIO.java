package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleDynamicStageFareDetailsIO extends BaseIO {
	private BigDecimal minFare;
	private BigDecimal maxFare;
	private StationIO fromStation;
	private StationIO toStation;
	
	private List<BusSeatLayoutIO> seatFare;
	private String tripDate;
}
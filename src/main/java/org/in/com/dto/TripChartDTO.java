package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripChartDTO extends BaseDTO<TripChartDTO> {

	private BusDTO bus;
	private int seatCount;
	private String scheduleCode;
	private DateTime tripDate;
	private TripDTO trip;
	private List<String> seatNumbers;
	private List<String> vacantSeats;
	private List<TripChartDetailsDTO> ticketDetailsList;
	private List<TripDTO> tripList;
	private List<String> vendorList;

}

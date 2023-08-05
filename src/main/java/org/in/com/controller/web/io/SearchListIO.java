package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchListIO extends BaseIO {
	private String serviceNumber;
	private String specialText;
	private String categoryCode;
	private int availabeSeats;
	private int depatureMinitues;
	private int arrialMinitues;
	private int mTicketFlag;
	private String fare;
	private List<String> amenities;
	private List<StationPointIO> boardingPointList;
	private List<StationPointIO> dropingPointList;
}

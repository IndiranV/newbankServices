package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class StageStationDTO {
	StationDTO station;
	private int minitues;
	private int stationSequence;
	private List<StationPointDTO> stationPoint = new ArrayList<>();
	private String mobileNumber;
}

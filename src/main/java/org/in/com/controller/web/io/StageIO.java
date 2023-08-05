package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;

@Data
public class StageIO {
	private String code;
	private StationIO fromStation;
	private StationIO toStation;
	private int stageSequence;
	private int distance;
	private List<StageFareIO> stageFare;
	private TripStatusIO stageStatus;
}
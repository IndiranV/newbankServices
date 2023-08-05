package org.in.com.controller.api_v3.io;

import java.util.List;

import lombok.Data;

@Data
public class StageIO {
	private String code;
	private StationIO fromStation;
	private StationIO toStation;
	private int stageSequence;
	private List<StageFareIO> stageFare;
	private BusIO bus;
}
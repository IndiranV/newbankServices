package org.in.com.controller.busbuddy.io;

import java.util.List;

import org.in.com.controller.web.io.StageFareIO;

import lombok.Data;

@Data
public class StageIO {
	private String code;
	private StationIO fromStation;
	private StationIO toStation;
	private int stageSequence;
	private List<StageFareIO> stageFare;
}
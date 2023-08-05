package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RouteIO extends BaseIO {
	private BaseIO fromStation;
	private BaseIO toStation;
	private int minFare;
	private int maxFare;
	private List<StageFareIO> stageFare;
}

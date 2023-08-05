package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.StationIO;
@Data
@EqualsAndHashCode(callSuper = true)
public class AllStationsIO extends BaseIO{
	private List<StationIO> stationsList;
}

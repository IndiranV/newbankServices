package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchIO extends BaseIO {
	private String travelDate;
	private String fromStationCode;
	private String toStationCode;
	private String scheduleCode;
	private String scheduleStageCode;
	private List<SearchListIO> searchList;

}

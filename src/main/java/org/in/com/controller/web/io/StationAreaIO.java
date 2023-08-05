package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StationAreaIO extends BaseIO {
	private String latitude;
	private String longitude;
	private int radius;
	private StationIO station;
}

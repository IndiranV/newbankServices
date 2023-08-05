package org.in.com.controller.busbuddy.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class RouteIO extends BaseIO {
	private BaseIO fromStation;
	private BaseIO toStation;
}

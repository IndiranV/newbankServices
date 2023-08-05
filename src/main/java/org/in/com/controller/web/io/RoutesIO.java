package org.in.com.controller.web.io;

import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.StationIO;
@Data
@EqualsAndHashCode(callSuper = true)
public class RoutesIO extends BaseIO{
	private Map<String, List<StationIO>> allRoutes;
}

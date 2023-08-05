package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SectorIO extends BaseIO {
	private List<ScheduleIO> schedules;
	private List<BusVehicleIO> vehicles;
	private List<StationIO> stations;
	private List<OrganizationIO> organizations;
}

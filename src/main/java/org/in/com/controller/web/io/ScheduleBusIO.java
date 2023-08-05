package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleBusIO extends BaseIO {
	private float distance;
	private ScheduleIO schedule;
	private BusIO bus;
	private NamespaceTaxIO tax;
	private BusBreakevenSettingsIO breakevenSettings;
	private List<BusSeatLayoutIO> busSeatLayout;
	private List<AmenitiesIO> amenities;
	private List<ScheduleBusIO> overrideList;

}
package org.in.com.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleBusDTO extends BaseDTO<ScheduleBusDTO> {
	private float distance;
	private ScheduleDTO schedule;
	private BusDTO bus;
	private NamespaceTaxDTO tax;
	private BusBreakevenSettingsDTO breakevenSettings;
	private List<AmenitiesDTO> amentiesList;
}
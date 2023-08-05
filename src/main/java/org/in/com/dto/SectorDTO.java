package org.in.com.dto;

import java.util.List;

import org.in.com.constants.Text;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SectorDTO extends BaseDTO<SectorDTO> {
	private List<ScheduleDTO> schedule;
	private List<BusVehicleDTO> vehicle;
	private List<StationDTO> station;
	private List<OrganizationDTO> organization;

	public String getScheduleIds() {
		String scheduleIds = Text.NA;
		if (schedule != null) {
			StringBuilder schedules = new StringBuilder();
			for (ScheduleDTO scheduleDTO : schedule) {
				if (scheduleDTO.getId() == 0) {
					continue;
				}
				schedules.append(scheduleDTO.getId());
				schedules.append(Text.COMMA);
			}

			if (StringUtil.isNotNull(schedules)) {
				scheduleIds = schedules.toString();
			}
		}
		return scheduleIds;
	}

	public String getVehicleIds() {
		String vehicleIds = Text.NA;
		if (vehicle != null) {
			StringBuilder vehicles = new StringBuilder();
			for (BusVehicleDTO vehicleDTO : vehicle) {
				if (vehicleDTO.getId() == 0) {
					continue;
				}
				vehicles.append(vehicleDTO.getId());
				vehicles.append(Text.COMMA);
			}

			if (StringUtil.isNotNull(vehicles)) {
				vehicleIds = vehicles.toString();
			}
		}
		return vehicleIds;
	}

	public String getStationIds() {
		String stationIds = Text.NA;
		if (station != null) {
			StringBuilder stations = new StringBuilder();
			for (StationDTO stationDTO : station) {
				if (stationDTO.getId() == 0) {
					continue;
				}
				stations.append(stationDTO.getId());
				stations.append(Text.COMMA);
			}

			if (StringUtil.isNotNull(stations)) {
				stationIds = stations.toString();
			}
		}
		return stationIds;
	}

	public String getOrganizationIds() {
		String organizationIds = Text.NA;
		if (organization != null) {
			StringBuilder organizations = new StringBuilder();
			for (OrganizationDTO organizationDTO : organization) {
				if (organizationDTO.getId() == 0) {
					continue;
				}
				organizations.append(organizationDTO.getId());
				organizations.append(Text.COMMA);
			}

			if (StringUtil.isNotNull(organizations)) {
				organizationIds = organizations.toString();
			}
		}
		return organizationIds;
	}
}

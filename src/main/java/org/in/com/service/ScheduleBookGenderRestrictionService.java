package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleBookGenderRestrictionDTO;
import org.in.com.dto.TripDTO;

public interface ScheduleBookGenderRestrictionService extends BaseService<ScheduleBookGenderRestrictionDTO> {
	public ScheduleBookGenderRestrictionDTO getScheduleBookGenderRestrictionBySchedule(AuthDTO authDTO, TripDTO tripDTO);
}

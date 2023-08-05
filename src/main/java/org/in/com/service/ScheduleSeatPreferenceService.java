package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatPreferenceDTO;

public interface ScheduleSeatPreferenceService {
	public List<ScheduleSeatPreferenceDTO> get(AuthDTO authDTO, ScheduleSeatPreferenceDTO dto);

	public ScheduleSeatPreferenceDTO Update(AuthDTO authDTO, ScheduleSeatPreferenceDTO dto);

	public List<ScheduleSeatPreferenceDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public List<ScheduleSeatPreferenceDTO> getTripScheduleSeatPreference(AuthDTO authDTO, ScheduleDTO scheduleDTO);

}

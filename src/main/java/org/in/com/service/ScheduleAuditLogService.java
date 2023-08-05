package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;

public interface ScheduleAuditLogService {

	public void updateScheduleAudit(AuthDTO authDTO, ScheduleDTO scheduleDTO);
	
	public void updateScheduleStationAudit(AuthDTO authDTO, ScheduleStationDTO scheduleStationDTO);

	public void updateScheduleStationPointAudit(AuthDTO authDTO, ScheduleStationPointDTO scheduleStationPointDTO); 
	
}

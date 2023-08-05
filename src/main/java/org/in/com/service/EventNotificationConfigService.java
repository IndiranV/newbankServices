package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.EventNotificationConfigDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.EventNotificationEM;

public interface EventNotificationConfigService extends BaseService<EventNotificationConfigDTO> {

	public EventNotificationConfigDTO getActiveNotificationConfig(AuthDTO authDTO, TripDTO tripDTO, StationDTO fromStation, StationDTO	toStation, EventNotificationEM notificationEvent, String ticketCode);
}

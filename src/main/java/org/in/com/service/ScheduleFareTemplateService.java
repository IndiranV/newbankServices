package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleFareTemplateDTO;
import org.in.com.dto.TripDTO;

public interface ScheduleFareTemplateService extends BaseService<ScheduleFareTemplateDTO> {
	public List<ScheduleFareTemplateDTO> getTripStageTemplate(AuthDTO authDTO, TripDTO trip);

	public ScheduleFareTemplateDTO getScheduleFareTemplate(AuthDTO authDTO, ScheduleFareTemplateDTO fareTemplate);

}

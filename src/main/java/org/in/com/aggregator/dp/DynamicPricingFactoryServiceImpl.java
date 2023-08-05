package org.in.com.aggregator.dp;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.enumeration.IntegrationTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.IntegrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONObject;

@Service
public class DynamicPricingFactoryServiceImpl implements DynamicPricingFactoryService {
	@Autowired
	IntegrationService integrationService;

	@Override
	public JSONObject getAlreadyRegisteredScheduleStage(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject registerScheduleStage(AuthDTO authDTO, ScheduleDynamicStageFareDTO scheduleDynamicStageFare) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject registerNewTripOpen(AuthDTO authDTO, ScheduleDTO schedule, DateTime endDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getScheduleStageFare(AuthDTO authDTO, ScheduleDTO schedule, DateTime tripDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateOccupancyStatus(AuthDTO authDTO, ScheduleDynamicStageFareDTO dynamicStageFare, TicketDTO ticket) {
		IntegrationDTO integration = integrationService.getIntegration(authDTO, IntegrationTypeEM.DPE, dynamicStageFare.getDynamicPriceProvider().getCode());
		if (integration == null) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		DynamicPricingFactoryInterface dpi = DynamicPricingFactory.returnDPInstance(dynamicStageFare.getDynamicPriceProvider().getImpl());
		dpi.updateOccupancyStatus(authDTO, integration, ticket);
	}

	@Override
	public void updateSeatVisibilityStatus(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation, ScheduleSeatVisibilityDTO scheduleSeatVisibility) {
		// TODO Auto-generated method stub

	}

	@Override
	public JSONObject registerScheduleViaStage(AuthDTO authDTO, ScheduleDynamicStageFareDTO stage, List<ScheduleDynamicStageFareDetailsDTO> stageFares) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject unRegisterScheduleViaStage(AuthDTO authDTO, ScheduleDynamicStageFareDTO stage, List<ScheduleDynamicStageFareDetailsDTO> stageFares) {
		// TODO Auto-generated method stub
		return null;
	}

	@Async
	public void notifyBusTypeChange(AuthDTO authDTO, ScheduleDTO schedule, ScheduleDynamicStageFareDetailsDTO dynamicStageFare, ScheduleBusOverrideDTO busOverride) {
		IntegrationDTO integration = integrationService.getIntegration(authDTO, IntegrationTypeEM.DPE, dynamicStageFare.getDynamicPriceProvider().getCode());
		if (integration == null) {
			System.out.println("DP intergration not found " + schedule.getCode());
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		DynamicPricingFactoryInterface dpi = DynamicPricingFactory.returnDPInstance(dynamicStageFare.getDynamicPriceProvider().getImpl());
		dpi.notifyBusTypeChange(authDTO, integration, schedule, busOverride);
	}
}

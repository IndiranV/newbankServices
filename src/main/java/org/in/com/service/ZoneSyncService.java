package org.in.com.service;

import java.util.List;

import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CalendarAnnouncementDTO;
import org.in.com.dto.FareRuleDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationOtaPartnerDTO;

public interface ZoneSyncService {

	public List<AmenitiesDTO> zoneSyncAmenities(AuthDTO authDTO, String bitsAuthtoken);

	public List<StationDTO> zoneSyncStation(AuthDTO authDTO, String bitsAuthtoken);

	public List<MenuDTO> zoneSyncMenu(AuthDTO authDTO, String bitsAuthtoken);

	public List<ReportQueryDTO> zoneSyncReportQuery(AuthDTO authDTO, String bitsAuthtoken);

	public List<FareRuleDetailsDTO> zoneSyncFareRuleDetails(AuthDTO authDTO, String bitsAuthtoken, FareRuleDTO fareRule);

	public List<StationOtaPartnerDTO> zoneSyncStationOtaPartner(AuthDTO authDTO, String bitsAuthtoken);

	public List<StationAreaDTO> zoneSyncStationArea(AuthDTO authDTO, String bitsAuthtoken);

	public List<StationAreaDTO> syncStationArea(AuthDTO authDTO);
	
	public List<CalendarAnnouncementDTO> zoneSyncCalendarAnnouncement(AuthDTO authDTO, String bitsAuthtoken);

}

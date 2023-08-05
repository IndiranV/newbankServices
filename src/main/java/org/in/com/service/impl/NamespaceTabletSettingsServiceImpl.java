package org.in.com.service.impl;

import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.in.com.cache.NamespaceCache;
import org.in.com.config.ApplicationConfig;
import org.in.com.dao.NamespaceTabletSettingsDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceTabletSettingsDTO;
import org.in.com.service.NamespaceService;
import org.in.com.service.NamespaceTabletSettingsService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class NamespaceTabletSettingsServiceImpl extends NamespaceCache implements NamespaceTabletSettingsService {
	@Autowired
	NamespaceService namespaceService;

	@Override
	public List<NamespaceTabletSettingsDTO> get(AuthDTO authDTO, NamespaceTabletSettingsDTO dto) {

		return null;
	}

	@Override
	public List<NamespaceTabletSettingsDTO> getAll(AuthDTO authDTO) {

		return null;
	}

	@Override
	public NamespaceTabletSettingsDTO Update(AuthDTO authDTO, NamespaceTabletSettingsDTO dto) {
		NamespaceTabletSettingsDAO dao = new NamespaceTabletSettingsDAO();
		dao.updateNamespaceTabletSettings(authDTO, dto);

		/** Clear Cache */
		namespaceService.removeNamespaceTabletSettingsCache(authDTO);
		return null;
	}

	@Override
	public NamespaceTabletSettingsDTO getNamespaceTabletSetting(AuthDTO authDTO) {
		NamespaceTabletSettingsDAO dao = new NamespaceTabletSettingsDAO();
		return dao.getNamespaceTabletSettings(authDTO, authDTO.getNamespace());
	}

	@Override
	public JSONObject getNamespaceTabletSettingJson(AuthDTO authDTO) {
		JSONObject json = new JSONObject();
		NamespaceTabletSettingsDTO tabletSettingsDTO = getNamespaceTabletSetting(authDTO);
		if (tabletSettingsDTO != null) {
			json.put("tabs", tabletSettingsDTO.getTabs());
			json.put("tripSyncPeriod", tabletSettingsDTO.getTripSyncPeriod());
			json.put("forceReleaseFlag", tabletSettingsDTO.getForceReleaseFlag());

			int discount = 0;
			int serviceCharge = 0;
			int optionGst = 0;

			boolean tripStartPhoto = false;
			boolean tripStartOdometer = false;
			boolean tripStartCrewPhoto = false;
			boolean tripEndPhoto = false;
			boolean tripEndOdometer = false;
			boolean tripEndCrewPhoto = false;

			int maskMobileNumberFlag = 0;
			int dialerEnabledFlag = 0;

			if (tabletSettingsDTO.getFlagCodes().length() >= 11) {
				discount = StringUtil.getIntegerValue(String.valueOf(tabletSettingsDTO.getFlagCodes().charAt(0)));
				serviceCharge = Integer.valueOf(String.valueOf(tabletSettingsDTO.getFlagCodes().charAt(1)));
				optionGst = Integer.valueOf(String.valueOf(tabletSettingsDTO.getFlagCodes().charAt(2)));

				tripStartPhoto = BooleanUtils.toBoolean(Integer.valueOf(String.valueOf(tabletSettingsDTO.getFlagCodes().charAt(3))));
				tripStartOdometer = BooleanUtils.toBoolean(Integer.valueOf(String.valueOf(tabletSettingsDTO.getFlagCodes().charAt(4))));
				tripStartCrewPhoto = BooleanUtils.toBoolean(Integer.valueOf(String.valueOf(tabletSettingsDTO.getFlagCodes().charAt(5))));
				tripEndPhoto = BooleanUtils.toBoolean(Integer.valueOf(String.valueOf(tabletSettingsDTO.getFlagCodes().charAt(6))));
				tripEndOdometer = BooleanUtils.toBoolean(Integer.valueOf(String.valueOf(tabletSettingsDTO.getFlagCodes().charAt(7))));
				tripEndCrewPhoto = BooleanUtils.toBoolean(Integer.valueOf(String.valueOf(tabletSettingsDTO.getFlagCodes().charAt(8))));

				maskMobileNumberFlag = Integer.valueOf(String.valueOf(tabletSettingsDTO.getFlagCodes().charAt(9)));
				dialerEnabledFlag = Integer.valueOf(String.valueOf(tabletSettingsDTO.getFlagCodes().charAt(10)));
			}

			JSONObject bookingJson = new JSONObject();
			bookingJson.put("discount", discount);
			bookingJson.put("maxDiscountAmount", tabletSettingsDTO.getMaxDiscountAmount());
			bookingJson.put("bookingOpenMinutes", tabletSettingsDTO.getBookingOpenMinutes());
			bookingJson.put("serviceCharge", serviceCharge);
			bookingJson.put("maxServiceChargeAmountPerSeat", tabletSettingsDTO.getMaxServiceChargePerSeat());
			bookingJson.put("optionGST", optionGst);
			bookingJson.put("bookingType", tabletSettingsDTO.getBookingType());
			json.put("booking", bookingJson);

			JSONObject tripJson = new JSONObject();
			JSONObject tripStartJson = new JSONObject();
			tripStartJson.put("photo", tripEndPhoto);
			tripStartJson.put("odometer", tripEndOdometer);
			tripStartJson.put("crewPhoto", tripStartCrewPhoto);
			tripStartJson.put("tripChartOpenMinutes", tabletSettingsDTO.getTripChartOpenMinutes());
			tripJson.put("tripStartFlow", tripStartJson);

			JSONObject tripEndJson = new JSONObject();
			tripEndJson.put("photo", tripStartPhoto);
			tripEndJson.put("odometer", tripStartOdometer);
			tripEndJson.put("crewPhoto", tripEndCrewPhoto);
			tripJson.put("tripEndFlow", tripEndJson);
			json.put("trip", tripJson);

			JSONObject boardingJson = new JSONObject();
			boardingJson.put("maskMobileNumber", maskMobileNumberFlag);
			bookingJson.put("hideBookedTicketFare", tabletSettingsDTO.getHideBookedTicketFare());
			bookingJson.put("tripChartOpenMinutes", tabletSettingsDTO.getTripChartOpenMinutes());
			boardingJson.put("isDialerEnabled", dialerEnabledFlag);
			boardingJson.put("hideBookedTicketFare", tabletSettingsDTO.getHideBookedTicketFare());
			json.put("boarding", boardingJson);
		}
		return json;
	}

	@Override
	public NamespaceTabletSettingsDTO getNamespaceTabletSettings(AuthDTO authDTO) {
		NamespaceTabletSettingsDTO namespaceTabletSettingsDTO = namespaceService.getNamespaceTabletSettings(authDTO);
		if (namespaceTabletSettingsDTO == null) {
			NamespaceTabletSettingsDAO dao = new NamespaceTabletSettingsDAO();
			namespaceTabletSettingsDTO = dao.getNamespaceTabletSettings(authDTO, authDTO.getNamespace());

			/** Default configure */
			if (namespaceTabletSettingsDTO == null) {
				NamespaceDTO namespace = new NamespaceDTO();
				namespace.setCode(ApplicationConfig.getServerZoneCode());
				namespace = namespaceService.getNamespace(namespace);
				namespaceTabletSettingsDTO = dao.getNamespaceTabletSettings(authDTO, namespace);
			}

			namespaceService.putNamespaceTabletSettings(authDTO, namespaceTabletSettingsDTO);
		}
		return namespaceTabletSettingsDTO;
	}

}

package org.in.com.service.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.cache.BusCache;
import org.in.com.cache.EhcacheManager;
import org.in.com.constants.Text;
import org.in.com.dao.BusDAO;
import org.in.com.dao.ScheduleBusDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.BusTypeCategoryDTO;
import org.in.com.dto.BusTypeCategoryDetailsDTO;
import org.in.com.dto.enumeration.BusCategoryTypeEM;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusService;
import org.in.com.utils.StringUtil;
import org.springframework.stereotype.Service;

import net.sf.ehcache.Element;

@Service
public class BusImpl extends BusCache implements BusService {
	private static String CATEGORY_CACHE_KEY = "BUS_TYPE_CAT_CODE";
	private static Map<String, String> categoryCodeMap = null;

	public BusDTO getBus(AuthDTO authDTO, BusDTO busDTO) {
		BusDTO bus = new BusDTO();
		if (busDTO.getId() != 0) {
			bus = getBusDTObyId(authDTO, busDTO);
		}
		else if (StringUtil.isNotNull(busDTO.getCode())) {
			bus = getBusDTO(authDTO, busDTO);
		}
		return bus;
	}

	public String getBusCategoryByCode(String categoryCode) {
		StringBuffer busType = new StringBuffer();
		if (categoryCodeMap == null) {
			categoryCodeMap = new HashMap<String, String>();
			List<BusTypeCategoryDTO> list = getBusTypeCategory();
			for (BusTypeCategoryDTO busTypeCategoryDTO : list) {
				for (BusTypeCategoryDetailsDTO busTypeDTO : busTypeCategoryDTO.getCategoryList()) {
					categoryCodeMap.put(busTypeDTO.getCode(), busTypeDTO.getName());
				}
			}
		}
		if (StringUtil.isNotNull(categoryCode)) {
			for (int i = 0; i < categoryCode.split("\\|").length; i++) {
				if (categoryCodeMap.get(categoryCode.split("\\|")[i]) != null && !categoryCodeMap.get(categoryCode.split("\\|")[i]).equals("Others")) {
					busType.append(categoryCodeMap.get(categoryCode.split("\\|")[i]) + " ");
				}

			}
		}
		return busType.toString();
	}

	public String getBusCategoryUsingEM(String categoryCode) {
		StringBuilder busType = new StringBuilder();
		if (StringUtil.isNotNull(categoryCode)) {
			for (String code : categoryCode.split("\\|")) {
				BusCategoryTypeEM category = BusCategoryTypeEM.getBusCategoryType(code);
				if (category != null && StringUtil.isNotNull(category.getName())) {
					busType.append(Text.SINGLE_SPACE + category.getName());
				}
			}
		}
		return busType.toString().trim();
	}

	public List<BusDTO> get(AuthDTO authDTO, BusDTO dto) {

		BusDAO dao = new BusDAO();
		return dao.getBus(authDTO, dto);
	}

	public List<BusDTO> getAll(AuthDTO authDTO) {
		BusDAO dao = new BusDAO();
		return dao.getBus(authDTO);
	}

	public BusDTO Update(AuthDTO authDTO, BusDTO dto) {
		// Bus used in schedule
		if (dto.getActiveFlag() != 1 && CheckBusmapUsed(authDTO, dto)) {
			throw new ServiceException(ErrorCode.BUSMAP_USED_SCHEDULE);
		}
		BusDAO dao = new BusDAO();
		dao.getBusUID(authDTO, dto);
		removeBusDTO(authDTO, dto);
		return dto;
	}

	private boolean CheckBusmapUsed(AuthDTO authDTO, BusDTO dto) {
		ScheduleBusDAO busDAO = new ScheduleBusDAO();
		return busDAO.CheckBusmapUsed(authDTO, dto);
	}

	public Collection<BusSeatTypeEM> getBusSeatType() {
		return Arrays.asList(BusSeatTypeEM.values());
	}

	public List<BusTypeCategoryDTO> getBusTypeCategory() {
		List<BusTypeCategoryDTO> busTypeCateList = null;
		Element busTypeCategort = EhcacheManager.getBusTypeCategoryCache().get(CATEGORY_CACHE_KEY);
		if (busTypeCategort != null) {
			busTypeCateList = (List<BusTypeCategoryDTO>) busTypeCategort.getObjectValue();
		}
		else {
			BusDAO dao = new BusDAO();
			busTypeCateList = dao.getCategotyDetails();
			EhcacheManager.getBusTypeCategoryCache().put(new Element(CATEGORY_CACHE_KEY, busTypeCateList));
		}
		return busTypeCateList;
	}

	public List<BusSeatLayoutDTO> getBusLayout(AuthDTO authDTO, BusDTO dto) {
		BusDAO dao = new BusDAO();
		return dao.getBusLayout(authDTO, dto);
	}

	public Collection<BusSeatLayoutDTO> getUpdateLayout(AuthDTO authDTO, BusDTO dto, List<BusSeatLayoutDTO> layoutList) {
		BusDAO dao = new BusDAO();
		
		List<BusSeatLayoutDTO> existingLayoutList = dao.getBusLayout(authDTO, dto);
		if (!existingLayoutList.isEmpty()) {
			BusDTO busDTO = new BusDTO();
			busDTO.setBusSeatLayoutDTO(new BusSeatLayoutDTO());
			busDTO.getBusSeatLayoutDTO().setList(existingLayoutList);
		
			Map<String, BusSeatLayoutDTO> seatMap = busDTO.getBusSeatLayoutMapFromList();
			for (BusSeatLayoutDTO layoutDTO : layoutList) {
				if (StringUtil.isNull(layoutDTO.getCode()) || seatMap.get(layoutDTO.getCode()) == null) {
					throw new ServiceException(ErrorCode.BUSMAP_MISSED_MATCHED);
				}
			}
		}
		
		List<BusSeatLayoutDTO> list = dao.getBusLayoutUID(authDTO, dto, layoutList);
		removeBusDTO(authDTO, dto);
		return list;

	}

	@Override
	public void UpdateSeatSequence(AuthDTO authDTO, BusDTO busDTO, List<BusSeatLayoutDTO> busDTOList) {
		busDTO.setId(getBusDTO(authDTO, busDTO).getId());
		if (busDTO.getId() != 0) {
			BusDAO dao = new BusDAO();
			dao.UpdateSeatSequence(authDTO, busDTO, busDTOList);
			removeBusDTO(authDTO, busDTO);
		}
		else {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}
	}

}

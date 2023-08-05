package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.in.com.cache.NamespaceBannerCache;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Text;
import org.in.com.dao.NamespaceBannerDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceBannerDTO;
import org.in.com.dto.NamespaceBannerDetailsDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.service.GroupService;
import org.in.com.service.NamespaceBannerService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class NamespaceBannerServiceImpl implements NamespaceBannerService {
	@Autowired
	GroupService groupService;

	public List<NamespaceBannerDTO> getAll(AuthDTO authDTO) {
		NamespaceBannerDAO bannerDAO = new NamespaceBannerDAO();
		List<NamespaceBannerDTO> namespaceBannerList = bannerDAO.getAllBanner(authDTO);
		for (NamespaceBannerDTO namespaceBannerDTO : namespaceBannerList) {
			for (GroupDTO groupDTO : namespaceBannerDTO.getGroup()) {
				groupService.getGroup(authDTO, groupDTO);
			}
			List<NamespaceBannerDetailsDTO> bannerDetailsList = bannerDAO.getBannerDetails(authDTO, namespaceBannerDTO);
			namespaceBannerDTO.setBannerDetails(bannerDetailsList);
		}
		return namespaceBannerList;
	}

	public NamespaceBannerDTO Update(AuthDTO authDTO, NamespaceBannerDTO bannerDTO) {
		NamespaceBannerDAO bannerDAO = new NamespaceBannerDAO();
		NamespaceBannerCache bannerCache = new NamespaceBannerCache();
		for (GroupDTO groupDTO : bannerDTO.getGroup()) {
			groupService.getGroup(authDTO, groupDTO);
		}
		NamespaceBannerDTO namespaceBannerDTO = bannerDAO.updateBanner(authDTO, bannerDTO);
		bannerCache.removeNamespaceBanner(authDTO);
		return namespaceBannerDTO;
	}

	public NamespaceBannerDTO getNamespaceBanner(AuthDTO authDTO, NamespaceBannerDTO bannerDTO) {
		NamespaceBannerDAO bannerDAO = new NamespaceBannerDAO();
		NamespaceBannerDTO namespaceBannerDTO = bannerDAO.getBanner(authDTO, bannerDTO);
		for (GroupDTO groupDTO : namespaceBannerDTO.getGroup()) {
			groupService.getGroup(authDTO, groupDTO);
		}
		List<NamespaceBannerDetailsDTO> bannerDetailsList = bannerDAO.getBannerDetails(authDTO, namespaceBannerDTO);
		namespaceBannerDTO.setBannerDetails(bannerDetailsList);
		return namespaceBannerDTO;
	}

	public List<NamespaceBannerDTO> get(AuthDTO authDTO, NamespaceBannerDTO bannerDTO) {
		return null;
	}

	public List<NamespaceBannerDTO> getActiveBanner(AuthDTO authDTO) {
		List<NamespaceBannerDTO> bannerRetrunList = new ArrayList<NamespaceBannerDTO>();
		NamespaceBannerCache bannerCache = new NamespaceBannerCache();
		List<NamespaceBannerDTO> bannerList = bannerCache.getBannerAndDetails(authDTO);
		
		List<NamespaceBannerDTO> advertisementBannerList = bannerCache.getAdminBannerAndDetails();
		bannerList.addAll(advertisementBannerList);
		
		DateTime today = DateUtil.NOW().getEndOfDay();
		for (Iterator<NamespaceBannerDTO> iterator = bannerList.iterator(); iterator.hasNext();) {
			NamespaceBannerDTO bannerItr = iterator.next();
			if (StringUtil.isNotNull(bannerItr.getFromDate()) && !today.gteq(bannerItr.getFromDate().getStartOfDay())) {
				iterator.remove();
				continue;
			}
			if (StringUtil.isNotNull(bannerItr.getToDate()) && !today.lteq(bannerItr.getToDate().getEndOfDay())) {
				iterator.remove();
				continue;
			}
			if (bannerItr.getDayOfWeek() != null && bannerItr.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (bannerItr.getDayOfWeek() != null && bannerItr.getDayOfWeek().substring(today.getWeekDay() - 1, today.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			GroupDTO group = BitsUtil.isGroupExists(bannerItr.getGroup(), authDTO.getGroup());
			if (!bannerItr.equals(Text.A_UPPER) && authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && group == null && !bannerItr.getGroup().isEmpty()) {
				iterator.remove();
				continue;
			}
			else if (bannerItr.equals(Text.A_UPPER) && ApplicationConfig.getServerZoneCode().equals(authDTO.getNamespaceCode()) && authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && group == null && !bannerItr.getGroup().isEmpty()) {
				iterator.remove();
				continue;
			}
			DeviceMediumEM medium = BitsUtil.isDeviceMediumExists(bannerItr.getDeviceMedium(), authDTO.getDeviceMedium());
			if (medium == null && !bannerItr.getDeviceMedium().isEmpty()) {
				iterator.remove();
				continue;
			}
			bannerRetrunList.add(bannerItr);
		}
		return bannerRetrunList;
	}

	public NamespaceBannerDTO updateBannerDetails(AuthDTO authDTO, NamespaceBannerDTO namespaceBanner) {
		NamespaceBannerDAO dao = new NamespaceBannerDAO();
		dao.updateBannerDetails(authDTO, namespaceBanner);
		NamespaceBannerCache bannerCache = new NamespaceBannerCache();
		bannerCache.removeNamespaceBanner(authDTO);
		return namespaceBanner;
	}
}

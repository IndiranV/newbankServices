package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.NamespaceBannerCacheDTO;
import org.in.com.cache.dto.NamespaceBannerDetailsCacheDTO;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Text;
import org.in.com.dao.NamespaceBannerDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceBannerDTO;
import org.in.com.dto.NamespaceBannerDetailsDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.MediaTypeEM;
import org.in.com.utils.DateUtil;

import net.sf.ehcache.Element;

public class NamespaceBannerCache {
	private static String CACHEKEY = "BANNER";
	private static String ADS_CACHEKEY = "BANNER_ADS";

	public List<NamespaceBannerDTO> getBannerAndDetails(AuthDTO authDTO) {
		String key = CACHEKEY + Text.UNDER_SCORE + authDTO.getNamespaceCode();
		List<NamespaceBannerCacheDTO> bannerCacheList = null;
		List<NamespaceBannerDTO> bannerList = null;
		Element element = EhcacheManager.getBannerEhCache().get(key);
		if (element != null) {
			bannerCacheList = (List<NamespaceBannerCacheDTO>) element.getObjectValue();
			bannerList = bindBannerFromCacheObject(bannerCacheList);
		}
		else {
			NamespaceBannerDAO bannerDAO = new NamespaceBannerDAO();
			bannerList = bannerDAO.getActiveBanner(authDTO);
			if (bannerList != null && !bannerList.isEmpty()) {
				bannerCacheList = bindBannerToCacheObject(bannerList);
				element = new Element(key, bannerCacheList);
				EhcacheManager.getBannerEhCache().put(element);
			}
		}
		return bannerList;
	}
	
	public List<NamespaceBannerDTO> getAdminBannerAndDetails() {
		String zoneCode = ApplicationConfig.getServerZoneCode();
		
		String key = ADS_CACHEKEY + Text.UNDER_SCORE + zoneCode;
		List<NamespaceBannerCacheDTO> bannerCacheList = null;
		List<NamespaceBannerDTO> bannerList = null;
		Element element = EhcacheManager.getBannerEhCache().get(key);
		if (element != null) {
			bannerCacheList = (List<NamespaceBannerCacheDTO>) element.getObjectValue();
			bannerList = bindBannerFromCacheObject(bannerCacheList);
		}
		else {
			NamespaceBannerDAO bannerDAO = new NamespaceBannerDAO();
			bannerList = bannerDAO.getActiveAdminBanner(zoneCode);
			if (bannerList != null && !bannerList.isEmpty()) {
				bannerCacheList = bindBannerToCacheObject(bannerList);
				element = new Element(key, bannerCacheList);
				EhcacheManager.getBannerEhCache().put(element);
			}
		}
		return bannerList;
	}

	private List<NamespaceBannerDTO> bindBannerFromCacheObject(List<NamespaceBannerCacheDTO> cacheList) {
		List<NamespaceBannerDTO> bannerList = new ArrayList<NamespaceBannerDTO>();
		for (NamespaceBannerCacheDTO bannerCacheDTO : cacheList) {
			NamespaceBannerDTO bannerDTO = new NamespaceBannerDTO();
			bannerDTO.setCode(bannerCacheDTO.getCode());
			bannerDTO.setName(bannerCacheDTO.getName());
			List<GroupDTO> groupList = new ArrayList<GroupDTO>();
			for (int groupId : bannerCacheDTO.getGroupId()) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(groupId);
				groupList.add(groupDTO);
			}
			bannerDTO.setGroup(groupList);
			bannerDTO.setDisplayModel(bannerCacheDTO.getDisplayModel());
			List<DeviceMediumEM> deviceMediumList = new ArrayList<>();
			for (int deviceMediumId : bannerCacheDTO.getDeviceMediumId()) {
				deviceMediumList.add(DeviceMediumEM.getDeviceMediumEM(deviceMediumId));
			}
			bannerDTO.setDeviceMedium(deviceMediumList);
			bannerDTO.setFromDate(DateUtil.getDateTime(bannerCacheDTO.getFromDate()));
			bannerDTO.setToDate(DateUtil.getDateTime(bannerCacheDTO.getToDate()));
			bannerDTO.setDayOfWeek(bannerCacheDTO.getDayOfWeek());
			bannerDTO.setColor(bannerCacheDTO.getColor());
			bannerDTO.setUpdatedAt(DateUtil.getDateTime(bannerCacheDTO.getUdpatedAt()));
			bannerDTO.setActiveFlag(bannerCacheDTO.getActiveFlag());

			List<NamespaceBannerDetailsDTO> bannerDetailsList = new ArrayList<NamespaceBannerDetailsDTO>();
			for (NamespaceBannerDetailsCacheDTO bannerDetailsCacheDTO : bannerCacheDTO.getBannerDetailsList()) {
				NamespaceBannerDetailsDTO bannerDetailsDTO = new NamespaceBannerDetailsDTO();
				bannerDetailsDTO.setCode(bannerDetailsCacheDTO.getCode());
				bannerDetailsDTO.setUrl(bannerDetailsCacheDTO.getUrl());
				bannerDetailsDTO.setRedirectUrl(bannerDetailsCacheDTO.getRedirectUrl());
				bannerDetailsDTO.setAlternateText(bannerDetailsCacheDTO.getAlternateText());
				bannerDetailsDTO.setSequence(bannerDetailsCacheDTO.getSequence());
				bannerDetailsDTO.setMediaType(MediaTypeEM.getMediaTypeEM(bannerDetailsCacheDTO.getMediaTypeCode()));
				bannerDetailsDTO.setActiveFlag(bannerDetailsCacheDTO.getActiveFlag());
				bannerDetailsList.add(bannerDetailsDTO);
			}
			bannerDTO.setBannerDetails(bannerDetailsList);
			bannerList.add(bannerDTO);
		}

		return bannerList;
	}

	private List<NamespaceBannerCacheDTO> bindBannerToCacheObject(List<NamespaceBannerDTO> bannerList) {
		List<NamespaceBannerCacheDTO> cacheList = new ArrayList<NamespaceBannerCacheDTO>();
		for (NamespaceBannerDTO bannerDTO : bannerList) {
			NamespaceBannerCacheDTO bannerCacheDTO = new NamespaceBannerCacheDTO();
			bannerCacheDTO.setCode(bannerDTO.getCode());
			bannerCacheDTO.setName(bannerDTO.getName());
			bannerCacheDTO.setFromDate(DateUtil.convertDate(bannerDTO.getFromDate()));
			bannerCacheDTO.setToDate(DateUtil.convertDate(bannerDTO.getToDate()));
			bannerCacheDTO.setDayOfWeek(bannerDTO.getDayOfWeek());
			bannerCacheDTO.setColor(bannerDTO.getColor());
			bannerCacheDTO.setDisplayModel(bannerDTO.getDisplayModel());
			List<Integer> groupIds = new ArrayList<>();
			for (GroupDTO groupDTO : bannerDTO.getGroup()) {
				groupIds.add(groupDTO.getId());
			}
			bannerCacheDTO.setGroupId(groupIds);

			List<Integer> deviceMediumIds = new ArrayList<>();
			for (DeviceMediumEM deviceMedium : bannerDTO.getDeviceMedium()) {
				deviceMediumIds.add(deviceMedium.getId());
			}
			bannerCacheDTO.setDeviceMediumId(deviceMediumIds);
			bannerCacheDTO.setUdpatedAt(DateUtil.convertDateTime(bannerDTO.getUpdatedAt()));
			bannerCacheDTO.setActiveFlag(bannerDTO.getActiveFlag());

			List<NamespaceBannerDetailsCacheDTO> bannerDetailsCacheList = new ArrayList<NamespaceBannerDetailsCacheDTO>();
			for (NamespaceBannerDetailsDTO bannerDetailsDTO : bannerDTO.getBannerDetails()) {
				NamespaceBannerDetailsCacheDTO bannerDetailsCacheDTO = new NamespaceBannerDetailsCacheDTO();
				bannerDetailsCacheDTO.setCode(bannerDetailsDTO.getCode());
				bannerDetailsCacheDTO.setUrl(bannerDetailsDTO.getUrl());
				bannerDetailsCacheDTO.setRedirectUrl(bannerDetailsDTO.getRedirectUrl());
				bannerDetailsCacheDTO.setAlternateText(bannerDetailsDTO.getAlternateText());
				bannerDetailsCacheDTO.setSequence(bannerDetailsDTO.getSequence());
				bannerDetailsCacheDTO.setMediaTypeCode(bannerDetailsDTO.getMediaType() != null ? bannerDetailsDTO.getMediaType().getCode() : Text.NA);
				bannerDetailsCacheDTO.setActiveFlag(bannerDetailsDTO.getActiveFlag());
				bannerDetailsCacheList.add(bannerDetailsCacheDTO);
			}
			bannerCacheDTO.setBannerDetailsList(bannerDetailsCacheList);
			cacheList.add(bannerCacheDTO);
		}
		return cacheList;
	}

	public void removeNamespaceBanner(AuthDTO authDTO) {
		String key = CACHEKEY + Text.UNDER_SCORE + authDTO.getNamespaceCode();
		String adsKey = ADS_CACHEKEY + Text.UNDER_SCORE + authDTO.getNamespaceCode();
		EhcacheManager.getBannerEhCache().remove(key);
		EhcacheManager.getBannerEhCache().remove(adsKey);
	}

}

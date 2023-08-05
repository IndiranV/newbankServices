package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.AppStoreDetailsCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.UserDAO;
import org.in.com.dto.AppStoreDetailsDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.utils.StringUtil;

import net.sf.ehcache.Element;

public class AppStoreDetailsCache {
	private static String CACHEKEY = "APP_";

	protected List<AppStoreDetailsDTO> getAppstoreByUser(AuthDTO authDTO, UserDTO userDTO) {
		String cacheKey = CACHEKEY + authDTO.getNamespace().getId() + Text.UNDER_SCORE + userDTO.getCode();
		List<AppStoreDetailsDTO> appstoreList = new ArrayList<AppStoreDetailsDTO>();
		Element element = EhcacheManager.getAppStoreDetailsEhCache().get(cacheKey);
		if (element != null) {
			List<AppStoreDetailsCacheDTO> cacheList = (List<AppStoreDetailsCacheDTO>) element.getObjectValue();
			bindAppstoreFromCache(cacheList, appstoreList);
		}
		else {
			UserDAO userDAO = new UserDAO();
			appstoreList = userDAO.getAppStoreDetails(authDTO, userDTO);
			if (!appstoreList.isEmpty() && StringUtil.isNotNull(userDTO.getCode())) {
				List<AppStoreDetailsCacheDTO> cacheList = bindAppstoreToCache(appstoreList);
				element = new Element(cacheKey, cacheList);
				EhcacheManager.getAppStoreDetailsEhCache().put(element);
			}
		}
		return appstoreList;
	}

	private List<AppStoreDetailsCacheDTO> bindAppstoreToCache(List<AppStoreDetailsDTO> appStoreList) {
		List<AppStoreDetailsCacheDTO> cacheList = new ArrayList<AppStoreDetailsCacheDTO>();
		for (AppStoreDetailsDTO appstoreDetails : appStoreList) {
			AppStoreDetailsCacheDTO appstoreCacheDTO = new AppStoreDetailsCacheDTO();
			appstoreCacheDTO.setCode(appstoreDetails.getCode());
			appstoreCacheDTO.setDeviceMedium(appstoreDetails.getDeviceMedium().getCode());
			appstoreCacheDTO.setModel(appstoreDetails.getModel());
			appstoreCacheDTO.setOs(appstoreDetails.getOs());
			appstoreCacheDTO.setUdid(appstoreDetails.getUdid());
			appstoreCacheDTO.setGcmToken(appstoreDetails.getGcmToken());
			appstoreCacheDTO.setActiveFlag(appstoreDetails.getActiveFlag());
			cacheList.add(appstoreCacheDTO);
		}
		return cacheList;
	}

	private void bindAppstoreFromCache(List<AppStoreDetailsCacheDTO> appstoreCacheList, List<AppStoreDetailsDTO> appstoreList) {
		for (AppStoreDetailsCacheDTO cacheDTO : appstoreCacheList) {
			AppStoreDetailsDTO appstoreDetails = new AppStoreDetailsDTO();
			appstoreDetails.setCode(cacheDTO.getCode());
			appstoreDetails.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(cacheDTO.getDeviceMedium()));
			appstoreDetails.setModel(cacheDTO.getModel());
			appstoreDetails.setOs(cacheDTO.getOs());
			appstoreDetails.setUdid(cacheDTO.getUdid());
			appstoreDetails.setGcmToken(cacheDTO.getGcmToken());
			appstoreDetails.setActiveFlag(cacheDTO.getActiveFlag());
			appstoreList.add(appstoreDetails);
		}
	}

	protected List<AppStoreDetailsDTO> getAppstoreByUserId(AuthDTO authDTO, UserDTO userDTO) {
		String groupCode = null;
		String key = CACHEKEY + authDTO.getNamespace().getId() + Text.UNDER_SCORE + userDTO.getId();
		Element elementKey = EhcacheManager.getAppStoreDetailsEhCache().get(key);
		if (elementKey != null) {
			groupCode = (String) elementKey.getObjectValue();
			userDTO.setCode(groupCode);
		}
		List<AppStoreDetailsDTO> appstoreList = getAppstoreByUser(authDTO, userDTO);
		if (elementKey == null && !appstoreList.isEmpty() && userDTO.getId() != 0 && StringUtil.isNotNull(userDTO.getCode())) {
			key = CACHEKEY + authDTO.getNamespace().getId() + Text.UNDER_SCORE + userDTO.getId();
			elementKey = new Element(key, userDTO.getCode());
			EhcacheManager.getAppStoreDetailsEhCache().put(elementKey);
		}
		return appstoreList;
	}

	protected void removeAppStoreDetailsCache(AuthDTO authDTO, UserDTO userDTO) {
		String cacheKey = CACHEKEY + authDTO.getNamespace().getId() + Text.UNDER_SCORE + userDTO.getCode();
		EhcacheManager.getAppStoreDetailsEhCache().remove(cacheKey);
	}

}

package org.in.com.listener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

@Component
final class ScheduleActivityCacheEventListener implements CacheEventListener {
	@Autowired
	NotificationService notificationService;
	@Autowired
	SearchService searchService;
	@Autowired
	NotificationPushService notificationPushService;

	@Override
	public void notifyElementRemoved(Ehcache cache, Element element) throws CacheException {
		if (element.getObjectKey() != null && element.getObjectValue() != null) {
			String scheduleCode = (String) element.getObjectKey();
			List<Map<String, String>> activityLog = (List<Map<String, String>>) element.getObjectValue();
			notifyScheduleActivity(scheduleCode, activityLog);
		}
	}

	@Override
	public void notifyElementPut(Ehcache cache, Element element) throws CacheException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyElementUpdated(Ehcache cache, Element element) throws CacheException {
		// TODO Auto-generated method stub

	}

	@Override
	public void notifyElementExpired(Ehcache cache, Element element) {
		if (element.getObjectKey() != null && element.getObjectValue() != null) {
			String scheduleCode = (String) element.getObjectKey();
			List<Map<String, String>> activityLog = (List<Map<String, String>>) element.getObjectValue();
			notifyScheduleActivity(scheduleCode, activityLog);
		}
	}

	@Override
	public void notifyElementEvicted(Ehcache cache, Element element) {
		if (element.getObjectKey() != null && element.getObjectValue() != null) {
			String scheduleCode = (String) element.getObjectKey();
			List<Map<String, String>> activityLog = (List<Map<String, String>>) element.getObjectValue();
			notifyScheduleActivity(scheduleCode, activityLog);
		}
	}

	@Override
	public void notifyRemoveAll(Ehcache cache) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Singleton instance");
	}

	private void notifyScheduleActivity(String scheduleCode, List<Map<String, String>> activityLog) {
		Map<String, List<Map<String, String>>> finalScheduleChanges = new HashMap<>();
		for (Map<String, String> activityLogMap : activityLog) {
			String key = activityLogMap.get("namespaceCode") + "_" + scheduleCode;
			if (finalScheduleChanges.get(key) == null) {
				List<Map<String, String>> scheduleLogList = new ArrayList<>();
				scheduleLogList.add(activityLogMap);
				finalScheduleChanges.put(key, scheduleLogList);
			}
			else if (finalScheduleChanges.get(key) != null) {
				List<Map<String, String>> scheduleLogList = finalScheduleChanges.get(key);
				scheduleLogList.add(activityLogMap);
				finalScheduleChanges.put(key, scheduleLogList);
			}
		}

		for (Entry<String, List<Map<String, String>>> entry : finalScheduleChanges.entrySet()) {
			AuthDTO authDTO = new AuthDTO();
			authDTO.setNamespaceCode(entry.getKey().split("_")[0]);
			String referenceCode = entry.getKey().split("_")[1];

			// Sort
			Comparator<Map<String, String>> comp = new BeanComparator("updatedAt");
			Collections.sort(entry.getValue(), comp);

			notificationService.sendScheduleUpdateEmail(authDTO, entry.getValue(), referenceCode);
			
			notificationPushService.pushScheduleEditNotification(authDTO, referenceCode, entry.getValue());
			// Update RB OTA
			if (authDTO.getNamespace().getProfile().getOtaPartnerCode().get(UserTagEM.API_USER_RB.getCode()) != null) {
				ScheduleDTO schedule = new ScheduleDTO();
				schedule.setCode(referenceCode);
				searchService.pushInventoryChangesEvent(authDTO, schedule);
			}
		}
	}
}

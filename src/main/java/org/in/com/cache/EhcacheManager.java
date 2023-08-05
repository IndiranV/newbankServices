package org.in.com.cache;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

public class EhcacheManager {
	// JVM cache Manage
	public static CacheManager cacheManager;
	// Session
	public static Cache authTokenEhCache;
	public static Cache apiTokenEhCache;
	public static Cache guestAuthTokenEhCache;
	public static Cache userMenuEhCache;

	// Master
	public static Cache stateEhCache;
	public static Cache stationEhCache;
	public static Cache menuEhCache;
	public static Cache namespaceEhCache;
	public static Cache amenitiesCache;
	public static Cache userCache;
	public static Cache cancellationTermsCache;
	public static Cache busCache;
	public static Cache busTypeCategoryCache;
	public static Cache groupCache;
	public static Cache organizationCache;
	public static Cache namespaceTaxCache;
	public static Cache fareRuleCache;
	public static Cache driverCache;
	public static Cache attendantCache;
	public static Cache bannerCache;
	public static Cache namespaceIntegrationEhCache;
	public static Cache scheduleTagEhCache;
	public static Cache busBreakevenEhCache;
	public static Cache stateFuelCache;
	public static Cache sectorEhCache;
	public static Cache notificationSubscriptionCache;
	public static Cache smsTemplateConfigCache;
	public static Cache eventNotificationConfigCache;
	public static Cache calendarAnnouncementEhCache;
	public static Cache termEhCache;
	public static Cache specialDiscountCriteriaEhCache;
	public static Cache busBuddyEhCache;

	// Inventory
	public static Cache tripEhCache;
	public static Cache tripStageEhCache;
	public static Cache tripQuotaSeatEhCache;
	public static Cache stationPointEhCache;
	public static Cache commerceCache;
	public static Cache scheduleCache;
	public static Cache scheduleTripDPCache;
	public static Cache searchStageCache;
	public static Cache scheduleFareTemplateCache;
	public static Cache scheduleTripStageFareCache;
	public static Cache scheduleActivityEhCache;
	public static Cache activeScheduleEhCache;
	public static Cache activeScheduleDetailsEhCache;

	// Transaction
	public static Cache passengerDetailsCache;
	public static Cache pgTransactionCache;
	public static Cache hdfcCache;
	public static Cache freshTransactionCache;
	public static Cache freshSMSNotificationCache;
	public static Cache OTPCache;
	public static Cache ticketAfterTripTimeCache;
	public static Cache ticketNotBoardedCache;
	public static Cache transactionValidityEhCache;
	public static Cache ticketEventEhCache;
	public static Cache appStoreDetailsCache;

	public static void InitCacheManager(URL url) {
		if (cacheManager == null) {
			cacheManager = new CacheManager(url);
		}
	}

	public static CacheManager getCacheManager() {
		return cacheManager;
	}

	public static Cache getAuthTokenEhCache() {
		if (authTokenEhCache == null) {
			authTokenEhCache = getCacheManager().getCache("authTokenCache");
		}
		return authTokenEhCache;
	}

	public static Cache getStateEhCache() {
		if (stateEhCache == null) {
			stateEhCache = getCacheManager().getCache("stateCache");
		}
		return stateEhCache;
	}

	public static Cache getStationEhCache() {
		if (stationEhCache == null) {
			stationEhCache = getCacheManager().getCache("stationCache");
		}
		return stationEhCache;
	}

	public static Cache getTripEhCache() {
		if (tripEhCache == null) {
			tripEhCache = getCacheManager().getCache("tripCache");
		}
		return tripEhCache;
	}

	public static Cache getStationPointEhCache() {
		if (stationPointEhCache == null) {
			stationPointEhCache = getCacheManager().getCache("stationPointCache");
		}
		return stationPointEhCache;
	}

	public static Cache getMenuEhCache() {
		if (menuEhCache == null) {
			menuEhCache = getCacheManager().getCache("menuCache");
		}
		return menuEhCache;
	}

	public static Cache getNamespaceEhCache() {
		if (namespaceEhCache == null) {
			namespaceEhCache = getCacheManager().getCache("namespaceCache");
		}
		return namespaceEhCache;
	}

	public static Cache getCommerceStaticEhCache() {
		if (commerceCache == null) {
			commerceCache = getCacheManager().getCache("commerceCache");
		}
		return commerceCache;
	}

	public static Cache getAmenitiesEhCache() {
		if (amenitiesCache == null) {
			amenitiesCache = getCacheManager().getCache("amenitiesCache");
		}
		return amenitiesCache;
	}

	public static Cache getCancellationTermsEhCache() {
		if (cancellationTermsCache == null) {
			cancellationTermsCache = getCacheManager().getCache("cancellationTermsCache");
		}
		return cancellationTermsCache;
	}

	public static Cache getBusEhCache() {
		if (busCache == null) {
			busCache = getCacheManager().getCache("busCache");
		}
		return busCache;
	}

	public static Cache getUserEhCache() {
		if (userCache == null) {
			userCache = getCacheManager().getCache("userCache");
		}
		return userCache;
	}

	public static Cache getGroupEhCache() {
		if (groupCache == null) {
			groupCache = getCacheManager().getCache("groupCache");
		}
		return groupCache;
	}

	public static Cache getOrganizationEhCache() {
		if (organizationCache == null) {
			organizationCache = getCacheManager().getCache("organizationCache");
		}
		return organizationCache;
	}

	public static Cache getScheduleEhCache() {
		if (scheduleCache == null) {
			scheduleCache = getCacheManager().getCache("scheduleCache");
		}
		return scheduleCache;
	}

	public static Cache getScheduleTripDPEhCache() {
		if (scheduleTripDPCache == null) {
			scheduleTripDPCache = getCacheManager().getCache("scheduleTripDPCache");
		}
		return scheduleTripDPCache;
	}

	public static Cache getBookingCache() {
		if (passengerDetailsCache == null) {
			passengerDetailsCache = getCacheManager().getCache("passengerDetailsCache");
		}
		return passengerDetailsCache;
	}

	public static Cache getPGTransactionCache() {
		if (pgTransactionCache == null) {
			pgTransactionCache = getCacheManager().getCache("pgTransactionCache");
		}
		return pgTransactionCache;
	}

	public static Cache getHdfcCache() {
		if (hdfcCache == null) {
			hdfcCache = getCacheManager().getCache("hdfcCache");
		}
		return hdfcCache;
	}

	public static Cache getGuestAuthTokenEhCache() {
		if (guestAuthTokenEhCache == null) {
			guestAuthTokenEhCache = getCacheManager().getCache("guestAuthTokenEhCache");
		}
		return guestAuthTokenEhCache;
	}

	public static Cache getFreshTransactionEhCache() {
		if (freshTransactionCache == null) {
			freshTransactionCache = getCacheManager().getCache("freshTransactionCache");
		}
		return freshTransactionCache;
	}

	public static Cache getFreshRequestEhCache() {
		if (freshSMSNotificationCache == null) {
			freshSMSNotificationCache = getCacheManager().getCache("freshSMSNotificationCache");
		}
		return freshSMSNotificationCache;
	}

	public static Cache getBusTypeCategoryCache() {
		if (busTypeCategoryCache == null) {
			busTypeCategoryCache = getCacheManager().getCache("busTypeCategoryCache");
		}
		return busTypeCategoryCache;
	}

	public static Cache getOTPCache() {
		if (OTPCache == null) {
			OTPCache = getCacheManager().getCache("OTPCache");
		}
		return OTPCache;
	}

	public static Cache getTicketAfterTripTimeCache() {
		if (ticketAfterTripTimeCache == null) {
			ticketAfterTripTimeCache = getCacheManager().getCache("ticketAfterTripTimeCache");
		}
		return ticketAfterTripTimeCache;
	}

	public static Cache getNamespaceTaxCache() {
		if (namespaceTaxCache == null) {
			namespaceTaxCache = getCacheManager().getCache("namespaceTaxCache");
		}
		return namespaceTaxCache;
	}

	public static Cache getSearchStageCache() {
		if (searchStageCache == null) {
			searchStageCache = getCacheManager().getCache("searchStageCache");
		}
		return searchStageCache;
	}

	public static Cache getTripQuotaSeatCache() {
		if (tripQuotaSeatEhCache == null) {
			tripQuotaSeatEhCache = getCacheManager().getCache("tripQuotaSeatCache");
		}
		return tripQuotaSeatEhCache;
	}

	public static Cache getTicketNotBoardedCache() {
		if (ticketNotBoardedCache == null) {
			ticketNotBoardedCache = getCacheManager().getCache("ticketNotBoardedCache");
		}
		return ticketNotBoardedCache;
	}

	public static Cache getScheduleActivityCache() {
		if (scheduleActivityEhCache == null) {
			scheduleActivityEhCache = getCacheManager().getCache("scheduleActivityCache");
		}
		return scheduleActivityEhCache;
	}

	public static Cache getTransactionValidityEhCache() {
		if (transactionValidityEhCache == null) {
			transactionValidityEhCache = getCacheManager().getCache("transactionValidityCache");
		}
		return transactionValidityEhCache;
	}

	public static Cache getTicketEventEhCache() {
		if (ticketEventEhCache == null) {
			ticketEventEhCache = getCacheManager().getCache("ticketEventCache");
		}
		return ticketEventEhCache;
	}

	public static Cache getFareRuleCache() {
		if (fareRuleCache == null) {
			fareRuleCache = getCacheManager().getCache("fareRuleCache");
		}
		return fareRuleCache;
	}

	public static Cache getDriverCache() {
		if (driverCache == null) {
			driverCache = getCacheManager().getCache("driverCache");
		}
		return driverCache;
	}

	public static Cache getAttendantCache() {
		if (attendantCache == null) {
			attendantCache = getCacheManager().getCache("attendantCache");
		}
		return attendantCache;
	}

	public static Cache getBannerEhCache() {
		if (bannerCache == null) {
			bannerCache = getCacheManager().getCache("bannerCache");
		}
		return bannerCache;
	}

	public static Cache getBusBuddyEhCache() {
		if (busBuddyEhCache == null) {
			busBuddyEhCache = getCacheManager().getCache("busBuddyCache");
		}
		return busBuddyEhCache;
	}

	public static Cache getNamespaceIntegrationEhCache() {
		if (namespaceIntegrationEhCache == null) {
			namespaceIntegrationEhCache = getCacheManager().getCache("namespaceIntegrationCache");
		}
		return namespaceIntegrationEhCache;
	}

	public static Cache getScheduleTagEhCache() {
		if (scheduleTagEhCache == null) {
			scheduleTagEhCache = getCacheManager().getCache("scheduleTagCache");
		}
		return scheduleTagEhCache;
	}

	public static Cache getBusBreakevenEhCache() {
		if (busBreakevenEhCache == null) {
			busBreakevenEhCache = getCacheManager().getCache("busBreakevenCache");
		}
		return busBreakevenEhCache;
	}

	public static Cache getStateFuelEhCache() {
		if (stateFuelCache == null) {
			stateFuelCache = getCacheManager().getCache("stateFuelCache");
		}
		return stateFuelCache;
	}

	public static Cache getScheduleFareTemplateCache() {
		if (scheduleFareTemplateCache == null) {
			scheduleFareTemplateCache = getCacheManager().getCache("scheduleFareTemplateCache");
		}
		return scheduleFareTemplateCache;
	}

	public static Cache getscheduleTripStageFareEhCache() {
		if (scheduleTripStageFareCache == null) {
			scheduleTripStageFareCache = getCacheManager().getCache("scheduleTripStageFareCache");
		}
		return scheduleTripStageFareCache;
	}

	public static Cache getAppStoreDetailsEhCache() {
		if (appStoreDetailsCache == null) {
			appStoreDetailsCache = getCacheManager().getCache("appStoreDetailsCache");
		}
		return appStoreDetailsCache;
	}

	public static Cache getNotificationSubscriptionEhCache() {
		if (notificationSubscriptionCache == null) {
			notificationSubscriptionCache = getCacheManager().getCache("notificationSubscriptionCache");
		}
		return notificationSubscriptionCache;
	}

	public static Cache getSMSTemplateConfigCache() {
		if (smsTemplateConfigCache == null) {
			smsTemplateConfigCache = getCacheManager().getCache("smsTemplateConfigCache");
		}
		return smsTemplateConfigCache;
	}

	public static Cache getSectorCache() {
		if (sectorEhCache == null) {
			sectorEhCache = getCacheManager().getCache("sectorCache");
		}
		return sectorEhCache;
	}

	public static Cache getUserMenuCache() {
		if (userMenuEhCache == null) {
			userMenuEhCache = getCacheManager().getCache("userMenuCache");
		}
		return userMenuEhCache;
	}

	public static Cache getEventNotificationConfigCache() {
		if (eventNotificationConfigCache == null) {
			eventNotificationConfigCache = getCacheManager().getCache("eventNotificationConfigCache");
		}
		return eventNotificationConfigCache;
	}

	public static Cache getTermEhCache() {
		if (termEhCache == null) {
			termEhCache = getCacheManager().getCache("termCache");
		}
		return termEhCache;
	}

	public static Cache getSpecialDiscountCriteriaEhCache() {
		if (specialDiscountCriteriaEhCache == null) {
			specialDiscountCriteriaEhCache = getCacheManager().getCache("specialDiscountCriteriaCache");
		}
		return specialDiscountCriteriaEhCache;
	}

	public static Cache getCalendarAnnouncementEhCache() {
		if (calendarAnnouncementEhCache == null) {
			calendarAnnouncementEhCache = getCacheManager().getCache("calendarAnnouncementCache");
		}
		return calendarAnnouncementEhCache;
	}

	public static Cache getActiveScheduleEhCache() {
		if (activeScheduleEhCache == null) {
			activeScheduleEhCache = getCacheManager().getCache("activeScheduleEhCache");
		}
		return activeScheduleEhCache;
	}

	@SuppressWarnings("unchecked")
	public static List<String> getKeys(Cache ehcache, String keyword) {
		return (List<String>) ehcache.getKeys().stream().filter(o -> o instanceof String).filter(o -> ((String) o).contains(keyword)).collect(Collectors.toList());
	}
}

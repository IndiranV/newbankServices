package org.in.com.controller.api;

import org.in.com.cache.EhcacheManager;
import org.in.com.config.ApplicationConfig;
import org.in.com.controller.api.io.ResponseIO;
import org.in.com.controller.web.BaseController;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;
import org.joda.time.DateTime;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

@Controller
@RequestMapping("/api/json/{accessToken}/cache")
public class CacheController extends BaseController {

	@RequestMapping(value = "/ehcache/{eventCode}/{cacheName}", method = { RequestMethod.POST, RequestMethod.GET }, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<String> evaluateCache(@PathVariable("accessToken") String accessToken, @PathVariable("eventCode") String eventCode, @PathVariable("cacheName") String cacheName, String cacheKey) throws Exception {

		validateMandatory(accessToken);

		if (StringUtil.isNull(eventCode) || StringUtil.isNull(cacheName)) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
		}

		StringBuilder builder = new StringBuilder();
		Gson gson = new Gson();
		if (eventCode.equalsIgnoreCase("VIEWKEYS")) {
			Cache cache = EhcacheManager.getCacheManager().getCache(cacheName);
			builder.append("<table align='center' border='1px'>");
			for (Object key : cache.getKeys()) {
				Element element = cache.get(key);
				if (element != null) {
					builder.append("<tr>");
					builder.append("<td>" + element.getObjectKey() + "</td><td>" + element.getSerializedSize() + "</td><td>" + new DateTime(element.getCreationTime()).toString() + "</td><td>" + new DateTime(element.getExpirationTime()).toString() + " </td>");
					builder.append("</tr>");
				}
			}
			builder.append("</table>");

		}
		else if (eventCode.equalsIgnoreCase("GETVALUE")) {
			if (StringUtil.isNull(cacheKey)) {
				throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
			}

			Cache cache = EhcacheManager.getCacheManager().getCache(cacheName);
			Element element = cache.get(cacheKey);
			if (element != null) {
				gson.toJson(element.getObjectValue());
				builder.append(gson.toJson(element.getObjectValue()));
			}
		}
		else if (eventCode.equalsIgnoreCase("CHECKKEYS")) {
			Cache cache = EhcacheManager.getCacheManager().getCache(cacheName);
			if (cache == null) {
				throw new ServiceException(ErrorCode.INVALID_CODE);
			}
			cache.evictExpiredElements();
		}
		else if (eventCode.equalsIgnoreCase("DELKEY")) {
			if (StringUtil.isNull(cacheKey)) {
				throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
			}
			Cache cache = EhcacheManager.getCacheManager().getCache(cacheName);
			cache.remove(cacheKey);
		}

		return ResponseIO.success(builder.toString());
	}

	private void validateMandatory(String accessToken) {
		NamespaceZoneEM namespaceZone = NamespaceZoneEM.getNamespaceZoneEM(ApplicationConfig.getServerZoneCode());
		if (StringUtil.isNull(accessToken) || namespaceZone == null || !accessToken.equals(namespaceZone.getToken())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}

	}
}

package org.in.com.listener;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;

import org.in.com.cache.AuthCache;
import org.in.com.cache.dto.AuthCacheDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
final class AuthCacheEventListener implements CacheEventListener {
	@Autowired
	AuthService authService;

	@Override
	public void notifyElementRemoved(final Ehcache cache, final Element element) throws CacheException {
		if (element.getObjectKey() != null && element.getObjectValue() != null) {
			String key = (String) element.getObjectKey();
			AuthCacheDTO authCache = (AuthCacheDTO) element.getObjectValue();
			elementSessionUpdate(authCache);
		}
	}

	@Override
	public void notifyElementPut(final Ehcache cache, final Element element) throws CacheException {

	}

	@Override
	public void notifyElementUpdated(final Ehcache cache, final Element element) throws CacheException {

	}

	@Override
	public void notifyElementExpired(final Ehcache cache, final Element element) {
		if (element.getObjectKey() != null && element.getObjectValue() != null) {
			String key = (String) element.getObjectKey();
			AuthCacheDTO authCache = (AuthCacheDTO) element.getObjectValue();
			elementSessionUpdate(authCache);
		}
	}

	@Override
	public void notifyElementEvicted(final Ehcache cache, final Element element) {
		if (element.getObjectKey() != null && element.getObjectValue() != null) {
			String key = (String) element.getObjectKey();
			AuthCacheDTO authCache = (AuthCacheDTO) element.getObjectValue();
			elementSessionUpdate(authCache);
			System.out.println("notifyElementEvicted auth: " + key);
		}

	}

	@Override
	public void notifyRemoveAll(final Ehcache cache) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Singleton instance");
	}

	private void elementSessionUpdate(AuthCacheDTO authCache) {
		AuthCache cache = new AuthCache();
		AuthDTO authDTO = cache.bindAuthFromCacheObject(authCache);
		authService.updateUserLoginSession(authDTO);
	}

}

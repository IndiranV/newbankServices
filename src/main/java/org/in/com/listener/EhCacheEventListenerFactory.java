package org.in.com.listener;

import java.util.Properties;

import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class EhCacheEventListenerFactory extends CacheEventListenerFactory {

	public static AuthCacheEventListener authCacheEventListener;
	public static TicketCacheEventListener ticketCacheEventListener;
	public static ScheduleActivityCacheEventListener scheduleActivityCacheEventListener;

	@Override
	public CacheEventListener createCacheEventListener(final Properties properties) {
		System.out.println(properties.toString());
		CacheEventListener eventListener = null;
		if (properties.get("bean").equals("authCacheEventListener")) {
			eventListener = getAuthCacheEventListener();
		}
		else if (properties.get("bean").equals("ticketCacheEventListener")) {
			eventListener = getTicketCacheEventListener();
		}
		else if (properties.get("bean").equals("scheduleActivityCacheEventListener")) {
			eventListener = getScheduleActivityCacheEventListener();
		}
		return eventListener;
	}

	@Bean
	public AuthCacheEventListener getAuthCacheEventListener() {
		if (authCacheEventListener == null) {
			authCacheEventListener = new AuthCacheEventListener();
		}
		return authCacheEventListener;
	}

	@Bean
	public TicketCacheEventListener getTicketCacheEventListener() {
		if (ticketCacheEventListener == null) {
			ticketCacheEventListener = new TicketCacheEventListener();
		}
		return ticketCacheEventListener;
	}

	@Bean
	public ScheduleActivityCacheEventListener getScheduleActivityCacheEventListener() {
		if (scheduleActivityCacheEventListener == null) {
			scheduleActivityCacheEventListener = new ScheduleActivityCacheEventListener();
		}
		return scheduleActivityCacheEventListener;
	}

}
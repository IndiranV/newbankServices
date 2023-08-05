package org.in.com.listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import lombok.extern.java.Log;

import org.in.com.aggregator.sms.SmsClient;
import org.in.com.aggregator.whatsapp.WhatsappClient;
import org.in.com.cache.EhcacheManager;
import org.in.com.config.ApplicationConfig;
import org.in.com.service.AmentiesService;
import org.in.com.service.BusService;
import org.in.com.service.MenuService;
import org.in.com.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Log
public class ContextListener {

	@Autowired
	MenuService menuService;
	@Autowired
	StateService stateService;
	@Autowired
	BusService busService;
	@Autowired
	AmentiesService amentiesService;

	@Bean
	public String contextInitialized() {
		log.info("Bus Service Listener Is Initialized...!");
		// Initial Eh-cache start
		URL Ehcacheurl = ContextListener.class.getResource("/ehcache.xml");
		EhcacheManager.InitCacheManager(Ehcacheurl);
		return "";
	}

	@Bean
	public String loadMenu() {
		menuService.reload();
		return "";
	}

	@Bean
	public String loadAmenitiesType() {
		amentiesService.reloadAmenties();
		return "";
	}

	@Bean
	public String loadServerEnv() {
		Properties props = new Properties();
		try {
			File configDir = new File(System.getProperty("catalina.base"), "conf");
			File configFile = new File(configDir, "application.properties");
			InputStream stream;
			stream = new FileInputStream(configFile);
			props.load(stream);
		}
		catch (Exception e) {
			log.info("unable to load Server Env");
		}
		finally {
			for (String key : props.stringPropertyNames()) {
				ApplicationConfig.CONFIGMAP.put(key, props.getProperty(key));
			}
		}
		return "";
	}

	@Bean
	public String loadSMSConfig() {
		Properties props = new Properties();
		try {
			File configDir = new File(System.getProperty("catalina.base"), "conf");
			File configFile = new File(configDir, "sms.properties");
			InputStream stream;
			stream = new FileInputStream(configFile);
			props.load(stream);
			log.info("SMS Properties loaded from Server");
		}
		catch (FileNotFoundException fe) {
			try {
				props.load(this.getClass().getResourceAsStream("/sms.properties"));
				log.info("SMS Properties loaded from Application");
			}
			catch (Exception e) {
				log.info("unable to load SMS Properties");
			}
		}
		catch (Exception e) {
			log.info("unable to load SMS Properties");
		}
		finally {
			for (String key : props.stringPropertyNames()) {
				SmsClient.config.put(key, props.getProperty(key));
			}
		}
		return "";
	}
	
	@Bean
	public String loadWhatsappConfig() {
		Properties props = new Properties();
		try {
			File configDir = new File(System.getProperty("catalina.base"), "conf");
			File configFile = new File(configDir, "whatsapp.properties");
			InputStream stream;
			stream = new FileInputStream(configFile);
			props.load(stream);
			log.info("Whatsapp Properties loaded from Server");
		}
		catch (FileNotFoundException fe) {
			try {
				props.load(this.getClass().getResourceAsStream("/whatsapp.properties"));
				log.info("Whatsapp Properties loaded from Application");
			}
			catch (Exception e) {
				log.info("unable to load Whatsapp Properties");
			}
		}
		catch (Exception e) {
			log.info("unable to load Whatsapp Properties");
		}
		finally {
			for (String key : props.stringPropertyNames()) {
				WhatsappClient.config.put(key, props.getProperty(key));
			}
		}
		return "";
	}
}

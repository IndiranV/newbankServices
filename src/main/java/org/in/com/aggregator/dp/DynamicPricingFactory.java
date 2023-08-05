package org.in.com.aggregator.dp;

import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DynamicPricingFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(DynamicPricingFactory.class);

	public static DynamicPricingFactoryInterface returnDPInstance(String providerImpl) {
		DynamicPricingFactoryInterface instance = null;
		String dpClassName = "org.in.com.aggregator.dp.impl." + providerImpl;
		try {
			Class<?> dpClass = Class.forName(dpClassName);
			instance = (DynamicPricingFactoryInterface) dpClass.newInstance();
		}
		catch (ClassNotFoundException e) {
			LOGGER.error("{} does not exist ,please create one in the same package,if exists check for the class name", e, dpClassName);
			throw new ServiceException(ErrorCode.GPS_VENDOR_NOT_FOUND);
		}
		catch (Exception e) {
			LOGGER.error("There is a problem in instatiating the class {} ,check for the modifiers of the class", e, dpClassName);
			throw new ServiceException(ErrorCode.GPS_VENDOR_NOT_FOUND);
		}
		return instance;
	}

}

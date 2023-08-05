package org.in.com.aggregator.payment.impl;

import org.in.com.aggregator.payment.PGInterface;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GatewayFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(GatewayFactory.class);
	
	public static PGInterface returnPgInstance(String pgCode) {
		PGInterface gatewayInstance = null;
		String pgClassName = "org.in.com.aggregator.payment.impl."+pgCode;
		try {
			Class<?> gatewayClass = Class.forName(pgClassName);
			gatewayInstance = (PGInterface) gatewayClass.newInstance();
		}
		catch (ClassNotFoundException e) {
			LOGGER.error("{} does not exist ,please create one in the same package,if exists check for the class name",e,pgClassName);
			throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
		}
		catch (Exception e) {
			LOGGER.error("There is a problem in instatiating the class {} ,check for the modifiers of the class",e,pgClassName);
			throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
		}
		return gatewayInstance;
	}
	
}

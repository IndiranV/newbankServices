package org.in.com.aggregator.gps;

import org.in.com.aggregator.gps.impl.GPSInterface;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GPSLocationDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class TrackBusServiceImpl implements TrackBusService {
	private static final Logger logger = LoggerFactory.getLogger("org.in.com.aggregator.bits.trackbus");

	public void updateGeoTripDetails(AuthDTO authDTO, GPSDeviceVendorEM deviceVendor, TripChartDTO tripChartDTO) {
		GPSInterface implInstance = getGPSImplFactory(deviceVendor);
		if (implInstance == null) {
			throw new ServiceException(ErrorCode.GPS_VENDOR_NOT_FOUND);
		}
		implInstance.updateGeoTripDetails(authDTO, tripChartDTO);
	}

	public void removeGeoTripDetails(AuthDTO authDTO, GPSDeviceVendorEM deviceVendor, TripDTO tripDTO) {
		GPSInterface implInstance = getGPSImplFactory(deviceVendor);
		if (implInstance == null) {
			throw new ServiceException(ErrorCode.GPS_VENDOR_NOT_FOUND);
		}
		implInstance.removeGeoTripDetails(authDTO, tripDTO);
	}

	private GPSInterface getGPSImplFactory(GPSDeviceVendorEM gpsDeviceVendorEM) {
		GPSInterface gpsInstance = null;
		String pgClassName = "org.in.com.aggregator.gps.impl." + gpsDeviceVendorEM.getServiceImpl();
		try {
			Class<?> gatewayClass = Class.forName(pgClassName);
			gpsInstance = (GPSInterface) gatewayClass.newInstance();
		}
		catch (ClassNotFoundException e) {
			logger.error("{} does not exist ,please create one in the same package,if exists check for the class name", e, pgClassName);
			throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
		}
		catch (Exception e) {
			logger.error("There is a problem in instatiating the class {} ,check for the modifiers of the class", e, pgClassName);
			throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
		}
		return gpsInstance;
	}

	public GPSLocationDTO getVehicleLocation(String operatorCode, GPSDeviceVendorEM gpsDeviceVendorEM, String deviceCode, String vehicleRegistrationeNumber) {
		GPSInterface implInstance = getGPSImplFactory(gpsDeviceVendorEM);
		if (implInstance == null) {
			logger.info(operatorCode + Text.HYPHEN + gpsDeviceVendorEM.getCode() + vehicleRegistrationeNumber + Text.HYPHEN + deviceCode + Text.HYPHEN + "implInstance Not found");
			throw new ServiceException(ErrorCode.GPS_VENDOR_NOT_FOUND);
		}
		GPSLocationDTO gpsLocationDTO = implInstance.getVehicleLocation(operatorCode, deviceCode, vehicleRegistrationeNumber);
		if (gpsLocationDTO == null) {
			logger.info(operatorCode + Text.HYPHEN + gpsDeviceVendorEM.getCode() + vehicleRegistrationeNumber + Text.HYPHEN + deviceCode + Text.HYPHEN + "Vehicle Location Not Found ");
			throw new ServiceException(ErrorCode.GPS_DEVICE_LOCATION_NOT_FOUND);
		}
		int diffMinuties = DateUtil.getMinutiesDifferent(new DateTime(gpsLocationDTO.getUpdatedTime()), DateUtil.NOW());
		if (diffMinuties > 60) {
			logger.info(operatorCode + Text.HYPHEN + gpsDeviceVendorEM.getCode() + vehicleRegistrationeNumber + Text.HYPHEN + deviceCode + Text.HYPHEN + "Vehicle Location diffMinuties: " + diffMinuties);
			throw new ServiceException(ErrorCode.GPS_DEVICE_LOCATION_NOT_FOUND);
		}
		return gpsLocationDTO;
	}

}

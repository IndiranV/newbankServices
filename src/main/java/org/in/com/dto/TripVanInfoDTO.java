package org.in.com.dto;

import org.in.com.dto.enumeration.NotificationTypeEM;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TripVanInfoDTO extends BaseDTO<TripVanInfoDTO> {
	private String mobileNumber;
	private DateTime tripDate;
	private NotificationTypeEM notificationType;
	private BusVehicleDTO vehicle;
	private BusVehicleDriverDTO driver;
	private BusVehicleVanPickupDTO vanPickup;
	private TripVanExceptionDTO tripVanException;
	private AuditDTO audit;
}

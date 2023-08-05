package org.in.com.dto;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.GPSDeviceVendorEM;
import org.in.com.dto.enumeration.VehicleTypeEM;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusVehicleDTO extends BaseDTO<BusVehicleDTO> {
	private String registrationDate;
	private String registationNumber;
	private String licNumber;
	private String gpsDeviceCode;
	private String mobileNumber;
	private DateTime lastAssignedDate;
	private BusDTO bus;
	private GPSDeviceVendorEM deviceVendor;
	private VehicleTypeEM vehicleType;

	public String getLastAssignedDateToString() {
		return lastAssignedDate != null ? lastAssignedDate.format(Text.DATE_DATE4J) : Text.NA;
	}
}

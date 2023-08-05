package org.in.com.dto;

import org.in.com.constants.Text;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusVehicleDriverDTO extends BaseDTO<BusVehicleDriverDTO> {
	private String lastName;
	private String dateOfBirth;
	private String bloodGroup;
	private String licenseNumber;
	private String badgeNumber;
	private String licenseExpiryDate;
	private String qualification;
	private String employeeCode;
	private String mobileNumber;
	private String emergencyContactNumber;
	private String joiningDate;
	private String remarks;
	private DateTime lastAssignedDate;
	private int assignedTripsCount;
	private int allowAllVehicle;
	private UserDTO user;
	private String aadharNo;

	public String getLastAssignedDateToString() {
		return lastAssignedDate != null ? lastAssignedDate.format(Text.DATE_DATE4J) : Text.NA;
	}
}

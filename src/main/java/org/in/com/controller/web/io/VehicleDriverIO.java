
package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleDriverIO extends BaseIO {
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
	private String lastAssignedDate;
	private int assignedTripsCount;
	private String remarks;
	private UserIO user;
	private String aadharNo;
}

package org.in.com.controller.api.io;

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
	private String aadharNo;
	private String joiningDate;
	private String remarks;
}
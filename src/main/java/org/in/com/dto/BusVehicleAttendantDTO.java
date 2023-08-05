package org.in.com.dto;

import org.in.com.dto.enumeration.AttendantCategoryEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusVehicleAttendantDTO extends BaseDTO<BusVehicleAttendantDTO> {
	private int age;
	private String mobile;
	private String alternateMobile;
	private String joiningDate;
	private String address;
	private String remarks;
	private AttendantCategoryEM category;
}

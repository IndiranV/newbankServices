package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.json.JSONObject;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.enumeration.NotificationBusContactEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.utils.StringUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripInfoDTO extends BaseDTO<TripInfoDTO> {
	private BusVehicleDTO busVehicle;
	private String driverName;
	private String remarks;
	private String driverMobile;
	private List<NotificationTypeEM> notificationStatus;
	private DateTime tripStartDateTime;
	private DateTime tripCloseDateTime;
	private String driverName2;
	private String driverMobile2;
	private String attenderName;
	private String attenderMobile;
	private String captainName;
	private String captainMobile;
	private BusVehicleDriverDTO primaryDriver;
	private BusVehicleDriverDTO secondaryDriver;
	private BusVehicleAttendantDTO attendant;
	private BusVehicleAttendantDTO captain;
	private List<ScheduleTagDTO> scheduleTagList;
	/** StartOdometer|DateTime|EndOdometer|DateTime */
	private String extras;
	private BusBreakevenSettingsDTO tripBreakeven;
	private float distance;
	private NotificationBusContactEM notificationBusContact;
	
	public String getNotificationStatusCodes() {
		StringBuilder builder = new StringBuilder();
		if (notificationStatus != null && !notificationStatus.isEmpty()) {
			for (NotificationTypeEM typeEM : notificationStatus) {
				if (builder.length() != 0) {
					builder.append(",");
				}
				builder.append(typeEM.getCode());
			}
		}
		return builder.toString();
	}

	public boolean checkNoficationTypeExists(NotificationTypeEM checkType) {
		if (notificationStatus != null && !notificationStatus.isEmpty()) {
			for (NotificationTypeEM typeEM : notificationStatus) {
				if (typeEM.getId() == checkType.getId()) {
					return true;
				}
			}
		}
		return false;
	}

	public void addNoficationType(NotificationTypeEM notificationType) {
		List<NotificationTypeEM> notificationStatusList = new ArrayList<NotificationTypeEM>();
		if (notificationStatus != null && !notificationStatus.isEmpty()) {
			notificationStatusList.addAll(notificationStatus);
		}
		notificationStatusList.add(notificationType);
		notificationStatus = notificationStatusList;
	}

	public String getSecondaryDriverDetails() {
		JSONObject driverAttenderJSON = new JSONObject();
		driverAttenderJSON.put("dn", driverName2);
		driverAttenderJSON.put("dm", driverMobile2);
		driverAttenderJSON.put("an", attenderName);
		driverAttenderJSON.put("am", attenderMobile);
		driverAttenderJSON.put("cn", captainName);
		driverAttenderJSON.put("cm", captainMobile);
		return driverAttenderJSON.toString();
	}

	public int getPrimaryDriverId() {
		return primaryDriver != null ? primaryDriver.getId() : Numeric.ZERO_INT;
	}

	public int getSecondaryDriverId() {
		return secondaryDriver != null ? secondaryDriver.getId() : Numeric.ZERO_INT;
	}

	public int getAttendantId() {
		return attendant != null ? attendant.getId() : Numeric.ZERO_INT;
	}

	public int getCaptainId() {
		return captain != null ? captain.getId() : Numeric.ZERO_INT;
	}

	public String getScheduleTagIds() {
		StringBuilder scheduleTagIds = new StringBuilder();
		String tags = Text.EMPTY;
		if (scheduleTagList != null) {
			for (ScheduleTagDTO scheduleTag : scheduleTagList) {
				if (scheduleTag.getId() == 0) {
					continue;
				}
				scheduleTagIds.append(scheduleTag.getId());
				scheduleTagIds.append(Text.COMMA);
			}
			tags = scheduleTagIds.toString();
		}
		if (StringUtil.isNull(tags)) {
			tags = Text.NA;
		}
		return tags;
	}
	
	public NotificationBusContactEM getNotificationBusContactType() {
		if (StringUtil.isNotNull(extras) && extras.split("\\|").length > 4) {
			return NotificationBusContactEM.getTypeEM(StringUtil.getIntegerValue(extras.split("\\|")[4]));
		}
		return NotificationBusContactEM.DRIVER_1;
	}
	
	public String getBusContactMobileNumber() {
		String mobileNumber = Text.NA;
		NotificationBusContactEM repoBusContactType = getNotificationBusContactType();
		if (repoBusContactType != null) {
			if (NotificationBusContactEM.DRIVER_1.getId() == repoBusContactType.getId() && StringUtil.isNotNull(driverMobile)) {
				mobileNumber = driverMobile;
			}
			else if (NotificationBusContactEM.DRIVER_2.getId() == repoBusContactType.getId() && StringUtil.isNotNull(driverMobile2)) {
				mobileNumber = driverMobile2;
			}
			else if (NotificationBusContactEM.ATTENDER.getId() == repoBusContactType.getId() && StringUtil.isNotNull(attenderMobile)) {
				mobileNumber = attenderMobile;
			}
			else if (NotificationBusContactEM.CAPTAIN.getId() == repoBusContactType.getId() && StringUtil.isNotNull(captainMobile)) {
				mobileNumber = captainMobile;
			}
		}
		// set default as primary driver mobile number
		if (StringUtil.isNull(mobileNumber)) {
			mobileNumber = driverMobile;
		}
		return mobileNumber;
	}
}

package org.in.com.dto;

import org.in.com.dto.enumeration.SeverityPermissionTypeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MenuEventDTO extends BaseDTO<MenuEventDTO> {
	private int permissionFlag;
	private SeverityPermissionTypeEM severity;
	private int enabledFlag;
	private String operationCode;
	private String attr1Value;

	public static String getPermission(int permissionFlag) {
		String permission = null;// Idel,Visable,Edit,ALL
		if (permissionFlag == 1) {
			permission = "ACT";
		}
		else {
			permission = "IDL";

		}
		return permission;
	}

	public static int getPermission(String permissionFlag) {
		int permission = 0;// Idel,Visable,Edit,ALL
		if ("ACT".equals(permissionFlag)) {
			permission = 1;
		}
		else {
			permission = 0;
		}
		return permission;
	}
}

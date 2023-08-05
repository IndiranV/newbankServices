package org.in.com.dto.enumeration;

public enum SeverityPermissionTypeEM {
	
	NOT_AVAILABLE(0,"NA","Not Available","NA"),
	PERMISSION_DEFAULT(1,"DEFAULT","Permission Type Default","EBL-DEFAULT"),
	PERMISSION_MAJOR(2,"MAJOR","Permission Type Major","EBL-MAJOR"),
	PERMISSION_CRITICAL(3,"CRITICAL","Permission Type Critical","EBL-CRITICAL"),
	PERMISSION_BLOCKER(4,"BLOCKER","Permission Type Blocker","EBL-BLOCKER");

	private final int id;
	private final String code;
	private final String name;
	private final String operationCode;

	private SeverityPermissionTypeEM(int id, String code, String name, String operationCode) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.operationCode = operationCode;
	}
	
	public Integer getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
 

	public String getOperationCode() {
		return operationCode;
	}

	public static SeverityPermissionTypeEM getSeverityPermissionTypeEM(int id) {
		SeverityPermissionTypeEM[] values = values();
		for (SeverityPermissionTypeEM permission : values) {
			if (permission.getId() == id) {
				return permission;
			}
		}
		return NOT_AVAILABLE;
	}

	public static SeverityPermissionTypeEM getSeverityPermissionTypeEM(String code) {
		SeverityPermissionTypeEM[] values = values();
		for (SeverityPermissionTypeEM pType : values) {
			if (pType.getCode().equalsIgnoreCase(code)) {
				return pType;
			}
		}
		return NOT_AVAILABLE;
	}
	public static SeverityPermissionTypeEM getSeverityOperationCode(String operationCode) {
		SeverityPermissionTypeEM[] values = values();
		for (SeverityPermissionTypeEM pType : values) {
			if (pType.getOperationCode().equalsIgnoreCase(operationCode)) {
				return pType;
			}
		}
		return NOT_AVAILABLE;
	}

}

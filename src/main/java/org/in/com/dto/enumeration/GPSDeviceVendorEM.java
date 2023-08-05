package org.in.com.dto.enumeration;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;

public enum GPSDeviceVendorEM {

	NO_GPS_DEVICE("NOGPS", 0, "No GPS Device", Text.DEFAULT, Text.DEFAULT),

	JTECH("JTECH", 1, "JTech GPS Device", "JTrackServiceImpl", "parveen"),

	REDBUS("RBGPS", 2, "Redbus GPS Device", "GEOServiceImpl", Text.DEFAULT),

	EZEEGPS("EZEEGPS", 3, "EzeeInfo GPS Device", "GEOServiceImpl", Text.DEFAULT),
	// Rajesh Transports
	INTANGLE("INTANGLE", 4, "INTANGLE VTS Device", "IntangleServiceImpl", "bits,rajeshtransports"),
	// Rathimeena Travels
	PLAYGPS("PLAYGPS", 5, "Play GPS Device", "PlayGpsServiceImpl", "bits,rathimeena"),

	SENSEL("SENSEL", 6, "Sensel VTS Device", "SenselVTSServiceImpl", "bits,rathimeena,holidayappeal");

	private final int id;
	private final String code;
	private final String name;
	private final String namespace;
	private final String serviceImpl;

	private GPSDeviceVendorEM(String code, int id, String name, String serviceImpl, String namespace) {
		this.code = code;
		this.id = id;
		this.name = name;
		this.serviceImpl = serviceImpl;
		this.namespace = namespace;
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

	public Integer getIntCode() {
		return Integer.valueOf(code);
	}

	public String getNamespace() {
		return namespace;
	}

	public String getServiceImpl() {
		return serviceImpl;
	}

	public String toString() {
		return code + " : " + id;
	}

	public static GPSDeviceVendorEM getGPSDeviceVendorEM(int id) {
		GPSDeviceVendorEM[] values = values();
		for (GPSDeviceVendorEM statusDTO : values) {
			if (statusDTO.getId() == id) {
				return statusDTO;
			}
		}
		return NO_GPS_DEVICE;
	}

	public static GPSDeviceVendorEM getGPSDeviceVendorEM(String Code) {
		GPSDeviceVendorEM[] values = values();
		for (GPSDeviceVendorEM errorCode : values) {
			if (errorCode.getCode().equalsIgnoreCase(Code)) {
				return errorCode;
			}
		}
		return NO_GPS_DEVICE;
	}

	public static List<GPSDeviceVendorEM> getGPSDeviceVendorByNamespace(String namespaceCode) {
		List<GPSDeviceVendorEM> vendorList = new ArrayList<GPSDeviceVendorEM>();
		GPSDeviceVendorEM[] values = values();
		for (GPSDeviceVendorEM deviceVendor : values) {
			for (String nscode : deviceVendor.getNamespace().split(Text.COMMA)) {
				if (nscode.equalsIgnoreCase(namespaceCode) || Text.DEFAULT.equalsIgnoreCase(nscode)) {
					vendorList.add(deviceVendor);
					break;
				}
			}
		}
		return vendorList;
	}
}

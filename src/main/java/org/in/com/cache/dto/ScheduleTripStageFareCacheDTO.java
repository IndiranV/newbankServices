package org.in.com.cache.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class ScheduleTripStageFareCacheDTO implements Serializable {
	private static final long serialVersionUID = 6466923475533300175L;
	private String code;
	private String tripDate;
	private String fareDetails;

}
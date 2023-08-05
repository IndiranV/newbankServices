package org.in.com.cache.dto;

import hirondelle.date4j.DateTime;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ScheduleCacheDTO implements Serializable {
	private static final long serialVersionUID = 2592401064509626271L;
	private int id;
	private int activeFlag;
	private String code;
	private String name;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String serviceNumber;
	private String displayName;
	private String apiDisplayName;
	private String pnrStartCode;
	private String preRequrities;
	private String lookupCode;
	private DateTime tripDate;
	private int categoryId;
	private String tagId;
	private List<ScheduleCacheDTO> overrideListCacheDTO = new ArrayList<ScheduleCacheDTO>();

}

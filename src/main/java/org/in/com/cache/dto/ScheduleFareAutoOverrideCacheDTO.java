package org.in.com.cache.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ScheduleFareAutoOverrideCacheDTO implements Serializable {
	private static final long serialVersionUID = 6466923475536800175L;
	private int id;
	private int activeFlag;
	private String code;
	private int busId;
	private List<Integer> groupList;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;

	private int overrideMinutes;
	private BigDecimal fare;
	private String fareOverrideModeCode;
	private String tag;
	private List<String> routeList;
	private List<Integer> busSeatTypes;
	private String seatTypeFareDetails;

	private List<ScheduleFareAutoOverrideCacheDTO> overrideList = new ArrayList<ScheduleFareAutoOverrideCacheDTO>();

}
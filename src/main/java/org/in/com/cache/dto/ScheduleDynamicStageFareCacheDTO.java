package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ScheduleDynamicStageFareCacheDTO implements Serializable {
	private static final long serialVersionUID = 5069266198811086893L;
	private String code;
	private String lookupCode;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int dynamicPriceProviderId;
	private List<ScheduleDynamicStageFareDetailsCacheDTO> dynamicStageFare;
	private List<ScheduleDynamicStageFareCacheDTO> overrideList = new ArrayList<ScheduleDynamicStageFareCacheDTO>();

}
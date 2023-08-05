package org.in.com.cache.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ExtraCommissionCacheDTO implements Serializable {
	private static final long serialVersionUID = 3448651258690030968L;
	private String code;
	private String name;
	private String commissionValueTypeCode;
	private BigDecimal commissionValue = BigDecimal.ZERO;
	private List<Integer> groupId;
	private List<Integer> userId;
	private String refferenceType;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String dateTypeCode;
	private String scheduleCodeList;
	private String routeCodeList;
	private List<ExtraCommissionCacheDTO> overrideList = new ArrayList<ExtraCommissionCacheDTO>();
	private BigDecimal maxCommissionLimit = BigDecimal.ZERO;
	private BigDecimal minTicketFare = BigDecimal.ZERO;
	private BigDecimal maxExtraCommissionAmount = BigDecimal.ZERO;
	private int minSeatCount;
	private int overrideCommissionFlag;

	private String extraCommissionSlabcode;
	private String slabCalenderTypeCode;
	private String slabCalenderModeCode;
	private String slabModeCode;
	private int slabFromValue;
	private int slabToValue;
}
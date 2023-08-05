package org.in.com.dto;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchDTO extends BaseDTO<SearchDTO> {
	private DateTime travelDate;
	private StationDTO fromStation;
	private StationDTO toStation;
}

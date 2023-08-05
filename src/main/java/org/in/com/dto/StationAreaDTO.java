package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StationAreaDTO extends BaseDTO<StationAreaDTO> {
	private String latitude;
	private String longitude;
	private int radius;
	private StationDTO station;

}

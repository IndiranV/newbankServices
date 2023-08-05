package org.in.com.dto;

import java.util.List;

import org.in.com.dto.enumeration.DeviceMediumEM;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NamespaceBannerDTO extends BaseDTO<NamespaceBannerDTO> {
	private List<GroupDTO> group;
	private String displayModel; // P - Pop up, S - Slider
	private List<DeviceMediumEM> deviceMedium;
	private DateTime fromDate;
	private DateTime toDate;
	private String dayOfWeek;
	private String color;
	private List<NamespaceBannerDetailsDTO> bannerDetails;
	private DateTime updatedAt;
}

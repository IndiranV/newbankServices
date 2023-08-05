package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NamespaceBannerIO extends BaseIO {
	private List<GroupIO> group;
	private String displayModel;
	private List<BaseIO> deviceMedium;
	private String fromDate;
	private String toDate;
	private String dayOfWeek;
	private String color;
	private List<NamespaceBannerDetailsIO> bannerDetails;
	private String updatedAt;
}

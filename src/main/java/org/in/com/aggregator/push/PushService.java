package org.in.com.aggregator.push;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TripChartDTO;

import net.sf.json.JSONObject;

public interface PushService {

	public void pushTripDetails(AuthDTO authDTO, TripChartDTO tripChartDTO, JSONObject jsonObject);
}

package org.in.com.aggregator.aws;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import hirondelle.date4j.DateTime;

public interface S3Service {
	public JsonArray getActiveTripList(AuthDTO authDTO, DateTime tripDate);

	public String exportReport(AuthDTO authDTO,  List<String> dataList, String reportName, String fileName);

	public URL generatePresignedURL(AuthDTO authDTO, JsonObject json);
	
	public Map<String, String> getAllStateFuelPrice(AuthDTO authDTO, DateTime fuelDate);

}

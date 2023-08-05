package org.in.com.aggregator.aws;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.utils.DateUtil;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import hirondelle.date4j.DateTime;

@Service
public class S3ServiceImpl implements S3Service {

	public JsonArray getActiveTripList(AuthDTO authDTO, DateTime tripDate) {
		S3Client s3Client = new S3Client();
		return s3Client.getActiveTripList(authDTO, tripDate);
	}

	@Override
	public String exportReport(AuthDTO authDTO, List<String> dataList, String reportCode, String fileName) {
		String finalUrl = ApplicationConfig.getServerZoneCode() + "/export/" + authDTO.getNamespaceCode() + "/report/" + reportCode + "/" + DateUtil.NOW().format("YYYY/MM") + "/" + fileName + ".csv";
		try {
			S3Client s3Client = new S3Client();
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			CSVPrinter csvPrinter = new CSVPrinter(new OutputStreamWriter(byteArrayOutputStream), CSVFormat.DEFAULT);
			for (String data : dataList) {
				csvPrinter.printRecord(data.split(Text.COMMA));
				// csvPrinter.println();
			}
			csvPrinter.flush();
			csvPrinter.close();

			byteArrayOutputStream.close();

			ByteArrayInputStream stream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
			System.out.println("finalUrl: " + finalUrl);
			s3Client.exportReport(authDTO, stream, finalUrl);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return finalUrl;
	}

	public URL generatePresignedURL(AuthDTO authDTO, JsonObject json) {
		S3Client s3Client = new S3Client();
		String path = json.get("reportCode").getAsString();
		String fileName = json.get("fileurl").getAsString();
		return s3Client.generatePresignedURL(authDTO, path, fileName);
	}

	@Override
	public Map<String, String> getAllStateFuelPrice(AuthDTO authDTO, DateTime fuelDate) {
		Map<String, String> stateFuelMap = new HashMap<String, String>();
		S3Client s3Client = new S3Client();
		JSONObject json = s3Client.getAllStateFuelPrice(authDTO, fuelDate);

		JSONArray stateArray = json.getJSONArray("price");
		for (Object object : stateArray) {
			JSONObject jsonObject = JSONObject.fromObject(object);
			String stateCode = jsonObject.getString("code");
			String fuelPrice = jsonObject.getString("value");
			stateFuelMap.put(stateCode, fuelPrice);
		}
		return stateFuelMap;
	}

}

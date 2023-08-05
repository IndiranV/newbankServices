package org.in.com.aggregator.cargo;

import org.in.com.constants.Numeric;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CargoDataConvertor {

	public JSONArray getCargoTransitDetails(JSONObject json) {
		JSONArray transitArray = new JSONArray();
		try {
			if (Numeric.ONE_INT == json.getInt("status")) {
				JSONArray jsonArray = json.getJSONArray("data");
				if (!jsonArray.isEmpty()) {
					for (Object object : jsonArray) {
						JSONObject jsonObject = (JSONObject) object;
						
						JSONObject transitObject = new JSONObject();
						transitObject = jsonObject.getJSONObject("transitCargo");
						
						JSONArray cargoArray = new JSONArray();
						JSONArray existCargoArray = jsonObject.getJSONArray("cargoList");
						for (Object cargoObject : existCargoArray) {
							JSONObject cargoJsonObject = (JSONObject) cargoObject;
							JSONObject additinalAttrObject = cargoJsonObject.getJSONObject("additionalAttribute");
							for (Object keyObject : additinalAttrObject.keySet()) {
								String key = (String) keyObject;
								cargoJsonObject.put(key, additinalAttrObject.get(key));
							}
							cargoJsonObject.remove("additionalAttribute");
							cargoArray.add(cargoJsonObject);
						}
						
						transitObject.put("cargoList", cargoArray);
						transitArray.add(transitObject);
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return transitArray;
	}
}

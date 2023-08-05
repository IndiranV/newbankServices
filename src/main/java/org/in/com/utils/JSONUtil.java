package org.in.com.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Text;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

public class JSONUtil {

	public static Map<String, String> jsonToMap(JSONObject json) throws JSONException {
		Map<String, String> retMap = new HashMap<String, String>();

		if (json != null) {
			retMap = toMap(json);
		}
		return retMap;
	}

	public static Map<String, String> toMap(JSONObject object) throws JSONException {
		Map<String, String> map = new HashMap<String, String>();

		Iterator<?> keysItr = object.keys();
		while (keysItr.hasNext()) {
			String key = (String) keysItr.next();
			Object value = object.get(key);

			if (StringUtil.isNull(String.valueOf(value)) && (value instanceof JSONObject || value instanceof JSONArray)) {
				map.put(key, Text.EMPTY);
				continue;
			}
			else if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}
			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			else if (value instanceof Integer || value instanceof Float || value instanceof Double || value instanceof JSONNull) {
				value = String.valueOf(value);
			}

			if (value != null) {
				map.put(key, String.valueOf(value));
			}
			else {
				map.put(key, Text.EMPTY);
			}
		}
		return map;
	}

	public static List<Object> toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < array.size(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

	public static Map<String, String> jsonToMap(String response) {
		return jsonToMap(JSONObject.fromObject(response));
	}

	// large json file
	public static String objectToJson(Object object) {
		ObjectMapper Obj = new ObjectMapper();
		String jsonStr = Text.NA;
		if (object != null) {
			try {
				jsonStr = Obj.writeValueAsString(object);
			}
			catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		return jsonStr;
	}

	// small json file
	public static String objectToJsonV2(Object object) throws JsonProcessingException {
		String jsonStr = Text.NA;
		if (object != null) {
			Gson gson = new Gson();
			jsonStr = gson.toJson(object);
		}
		return jsonStr;
	}

	public static String listToJsonString(List<String> objectList) {
		return new Gson().toJson(objectList);
	}
}

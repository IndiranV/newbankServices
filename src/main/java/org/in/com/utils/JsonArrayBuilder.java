package org.in.com.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonArrayBuilder {
	public final JsonArray json = new JsonArray();

	public JsonArray toJson() {
		return json.getAsJsonArray();
	}

	public JsonArrayBuilder add(JsonObject value) {
		json.add(value);
		return this;
	}

}

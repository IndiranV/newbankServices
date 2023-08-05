package org.in.com.utils;

import java.math.BigDecimal;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

// https://stackoverflow.com/questions/8876271/how-to-serialize-a-jsonobject-without-too-much-quotes
public class JsonBuilder {
	public final JsonObject json = new JsonObject();

	public JsonObject toJson() {
		return json.getAsJsonObject();
	}

	public JsonBuilder add(String key, JsonObject value) {
		json.add(key, value);
		return this;
	}

	public JsonBuilder add(String key, JsonArray jsonArray) {
		json.add(key, jsonArray);
		return this;
	}

	public JsonBuilder add(String key, String value) {
		json.addProperty(key, value);
		return this;
	}

	public JsonBuilder add(String key, BigDecimal value) {
		json.addProperty(key, value);
		return this;
	}

	public JsonBuilder add(String key, JsonBuilder value) {
		json.add(key, value.json);
		return this;
	}
}

class JsonBuilde1r {

	public static void main(String[] args) {

		System.out.println(new JsonBuilder().add("key1", "value1").add("key2", "value2").add("key3", new JsonBuilder().add("innerKey", "value3")).toJson());
	}
}
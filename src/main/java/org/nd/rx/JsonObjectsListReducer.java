package org.nd.rx;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import net.minidev.json.JSONObject;



public class JsonObjectsListReducer implements BiFunction<List<JSONObject>, Optional<JSONObject>, List<JSONObject>> {

    @Override
    public List<JSONObject> apply(List<JSONObject> listOfJsonObject, Optional<JSONObject> value) {
	JSONObject object = value.get();
	if(object!=null) listOfJsonObject.add(object);
	return listOfJsonObject;
    }




}

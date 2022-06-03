package org.nd.services.filtering;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.nd.managers.CachesManager;
import org.nd.rx.JsonObjectsListReducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.wnameless.json.unflattener.JsonUnflattener;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vavr.Tuple;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@Singleton
public class Extractor {
    private static Logger logger = LoggerFactory.getLogger(Extractor.class);
    
	
	@Inject
	private CachesManager cachesManager;


    public Mono<List<JSONObject>> execute(List<JSONObject> jsonsList, String pathToExtract) {

	    // fragments names
	    List<String> fragmentsNames = new ArrayList<String>();
	    try {
		String[] splits = pathToExtract.split(",");
		for (String split : splits) {
		    fragmentsNames.add(split.trim());
		}
	    } catch (Exception e) {
	    }


	    List<JSONObject> result = new ArrayList<JSONObject>();

	   return  Flux
		   .fromIterable(jsonsList)
		.map(json -> json.getAsString("_systemId"))
			.map(systemId -> Tuple.of(cachesManager.flattenFromCache(systemId), systemId))
			.map(pair -> {

			    String systemId = pair._2();
			    Map<String, Object> flattenJson = pair._1();
			    Map<String, Object> output = new HashMap<String, Object>();

			    for (String frName : fragmentsNames) {
				Object value = flattenJson.get(frName);
				if (value != null)
				    output.put(frName, value);
			    }
			    output.put("_systemId", systemId);
			    String outputJson = JsonUnflattener.unflatten(output);
			    Optional<JSONObject> extractResult = Optional.of((JSONObject) JSONValue.parse(outputJson));
			    return extractResult;

			})
			.reduce(result, new JsonObjectsListReducer());

    }
    
 // ------------------------------------------------------------------------------------------------------------------------
    
    public Mono<JSONObject> execute(JSONObject inputJson, String pathToExtract) {

	    // fragments names
	    List<String> fragmentsNames = new ArrayList<String>();
	    try {
		String[] splits = pathToExtract.split(",");
		for (String split : splits) {
		    fragmentsNames.add(split.trim());
		}
	    } catch (Exception e) {
	    }

	   return  Mono.just(inputJson)
		.map(json -> json.getAsString("_systemId"))
			.map(systemId -> Tuple.of(cachesManager.flattenFromCache(systemId), systemId))
			.map(pair -> {

			    String systemId = pair._2();
			    Map<String, Object> flattenJson = pair._1();
			    Map<String, Object> output = new HashMap<String, Object>();

			    for (String frName : fragmentsNames) {
				Object value = flattenJson.get(frName);
				if (value != null)
				    output.put(frName, value);
			    }
			    output.put("_systemId", systemId);
			    String outputJson = JsonUnflattener.unflatten(output);
			   JSONObject extractResult = (JSONObject) JSONValue.parse(outputJson);
			    return extractResult;

			});

}

}

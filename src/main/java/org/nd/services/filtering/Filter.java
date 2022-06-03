package org.nd.services.filtering;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.nd.managers.CachesManager;
import org.nd.rx.StringArrayReducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jayway.jsonpath.JsonPath;

import io.vavr.Tuple;
import io.vavr.control.Try;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Singleton
public class Filter {
    private static Logger logger = LoggerFactory.getLogger(Filter.class);
    
	@Inject
	private CachesManager cachesManger;


    public Mono<List<String>> execute(List<String> unfilteredKeysArray, String JsonPathQuery) {
	
	

	    JsonPath jsonPath = Try.of(() -> JsonPath.compile(JsonPathQuery)).getOrNull();	    
	    List<String> result = new ArrayList<String>();

	    return Flux
	    .fromIterable(unfilteredKeysArray)
	    .parallel()
	        .runOn(Schedulers.parallel())
		    .map(systemId -> Tuple.of(cachesManger.readContextFromCache(systemId), systemId))
		    .map(pair -> {

			Object results = Try.of(() -> pair._1().read(jsonPath)).getOrNull();
			Optional<String> jsonPathResult = Optional.empty();
			if (results != null) {
			    if (results instanceof List) {
				if (!((List) results).isEmpty()) {
				    jsonPathResult = Optional.of(pair._2());
				}
			    } else {
				jsonPathResult = Optional.of(pair._2());
			    }
			}
			return jsonPathResult;

		    })
		    .filter(item -> item.isPresent())
		    .sequential()
		    .reduce(result, new StringArrayReducer());


    }

}

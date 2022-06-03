package org.nd.services.operations;
import org.nd.dto.QueryHolder;
import org.nd.exceptions.NotFoundException;
import org.nd.managers.CachesManager;
import org.nd.services.filtering.Extractor;
import org.nd.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.minidev.json.JSONObject;
import reactor.core.publisher.Mono;


@Singleton
public class GetOne {
	private static Logger logger = LoggerFactory.getLogger(GetOne.class);
	
	@Inject
	private CachesManager cachesManager;
	
	@Inject
	private Extractor extractor;

	public  Mono<JSONObject> execute(String systemId, QueryHolder queryHolder) {    

	    JSONObject json = cachesManager.jsonFromCache(systemId);
	    
	    if (json != null) {

		// if extarct path found try to extract element
		if (Utils.notNullAndNotEmpty(queryHolder.getExtract())) {	
		    
		    return extractor.execute(json, queryHolder.getExtract());
		    
		} else {
		    return Mono.just(json);
		}
		
	    } else {
		return Mono.error(new NotFoundException());
	    }

	}

}

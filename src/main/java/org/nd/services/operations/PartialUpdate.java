package org.nd.services.operations;

import org.nd.exceptions.NotFoundException;
import org.nd.managers.CachesManager;
import org.nd.services.fs.FsService;
import org.nd.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.hemantsonu20.json.JsonMerge;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import reactor.core.publisher.Mono;

@Singleton
public class PartialUpdate {
    private static Logger logger = LoggerFactory.getLogger(PartialUpdate.class);

    @Inject
    private FsService fsService;

    @Inject
    private CachesManager cachesManager;

    public Mono<JSONObject> execute(JSONObject partialJson) {

	String systemId = partialJson.getAsString("_systemId");

	// if systemId == null return with fail
	if (systemId == null) {
	    return Mono.error(new NotFoundException());
	} else {
	    
	    JSONObject jsonObject = cachesManager.jsonFromCache(systemId);
	    
	    if(jsonObject == null) {
		return Mono.error(new NotFoundException());
	    }else {
		    return Mono.just(jsonObject) 
			    .map(mainDoc -> JsonMerge.merge(partialJson.toString(), mainDoc.toString()))
			    .map(mergeOutput -> Utils.parseOrNull(mergeOutput))
			    .flatMap(mergedDoc -> fsService.saveToFs(mergedDoc, systemId,  "update"));
	    }
	    

	    
	}

    }

}

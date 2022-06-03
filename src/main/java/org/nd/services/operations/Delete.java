package org.nd.services.operations;

import org.nd.exceptions.NotFoundException;
import org.nd.exceptions.ParsingException;
import org.nd.services.fs.FsService;
import org.nd.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.minidev.json.JSONObject;
import reactor.core.publisher.Mono;

@Singleton
public class Delete {
    private static Logger logger = LoggerFactory.getLogger(Delete.class);

    @Inject
    private FsService fsService;

    public Mono<JSONObject> execute(JSONObject jsonToDelete, String id) {
	
	//get systemId
	String systemId = null;
	if(Utils.notNullAndNotEmpty(id) ) {
		systemId = id;
	}else {
		try {
		    systemId = jsonToDelete.getAsString("_systemId");
		} catch (Exception e) {
		    return Mono.error(new ParsingException());
		}
	}


	// if systemId == null return with fail
	if (systemId == null) {
	    return Mono.error(new NotFoundException());
	} else {	    
	    return fsService.deleteFromFs(systemId);	    
	}
	

    }

}

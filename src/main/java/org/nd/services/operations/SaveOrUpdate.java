package org.nd.services.operations;
import org.nd.services.fs.FsService;
import org.nd.utils.TUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.minidev.json.JSONObject;
import reactor.core.publisher.Mono;


@Singleton
public class SaveOrUpdate {
	private static Logger logger = LoggerFactory.getLogger(SaveOrUpdate.class);
	
	@Inject
	private FsService fsService;

	public  Mono<JSONObject> execute(JSONObject  json) {

		String systemId = null;
		String operation = null;

		// get id
		if (json.containsKey("_systemId")) {
			// it's an update
			systemId = json.getAsString("_systemId");
			operation =  "update";
		} else {
			// it's a new document
			systemId = (new TUID()).getId();
			json.put("_systemId", systemId);
			operation = "save";
		}	

		return fsService.saveToFs(json, systemId, operation);

	}

}

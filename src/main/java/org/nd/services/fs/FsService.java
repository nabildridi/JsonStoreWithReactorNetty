package org.nd.services.fs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.nd.managers.CachesManager;
import org.nd.managers.KvDatabaseManager;
import org.nd.rx.JsonObjectsListReducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.minidev.json.JSONObject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Singleton
public class FsService {
    private static Logger logger = LoggerFactory.getLogger(FsService.class);

	@Inject
	private KvDatabaseManager kvDatabaseManager;
	
	@Inject
	private CachesManager cachesManager;

    

    // -----------------------------------------------------------------------------------------------------------------------------------------

    public Mono<JSONObject> saveToFs(JSONObject  json, String systemId, String operation) {

		// refresh kv database
		kvDatabaseManager.writeAndFlush(systemId, json.toString());
		// refresh cache
		cachesManager.invalidate(systemId, true);
		
		//update files index
		CopyOnWriteArrayList<String> filesIndex = kvDatabaseManager.getFileIndex();
		if(!filesIndex.contains(systemId)) {
		    filesIndex.add(systemId);
		}
				
		return Mono.just(json);
	
    }   
    
 // -----------------------------------------------------------------------------------------------------------------------------------------
    public Mono<JSONObject> deleteFromFs(String systemId) {

	// remove from kv database
	kvDatabaseManager.deleteAndFlush(systemId);

	// remove from cache
	cachesManager.invalidate(systemId, false);
			
	//update files index
	CopyOnWriteArrayList<String> filesIndex = kvDatabaseManager.getFileIndex();
	filesIndex.remove(systemId);
	
	JSONObject json = new JSONObject();
	json.put("_systemId", systemId);
				
	return Mono.just(json);
	
    }
    
 // -----------------------------------------------------------------------------------------------------------------------------------------
    public Mono<List<JSONObject>> readListOfFile(List<String> idsJson) {
		
	List<JSONObject> result = new ArrayList<JSONObject>();

		return Flux
		.fromIterable(idsJson)
			    .map(systemId ->  Optional.ofNullable( cachesManager.jsonFromCache(systemId)))
			    .reduce(result, new JsonObjectsListReducer());

	
    }

}

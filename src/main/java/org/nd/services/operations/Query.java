package org.nd.services.operations;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.nd.dto.QueryHolder;
import org.nd.managers.KvDatabaseManager;
import org.nd.services.filtering.Extractor;
import org.nd.services.filtering.Filter;
import org.nd.services.filtering.Sorter;
import org.nd.services.fs.FsService;
import org.nd.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import reactor.core.publisher.Mono;

@Singleton
public class Query {

	private static Logger logger = LoggerFactory.getLogger(Query.class);
	
	@Inject
	private FsService fsService;
	
	@Inject
	private KvDatabaseManager kvDatabaseManager;
	
	@Inject
	private Sorter sorter;
	
	@Inject
	private Extractor extractor;
	
	@Inject
	private Filter filter;


	public Mono<JSONObject> execute(JSONObject  jsonQuery) {

			QueryHolder queryHolder =Utils.mapTo(jsonQuery.toJSONString(), QueryHolder.class);
			
			CopyOnWriteArrayList<String> filesIndex = kvDatabaseManager.getFileIndex();
			
			//if files list is empty no need to continue
			if(filesIndex.size() == 0) {
			    return constructJsonResponse(new ArrayList<JSONObject>(), queryHolder);
			}

			// if filter is null -> regular pagination
			if (!Utils.notNullAndNotEmpty( queryHolder.getFilter()) ) {

			    	queryHolder.setTotalElement(filesIndex.size());
				
				return Mono.just(filesIndex)
				.flatMap(mainArray -> makeSort(mainArray, queryHolder))
				.flatMap(mainArray -> makePagination(mainArray, queryHolder))
				.flatMap(mainArray -> makeData(mainArray))
				.flatMap(mainArray -> makeExtract(mainArray, queryHolder))
				.flatMap(mainArray -> constructJsonResponse(mainArray, queryHolder));
				

			}

			// if filter not null
			if (Utils.notNullAndNotEmpty( queryHolder.getFilter())) {
			    
			    return Mono.just(filesIndex)
				    .flatMap(mainArray -> filter.execute(mainArray, queryHolder.getFilter()))
				    .doOnNext(filteredKeysResult -> {
					queryHolder.setTotalElement(filteredKeysResult.size());
				    })
					.flatMap(mainArray -> makeSort(mainArray, queryHolder))
					.flatMap(mainArray -> makePagination(mainArray, queryHolder))
					.flatMap(mainArray -> makeData(mainArray))
					.flatMap(mainArray -> makeExtract(mainArray, queryHolder))
					.flatMap(mainArray -> constructJsonResponse(mainArray, queryHolder));

			}
			
			return constructJsonResponse(new ArrayList<JSONObject>(), queryHolder);

	

	}

	private Mono<List<String>> makeSort(List<String> unsortedKeysArray, QueryHolder queryHolder) {
		
		
		//does sorting info exists?
		if(!Utils.notNullAndNotEmpty(queryHolder.getSortField())  || !Utils.notNullAndNotEmpty(queryHolder.getSortOrder())) {			
			return Mono.just(unsortedKeysArray);
		}
		
		//sort order must be "1" or "-1"
		if(!queryHolder.getSortOrder().equals("1") && !queryHolder.getSortOrder().equals("-1")){
			return Mono.just(unsortedKeysArray);
		}	
		
		//unsorted list must be not null or empty
		if(unsortedKeysArray == null || unsortedKeysArray.isEmpty()){
			return Mono.just(unsortedKeysArray);
		}
		
		return sorter.execute(unsortedKeysArray, queryHolder);

	}

	private Mono<List<String>> makePagination(List<String> inputArray, QueryHolder queryHolder) {
		
		//page and size must be not null or negative
		if(queryHolder.getPage() == null || queryHolder.getPage()<0) {
		    return Mono.just(inputArray);
		}
		
		if(queryHolder.getSize() == null || queryHolder.getSize()<=0) {
		    return Mono.just(inputArray);
		}

		try {
			
			List<String> pagedList = Utils.getPage(inputArray, queryHolder.getPage(), queryHolder.getSize());
		
			return Mono.just(pagedList);
		} catch (Exception e) {
		    return Mono.just(inputArray);
		}

	}

	private Mono<List<JSONObject>> makeData(List<String> idsJson) {
		return fsService.readListOfFile(idsJson);
	}

	private Mono<List<JSONObject>> makeExtract(List<JSONObject> docs, QueryHolder queryHolder) {

	    //does extract info exists?
		if(!Utils.notNullAndNotEmpty(queryHolder.getExtract()) ) {			
		    return Mono.just(docs);
		}		

		return extractor.execute(docs, queryHolder.getExtract());
	}

	private Mono<JSONObject> constructJsonResponse(List<JSONObject> docs, QueryHolder queryHolder) {
	    	JSONObject json = new JSONObject();
		json.put("content", docs);
		json.put("totalElements", queryHolder.getTotalElement());

		 return Mono.just(json);
	}

}

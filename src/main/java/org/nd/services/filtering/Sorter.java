package org.nd.services.filtering;

import java.util.Comparator;
import java.util.List;

import org.nd.dto.QueryHolder;
import org.nd.managers.CachesManager;
import org.nd.rx.SortedMapReducer;
import org.nd.utils.InverseComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.vavr.Tuple;
import io.vavr.collection.TreeMultimap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Singleton
public class Sorter {
    private static Logger logger = LoggerFactory.getLogger(Sorter.class);
	
	@Inject
	private CachesManager cachesManager;
	


    public Mono<List<String>> execute(List<String> inputKeysArray, QueryHolder queryHolder) {

	    TreeMultimap<String, String> resultMap = null;
	    Comparator<String> natural = Comparator.<String>naturalOrder();
	    
	    if (queryHolder.getSortOrder().equals("1")) {
		resultMap = TreeMultimap.withSet().empty(natural );
	    }
	    if (queryHolder.getSortOrder().equals("-1")) {
		resultMap = TreeMultimap.withSet().empty(new InverseComparator() );
	    }

	   return Flux
	    .fromIterable(inputKeysArray)
	    .parallel()
	        .runOn(Schedulers.parallel())
		    .map(systemId -> {			
			
			Object o = cachesManager.flattenFromCache(systemId).get(queryHolder.getSortField());
			String result = "";
			if(o != null) result =String.valueOf( o );		
			return Tuple.of(systemId, result); 
			
		    })	
		    .sequential()
		    .reduce(resultMap, new SortedMapReducer())
		    .map(sortedMap -> sortedMap.values().toJavaList());



    }
    

}

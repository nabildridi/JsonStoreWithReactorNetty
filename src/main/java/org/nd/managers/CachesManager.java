package org.nd.managers;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

@Singleton
public class CachesManager {
	private Logger logger = LoggerFactory.getLogger(CachesManager.class);

	@Inject
	private ConfigurationManager configurationManager;
	
	@Inject
	private KvDatabaseManager kvDatabaseManager;

	private LoadingCache<String, String> fileCache;
	private LoadingCache<String, JSONObject> jsonObjectCache;
	private LoadingCache<String, ReadContext> readContextCache;
	private LoadingCache<String, Map<String, Object>> flattenCache;

	@Inject
	public void init() {

		Instant start = Instant.now();
		
		Integer cachesSize = configurationManager.config().getAsNumber("cache_size").intValue();
		
		boolean makePreload = Boolean.parseBoolean( configurationManager.config().getAsString("cache_preload") );


		fileCache = Caffeine.newBuilder().maximumSize(cachesSize).build(key -> getFile(key));

		jsonObjectCache = Caffeine.newBuilder().maximumSize(cachesSize).build(key -> getJsonObject(key));

		readContextCache = Caffeine.newBuilder().maximumSize(cachesSize).build(key -> getReadContext(key));

		flattenCache = Caffeine.newBuilder().maximumSize(cachesSize).build(key -> getFlatten(key));
				

		if (makePreload) {
			// initial preloading
			int preloadCount = Math.min(kvDatabaseManager.getFileIndex().size(), cachesSize);
			logger.debug("Preload activated, to preload " + preloadCount + " files, it may take some time");

			for (int i = 0; i < preloadCount; i++) {
				String id = kvDatabaseManager.getFileIndex().get(i);
				try {
					fileCache.put(id, getFile(id));
					jsonObjectCache.put(id, getJsonObject(id));
					readContextCache.put(id, getReadContext(id));
					flattenCache.put(id, getFlatten(id));
				} catch (Exception e) {
				}
			}

			Instant end = Instant.now();
			Duration timeElapsed = Duration.between(start, end);
			logger.debug("Caches preload completed; Time taken : " + timeElapsed.toSeconds() + " seconds");

		}

	}

	// -----------------------------------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------------------------------

	private String getFile(String id) {
		return kvDatabaseManager.read(id);
	}

	private JSONObject getJsonObject(String id) {
		try {
			return  (JSONObject) JSONValue.parse(fileCache.get(id));
		} catch (Exception e) {
			return null;
		}
	}

	private ReadContext getReadContext(String id) {
		try {
			return JsonPath.parse(fileCache.get(id));
		} catch (Exception e) {
			return null;
		}
	}

	private Map<String, Object> getFlatten(String id) {
		try {
			return JsonFlattener.flattenAsMap(fileCache.get(id));
		} catch (Exception e) {
			return null;
		}
	}

	// -----------------------------------------------------------------------------------------------------------------------------------------
	public String stringFromCache(String id) {
		try {

			return fileCache.get(id);
		} catch (Exception e) {
			return null;
		}

	}

	// ------------------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------------------------------
	public JSONObject jsonFromCache(String id) {
		try {

			return jsonObjectCache.get(id);
		} catch (Exception e) {
			return null;
		}

	}

	// ------------------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------------------------------
	public Map<String, Object> flattenFromCache(String id) {
		try {

			return flattenCache.get(id);
		} catch (Exception e) {
			return null;
		}

	}

	// ------------------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------------------------------
	public ReadContext readContextFromCache(String id) {
		try {

			return readContextCache.get(id);
		} catch (Exception e) {
			return null;
		}

	}

	// ------------------------------------------------------------------------------------------------------------------------
	// -----------------------------------------------------------------------------------------------------------------------------------------
	public void invalidate(String id, boolean reload) {
		if (fileCache.getIfPresent(id) != null) {
			fileCache.invalidate(id);
		}

		if (jsonObjectCache.getIfPresent(id) != null) {
			jsonObjectCache.invalidate(id);
		}

		if (readContextCache.getIfPresent(id) != null) {
		    readContextCache.invalidate(id);
		}

		if (flattenCache.getIfPresent(id) != null) {
			flattenCache.invalidate(id);
		}

		if (reload) {
			fileCache.put(id, getFile(id));
			jsonObjectCache.put(id, getJsonObject(id));
			readContextCache.put(id, getReadContext(id));
			flattenCache.put(id, getFlatten(id));
		}

	}
	// ------------------------------------------------------------------------------------------------------------------------
	
}
package org.nd.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class Utils {
    
    
	public static boolean notNullAndNotEmpty(String value) {
		
		return value!= null && !value.isEmpty() && !value.isBlank();
		
	}
	
	public static <T> List<T> getPage(List<T> sourceList, int page, int pageSize) {
	    
	    int fromIndex = page  * pageSize;
	    if(sourceList == null || sourceList.size() <= fromIndex){
	        return Collections.emptyList();
	    }
	    
	    // toIndex exclusive
	    return sourceList.subList(fromIndex, Math.min(fromIndex + pageSize, sourceList.size()));
	}
	
	public static JSONObject parse(String s) throws Exception {	
	    
	    return (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(s);
		
	}
	
	public static JSONObject parseOrNull(String s) {	    
	    try {
		return (JSONObject) new JSONParser(JSONParser.MODE_PERMISSIVE).parse(s);
	    } catch (Exception e) {
		return null;
	    }		
	}
	
	public static <T> T mapTo(String s, Class<T> mapTo)  {	    
	    try {
		return new JSONParser(JSONParser.MODE_PERMISSIVE).parse(s, mapTo);
	    } catch (ParseException e) {
		try {
		    return mapTo.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
			| InvocationTargetException | NoSuchMethodException | SecurityException e1) {
		    return null;
		}
	    }		
	}
	
	
	
}

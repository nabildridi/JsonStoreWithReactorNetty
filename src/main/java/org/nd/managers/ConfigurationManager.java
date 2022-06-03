package org.nd.managers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

@Singleton
public class ConfigurationManager {
    private Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    private JSONObject jsonConfig;

    @Inject
    public void init() {

	InputStream is = getClass().getClassLoader().getResourceAsStream("conf/config.json");
	String content = "{}";
	try {
	    content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
	} catch (IOException e) {}
	jsonConfig = (JSONObject) JSONValue.parse(content);

    }
    // -----------------------------------------------------------------------------------------------------------------------------------------
    
    public  JSONObject config() {
	return jsonConfig;
    }
    
    

}
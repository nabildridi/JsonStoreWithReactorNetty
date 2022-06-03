package org.nd;

import org.nd.injector.AppInjector;
import org.nd.server.ServerService;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class MainClass {

    public static void main(String[] args) {

	Injector injector = Guice.createInjector(new  AppInjector());		
	
	ServerService app = injector.getInstance(ServerService.class);
	
	app.start();

    }

}

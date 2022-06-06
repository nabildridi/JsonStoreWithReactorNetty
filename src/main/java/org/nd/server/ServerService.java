package org.nd.server;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.nd.dto.QueryHolder;
import org.nd.exceptions.ParsingException;
import org.nd.managers.ConfigurationManager;
import org.nd.services.operations.Delete;
import org.nd.services.operations.GetOne;
import org.nd.services.operations.PartialUpdate;
import org.nd.services.operations.Query;
import org.nd.services.operations.SaveOrUpdate;
import org.nd.utils.Utils;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.util.CharsetUtil;
import io.vavr.control.Try;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import reactor.core.publisher.Mono;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

@Singleton
public class ServerService {
    private static Logger logger = LoggerFactory.getLogger(ServerService.class);

    private static HttpServer server;

    @Inject
    private SaveOrUpdate saveOrUpdate;

    @Inject
    private GetOne getOneOperation;

    @Inject
    private Query queryOperation;

    @Inject
    private PartialUpdate partialUpdateOperation;

    @Inject
    private Delete deleteOperation;

    @Inject
    private ConfigurationManager configurationManager;

    public void start() {

	Integer port = configurationManager.config().getAsNumber("application_port").intValue();

	server = HttpServer.create()
		.port(port)
		.route(routes -> routes.
        		get("/{id}", getById()).
        		post("/query", query())
        		.post("/", saveOrUpdate()).
        		put("/", partialUpdate())
        		.delete("/{id}", delete())
        		
		);


	server.bindNow().onDispose().block();

    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------
    private BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> getById() {

	return (req, res) -> {
	    String systemId = req.param("id");
	    Map<String, List<String>> parameters = new QueryStringDecoder(req.uri(), CharsetUtil.UTF_8).parameters();
	    QueryHolder queryHolder = new QueryHolder(parameters);

	    return getOneOperation.execute(systemId, queryHolder)
		    .flatMap(json -> this.ok(res, json))
		    .onErrorResume(error -> error(res, error.getMessage()));

	};
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------
    private BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> query() {

	return (req, res) -> {

	    return req.receive()
		    .aggregate()
		    .asString()
		    .flatMap(jsonStr -> {
                		JSONObject query = Try.of(() -> Utils.parse(jsonStr)).getOrNull();
                		if (query != null) {
                		    return Mono.just(query);
                		} else {
                		    return Mono.error(new ParsingException());
                		}

        	    }).flatMap(query -> queryOperation.execute(query))
        		    .flatMap(json -> this.ok(res, json))
        		    .onErrorResume(error -> error(res, error.getMessage()));
        
        	};
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------
    private BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> saveOrUpdate() {

	return (req, res) -> {

	    return req.receive().aggregate().asString().flatMap(jsonStr -> {
		JSONObject query = Try.of(() -> Utils.parse(jsonStr)).getOrNull();
		if (query != null) {
		    return Mono.just(query);
		} else {
		    return Mono.error(new ParsingException());
		}

	    }).flatMap(query -> saveOrUpdate.execute(query)).flatMap(json -> this.ok(res, json))
		    .onErrorResume(error -> error(res, error.getMessage()));

	};
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------
    private BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> partialUpdate() {

	return (req, res) -> {

	    return req.receive().aggregate().asString().flatMap(jsonStr -> {
		JSONObject query = Try.of(() -> Utils.parse(jsonStr)).getOrNull();
		if (query != null) {
		    return Mono.just(query);
		} else {
		    return Mono.error(new ParsingException());
		}

	    }).flatMap(query -> partialUpdateOperation.execute(query))
		    .flatMap(json -> this.ok(res, json))
		    .onErrorResume(error -> error(res, error.getMessage()));

	};
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------------------
    private BiFunction<HttpServerRequest, HttpServerResponse, Publisher<Void>> delete() {

	return (req, res) -> {
	    String systemId = req.param("id");

	    if (Utils.notNullAndNotEmpty(systemId)) {
		return deleteOperation.execute(null, systemId).flatMap(json -> this.ok(res, json))
			.onErrorResume(error -> error(res, error.getMessage()));
		
	    } else {
		return req.receive().aggregate().asString().flatMap(jsonStr -> {
		    JSONObject jsonToDelete = Try.of(() -> Utils.parse(jsonStr)).getOrNull();
		    if (jsonToDelete == null) {
			return Mono.error(new ParsingException());
		    } else {
			return Mono.just(jsonToDelete);
		    }

		}).flatMap(jsonToDelete -> deleteOperation.execute(jsonToDelete, null))
			.flatMap(json -> this.ok(res, json)).onErrorResume(error -> error(res, error.getMessage()));
	    }

	};
    }


    // ----------------------------------------------------------------------------------------------------------------------------------------------------------
    private Mono<Void> ok(HttpServerResponse res, JSONObject json) {

	return res.header(CONTENT_TYPE, APPLICATION_JSON).sendString(Mono.just(json.toString())).then();

    }

    private Mono<Void> error(HttpServerResponse res, String message) {

	return res.status(HttpResponseStatus.BAD_REQUEST).sendString(Mono.just(message)).then();

    }
  
        

}

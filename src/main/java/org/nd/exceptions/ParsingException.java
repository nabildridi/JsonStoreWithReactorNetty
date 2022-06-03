package org.nd.exceptions;

public class ParsingException extends Exception { 

    private static final long serialVersionUID = 1L;

    public ParsingException() {
        super();
    }

    @Override
    public String getMessage() {
	return "Unparsable json";
    }   
    
}

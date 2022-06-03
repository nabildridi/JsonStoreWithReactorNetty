package org.nd.exceptions;

public class NotFoundException extends Exception { 

    private static final long serialVersionUID = 1L;

    public NotFoundException() {
        super();
    }
    
    @Override
    public String getMessage() {
	return "unable to find id";
    }
    
}

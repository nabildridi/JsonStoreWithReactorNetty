package org.nd.rx;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;


public class StringArrayReducer implements BiFunction<List<String>, Optional<String>, List<String>> {

    @Override
    public List<String> apply(List<String> result, Optional<String> value) {
	result.add( value.get());
	return result;
    }




}

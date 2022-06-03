package org.nd.rx;

import java.util.function.BiFunction;

import io.vavr.Tuple2;
import io.vavr.collection.TreeMultimap;


public class SortedMapReducer
	implements BiFunction<TreeMultimap<String, String>, Tuple2<String, String>, TreeMultimap<String, String>> {

    @Override
    public TreeMultimap<String, String> apply(TreeMultimap<String, String> resultMap, Tuple2<String, String> pair){
	// reverse : set key in value and value in key to sort by json value later
	resultMap = resultMap.put(pair._2(), pair._1());

	return resultMap;
    }

}

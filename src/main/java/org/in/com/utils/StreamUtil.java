package org.in.com.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import hirondelle.date4j.DateTime;

public class StreamUtil {
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

		Map<Object, Boolean> seen = new ConcurrentHashMap<>();
		return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	public static List<DateTime> limitDateTime(Stream<DateTime> ss, int range) {
		return ss.limit(range).collect(Collectors.toList());
	}
}

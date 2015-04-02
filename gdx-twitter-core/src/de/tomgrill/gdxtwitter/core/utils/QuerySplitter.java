package de.tomgrill.gdxtwitter.core.utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.utils.ObjectMap;

public class QuerySplitter {
	public static ObjectMap<String, String> split(String stringToSplit) {
		ObjectMap<String, String> query_pairs = new ObjectMap<String, String>();
		String[] pairs = stringToSplit.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			// query_pairs.put(URLDecoder.decode(pair.substring(0, idx),
			// "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));

			query_pairs.put(pair.substring(0, idx), pair.substring(idx + 1));
		}
		return query_pairs;
	}

	/**
	 * Allows same keys and no value parameters
	 * 
	 * @param url
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static Map<String, List<String>> advancedSplit(URL url) throws UnsupportedEncodingException {
		final Map<String, List<String>> query_pairs = new LinkedHashMap<String, List<String>>();
		final String[] pairs = url.getQuery().split("&");
		for (String pair : pairs) {
			final int idx = pair.indexOf("=");
			final String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			if (!query_pairs.containsKey(key)) {
				query_pairs.put(key, new LinkedList<String>());
			}
			final String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
			query_pairs.get(key).add(value);
		}
		return query_pairs;
	}
}

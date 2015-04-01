package de.tomgrill.gdxtwitter.core;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PercentEncoder {

	public static final String ENCODING = "UTF-8";

	public static String encode(String s) {
		if (s == null) {
			return "";
		}
		try {
			return URLEncoder.encode(s, ENCODING)
			// OAuth encodes some characters differently:
					.replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
			// This could be done faster with more hand-crafted code.
		} catch (UnsupportedEncodingException wow) {
			throw new RuntimeException(wow.getMessage(), wow);
		}
	}
}

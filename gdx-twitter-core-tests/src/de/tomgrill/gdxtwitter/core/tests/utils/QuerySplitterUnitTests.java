package de.tomgrill.gdxtwitter.core.tests.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.badlogic.gdx.utils.ObjectMap;

import de.tomgrill.gdxtwitter.core.utils.QuerySplitter;

public class QuerySplitterUnitTests {
	private String query = "key1=value1" + "&superkey=supervalue";

	@Test
	public void split() {
		ObjectMap<String, String> map = QuerySplitter.split(query);

		assertTrue(map.containsKey("key1"));
		assertTrue(map.containsKey("superkey"));

		assertEquals("value1", map.get("key1"));
		assertEquals("supervalue", map.get("superkey"));

	}
}

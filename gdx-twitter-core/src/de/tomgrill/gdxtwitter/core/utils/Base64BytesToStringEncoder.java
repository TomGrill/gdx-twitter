package de.tomgrill.gdxtwitter.core.utils;

import com.badlogic.gdx.utils.Base64Coder;

public class Base64BytesToStringEncoder {

	private Base64BytesToStringEncoder() {
	}

	public static String encode(byte[] signature) {
		return new String(Base64Coder.encode(signature));
	}

}

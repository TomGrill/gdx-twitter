package de.tomgrill.gdxtwitter.core;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.OrderedMap;

import de.tomgrill.gdxtwitter.core.utils.Base64BytesToStringEncoder;
import de.tomgrill.gdxtwitter.core.utils.HmacSha1;
import de.tomgrill.gdxtwitter.core.utils.PercentEncoder;

public class TwitterSignature {

	private TwitterRequestType requestType;
	private String baseURL;
	private String consumerSecret;
	private String userTokenSecret;

	private ObjectMap<String, String> parameters = new ObjectMap<String, String>();
	private OrderedMap<String, String> percentEncodedParameters = new OrderedMap<String, String>();

	public TwitterSignature(TwitterRequestType requestType, String baseURL, String consumerSecret, String userTokenSecret, ObjectMap<String, String> parameters) {
		this(requestType, baseURL, consumerSecret, userTokenSecret);
		addParameters(parameters);
	}

	public TwitterSignature(TwitterRequestType requestType, String baseURL, String consumerSecret, String userTokenSecret) {
		this.requestType = requestType;
		this.baseURL = baseURL;
		this.consumerSecret = consumerSecret;
		this.userTokenSecret = userTokenSecret;
	}

	public void addParameters(ObjectMap<String, String> parameters) {
		this.parameters.putAll(parameters);

		if (this.parameters.containsKey("oauth_signature")) {
			this.parameters.remove("oauth_signature");
		}
	}

	private String getParameterString() {

		percentEncodeAndOrderParameters();

		String result = "";

		Array<String> orderedKey = percentEncodedParameters.orderedKeys();
		orderedKey.sort();

		for (int i = 0; i < orderedKey.size; i++) {
			if (result.length() > 0) {
				result += "&";
			}
			result += orderedKey.get(i) + "=" + percentEncodedParameters.get(orderedKey.get(i));
		}

		return result;
	}

	private void percentEncodeAndOrderParameters() {
		percentEncodedParameters = new OrderedMap<String, String>();

		ObjectMap.Entries<String, String> entries = parameters.iterator();

		while (entries.hasNext()) {
			ObjectMap.Entry<String, String> pair = entries.next();
			percentEncodedParameters.put(PercentEncoder.encode(pair.key), PercentEncoder.encode(pair.value));
		}

	}

	public void addParameter(String key, String value) {
		if (key.equals("oauth_signature")) {
			return;
		}
		parameters.put(key, value);
	}

	private String signatureBaseString() {

		String singatureBaseString = requestType.name().toUpperCase() + "&" + PercentEncoder.encode(baseURL) + "&";

		singatureBaseString += PercentEncoder.encode(getParameterString());

		return singatureBaseString;
	}

	private String signingKey() {
		return PercentEncoder.encode(consumerSecret) + "&" + PercentEncoder.encode(userTokenSecret);
	}

	public String getSignature() {

		byte[] signatureBytes = null;
		try {
			signatureBytes = HmacSha1.calculateToBytes(signatureBaseString(), signingKey());
		} catch (Exception e) {
			throw new RuntimeException("This should never happen.");
		}

		return Base64BytesToStringEncoder.encode(signatureBytes);
	}

}

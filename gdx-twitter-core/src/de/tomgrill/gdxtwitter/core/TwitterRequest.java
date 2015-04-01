package de.tomgrill.gdxtwitter.core;

import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.TimeUtils;

public class TwitterRequest {

	private String url;
	private String consumerKey;
	private String consumerSecret;

	private String userToken;
	private String userTokenSecret;

	private TwitterRequestType requestType;

	private OrderedMap<String, String> parameters = new OrderedMap<String, String>();

	private String headerString;

	public TwitterRequest(TwitterRequestType requestType, String url, String consumerKey, String consumerSecret, String userToken, String userTokenSecret) {
		setConsumerKey(consumerKey);
		setConsumerSecret(consumerSecret);
		setUrl(url);
		this.requestType = requestType;
		this.userToken = userToken;
		this.userTokenSecret = userTokenSecret;

	}

	public void put(String key, String value) {
		parameters.put(key, value);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getConsumerKey() {
		return consumerKey;
	}

	public void setConsumerKey(String consumerKey) {
		this.consumerKey = consumerKey;
	}

	public TwitterRequest build() {

		if (this.userToken != null && this.userToken.length() > 0) {
			put("oauth_token", this.userToken);
		}

		headerString = "OAuth ";

		String timestamp = Long.toString(TimeUtils.millis() / 1000L);

		put("oauth_consumer_key", this.consumerKey);
		put("oauth_nonce", timestamp + "NOUNCE");
		put("oauth_signature_method", "HMAC-SHA1");
		put("oauth_timestamp", timestamp);
		put("oauth_version", "1.0");

		TwitterSignature twitterSignature = new TwitterSignature(requestType, this.url, this.consumerSecret, this.userTokenSecret, parameters);
		String signature = twitterSignature.getSignature();
		put("oauth_signature", signature);

		OrderedMap.Entries<String, String> entries = parameters.iterator();

		while (entries.hasNext()) {
			OrderedMap.Entry<String, String> pair = entries.next();

			headerString += PercentEncoder.encode(pair.key) + "=\"" + PercentEncoder.encode(pair.value) + "\"";
			if (entries.hasNext()) {
				headerString += ", ";
			}

		}

		return this;
	}

	public String getHeader() {
		return headerString;
	}

	public String getConsumerSecret() {
		return consumerSecret;
	}

	public void setConsumerSecret(String consumerSecret) {
		this.consumerSecret = consumerSecret;
	}

	public TwitterRequestType getRequestType() {
		return this.requestType;
	}
}

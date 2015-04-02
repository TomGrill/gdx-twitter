package de.tomgrill.gdxtwitter.core;

import com.badlogic.gdx.utils.OrderedMap;
import com.badlogic.gdx.utils.TimeUtils;

import de.tomgrill.gdxtwitter.core.utils.PercentEncoder;

public class TwitterRequest {

	private String url;
	private String consumerKey;
	private String consumerSecret;

	private String token;
	private String tokenSecret;

	private TwitterRequestType requestType;

	private OrderedMap<String, String> parameters = new OrderedMap<String, String>();
	private OrderedMap<String, String> dataParameters = new OrderedMap<String, String>();

	private String headerString = "";
	private String dataString = "";

	public TwitterRequest(TwitterRequestType requestType, String url) {
		this.requestType = requestType;
		setUrl(url);
	}

	TwitterRequest(TwitterRequestType requestType, String url, String consumerKey, String consumerSecret, String token, String tokenSecret) {
		setConsumerKey(consumerKey);
		setConsumerSecret(consumerSecret);
		setUrl(url);
		this.requestType = requestType;
		this.token = token;
		this.tokenSecret = tokenSecret;

	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getTokenSecret() {
		return tokenSecret;
	}

	public void setTokenSecret(String tokenSecret) {
		this.tokenSecret = tokenSecret;
	}

	public TwitterRequest put(String key, String value) {
		parameters.put(key, value);
		return this;

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

		if (this.token != null && this.token.length() > 0) {
			put("oauth_token", this.token);
		}

		headerString = "OAuth ";

		String timestamp = Long.toString(TimeUtils.millis() / 1000L);

		put("oauth_consumer_key", this.consumerKey);
		put("oauth_nonce", timestamp + "NOUNCE");
		put("oauth_signature_method", "HMAC-SHA1");
		put("oauth_timestamp", timestamp);
		put("oauth_version", "1.0");

		TwitterSignature twitterSignature = new TwitterSignature(requestType, this.url, this.consumerSecret, this.tokenSecret, parameters);
		String signature = twitterSignature.getSignature();
		put("oauth_signature", signature);

		OrderedMap.Entries<String, String> entries = parameters.iterator();

		while (entries.hasNext()) {
			OrderedMap.Entry<String, String> pair = entries.next();

			if (pair.key.equals("oauth_token") || pair.key.equals("oauth_signature") || pair.key.equals("oauth_version") || pair.key.equals("oauth_consumer_key")
					|| pair.key.equals("oauth_nonce") || pair.key.equals("oauth_signature_method") || pair.key.equals("oauth_timestamp")) {
				headerString += PercentEncoder.encode(pair.key) + "=\"" + PercentEncoder.encode(pair.value) + "\", ";
			} else {
				dataString += PercentEncoder.encode(pair.key) + "=" + PercentEncoder.encode(pair.value) + "&";
			}
		}

		headerString = headerString.substring(0, headerString.length() - 2);

		if (dataString.length() > 0) {
			dataString = dataString.substring(0, dataString.length() - 1);
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

	public String getData() {
		return dataString;
	}
}

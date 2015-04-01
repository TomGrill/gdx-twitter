/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.tomgrill.gdxtwitter.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpRequestBuilder;

public abstract class TwitterAPI {

	protected boolean isSignedin = false;
	protected TwitterConfig config;
	protected String userToken = null;
	protected String userTokenSecret = null;

	public TwitterAPI(TwitterConfig config) {
		this.config = config;
	}

	public boolean isLoaded() {
		return false;
	}

	public boolean isSignedin() {
		return isSignedin;
	}

	public TwitterConfig getConfig() {
		return config;
	}

	public String getUserToken() {
		return userToken;
	}

	public String getUserTokenSecret() {
		return userTokenSecret;
	}

	abstract public void signin(boolean allowGUI, ResponseListener reponseListener);

	public void verifyCredentials(final ResponseListener listener) {

		TwitterRequest verifyCredentialsRequest = new TwitterRequest(TwitterRequestType.GET, "https://api.twitter.com/1.1/account/verify_credentials.json",
				config.TWITTER_CONSUMER_KEY, config.TWITTER_CONSUMER_SECRET, this.userToken, this.userTokenSecret);

		sendRequest(verifyCredentialsRequest, new HttpResponseListener() {

			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {

				if (httpResponse.getStatus().getStatusCode() == 200) {
					listener.success();
				} else {
					listener.error("ERROR:\n" + httpResponse.getResultAsString());
				}
			}

			@Override
			public void failed(Throwable t) {
				listener.error("ERROR: Connection failed. Returned error message:\n" + t.toString());

			}

			@Override
			public void cancelled() {
				listener.cancel();
			}
		});

	}

	public void sendRequest(TwitterRequest twitterRequest, HttpResponseListener listener) {

		HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
		HttpRequest httpRequest = requestBuilder.newRequest().method(twitterRequest.getRequestType().name()).url(twitterRequest.getUrl()).build();

		httpRequest.setHeader("Authorization", twitterRequest.build().getHeader());
		Gdx.net.sendHttpRequest(httpRequest, listener);

	}

}

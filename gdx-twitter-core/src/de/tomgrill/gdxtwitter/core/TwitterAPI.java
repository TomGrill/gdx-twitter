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

import de.tomgrill.gdxtwitter.core.session.TwitterSession;

public abstract class TwitterAPI {

	private String TAG = "gdx-twitter";

	protected boolean isSignedin = false;

	protected TwitterConfig config;
	protected TwitterSession session;

	public TwitterAPI(TwitterConfig config) {
		this.config = config;

		this.session = this.config.TWITTER_SESSION;
		this.session.restore();
	}

	/**
	 * 
	 * @return true when gdx-twitter is loaded on this platform and can be used.
	 *         false when gdx-twitter is not loaded.
	 */
	public boolean isLoaded() {
		return false;
	}

	/**
	 * Indicates whether a user is signed in with Twitter. Requests can only be
	 * made when the user is signed in. However when the user has unauthorized
	 * your application or your application keys have changed then this will
	 * also return true and your next request will throw an error.
	 * 
	 * @return
	 */
	public boolean isSignedin() {
		return isSignedin;
	}

	/**
	 * Returns the currently stored oauth token. May be null.
	 * 
	 * @return oauth token
	 */
	public String getToken() {
		return session.getToken();
	}

	/**
	 * Sets and stores a oauth token pair (token + secret).
	 * 
	 * @param token
	 *            oauth token
	 * @param secret
	 *            oauth secret
	 */
	public void setTokenAndSecret(String token, String secret) {
		session.setTokenAndSecret(token, secret);
	}

	/**
	 * Returns the currenlty stored oauth token. May be null.
	 * 
	 * @return
	 */
	public String getTokenSecret() {
		return session.getTokenSecret();
	}

	abstract public void signin(boolean allowGUI, ResponseListener reponseListener);

	public void verifyCredentials(String token, String tokenSecret, final ResponseListener listener) {

		TwitterRequest verifyCredentialsRequest = new TwitterRequest(TwitterRequestType.GET, "https://api.twitter.com/1.1/account/verify_credentials.json",
				config.TWITTER_CONSUMER_KEY, config.TWITTER_CONSUMER_SECRET, token, tokenSecret);

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

	private void sendRequest(TwitterRequest twitterRequest, HttpResponseListener listener) {

		HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
		HttpRequest httpRequest = requestBuilder.newRequest().method(twitterRequest.getRequestType().name()).url(twitterRequest.getUrl()).build();

		// System.out.println(twitterRequest.build().getHeader());
		twitterRequest.build();

		httpRequest.setHeader("Authorization", twitterRequest.getHeader());
		httpRequest.setContent(twitterRequest.getData());
		Gdx.net.sendHttpRequest(httpRequest, listener);

	}

	// private void newAPIRequest(TwitterRequestType type, String url,
	// HttpResponseListener listener) {
	// if (isSignedin()) {
	// TwitterRequest request = new TwitterRequest(type, url,
	// config.TWITTER_CONSUMER_KEY, config.TWITTER_CONSUMER_SECRET, getToken(),
	// getTokenSecret());
	// sendRequest(request, listener);
	//
	// } else {
	// Gdx.app.debug(TAG, "Cannot do request when user is signed out.");
	// listener.cancelled();
	// }
	// }

	/**
	 * Makes a new request to Twitter API.
	 * 
	 * @param request
	 *            TwitterRequest
	 * @param listener
	 *            handle response here
	 */
	public void newAPIRequest(TwitterRequest request, HttpResponseListener listener) {
		if (isSignedin()) {
			request.setConsumerKey(config.TWITTER_CONSUMER_KEY);
			request.setConsumerSecret(config.TWITTER_CONSUMER_SECRET);
			request.setToken(getToken());
			request.setTokenSecret(getTokenSecret());

			sendRequest(request, listener);

		} else {
			Gdx.app.debug(TAG, "Cannot do request when user is signed out.");
			listener.cancelled();
		}
	}

	private void resetSession() {
		session.reset();
	}

	/**
	 * Disconnects the user from the TwitterAPI. When deleteSessionData is
	 * false, on the next login TwitterAPI tries to reuse session data.
	 * Otherwise all gained data will be deleted and the user has to give new
	 * authorization on the next login.
	 * 
	 * @param deleteSessionData
	 *            true if you want to delete gained session data. false if u
	 *            want to keep it for later logins.
	 */
	public void signout(boolean deleteSessionData) {
		isSignedin = false;
		if (deleteSessionData) {
			resetSession();
		}
	}

}

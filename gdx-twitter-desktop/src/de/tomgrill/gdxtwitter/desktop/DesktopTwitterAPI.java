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

package de.tomgrill.gdxtwitter.desktop;

import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.HttpStatus;

import de.tomgrill.gdxtwitter.core.TwitterAPI;
import de.tomgrill.gdxtwitter.core.TwitterConfig;
import de.tomgrill.gdxtwitter.core.TwitterResponseListener;

public class DesktopTwitterAPI extends TwitterAPI {

	private static final String TAG = "gdx-twitter";

	private TwitterResponseListener responseListener;

	private OAuthProvider provider;
	private CommonsHttpOAuthConsumer consumer;

	public DesktopTwitterAPI(TwitterConfig config) {
		super(config);

		System.out.println("loaded");

		consumer = new CommonsHttpOAuthConsumer(config.TWITTER_CONSUMER_KEY, config.TWITTER_CONSUMER_SECRET);
		provider = new CommonsHttpOAuthProvider("https://api.twitter.com/oauth/request_token", "https://api.twitter.com/oauth/access_token",
				"https://api.twitter.com/oauth/authorize");
	}

	@Override
	public boolean isLoaded() {
		return true;
	}

	@Override
	public void signin(final boolean allowGUI, final TwitterResponseListener responseListener) {
		System.out.println("sign in");
		this.responseListener = responseListener;

		isSignedin = false;
		if (session.getToken() != null && session.getTokenSecret() != null) {
			verifyCredentials(session.getToken(), session.getTokenSecret(), new TwitterResponseListener() {

				@Override
				public void success(String data) {
					isSignedin = true;
					responseListener.success(data);
				}

				@Override
				public void apiError(HttpStatus response, String data) {
					if (allowGUI) {
						runGUILogin(responseListener);
					} else {
						signout(true);
						responseListener.apiError(response, data);
					}
				}

				@Override
				public void httpError(Throwable t) {

					if (allowGUI) {
						runGUILogin(responseListener);
					} else {
						responseListener.httpError(t);
					}
				}

				@Override
				public void cancelled() {
					if (allowGUI) {
						runGUILogin(responseListener);
					} else {
						responseListener.cancelled();
					}
				}
			});
		} else {
			signout(true);
			if (allowGUI) {
				runGUILogin(responseListener);
			} else {
				Gdx.app.debug(TAG, "Silent login failed.");
			}
		}

	}

	private void runGUILogin(final TwitterResponseListener responseListener) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				JXBrowserTwitterGUI browser = new JXBrowserTwitterGUI();
				browser.setConfig(config);
				browser.setDesktopTwitterAPI(DesktopTwitterAPI.this);

				browser.setConsumer(consumer);
				browser.setProvider(provider);

				browser.show(new TwitterResponseListener() {

					@Override
					public void success(String data) {
						isSignedin = true;

						System.out.println("NEED TO STORE ACCESS TOKENS ???");

						responseListener.success(data);

					}

					@Override
					public void apiError(HttpStatus response, String data) {
						responseListener.apiError(response, data);

					}

					@Override
					public void httpError(Throwable t) {
						responseListener.equals(t.getMessage());

					}

					@Override
					public void cancelled() {
						responseListener.cancelled();

					}
				});

			}
		}).start();

	}

	public void sendDenied() {
		responseListener.apiError(new HttpStatus(400), "Bad Request");
	}

	public void sendVerifier(String verifier) {
		try {
			provider.retrieveAccessToken(consumer, verifier, new String[0]);
			session.setTokenAndSecret(consumer.getToken(), consumer.getTokenSecret());

			verifyCredentials(session.getToken(), session.getTokenSecret(), new TwitterResponseListener() {

				@Override
				public void success(String data) {
					isSignedin = true;
					responseListener.success(data);
				}

				@Override
				public void apiError(HttpStatus response, String data) {
					signout(true);
					responseListener.apiError(response, data);

				}

				@Override
				public void httpError(Throwable t) {
					responseListener.httpError(t);

				}

				@Override
				public void cancelled() {
					responseListener.cancelled();

				}

			});

		} catch (Exception e) {
			responseListener.cancelled();

		}

	}
}

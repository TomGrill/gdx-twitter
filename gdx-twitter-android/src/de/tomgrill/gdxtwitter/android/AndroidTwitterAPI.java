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

package de.tomgrill.gdxtwitter.android;

import android.app.Activity;
import android.content.Intent;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.net.HttpStatus;

import de.tomgrill.gdxtwitter.core.TwitterAPI;
import de.tomgrill.gdxtwitter.core.TwitterConfig;
import de.tomgrill.gdxtwitter.core.TwitterResponseListener;

public class AndroidTwitterAPI extends TwitterAPI implements LifecycleListener {

	private static final String TAG = "gdx-twitter";

	private Activity activity;
	private Intent intent;

	private boolean signinProcessStarted = false;

	protected boolean userHasCanceledSignin = false;

	private TwitterResponseListener reponseListener;

	public AndroidTwitterAPI(Activity activity, TwitterConfig config) {
		super(config);
		this.activity = activity;

	}

	@Override
	public boolean isLoaded() {
		return true;
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
		if (signinProcessStarted) {
			signinProcessStarted = false;

			if (AndroidTwitterAuthIntent.TWITTER_SIGNIN_CANCELED) {
				this.reponseListener.cancelled();
			} else {
				session.setTokenAndSecret(AndroidTwitterAuthIntent.TWITTER_USER_TOKEN, AndroidTwitterAuthIntent.TWITTER_USER_TOKEN_SECRET);
				verifyCredentials(session.getToken(), session.getTokenSecret(), new TwitterResponseListener() {

					@Override
					public void success(String data) {
						isSignedin = true;
						reponseListener.success(data);
					}

					@Override
					public void apiError(HttpStatus response, String data) {
						signout(true);
						reponseListener.apiError(response, data);

					}

					@Override
					public void httpError(Throwable t) {
						reponseListener.httpError(t);

					}

					@Override
					public void cancelled() {
						reponseListener.cancelled();

					}

				});
			}

		}

	}

	@Override
	public void dispose() {
	}

	@Override
	public void signin(final boolean allowGUI, final TwitterResponseListener responseListener) {
		isSignedin = false;
		if (!signinProcessStarted) {
			signinProcessStarted = true;

			if (session.getToken() != null && session.getTokenSecret() != null) {
				verifyCredentials(session.getToken(), session.getTokenSecret(), new TwitterResponseListener() {

					@Override
					public void success(String data) {
						isSignedin = true;
						signinProcessStarted = false;
						responseListener.success(data);

					}

					@Override
					public void apiError(HttpStatus response, String data) {
						if (allowGUI) {
							runGUILogin(responseListener);
						} else {
							signout(true);
							signinProcessStarted = false;
							responseListener.apiError(response, data);
						}

					}

					@Override
					public void httpError(Throwable t) {
						if (allowGUI) {
							runGUILogin(responseListener);
						} else {
							signinProcessStarted = false;
							responseListener.httpError(t);
						}
					}

					@Override
					public void cancelled() {
						if (allowGUI) {
							runGUILogin(responseListener);
						} else {
							signinProcessStarted = false;
							responseListener.cancelled();
						}

					}
				});

			} else {
				if (allowGUI) {
					runGUILogin(responseListener);
				} else {
					signout(true);
					signinProcessStarted = false;
					Gdx.app.debug(TAG, "Silent login failed.");
				}
			}

		}

	}

	private void runGUILogin(final TwitterResponseListener reponseListener) {
		this.reponseListener = reponseListener;
		intent = new Intent(activity, AndroidTwitterAuthIntent.class);
		intent.putExtra("TWITTER_CALLBACK_URL", config.TWITTER_ANDROID_SCHEME + "://" + config.TWITTER_ANDROID_HOST);
		intent.putExtra("TWITTER_CONSUMER_KEY", config.TWITTER_CONSUMER_KEY);
		intent.putExtra("TWITTER_CONSUMER_SECRET", config.TWITTER_CONSUMER_SECRET);
		activity.startActivity(intent);

	}

}

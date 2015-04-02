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

import de.tomgrill.gdxtwitter.core.ResponseListener;
import de.tomgrill.gdxtwitter.core.TwitterAPI;
import de.tomgrill.gdxtwitter.core.TwitterConfig;

public class AndroidTwitterAPI extends TwitterAPI implements LifecycleListener {

	private static final String TAG = "gdx-twitter";

	private Activity activity;
	private Intent intent;

	private boolean signinProcessStarted = false;

	protected boolean userHasCanceledSignin = false;

	private ResponseListener reponseListener;

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
				this.reponseListener.cancel();
			} else {
				session.setTokenAndSecret(AndroidTwitterAuthIntent.TWITTER_USER_TOKEN, AndroidTwitterAuthIntent.TWITTER_USER_TOKEN_SECRET);

				if (session.getToken() != null && session.getTokenSecret() != null) {
					isSignedin = true;
					this.reponseListener.success();
				} else {
					this.reponseListener.error("ERROR WITH GUI LOGIN");
				}
			}

		}

	}

	@Override
	public void dispose() {
	}

	@Override
	public void signin(final boolean allowGUI, final ResponseListener responseListener) {
		isSignedin = false;
		if (!signinProcessStarted) {
			signinProcessStarted = true;

			if (session.getToken() != null && session.getTokenSecret() != null) {
				verifyCredentials(session.getToken(), session.getTokenSecret(), new ResponseListener() {

					@Override
					public void error(String errorMsg) {

						if (allowGUI) {
							runGUILogin(responseListener);
						} else {
							signout(true);
							signinProcessStarted = false;
							responseListener.error(errorMsg);
						}
					}

					@Override
					public void success() {
						isSignedin = true;
						signinProcessStarted = false;
						responseListener.success();
					}

					@Override
					public void cancel() {
						if (allowGUI) {
							runGUILogin(responseListener);
						} else {
							signout(true);
							signinProcessStarted = false;
							responseListener.cancel();
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

	private void runGUILogin(final ResponseListener reponseListener) {
		this.reponseListener = reponseListener;
		intent = new Intent(activity, AndroidTwitterAuthIntent.class);
		intent.putExtra("TWITTER_CALLBACK_URL", config.TWITTER_CALLBACK_URL);
		intent.putExtra("TWITTER_CONSUMER_KEY", config.TWITTER_CONSUMER_KEY);
		intent.putExtra("TWITTER_CONSUMER_SECRET", config.TWITTER_CONSUMER_SECRET);
		activity.startActivity(intent);

	}

}

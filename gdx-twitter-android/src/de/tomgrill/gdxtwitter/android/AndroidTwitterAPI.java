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
import de.tomgrill.gdxtwitter.core.session.TwitterSession;

public class AndroidTwitterAPI extends TwitterAPI implements LifecycleListener {

	private static final String TAG = "gdx-twitter";

	private Activity activity;
	private Intent intent;

	private boolean signinProcessStarted = false;

	protected boolean userHasCanceledSignin = false;

	private ResponseListener reponseListener;

	private TwitterSession twitterSession;

	public AndroidTwitterAPI(Activity activity, TwitterConfig config) {
		super(config);
		this.activity = activity;

		twitterSession = config.TWITTER_SESSION;

		twitterSession.restore();

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
				twitterSession.setTokenAndSecret(AndroidTwitterAuthIntent.TWITTER_USER_TOKEN, AndroidTwitterAuthIntent.TWITTER_USER_TOKEN_SECRET);

				if (twitterSession.getToken() != null && twitterSession.getTokenSecret() != null) {
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
		// TODO Auto-generated method stub

	}

	@Override
	public void signin(final boolean allowGUI, final ResponseListener reponseListener) {
		isSignedin = false;
		if (!signinProcessStarted) {
			signinProcessStarted = true;

			if (twitterSession.getToken() != null && twitterSession.getTokenSecret() != null) {
				System.out.println("verify existing tokens");
				verifyCredentials(twitterSession.getToken(), twitterSession.getTokenSecret(), new ResponseListener() {

					@Override
					public void error(String errorMsg) {

						if (allowGUI) {
							runGUILogin(reponseListener);
						} else {
							resetSession();
							reponseListener.error(errorMsg);
						}
					}

					@Override
					public void success() {
						isSignedin = true;
						reponseListener.success();
					}

					@Override
					public void cancel() {
						if (allowGUI) {
							runGUILogin(reponseListener);
						} else {
							resetSession();
							reponseListener.cancel();
						}
					}
				});

			} else {
				if (allowGUI) {
					runGUILogin(reponseListener);
				} else {
					resetSession();
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
		// intent.putExtra("STORAGE_FILENAME", config.TWITTER_SESSION_FILENAME);
		activity.startActivity(intent);

	}

	// private void loadUserTokensFromStorage() {
	//
	// this.userToken = prefs.getString(config.PREFERECES_VARIABLE_PREFIX +
	// "TWITTER_USER_TOKEN", null);
	// this.userTokenSecret = prefs.getString(config.PREFERECES_VARIABLE_PREFIX
	// + "TWITTER_USER_TOKEN_SECRET", null);
	// this.userHasCanceledSignin =
	// prefs.getBoolean(config.PREFERECES_VARIABLE_PREFIX +
	// "TWITTER_SIGNIN_CANCELED", false);
	//
	// }

	public void resetSession() {
		twitterSession.reset();

		// prefs.putString(config.PREFERECES_VARIABLE_PREFIX +
		// "TWITTER_USER_TOKEN", null);
		// prefs.putString(config.PREFERECES_VARIABLE_PREFIX +
		// "TWITTER_USER_TOKEN_SECRET", null);
		// prefs.putBoolean(config.PREFERECES_VARIABLE_PREFIX +
		// "TWITTER_SIGNIN_CANCELED", false);
		// prefs.flush();
	}

	// private void storeTokens() {
	// prefs.putString(config.PREFERECES_VARIABLE_PREFIX + "TWITTER_USER_TOKEN",
	// userToken);
	// prefs.putString(config.PREFERECES_VARIABLE_PREFIX +
	// "TWITTER_USER_TOKEN_SECRET", userTokenSecret);
	// prefs.flush();
	// }
}

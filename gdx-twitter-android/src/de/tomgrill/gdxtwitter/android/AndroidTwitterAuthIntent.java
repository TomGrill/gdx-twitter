package de.tomgrill.gdxtwitter.android;

import junit.framework.Assert;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class AndroidTwitterAuthIntent extends Activity {

	private static final String TAG = AndroidTwitterAuthIntent.class.getName();

	private OAuthProvider provider;
	private CommonsHttpOAuthConsumer consumer;

	private Intent mIntent;

	private String TWITTER_CONSUMER_KEY;
	private String TWITTER_CONSUMER_SECRET;
	private String TWITTER_CALLBACK_URL;
	private String STORAGE_FILENAME;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mIntent = this.getIntent();

		TWITTER_CONSUMER_KEY = mIntent.getStringExtra("TWITTER_CONSUMER_KEY");
		TWITTER_CONSUMER_SECRET = mIntent.getStringExtra("TWITTER_CONSUMER_SECRET");
		TWITTER_CALLBACK_URL = mIntent.getStringExtra("TWITTER_CALLBACK_URL");
		STORAGE_FILENAME = mIntent.getStringExtra("STORAGE_FILENAME");

		consumer = new CommonsHttpOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);
		provider = new CommonsHttpOAuthProvider("https://api.twitter.com/oauth/request_token", "https://api.twitter.com/oauth/access_token",
				"https://api.twitter.com/oauth/authorize");

		/**
		 * we delete all existing knowledge of this user and assume her canceled
		 * the signin process.
		 */

		Preferences prefs = Gdx.app.getPreferences(STORAGE_FILENAME);
		prefs.putString("TWITTER_USER_TOKEN", null);
		prefs.putString("TWITTER_USER_TOKEN_SECRET", null);
		prefs.putBoolean("TWITTER_SIGNIN_CANCELED", true);
		prefs.flush();

		if (mIntent.getData() == null) {
			try {
				(new RetrieveRequestTokenTask()).execute(new Void[0]);
			} catch (Exception e) {
				Gdx.app.log(TAG, e.getMessage());
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		// Log.d(TAG, "onNewIntent() called");
		Uri uri = intent.getData();
		if (uri != null) {
			// Get the stuff we saved in the async task so we can confirm that
			// it all matches up

			// String token = mSettings.getString(App.REQUEST_TOKEN, null);
			// String secret = mSettings.getString(App.REQUEST_SECRET, null);

			// Intent i = new Intent(this, BloaActivity.class); // Currently how
			// we get back to the main activity

			// if (token == null || secret == null) {
			// throw new IllegalStateException("We should have saved!");
			// }

			String otoken = uri.getQueryParameter(OAuth.OAUTH_TOKEN);
			if (otoken != null) {
				// This is a sanity check which should never fail - hence the
				// assertion
				Assert.assertEquals(otoken, consumer.getToken());

				// We send out and save the request token, but the secret is not
				// the same as the verifier
				// Apparently, the verifier is decoded to get the secret, which
				// is then compared - crafty
				String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

				// We do this in a task now or get an automatic crash
				(new RetrieveAccessTokenTask()).execute(verifier);
			} else {
				String denied = uri.getQueryParameter("denied");
				// Log.e(TAG, "Access denied or canceled. Token returned is: " +
				// denied);
				finish();
			}
		}
	}

	// This is new and required - we can't be decoding the tokens on the UI
	// thread anymore
	private class RetrieveRequestTokenTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {

			String url = null;
			try {
				String authUrl = provider.retrieveRequestToken(consumer, TWITTER_CALLBACK_URL);
				// Toast.makeText(activity, "Please authorize this app!",
				// Toast.LENGTH_LONG).show();

				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));

				return authUrl;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return url;
		}

		@Override
		protected void onPostExecute(String url) {
			super.onPostExecute(url);

			if (url != null) {
				AndroidTwitterAuthIntent.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			} else {
				// TODO what is happening
				Gdx.app.log(TAG, "URL IS NULL - nOT GOOD");
			}
		}
	}

	// This is new and required - we can't be decoding the tokens on the UI
	// thread anymore
	private class RetrieveAccessTokenTask extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {

			try {
				// This is the moment of truth - we could throw here
				provider.retrieveAccessToken(consumer, params[0]);
				return true;
			} catch (Exception e) {
				Gdx.app.log(TAG, e.getMessage());

			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);

			if (success) {
				// Now we can retrieve the goodies
				String token = consumer.getToken();
				String secret = consumer.getTokenSecret();
				Preferences prefs = Gdx.app.getPreferences(STORAGE_FILENAME);
				prefs.putString("TWITTER_USER_TOKEN", token);
				prefs.putString("TWITTER_USER_TOKEN_SECRET", secret);
				prefs.putBoolean("TWITTER_SIGNIN_CANCELED", false);
				prefs.flush();
			}

			finish();
		}
	}

}

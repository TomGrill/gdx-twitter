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

package de.tomgrill.gdxtwitter.ios;

import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.robovm.apple.coregraphics.CGRect;
import org.robovm.apple.foundation.NSError;
import org.robovm.apple.foundation.NSURL;
import org.robovm.apple.foundation.NSURLRequest;
import org.robovm.apple.uikit.UIApplication;
import org.robovm.apple.uikit.UIColor;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIViewAutoresizing;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.apple.uikit.UIWebView;
import org.robovm.apple.uikit.UIWebViewDelegateAdapter;
import org.robovm.apple.uikit.UIWebViewNavigationType;
import org.robovm.apple.uikit.UIWindow;

import com.badlogic.gdx.Gdx;

import de.tomgrill.gdxtwitter.core.ResponseListener;
import de.tomgrill.gdxtwitter.core.TwitterAPI;
import de.tomgrill.gdxtwitter.core.TwitterConfig;

public class IOSTwitterAPI extends TwitterAPI {

	private static final String TAG = "gdx-twitter";

	private UIWebView webView;
	private UIWindow window;

	private UIViewController rootViewController;

	private OAuthProvider provider;
	private CommonsHttpOAuthConsumer consumer;

	public IOSTwitterAPI(TwitterConfig config) {
		super(config);

		consumer = new CommonsHttpOAuthConsumer(config.TWITTER_CONSUMER_KEY, config.TWITTER_CONSUMER_SECRET);
		provider = new CommonsHttpOAuthProvider("https://api.twitter.com/oauth/request_token", "https://api.twitter.com/oauth/access_token",
				"https://api.twitter.com/oauth/authorize");

	}

	@Override
	public boolean isLoaded() {
		return true;
	}

	private void runGUILogin(final ResponseListener responseListener) {
		if (rootViewController == null) {
			rootViewController = new UIViewController();
		}

		if (window == null) {
			window = new UIWindow(UIScreen.getMainScreen().getApplicationFrame());
			window.setRootViewController(rootViewController);
			window.addSubview(rootViewController.getView());
		}

		if (webView == null) {

			// create the UIWebView
			CGRect webFrame = rootViewController.getView().getFrame();
			webFrame.getOrigin().setY(webFrame.getOrigin().getY() + (10 * 2.0) + 27 + 70);
			// .setY(webFrame.getOrigin().getY() + (Constants.TWEEN_MARGIN *
			// 2.0) + Constants.TEXT_FIELD_HEIGHT + 70); // leave
			// room
			// for
			// the
			// URL
			// input
			// field
			webFrame.getSize().setHeight(webFrame.getSize().getHeight() - 40.0);

			webView = new UIWebView(webFrame);
			webView.setBackgroundColor(UIColor.white());
			webView.setScalesPageToFit(true);
			webView.setAutoresizingMask(UIViewAutoresizing.FlexibleWidth);
		}

		webView.setDelegate(new UIWebViewDelegateAdapter() {

			@Override
			public void didStartLoad(UIWebView webView) {
				UIApplication.getSharedApplication().setNetworkActivityIndicatorVisible(true);
			}

			@Override
			public void didFinishLoad(UIWebView webView) {
				UIApplication.getSharedApplication().setNetworkActivityIndicatorVisible(false);

			}

			@Override
			public void didFailLoad(UIWebView webView, NSError error) {
				UIApplication.getSharedApplication().setNetworkActivityIndicatorVisible(false);
			}

			@Override
			public boolean shouldStartLoad(UIWebView webView, NSURLRequest request, UIWebViewNavigationType navigationType) {

				String urlToCall = request.getURL().toString();

				if (urlToCall.contains("oauth_verifier=") && urlToCall.contains(config.TWITTER_CALLBACK_URL)) {
					String oauthIdentifier = "oauth_verifier=";

					int amperIndex = 0;
					amperIndex = urlToCall.indexOf("&", amperIndex);
					String verifier = urlToCall.substring(urlToCall.lastIndexOf(oauthIdentifier) + oauthIdentifier.length(), urlToCall.length());

					webView.stopLoading();
					window.setHidden(true);

					receiveAccessToken(verifier, responseListener);

				}

				return true;
			}

		});
		window.addSubview(webView);

		String authUrl = null;
		;
		try {
			authUrl = provider.retrieveRequestToken(consumer, config.TWITTER_CALLBACK_URL);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (authUrl == null) {
			responseListener.error("Could not build authUrl");
			return;
		}

		window.makeKeyAndVisible();
		webView.loadRequest(new NSURLRequest(new NSURL(authUrl)));

	}

	@Override
	public void signin(final boolean allowGUI, final ResponseListener responseListener) {
		isSignedin = false;
		if (session.getToken() != null && session.getTokenSecret() != null) {
			verifyCredentials(session.getToken(), session.getTokenSecret(), new ResponseListener() {

				@Override
				public void success() {
					isSignedin = true;
					responseListener.success();

				}

				@Override
				public void error(String errorMsg) {
					if (allowGUI) {
						runGUILogin(responseListener);
					} else {
						resetSession();
						responseListener.error(errorMsg);
					}
				}

				@Override
				public void cancel() {
					if (allowGUI) {
						runGUILogin(responseListener);
					} else {
						resetSession();
						responseListener.cancel();
					}

				}
			});
		} else {
			if (allowGUI) {
				runGUILogin(responseListener);
			} else {
				resetSession();
				Gdx.app.debug(TAG, "Silent login failed.");
			}
		}
	}

	private void receiveAccessToken(String verifier, final ResponseListener responseListener) {
		try {
			provider.retrieveAccessToken(consumer, verifier, new String[0]);
			session.setTokenAndSecret(consumer.getToken(), consumer.getTokenSecret());
			responseListener.success();

		} catch (Exception e) {
			responseListener.error(e.getMessage());

		}

	}

}

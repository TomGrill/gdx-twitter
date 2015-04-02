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
import org.robovm.apple.uikit.UIButton;
import org.robovm.apple.uikit.UIColor;
import org.robovm.apple.uikit.UIControl;
import org.robovm.apple.uikit.UIControlContentHorizontalAlignment;
import org.robovm.apple.uikit.UIControlContentVerticalAlignment;
import org.robovm.apple.uikit.UIControlState;
import org.robovm.apple.uikit.UIEvent;
import org.robovm.apple.uikit.UIScreen;
import org.robovm.apple.uikit.UIView;
import org.robovm.apple.uikit.UIViewAutoresizing;
import org.robovm.apple.uikit.UIViewController;
import org.robovm.apple.uikit.UIWebView;
import org.robovm.apple.uikit.UIWebViewDelegateAdapter;
import org.robovm.apple.uikit.UIWebViewNavigationType;
import org.robovm.apple.uikit.UIWindow;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.net.HttpStatus;

import de.tomgrill.gdxtwitter.core.TwitterAPI;
import de.tomgrill.gdxtwitter.core.TwitterConfig;
import de.tomgrill.gdxtwitter.core.TwitterResponseListener;

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

	private void runGUILogin(final TwitterResponseListener responseListener) {
		if (rootViewController == null) {
			rootViewController = new UIViewController();
		}

		if (window == null) {
			window = new UIWindow(UIScreen.getMainScreen().getApplicationFrame());
			window.setRootViewController(rootViewController);
			window.addSubview(rootViewController.getView());
		}

		if (webView == null) {

			CGRect buttonFrame = new CGRect(10f, 0.0, 106.0f, 55f);

			CGRect fullScreen = rootViewController.getView().getFrame();
			UIView topBackground = new UIView(new CGRect(0f, 0f, fullScreen.getWidth(), 55f));
			topBackground.setBackgroundColor(UIColor.white());

			UIView topDivider = new UIView(new CGRect(0f, 55f, fullScreen.getWidth(), 1f));
			topDivider.setBackgroundColor(UIColor.white());
			topDivider.setBackgroundColor(UIColor.darkGray());

			UIButton cancelButton = new UIButton(buttonFrame);

			cancelButton.setTitle("cancel", UIControlState.Normal);
			cancelButton.setContentVerticalAlignment(UIControlContentVerticalAlignment.Center);
			cancelButton.setContentHorizontalAlignment(UIControlContentHorizontalAlignment.Left);
			cancelButton.setTitleColor(UIColor.darkGray(), UIControlState.Normal);
			cancelButton.addOnTouchUpInsideListener(new UIControl.OnTouchUpInsideListener() {

				@Override
				public void onTouchUpInside(UIControl control, UIEvent event) {
					responseListener.cancelled();
					window.setHidden(true);

				}

			});

			topBackground.addSubview(cancelButton);

			window.addSubview(topBackground);
			window.addSubview(topDivider);

			CGRect webFrame = rootViewController.getView().getFrame();
			webFrame.getOrigin().setY(webFrame.getOrigin().getY() + 56f);

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

				// System.out.println(urlToCall);

				if (urlToCall.contains(config.TWITTER_CALLBACK_URL)) {
					if (urlToCall.contains("oauth_verifier=")) {
						String oauthIdentifier = "oauth_verifier=";

						int amperIndex = 0;
						amperIndex = urlToCall.indexOf("&", amperIndex);
						String verifier = urlToCall.substring(urlToCall.lastIndexOf(oauthIdentifier) + oauthIdentifier.length(), urlToCall.length());

						webView.stopLoading();
						window.setHidden(true);

						receiveAccessToken(verifier, responseListener);

					}

					if (urlToCall.contains("denied=")) {
						webView.stopLoading();
						window.setHidden(true);
						responseListener.cancelled();
					}

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
			responseListener.apiError(new HttpStatus(400), "Bad Request");
			return;
		}

		window.makeKeyAndVisible();
		webView.loadRequest(new NSURLRequest(new NSURL(authUrl)));

	}

	@Override
	public void signin(final boolean allowGUI, final TwitterResponseListener responseListener) {
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

	private void receiveAccessToken(String verifier, final TwitterResponseListener responseListener) {
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

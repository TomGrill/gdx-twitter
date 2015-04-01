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
import org.robovm.apple.uikit.UIWebViewDelegate;
import org.robovm.apple.uikit.UIWebViewNavigationType;
import org.robovm.apple.uikit.UIWindow;

import de.tomgrill.gdxtwitter.core.ResponseListener;
import de.tomgrill.gdxtwitter.core.TwitterAPI;
import de.tomgrill.gdxtwitter.core.TwitterConfig;

public class IOSTwitterAPI extends TwitterAPI {

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

	@Override
	public void signin(final boolean allowGUI, final ResponseListener reponseListener) {
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

		webView.setDelegate(new UIWebViewDelegate() {

			@Override
			public void didStartLoad(UIWebView webView) {
				UIApplication.getSharedApplication().setNetworkActivityIndicatorVisible(true);
				System.out.println("started loading");
			}

			@Override
			public void didFinishLoad(UIWebView webView) {
				UIApplication.getSharedApplication().setNetworkActivityIndicatorVisible(false);
				System.out.println("finished loading");

			}

			@Override
			public void didFailLoad(UIWebView webView, NSError error) {
				UIApplication.getSharedApplication().setNetworkActivityIndicatorVisible(false);

				System.out.println("error loading");
				reponseListener.error(error.description());

			}

			@Override
			public boolean shouldStartLoad(UIWebView webView, NSURLRequest request, UIWebViewNavigationType navigationType) {
				System.out.println("shouldStartLoad " + request.getURL());
				return false;
			}
		});
		window.addSubview(webView);

		try {
			String authUrl = provider.retrieveRequestToken(consumer, "http://www.tpronold.de");

		} catch (Exception e) {
			e.printStackTrace();
		}

		window.makeKeyAndVisible();
		webView.loadRequest(new NSURLRequest(new NSURL("http://www.apple.com/")));

	}
}

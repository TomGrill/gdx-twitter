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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import de.tomgrill.gdxtwitter.core.TwitterConfig;

public class JXBrowserTwitterGUI extends Application {

	private static DesktopTwitterAPI desktopTwitterAPI;

	private static TwitterConfig config;

	private static String authUrl;

	private static String url;
	private static WebView browser;
	private static WebEngine engine;
	private static StackPane sp;
	private static Scene root;

	private static Stage primaryStage;

	private static OAuthProvider provider;
	private static CommonsHttpOAuthConsumer consumer;

	public static void open() {

		if (!RunHelper.isStarted) {
			RunHelper.isStarted = true;
			Application.launch(new String());
		}

	}

	private static void generateAuthUrl() {

		try {
			authUrl = provider.retrieveRequestToken(consumer, config.TWITTER_CALLBACK_URL);

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (authUrl == null) {
			JXBrowserTwitterGUI.desktopTwitterAPI.sendDenied();
		} else {
			url = authUrl;
		}

	}

	@Override
	public void start(Stage primaryStage2) throws Exception {

		Platform.setImplicitExit(false);

		generateAuthUrl();

		primaryStage = primaryStage2;
		primaryStage.setAlwaysOnTop(true);
		primaryStage.setTitle("Twitter Signin");

		browser = new WebView();

		engine = browser.getEngine();

		engine.load(url);
		engine.locationProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> arg0, String oldloc, String newLocation) {
				System.out.println(newLocation);
				if (newLocation.contains("oauth_verifier=")) {
					String oauthIdentifier = "oauth_verifier=";

					int amperIndex = 0;
					amperIndex = newLocation.indexOf("&", amperIndex);
					String verifier = newLocation.substring(newLocation.lastIndexOf(oauthIdentifier) + oauthIdentifier.length(), newLocation.length());

					JXBrowserTwitterGUI.desktopTwitterAPI.sendVerifier(verifier);

					closeBrowser();
				}

				if (newLocation.contains("denied=")) {
					JXBrowserTwitterGUI.desktopTwitterAPI.sendCancel();
					closeBrowser();
				}
			}

		});

		sp = new StackPane();
		sp.getChildren().add(browser);

		root = new Scene(sp);

		primaryStage.setScene(root);
		primaryStage.show();

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				JXBrowserTwitterGUI.desktopTwitterAPI.sendCancel();
			}
		});
	}

	private void closeBrowser() {
		primaryStage.close();
	}

	public static void setConfig(TwitterConfig config) {
		JXBrowserTwitterGUI.config = config;

	}

	public static void setDesktopTwitterAPI(DesktopTwitterAPI desktopTwitterAPI) {
		JXBrowserTwitterGUI.desktopTwitterAPI = desktopTwitterAPI;

	}

	public static void setConsumer(CommonsHttpOAuthConsumer consumer) {
		JXBrowserTwitterGUI.consumer = consumer;

	}

	public static void setProvider(OAuthProvider provider) {
		JXBrowserTwitterGUI.provider = provider;

	}

	public static void reuse() {
		generateAuthUrl();
		engine.load(url);
		primaryStage.show();
	}

}

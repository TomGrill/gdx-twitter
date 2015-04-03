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

import com.badlogic.gdx.net.HttpStatus;

import de.tomgrill.gdxtwitter.core.TwitterConfig;
import de.tomgrill.gdxtwitter.core.TwitterResponseListener;

public class JXBrowserTwitterGUI extends Application {

	/**
	 * TODO I really dont like this static variables. But that is doing it for
	 * now. NEEDS REFACTOR
	 */
	private static TwitterResponseListener listener;
	private static boolean applicationIsStartet;

	private static DesktopTwitterAPI desktopTwitterAPI;

	private static TwitterConfig config;

	private static String authUrl;

	private String url;
	private WebView browser;
	private WebEngine engine;
	private StackPane sp;
	private Scene root;

	private Stage primaryStage;

	private static OAuthProvider provider;
	private static CommonsHttpOAuthConsumer consumer;

	public void open() {

		authUrl = null;
		try {
			authUrl = provider.retrieveRequestToken(consumer, "http://tpronold.de/");

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (authUrl == null) {
			listener.apiError(new HttpStatus(400), "Bad Request");
			return;
		}

		if (!applicationIsStartet) {
			applicationIsStartet = true;
			Application.launch(new String());
		}

	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		Platform.setImplicitExit(false);

		url = authUrl;

		this.primaryStage = primaryStage;
		this.primaryStage.setAlwaysOnTop(true);
		this.primaryStage.setTitle("Twitter Signin");

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
					JXBrowserTwitterGUI.desktopTwitterAPI.sendDenied();
					closeBrowser();
				}
			}

		});

		sp = new StackPane();
		sp.getChildren().add(browser);

		root = new Scene(sp);

		this.primaryStage.setScene(root);
		this.primaryStage.show();

		this.primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				JXBrowserTwitterGUI.listener.cancelled();
			}
		});
	}

	private void closeBrowser() {
		primaryStage.close();
	}

	public void show(TwitterResponseListener listener) {
		JXBrowserTwitterGUI.listener = listener;

		open();
	}

	public void setConfig(TwitterConfig config) {
		JXBrowserTwitterGUI.config = config;

	}

	public void setDesktopTwitterAPI(DesktopTwitterAPI desktopTwitterAPI) {
		JXBrowserTwitterGUI.desktopTwitterAPI = desktopTwitterAPI;

	}

	public void setConsumer(CommonsHttpOAuthConsumer consumer) {
		JXBrowserTwitterGUI.consumer = consumer;

	}

	public void setProvider(OAuthProvider provider) {
		JXBrowserTwitterGUI.provider = provider;

	}

}

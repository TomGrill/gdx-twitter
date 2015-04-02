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

package de.tomgrill.gdxtwitter.core;

import com.badlogic.gdx.Gdx;

import de.tomgrill.gdxtwitter.core.session.PreferencesTwitterSession;
import de.tomgrill.gdxtwitter.core.session.TwitterSession;

public class TwitterConfig {

	/**
	 * Put your Consumer Key (API Key) here. Get it at <a
	 * href="https://apps.twitter.com/">https://apps.twitter.com/</a>
	 * 
	 */
	public String TWITTER_CONSUMER_KEY = "";

	/**
	 * Put your Consumer Secret (API Secret) here. Get it at <a
	 * href="https://apps.twitter.com/">https://apps.twitter.com/</a>
	 * 
	 */
	public String TWITTER_CONSUMER_SECRET = "";

	/*
	 * ##########################################################################
	 * 
	 * 
	 * Only edit settings below this line if you know what you do. Expert usage
	 * only
	 * 
	 * ##########################################################################
	 */

	/**
	 * It is NOT recommended to change this value. If you do so you have to edit
	 * your AndroidManifest.xml as well.
	 */
	public String TWITTER_CALLBACK_URL = "gdx-twitter://twitter";

	/**
	 * Prefix for variable names.
	 */
	public String PREFERECES_VARIABLE_PREFIX = "gdx-twitter.";

	/**
	 * Filename where session data is stored.
	 */
	public String TWITTER_SESSION_FILENAME = ".gdx-twitter-session";

	/**
	 * Class which manages the Twitter session. Default is
	 * {@link PreferencesTwitterSession}. You can write your own session manager
	 * class. For example if you want to store the tokens in a database. Must
	 * implement {@link TwitterSession}.
	 */
	public TwitterSession TWITTER_SESSION = new PreferencesTwitterSession(Gdx.app.getPreferences(TWITTER_SESSION_FILENAME), PREFERECES_VARIABLE_PREFIX);

}

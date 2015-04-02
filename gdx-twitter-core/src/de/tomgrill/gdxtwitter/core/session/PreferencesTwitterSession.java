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

package de.tomgrill.gdxtwitter.core.session;

import com.badlogic.gdx.Preferences;

public class PreferencesTwitterSession implements TwitterSession {

	private String token = null;
	private String tokenSecret = null;
	private String variablePrefix = "";

	private Preferences prefs;

	private boolean isLoaded = true;

	public PreferencesTwitterSession(Preferences prefs, String variablePrefix) {
		this.prefs = prefs;
		this.variablePrefix = variablePrefix;
	}

	public PreferencesTwitterSession(Preferences prefs) {
		this(prefs, "");
	}

	@Override
	public boolean restore() {

		token = prefs.getString(variablePrefix + "TOKEN", null);
		tokenSecret = prefs.getString(variablePrefix + "TOKEN_SECRET", null);

		if (token != null && tokenSecret != null) {
			isLoaded = true;
			return true;
		}
		isLoaded = false;
		return false;
	}

	@Override
	public String getToken() {
		return token;
	}

	@Override
	public String getTokenSecret() {
		return tokenSecret;
	}

	@Override
	public void setTokenAndSecret(String token, String tokenSecret) {
		isLoaded = false;

		if (token != null && tokenSecret != null) {

			this.token = token;
			this.tokenSecret = tokenSecret;

			prefs.putString(variablePrefix + "TOKEN", this.token);
			prefs.putString(variablePrefix + "TOKEN_SECRET", this.tokenSecret);

			prefs.flush();

			isLoaded = true;
		}

	}

	@Override
	public void reset() {
		prefs.remove(variablePrefix + "TOKEN");
		prefs.remove(variablePrefix + "TOKEN_SECRET");
		prefs.flush();

		token = null;
		tokenSecret = null;

		isLoaded = false;

	}

	@Override
	public boolean isLoaded() {
		return isLoaded;
	}

}

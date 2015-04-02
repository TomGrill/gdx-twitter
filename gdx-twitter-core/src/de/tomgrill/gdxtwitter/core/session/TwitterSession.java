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

public interface TwitterSession {

	/**
	 * Loads an existing pair of tokens (token + secret).
	 * 
	 * @return whether token pair has been loaded successfully
	 */
	public boolean restore();

	public String getToken();

	public String getTokenSecret();

	/**
	 * Sets a token pair (token + secret).
	 * 
	 * @param token
	 *            the user token
	 * @param secret
	 *            the user token secret
	 */
	public void setTokenAndSecret(String token, String secret);

	/**
	 * resets and deletes all session data.
	 */
	public void reset();

	/**
	 * Returns true when there are session tokens available. This does not mean
	 * that available session tokens are valid.
	 * 
	 * @return true when tokens are available else returns false.
	 */
	public boolean isLoaded();

}

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

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;

public class TwitterSystem {

	public static final String TAG = "gdx-twitter";

	private TwitterAPI twitterAPI;
	private TwitterConfig config;

	private Class<?> gdxClazz = null;
	private Object gdxAppObject = null;

	public TwitterSystem(TwitterConfig config) {
		this.config = config;

		loadGdxReflections();
		tryLoadAndroidTwitterAPI();
	}

	private void loadGdxReflections() {

		try {
			gdxClazz = ClassReflection.forName("com.badlogic.gdx.Gdx");
			gdxAppObject = ClassReflection.getField(gdxClazz, "app").get(null);

		} catch (ReflectionException e) {
			throw new RuntimeException("No libGDX environment. \n");
		}

	}

	private void tryLoadAndroidTwitterAPI() {
		if (Gdx.app.getType() == ApplicationType.Android) {
			try {

				Class<?> activityClazz = ClassReflection.forName("android.app.Activity");

				Class<?> twitterClazz = ClassReflection.forName("de.tomgrill.gdxtwitter.android.AndroidTwitterAPI");

				Object activity = null;

				if (ClassReflection.isAssignableFrom(activityClazz, gdxAppObject.getClass())) {

					activity = gdxAppObject;
				} else {

					Class<?> supportFragmentClass = findClass("android.support.v4.app.Fragment");
					// {
					if (supportFragmentClass != null && ClassReflection.isAssignableFrom(supportFragmentClass, gdxAppObject.getClass())) {

						activity = ClassReflection.getMethod(supportFragmentClass, "getActivity").invoke(gdxAppObject);
					} else {
						Class<?> fragmentClass = findClass("android.app.Fragment");
						if (fragmentClass != null && ClassReflection.isAssignableFrom(fragmentClass, gdxAppObject.getClass())) {
							activity = ClassReflection.getMethod(fragmentClass, "getActivity").invoke(gdxAppObject);
						}
					}

				}

				if (activity == null) {
					throw new RuntimeException("Can't find your gdx activity to instantiate gdx-twitter. " + "Looks like you have implemented AndroidApplication without using "
							+ "Activity or Fragment classes or Activity is not available at the moment");
				}

				Object twitter = ClassReflection.getConstructor(twitterClazz, activityClazz, TwitterConfig.class).newInstance(activity, config);

				twitterAPI = (TwitterAPI) twitter;

				Gdx.app.debug(TAG, "gdx-twitter for Android loaded successfully.");

			} catch (Exception e) {
				Gdx.app.debug(TAG, "Error creating gdx-twitter for Android (are the gdx-twitter **.jar files installed?). \n");
				e.printStackTrace();
			}
		}
	}

	public TwitterAPI getTwitterAPI() {
		return twitterAPI;
	}

	/** @return null if class is not available in runtime */
	private static Class<?> findClass(String name) {
		try {
			return ClassReflection.forName(name);
		} catch (Exception e) {
			return null;
		}
	}
}
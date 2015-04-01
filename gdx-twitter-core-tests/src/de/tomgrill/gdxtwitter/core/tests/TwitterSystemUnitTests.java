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

package de.tomgrill.gdxtwitter.core.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import de.tomgrill.gdxtwitter.core.TwitterConfig;
import de.tomgrill.gdxtwitter.core.TwitterSystem;
import de.tomgrill.gdxtwitter.core.tests.stubs.ActivityStub;
import de.tomgrill.gdxtwitter.core.tests.stubs.FragmentStub;
import de.tomgrill.gdxtwitter.core.tests.stubs.GdxStub;
import de.tomgrill.gdxtwitter.core.tests.stubs.SupportFragmentStub;
import de.tomgrill.gdxtwitter.core.tests.stubs.TwitterAPIStub;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ClassReflection.class, Field.class, Constructor.class, Method.class })
public class TwitterSystemUnitTests {

	private TwitterConfig config;

	private ClassReflection classReflectionMock;
	private Field fieldMock;
	private Constructor constructorMock;
	private Method methodMock;

	private ActivityStub activityStub;
	private TwitterAPIStub twitterAPIStub;
	private GdxStub gdxStub;
	private SupportFragmentStub supportFragmentStub;
	private FragmentStub fragmentStub;

	private TwitterSystem fixture;

	@SuppressWarnings("static-access")
	@Before
	public void setup() {
		config = new TwitterConfig();

		PowerMockito.mockStatic(ClassReflection.class);
		classReflectionMock = Mockito.mock(ClassReflection.class);

		PowerMockito.mockStatic(Field.class);
		fieldMock = Mockito.mock(Field.class);

		PowerMockito.mockStatic(Constructor.class);
		constructorMock = Mockito.mock(Constructor.class);

		PowerMockito.mockStatic(Method.class);
		methodMock = Mockito.mock(Method.class);

		activityStub = new ActivityStub();
		twitterAPIStub = new TwitterAPIStub();
		gdxStub = new GdxStub();
		supportFragmentStub = new SupportFragmentStub();
		fragmentStub = new FragmentStub();

		Gdx.app = Mockito.mock(HeadlessApplication.class);

		try {
			Mockito.when(classReflectionMock.forName("com.badlogic.gdx.Gdx")).thenReturn(gdxStub.getClass());
			Mockito.when(classReflectionMock.getField(gdxStub.getClass(), "app")).thenReturn(fieldMock);
			Mockito.when(fieldMock.get(null)).thenReturn(Gdx.app);

		} catch (ReflectionException e) {
		}
	}

	@SuppressWarnings("static-access")
	private void androidSetup() {

		try {
			Mockito.when(classReflectionMock.forName("android.app.Activity")).thenReturn(activityStub.getClass());
			Mockito.when(classReflectionMock.forName("de.tomgrill.gdxtwitter.android.AndroidTwitterAPI")).thenReturn(twitterAPIStub.getClass());
			Mockito.when(Gdx.app.getType()).thenReturn(ApplicationType.Android);
			Mockito.when(classReflectionMock.getConstructor(twitterAPIStub.getClass(), activityStub.getClass(), TwitterConfig.class)).thenReturn(constructorMock);

		} catch (ReflectionException e) {
		}
	}

	@Test
	@SuppressWarnings("static-access")
	public void androidIsLoadedWithActivityMode() {
		androidSetup();

		try {
			Mockito.when(constructorMock.newInstance(Gdx.app, config)).thenReturn(twitterAPIStub);

		} catch (ReflectionException e) {
			e.printStackTrace();
		}

		Mockito.when(classReflectionMock.isAssignableFrom(activityStub.getClass(), Gdx.app.getClass())).thenReturn(true);

		fixture = new TwitterSystem(config);
		assertEquals(twitterAPIStub, fixture.getTwitterAPI());
	}

	@Test
	@SuppressWarnings("static-access")
	public void androidIsLoadedWithV4Fragment() {
		androidSetup();

		Mockito.when(classReflectionMock.isAssignableFrom(activityStub.getClass(), Gdx.app.getClass())).thenReturn(false);
		Mockito.when(classReflectionMock.isAssignableFrom(supportFragmentStub.getClass(), Gdx.app.getClass())).thenReturn(true);

		try {
			Mockito.when(constructorMock.newInstance(activityStub, config)).thenReturn(twitterAPIStub);
			Mockito.when(classReflectionMock.forName("android.support.v4.app.Fragment")).thenReturn(supportFragmentStub.getClass());
			Mockito.when(classReflectionMock.getMethod(supportFragmentStub.getClass(), "getActivity")).thenReturn(methodMock);
			Mockito.when(methodMock.invoke(Gdx.app)).thenReturn(activityStub);

		} catch (ReflectionException e) {
		}

		fixture = new TwitterSystem(config);

		assertEquals(twitterAPIStub, fixture.getTwitterAPI());
	}

	@Test
	@SuppressWarnings("static-access")
	public void androidIsLoadedWithFragment() {
		androidSetup();

		Mockito.when(classReflectionMock.isAssignableFrom(activityStub.getClass(), Gdx.app.getClass())).thenReturn(false);
		Mockito.when(classReflectionMock.isAssignableFrom(supportFragmentStub.getClass(), Gdx.app.getClass())).thenReturn(false);
		Mockito.when(classReflectionMock.isAssignableFrom(fragmentStub.getClass(), Gdx.app.getClass())).thenReturn(true);

		try {
			Mockito.when(constructorMock.newInstance(activityStub, config)).thenReturn(twitterAPIStub);
			Mockito.when(classReflectionMock.forName("android.app.Fragment")).thenReturn(fragmentStub.getClass());
			Mockito.when(classReflectionMock.getMethod(fragmentStub.getClass(), "getActivity")).thenReturn(methodMock);
			Mockito.when(methodMock.invoke(Gdx.app)).thenReturn(activityStub);

		} catch (ReflectionException e) {
		}

		fixture = new TwitterSystem(config);
		assertEquals(twitterAPIStub, fixture.getTwitterAPI());
	}
}

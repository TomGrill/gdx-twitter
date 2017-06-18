# gdx-twitter
libGDX extension providing cross-platform support for Twitter API.

## Updates & News
Follow me to receive release updates about this and my other projects (Promise: No BS posts)

https://twitter.com/TomGrillGames and https://www.facebook.com/tomgrillgames

I will also stream sometimes when developing at https://www.twitch.tv/tomgrill and write a blog article from time to time at http://tomgrill.de 

## Version
Current status i **beta**. (It is not recommended to use this library in production releases.)

Current snapshot: **0.1.1**

Current stable: **not yet existing**

##Supported Platforms
Android, iOS, Desktop

## That is how Twitter API works.

Basically you send an HTTP request to Twitter and you will get a result which is nothing more than a JSON string. Before you can use the Twitter API you need a OAuth token and secret for a user allowing you to make requests on the users behalf. The tokens are stored and can be used for future requests.


**Read more:**

Sign in: https://dev.twitter.com/web/sign-in

Twitter API: https://dev.twitter.com/overview/api

## Setting up your Twitter App
Go to https://apps.twitter.com/ and create a new app. Depending on your requirements edit the access level accordingly. You should have a Consumer Key (API Key) and Consumer Secret (API Secret) by now. Keep those keys secret!!

Also make sure to uncheck "Enable Callback Locking (It is recommended to enable callback locking to ensure apps cannot overwrite the callback url)" option in your app settings.

Note: You must create a callback URL, even if you do not use it. 

## Installation

**Core**

Add this to your libGDX build.gradle
```
project(":core") {
	dependencies {
	    ...
	    compile "de.tomgrill.gdxtwitter:gdx-twitter-core:0.1.1-SNAPSHOT"
	    ...
	}
}
```

**Android**

Add this to your AndroidManifest.xml
```
<uses-permission android:name="android.permission.INTERNET" />

<application
	...
 	<activity
            android:name="de.tomgrill.gdxtwitter.android.AndroidTwitterAuthIntent"
            android:launchMode="singleTask"
        >
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data android:scheme="gdx-twitter" android:host="twitter" />
            </intent-filter>
        </activity>
	...
</application>
```
Add this to your libGDX build.gradle
```
project(":android") {
	dependencies {
	    ...
	    compile "de.tomgrill.gdxtwitter:gdx-twitter-android:0.1.1-SNAPSHOT"
	    ...
	}
}
```

**iOS**

Add this to your robovm.xml
```
<forceLinkClasses>
    ....
    <pattern>de.tomgrill.gdxtwitter.ios.IOSTwitterAPI</pattern>
</forceLinkClasses>
```

Add this to your libGDX build.gradle
```
project(":ios") {
	dependencies {
	    ...
	    compile "de.tomgrill.gdxtwitter:gdx-twitter-ios:0.1.1-SNAPSHOT"
	    ...
	}
}
```


**Desktop**

```
project(":desktop") {
	dependencies {
	    ...
	    compile "de.tomgrill.gdxtwitter:gdx-twitter-desktop:0.1.1-SNAPSHOT"
	    ...
	}
}
```


## Usage

**View the libGDX Twitter sample app**
https://github.com/TomGrill/gdx-twitter-app


**Enable**
```
Gdx.app.setLogLevel(Application.LOG_DEBUG); // only if you want log output

TwitterConfig twitterConfig = new TwitterConfig();
twitterConfig.TWITTER_CONSUMER_KEY = "YOUR_CONSUMER_KEY_HERE"; 
twitterConfig.TWITTER_CONSUMER_SECRET = "YOUR_CONSUMER_SECRET_HERE"; 
twitterConfig.TWITTER_CALLBACK_URL = "SET THIS TO THE SAME URL YOU ADDED TO YOUR APP SETUP ABOVE"; 

TwitterSystem twitterSystem = new TwitterSystem(twitterConfig);
TwitterAPI TwitterAPI =twitterSystem.getTwitterAPI(); 
```

**Signin with Twitter**

This will start the signin process for the current platform. 
The first parameter enables the GUI signin when set true. 
When set false the login process will be done silently in the background if there is an existing session which can be reused.

```
if (twitterAPI.isLoaded() && !twitterAPI.isSignedin()) {
	twitterAPI.signin(true, new TwitterResponseListener() {

		@Override
		public void success(String data) {
			System.out.println("Signin successfull" + data);

		}

		@Override
		public void apiError(HttpStatus response, String data) {
			System.out.println("Signin with API error: " + data);

		}

		@Override
		public void httpError(Throwable t) {
			System.out.println("Signin with http error: " + t.getMessage());

		}

		@Override
		public void cancelled() {
			System.out.println("Signin canceled");

		}
	});
}
```

**Making a API request**

This is an example for a tweet request. (https://dev.twitter.com/rest/reference/post/statuses/update) The Twitter API allows a lot of requests. I did not cover all possibles. So there might be stuff that does not work yet.

Add all required parameters with put(..) to your request. In this case we just need the **status** parameter.


```
TwitterRequest tweetTextRequest = new TwitterRequest(TwitterRequestType.POST, "https://api.twitter.com/1.1/statuses/update.json");
tweetTextRequest.put("status", "Awesome, this is my first tweet with https://github.com/TomGrill/gdx-twitter. Thanks @TomGrillGames");

twitterAPI.newAPIRequest(tweetTextRequest, new TwitterResponseListener() {

	@Override
	public void cancelled() {
		System.out.println("cancelled");

	}

	@Override
	public void success(String data) {
		System.out.println("success: " + data);

	}

	@Override
	public void apiError(HttpStatus response, String data) {
		System.out.println("apiError " + data);

	}

	@Override
	public void httpError(Throwable t) {
		System.out.println("httpError" + t.getMessage());

	}
});
```

** Proguard settings **

```
-keep class com.badlogic.gdx.Gdx { *; }
-keep class com.badlogic.gdx.Application { ; }
-keep class com.badlogic.gdx.LifecycleListener { ; }
-keep class de.tomgrill.gdxtwitter. { *; }
```

## Release History

Release history for major milestones (available via Maven):

* Version 0.1.0: Initial Release
* Version 0.1.1: Add timeout variable

## Reporting Issues

Something not working quite as expected? Do you need a feature that has not been implemented yet? Check the issue tracker and add a new one if your problem is not already listed. Please try to provide a detailed description of your problem, including the steps to reproduce it.

## Contributing

Awesome! If you would like to contribute with a new feature or a bugfix, fork this repo and submit a pull request.

## License

The gdx-twitter project is licensed under the Apache 2 License, meaning you can use it free of charge, without strings attached in commercial and non-commercial projects. We love to get (non-mandatory) credit in case you release a game or app using gdx-twitter!

# gdx-twitter
libGDX extension providing cross-platform support for Twitter API.

## Version
Current status i **beta**. (It is not recommended to use this library in production releases.)

Current snapshot: **0.1.0**

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

## Installation

**Core**

Add this to your libGDX build.gradle
```
project(":core") {
	dependencies {
	    ...
	    compile "de.tomgrill.gdxtwitter:gdx-twitter-core:0.1.0-SNAPSHOT"
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
	    compile "de.tomgrill.gdxtwitter:gdx-twitter-android:0.1.0-SNAPSHOT"
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
	    compile "de.tomgrill.gdxtwitter:gdx-twitter-ios:0.1.0-SNAPSHOT"
	    ...
	}
}
```


**Desktop**

```
project(":desktop") {
	dependencies {
	    ...
	    compile "de.tomgrill.gdxtwitter:gdx-twitter-desktop:0.1.0-SNAPSHOT"
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

This is an example for a tweet request. The Twitter API allows a lot of requests. I did not cover all possibles. So there might be stuff that does not work yet.
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

package de.tomgrill.gdxtwitter.core.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.badlogic.gdx.utils.ObjectMap;

import de.tomgrill.gdxtwitter.core.TwitterRequestType;
import de.tomgrill.gdxtwitter.core.TwitterSignature;

public class TwitterSignatureUnitTests {

	private TwitterSignature fixture;

	@Before
	public void setup() {

		/**
		 * Test based of values of
		 * https://dev.twitter.com/oauth/overview/creating-signatures
		 */

		String consumerSecret = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw";
		String oAuthTokenSecret = "LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE";

		fixture = new TwitterSignature(TwitterRequestType.POST, "https://api.twitter.com/1/statuses/update.json", consumerSecret, oAuthTokenSecret);
		fixture.addParameter("status", "Hello Ladies + Gentlemen, a signed OAuth request!");
		fixture.addParameter("include_entities", "true");
		fixture.addParameter("oauth_consumer_key", "xvz1evFS4wEEPTGEFPHBog");
		fixture.addParameter("oauth_nonce", "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg");
		fixture.addParameter("oauth_signature_method", "HMAC-SHA1");
		fixture.addParameter("oauth_timestamp", "1318622958");
		fixture.addParameter("oauth_token", "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb");
		fixture.addParameter("oauth_version", "1.0");

	}

	@Test
	public void signaturIsCorrect() {
		assertEquals("tnnArxj06cWHq44gCs1OSKk/jLY=", fixture.getSignature());
	}

	@Test
	public void parametero_oauth_signature_isIgnoredWhenBuildingSingature() {
		fixture.addParameter("oauth_signature", "abcted");

		ObjectMap<String, String> parametersToAdd = new ObjectMap<String, String>();
		parametersToAdd.put("oauth_signature", "abcted");

		fixture.addParameters(parametersToAdd);

		assertEquals("tnnArxj06cWHq44gCs1OSKk/jLY=", fixture.getSignature());

	}
}

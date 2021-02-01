package fr.be.your.self.backend;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map.Entry;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;

public class GoogleTokenVerify {

	public static void main(String[] args) throws GeneralSecurityException, IOException {
		final String idTokenValue = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjUzYzY2YWFiNTBjZmRkOTFhMTQzNTBhNjY0ODJkYjM4MDBjODNjNjMiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiI0OTA3OTk1MDM0NzMtbXRqOThmNDEyNWkyamRma2RjN2txMmNjMTh0YmYybHQuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiI0OTA3OTk1MDM0NzMtN3ZoOW82Zmd0cWxwMDR1NWphNGx2bWU1OG5la2p0dmQuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDk5OTA2OTY4ODAyOTgwODUwNTYiLCJlbWFpbCI6ImF0b3oubmV0QGdtYWlsLmNvbSIsImVtYWlsX3ZlcmlmaWVkIjp0cnVlLCJuYW1lIjoiR29vZCBGcmllbmQiLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EtL0FPaDE0R2l5cWkzWW9keXlFcVdvcDFuU1o1ekJpZ0JDUVJkZEJzYVl5eDQ1RXc9czk2LWMiLCJnaXZlbl9uYW1lIjoiR29vZCIsImZhbWlseV9uYW1lIjoiRnJpZW5kIiwibG9jYWxlIjoiZW4iLCJpYXQiOjE1ODUwNjYwNjksImV4cCI6MTU4NTA2OTY2OX0.Qww9NAPjP_-6GUfi8qR0KodKSQAnFcEvruudsvlhafzZZUGq_jQGWIy_u0i3aVWnHfQC6sBAzYd5WuaNav9XDHJXFleH2T-EYAOvrXuFjwUmKoQAfYrlkoMqajUnLTa0Nhhv00IhYiHfaINezhpM0_cZ7Lpja-VLN0aogEeQ8Fbp0UHKxeJmtiVAovOOmopE7W0ytmTF4iuYgrK8jXLufAjsIkEFigX0Sd30zWqUK787Xr14AciHOie3kVGVhhhV3uYeiJqf-0sXpIriWVJlVQXwhtIvFHsc2X735ZqampFWEnxs09jzCEFbPWAD15AlphAb4blr-0E35RsUf3uVpw";
		final String googleClientId = "490799503473-7vh9o6fgtqlp04u5ja4lvme58nekjtvd.apps.googleusercontent.com";

		final HttpTransport transport = new NetHttpTransport();
		final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

		final GoogleIdTokenVerifier tokenVerifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
				.setAudience(Collections.singletonList(googleClientId)).build();

		final GoogleIdToken idToken = tokenVerifier.verify(idTokenValue);
		final Payload payload = idToken.getPayload();
		final String userId = payload.getSubject();

		boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
		//String displayName = (String) payload.get("displayName");
		String name = (String) payload.get("name");
		String pictureUrl = (String) payload.get("picture");
		String locale = (String) payload.get("locale");
		String familyName = (String) payload.get("family_name");
		String givenName = (String) payload.get("given_name");

		System.out.println("UserId: " + userId);
		System.out.println("Email: " + payload.getEmail());
		//System.out.println("Type: " + payload.getType());
		System.out.println("Email Verify: " + emailVerified);
		//System.out.println("Display Name: " + displayName);
		System.out.println("Name: " + name);
		System.out.println("Picture: " + pictureUrl);
		System.out.println("Locale: " + locale);
		System.out.println("Family Name: " + familyName);
		System.out.println("Given Name: " + givenName);
		
		System.out.println();
		System.out.println();
		for (Entry<String, Object> item : payload.entrySet()) {
			System.out.println(item.getKey() + " => " + item.getValue());
		}
	}

}

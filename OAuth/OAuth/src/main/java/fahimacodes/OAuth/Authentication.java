package fahimacodes.OAuth;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.json.JSONObject;

/**
 * POST requests sent to retrieve access token & refresh token,
 * and refresh access token upon expiry
 * Originally used for Zoom OAuth 2.0 application
 */
public class Authentication {
	private OAuth oAuth = new OAuth();

	public static void main(String[] args) throws IOException, InterruptedException {

		String clientID = "CLIENT ID FROM OAUTH APP";
		String clientSecret = "CLIENT SECRET FROM OAUTH APP";
		String redirectURI = "URI TO HANDLE SUCCESSFUL AUTHORIZATION FROM OAUTH APP";
		String code = "AUTHORIZATION CODE";

		Authentication auth = new Authentication();

		auth.accessToken(clientID, clientSecret, code, redirectURI);
		auth.refreshAccessToken(clientID, clientSecret);
	}

	private OAuth accessToken(String clientID, String clientSecret, String code, String redirectURI)
			throws IOException, InterruptedException {

		String keys = clientID + ":" + clientSecret;

		HashMap<String, String> parameters = new HashMap<>();
		parameters.put("grant_type", "authorization_code");
		parameters.put("code", code);
		parameters.put("redirect_uri", redirectURI);
		String form = parameters.keySet().stream()
				.map(key -> key + "=" + URLEncoder.encode(parameters.get(key), StandardCharsets.UTF_8))
				.collect(Collectors.joining("&"));

		String encoding = Base64.getEncoder().encodeToString(keys.getBytes());
		HttpClient client = HttpClient.newHttpClient();
		// Update URI according to specified endpoint 
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://zoom.us/oauth/token"))
				.headers("Content-Type", "application/x-www-form-urlencoded", "Authorization", "Basic " + encoding)
				.POST(BodyPublishers.ofString(form)).build();
		HttpResponse<?> response = client.send(request, BodyHandlers.ofString());
		System.out.println("ACCESS TOKEN RESPONSE: " + response.body().toString());

		JSONObject jsonObj = new JSONObject(response.body().toString());

		String access_token = jsonObj.getString("access_token");
		String refresh_token = jsonObj.getString("refresh_token");
		oAuth.setAccessToken(access_token);
		oAuth.setRefreshToken(refresh_token);

		return oAuth;
	}

	private OAuth refreshAccessToken(String clientID, String clientSecret) throws IOException, InterruptedException {

		String keys = clientID + ":" + clientSecret;

		HashMap<String, String> parameters = new HashMap<>();
		parameters.put("grant_type", "refresh_token");
		parameters.put("refresh_token", oAuth.getRefreshToken());
		String form = parameters.keySet().stream()
				.map(key -> key + "=" + URLEncoder.encode(parameters.get(key), StandardCharsets.UTF_8))
				.collect(Collectors.joining("&"));

		String encoding = Base64.getEncoder().encodeToString(keys.getBytes());
		HttpClient client = HttpClient.newHttpClient();
		// Update URI according to specified endpoint 
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://zoom.us/oauth/token"))
				.headers("Content-Type", "application/x-www-form-urlencoded", "Authorization", "Basic " + encoding)
				.POST(BodyPublishers.ofString(form)).build();
		HttpResponse<?> response = client.send(request, BodyHandlers.ofString());
		System.out.println("REFRESH ACCESS TOKEN RESPONSE: " + response.body().toString());

		JSONObject jsonObj = new JSONObject(response.body().toString());

		String access_token = jsonObj.getString("access_token");
		String refresh_token = jsonObj.getString("refresh_token");
		oAuth.setAccessToken(access_token);
		oAuth.setRefreshToken(refresh_token);

		return oAuth;
	}

	/**
	* Container class for OAuth 2.0 to hold authentication & authorization token 
	 * of users to make subsequent Zoom API requests
	 */
	public class OAuth {

		private String accessToken;
		private String refreshToken;

		public OAuth() {
		}

		public OAuth(String accessToken, String refreshToken) {

			this.setAccessToken(accessToken);
			this.setRefreshToken(refreshToken);
		}

		public String getAccessToken() {
			return accessToken;
		}

		public void setAccessToken(String accessToken) {
			this.accessToken = accessToken;
		}

		public String getRefreshToken() {
			return refreshToken;
		}

		public void setRefreshToken(String refreshToken) {
			this.refreshToken = refreshToken;
		}
	}
}

package controllers;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.BadRequestException;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.CurrentUserRequest;
import com.wrapper.spotify.methods.authentication.ClientCredentialsGrantRequest;
import com.wrapper.spotify.models.AuthorizationCodeCredentials;
import com.wrapper.spotify.models.LibraryTrack;
import com.wrapper.spotify.models.Page;
import com.wrapper.spotify.models.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * {@see https://github.com/thelinmichael/spotify-web-api-java}
 */
public class SpotifyController {
    private static volatile SpotifyController instance;
    private static final Object lock = new Object();

    private static final String CLIENT_ID = "24c87b0353a141768e9b842eb7bd0f67";
    private static final String CLIENT_SECRET = "cc5d6ebca4b445c782b6aced791710ab";
    private static final String REDIRECT_URI = "http://localhost:9000/callback";
    //  static final String REDIRECT_URI = "https://mir-analytics.herokuapp.com/callback";

    private static final Api api = Api.builder()
            .clientId(CLIENT_ID)
            .clientSecret(CLIENT_SECRET)
            .redirectURI(REDIRECT_URI)
            .build();

    /* Set the necessary scopes that the application will need from the user */ // TODO
    private final List<String> scopes = Arrays.asList("user-read-private", "user-read-email",
            "user-library-read", "user-library-modify", "playlist-modify-public");

    /* Set a state. This is used to prevent cross site request forgeries. */
    private final String state = "someExpectedStateString"; // TODO

    /**
     * TODO SHOULD BE INJECTED AT CONSTRUCTION TIME
     */
    // private String authorizeURL = api.createAuthorizeURL(scopes, state);
    private String authorizeURL = createAuthorizeURL(scopes, state, true);

    /* Create a request object. */
    final ClientCredentialsGrantRequest request = api.clientCredentialsGrant().build();

    private SpotifyController() {}

    /**
     * {@see} http://stackoverflow.com/a/11165926
     *
     * @return an instance of SpotifyController
     */
    public static SpotifyController getInstance() {
        SpotifyController i = instance;
        if (instance == null) {
            synchronized (lock) {   // While waiting for the lock, another
                i = instance;       // thread may have instantiated the object.
                if (i == null) {
                    i = new SpotifyController();
                    instance = i;
                }
            }
        }
        return instance;
    }

    private String createAuthorizeURL(List<String> scopes, String state, boolean val) {
        return api.createAuthorizeURL(scopes)
                .state(state)
                .showDialog(val)
                .build()
                .toStringWithQueryParameters();
    }

    /**
     * send the user to the authorizeURL;
     * e.g. https://accounts.spotify.com:443/authorize?client_id=5fe01282e44241328a84e7c5cc169165
     * &response_type=code&redirect_uri=https://example.com/callback
     * &scope=user-read-private%20user-read-email&state=some-state-of-my-choice
     *
     * @return the Spotify url to prompt the user for authorization
     */
    public String getAuthorizeURL() {
        return authorizeURL;
    }

    /**
     * Use the request object to make the request, either asynchronously (getAsync)
     * or synchronously (get)
     */
    public void getAccessToken(String code) {
        // Make a token request. Asynchronous requests are made with the .getAsync method and synchronous requests
        // are made with the .get method. This holds for all type of requests. */
        final SettableFuture<AuthorizationCodeCredentials> authorizationCodeCredentialsFuture = api.authorizationCodeGrant(code).build().getAsync();

        // Add callbacks to handle success and failure
        Futures.addCallback(authorizationCodeCredentialsFuture, new FutureCallback<AuthorizationCodeCredentials>() {

            @Override
            public void onSuccess(AuthorizationCodeCredentials authorizationCodeCredentials) {
                // The tokens were retrieved successfully! */
                System.out.println("Successfully retrieved an access token! " + authorizationCodeCredentials.getAccessToken());
                System.out.println("The access token expires in " + authorizationCodeCredentials.getExpiresIn() + " seconds");
                System.out.println("Luckily, I can refresh it using this refresh token! " + authorizationCodeCredentials.getRefreshToken());

                // Set the access token and refresh token so that they are used whenever needed */
                api.setAccessToken(authorizationCodeCredentials.getAccessToken());
                api.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println(throwable.getMessage());
                // Let's say that the client id is invalid, or the code has been used more than once,
                // the request will fail. Why it fails is written in the throwable's message. */

            }
        });
    }

    /**
     * Get display_name if present, otherwise the last bit of user_URI (i.e. the username)
     *
     * @return the display_name or username of the user
     */
    public String getName() {
        final CurrentUserRequest request = api.getMe().build();
        try {
            final User user = request.get();
            String name = user.getDisplayName();
            if(name == null) {
                // user_URI is of the form `spotify:user:username`
                name = user.getUri().substring(13);
            }
            return name;
        } catch (Exception e) {
            System.out.println("Something went wrong!" + e.getMessage());
            return null;
        }
    }
            /*
            System.out.println("Display name: " + user.getDisplayName());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Images:");
            for (Image image : user.getImages()) {
                System.out.println(image.getUrl());
            }

            System.out.println("This account is a " + user.getProduct() + " account");
            /*

             */

    /**
     * {@see} https://developer.spotify.com/web-api/get-users-saved-tracks/
     *
     * @return a List of `LibraryTrack`s
     */
    public List<LibraryTrack> getSavedTracks() throws IOException, WebApiException {
        try { // getAsync() returns a Settable<Future>, check that with Java 8
            return sendTracks(20);
        } catch (Exception e) {
            // might be a 401 invalid credential: redirect to login?
            System.out.println("Something went wrong! DARN: " + e.getMessage());
            throw e;
        }
    }

    private List<LibraryTrack> sendTracks(int pageSize) throws WebApiException, IOException {
        String next;
        int offset = 0;
        // do {
        //next =
        return sendTracks(offset, pageSize);
        //offset += pageSize;
        //  } while(next != null); FIX THIS LATER, TRY WITH ONE BATCH OF TRACKS;
    }

    private List<LibraryTrack> sendTracks(int offset, int pageSize) throws IOException, WebApiException {
        try {
            Page<LibraryTrack> libraryTracksPage = api.getMySavedTracks().offset(offset).limit(pageSize).build().get();
            return libraryTracksPage.getItems();
            //  String next = libraryTracksPage.getNext();
            //  System.out.println("NEXT: " + next);
            //  return next;
        } catch(BadRequestException br) {
            System.out.println(br.getMessage());
            throw br;
        }
    }


}

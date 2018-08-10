package com.yoavfranco.wikigame.HTTP;

import android.os.AsyncTask;

import com.yoavfranco.wikigame.HTTP.WikiGameInterface.WikiError;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * Created by yoav on 13/11/16.
 */

public class WikiGameAPI {

    private static final int TYPE_REQUEST_WIKIPAGE = 1;
    private static final int TYPE_REQUEST_LEVEL_SHUFFLE = 2;
    private static final int TYPE_REQUEST_NEWGAME = 3;
    private static final int TYPE_REQUEST_WELCOME = 4;
    private static final int TYPE_REQUEST_GOBACK = 5;
    private static final int TYPE_REQUEST_REGISTER_GUEST = 6;
    private static final int TYPE_REQUEST_REGISTER_USER = 7;
    private static final int TYPE_REQUEST_LEADERBOARDS = 8;
    private static final int TYPE_REQUEST_SEND_FRIEND_REQUEST = 9;
    private static final int TYPE_REQUEST_ACCEPT_FRIEND_REQUEST = 10;
    private static final int TYPE_REQUEST_IGNORE_FRIEND_REQUEST = 11;
    private static final int TYPE_REQUEST_TRY_CHALLENGE = 12;
    private static final int TYPE_REQUEST_CHALLENGE_SHUFFLE = 13;
    private static final int TYPE_REQUEST_SOCIAL = 14;
    private static final int TYPE_REQUEST_SHUFFLE_PRACTICE = 15;
    private static final int TYPE_REQUEST_CHALLENGE_GIVE_UP = 16;
    private static final int TYPE_REQUEST_UPDATE_FCM_TOKEN = 17;
    private static final int TYPE_REQUEST_ENABLE_NOTIFICATIONS = 18;
    private static final int TYPE_REQUEST_DISABLE_NOTIFICATIONS = 19;
    private static final int TYPE_REQUEST_FIX_LINK = 20;
    private static final int TYPE_REQUEST_LOGOUT = 21;
    private static final int TYPE_REQUEST_UPDATE_VERSION = 22;
    private static final int TYPE_REQUEST_CHOOSE = 23;
    private static final int TYPE_REQUEST_CHOOSE_CHALLENGE = 24;
    private static final int TYPE_REQUEST_ARTICLES = 25;

    private static CookieJar cookieJar = new WikiCookieJar();
    private final HttpUrl apiBasicURL;
    private OkHttpClient httpClient;

    public WikiGameAPI() {
        this.httpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).cookieJar(cookieJar).build();
        this.apiBasicURL = new HttpUrl.Builder().scheme("http").host(Consts.SERVER_HOST).addPathSegment("api").port(Consts.SERVER_PORT).build();
       //Utils.logDebug("HTTP base url ", apiBasicURL.toString());
    }

    public void welcomeRequestAsync(String username, String password, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_WELCOME, callbackInterface).execute(username, password);
    }

    public String welcomeRequest(String username, String password) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("welcome")
                .addQueryParameter("username", username)
                .addQueryParameter("password", password)
                .build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void updateFcmTokenAsync(String fcmToken, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_UPDATE_FCM_TOKEN, callbackInterface).execute(fcmToken);
    }

    public String updateFcmToken(String fcmToken) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("notifications").addQueryParameter("fcm_token", fcmToken).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void enableNotificationsAsync(WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_ENABLE_NOTIFICATIONS, callbackInterface).execute();
    }

    public String enableNotifications() {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("notifications").addQueryParameter("state", "enabled").build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void disableNotificationsAsync(WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_DISABLE_NOTIFICATIONS, callbackInterface).execute();
    }

    public String disableNotifications() {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("notifications").addQueryParameter("state", "disabled").build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void startNewGameAsync(WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_NEWGAME, callbackInterface).execute();
    }

    public String startNewGame() {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("wiki").build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void socialAsync(WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_SOCIAL, callbackInterface).execute();
    }

    public String social() {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("social").build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void followLinkAsync(String articleName, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_WIKIPAGE, callbackInterface).execute(articleName);
    }

    public String followLink(String articleName) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("wiki").addQueryParameter("link", articleName).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void fixLinkAsync(String fixedLink, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_FIX_LINK, callbackInterface).execute(fixedLink);
    }

    public String fixLink(String fixedLink) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("wiki").addQueryParameter("link", fixedLink).addQueryParameter("fix", "true").build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void leaderboardsAsync(WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_LEADERBOARDS, callbackInterface).execute();
    }

    public String leaderboards() {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("leaderboards").build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void sendFriendRequestAsync(String username, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_SEND_FRIEND_REQUEST, callbackInterface).execute(username);
    }

    public String sendFriendRequest(String username) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("friend").addQueryParameter("action", "send").addQueryParameter("username", username).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void acceptFriendRequestAsync(String username, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_ACCEPT_FRIEND_REQUEST, callbackInterface).execute(username);
    }

    public String acceptFriendRequest(String username) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("friend").addQueryParameter("action", "accept").addQueryParameter("username", username).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void ignoreFriendRequestAsync(String username, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_IGNORE_FRIEND_REQUEST, callbackInterface).execute(username);
    }

    public String ignoreFriendRequest(String username) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("friend").addQueryParameter("action", "ignore").addQueryParameter("username", username).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void giveUpChallengeAsync(WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_CHALLENGE_GIVE_UP, callbackInterface).execute();
    }

    public String getArticles() {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("articles").build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void getArticlesAsync(WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_ARTICLES, callbackInterface).execute();
    }

    public String giveUpChallenge() {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("challenge").addQueryParameter("action", "give_up").build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void tryChallengeAsync(String username, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_TRY_CHALLENGE, callbackInterface).execute(username);
    }

    public String tryChallenge(String username) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("challenge").addQueryParameter("action", "try_challenge").addQueryParameter("username", username).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void registerGuestAsync(String osVersion, String country, String appVersion, String phoneModel, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_REGISTER_GUEST, callbackInterface).execute(osVersion, country, appVersion, phoneModel);
    }

    public String registerGuest(String osVersion, String country, String appVersion, String phoneModel) {

        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("register").addQueryParameter("os_version", osVersion).addQueryParameter("app_version", appVersion).addQueryParameter("country", country).addQueryParameter("phone_model", phoneModel).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void registerUserAsync(String username, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_REGISTER_USER, callbackInterface).execute(username);
    }

    public String registerUser(String username) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("register").addQueryParameter("username", username).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void shuffleLevelAsync(String level, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_LEVEL_SHUFFLE, callbackInterface).execute(level);
    }

    public String shuffleLevel(String level) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("shuffle").addQueryParameter("level", level).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void shufflePracticeAsync(String mode, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_SHUFFLE_PRACTICE, callbackInterface).execute(mode);
    }

    public String shufflePractice(String mode) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("shuffle").addQueryParameter("mode", mode).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void shuffleChallengeAsync(String challengedUserName, String mode, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_CHALLENGE_SHUFFLE, callbackInterface).execute(challengedUserName, mode);
    }

    public String shuffleChallenge(String challengedUserName, String mode) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("shuffle").addQueryParameter("challenged_username", challengedUserName).addQueryParameter("mode", mode).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void chooseChallengeAsync(String challengedUserName, String mode, String startArticle, String targetArticle, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_CHOOSE_CHALLENGE, callbackInterface).execute(challengedUserName, mode, startArticle, targetArticle);
    }

    public String chooseChallenge(String challengedUserName, String mode, String startArticle, String targetArticle) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("choose").addQueryParameter("challenged_username", challengedUserName).addQueryParameter("mode", mode).addQueryParameter("start_article", startArticle).addQueryParameter("target_article", targetArticle).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void chooseAsync(String mode, String startArticle, String targetArticle, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_CHOOSE, callbackInterface).execute(mode, startArticle, targetArticle);
    }

    public String choose(String mode, String startArticle, String targetArticle) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("choose").addQueryParameter("mode", mode).addQueryParameter("start_article", startArticle).addQueryParameter("target_article", targetArticle).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void goBackAsync(WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_GOBACK, callbackInterface).execute();
    }

    public String goBack() {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("wiki").addQueryParameter("back", "true").build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void updateVersionAsync(String newVersion, WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_UPDATE_VERSION, callbackInterface).execute(newVersion);
    }

    public String updateVersion(String newVersion) {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("update_version").addQueryParameter("version", newVersion).build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public void logoutAsync(WikiGameInterface callbackInterface) {
        new ServerAPIRequest(TYPE_REQUEST_LOGOUT, callbackInterface).execute();
    }

    public String logout() {
        HttpUrl httpUrl = this.apiBasicURL.newBuilder().addPathSegment("logout").build();
        okhttp3.Request request = new okhttp3.Request.Builder().url(httpUrl).build();
        try {
            Response response = this.httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    private class ServerAPIRequest extends AsyncTask<String, Void, String> {
        WikiGameInterface callbackInterface;
        int type;

        ServerAPIRequest(int type, WikiGameInterface callbackInterface) {
            this.type = type;
            this.callbackInterface = callbackInterface;
        }

        @Override
        protected String doInBackground(String... params) {
            switch (type) {
                case TYPE_REQUEST_WELCOME: {
                    String username = params[0];
                    String password = params[1];
                    return welcomeRequest(username, password);
                }
                case TYPE_REQUEST_NEWGAME: {
                    return startNewGame();
                }
                case TYPE_REQUEST_LEVEL_SHUFFLE: {
                    String level = params[0];
                    return shuffleLevel(level);
                }
                case TYPE_REQUEST_WIKIPAGE: {
                    String articleName = params[0];
                    return followLink(articleName);
                }
                case TYPE_REQUEST_GOBACK: {
                    return goBack();
                }
                case TYPE_REQUEST_REGISTER_GUEST: {
                    String os_version = params[0];
                    String country = params[1];
                    String appVersion = params[2];
                    String phoneModel = params[3];
                    return registerGuest(os_version, country, appVersion, phoneModel);
                }
                case TYPE_REQUEST_REGISTER_USER: {
                    String username1 = params[0];
                    return registerUser(username1);
                }
                case TYPE_REQUEST_LEADERBOARDS: {
                    return leaderboards();
                }
                case TYPE_REQUEST_SEND_FRIEND_REQUEST: {
                    String username = params[0];
                    return sendFriendRequest(username);
                }
                case TYPE_REQUEST_ACCEPT_FRIEND_REQUEST: {
                    String username = params[0];
                    return acceptFriendRequest(username);
                }
                case TYPE_REQUEST_IGNORE_FRIEND_REQUEST: {
                    String username = params[0];
                    return ignoreFriendRequest(username);
                }
                case TYPE_REQUEST_CHALLENGE_GIVE_UP: {
                    return giveUpChallenge();
                }
                case TYPE_REQUEST_TRY_CHALLENGE: {
                    String username = params[0];
                    return tryChallenge(username);
                }
                case TYPE_REQUEST_CHALLENGE_SHUFFLE: {
                    String username = params[0];
                    String mode = params[1];
                    return shuffleChallenge(username, mode);
                }
                case TYPE_REQUEST_SOCIAL: {
                    return social();
                }
                case TYPE_REQUEST_SHUFFLE_PRACTICE: {
                    String mode = params[0];
                    return shufflePractice(mode);
                }
                case TYPE_REQUEST_UPDATE_FCM_TOKEN: {
                    String fcmToken = params[0];
                    return updateFcmToken(fcmToken);
                }
                case TYPE_REQUEST_FIX_LINK: {
                    String fixedLink = params[0];
                    return fixLink(fixedLink);
                }
                case TYPE_REQUEST_ENABLE_NOTIFICATIONS: {
                    return enableNotifications();
                }
                case TYPE_REQUEST_DISABLE_NOTIFICATIONS: {
                    return disableNotifications();
                }
                case TYPE_REQUEST_UPDATE_VERSION: {
                    String newVersion = params[0];
                    return updateVersion(newVersion);
                }
                case TYPE_REQUEST_CHOOSE: {
                    String mode = params[0];
                    String startArticle = params[1];
                    String targetArticle = params[2];
                    return choose(mode, startArticle, targetArticle);
                }
                case TYPE_REQUEST_CHOOSE_CHALLENGE: {
                    String challengedUsername = params[0];
                    String mode = params[1];
                    String startArticle = params[2];
                    String targetArticle = params[3];
                    return chooseChallenge(challengedUsername, mode, startArticle, targetArticle);
                }
                case TYPE_REQUEST_ARTICLES: {
                    return getArticles();
                }
                case TYPE_REQUEST_LOGOUT: {
                    return logout();
                }
            }
            return "";

        }

        @Override
        protected void onPostExecute(String result) {
            if (callbackInterface == null) return;

            if (result == null) {
                // IOException while making the request
                callbackInterface.onFailedMakingWikiRequest(WikiError.IOException);
                return;
            }

            try {
                callbackInterface.onFinishedProcessingWikiRequest(new JSONObject(result));
            } catch (JSONException e) {
                // invalid JSON response
                callbackInterface.onFailedMakingWikiRequest(WikiError.InvalidJSON);
            }
        }
    }

}
package com.yoavfranco.wikigame.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.gms.ads.MobileAds;
import com.yoavfranco.wikigame.HTTP.WikiGameAPI;
import com.yoavfranco.wikigame.HTTP.WikiGameInterface;
import com.yoavfranco.wikigame.R;
import com.yoavfranco.wikigame.utils.Consts;
import com.yoavfranco.wikigame.utils.ErrorDialogs;
import com.yoavfranco.wikigame.utils.Level;
import com.yoavfranco.wikigame.utils.SocialInfo;
import com.yoavfranco.wikigame.utils.SuggestedFriend;
import com.yoavfranco.wikigame.utils.UserInfo;
import com.yoavfranco.wikigame.utils.Utils;
import com.yoavfranco.wikigame.utils.Version;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import android.support.v7.app.AppCompatDelegate;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class LoadingActivity extends GameActivity {

    WikiGameAPI wikiAPI;
    JSONObject welcomeResponse;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wikiAPI = new WikiGameAPI();

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-9470442411108307~7545765072");

        // should accelerate the loading of NewWikiDisplay. not sure if works.
        WebView v = new WebView(this);
        v. setWebViewClient(new WebViewClient());

        login();
    }

    private void login() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String prefsUsername = prefs.getString(Consts.KEY_USER_NAME, null);
        String prefsPassword = prefs.getString(Consts.KEY_USER_PASSWORD, null);
        String appVersionInPrefs = prefs.getString(Consts.KEY_APP_VERSION, null);
        String userCountry = Utils.getUserCountry(getApplicationContext());
        String appVersion = Utils.getAppVersion(getApplicationContext());
        String phoneModel = Utils.getDeviceName();

        if (appVersionInPrefs != null) {
            if (!appVersionInPrefs.equals(appVersion)) {
                onVersionUpdated(appVersion);
            }
        }

        if (prefsUsername == null || prefsPassword == null) {
            wikiAPI.registerGuestAsync(android.os.Build.VERSION.RELEASE, userCountry, appVersion, phoneModel, new WikiGameInterface(this) {
                @Override
                public void onFinishedProcessingWikiRequest(JSONObject response) {
                    try {
                        if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                            JSONObject userInfo = response.getJSONObject("user_info");
                            String username = userInfo.getString("username");
                            String password = userInfo.getString("password");
                            SharedPreferences.Editor prefEditor = prefs.edit();
                            prefEditor.putString(Consts.KEY_USER_NAME, username);
                            prefEditor.putString(Consts.KEY_USER_PASSWORD, password);
                            prefEditor.apply();
                            sendWelcomeRequest(username, password);
                        } else {
                            ErrorDialogs.showSomethingWentWrongDialog(getActivityContext(), true);
                            // TODO: Handle error in registration
                        }
                    } catch (JSONException e) {
                        ErrorDialogs.showBadResponseDialog(getActivityContext(), true);
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailedMakingWikiRequest(WikiError errorCause) {
                    ErrorDialogs.showNetworkErrorDialog(LoadingActivity.this, true);
                }
            });
        } else {
            sendWelcomeRequest(prefsUsername, prefsPassword);
        }
    }

    private void onVersionUpdated(final String newVersion) {
        // the app was updated. tell it to server.
        wikiAPI.updateVersionAsync(newVersion, new WikiGameInterface(this) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                try {
                    if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                        // good.
                        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoadingActivity.this);
                        SharedPreferences.Editor prefEditor = prefs.edit();
                        prefEditor.putString(Consts.KEY_APP_VERSION, newVersion);
                        prefEditor.apply();

                    } else {
                        // bad.
                    }
                } catch (JSONException e) {
                    // bad.
                }
            }

            @Override
            public void onFailedMakingWikiRequest(WikiError errorCause) {
                // that's not so bad, we can live with it
                Toast.makeText(getActivityContext(), "A network error has occurred",Toast.LENGTH_SHORT);
            }
        });
    }

    private void sendWelcomeRequest(final String username, String password) {

        wikiAPI.welcomeRequestAsync(username, password, new WikiGameInterface(this) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {

                try {
                    if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_OK)) {
                        welcomeResponse = response;
                        boolean isFine = handleVersions(response.getString("latest_app_version"), response.getString("minimum_required_app_version"), Utils.getAppVersion(getApplicationContext()));
                        if (isFine) {
                            // if the versions handing required showing a dialog, the buttons will call continueParsingWelcomeResponse().
                            continueParsingWelcomeResponse();
                        }

                    } else if (response.getString(Consts.STATUS_CODE_KEY).equals(Consts.STATUS_INVALID_CRENTIALS)) {
                            if (!Utils.isGuestUsername(username)) {
                                showFailedToLoginDialog();
                            } else {
                                Toast.makeText(LoadingActivity.this, "Failed to login, created new user", Toast.LENGTH_SHORT);
                                clearCredentials();
                                login();
                            }
                    }
                    else {
                        ErrorDialogs.showSomethingWentWrongDialog(getActivityContext(), true);
                    }
                } catch (JSONException e) {
                    ErrorDialogs.showBadResponseDialog(getActivityContext(), true);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailedMakingWikiRequest(WikiError errorCause) {
                ErrorDialogs.showNetworkErrorDialog(LoadingActivity.this, true);
            }
        });
    }

    private void continueParsingWelcomeResponse() {
        try {
            if (this.welcomeResponse == null) {
                // what?
                return;
            }
            JSONObject response = this.welcomeResponse;
            JSONObject userInfoObject = response.getJSONObject("user_info");
            UserInfo userInfo = UserInfo.fromJSON(userInfoObject);
            SocialInfo socialInfo = SocialInfo.fromJSON(userInfoObject);
            int maximumAllowedClicks = response.getInt("maximum_allowed_clicks");
            int maximumAllowedTime = response.getInt("maximum_allowed_time");

            JSONArray levelsObject = userInfoObject.getJSONArray("levels_info");
            JSONArray suggestedFriendsObject = response.getJSONArray("suggested_friends");
            ArrayList<Level> gameLevels = new ArrayList<>();
            for (int i = 0; i < levelsObject.length(); i++) {
                gameLevels.add(Level.fromJSON(levelsObject.getJSONObject(i)));
            }
            ArrayList<SuggestedFriend> suggestedFriends = new ArrayList<>();
            for (int i = 0; i < suggestedFriendsObject.length(); i++) {
                suggestedFriends.add(SuggestedFriend.fromJSON(suggestedFriendsObject.getJSONObject(i)));
            }
            for (int i = levelsObject.length(); i < response.getInt("num_levels"); i++) {
                Level level = new Level((i + 1) + "", true, "unknown", 0, 0);
                gameLevels.add(level);
            }

            Intent intent = new Intent(LoadingActivity.this, MainActivity.class);
            intent.putExtra(Consts.BUNDLE_LEVELS_KEY, gameLevels);
            intent.putExtra("maximum_allowed_clicks", maximumAllowedClicks);
            intent.putExtra("maximum_allowed_time", maximumAllowedTime);
            intent.putExtra("suggested_friends", suggestedFriends);
            intent.putExtra("user_info", userInfo);
            intent.putExtra("social_info", socialInfo);
            intent.putExtra("user_total_points", userInfo.getTotalPoints());
            startActivity(intent);
            finish();
        } catch (JSONException e) {
            ErrorDialogs.showBadResponseDialog(this, true);
            e.printStackTrace();
        }
    }

    private boolean handleVersions(final String latestVersionStr, String minimumRequiredVersionStr, String currentVersionStr) {
        Version latestVersion = new Version(latestVersionStr);
        Version minimumRequiredVersion = new Version(minimumRequiredVersionStr);
        Version currentVersion = new Version(currentVersionStr);

        if (currentVersion.compareTo(minimumRequiredVersion) == -1) {
            // well, we're sorry but you must update the app
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
            new MaterialStyledDialog.Builder(this)
                    // TODO: add strings to xml/strings file
                    .setTitle("Out-dated version")
                    .setDescription("You must update to a newer version of the app to keep using it.")
                    .setStyle(Style.HEADER_WITH_TITLE)
                    .setCancelable(false)
                    .withIconAnimation(false)
                    .withDialogAnimation(true)
                    .withDivider(true)
                    .setPositiveText("UPDATE")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.yoavfranco.wikigame")));
                            LoadingActivity.this.finish();
                        }
                    }).show();
            return false;
        } else if (currentVersion.compareTo(latestVersion) == -1) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String dontShowAgainVersion = prefs.getString(Consts.KEY_DONT_SHOW_AGAIN_VERSION, null);
            if (dontShowAgainVersion != null) {
                if (latestVersionStr.equals(dontShowAgainVersion)) {
                    // bye bye
                    return true;
                }
            }
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
            new MaterialStyledDialog.Builder(this)
                    // TODO: add strings to xml/strings file
                    .setTitle("Newer version available!")
                    .setDescription("You are encouraged to download the new version of this app (" + latestVersionStr + "). Would you like to?")
                    .setStyle(Style.HEADER_WITH_TITLE)
                    .setCancelable(false)
                    .withIconAnimation(false)
                    .withDialogAnimation(true)
                    .withDivider(true)
                    .setPositiveText("UPDATE")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.yoavfranco.wikigame")));
                            LoadingActivity.this.finish();
                        }
                    }).setNegativeText("NOT NOW")
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            SharedPreferences.Editor prefEditor = prefs.edit();
                            prefEditor.putString(Consts.KEY_DONT_SHOW_AGAIN_VERSION, latestVersionStr);
                            prefEditor.apply();
                            dialog.dismiss();
                            continueParsingWelcomeResponse();
                        }
                    }).show();
            return false;
        }

        return true;
    }

    public void showFailedToLoginDialog () {
        // TODO: add strings to xml/strings file
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        new MaterialStyledDialog.Builder(this)
        .setTitle("Cannot Login")
                .setDescription("It seems like this user does not exist anymore. Would you like to remove it?")
                .setIcon(R.drawable.ic_no_internet_connection)
                .setCancelable(false)
                .withIconAnimation(false)
                .withDialogAnimation(true)
                .withDivider(true)
                .setPositiveText("REMOVE USER")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    clearCredentials();
                    login();
                    }
                }).setNegativeText("CANCEL")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        LoadingActivity.this.finish();
                    }
                }).show();
    }

    private void clearCredentials() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoadingActivity.this);
        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString(Consts.KEY_USER_NAME, null);
        prefEditor.putString(Consts.KEY_USER_PASSWORD, null);
        prefEditor.apply();
    }
}

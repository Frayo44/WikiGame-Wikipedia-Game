package com.yoavfranco.wikigame.services;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by yoav on 01/04/17.
 */
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.yoavfranco.wikigame.HTTP.WikiGameAPI;
import com.yoavfranco.wikigame.HTTP.WikiGameInterface;

import org.json.JSONObject;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyAndroidFCMIIDService";

    @Override
    public void onTokenRefresh() {
        //Get hold of the registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendTokenToServer(refreshedToken);
        //Log the token
        Log.d(TAG, "Refreshed token: " + refreshedToken);
    }

    private void sendTokenToServer(String token) {

        WikiGameAPI wikiGameAPI = new WikiGameAPI();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String username = prefs.getString("username", null);
        wikiGameAPI.updateFcmTokenAsync(token, new WikiGameInterface(this) {
            @Override
            public void onFinishedProcessingWikiRequest(JSONObject response) {
                // If the request failed, we're in a problem.
                // TODO: try re-sending the token every launch until the server returns OK.
            }
        });

        SharedPreferences.Editor prefEditor = prefs.edit();
        prefEditor.putString("user_token", token);
        prefEditor.apply();
    }
}
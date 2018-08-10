package com.yoavfranco.wikigame.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Tomer on 31/03/2017.
 */

public class UserInfo implements Serializable {

    private String username;
    private int totalPoints;
    private String countryCode;
    private String flagURL;
    private boolean notificationsEnabled;

    public UserInfo(String username, int totalPoints, String countryCode, String flagURL, boolean notificationsEnabled) {
        this.username = username;
        this.totalPoints = totalPoints;
        this.countryCode = countryCode;
        this.flagURL = flagURL;
        this.notificationsEnabled = notificationsEnabled;
    }

    public static UserInfo fromJSON(JSONObject json) throws JSONException {
        String username = json.getString("username");
        int points = json.getInt("points");
        String countryCode = json.has("country_code") ? json.getString("country_code") : "-";
        String countryFlag = json.has("country_flag") ? json.getString("country_flag") : "/flags/default_flag.png";
        boolean notificationsEnabled = json.has("notifications_enabled") ? json.getBoolean("notifications_enabled") : true;

        return new UserInfo(username, points, countryCode, Utils.toServerURL(countryFlag), notificationsEnabled);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getFlagURL() {
        return flagURL;
    }

    public void setFlagURL(String flagURL) {
        this.flagURL = flagURL;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}

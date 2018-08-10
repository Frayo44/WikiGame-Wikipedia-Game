package com.yoavfranco.wikigame.utils;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by yoav on 07/03/17.
 */

public class Friend implements Serializable {

    private String username;
    private int victories;
    private int looses;
    private int points;
    private Challenge challenge;
    private boolean isHisTurn;
    private String countryCode;
    private String flagURL;

    public Friend(String username, int victories, int looses, int points, boolean isHisTurn, String countryCode, String flagURL) {
        this.username = username;
        this.victories = victories;
        this.looses = looses;
        this.points = points;
        this.isHisTurn = isHisTurn;
        this.countryCode = countryCode;
        this.flagURL = flagURL;
    }

    // "not-yet-a-friend" constructor
    public Friend(String username, String countryCode, String flagURL) {
        this(username, 0, 0, 0, false, countryCode, flagURL);
    }

    @Nullable
    public static Friend fromJSON(JSONObject json) throws JSONException {
        String countryCode = json.has("country_code") ? json.getString("country_code") : "-";
        String flagURL = json.has("country_flag") ? json.getString("country_flag") : "/flags/default_flag.png";
        int points = json.has("points") ? json.getInt("points") : 0;

        return new Friend(json.getString("username"), json.getInt("victories_count"), json.getInt("looses_count"), points, json.getBoolean("is_his_turn"), countryCode, Utils.toServerURL(flagURL));
    }

    public boolean isChallengedByUsername(String username)
    {
        if (this.challenge == null) return false;
        return this.challenge.getChallengingUsername().equals(username);
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Friend)) return false;
        Friend o = (Friend) obj;
        return o.getUsername().equals(this.getUsername());
    }

    public boolean isHisTurn()
    {
        return this.isHisTurn;
    }

    public int getLooses() {
        return looses;
    }

    public void setLooses(int looses) {
        this.looses = looses;
    }

    public int getVictories() {
        return victories;
    }

    public void setVictories(int victories) {
        this.victories = victories;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
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

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

}

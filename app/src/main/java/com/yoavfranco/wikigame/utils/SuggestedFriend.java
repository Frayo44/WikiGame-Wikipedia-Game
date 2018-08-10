package com.yoavfranco.wikigame.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Tomer on 18/03/2017.
 */

public class SuggestedFriend implements Serializable {

    private String username;
    private int points;
    private Date lastUseTime;
    private String countryCode;
    private String flagURL;

    public SuggestedFriend(String username, int points, Date lastUseTime, String countryCode, String flagURL) {
        this.username = username;
        this.points = points;
        this.lastUseTime = lastUseTime;
        this.countryCode = countryCode;
        this.flagURL = flagURL;
    }

    public static SuggestedFriend fromJSON(JSONObject json) throws JSONException
    {
        String countryCode = json.has("country_code") ? json.getString("country_code") : "-";
        String flagURL = json.has("country_flag") ? json.getString("country_flag") : "/flags/default_flag.png";
        return new SuggestedFriend(json.getString("username"), json.getInt("points"), Utils.parseDate(json.getString("last_use_time")), countryCode, Utils.toServerURL(flagURL));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public Date getLastUseTime() {
        return lastUseTime;
    }

    public void setLastUseTime(Date lastUseTime) {
        this.lastUseTime = lastUseTime;
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


}

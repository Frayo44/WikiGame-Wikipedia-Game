package com.yoavfranco.wikigame.utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yoav on 19/03/17.
 */

public class FriendRequest implements Serializable {

    private String senderUsername;
    private String receiverUsername;
    private Date sentTime;
    private String countryCode;
    private String flagURL;

    public FriendRequest(String senderUsername, String receiverUsername, Date sentTime, String countryCode, String flagURL) {
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.sentTime = sentTime;
        this.countryCode = countryCode;
        this.flagURL = flagURL;
    }

    public static FriendRequest fromJSON(JSONObject json) throws JSONException
    {
        String senderCountryCode = json.has("country_code") ? json.getString("country_code") : "-";
        String flagURL = json.has("country_flag") ? json.getString("country_flag") : "/flags/default_flag.png";
        return new FriendRequest(json.getString("sender_username"), json.getString("receiver_username"), Utils.parseDate(json.getString("sent_time")), senderCountryCode, Utils.toServerURL(flagURL));
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public void setReceiverUsername(String receiverUsername) {
        this.receiverUsername = receiverUsername;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
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

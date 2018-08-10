package com.yoavfranco.wikigame.utils;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yoav on 07/03/17.
 */

public class Challenge implements Serializable {

    private String challengedUsername;
    private String challengingUsername;
    private String startArticle;
    private String targetArticle;
    private String mode;
    private int numClicks;
    private int time;
    private String message;
    private Date sentTime;

    public Challenge(String challengedUsername, String challengingUsername, String mode, String startArticle, String targetArticle, int numClicks, int time, String message, Date sentTime) {
        this.time = time;
        this.numClicks = numClicks;
        this.mode = mode;
        this.targetArticle = targetArticle;
        this.startArticle = startArticle;
        this.challengingUsername = challengingUsername;
        this.challengedUsername = challengedUsername;
        this.message = message;
        this.sentTime = sentTime;
    }

    @Nullable
    public static Challenge fromJSON(JSONObject json) throws JSONException {
            String mode = json.has("mode") ? json.getString("mode") : Consts.CLICKS_MODE;
            return new Challenge(json.getString("challenged_username"),json.getString("challenging_username"),mode,json.getString("start_article"),json.getString("target_article"),json.getInt("num_clicks"),json.getInt("time"),json.getString("custom_message"), Utils.parseDate(json.getString("sent_time")));
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getNumClicks() {
        return numClicks;
    }

    public void setNumClicks(int numClicks) {
        this.numClicks = numClicks;
    }

    public String getTargetArticle() {
        return targetArticle;
    }

    public void setTargetArticle(String targetArticle) {
        this.targetArticle = targetArticle;
    }

    public String getStartArticle() {
        return startArticle;
    }

    public void setStartArticle(String startArticle) {
        this.startArticle = startArticle;
    }

    public String getChallengingUsername() {
        return challengingUsername;
    }

    public void setChallengingUsername(String challengingUsername) {
        this.challengingUsername = challengingUsername;
    }

    public String getChallengedUsername() {
        return challengedUsername;
    }

    public void setChallengedUsername(String challengedUsername) {
        this.challengedUsername = challengedUsername;
    }

    public Date getSentTime() {
        return sentTime;
    }

    public void setSentTime(Date sentTime) {
        this.sentTime = sentTime;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isClicksMode() {return  this.mode.equals(Consts.CLICKS_MODE); }

}

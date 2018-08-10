package com.yoavfranco.wikigame.utils;

import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by yoav on 25/11/16.
 */

public class Level implements Serializable {

    long levelID;
    private String levelName;
    private boolean isLocked;
    private String mode;
    private int best;
    private int maximumAllowedRecord;

    public Level(String levelName, boolean isLocked, String mode, int best, int maximumAllowedRecord) {
        this.levelName = levelName;
        this.mode = mode;
        this.isLocked = isLocked;
        this.best = best;
        this.maximumAllowedRecord = maximumAllowedRecord;
    }

    // locked level constructor
    public Level(String levelName) {
        this.levelName = levelName;
        this.mode = Consts.CLICKS_MODE;
        this.isLocked = true;
        this.best = 0;
        this.maximumAllowedRecord = 0;
    }

    @Nullable
    public static Level fromJSON(JSONObject json) throws JSONException {
        int best = json.has("best") ? json.getInt("best") : 0;
        String mode = json.has("mode") ? json.getString("mode") : Consts.CLICKS_MODE;
        int maximumAllowedRecord = mode.equals(Consts.CLICKS_MODE) ? json.getInt("maximum_allowed_clicks") : json.getInt("maximum_allowed_time");
        return new Level(json.getString("level_name"), false, mode, best, maximumAllowedRecord);
    }

    public long getLevelID() {
        return levelID;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getBest() {
        return best;
    }

    public void setBest(int best) {
        this.best = best;
    }

    public int getMaximumAllowedRecord() {
        return maximumAllowedRecord;
    }

    public void setMaximumAllowedRecord(int maximumAllowedRecord) {
        this.maximumAllowedRecord = maximumAllowedRecord;
    }

    public boolean isClicksMode() {
        return this.mode.equals(Consts.CLICKS_MODE);
    }
}

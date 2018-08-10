package com.yoavfranco.wikigame.utils;

import java.io.Serializable;

/**
 * Created by yoav on 25/11/16.
 */

public class LeaderboardItem implements Serializable {

    long levelID;
    private String rank;
    private String displayName;
    private String pointsText;
    private String countryFlag;
    private String countryCode;

    public LeaderboardItem(String rank, String displayName, String points, String countryFlag, String countryCode) {
        this.rank = rank;
        this.displayName = displayName;
        this.pointsText = points;
        this.levelID = 1;
        this.countryCode = countryCode;
        this.countryFlag = countryFlag;
    }

    public String getCountryFlag() {
        return countryFlag;
    }
    public void setCountryFlag(String countryFlag) {
        this.countryFlag = countryFlag;
    }

    public String getCountryCode() {
        return countryCode;
    }
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public long getLevelID() {
        return levelID;
    }
    public String getPointsText() {
        return pointsText;
    }

    public void setPointsText(String pointsText) {
        this.pointsText = pointsText;
    }

    public void setLevelID(long levelID) {
        this.levelID = levelID;
    }

    public String getRank() {
        return rank;
    }

    public void setRank(String rank) {
        this.rank = rank;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

}

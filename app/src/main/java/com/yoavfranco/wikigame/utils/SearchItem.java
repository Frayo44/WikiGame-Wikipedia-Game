package com.yoavfranco.wikigame.utils;

import java.io.Serializable;

/**
 * Created by yoav on 02/04/17.
 */

public class SearchItem implements Serializable {

    String title;
    String decription;

    public SearchItem(String title, String decription) {
        this.title = title;
        this.decription = decription;
    }

    public String getSubject() {
        return decription;
    }

    public void setDecription(String decription) {
        this.decription = decription;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }



}

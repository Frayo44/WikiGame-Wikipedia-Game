package com.yoavfranco.wikigame.utils;

import android.graphics.drawable.Drawable;

/**
 * Created by tomer aka rosenpin on 2/9/16.
 */
public class Item {
    String title, description;
    Drawable img;

    public Item(String title, String description, Drawable img) {
        this.title = title;
        this.description = description;
        this.img = img;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Drawable getImg() {
        return img;
    }
}
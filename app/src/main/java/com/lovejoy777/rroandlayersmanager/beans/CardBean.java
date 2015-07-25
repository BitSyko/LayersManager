package com.lovejoy777.rroandlayersmanager.beans;

import android.graphics.drawable.Drawable;

public class CardBean {
    private String title;
    private String description;
    private Drawable image;

    public CardBean(String title, String description, Drawable image) {
        this.title = title;
        this.description = description;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Drawable getImage() {
        return image;
    }
}
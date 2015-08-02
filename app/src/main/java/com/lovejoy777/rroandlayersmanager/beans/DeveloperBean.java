package com.lovejoy777.rroandlayersmanager.beans;

import android.graphics.drawable.Drawable;


public class DeveloperBean extends CardBean {

    private String webpage;

    public DeveloperBean(String title, String description, Drawable image, String webpage) {
        super(title, description, image);
        this.webpage = webpage;
    }

    public String getWebpage() {
        return webpage;
    }
}

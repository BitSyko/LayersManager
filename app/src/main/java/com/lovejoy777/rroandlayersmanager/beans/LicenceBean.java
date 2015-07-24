package com.lovejoy777.rroandlayersmanager.beans;

import android.graphics.drawable.Drawable;

public class LicenceBean extends DeveloperBean {

    private final String longDescription;

    public LicenceBean(String title, String shortDescription, Drawable image, String webpage, String longDescription) {
        super(title, shortDescription, image, webpage);
        this.longDescription = longDescription;
    }


    public String getLongDescription() {
        return longDescription;
    }
}

package com.lovejoy777.rroandlayersmanager.beans;

import android.app.Activity;

public class DeveloperFactory {

    private Activity activity;

    public DeveloperFactory(Activity activity) {
        this.activity = activity;
    }

    public DeveloperBean createDeveloper(String title, String description, int image, String webpage) {
        return new DeveloperBean(title, description, activity.getDrawable(image), webpage);
    }

    public DeveloperBean createDeveloper(String title, String description, int image, int webpage) {
        return new DeveloperBean(title, description, activity.getDrawable(image), activity.getString(webpage));
    }


}

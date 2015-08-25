package com.bitsyko;

import android.graphics.drawable.Drawable;

public class Placeholder implements ApplicationInfo {

    String name;
    String developer;
    Drawable icon;

    public Placeholder(String name, String developer, Drawable icon) {
        this.name = name;
        this.developer = developer;
        this.icon = icon;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDeveloper() {
        return developer;
    }

    @Override
    public Drawable getIcon() {
        return icon;
    }

    @Override
    public String getPackageName() {
        return null;
    }
}

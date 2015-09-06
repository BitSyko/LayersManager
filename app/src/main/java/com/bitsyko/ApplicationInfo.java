package com.bitsyko;

import android.graphics.drawable.Drawable;

import java.util.Comparator;

public interface ApplicationInfo {

    String getName();

    String getDeveloper();

    Drawable getIcon();

    String getPackageName();

    Comparator<ApplicationInfo> compareName = new Comparator<ApplicationInfo>() {
        public int compare(ApplicationInfo layer1, ApplicationInfo layer2) {
            return layer1.getName().compareToIgnoreCase(layer2.getName());
        }
    };


    Comparator<ApplicationInfo> compareDev = new Comparator<ApplicationInfo>() {
        public int compare(ApplicationInfo layer1, ApplicationInfo layer2) {
            return layer1.getDeveloper().compareToIgnoreCase(layer2.getDeveloper());
        }
    };


}

package com.bitsyko.libicons;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SystemApplicationHelper {

    private Context context;

    private Map<String, Collection<String>> installedAppsAndActivities;
    private Collection<String> installedActivities;

    //No instances
    private SystemApplicationHelper(Context context) {
        this.context = context;
    }

    private static SystemApplicationHelper instance;

    public static SystemApplicationHelper getInstance(Context context) {

        if (instance == null) {

            if (context == null) {
                throw new RuntimeException("Can't create instance without context");
            }

            instance = new SystemApplicationHelper(context);

        }

        return instance;
    }


    public Map<String, Collection<String>> getInstalledAppsAndTheirLauncherActivities() {

        if (installedAppsAndActivities == null) {

            installedAppsAndActivities = new HashMap<>();

            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            final List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(mainIntent, 0);

            for (ResolveInfo info : apps) {

                String appName = info.activityInfo.packageName;
                String activityName = info.activityInfo.name;


                if (!installedAppsAndActivities.containsKey(appName)) {
                    installedAppsAndActivities.put(appName, new ArrayList<String>());
                }

                installedAppsAndActivities.get(appName).add(activityName);

            }

        }

        return installedAppsAndActivities;

    }


    public Collection<String> getInstalledAppsWithLauncherActivities() {

        if (installedActivities == null) {

            installedActivities = new HashSet<>();

            final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            final List<ResolveInfo> apps = context.getPackageManager().queryIntentActivities(mainIntent, 0);

            for (ResolveInfo info : apps) {
                installedActivities.add(info.activityInfo.packageName);
            }

        }

        return installedActivities;

    }


}

package com.bitsyko.liblayers;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class Layer {
    private static final String ACTION_PICK_PLUGIN = "com.layers.plugins.PICK_OVERLAYS";

    private String name;
    private String packageName;
    private String developer;
    private Drawable icon;


    //Only library can create instances
    public Layer(String name, String developer, Drawable icon, String packageName) {
        this.name = name;
        this.developer = developer;
        this.icon = icon;
        this.packageName = packageName;
    }


    public static List<Layer> getLayersInSystem(Activity activity) {

        List<Layer> layerList = new ArrayList<>();

        PackageManager packageManager = activity.getPackageManager();
        Intent baseIntent = new Intent(ACTION_PICK_PLUGIN);
        baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
        ArrayList<ResolveInfo> list = (ArrayList<ResolveInfo>) packageManager.queryIntentServices(baseIntent,
                PackageManager.GET_RESOLVED_FILTER);


        for (ResolveInfo info : list) {
            ServiceInfo sinfo = info.serviceInfo;

            ApplicationInfo ai;

            try {
                ai = activity.getPackageManager().getApplicationInfo(sinfo.packageName, PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                throw new ErrorThatShouldNeverHappen();
            }

            Bundle bundle = ai.metaData;

            String name = bundle.getString("Layers_Name");
            String developer = bundle.getString("Layers_Developer");

            String packageName = ai.packageName;


            String mDrawableName = "icon";
            PackageManager manager = activity.getPackageManager();

            Resources mApk1Resources;

            try {
                mApk1Resources = manager.getResourcesForApplication(packageName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                throw new ErrorThatShouldNeverHappen();
            }

            int mDrawableResID = mApk1Resources.getIdentifier(mDrawableName, "drawable", packageName);

            Drawable myDrawable = mApk1Resources.getDrawable(mDrawableResID, null);

            layerList.add(new Layer(name, developer, myDrawable, packageName));

        }

        return layerList;

    }


    public String getName() {
        return name;
    }

    public String getDeveloper() {
        return developer;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getPackageName() {
        return packageName;
    }

}

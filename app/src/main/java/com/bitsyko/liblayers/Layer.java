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

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Layer implements Closeable {
    private static final String ACTION_PICK_PLUGIN = "com.layers.plugins.PICK_OVERLAYS";

    private String name;
    private String packageName;
    private String developer;
    private List<Drawable> screenShots;
    private Drawable promo;
    private PackageManager packageManager;
    private Resources resources;

    private Drawable icon;

    public Layer(String name, String developer, Drawable icon) {
        this(name, developer, icon, null, null, null);
    }

    public Layer(String name, String developer, Drawable icon, String packageName,
                 PackageManager packageManager, Resources resources) {
        this.name = name;
        this.developer = developer;
        this.icon = icon;
        this.packageName = packageName;
        this.packageManager = packageManager;
        this.resources = resources;
    }

    public static Layer layerFromPackageName(String packageName, Activity activity)
            throws PackageManager.NameNotFoundException {

        ApplicationInfo ai = activity.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);

        Bundle bundle = ai.metaData;

        String name = bundle.getString("Layers_Name");
        String developer = bundle.getString("Layers_Developer");

        String mDrawableName = "icon";
        PackageManager manager = activity.getPackageManager();

        Resources mApk1Resources = manager.getResourcesForApplication(packageName);

        int mDrawableResID = mApk1Resources.getIdentifier(mDrawableName, "drawable", packageName);

        Drawable myDrawable = mApk1Resources.getDrawable(mDrawableResID, null);

        return new Layer(name, developer, myDrawable, packageName, manager, mApk1Resources);
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

            try {
                layerList.add(layerFromPackageName(sinfo.packageName, activity));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                throw new ErrorThatShouldNeverHappen();
            }

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

    public List<Drawable> getScreenShots() {
        return getScreenShots(null);
    }

    public List<Drawable> getScreenShots(Callback<Drawable> callback) {

        if (screenShots == null) {

            screenShots = new ArrayList<>();

            int i = 1;

            while (true) {

                String drawableName = "screenshot" + i;

                int mDrawableResID = resources.getIdentifier(drawableName, "drawable", packageName);

                if (mDrawableResID == 0) {
                    break;
                }

                Drawable drawable = resources.getDrawable(mDrawableResID, null);

                screenShots.add(resources.getDrawable(mDrawableResID, null));

                i++;

                if (callback != null) {
                    callback.callback(drawable);
                }


            }

        }

        return screenShots;
    }

    public Drawable getPromo() {

        if (promo == null) {

            int promoID = resources.getIdentifier("heroimage", "drawable", packageName);

            promo = resources.getDrawable(promoID, null);

        }


        return promo;
    }

    @Override
    public void close() throws IOException {

    }
}

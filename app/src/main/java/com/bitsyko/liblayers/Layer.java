package com.bitsyko.liblayers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.lovejoy777.rroandlayersmanager.commands.RootCommands;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Layer implements Closeable {
    private static final String ACTION_PICK_PLUGIN = "com.layers.plugins.PICK_OVERLAYS";

    private String name;
    private String packageName;
    private String developer;
    private List<Drawable> screenShots;
    private Drawable promo;
    private PackageManager packageManager;
    private ApplicationInfo applicationInfo;
    private Resources resources;
    private Context context;

    private Map<String, String> additionalData;

    private Drawable icon;

    public Layer(String name, String developer, Drawable icon) {
        this(name, developer, icon, null, null, null, null, null);
    }

    public Layer(String name, String developer, Drawable icon, String packageName,
                 PackageManager packageManager, Resources resources, ApplicationInfo applicationInfo,
                 Context context) {
        this.name = name;
        this.developer = developer;
        this.icon = icon;
        this.packageName = packageName;
        this.packageManager = packageManager;
        this.resources = resources;
        this.applicationInfo = applicationInfo;
        this.context = context;
    }

    public static Layer layerFromPackageName(String packageName, Context context)
            throws PackageManager.NameNotFoundException {

        ApplicationInfo applicationInfo = context.getApplicationContext().getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);

        Bundle bundle = applicationInfo.metaData;

        String name = bundle.getString("Layers_Name");
        String developer = bundle.getString("Layers_Developer");

        String mDrawableName = "icon";
        PackageManager manager = context.getApplicationContext().getPackageManager();

        Resources resources = manager.getResourcesForApplication(packageName);

        int iconID = resources.getIdentifier(mDrawableName, "drawable", packageName);

        Drawable icon = resources.getDrawable(iconID, null);

        return new Layer(name, developer, icon, packageName, manager, resources, applicationInfo, context);
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
        return getScreenShots(new Callback<Drawable>() {
            @Override
            public void callback(Drawable object) {
            }
        });
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
                callback.callback(drawable);
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

    public List<LayerFile> getLayersInPackage() {

        Bundle bundle = applicationInfo.metaData;

        List<LayerFile> files = new ArrayList<>();

        if (bundle.containsKey("Layers_NormalOverlays")) {
            //v1.1

            Log.d("Normal overlays: ", bundle.getString("Layers_NormalOverlays"));

            String[] normalOverlayNames = bundle.getString("Layers_NormalOverlays").split(",");

            for (String layer : normalOverlayNames) {
                files.add(new LayerFile(this, layer, false));
            }

            String[] styleSpecificOverlayNames = bundle.getString("Layers_StyleSpecificOverlays", "").split(",");

            for (String layer : styleSpecificOverlayNames) {
                files.add(new LayerFile(this, layer, true));
            }


        } else {
            //v1.0

            Log.d("Overlays", bundle.getString("Layers_OverlayNames"));

            //Overlays
            String[] overlays = bundle.getString("Layers_OverlayNames").split(",");

            boolean normalOverlay = true;


            for (String overlay : overlays) {

                if (overlay.equals(" ") || overlay.equals("")) {
                    normalOverlay = false;
                    continue;
                }

                files.add(new LayerFile(this, overlay, !normalOverlay));

            }

        }


        return files;

    }

    public List<String> getColors() {

        Bundle bundle = applicationInfo.metaData;

        ArrayList<String> list = new ArrayList<>();

        list.addAll(Arrays.asList(bundle.getString("Layers_Colors", "").split(",")));
        list.addAll(Arrays.asList(bundle.getString("Styles", "").split(",")));

        list.remove("");

        return list;
    }

    public Resources getResources() {
        return resources;
    }

    public String getWhatsNew() {
        return applicationInfo.metaData.getString("Layers_WhatsNew");
    }

    public String getDescription() {
        return applicationInfo.metaData.getString("Layers_Description");
    }

    public String getCacheDir() {
        return context.getCacheDir().getAbsolutePath();
    }


    @Override
    public void close() throws IOException {
        RootCommands.DeleteFileRoot(getCacheDir() + File.separator + getName());
    }
}

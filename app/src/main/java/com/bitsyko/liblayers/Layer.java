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
import android.util.ArrayMap;
import android.util.Log;
import android.util.Pair;

import com.lovejoy777.rroandlayersmanager.commands.RootCommands;
import com.stericson.RootTools.RootTools;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

public class Layer implements Closeable {
    private static final String ACTION_PICK_PLUGIN = "com.layers.plugins.PICK_OVERLAYS";

    private final String name;
    private final String packageName;
    private final String developer;
    private List<Drawable> screenShots;
    private Drawable promo;
    private final ApplicationInfo applicationInfo;
    private final Resources resources;
    private final Context context;
    private List<String> colors;

    //Map for unpacked zipfiles from layer
    private Map<String, ZipFile> zipFileMap = new ArrayMap<>();

    private final Drawable icon;

    public Layer(String name, String developer, Drawable icon) {
        this(name, developer, icon, null, null, null, null);
    }

    private Layer(String name, String developer, Drawable icon, String packageName,
                  Resources resources, ApplicationInfo applicationInfo, Context context) {
        this.name = name;
        this.developer = developer;
        this.icon = icon;
        this.packageName = packageName;
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

        return new Layer(name, developer, icon, packageName, resources, applicationInfo, context);
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
            }

        }

        return layerList;
    }

    public String getVersionCode() {
        try {
            return context.getApplicationContext().getPackageManager().getPackageInfo(packageName, 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
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

    private int screenShotId = 1;

    public Pair<Integer, Drawable> getNextScreenshot() {

        String drawableName = "screenshot" + screenShotId;

        int mDrawableResID = resources.getIdentifier(drawableName, "drawable", packageName);

        if (mDrawableResID == 0) {
            return new Pair<>(0, null);
        }

        Drawable drawable = resources.getDrawable(mDrawableResID, null);

        screenShotId++;

        return new Pair<>(screenShotId - 1, drawable);
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

                files.add(new LayerFile(this, layer, false, getPluginVersion()));
            }

            String[] styleSpecificOverlayNames = bundle.getString("Layers_StyleSpecificOverlays", "").split(",");


            for (String layer : styleSpecificOverlayNames) {
                if (!layer.equals("")) {
                    files.add(new LayerFile(this, layer, true, getPluginVersion()));
                }
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

                files.add(new LayerFile(this, overlay, !normalOverlay,getPluginVersion()));

            }

        }


        return files;

    }

    public int getPluginVersion(){
        int mPluginVersion = 2;
        Bundle bundle = applicationInfo.metaData;
        if (bundle.containsKey("Layers_PluginVersion")){
            mPluginVersion = Integer.parseInt(bundle.getString("Layers_PluginVersion"));
        }
        return mPluginVersion;
    }

    public List<String> getColors() {

        if (colors == null) {

            Bundle bundle = applicationInfo.metaData;

            colors = new ArrayList<>();

            colors.addAll(Arrays.asList(bundle.getString("Layers_Colors", "").split(",")));
            colors.addAll(Arrays.asList(bundle.getString("Styles", "").split(",")));

            colors.remove("");

        }

        return colors;
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

    public Context getRelatedContext() {
        return context;
    }

    public ZipFile getFileFromMap(String name) {
        return zipFileMap.get(name);
    }

    public ZipFile putFileToMap(ZipFile zipFile, String name) {
        return zipFileMap.put(name, zipFile);
    }

    public boolean mapHasFile(String name) {
        return zipFileMap.containsKey(name);
    }

    @Override
    public void close() throws IOException {
        if (new File(getCacheDir() + File.separator + getName()).exists()) {
            RootCommands.DeleteFileRoot(getCacheDir() + File.separator + getName());
        }

        for (ZipFile zipFile : zipFileMap.values()) {
            zipFile.close();
        }

        zipFileMap.clear();
    }

}

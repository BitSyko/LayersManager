package com.bitsyko.liblayers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Pair;

import com.bitsyko.liblayers.layerfiles.ColorOverlay;
import com.bitsyko.liblayers.layerfiles.CustomStyleOverlay;
import com.bitsyko.liblayers.layerfiles.GeneralOverlay;
import com.bitsyko.liblayers.layerfiles.LayerFile;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
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
    private List<Color> colors = new ArrayList<>();
    private String generalZip;

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
        this.context = context.getApplicationContext();
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


    public List<com.bitsyko.liblayers.layerfiles.LayerFile> getLayersInPackage() {

        List<com.bitsyko.liblayers.layerfiles.LayerFile> layers = new ArrayList<>();

        Bundle bundle = applicationInfo.metaData;

        AssetManager assetManager = getResources().getAssets();

        String layerNameWithoutWhitespace = StringUtils.deleteWhitespace(name);

        List<String> layerZips = new ArrayList<>();

        try {
            layerZips.addAll(Arrays.asList(assetManager.list("Files")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<LayerFile> generalOverlays = new ArrayList<>();
        Set<LayerFile> colorOverlays = new HashSet<>();
        List<LayerFile> customStylesOverlay = new ArrayList<>();

        for (String overlayFile : layerZips) {

            InputStream in;
            try {
                in = assetManager.open("Files" + File.separator + overlayFile);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            boolean generalOverlay = false;

            if (StringUtils.endsWithIgnoreCase(overlayFile, "general.zip")) {
                generalOverlay = true;
            }

            //Extracting zip
            File zipFile = new File(context.getCacheDir() + File.separator + StringUtils.deleteWhitespace(getName()) + File.separator + overlayFile);

            try {
                FileUtils.copyInputStreamToFile(in, zipFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Checking zip content

            ArrayList<? extends ZipEntry> zipEntries = new ArrayList<>();

            try {
                ZipFile zip = new ZipFile(zipFile);
                zipEntries = Collections.list(zip.entries());
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean customStyle = false;
            List<Color> customStyles = new ArrayList<>();


            for (ZipEntry zipEntry : zipEntries) {

                if (generalOverlay) {
                    generalZip = overlayFile;
                    generalOverlays.add(new GeneralOverlay(this, zipEntry.getName()));
                    continue;
                }

                //Contains folders <=> custom style overlay
                if (zipEntry.getName().contains("/")) {

                    //If it's not folder
                    if (!zipEntry.getName().endsWith("/")) {
                        customStyle = true;
                        customStyles.add(new Style(zipEntry.getName(), this));
                    }

                } else {
                    //Color overlay
                    colorOverlays.add(new ColorOverlay(this, zipEntry.getName()));
                    colors.add(new Color(overlayFile, this));
                }


            }

            if (customStyle) {
                customStylesOverlay.add(new CustomStyleOverlay(this, overlayFile, customStyles));
            }

        }


        //To remove duplicates
        //TODO: Make it better
        colors = new ArrayList<>(new HashSet<>(colors));
        Collections.sort(colors);

        List<LayerFile> colorOverlaysList = new ArrayList<>(colorOverlays);

        Collections.sort(generalOverlays);
        Collections.sort(customStylesOverlay);
        Collections.sort(colorOverlaysList);

        //We're showing custom styles overlay after general
        layers.addAll(generalOverlays);
        layers.addAll(customStylesOverlay);
        layers.addAll(colorOverlaysList);

        return layers;
    }

    /*

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

*/


    public int getPluginVersion() {
        int mPluginVersion = 2;
        Bundle bundle = applicationInfo.metaData;
        if (bundle.containsKey("Layers_PluginVersion")) {
            mPluginVersion = Integer.parseInt(bundle.getString("Layers_PluginVersion"));
        }
        return mPluginVersion;
    }

    public List<Color> getColors() {
        return new ArrayList<>(colors);
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
            RootCommands.DeleteFileRoot(context.getCacheDir() + File.separator + StringUtils.deleteWhitespace(getName()));
        }

        for (ZipFile zipFile : zipFileMap.values()) {
            zipFile.close();
        }

        zipFileMap.clear();
    }


    public String getGeneralZip() {
        return generalZip;
    }


}

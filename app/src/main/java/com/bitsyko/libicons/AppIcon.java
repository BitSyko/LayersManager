package com.bitsyko.libicons;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.util.Pair;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kellinwood.security.zipsigner.ZipSigner;

public class AppIcon {

    private ApplicationInfo applicationInfo;
    private Resources res;
    private Context context;
    private IconPack iconPack;

    //ClassName:Drawable
    List<Pair<String, String>> iconList = new ArrayList<>();

    private boolean activityInList(String activity) {

        for (Pair<String, String> pair : iconList) {
            if (pair.first.equals(activity)) {
                return true;
            }
        }

        return false;
    }

    public AppIcon(Context context, String packageName, IconPack iconPack) throws PackageManager.NameNotFoundException {
        this.context = context;
        this.applicationInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES);
        this.res = context.getPackageManager().getResourcesForApplication(applicationInfo);
        this.iconPack = iconPack;
    }

    public AppIcon(Context context, String packageName, IconPack iconPack, Collection<Pair<String, String>> iconList) throws PackageManager.NameNotFoundException {
        this(context, packageName, iconPack);
        this.iconList.addAll(iconList);
    }

    public void addIconPair(Pair<String, String> pair) {
        iconList.add(pair);
    }

    public void addIconPair(Collection<Pair<String, String>> pairs) {
        iconList.addAll(pairs);
    }

    public String getPackageName() {
        return applicationInfo.packageName;
    }

    public String getName() {
        return String.valueOf(applicationInfo.loadLabel(context.getPackageManager()));
    }

    public void install() throws Exception {

        //Assumption: Icons are in the same folder as launcher icon

        String apkLocation = applicationInfo.sourceDir;
        File appt = new File(context.getCacheDir() + "/aapt");

        Process nativeApp = Runtime.getRuntime().exec(new String[]{
                appt.getAbsolutePath(),
                "dump", "badging",
                apkLocation});

        String data = IOUtils.toString(nativeApp.getInputStream());
        String error = IOUtils.toString(nativeApp.getErrorStream());

        nativeApp.waitFor();

        if (!StringUtils.isEmpty(error)) {
            throw new RuntimeException(error);
        }


        String[] lines = data.split(System.getProperty("line.separator"));

        List<String> list = new ArrayList<>();

        for (String string : lines) {
            if (string.contains("application-icon-")) {
                list.add(StringUtils.substringBetween(string, "'"));
            }
        }

        if (list.isEmpty()) {
            throw new RuntimeException("No application icon");
        }

        String appIcon = new File(list.get(0)).getName().replace(".png", "");

        List<String> iconLocation = new ArrayList<>();

        for (String string : list) {
            iconLocation.add(new File(string).getParent());
        }

        PackageInfo packageInfo = context.getPackageManager().getPackageInfo(getPackageName(),
                PackageManager.GET_ACTIVITIES);


        Map<String, String> activitiesWithIcons = new HashMap<>();

        Resources resources = context.getPackageManager().getResourcesForApplication(getPackageName());
        Resources iconPackResources = context.getPackageManager().getResourcesForApplication(iconPack.getPackageName());

        if (packageInfo.activities == null || packageInfo.activities.length == 0) {
            Log.e(getPackageName(), "No activities");
            return;
        }

        for (android.content.pm.ActivityInfo a : packageInfo.activities) {
            activitiesWithIcons.put(a.name, StringUtils.substringAfter(resources.getResourceName(a.getIconResource()), "/"));
        }


        Log.d("App Icon", appIcon);
        Log.d("Icon locations", String.valueOf(iconLocation));
        Log.d("Activities", String.valueOf(activitiesWithIcons));


        for (Pair<String, String> activityWithIcon : iconList) {

            if (!activitiesWithIcons.keySet().contains(activityWithIcon.first)) {
                continue;
            }

            String iconName = activitiesWithIcons.get(activityWithIcon.first);

            int drawableIconID = iconPackResources.getIdentifier(activityWithIcon.second, "drawable", iconPack.getPackageName());
            int mipmapIconID = iconPackResources.getIdentifier(activityWithIcon.second, "mipmap", iconPack.getPackageName());

            if (drawableIconID == 0 && mipmapIconID == 0) {
                Log.e("No icon found", activityWithIcon.second);
            }

            int finalIconID = drawableIconID == 0 ? mipmapIconID : drawableIconID;

            BitmapDrawable icon;

            try {
                icon = (BitmapDrawable) iconPackResources.getDrawable(finalIconID, null);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Vector", "Not supported");
                continue;
            }

            if (icon == null) {
                Log.d("Missing resource", activityWithIcon.second);
                continue;
            }


            for (String location : iconLocation) {

                File destFile = new File(context.getCacheDir() + "/tempFolder/" + getPackageName() + "/" + location + "/" + iconName + ".png");

                destFile.getParentFile().mkdirs();

                FileOutputStream out = new FileOutputStream(destFile);
                icon.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, out);

                out.close();

            }


        }



        String tempManifest =
                IOUtils.toString(context.getAssets().open("AndroidManifest.xml"))
                        .replace("<<TARGET_PACKAGE>>", getPackageName())
                        .replace("<<PACKAGE_NAME>>", "pl.andrzejressel.icon." + getPackageName());


        FileUtils.writeStringToFile(new File(context.getCacheDir() + "/tempFolder/" + getPackageName() + "/AndroidManifest.xml"), tempManifest);


        if (!new File(context.getCacheDir() + "/tempFolder/" + getPackageName() + "/res").exists()) {
            return;
        }


        File unsignedApp = new File(context.getCacheDir() + "/tempFolder/unsigned." + getPackageName() + ".apk");

        File signedApp = new File(context.getCacheDir() + "/tempFolder/signed." + getPackageName() + ".apk");



        nativeApp = Runtime.getRuntime().exec(new String[]{
                appt.getAbsolutePath(), "p",
                "-M", context.getCacheDir() + "/tempFolder/" + getPackageName() + "/AndroidManifest.xml",
                "-S", context.getCacheDir() + "/tempFolder/" + getPackageName() + "/res",
                "-I", "system/framework/framework-res.apk",
                "-F", unsignedApp.getAbsolutePath()});


        IOUtils.toString(nativeApp.getInputStream());
        IOUtils.toString(nativeApp.getErrorStream());

        nativeApp.waitFor();

        Log.d("Signing start", "");

        ZipSigner zipSigner = new ZipSigner();
        zipSigner.setKeymode("testkey");
        zipSigner.signZip(unsignedApp.getAbsolutePath(), signedApp.getAbsolutePath());

        Log.d("Signing end", "");





    }


}

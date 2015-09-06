package com.bitsyko.libicons;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IconPack implements com.bitsyko.ApplicationInfo, Closeable {

    Context context;
    ApplicationInfo applicationInfo;
    Resources res;

    public IconPack(String packageName, Context context) throws PackageManager.NameNotFoundException {
        this.applicationInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        this.res = context.getPackageManager().getResourcesForApplication(applicationInfo);
        this.context = context;
    }


    public static List<IconPack> getIconPacksInSystem(Activity activity) {

        List<String> iconPacksPackages = new ArrayList<>(getSupportedPackages(activity));

        List<IconPack> iconPacks = new ArrayList<>();

        for (String packageName : iconPacksPackages) {

            try {
                IconPack iconPack = new IconPack(packageName, activity);
                iconPacks.add(iconPack);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

        }

        return iconPacks;
    }


    private XmlPullParser getXml(String file) {

        XmlPullParser parser = null;

        InputStream inputStream;

        try {
            inputStream = res.getAssets().open(file + ".xml");
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            parser = factory.newPullParser();
            parser.setInput(inputStream, "UTF-8");
        } catch (Exception e) {
            // Catch any exception since we want to fall back to parsing the xml/
            // resource in all cases
            int resId = res.getIdentifier(file, "xml", getPackageName());
            if (resId != 0) {
                parser = res.getXml(resId);
            }
        }

        return parser;
    }


    private static Set<String> getSupportedPackages(Activity activity) {
        Intent i = new Intent();

        Set<String> packages = new HashSet<>();
        PackageManager packageManager = activity.getPackageManager();
        for (String action : sSupportedActions) {
            i.setAction(action);
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                packages.add(r.activityInfo.packageName);
            }
        }
        i = new Intent(Intent.ACTION_MAIN);
        for (String category : sSupportedCategories) {
            i.addCategory(category);
            for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
                packages.add(r.activityInfo.packageName);
            }
            i.removeCategory(category);
        }
        return packages;
    }

    public final static String[] sSupportedActions = new String[]{
            "org.adw.launcher.THEMES",
            "com.gau.go.launcherex.theme"
    };

    public static final String[] sSupportedCategories = new String[]{
            "com.fede.launcher.THEME_ICONPACK",
            "com.anddoes.launcher.THEME",
            "com.teslacoilsw.launcher.THEME"
    };


    @Override
    public String getName() {
        return String.valueOf(applicationInfo.loadLabel(context.getPackageManager()));
    }

    @Override
    public String getDeveloper() {
        return "";
    }

    @Override
    public Drawable getIcon() {
        return applicationInfo.loadIcon(context.getPackageManager());
    }

    @Override
    public String getPackageName() {
        return applicationInfo.packageName;
    }

    @Override
    public void close() throws IOException {

    }

    public List<AppIcon> getCompatibleApps() {

        Map<String, List<Pair<String, String>>> map = new HashMap<>();

        try {
            loadResourcesFromXmlParser(getXml("appfilter"), map);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }


        List<AppIcon> appList = new ArrayList<>();

        List<ApplicationInfo> packages = context.getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA);

        List<String> installedPackages = new ArrayList<>();

        for (ApplicationInfo app : packages) {
            installedPackages.add(app.packageName);
        }


        for (String appName : map.keySet()) {
            if (!installedPackages.contains(appName)) {
                continue;
            }

            try {
                appList.add(new AppIcon(context, appName, this, map.get(appName)));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }


        return appList;
    }


    public List<Drawable> getPreviewImages() {

        List<String> previewString;

        try {
            previewString = getPreviewImagesXml(getXml("themecfg"), "preview");
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }


        List<Drawable> drawableList = new ArrayList<>();

        for (String preview : previewString) {

            int drawableId = res.getIdentifier(preview, "drawable", getPackageName());
            int mipmapId = res.getIdentifier(preview, "mipmap", getPackageName());

            if (mipmapId == 0 && drawableId == 0) {
                continue;
            }

            int correctId = drawableId == 0 ? mipmapId : drawableId;

            drawableList.add(res.getDrawable(correctId));

        }

        return drawableList;

    }

    private List<String> getPreviewImagesXml(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {

        List<String> images = new ArrayList<>();

        int eventType = parser.getEventType();

        do {

            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }

            if (!parser.getName().equalsIgnoreCase(tag)) {
                continue;
            }

            String attribute = parser.getAttributeValue(0);

            images.add(attribute);

        } while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT);

        return images;

    }

    private String getFirstTagElement(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {

        int eventType = parser.getEventType();
        do {

            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }

            if (!parser.getName().equalsIgnoreCase(tag)) {
                continue;
            }

            return parser.getAttributeValue(0);

        } while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT);


        return null;

    }


    private String getTextInTag(XmlPullParser parser, String tag) throws XmlPullParserException, IOException {

        int eventType = parser.getEventType();
        do {

            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }

            if (!parser.getName().equalsIgnoreCase(tag)) {
                continue;
            }

            return parser.nextText();


        } while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT);


        return null;

    }


    public String getDescription() {
        try {
            return getTextInTag(getXml("themecfg"), "themeName");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getWhatsNew() {
        try {
            return getTextInTag(getXml("themecfg"), "themeInfo");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void loadResourcesFromXmlParser(XmlPullParser parser,
                                            Map<String, List<Pair<String, String>>> iconPackResources) throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        do {

            if (eventType != XmlPullParser.START_TAG) {
                continue;
            }

            if (!parser.getName().equalsIgnoreCase("item")) {
                continue;
            }

            String component = parser.getAttributeValue(null, "component");
            String drawable = parser.getAttributeValue(null, "drawable");

            // Validate component/drawable exist
            if (TextUtils.isEmpty(component) || TextUtils.isEmpty(drawable)) {
                continue;
            }

            // Validate format/length of component
            if (!component.startsWith("ComponentInfo{") || !component.endsWith("}")
                    || component.length() < 16) {
                continue;
            }

            // Sanitize stored value
            component = component.substring(14, component.length() - 1);


            ComponentName name = ComponentName.unflattenFromString(component);

            if (component.contains("/") && name != null) {

                String packageName = name.getPackageName();
                String className = name.getClassName();

                if (!iconPackResources.containsKey(packageName)) {
                    iconPackResources.put(packageName, new ArrayList<Pair<String, String>>());
                }

                iconPackResources.get(packageName).add(new Pair<>(className, drawable));


            } else {
                Log.d("Error package", component);
            }

        } while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT);
    }


}

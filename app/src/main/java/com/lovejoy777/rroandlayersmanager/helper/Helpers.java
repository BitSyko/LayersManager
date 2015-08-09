package com.lovejoy777.rroandlayersmanager.helper;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Helpers {

    public static Collection<String> allPackagesInSystem(Context context) {

        ArrayList<String> packagesString = new ArrayList<>();

        final PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages = pm
                .getInstalledApplications(PackageManager.GET_META_DATA);


        for (ApplicationInfo applicationInfo : packages) {
            packagesString.add(applicationInfo.packageName);
        }



        return packagesString;
    }

}

package com.lovejoy777.rroandlayersmanager.helper;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
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


    public static String commandToString(String command) {
        try {
            Process process = new ProcessBuilder()
                    .command(command)
                    .redirectErrorStream(true)
                    .start();

            InputStream input = process.getInputStream();

            return IOUtils.toString(input);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}

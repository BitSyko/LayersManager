package com.lovejoy777.rroandlayersmanager.commands;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.bitsyko.liblayers.layerfiles.LayerFile;
import com.lovejoy777.rroandlayersmanager.AsyncResponse;
import com.lovejoy777.rroandlayersmanager.DeviceSingleton;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.Utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class Commands {

    //No instances
    private Commands() {
    }

    public static ArrayList<String> loadFiles(String directory) {

        File f = new File(directory);
        ArrayList<String> files = new ArrayList<>();

        if (!f.exists() || !f.isDirectory()) {
            return files;
        }

        for (File file : f.listFiles()) {
            if (!file.isDirectory()) {
                files.add(file.getName());
            }
        }
        Collections.sort(files, String.CASE_INSENSITIVE_ORDER);
        return files;
    }

    public static ArrayList<String> loadFolders(String directory) {

        File f = new File(directory);
        ArrayList<String> folders = new ArrayList<>();

        if (!f.exists() || !f.isDirectory()) {
            return folders;
        }

        for (File file : f.listFiles()) {
            if (file.isDirectory()) {
                folders.add(file.getName());
            }
        }
        return folders;
    }


    public static void reboot(final Context context) {
        AlertDialog.Builder progressDialogReboot = new AlertDialog.Builder(context);
        progressDialogReboot.setTitle(R.string.commands_rebootdialog_title);
        progressDialogReboot.setMessage(R.string.commands_rebootdialog_message);
        progressDialogReboot.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            //when Cancel Button is clicked
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        progressDialogReboot.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            //when Cancel Button is clicked
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Runtime.getRuntime()
                            .exec(new String[]{"su", "-c", "am restart"});
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        progressDialogReboot.show();
    }


    public static ArrayList<String> fileNamesFromZip(File zip) throws IOException {

        ArrayList<String> files = new ArrayList<>();

        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
        ZipEntry ze;

        while ((ze = zis.getNextEntry()) != null) {
            files.add(ze.getName());
        }

        return files;

    }


    public static class InstallZipBetterWay extends AsyncTask<String, Void, Void> {

        Context context;
        ProgressDialog progressBackup;
        AsyncResponse callback;

        public InstallZipBetterWay(Context context, AsyncResponse callback) {
            this.context = context;
            this.callback = callback;
        }

        @Override
        protected void onPreExecute() {
            progressBackup = ProgressDialog.show(context, context.getString(R.string.commands_installingdialog_title),
                    context.getString(R.string.commands_installingdialog_message) + "...", true);
        }

        @Override
        protected Void doInBackground(String... files) {

            String tempDir = context.getCacheDir().getAbsolutePath() + File.separator + "overlay/";

            File OverlayDirectory = new File(DeviceSingleton.getInstance().getMountFolder());
            if (!OverlayDirectory.exists()){
                OverlayDirectory.mkdir();
            }

            try {
                FileUtils.deleteDirectory(new File(tempDir));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            if (!new File(tempDir).mkdirs()) {
                if (!new File(tempDir).exists()) {
                    throw new RuntimeException("Cannot create temp folder");
                }
            }


            for (String file : files) {

                if (file.endsWith(".zip")) {

                    Log.d("Extracting", file);

                    try {

                        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)));
                        ZipEntry ze;
                        ZipFile zipFile = new ZipFile(file);

                        while ((ze = zis.getNextEntry()) != null) {
                            FileUtils.copyInputStreamToFile(zipFile.getInputStream(ze), new File(tempDir + ze.getName()));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("Extracting failed", file);
                    }


                } else if (file.endsWith(".apk")) {

                    Log.d("Copying", file);

                    File apkFile = new File(file);

                    try {
                        FileUtils.copyFile(apkFile, new File(tempDir + apkFile.getName()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("Copying failed", file);
                    }

                }


            }

            Utils.remount("rw",DeviceSingleton.getInstance().getMountFolder());
            System.out.println("MOVE!");
            Utils.moveFile(tempDir , DeviceSingleton.getInstance().getParentOfOverlayFolder() + "/");
            Utils.applyPermissionsRecursive(DeviceSingleton.getInstance().getOverlayFolder(), "644");
            Utils.applyPermissions(DeviceSingleton.getInstance().getOverlayFolder(), "755");
            Utils.remount("ro",DeviceSingleton.getInstance().getMountFolder());
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            progressBackup.dismiss();
            if (callback != null) {
                callback.processFinish();
            }
        }
    }

    public static BufferedReader runCommand(String cmd) {
        BufferedReader reader;
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(
                    process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));
            String err = (new BufferedReader(new InputStreamReader(
                    process.getErrorStream()))).readLine();
            os.flush();

            if (process.waitFor() != 0 || !StringUtils.isEmpty(err)) {

                if (err != null) {
                    Log.e("Root Error, cmd: " + cmd, err);
                } else {
                    Log.e("Root Error, cmd: " + cmd, "");
                }

                return null;
            }
            return reader;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static class UnInstallOverlays extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress;
        private ArrayList<String> paths;
        private Context Context;
        private AsyncResponse delegate;
        private int i = 0;

        public UnInstallOverlays(ArrayList<String> paths, Context context, AsyncResponse response) {
            this.paths = paths;
            this.Context = context;
            this.delegate = response;
        }

        protected void onPreExecute() {
            progress = new ProgressDialog(Context);
            progress.setTitle(R.string.commands_uninstalldialog_title);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setProgress(0);
            progress.show();
            progress.setCancelable(false);
            progress.setMax(paths.size());
        }

        @Override
        protected Void doInBackground(Void... params) {
            Utils.remount("rw",DeviceSingleton.getInstance().getMountFolder());
            for (String path : paths) {
                Log.d("Removing: ", path);
                try {
                    Utils.deleteFile(path);
                } catch (Exception e) {
                    Log.w("Cannot remove: ", path);
                    e.printStackTrace();
                }
                publishProgress();
            }
            Utils.remount("ro",DeviceSingleton.getInstance().getMountFolder());
            return null;
        }

        protected void onProgressUpdate(Void... progress2) {
            progress.setProgress(++i);
        }

        protected void onPostExecute(Void result) {
            progress.dismiss();
            if (delegate != null) {
                delegate.processFinish();
            }
        }

    }

    public static class InstallOverlaysBetterWay extends AsyncTask<Void, String, Void> {

        private ProgressDialog progress;
        private AsyncResponse delegate;
        private List<LayerFile> layersToInstall;
        private Context context;
        private int i = 0;
        public int mode;

        public InstallOverlaysBetterWay(List<LayerFile> layersToInstall, Context context, AsyncResponse delegate,int mode) {
            this.layersToInstall = layersToInstall;
            this.context = context;
            this.delegate = delegate;
            this.mode = mode;
        }

        @Override
        protected void onPreExecute() {

                progress = new ProgressDialog(context);
                progress.setTitle(R.string.commands_installingdialog_title);
                progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progress.setProgress(i);
                progress.setCancelable(false);
                progress.setMax(layersToInstall.size()+4);
                progress.setMessage("");
                progress.show();


        }


        @Override
        protected Void doInBackground(Void... params) {

            // Modes
            //  0: Normal Installation,
            //  1: Create Symlink before installation
            //  2: Install to symLink


    // Mount
    publishProgress("Mounting Filesystem");
                switch (mode){
                    case 0:
                        Utils.remount("rw",DeviceSingleton.getInstance().getMountFolder());
                        break;
                    case 1:
                        Utils.remount("rw", "/system");
                        Utils.remount("rw", "/vendor");
                        break;
                    case 2:
                        Utils.remount("rw", "/system");
                        break;
                }


    //Manage Overlay Folders
    publishProgress("Managing Overlay folders");
            switch (mode){
                case 0:
                    File OverlayDirectory = new File(DeviceSingleton.getInstance().getOverlayFolder());
                    if (!OverlayDirectory.exists()){
                        Utils.createFolder2(DeviceSingleton.getInstance().getOverlayFolder());
                    }
                    break;
                case 1:
                    //Create /System/Overlay
                    Log.d("SymLinker","Creating /system/overlay");
                    Utils.createFolder2("/system/overlay/");
                    //Delete /vendor/Overlay if existing
                    Log.d("SymLinker","Delete /vendor/Overlay if existing");
                    File VendorOverlay = new File(DeviceSingleton.getInstance().getOverlayFolder());
                    if (VendorOverlay.exists()){
                        Utils.deleteFile(VendorOverlay.getAbsolutePath());
                    }
                    //SymLink system/Overlay to Vendor/Overlay
                    Log.d("SymLinker","SymLink system/Overlay to Vendor/Overlay");
                    Utils.Symlink("/system/overlay/", DeviceSingleton.getInstance().getParentOfOverlayFolder());
                    break;
                case 2:
                    //
                    break;
            }


    //Copy Overlays
            String OverlayFolder = null;
            switch (mode){
                case 0:
                    OverlayFolder = DeviceSingleton.getInstance().getOverlayFolder() + "/";
                    break;
                case 1:
                    OverlayFolder = "/system/overlay/";
                    break;
                case 2:
                    OverlayFolder = "/system/overlay/";
                    break;
            }
            for (LayerFile layerFile : layersToInstall) {
                try {
                    Utils.moveFile(layerFile.getFile(context).getAbsolutePath(),OverlayFolder);
                    publishProgress("Installing: "+layerFile.getNiceName());
                } catch (Exception e) {
                    e.printStackTrace();
                    publishProgress(e.getMessage());
                }
            }
            if (!layersToInstall.isEmpty()) {
                try {
                    layersToInstall.get(0).getLayer().close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


    //Change Permissions
    publishProgress("Changing Permissions");
            Utils.applyPermissionsRecursive("/system/overlay/", "644");
            Utils.applyPermissionsRecursive(OverlayFolder, "644");
            Log.d("SymLinker","Give Permissions to Overlay Files (755)");
            Utils.applyPermissions(OverlayFolder, "755");


    //Remount
    publishProgress("Remounting Filesystem");
            switch (mode){
                case 0:
                    Utils.remount("ro",DeviceSingleton.getInstance().getMountFolder());
                    break;
                case 1:
                    Utils.remount("ro", "/system");
                    Utils.remount("ro", "/vendor");
                    break;
                case 2:
                    Utils.remount("ro", "/system");
                    break;
            }

/*

            if (doSymlink){
                //Create /System/Overlay
                Log.d("SymLinker","Creating /system/overlay");
                Utils.createFolder2("/system/overlay/");
                //Delete /vendor/Overlay if existing
                Log.d("SymLinker","Delete /vendor/Overlay if existing");
                File VendorOverlay = new File(DeviceSingleton.getInstance().getOverlayFolder());
                if (VendorOverlay.exists()){
                    Utils.deleteFile(VendorOverlay.getAbsolutePath());
                }
            }
            if (symLinkAlreadyPresent){
                //
            }
            if (!doSymlink){

            }



            //SYSTEMLINK
            if (doSymlink){
                //Mount System & Vendor
                publishProgress("Mounting");
                Log.d("SymLinker","Mounting System and Vendor");
                Utils.remount("rw", "/system");
                Utils.remount("rw", "/vendor");

                //Create /System/Overlay
                publishProgress("Create new Overlay Folder");
                Log.d("SymLinker","Creating /system/overlay");
                Utils.createFolder2("/system/overlay/");

                //Copy Overlays
                Log.d("SymLinker","Copy Overlays");
                for (LayerFile layerFile : layersToInstall) {
                    try {
                        Utils.moveFile(layerFile.getFile(context).getAbsolutePath(), "/system/overlay/");
                            publishProgress(layerFile.getNiceName());
                        } catch (Exception e) {
                            e.printStackTrace();
                            publishProgress(e.getMessage());
                        }
                    }
                    if (!layersToInstall.isEmpty()) {
                        try {
                            layersToInstall.get(0).getLayer().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    publishProgress("Change Permissions");
                    //Give Permissions to Overlay Files (644)
                    Log.d("SymLinker","Give Permissions to Overlay Files (644)");
                    Utils.applyPermissionsRecursive("/system/overlay/", "644");
                    //Give Permissions to Overlay Folder (755)
                    Log.d("SymLinker","Give Permissions to Overlay Files (755)");
                    Utils.applyPermissions("/system/overlay/", "755");

                    publishProgress("Delete old Overlay folder");
                    //Delete /vendor/Overlay if existing
                    Log.d("SymLinker","Delete /vendor/Overlay if existing");
                    File VendorOverlay = new File(DeviceSingleton.getInstance().getOverlayFolder());
                    if (VendorOverlay.exists()){
                        Utils.deleteFile(VendorOverlay.getAbsolutePath());
                    }

                    publishProgress("Creating Symlink");
                    //SymLink system/Overlay to Vendor/Overlay
                    Log.d("SymLinker","SymLink system/Overlay to Vendor/Overlay");
                    Utils.Symlink("/system/overlay/", DeviceSingleton.getInstance().getParentOfOverlayFolder());
                    publishProgress("Change Permissions");
                    //Give Permissions to Overlay Folder (755)
                    Log.d("SymLinker","Give Permissions to Overlay Files (755) AGAIN");
                    Utils.applyPermissions("/system/overlay/", "755");

                    Utils.remount("ro", "/system");
                    Utils.remount("ro", "/vendor");

            }else {


                //SymlinkedMode
                if (symLinkAlreadyPresent){
                    publishProgress("Mounting");
                    Utils.remount("rw","/system");
                    for (LayerFile layerFile : layersToInstall) {
                        try {
                            Utils.moveFile(layerFile.getFile(context).getAbsolutePath(), "/system/overlay/");

                            publishProgress(layerFile.getNiceName());
                        } catch (Exception e) {
                            e.printStackTrace();
                            publishProgress(e.getMessage());
                        }

                    }
                    if (!layersToInstall.isEmpty()) {
                        try {
                            layersToInstall.get(0).getLayer().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    publishProgress("Change Permissions");
                    //Give Permissions to Overlay Files (644)
                    Log.d("SymLinker","Give Permissions to Overlay Files (644)");
                    Utils.applyPermissionsRecursive("/system/overlay/", "644");
                    //Give Permissions to Overlay Folder (755)
                    Log.d("SymLinker","Give Permissions to Overlay Files (755) AGAIN");
                    Utils.applyPermissions("/system/overlay/", "755");
                    Utils.remount("ro", "/system");
                }
                //NORMAL MODE
                else {
                    // MOUNT /SYSTEM RW
                    Utils.remount("rw",DeviceSingleton.getInstance().getMountFolder());
                    File OverlayDirectory = new File(DeviceSingleton.getInstance().getMountFolder());
                    if (!OverlayDirectory.exists()){
                        OverlayDirectory.mkdir();
                    }
                    for (LayerFile layerFile : layersToInstall) {
                        try {
                            Utils.moveFile(layerFile.getFile(context).getAbsolutePath(),
                                    DeviceSingleton.getInstance().getOverlayFolder() + "/");

                            publishProgress();
                        } catch (Exception e) {
                            e.printStackTrace();
                            publishProgress(e.getMessage());
                        }

                    }
                    if (!layersToInstall.isEmpty()) {
                        try {
                            layersToInstall.get(0).getLayer().close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    Utils.applyPermissionsRecursive(DeviceSingleton.getInstance().getOverlayFolder(), "644");
                    Utils.applyPermissions(DeviceSingleton.getInstance().getOverlayFolder(), "755");
                    Utils.remount("ro",DeviceSingleton.getInstance().getMountFolder());
                }





            }


/*
            // ANDROID N LOW VENDOR SPACE WORKAROUND
                Utils.remount("rw", "/system");
                Utils.remount("rw", "/vendor");


                //1. Delete VENDOR OVERLAY
                File VendorOverlay = new File(DeviceSingleton.getInstance().getOverlayFolder());
                if (VendorOverlay.exists()){
                    Utils.deleteFile(VendorOverlay.getAbsolutePath());
                }

                //2. Create DATA Overlay
                //File DataOverlay = new File("/data/overlay/");
                //if (DataOverlay.exists()){
                    Utils.createFolder2("/system/overlay/");
               // }

                //3. Copy Ovrlays to DATA Overlays
                for (LayerFile layerFile : layersToInstall) {
                    try {
                        Utils.moveFile(layerFile.getFile(context).getAbsolutePath(),
                                "/system/overlay/");

                        publishProgress();
                    } catch (Exception e) {
                        e.printStackTrace();
                        publishProgress(e.getMessage());
                    }

                }
                if (!layersToInstall.isEmpty()) {
                    try {
                        layersToInstall.get(0).getLayer().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Utils.applyPermissionsRecursive("/system/overlay/", "644");
                Utils.applyPermissions("/system/overlay/", "755");

                // 4. SymLink to DATA Overlay to Vendor/Overlay
                Utils.Symlink("/system/overlay/", DeviceSingleton.getInstance().getParentOfOverlayFolder());
                Utils.applyPermissions("/system/overlay/", "755");

    /*
                //1. Delete VENDOR OVERLAY
                File StockOverlayDirectory = new File(DeviceSingleton.getInstance().getOverlayFolder());
                if (StockOverlayDirectory.exists()){
                    Utils.deleteFile(StockOverlayDirectory.getAbsolutePath());
                }

                //2. Delete SYSTEM OVERLAY
                File OverlayDirectory = new File("/system/overlay/");
                if (OverlayDirectory.exists()){
                    Utils.deleteFile(OverlayDirectory.getAbsolutePath());
                }


                //3. Helper Folder
                String HelperFolder = Environment.getExternalStorageDirectory() + "/overlay/";
                File dir1 = new File(HelperFolder);
                dir1.mkdirs();

                //4. Symlink Helper to Stock Folder
                Utils.Symlink(HelperFolder,StockOverlayDirectory.getAbsolutePath());

                //5. Install theme to stock Folder
                for (LayerFile layerFile : layersToInstall) {
                    try {
                        Utils.moveFile(layerFile.getFile(context).getAbsolutePath(),
                                DeviceSingleton.getInstance().getOverlayFolder() + "/");

                        publishProgress();
                    } catch (Exception e) {
                        e.printStackTrace();
                        publishProgress(e.getMessage());
                    }

                }
                if (!layersToInstall.isEmpty()) {
                    try {
                        layersToInstall.get(0).getLayer().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Utils.applyPermissionsRecursive(DeviceSingleton.getInstance().getOverlayFolder(), "644");
                Utils.applyPermissions(DeviceSingleton.getInstance().getOverlayFolder(), "755");

                //6. Delete VENDOR OVERLAY
                if (StockOverlayDirectory.exists()){
                    Utils.deleteFile(StockOverlayDirectory.getAbsolutePath());
                }

                // 7. NEW OVERLAY FOLDER: SYSTEM/OVERLAY
                Utils.createFolder(OverlayDirectory);
                Utils.applyPermissions(OverlayDirectory.getAbsolutePath(),"755");

                // 8. Copy Overlays
               // File[] fList = dir1.listFiles();
                //for (File file : fList) {
                //    Utils.moveFile(file.getAbsolutePath(),"/system/overlay");
                //}
                Utils.moveFile(HelperFolder,"/system/");
                Utils.applyPermissions(OverlayDirectory.getAbsolutePath(),"755");
                Utils.applyPermissionsRecursive(OverlayDirectory.getAbsolutePath(),"644");

                // 9. SymLink to Vendor/Overlay
                Utils.Symlink("/system/overlay/", DeviceSingleton.getInstance().getParentOfOverlayFolder());

                Utils.applyPermissions(OverlayDirectory.getAbsolutePath(),"755");
                /*
                // 1. NEW OVERLAY FOLDER: SYSTEM/OVERLAY
                File OverlayDirectory = new File("/system/overlay/");
                Utils.deleteFile(OverlayDirectory.getAbsolutePath());
                Utils.createFolder(OverlayDirectory);
                Utils.applyPermissions(OverlayDirectory.getAbsolutePath(),"755");

                //2. Delete Normall Overlay Folder




                // 4. Helper Directory SD CARD
                String sdOverlays1 = Environment.getExternalStorageDirectory() + "/overlay/";
                File dir1 = new File(sdOverlays1);
                dir1.mkdirs();

                // 5. Copy to helper Directory
                for (LayerFile layerFile : layersToInstall) {
                    try {
                        Utils.moveFile(layerFile.getFile(context).getAbsolutePath(),
                                sdOverlays1);
                        publishProgress();
                    } catch (Exception e) {
                        e.printStackTrace();
                        publishProgress(e.getMessage());
                    }
                }

                // 4. Copy Overlays
                Utils.moveFile(sdOverlays1,"/system/");

                Utils.applyPermissionsRecursive(OverlayDirectory.getAbsolutePath(),"644");

                // 3. SymLink to Vendor/Overlay
                Utils.Symlink("/system/overlay/", DeviceSingleton.getInstance().getParentOfOverlayFolder());

                // 2. SystemLink to VENDOR/OVERLAY
            /*

            File OverlayDirectory = new File(DeviceSingleton.getInstance().getMountFolder());
            if (OverlayDirectory.exists()){
                OverlayDirectory.delete();
            }

                // Create overlay folder on internal sd
                String sdOverlays1 = Environment.getExternalStorageDirectory() + "/overlay/";
                File dir1 = new File(sdOverlays1);
                dir1.mkdirs();

                //Copy Overlays to temp. folder
                for (LayerFile layerFile : layersToInstall) {
                    try {
                        Utils.moveFile(layerFile.getFile(context).getAbsolutePath(),
                                sdOverlays1);
                        publishProgress();
                    } catch (Exception e) {
                        e.printStackTrace();
                        publishProgress(e.getMessage());
                    }
                }
/*

                //Copy Files to Workaround direct.
                Utils.moveFile(sdOverlays1,"/system/");
                //Check & Create Workaround Overlay Directory
                Utils.remount("rw");


                //Chmod Workaraound folder
                Utils.applyPermissions("/system/overlay/", "755");

                //Chmod Files
                Utils.applyPermissionsRecursive("/system/overlay/","644");

                //Symlink
                Utils.Symlink("/system/overlay/","/vendor");



           /* Utils.remount("rw");


            */

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values.length != 0) {
                //Toast.makeText(context, values[0], Toast.LENGTH_LONG).show();
            }
            progress.setProgress(++i);
            progress.setMessage(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progress.dismiss();
            if (delegate != null) {
                delegate.processFinish();
            }
        }

    }

    public static int getSortMode(Activity context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("sortMode", 0);
    }

    public static void setSortMode(Activity context, int mode) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("sortMode", mode).commit();
    }


    public static void killLauncherIcon(Context context) {

        PackageManager p = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, com.lovejoy777.rroandlayersmanager.MainActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        Toast.makeText(context, context.getResources().getString(R.string.commands_hidelauncher_removed), Toast.LENGTH_SHORT).show();

    }

    public static void ReviveLauncherIcon(Context context) {

        PackageManager p = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, com.lovejoy777.rroandlayersmanager.MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Toast.makeText(context, context.getResources().getString(R.string.commands_hidelauncher_revived), Toast.LENGTH_SHORT).show();

    }

   public static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }




}

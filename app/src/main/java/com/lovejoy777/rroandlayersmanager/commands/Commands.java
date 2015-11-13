package com.lovejoy777.rroandlayersmanager.commands;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.bitsyko.liblayers.layerfiles.LayerFile;
import com.lovejoy777.rroandlayersmanager.AsyncResponse;
import com.lovejoy777.rroandlayersmanager.DeviceSingleton;
import com.lovejoy777.rroandlayersmanager.R;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
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
        progressDialogReboot.setTitle(R.string.Reboot);
        progressDialogReboot.setMessage(R.string.PreformReboot);
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
                            .exec(new String[]{"su", "-c", "busybox killall system_server"});
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
            progressBackup = ProgressDialog.show(context, context.getString(R.string.installingOverlays),
                    context.getString(R.string.installing) + "...", true);
        }

        @Override
        protected Void doInBackground(String... files) {

            String tempDir = context.getCacheDir().getAbsolutePath() + File.separator + "zipCache/";

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


            remountSystem("rw");

            RootCommands.moveRoot(tempDir + "*", DeviceSingleton.getInstance().getOverlayFolder() + "/");

            try {
                // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER & FILES TO 644 RECURING
                CommandCapture command9 = new CommandCapture(0, "chmod -R 644 " + DeviceSingleton.getInstance().getOverlayFolder());
                RootTools.getShell(true).add(command9);
                while (!command9.isFinished()) {
                    Thread.sleep(1);
                }


                // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER BACK TO 755
                CommandCapture command10 = new CommandCapture(0, "chmod 755 " + DeviceSingleton.getInstance().getOverlayFolder());
                RootTools.getShell(true).add(command10);
                while (!command10.isFinished()) {
                    Thread.sleep(1);
                }

                remountSystem("ro");
                // CLOSE ALL SHELLS
                RootTools.closeAllShells();

            } catch (IOException | RootDeniedException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }


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

    public static boolean remountSystem(String mountType) {
        String mountPoint = DeviceSingleton.getInstance().getMountFolder();
        BufferedReader reader = runCommand("mount -o remount,"
                + mountType + " " + mountPoint + "\n");
        try {
            if (reader != null) reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
            progress.setTitle(R.string.uninstallingOverlays);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setProgress(0);
            progress.show();
            progress.setCancelable(false);
            progress.setMax(paths.size());
        }

        @Override
        protected Void doInBackground(Void... params) {
            remountSystem("rw");
            for (String path : paths) {
                Log.d("Removing: ", path);
                try {
                    RootTools.deleteFileOrDirectory(RootCommands.getCommandLineString(path), false);
                } catch (Exception e) {
                    Log.w("Cannot remove: ", path);
                    e.printStackTrace();
                }
                publishProgress();
            }
            remountSystem("ro");
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

        public InstallOverlaysBetterWay(List<LayerFile> layersToInstall, Context context, AsyncResponse delegate) {
            this.layersToInstall = layersToInstall;
            this.context = context;
            this.delegate = delegate;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(context);
            progress.setTitle(R.string.installingOverlays);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setProgress(i);
            progress.show();
            progress.setCancelable(false);
            progress.setMax(layersToInstall.size());
        }


        @Override
        protected Void doInBackground(Void... params) {

            // MOUNT /SYSTEM RW
            remountSystem("rw");

            for (LayerFile layerFile : layersToInstall) {
                try {

                    String filelocation = RootCommands.getCommandLineString(layerFile.getFile(context).getAbsolutePath());

                    RootCommands.moveRoot(filelocation, DeviceSingleton.getInstance().getOverlayFolder() + "/");

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

            try {
                // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER & FILES TO 644 RECURING
                CommandCapture command9 = new CommandCapture(0, "chmod -R 644 " + DeviceSingleton.getInstance().getOverlayFolder());
                RootTools.getShell(true).add(command9);
                while (!command9.isFinished()) {
                    Thread.sleep(1);
                }


                // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER BACK TO 755
                CommandCapture command10 = new CommandCapture(0, "chmod 755 " + DeviceSingleton.getInstance().getOverlayFolder());
                RootTools.getShell(true).add(command10);
                while (!command10.isFinished()) {
                    Thread.sleep(1);
                }

                remountSystem("ro");

                // CLOSE ALL SHELLS
                RootTools.closeAllShells();

            } catch (IOException | RootDeniedException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (values.length != 0) {
                Toast.makeText(context, values[0], Toast.LENGTH_LONG).show();
            }
            progress.setProgress(++i);
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
        Toast.makeText(context, context.getResources().getString(R.string.launcherIconRemoved), Toast.LENGTH_SHORT).show();

    }

    public static void ReviveLauncherIcon(Context context) {

        PackageManager p = context.getPackageManager();
        ComponentName componentName = new ComponentName(context, com.lovejoy777.rroandlayersmanager.MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        Toast.makeText(context, context.getResources().getString(R.string.launcherIconRevived), Toast.LENGTH_SHORT).show();

    }

}

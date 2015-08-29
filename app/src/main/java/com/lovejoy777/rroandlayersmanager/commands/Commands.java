package com.lovejoy777.rroandlayersmanager.commands;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.bitsyko.libicons.AppIcon;
import com.bitsyko.liblayers.LayerFile;
import com.bitsyko.liblayers.NoFileInZipException;
import com.lovejoy777.rroandlayersmanager.AsyncResponse;
import com.lovejoy777.rroandlayersmanager.DeviceSingleton;
import com.lovejoy777.rroandlayersmanager.R;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
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


    public static ArrayList<String> RootloadFiles(final Context context, final Activity act, String directory) {
        ArrayList<String> files = new ArrayList<>();
        if (RootTools.isAccessGiven()) {
            try {
                String line;
                Process process = Runtime.getRuntime().exec("su");
                OutputStream stdin = process.getOutputStream();
                InputStream stderr = process.getErrorStream();
                InputStream stdout = process.getInputStream();

                stdin.write(("ls -a " + directory + "\n").getBytes());

                stdin.write("exit\n".getBytes());
                stdin.flush();   //flush stream
                stdin.close(); //close stream

                BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

                while ((line = br.readLine()) != null) {

                    files.add(line);
                }
                br.close();
                br = new BufferedReader(new InputStreamReader(stderr));
                while ((line = br.readLine()) != null) {
                    Log.e("[Error]", line);
                }
                process.waitFor();//wait for process to finish
                process.destroy();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            act.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast toast = Toast.makeText(context, R.string.noRoot, Toast.LENGTH_LONG);
                    toast.show();
                }
            });

        }
        return files;
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


    public static InputStream fileFromZip(File zip, String file) throws IOException {

        ZipFile zipFile = new ZipFile(zip);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zip)));
        ZipEntry ze;

        while ((ze = zis.getNextEntry()) != null) {
            if (ze.getName().equalsIgnoreCase(file.replaceAll(" ", ""))) {
                return zipFile.getInputStream(ze);
            }
        }


        throw new NoFileInZipException("No " + file + " in " + zip.getAbsolutePath());
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

            RootCommands.DeleteFileRoot(tempDir);

            if (!new File(tempDir).mkdirs()) {
                throw new RuntimeException("Cannot create temp folder");
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


            RootTools.remount(DeviceSingleton.getInstance().getMountFolder(), "RW");

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

                RootTools.remount(DeviceSingleton.getInstance().getMountFolder(), "RO");

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
            RootTools.remount(DeviceSingleton.getInstance().getMountFolder(), "RW");
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
            RootTools.remount(DeviceSingleton.getInstance().getMountFolder(), "RO");
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
        private String color;
        private int i = 0;

        public InstallOverlaysBetterWay(List<LayerFile> layersToInstall, String color, Context context, AsyncResponse delegate) {
            this.layersToInstall = layersToInstall;
            this.context = context;
            this.color = color;
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
            RootTools.remount(DeviceSingleton.getInstance().getMountFolder(), "RW");


            for (LayerFile layerFile : layersToInstall) {
                try {

                    String filelocation;

                    if (layerFile.isColor()) {
                        filelocation = RootCommands.getCommandLineString(layerFile.getFile(color).getAbsolutePath());
                    } else {
                        filelocation = RootCommands.getCommandLineString(layerFile.getFile().getAbsolutePath());
                    }

                    RootCommands.moveRoot(filelocation, DeviceSingleton.getInstance().getOverlayFolder() + "/");

                    publishProgress();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoFileInZipException e1) {
                    publishProgress(e1.getMessage());
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

                RootTools.remount(DeviceSingleton.getInstance().getMountFolder(), "RO");

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

    private static final String[] aaptUrls = {
            "https://www.dropbox.com/s/au7ccu1gtroqvzt/aapt_x86?dl=1",
            "https://www.dropbox.com/s/5x2fpgw6ojyao2d/aapt_arm?dl=1"};


    public static class InstallIcons extends AsyncTask<Void, String, Void> {

        ProgressDialog progress;
        Context context;
        List<AppIcon> list;
        AsyncResponse asyncResponse;
        int i = 0;

        public InstallIcons(Context context, List<AppIcon> list, AsyncResponse asyncResponse) {
            this.context = context;
            this.list = list;
            this.asyncResponse = asyncResponse;
        }

        @Override
        protected void onPreExecute() {
            progress = new ProgressDialog(context);
            progress.setTitle(R.string.installingOverlays);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setProgress(0);
            progress.setCancelable(false);
            progress.setMax(list.size());
            progress.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //super.onProgressUpdate(values);
            progress.setProgress(++i);
            if (values.length != 0) {
                Toast.makeText(context, values[0], Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                FileUtils.deleteDirectory(new File(context.getCacheDir() + "/tempFolder/"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Downloading aapt
            File appt = new File(context.getCacheDir() + "/aapt");

            if (!appt.exists()) {

                for (String url : aaptUrls) {

                    try {
                        FileUtils.copyURLToFile(new URL(url), appt);

                        Process checkAapt = Runtime.getRuntime().exec(new String[]{
                                appt.getAbsolutePath(), "v"});

                        String data = IOUtils.toString(checkAapt.getInputStream());
                        String error = IOUtils.toString(checkAapt.getErrorStream());

                        checkAapt.waitFor();

                        if (StringUtils.isEmpty(error)) {
                            Log.d("AAPT", data);
                            break;
                        }


                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }


                }

            }


            try {
                // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER BACK TO 755
                CommandCapture command10 = new CommandCapture(0, "chmod 700 " + appt.getAbsolutePath());
                RootTools.getShell(true).add(command10);
                while (!command10.isFinished()) {
                    Thread.sleep(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            for (AppIcon app : list) {
                Log.d("Installing", app.getPackageName());
                try {
                    app.install();
                    publishProgress();
                } catch (Exception e) {
                    e.printStackTrace();
                    publishProgress(e.getMessage());
                }
            }


            Log.d("Icon", "Moving");

            RootTools.remount(DeviceSingleton.getInstance().getMountFolder(), "RW");


            try {


                CommandCapture command3 = new CommandCapture(0, "mv -f " + context.getCacheDir() + "/tempFolder/signed*" + " " + DeviceSingleton.getInstance().getOverlayFolder());

                RootTools.getShell(true).add(command3);
                while (!command3.isFinished()) {
                    Thread.sleep(1);
                }


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


                RootTools.closeAllShells();

            } catch (Exception e) {
                e.printStackTrace();
            }

            RootTools.remount(DeviceSingleton.getInstance().getMountFolder(), "RO");


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progress.dismiss();
            asyncResponse.processFinish();
        }
    }


    public static int getSortMode(Activity context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt("sortMode", 1);
    }

    public static void setSortMode(Activity context, int mode) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("sortMode", mode).commit();
    }
}

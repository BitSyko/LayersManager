package com.lovejoy777.rroandlayersmanager.commands;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.bitsyko.liblayers.LayerFile;
import com.bitsyko.liblayers.NoFileInZipException;
import com.lovejoy777.rroandlayersmanager.AsyncResponse;
import com.lovejoy777.rroandlayersmanager.R;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;


public class Commands {
    private final static int BUFFER_SIZE = 1024;

    //No instances
    private Commands() {
    }


    public static ArrayList<String> RootloadFiles(final Context context, final Activity act, String directory) {
        ArrayList<String> files = new ArrayList<String>();
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
       // f.mkdirs();

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

    public static void unzipPluginOverlays(int NumberOfOverlays, int NumberOfColorOverlays, List<Integer> InstallOverlayList, String package2, String ThemeName, Context context, String whichColor) {

        int NumberOfSelectedNormalOverlays = 0;
        for (int i = 0; i < NumberOfOverlays; i++) {
            NumberOfSelectedNormalOverlays = NumberOfSelectedNormalOverlays + InstallOverlayList.get(i);
        }


        int NumberOfSelectedColorOverlays = 0;
        for (int i = NumberOfOverlays + 1; i < NumberOfColorOverlays + NumberOfOverlays + 1; i++) {
            NumberOfSelectedColorOverlays = NumberOfSelectedColorOverlays + InstallOverlayList.get(i);
        }

        System.out.println("NumberSelected " + NumberOfSelectedNormalOverlays);
        String filePath;
        String destinationGeneral = Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeName + "/General/";
        String destinationColor = Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeName + "/";

        if (NumberOfSelectedNormalOverlays > 0 /*|| NumberOfSelectedAdditionalOverlays > 0*/) {

            File GeneralDirectory = new File(Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeName + "/General");
            GeneralDirectory.mkdirs();
            filePath = Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeName + "/" + ThemeName + "_General.zip";

            try {
                FileInputStream inputStream = new FileInputStream(filePath);
                ZipInputStream zipStream = new ZipInputStream(inputStream);
                ZipEntry zEntry;
                while ((zEntry = zipStream.getNextEntry()) != null) {
                    FileOutputStream fout = new FileOutputStream(
                            destinationGeneral + "/" + zEntry.getName());
                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = zipStream.read(buffer)) != -1) {
                        bufout.write(buffer, 0, read);
                    }
                    zipStream.closeEntry();
                    bufout.close();
                    fout.close();

                }
                zipStream.close();
                Log.d("Unzip", "Unzipping complete. path :  " + destinationGeneral);
            } catch (Exception e) {
                Log.d("Unzip", "Unzipping failed");
                e.printStackTrace();
            }
        }

        if (NumberOfSelectedColorOverlays > 0 /*|| NumberOfSelectedAdditionalOverlays > 0*/) {

            File ColorDirectory = new File(Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeName + "/" + whichColor);
            ColorDirectory.mkdirs();

            destinationColor = destinationColor + whichColor;

            filePath = Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeName + "/" + ThemeName + "_" + whichColor + ".zip";
            try {
                FileInputStream inputStream = new FileInputStream(filePath);
                ZipInputStream zipStream = new ZipInputStream(inputStream);
                ZipEntry zEntry;
                while ((zEntry = zipStream.getNextEntry()) != null) {

                    FileOutputStream fout = new FileOutputStream(
                            destinationColor + "/" + zEntry.getName());
                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = zipStream.read(buffer)) != -1) {
                        bufout.write(buffer, 0, read);
                    }
                    zipStream.closeEntry();
                    bufout.close();
                    fout.close();

                }
                zipStream.close();
            } catch (Exception e) {
                Log.d("Unzip", "Unzipping failed");
                e.printStackTrace();
            }

        }

    }


    public static void unzipNormalOverlays(String zipFile, String location) throws IOException {

        int size;
        byte[] buffer = new byte[1024];

        try {

            if (!location.endsWith("/")) {
                location += "/";
            }
            File f = new File(location);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile), 1024));
            try {
                ZipEntry ze;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();
                    File unzipFile = new File(path);

                    if (ze.isDirectory()) {
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {

                        // check for and create parent directories if they don't exist
                        File parentDir = unzipFile.getParentFile();
                        if (null != parentDir) {
                            if (!parentDir.isDirectory()) {
                                parentDir.mkdirs();
                            }
                        }
                        // unzipNormalOverlays the file
                        FileOutputStream out = new FileOutputStream(unzipFile, false);
                        BufferedOutputStream fout = new BufferedOutputStream(out, 1024);
                        try {
                            while ((size = zin.read(buffer, 0, 1024)) != -1) {
                                fout.write(buffer, 0, size);
                            }
                            zin.closeEntry();
                        } finally {
                            fout.flush();
                            fout.close();
                            out.close();
                        }
                    }
                }
            } finally {
                zin.close();
            }
        } catch (Exception e) {
            Log.e("TAG", "Unzip exception", e);
        }
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
                    Process proc = Runtime.getRuntime()
                            .exec(new String[]{"su", "-c", "busybox killall system_server"});
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        progressDialogReboot.show();
    }

    private static void CopyFolderToSDCard(String package2, Context context, String ThemeName) {
        Context otherContext = null;
        final String packName = package2;
        try {
            otherContext = context.createPackageContext(packName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        AssetManager am = null;
        if (otherContext != null) {
            am = otherContext.getAssets();
        }

        String ThemeNameNoSpace = ThemeName.replaceAll(" ", "");
        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(packName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        File ThemeDirectory = new File(Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeNameNoSpace + "/");
        ThemeDirectory.mkdirs();
        CopyFolderToSDCard(context, ThemeNameNoSpace, am);
    }


    //copy files to sd card
    public static void CopyFolderToSDCard(Context context, String ThemeNameNoSpace, AssetManager assetFiles) {

        try {

            String[] files = assetFiles.list("Files");

            //initialize streams
            InputStream in;
            OutputStream out;

            for (String file : files) {

                if (file.equalsIgnoreCase("images")
                        || file.equalsIgnoreCase("js")) {
                    //nothing
                } else {
                    in = assetFiles.open("Files/" + file);
                    out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeNameNoSpace + "/" + file);
                    copyAssetFiles(in, out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void copyAssetFiles(InputStream in, OutputStream out) {

        try {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
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


    public static class InstallOverlays extends AsyncTask<Integer, Integer, Integer> {
        public AsyncResponse delegate = null;
        ProgressDialog progress;
        String Mode;
        Context Context;
        String ThemeName;
        ArrayList<String> Paths;
        String Package2;
        int NumberOfOverlays;
        int NumberOfColorOverlays;
        List<Integer> InstallOverlayList;
        String whichcolor;

        public InstallOverlays(String mode, Context context, String themeName, ArrayList<String> paths, String package2,
                               int NumberOfOverlays, int NumberOfColorOverlays, List<Integer> InstallOverlayList,
                               String whichColor, AsyncResponse delegate) {
            this.Mode = mode;
            this.Context = context;
            this.ThemeName = themeName;
            this.Paths = paths;
            this.Package2 = package2;
            this.NumberOfColorOverlays = NumberOfColorOverlays;
            this.NumberOfOverlays = NumberOfOverlays;
            this.InstallOverlayList = InstallOverlayList;
            this.whichcolor = whichColor;
            this.delegate = delegate;
        }

        protected void onPreExecute() {
            progress = new ProgressDialog(Context);
            progress.setTitle(R.string.installingOverlays);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setProgress(0);
            progress.show();
            progress.setCancelable(false);
            if (Mode.equals("Plugin")) {
                progress.setMax(Paths.size() + 1);
            } else {
                progress.setMax(Paths.size());
            }

        }


        @Override
        protected Integer doInBackground(Integer... params) {

            if (Mode.equals("Plugin")) {
                CopyFolderToSDCard(Package2, Context, ThemeName);  //copy Overlay Files to SD Card
                unzipPluginOverlays(NumberOfOverlays, NumberOfColorOverlays, InstallOverlayList, Package2, ThemeName, Context, whichcolor);  //unzipNormalOverlays Overlay ZIP´s
                publishProgress(1);
            }
            moveOverlays(Paths, Mode);
            return null;
        }

        protected void onProgressUpdate(Integer... progress2) {
            progress.setProgress(progress2[0]);
        }

        protected void onPostExecute(Integer result) {
            progress.dismiss();
            delegate.processFinish();
        }

        private void moveOverlays(ArrayList<String> paths, String Mode) {
            if (paths != null) {


                // MOUNT /SYSTEM RW
                RootTools.remount("/system", "RW");

                try {
                    // Create vendor Overlay Folder
                    CommandCapture command7 = new CommandCapture(0, "mkdir /vendor/overlay");
                    RootTools.getShell(true).add(command7);
                    while (!command7.isFinished()) {
                        Thread.sleep(1);
                    }

                    // CHANGE PERMISSIONS TO 777
                    CommandCapture command8 = new CommandCapture(0, "chmod -R 777 /vendor/overlay");
                    RootTools.getShell(true).add(command8);
                    while (!command8.isFinished()) {
                        Thread.sleep(1);
                    }
                } catch (IOException | RootDeniedException | TimeoutException | InterruptedException e) {
                    e.printStackTrace();
                }
                int i = 1;
                for (String path : paths) {
                    i = i + 1;

                    String layersdata = Context.getApplicationInfo().dataDir + "/overlay";
                    String overlaypath = "/vendor";
                    String iszip = ".zip";
                    String isapk = ".apk";


                    //IF File IS LESS THAN 1 CHAR DO THIS.
                    if (path.length() <= 1) {

                        Toast.makeText(Context, "CHOOSE A FILE", Toast.LENGTH_LONG).show();

                    } else {

                        // DELETES /DATA/DATA/OVERLAY
                        File dir = new File(Context.getApplicationInfo().dataDir + "/overlay/");
                        if (dir.exists() && dir.isDirectory()) {

                            RootCommands.DeleteFileRoot(Context.getApplicationInfo().dataDir + "/overlay");
                        }

                        // IF File ENDS WITH .ZIP
                        if (path.endsWith(iszip)) {

                            try {
                                // UNZIP & MOVE Files to temporary location
                                unzipNormalOverlays(path, layersdata);

                                // CHANGE PERMISSIONS 755
                                CommandCapture command = new CommandCapture(0, "chmod -R 755 " + layersdata);
                                RootTools.getShell(true).add(command);
                                while (!command.isFinished()) {
                                    Thread.sleep(1);
                                }

                                // IF PREVIEWIMAGES.ZIP EXISTS DELETE IT
                                String previewimageszip = Context.getApplicationInfo().dataDir + "/overlay/previewimages.zip";
                                File dir1 = new File(previewimageszip);
                                if (dir1.exists()) {

                                    RootCommands.DeleteFileRoot(previewimageszip);
                                }

                                try {

                                    // CHANGE PERMISSIONS OF UNZIPPED FOLDER & FILES
                                    CommandCapture command6 = new CommandCapture(0, "chmod -R 666 " + layersdata + "/");
                                    RootTools.getShell(true).add(command6);
                                    while (!command6.isFinished()) {
                                        Thread.sleep(1);
                                    }


                                    // COPY NEW FILES TO /VENDOR/OVERLAY FOLDER
                                    RootCommands.moveCopyRoot(layersdata + "/", overlaypath);


                                } catch (RootDeniedException | TimeoutException | InterruptedException e) {
                                    e.printStackTrace();
                                }

                            } catch (IOException | RootDeniedException | InterruptedException | TimeoutException e) {
                                e.printStackTrace();
                            }
                        }

                        // IF FILE ENDS WITH APK INSTALL APK
                        if (path.endsWith(isapk)) {

                            // COPY NEW FILES TO /VENDOR/OVERLAY FOLDER
                            System.out.println(path);
                            RootCommands.moveCopyRoot(path, overlaypath + "/overlay");

                        }
                    }
                    if (Mode.equals("Plugin")) {
                        publishProgress(i);
                    } else {
                        publishProgress(i - 1);
                    }


                }


                try {
                    // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER & FILES TO 644 RECURING
                    CommandCapture command9 = new CommandCapture(0, "chmod -R 644 /vendor/overlay");
                    RootTools.getShell(true).add(command9);
                    while (!command9.isFinished()) {
                        Thread.sleep(1);
                    }


                    // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER BACK TO 755
                    CommandCapture command10 = new CommandCapture(0, "chmod 755 /vendor/overlay");
                    RootTools.getShell(true).add(command10);
                    while (!command10.isFinished()) {
                        Thread.sleep(1);
                    }

                    RootTools.remount("/system", "RO");

                    // CLOSE ALL SHELLS
                    RootTools.closeAllShells();

                } catch (IOException | RootDeniedException | TimeoutException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class UnInstallOverlays extends AsyncTask<Integer, Integer, Integer> {
        ProgressDialog progress;
        ArrayList<String> Paths;
        Context Context;
        private AsyncResponse delegate;

        public UnInstallOverlays(ArrayList<String> paths, Context context, AsyncResponse response) {
            this.Paths = paths;
            this.Context = context;
            this.delegate = response;
        }

        public UnInstallOverlays(ArrayList<String> paths, Context context) {
            this(paths, context, null);
        }

        protected void onPreExecute() {
            progress = new ProgressDialog(Context);
            progress.setTitle(R.string.uninstallingOverlays);
            progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progress.setProgress(0);
            progress.show();
            progress.setCancelable(false);
            progress.setMax(Paths.size());
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            int i = 0;
            RootTools.remount("/system", "RW");
            for (String path : Paths) {
                Log.d("Removing: ", path);
                i = i + 1;
                try {
                    RootTools.deleteFileOrDirectory(RootCommands.getCommandLineString(path), false);
                } catch (Exception e) {
                    Log.w("Cannot remove: ", path);
                    e.printStackTrace();
                }
                publishProgress(i);
            }
            RootTools.remount("/system", "RO");
            return null;
        }

        protected void onProgressUpdate(Integer... progress2) {
            progress.setProgress(progress2[0]);
        }

        protected void onPostExecute(Integer result) {
            progress.dismiss();
            if (delegate != null) {
                delegate.processFinish();
            }
        }

    }

    public static class InstallOverlaysBetterWay extends AsyncTask<Void, String, Void> {

        ProgressDialog progress;
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
            RootTools.remount("/system", "RW");

            try {
                // Create vendor Overlay Folder
                CommandCapture command7 = new CommandCapture(0, "mkdir /system/vendor/overlay");
                RootTools.getShell(true).add(command7);
                while (!command7.isFinished()) {
                    Thread.sleep(1);
                }

                // CHANGE PERMISSIONS TO 777
                CommandCapture command8 = new CommandCapture(0, "chmod -R 777 /system/vendor/overlay");
                RootTools.getShell(true).add(command8);
                while (!command8.isFinished()) {
                    Thread.sleep(1);
                }


            } catch (IOException | RootDeniedException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }


            for (LayerFile layerFile : layersToInstall) {
                try {
                    if (layerFile.isColor()) {
                        RootCommands.moveCopyRoot(layerFile.getFile(color).getAbsolutePath(), "/system/vendor/overlay/");
                    } else {
                        RootCommands.moveCopyRoot(layerFile.getFile().getAbsolutePath(), "/system/vendor/overlay/");
                    }

                    publishProgress();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoFileInZipException e1) {
                    publishProgress(e1.getMessage());
                }

            }


            try {
                // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER & FILES TO 644 RECURING
                CommandCapture command9 = new CommandCapture(0, "chmod -R 644 /system/vendor/overlay");
                RootTools.getShell(true).add(command9);
                while (!command9.isFinished()) {
                    Thread.sleep(1);
                }


                // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER BACK TO 755
                CommandCapture command10 = new CommandCapture(0, "chmod 755 /system/vendor/overlay");
                RootTools.getShell(true).add(command10);
                while (!command10.isFinished()) {
                    Thread.sleep(1);
                }

                RootTools.remount("/system", "RO");

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
}

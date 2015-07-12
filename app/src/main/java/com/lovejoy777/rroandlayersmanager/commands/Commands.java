package com.lovejoy777.rroandlayersmanager.commands;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Niklas on 29.06.2015.
 */
public class Commands {
    private ArrayList<String> Files = new ArrayList<String>();


   public ArrayList<String> loadFiles(String directory){
        try {
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            stdin.write(("ls -a "+directory+"\n").getBytes());

            stdin.write("exit\n".getBytes());
            stdin.flush();   //flush stream
            stdin.close(); //close stream

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

            while ((line = br.readLine()) != null) {

                Files.add(line);
            }
            br.close();
            br =
                    new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("[Error]", line);
            }
            process.waitFor();//wait for process to finish
            process.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Files;
    }

    public void InstallOverlays(Context context,ArrayList<String> paths) {
        if (paths != null) {
            for (String path : paths) {
                if (path.startsWith("file://")) {
                    path = path.substring(7);

                    String layersdata = context.getApplicationInfo().dataDir + "/overlay";
                    String overlaypath = "/vendor";
                    String iszip = ".zip";
                    String isapk = ".apk";

                    //IF SZP IS LESS THAN 1 CHAR DO THIS.
                    if (path.length() <= 1) {

                        Toast.makeText(context, "CHOOSE A FILE", Toast.LENGTH_LONG).show();

                    } else {

                        // DELETES /DATA/DATA/OVERLAY
                        File dir = new File(context.getApplicationInfo().dataDir + "/overlay/");
                        if (dir.exists() && dir.isDirectory()) {

                            RootCommands.DeleteFileRoot(context.getApplicationInfo().dataDir + "/overlay");
                        }

                        // IF SZP ENDS WITH .ZIP
                        if (path.endsWith(iszip)) {

                            try {
                                // UNZIP & MOVE TO LAYERS DATA OVERLAY FOLDER
                                unzip(path, layersdata);

                                // CHANGE PERMISSIONS 755
                                CommandCapture command = new CommandCapture(0, "chmod -R 755 " + layersdata);
                                RootTools.getShell(true).add(command);
                                while (!command.isFinished()) {
                                    Thread.sleep(1);
                                }

                                // IF PREVIEWIMAGES.ZIP EXISTS DELETE IT
                                String previewimageszip = context.getApplicationInfo().dataDir + "/overlay/previewimages.zip";
                                File dir1 = new File(previewimageszip);
                                if (dir1.exists()) {

                                    RootCommands.DeleteFileRoot(previewimageszip);
                                }

                                try {

                                    // MK DIR LAYERSDATA
                                    CommandCapture command1 = new CommandCapture(0, "mkdir " + layersdata);
                                    RootTools.getShell(true).add(command1);
                                    while (!command1.isFinished()) {
                                        Thread.sleep(1);
                                    }

                                    // RootCommands.moveCopyRoot(path, layersdata + "/");
                                    // ELSE NOT A .ZIP THEN COPY FILE TO LAYERSDATA
                                    // CommandCapture command5 = new CommandCapture(0, "cp -f " + SZP + " " + layersdata + "/");
                                    //RootTools.getShell(true).add(command5);
                                    // while (!command5.isFinished()) {
                                    //     Thread.sleep(1);
                                    // }

                                    // CHANGE PERMISSIONS OF UNZIPPED FOLDER & FILES
                                    CommandCapture command6 = new CommandCapture(0, "chmod -R 666 " + layersdata + "/");
                                    RootTools.getShell(true).add(command6);
                                    while (!command6.isFinished()) {
                                        Thread.sleep(1);
                                    }
                                    // MOUNT /SYSTEM RW
                                    RootTools.remount("/system", "RW");

                                    // CHANGE PERMISSIONS OF UNZIPPED FOLDER & FILES
                                    CommandCapture command7 = new CommandCapture(0, "mkdir /vendor/overlay");
                                    RootTools.getShell(true).add(command7);
                                    while (!command7.isFinished()) {
                                        Thread.sleep(1);
                                    }

                                    // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER & FILES TO 777
                                    CommandCapture command8 = new CommandCapture(0, "chmod -R 777 /vendor/overlay");
                                    RootTools.getShell(true).add(command8);
                                    while (!command8.isFinished()) {
                                        Thread.sleep(1);
                                    }


                                    // COPY NEW FILES TO /VENDOR/OVERLAY FOLDER
                                    RootCommands.moveCopyRoot(layersdata + "/", overlaypath);

                                    // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER & FILES TO 666
                                    CommandCapture command9 = new CommandCapture(0, "chmod -R 644 /vendor/overlay");
                                    RootTools.getShell(true).add(command9);
                                    while (!command9.isFinished()) {
                                        Thread.sleep(1);
                                    }

                                    // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER BACK TO 777
                                    CommandCapture command10 = new CommandCapture(0, "chmod 755 /vendor/overlay");
                                    RootTools.getShell(true).add(command10);
                                    while (!command10.isFinished()) {
                                        Thread.sleep(1);
                                        RootTools.remount("/system", "RO");
                                    }

                                    // CLOSE ALL SHELLS
                                    RootTools.closeAllShells();

                                } catch (RootDeniedException | TimeoutException | InterruptedException e) {
                                    e.printStackTrace();
                                }

                            } catch (IOException | RootDeniedException | InterruptedException | TimeoutException e) {
                                e.printStackTrace();
                            }
                        }

                        // IF SZP ENDS WITH APK INSTALL APK
                        if (path.endsWith(isapk)) {
                            // INSTALL APK'S

                            try {

                                // MK DIR LAYERSDATA
                                CommandCapture command1 = new CommandCapture(0, "mkdir " + layersdata);
                                RootTools.getShell(true).add(command1);
                                while (!command1.isFinished()) {
                                    Thread.sleep(1);
                                }

                                RootCommands.moveCopyRoot(path, layersdata + "/");
                                // ELSE NOT A .ZIP THEN COPY FILE TO LAYERSDATA
                                // CommandCapture command5 = new CommandCapture(0, "cp -f " + path + " " + layersdata + "/");
                                // RootTools.getShell(true).add(command5);
                                //  while (!command5.isFinished()) {
                                //      Thread.sleep(1);
                                //  }

                                // CHANGE PERMISSIONS OF UNZIPPED FOLDER & FILES
                                CommandCapture command6 = new CommandCapture(0, "chmod -R 666 " + layersdata + "/");
                                RootTools.getShell(true).add(command6);
                                while (!command6.isFinished()) {
                                    Thread.sleep(1);
                                }
                                // MOUNT /SYSTEM RW
                                RootTools.remount("/system", "RW");

                                // CHANGE PERMISSIONS OF UNZIPPED FOLDER & FILES
                                CommandCapture command7 = new CommandCapture(0, "mkdir /vendor/overlay");
                                RootTools.getShell(true).add(command7);
                                while (!command7.isFinished()) {
                                    Thread.sleep(1);
                                }

                                // CHANGE PERMISSIONS TO COPY FINAL /VENDOR/OVERLAY FOLDER & FILES TO 777
                                CommandCapture command8 = new CommandCapture(0, "chmod -R 777 /vendor/overlay");
                                RootTools.getShell(true).add(command8);
                                while (!command8.isFinished()) {
                                    Thread.sleep(1);
                                }

                                // COPY NEW FILES TO /VENDOR/OVERLAY FOLDER
                                RootCommands.moveCopyRoot(layersdata + "/", overlaypath);

                                // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER & FILES TO 666 RECURING
                                CommandCapture command9 = new CommandCapture(0, "chmod -R 644 /vendor/overlay");
                                RootTools.getShell(true).add(command9);
                                while (!command9.isFinished()) {
                                    Thread.sleep(1);
                                }

                                // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER BACK TO 777
                                CommandCapture command10 = new CommandCapture(0, "chmod 755 /vendor/overlay");
                                RootTools.getShell(true).add(command10);
                                while (!command10.isFinished()) {
                                    Thread.sleep(1);
                                    RootTools.remount("/system", "RO");
                                }

                                // CLOSE ALL SHELLS
                                RootTools.closeAllShells();

                            } catch (IOException | RootDeniedException | TimeoutException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }


    public void unzip(String zipFile, String location) throws IOException {

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
                        // unzip the file
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


}

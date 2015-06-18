package com.lovejoy777.rroandlayersmanager.actions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;
import com.lovejoy777.rroandlayersmanager.filepicker.FilePickerActivity;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by lovejoy777 on 10/06/15.
 */
public class Install extends AppCompatActivity{

    static final String TAG = "Install";
    private String previewimageszip = null;

    final String startDirInstall = Environment.getExternalStorageDirectory() +  "/Overlays";
    private static final int CODE_SD = 0;
    private static final int CODE_DB = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        previewimageszip = getApplicationInfo().dataDir + "/overlay/previewimages.zip";
        // GET STRING SZP
        final Intent extras = getIntent();
        String SZP = null;
        if (extras != null) {
            SZP = extras.getStringExtra("key1");
        }

        if (SZP != null) {

            installcommand();

        } else {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            // Set these depending on your use case. These are the defaults.
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, startDirInstall);
            i.putExtra("FilePickerMode","Install Overlays");

            // start filePicker forResult
            startActivityForResult(i, CODE_SD);
        }
    } // ends onCreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if ((CODE_SD == requestCode || CODE_DB == requestCode) &&
                resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE,
                    false)) {
                ArrayList<String> paths = data.getStringArrayListExtra(
                        FilePickerActivity.EXTRA_PATHS);
                StringBuilder sb = new StringBuilder();

                if (paths != null) {
                    for (String path : paths) {
                        if (path.startsWith("file://")) {
                            path = path.substring(7);
                            sb.append(path);
                        }
                    }

                    String SZP = (sb.toString());
                    Intent iIntent = new Intent(this, Install.class);
                    iIntent.putExtra("key1", SZP);
                    startActivity(iIntent);
                    finish();

                }

            } else {
                // Get the File path from the Uri
                String SZP = (data.getData().toString());
                if (SZP.startsWith("file://")) {
                    SZP = SZP.substring(7);
                    Intent iIntent = new Intent(this, Install.class);
                    iIntent.putExtra("key1", SZP);
                    startActivity(iIntent);
                    finish();
                }
            }
        }
    } // ends onActivityForResult

    public void installcommand() {

        Intent extras = getIntent();

        if (extras != null) {

            String SZP = extras.getStringExtra("key1");
            String layersdata = getApplicationInfo().dataDir + "/overlay";
            String iszip = ".zip";
            String isapk = ".apk";

            //IF SZP IS LESS THAN 1 CHAR DO THIS.
            if (SZP.length() <= 1) {

                Toast.makeText(Install.this, "CHOOSE A FILE", Toast.LENGTH_LONG).show();

            } else {

                // DELETES /DATA/DATA/OVERLAY
                File dir = new File(getApplicationInfo().dataDir + "/overlay/");
                if (dir.exists() && dir.isDirectory()) {

                    RootCommands.DeleteFileRoot(getApplicationInfo().dataDir + "/overlay");
                }

                // IF SZP ENDS WITH .ZIP
                if (SZP.endsWith(iszip)) {

                    try {
                        // UNZIP & MOVE TO LAYERS DATA OVERLAY FOLDER
                        unzip(SZP, layersdata);

                        // CHANGE PERMISSIONS 755
                        CommandCapture command = new CommandCapture(0, "chmod -R 755 " + layersdata);
                        RootTools.getShell(true).add(command);
                        while (!command.isFinished()) {
                            Thread.sleep(1);
                        }

                        // IF PREVIEWIMAGES.ZIP EXISTS DELETE IT
                        File dir1 = new File(previewimageszip);
                        if (dir1.exists()) {

                            RootCommands.DeleteFileRoot(previewimageszip);
                        }

                            // INSTALL .ZIP
                            installzip();


                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (RootDeniedException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // IF SZP ENDS WITH APK INSTALL APK
                if (SZP.endsWith(isapk)) {
                    // INSTALL APK'S
                    installapk();
                }
            }
        } else {

        }
    } // ENDS COMMAND 1

    public void installzip() {

        String layersdata = getApplicationInfo().dataDir + "/overlay";
        String overlaypath = "/vendor";

        try {

            // CHANGE PERMISSIONS OF UNZIPPED FOLDER & FILES
            CommandCapture command = new CommandCapture(0, "chmod -R 666 " + layersdata + "/");
            RootTools.getShell(true).add(command);
            while (!command.isFinished()) {
                Thread.sleep(1);
            }

            // MOUNT /SYSTEM RW
            RootTools.remount("/system", "RW");

            // MK DIR /VENDOR/OVERLAY
            CommandCapture command1 = new CommandCapture(0, "mkdir /vendor/overlay");
            RootTools.getShell(true).add(command1);
            while (!command1.isFinished()) {
                Thread.sleep(1);
            }

            // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER & FILES TO 777
            CommandCapture command2 = new CommandCapture(0, "chmod -R 777 /vendor/overlay");
            RootTools.getShell(true).add(command2);
            while (!command2.isFinished()) {
                Thread.sleep(1);
            }

            // COPY NEW FILES TO /VENDOR/OVERLAY FOLDER
            RootCommands.moveCopyRoot(layersdata + "/", overlaypath);

            // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER & FILES TO 666
            CommandCapture command3 = new CommandCapture(0, "chmod -R 666 /vendor/overlay");
            RootTools.getShell(true).add(command3);
            while (!command3.isFinished()) {
                Thread.sleep(1);
            }

            // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER BACK TO 777
            CommandCapture command4 = new CommandCapture(0, "chmod 777 /vendor/overlay");
            RootTools.getShell(true).add(command4);
            while (!command4.isFinished()) {
                Thread.sleep(1);
            }

            // CLOSE ALL SHELLS
            RootTools.closeAllShells();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();

        } finally {
            finish();
        }
    }

    public void installapk() {

        Intent extras = getIntent();
        String SZP = extras.getStringExtra("key1");
        String layersdata = getApplicationInfo().dataDir + "/overlay";

        try {

            // MK DIR LAYERSDATA
            CommandCapture command1 = new CommandCapture(0, "mkdir " + layersdata);
            RootTools.getShell(true).add(command1);
            while (!command1.isFinished()) {
                Thread.sleep(1);
            }

            // ELSE NOT A .ZIP THEN COPY FILE TO LAYERSDATA
            CommandCapture command5 = new CommandCapture(0, "cp -f " + SZP + " " + layersdata + "/");
            RootTools.getShell(true).add(command5);
            while (!command5.isFinished()) {
                Thread.sleep(1);
            }

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



            // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER & FILES TO 666
            CommandCapture command9 = new CommandCapture(0, "chmod -R 666 /vendor/overlay");
            RootTools.getShell(true).add(command9);
            while (!command9.isFinished()) {
                Thread.sleep(1);
            }

            // CHANGE PERMISSIONS OF FINAL /VENDOR/OVERLAY FOLDER BACK TO 777
            CommandCapture command10 = new CommandCapture(0, "chmod 777 /vendor/overlay");
            RootTools.getShell(true).add(command10);
            while (!command10.isFinished()) {
                Thread.sleep(1);
                RootTools.remount("/system", "RO");
            }

            // CLOSE ALL SHELLS
            RootTools.closeAllShells();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();

        } finally {
            finish();
        }
    }

    /**
     * **********************************************************************************************************
     * UNZIP UTIL
     * ************
     * Unzip a zip file.  Will overwrite existing files.
     *
     * @param zipFile  Full path of the zip file you'd like to unzip.
     * @param location Full path of the directory you'd like to unzip to (will be created if it doesn't exist).
     * @throws java.io.IOException *************************************************************************************************************
     */
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
            Log.e(TAG, "Unzip exception", e);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back2, R.anim.back1);
    }
}

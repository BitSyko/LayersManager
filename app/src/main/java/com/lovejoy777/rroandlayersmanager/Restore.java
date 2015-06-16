package com.lovejoy777.rroandlayersmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lovejoy777.rroandlayersmanager.activities.MainActivity;
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
 * Created by lovejoy777 on 13/06/15.
 */
public class Restore extends Activity{

    static final String TAG = "Restore";
    final String startDirBackup = Environment.getExternalStorageDirectory() +  "/Overlays/Backup";
    private static final int CODE_SD = 0;
    private static final int CODE_DB = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // GET STRING SZP
        final Intent extras = getIntent();
        String SZP = null;
        if (extras != null) {
            SZP = extras.getStringExtra("key1");
        }

        if (SZP != null) {

            restore();

        } else {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            // Set these depending on your use case. These are the defaults.
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, startDirBackup);
            i.putExtra("FilePickerMode","Restore Overlays");

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
                    Intent iIntent = new Intent(this, Restore.class);
                    iIntent.putExtra("key1", SZP);
                    startActivity(iIntent);
                    finish();

                }

            } else {
                // Get the File path from the Uri
                String SZP = (data.getData().toString());
                if (SZP.startsWith("file://")) {
                    SZP = SZP.substring(7);
                    Intent iIntent = new Intent(this, Restore.class);
                    iIntent.putExtra("key1", SZP);
                    startActivity(iIntent);
                    finish();
                }
            }
        }
    } // ends onActivityForResult

    public void restore() {

        Intent extras = getIntent();
        String SZP = extras.getStringExtra("key1");

        if (SZP != null) {


            new RestoreOverlays().execute(SZP);
        } else {

            Toast.makeText(Restore.this, "select a backup to restore", Toast.LENGTH_LONG).show();

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




    private class RestoreOverlays extends AsyncTask<String,String,Void> {
        ProgressDialog progressBackup;

        protected void onPreExecute() {

            progressBackup = ProgressDialog.show(Restore.this, "Restore Overlays",
                    "Restoring...", true);
        }

        @Override
        protected Void doInBackground(String... params) {
            String SZP = params[0];
            try {

                RootTools.remount("/system/", "RW");

                // DELETE /VENDOR/OVERLAY

                RootCommands.DeleteFileRoot("/vendor/overlay");

                // MK DIR /VENDOR/OVERLAY
                CommandCapture command3 = new CommandCapture(0, "mkdir /vendor/overlay");

                RootTools.getShell(true).add(command3);
                while (!command3.isFinished()) {
                    Thread.sleep(1);
                }

                // MK DIR /SDCARD/OVERLAYS/BACKUP/TEMP
                CommandCapture command4 = new CommandCapture(0, "mkdir" + Environment.getExternalStorageDirectory() + "/Overlays/Backup/Temp");

                RootTools.getShell(true).add(command4);
                while (!command4.isFinished()) {
                    Thread.sleep(1);
                }

                // MK DIR /SDCARD/OVERLAYS/BACKUP/TEMP/OVERLAY
                CommandCapture command5 = new CommandCapture(0, "mkdir" + Environment.getExternalStorageDirectory() + "/Overlays/Backup/Temp/overlay");

                RootTools.getShell(true).add(command5);
                while (!command5.isFinished()) {
                    Thread.sleep(1);
                }

                // CHANGE PERMISSIONS OF /VENDOR/OVERLAY && /SDCARD/OVERLAYS/BACKUP
                CommandCapture command6 = new CommandCapture(0, "chmod 755 /vendor/overlay", "chmod 755 " + Environment.getExternalStorageDirectory() + "/Overlays/Backup");
                RootTools.getShell(true).add(command6);
                while (!command6.isFinished()) {
                    Thread.sleep(1);
                }

                // UNZIP SZP TO /SDCARD/OVERLAYS/BACKUP/TEMP/OVERLAY FOLDER
                unzip(SZP, Environment.getExternalStorageDirectory() + "/Overlays/Backup/Temp/overlay");

                // MOVE /SDCARD/OVERLAYS/BACKUP/TEMP/OVERLAY TO /VENDOR/
                RootCommands.moveCopyRoot(Environment.getExternalStorageDirectory() + "/Overlays/Backup/Temp/overlay", "/vendor/");

                // DELETE /SDCARD/OVERLAYS/BACKUP/TEMP FOLDER
                RootCommands.DeleteFileRoot(Environment.getExternalStorageDirectory() + "/Overlays/Backup/Temp");

                // CHANGE PERMISSIONS OF /VENDOR/OVERLAY/ 666  && /VENDOR/OVERLAY 777 && /SDCARD/OVERLAYS/BACKUP/ 666
                CommandCapture command7 = new CommandCapture(0, "chmod -R 666 /vendor/overlay", "chmod 755 /vendor/overlay", "chmod -R 666" + Environment.getExternalStorageDirectory() + "/Overlays/Backup");
                RootTools.getShell(true).add(command7);
                while (!command7.isFinished()) {
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
        }
            return null;

        }

        protected void onPostExecute(Void result) {

            progressBackup.dismiss();



            //show SnackBar after sucessfull installation of the overlays

            /*Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), "Restored Overlays", Snackbar.LENGTH_LONG)
                    .setAction(R.string.Reboot, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //(new Reboot()).execute();
                        }
                    })
                    .show();
*/
            Toast.makeText(Restore.this, "restore complete", Toast.LENGTH_LONG).show();
            finish();

            // LAUNCH LAYERS.CLASS
            overridePendingTransition(R.anim.back2, R.anim.back1);
            Intent iIntent = new Intent(Restore.this, MainActivity.class);
            iIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(iIntent);

        }
    }
}

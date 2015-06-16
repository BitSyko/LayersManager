package com.lovejoy777.rroandlayersmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.lovejoy777.rroandlayersmanager.commands.RootCommands;
import com.lovejoy777.rroandlayersmanager.filepicker.FilePickerActivity;
import com.stericson.RootTools.RootTools;

import java.util.ArrayList;

/**
 * Created by lovejoy777 on 13/06/15.
 */
public class Delete extends Activity{

        static final String TAG = "Delete";
        final String startDirvendor = "/vendor/overlay";
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

                deletemultiplecommand();

            } else {
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, startDirvendor);
                i.putExtra("FilePickerMode","Uninstall Overlays");

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
                            // sb.append("\n");
                        }
                    }

                    String SZP = (sb.toString());
                    Intent iIntent = new Intent(this, Delete.class);
                    iIntent.putExtra("key1", SZP);
                    iIntent.putStringArrayListExtra("key2", paths);
                    startActivity(iIntent);

                    finish();
                }

            } else {
                // Get the File path from the Uri
                String SZP = (data.getData().toString());
                if (SZP.startsWith("file://")) {
                    SZP = SZP.substring(7);
                    Intent iIntent = new Intent(this, Delete.class);
                    iIntent.putExtra("key1", SZP);
                    startActivity(iIntent);

                    finish();
                }
            }
        }
    } // ends onActivity for result

    public void deletemultiplecommand () {

        ArrayList<String> paths;
        paths = getIntent().getStringArrayListExtra("key2");

        if (paths != null) {

            for (String path : paths) {
                if (path.startsWith("file://")) {
                    path = path.substring(7);

                    RootTools.remount("/system", "RW");
                    RootCommands.DeleteFileRoot(path);
                }
            }

            Toast.makeText(Delete.this, "deleted selected layers", Toast.LENGTH_LONG).show();
            finish();

        } else {

            Toast.makeText(Delete.this, "nothing to delete", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back2, R.anim.back1);
    }
}

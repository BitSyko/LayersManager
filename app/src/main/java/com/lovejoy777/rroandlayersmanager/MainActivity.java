package com.lovejoy777.rroandlayersmanager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends Activity
{

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!RootTools.isAccessGiven()) {

            final View coordinatorLayoutView = findViewById(R.id.main_content2);
            Snackbar.make(coordinatorLayoutView, "No root access available", Snackbar.LENGTH_LONG)
                    .setAction("Get Root", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=eu.chainfire.supersu")));
                        }
                    })
                    .show();
        }



        // switch and build.prop call for no launcher Icon.
        SharedPreferences prefs = this.getSharedPreferences("switch1", Context.MODE_PRIVATE);
        boolean switch1 = prefs.getBoolean("switch1", false);

        Process p1 = null;
        String noIcon = "";
        try {
            p1 = new ProcessBuilder("/system/bin/getprop", "ro.layers.noIcon").redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p1.getInputStream()));
            String line = "";
            if ((line = br.readLine()) != null) {
                noIcon = line;

                if (noIcon.length() >= 3 && switch1 == true) {

                    PackageManager p = getPackageManager();
                    ComponentName componentName = new ComponentName(this, com.lovejoy777.rroandlayersmanager.MainActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
                    p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

                    // Toast.makeText(MainActivity.this, noIcon, Toast.LENGTH_LONG).show();
                }

            } else {
                //Toast.makeText(MainActivity.this, "is not null", Toast.LENGTH_LONG).show();
                Toast.makeText(MainActivity.this, "Icon null", Toast.LENGTH_LONG).show();

            }
            p1.destroy();
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }

        Intent intent = new Intent(MainActivity.this,menu.class);
        startActivity(intent);

        finish();

    }
}

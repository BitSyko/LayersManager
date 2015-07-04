package com.lovejoy777.rroandlayersmanager;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.afollestad.materialcab.MaterialCab;
import com.lovejoy777.rroandlayersmanager.actions.Delete;
import com.lovejoy777.rroandlayersmanager.actions.Install;
import com.lovejoy777.rroandlayersmanager.actions.Restore;
import com.lovejoy777.rroandlayersmanager.activities.AboutActivity;
import com.lovejoy777.rroandlayersmanager.activities.DetailedTutorialActivity;
import com.lovejoy777.rroandlayersmanager.activities.Intro;
import com.lovejoy777.rroandlayersmanager.activities.SettingsActivity;
import com.lovejoy777.rroandlayersmanager.adapters.CardViewAdapter;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;
import com.lovejoy777.rroandlayersmanager.helper.CardViewContent;
import com.lovejoy777.rroandlayersmanager.helper.RecyclerItemClickListener;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    }
}

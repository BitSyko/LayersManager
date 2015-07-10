package com.lovejoy777.rroandlayersmanager;

import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;


import com.lovejoy777.rroandlayersmanager.activities.Intro;
import com.lovejoy777.rroandlayersmanager.fragments.UninstallFragment;
import com.lovejoy777.rroandlayersmanager.fragments.BackupRestoreFragment;
import com.lovejoy777.rroandlayersmanager.activities.AboutActivity;
import com.lovejoy777.rroandlayersmanager.activities.DetailedTutorialActivity;
import com.lovejoy777.rroandlayersmanager.fragments.PluginFragment;
import com.lovejoy777.rroandlayersmanager.activities.SettingsActivity;
import com.lovejoy777.rroandlayersmanager.adapters.CardViewAdapter;
import com.lovejoy777.rroandlayersmanager.helper.CardViewContent;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class menu extends AppCompatActivity
{

    public static final String ACTION_PICK_PLUGIN = "com.layers.plugins.PICK_OVERLAYS";
    static final String BUNDLE_EXTRAS_CATEGORY = "category";
    static final String BUNDLE_EXTRAS_PACKAGENAME = "packageName";
    private DrawerLayout mDrawerLayout;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);
        if (!RootTools.isAccessGiven()) {

            /*final View coordinatorLayoutView = findViewById(R.id.main_content2);
            Snackbar.make(coordinatorLayoutView, "No root access available", Snackbar.LENGTH_LONG)
                    .setAction("Get Root", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=eu.chainfire.supersu")));
                        }
                    })
                    .show(); */
        }

        loadToolbarNavDrawer();

        createImportantDirectories();

        changeFragment(1);
        getSupportActionBar().setElevation(0);

        LoadTutorial();
    }

    private void LoadTutorial() {

        // Get the app's shared preferences
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the value for the run counter
        int counter = app_preferences.getInt("counter", 0);

        if (counter < 1){

            Intent intent = new Intent(this,Intro.class);
            startActivity(intent);

        }
        // Increment the counter
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putInt("counter", ++counter);
        editor.commit();
    }

    private void loadToolbarNavDrawer() {
        //set Toolbar
        final android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //set NavigationDrawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }


    //navigationDrawerIcon Onclick
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Fragment currentFragment = menu.this.getFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof OverlayDetailActivity){
                    FragmentManager fm = getFragmentManager();
                    fm.popBackStack();
                    //changeFragment(1);
                }else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //set NavigationDrawerContent
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();
                        menuItem.setChecked(true);
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.anni1, R.anim.anni2).toBundle();
                        int id = menuItem.getItemId();
                        switch (id){
                            case R.id.nav_home:
                                changeFragment(1);
                                getSupportActionBar().setElevation(0);
                                break;
                            case R.id.nav_about:
                                Intent about = new Intent(menu.this, AboutActivity.class);
                                startActivity(about, bndlanimation);
                                break;
                            case R.id.nav_delete:
                                changeFragment(2);
                                getSupportActionBar().setElevation(0);
                                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu);
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                                break;
                            case R.id.nav_tutorial:
                                Intent tutorial = new Intent(menu.this, DetailedTutorialActivity.class);
                                startActivity(tutorial, bndlanimation);
                                break;
                            case R.id.nav_restore:
                                changeFragment(3);
                                getSupportActionBar().setElevation(0);
                                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu);
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                                break;
                            case R.id.nav_showcase:
                                boolean installed = appInstalledOrNot("com.lovejoy777.showcase");
                                if(installed) {
                                    //This intent will help you to launch if the package is already installed
                                    Intent intent = new Intent();
                                    intent.setComponent(new ComponentName("com.lovejoy777.showcase", "com.lovejoy777.showcase.MainActivity1"));
                                    startActivity(intent);
                                    break;
                                } else {
                                    Toast.makeText(menu.this, "Please install the layers showcase plugin", Toast.LENGTH_LONG).show();
                                    System.out.println("App is not currently installed on your phone");
                                }
                            case R.id.nav_settings:
                                Intent settings = new Intent(menu.this, SettingsActivity.class);
                                startActivity(settings, bndlanimation);
                                break;
                            case R.id.nav_playStore:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=Layers+Theme&c=apps&docType=1&sp=CAFiDgoMTGF5ZXJzIFRoZW1legIYAIoBAggB:S:ANO1ljK_ZAY")),bndlanimation);
                                break;
                        }
                        return false;
                    }
        });
    }

    private void changeFragment(int position) {
        Fragment fragment = null;
        Bundle args = new Bundle();
        FragmentManager fragmentManager = getFragmentManager();
        switch (position){
            case 1:
                fragment = new PluginFragment();
                break;
            case 2:
                fragment = new UninstallFragment();
                break;
            case 3:
                fragment = new BackupRestoreFragment();
                break;
        }

        fragment.setArguments(args);
        // Insert the fragment by replacing any existing fragment

        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

    }

    public void changeFragment2(String category, String package2){
        Fragment fragment = null;
        Bundle args = new Bundle();
        args.putString(BUNDLE_EXTRAS_CATEGORY, category);
        args.putString(BUNDLE_EXTRAS_PACKAGENAME, package2);
        fragment = new OverlayDetailActivity();

        fragment.setArguments(args);


        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .addToBackStack("test")
                .replace(R.id.fragment_container, fragment)
                .commit();
    }


    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }


    private void createImportantDirectories(){
        String sdOverlays = Environment.getExternalStorageDirectory() + "/Overlays";
        String sdcard = Environment.getExternalStorageDirectory() + "";

        RootTools.remount(sdcard, "RW");

        // CREATES /SDCARD/OVERLAYS
        File dir = new File(sdOverlays);
        if (!dir.exists() && !dir.isDirectory()) {
            CommandCapture command3 = new CommandCapture(0, "mkdir " + sdOverlays);
            try {
                RootTools.getShell(true).add(command3);
                while (!command3.isFinished()) {
                    Thread.sleep(1);
                }

            } catch (IOException | TimeoutException | InterruptedException | RootDeniedException e) {
                e.printStackTrace();
            }
        }

            String sdOverlays1 = Environment.getExternalStorageDirectory() + "/Overlays/Backup";
            // CREATES /SDCARD/OVERLAYS/BACKUP
            File dir1 = new File(sdOverlays1);
            if (!dir1.exists() && !dir1.isDirectory()) {
                CommandCapture command4 = new CommandCapture(0, "mkdir " + sdOverlays1);
                try {
                    RootTools.getShell(true).add(command4);
                    while (!command4.isFinished()) {
                        Thread.sleep(1);
                    }

                } catch (IOException | TimeoutException | InterruptedException | RootDeniedException e) {
                    e.printStackTrace();
                }
            }

        RootTools.remount("/system", "RW");
        String vendover = "/vendor/overlay";
        // CREATES /VENDOR/OVERLAY
        File dir2 = new File(vendover);
        if (!dir2.exists() && !dir2.isDirectory()) {
            CommandCapture command5 = new CommandCapture(0, "mkdir " + vendover);
            try {
                RootTools.getShell(true).add(command5);
                while (!command5.isFinished()) {
                    Thread.sleep(1);
                }

            } catch (IOException | TimeoutException | InterruptedException | RootDeniedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getFragmentManager();
        Fragment currentFragment = menu.this.getFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof OverlayDetailActivity) {
            fm.popBackStack();
            //changeFragment(1);
        }else {
            super.onBackPressed();
        }

    }
}

package com.lovejoy777.rroandlayersmanager.activities;

import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

import com.lovejoy777.rroandlayersmanager.R;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SettingsActivity extends PreferenceActivity implements

        SharedPreferences.OnSharedPreferenceChangeListener {

    private static boolean mRootAccess;

    public static boolean rootAccess() {
        return mRootAccess && RootTools.isAccessGiven();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName("myPrefs");
        addPreferencesFromResource(R.xml.settings);


    }

    private void killLauncherIcon() {

        Process p1 = null;
        String noIcon = "";
        try {
            p1 = new ProcessBuilder("/system/bin/getprop", "ro.layers.noIcon").redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p1.getInputStream()));
            String line = "";
            if ((line = br.readLine()) != null) {
                noIcon = line;

                if (noIcon.length() >= 3) {

                    PackageManager p = getPackageManager();
                    ComponentName componentName = new ComponentName(this, com.lovejoy777.rroandlayersmanager.MainActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
                    p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.launcherIconRemoved), Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(SettingsActivity.this, getResources().getString(R.string.romNeedsSupport), Toast.LENGTH_LONG).show();
                    SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = myPrefs.edit();
                    editor.putBoolean("switch1", false);
                    editor.apply();

                }

            } else {

                Toast.makeText(SettingsActivity.this, getResources().getString(R.string.noBuildPropCommit), Toast.LENGTH_LONG).show();
                SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = myPrefs.edit();
                editor.putBoolean("switch1", false);
                editor.apply();
            }
            p1.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void ReviveLauncherIcon() {

        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, com.lovejoy777.rroandlayersmanager.MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back2, R.anim.back1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences myPrefs = this.getSharedPreferences("myPrefs", MODE_PRIVATE);

        // Hiding Launcher Icon
        Boolean HideLauncherIcon = myPrefs.getBoolean("switch1", false);

        if (HideLauncherIcon) {
            killLauncherIcon();
        } else {
            ReviveLauncherIcon();
        }

        // Hiding Boots from nav drawer
        Boolean HideBoots = myPrefs.getBoolean("switch2", false);
        // Hiding Fonts from nav drawer
        Boolean HideFonts = myPrefs.getBoolean("switch3", false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

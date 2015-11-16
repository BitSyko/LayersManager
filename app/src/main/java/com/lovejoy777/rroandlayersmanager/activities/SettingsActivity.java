package com.lovejoy777.rroandlayersmanager.activities;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.commands.Commands;

public class SettingsActivity extends PreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    Boolean showLauncherShortcut = true;
    String settingsPackageName= "com.android.settings";
    String settingsLayersDrawableName = "ic_bitsyko_layers";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TO-DO: fix all the deprecated functions. Per google, we should utilize a Settings Fragment Activity?
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName("myPrefs");
        addPreferencesFromResource(R.xml.settings);
        Preference switch1 = findPreference("switch1");
        if (mShowLauncherShortcut()) {
            switch1.setEnabled(false);
            switch1.setSummary(R.string.LauncherIconSummaryDisabled);
        }
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

    public boolean mShowLauncherShortcut() {

        try {
            Resources res = getApplicationContext().getPackageManager().getResourcesForApplication(settingsPackageName);
            int drawableid = res.getIdentifier(settingsPackageName+":drawable/"+settingsLayersDrawableName, "drawable", settingsPackageName);
            if ( drawableid != 0 ) {
                showLauncherShortcut = false;
                Log.d("LayersManager", "checked settings for icon, true");
            } else {
                Log.d("LayersManager", "checked settings for icon, false");
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e("LayersManager", "System Settings apk not found!");
        }

        return showLauncherShortcut;
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
        Boolean HideLauncherIcon = myPrefs.getBoolean("switch1", false);

        if (HideLauncherIcon) {
            Commands.killLauncherIcon(this);
        } else {
            Commands.ReviveLauncherIcon(this);
        }
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

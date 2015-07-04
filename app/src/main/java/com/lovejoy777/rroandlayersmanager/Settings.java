package com.lovejoy777.rroandlayersmanager;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by lovejoy777 on 02/06/15.
 */
public class Settings  extends AppCompatActivity {

    CardView card1;
    private Switch mySwitch1;
    private static boolean mRootAccess;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);


        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);


        card1 = (CardView) findViewById(R.id.CardView_Settings1);

        // CARD 6
        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent menuactivity = new Intent(DetailedTutorialActivity.this, Intro.class);

                //  Bundle bndlanimation =
                //        ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.anni1, R.anim.anni2).toBundle();
                //  startActivity(menuactivity, bndlanimation);

            }
        }); // end card6

        // loadPrefs();

        mySwitch1 = (Switch) findViewById(R.id.switch1);
        //set the switch to ON
        mySwitch1.setChecked(false);
        //attach a listener to check for changes in state
        mySwitch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {


                if (isChecked) {

                    killLauncherIcon();

                } else {

                    ReviveLauncherIcon();

                    savePrefs("switch1", false);
                    //Toast.makeText(Settings.this, "Launcher icon restored", Toast.LENGTH_SHORT).show();
                }

            }
        });

        loadPrefs();


    }

    private void killLauncherIcon() {

        Process p1 = null;
        String noIcon = "";
        try {
            p1 = new ProcessBuilder("/system/bin/getprop", "ro.layers.noIcon").redirectErrorStream(true).start();
            BufferedReader br = new BufferedReader(new InputStreamReader(p1.getInputStream()));
            String line = "";
            if ((line=br.readLine()) != null){
                noIcon = line;

                if (noIcon.length() >= 3) {

                    PackageManager p = getPackageManager();
                    ComponentName componentName = new ComponentName(this, com.lovejoy777.rroandlayersmanager.Settings.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
                    p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                    Toast.makeText(Settings.this, "Launcher icon removed", Toast.LENGTH_SHORT).show();
                    savePrefs("switch1", true);

                } else{

                    Toast.makeText(Settings.this, "Your rom needs support for this function", Toast.LENGTH_LONG).show();
                }

            } else {

                Toast.makeText(Settings.this, "null build.prop commit", Toast.LENGTH_LONG).show();

            }
            p1.destroy();
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void ReviveLauncherIcon() {

        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, com.lovejoy777.rroandlayersmanager.MainActivity.class);
        p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

    }

    private void loadPrefs() {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean swvalue = sp.getBoolean("switch1", false);
        if (swvalue) {

            mySwitch1.setChecked(true);

        } else {

            mySwitch1.setChecked(false);
        }
    }

    public void savePrefs(String key, boolean value) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor edit = sp.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    public static boolean rootAccess() {
        return mRootAccess && RootTools.isAccessGiven();
    }




}
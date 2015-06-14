package com.lovejoy777.rroandlayersmanager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.stericson.RootTools.RootTools;

/**
 * Created by lovejoy777 on 02/06/15.
 */
public class Settings  extends Activity {

    private static boolean mRootAccess;

    public static void updatePreferences(Context context) {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);

        mRootAccess = p.getBoolean("enablerootaccess", true);

        rootAccess();
    }

    public static boolean rootAccess() {
        return mRootAccess && RootTools.isAccessGiven();
    }

}

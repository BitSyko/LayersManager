package com.lovejoy777.rroandlayersmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.stericson.RootTools.RootTools;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!RootTools.isAccessGiven()) {

            Toast.makeText(MainActivity.this, R.string.noRoot, Toast.LENGTH_LONG).show();
        }

        DeviceSingleton.getInstance();

        Intent intent = new Intent(MainActivity.this, menu.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();

    }
}

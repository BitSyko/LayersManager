package com.lovejoy777.rroandlayersmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DeviceSingleton.getInstance();

        Intent intent = new Intent(MainActivity.this, menu.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
        finish();

    }
}

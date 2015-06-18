package com.lovejoy777.rroandlayersmanager.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.lovejoy777.rroandlayersmanager.R;

/**
 * Created by lovejoy on 05/10/14.
 */
public class DetailedTutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
         // ENDS SWVALUE ELSE
        super.onCreate(savedInstanceState);
        setContentView(R.layout.instructions);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar5);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);



    } // ends onCreate

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back2, R.anim.back1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.back2, R.anim.back1);
            return true;
        }
        return false;
    }
}
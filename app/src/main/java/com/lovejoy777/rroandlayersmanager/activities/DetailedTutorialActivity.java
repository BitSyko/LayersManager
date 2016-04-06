package com.lovejoy777.rroandlayersmanager.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.menu;
import com.rubengees.introduction.IntroductionActivity;
import com.rubengees.introduction.IntroductionBuilder;
import com.rubengees.introduction.entity.Option;

public class DetailedTutorialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // ENDS SWVALUE ELSE
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        // Handle Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar5);
        toolbar.setTitle(R.string.instructionstitle);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);


        CardView card1 = (CardView) findViewById(R.id.CardView_Instructions6);

        // CARD 6
        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.loadTutorial(DetailedTutorialActivity.this);
            }
        }); // end card6

    } // ends onCreate

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back2, R.anim.back1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IntroductionBuilder.INTRODUCTION_REQUEST_CODE &&
                resultCode == RESULT_OK) {

            for (Option option : data.<Option>getParcelableArrayListExtra(IntroductionActivity.
                    OPTION_RESULT)) {

                if (option.getPosition()==5 && option.isActivated()){
                    SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    myprefs.edit().putBoolean("switch1",true).commit();
                    Commands.killLauncherIcon(this);
                }
                if (option.getPosition()==6 && option.isActivated()){
                    SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    myprefs.edit().putBoolean("disableNotInstalledApps",true).commit();
                }
            }
        }

    }
}
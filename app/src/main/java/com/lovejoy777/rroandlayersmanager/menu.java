package com.lovejoy777.rroandlayersmanager;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;
import com.bitsyko.liblayers.Layer;
import com.lovejoy777.rroandlayersmanager.activities.*;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.fragments.*;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.rubengees.introduction.IntroductionActivity;
import com.rubengees.introduction.IntroductionBuilder;
import com.rubengees.introduction.entity.Option;
import com.rubengees.introduction.entity.Slide;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class menu extends AppCompatActivity {

    @Bind(R.id.toolbar_fragmentContainer) Toolbar toolbar;
    @Bind(R.id.drawerLayout_fragmentContainer) DrawerLayout drawerLayout;
    @Bind(R.id.navigationView_menu) NavigationView navigationView;
    private int sdkVersion;
    private boolean omsCompatible;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_container);

        ButterKnife.bind(this);
        loadToolbarNavDrawer();

        if (!Utils.isRootAvailable()) {
            Toast.makeText(this, getString(R.string.menu_toast_noRoot), Toast.LENGTH_LONG).show();
        } else {
            createImportantDirectories();
        }

        //GET ANDROID VERSION
        sdkVersion= Build.VERSION.SDK_INT;

        //Is oms compatible
        omsCompatible = false;

        AlertDialog.Builder installdialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = getLayoutInflater();
        View dontShowAgainLayout = inflater.inflate(R.layout.dialog_donotshowagain, null);
        final CheckBox dontShowAgain = ButterKnife.findById(dontShowAgainLayout, R.id.cb_dontShowAgainDialog_dontShowAgain);

        installdialog.setView(dontShowAgainLayout);
        installdialog.setCancelable(false);
        installdialog.setTitle(R.string.SubstratumSwitch_NewEra);
        if (sdkVersion<23){
            installdialog.setMessage(getString(R.string.SubstratumSwitch_DescriptionNotSupported_Part1)+Build.VERSION.RELEASE+getString(R.string.SubstratumSwitch_DescriptionNotSupported_Part2));
            installdialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (dontShowAgain.isChecked()) {
                        SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = myprefs.edit();
                        editor.putString("SubstratumDialog", "checked");
                        editor.apply();
                    }
                    Boolean tutorialShown = PreferenceManager.getDefaultSharedPreferences(menu.this).getBoolean("tutorialShown", false);
                    if (!tutorialShown) {
                        loadTutorial(menu.this);
                    }else{
                        changeFragment(1);
                    }
                }
            });
        }else{
            installdialog.setMessage(R.string.SubstratumSwitch_DescriptionSupported);
            installdialog.setPositiveButton("Yes please", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (dontShowAgain.isChecked()) {
                        SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = myprefs.edit();
                        editor.putString("SubstratumDialog", "checked");
                        editor.apply();
                    }
                    loadSubstarumTutorial(menu.this);
                    //start async task to install the Overlays
                    //InstallAsyncOverlays();
                }
            });
        }


        installdialog.setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (dontShowAgain.isChecked()) {
                    SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = myprefs.edit();
                    editor.putString("SubstratumDialog", "checked");
                    editor.apply();
                }
                Boolean tutorialShown = PreferenceManager.getDefaultSharedPreferences(menu.this).getBoolean("tutorialShown", false);
                if (!tutorialShown) {
                    loadTutorial(menu.this);
                }else{
                    changeFragment(1);
                }
            }
        });;
        SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String skipMessage = myprefs.getString("SubstratumDialog", "unchecked");
        if (!skipMessage.equals("checked")) {
            installdialog.show();
        } else {
            Boolean tutorialShown = PreferenceManager.getDefaultSharedPreferences(menu.this).getBoolean("tutorialShown", false);
            if (!tutorialShown) {
                loadTutorial(menu.this);
            }else{
                changeFragment(1);
            }
        }
    }

    public static void loadTutorial(final Activity context) {
        new IntroductionBuilder(context).withSlides(generateSlides()).introduceMyself();
    }

    public static List<Slide> generateSlides() {
        List<Slide> slides = new ArrayList<>();

        slides.add(new Slide().withTitle(R.string.Slide1_Heading).withDescription(R.string.Slide1_Text).
                withColorResource(R.color.tutorial_background_1).withImage(R.drawable.layersmanager));
        slides.add(new Slide().withTitle(R.string.Slide2_Heading).withDescription(R.string.Slide2_Text)
                .withColorResource(R.color.tutorial_background_2).withImage(R.drawable.intro_2));
        slides.add(new Slide().withTitle(R.string.Slide3_Heading).withDescription(R.string.Slide3_Text)
                .withColorResource(R.color.tutorial_background_3).withImage(R.drawable.intro_3));
        slides.add(new Slide().withTitle(R.string.Slide4_Heading).withDescription(R.string.Slide4_Text)
                .withColorResource(R.color.tutorial_background_4).withImage(R.drawable.intro_4));
        slides.add(new Slide().withTitle(R.string.Slide5_Heading).withDescription(R.string.Slide5_Text)
                .withColorResource(R.color.tutorial_background_5).withImage(R.drawable.intro_5));
        slides.add(new Slide().withTitle(R.string.Slide6_Heading).withOption(new Option(R.string.settings_hidelauncher_title))
                .withColorResource(R.color.tutorial_background_6).withImage(R.drawable.layersmanager_crossed));
        slides.add(new Slide().withTitle(R.string.Slide7_Heading).withOption(new Option(R.string.settings_hideoverlays_title))
                .withColorResource(R.color.tutorial_background_6).withImage(R.drawable.intro_7));
        return slides;
    }

    public void loadSubstarumTutorial(final Activity context) {
        new IntroductionBuilder(context).withSlides(generateSlidesSubstartum()).introduceMyself();
    }

    public List<Slide> generateSlidesSubstartum() {
        List<Slide> slides = new ArrayList<>();

        slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide1_Title).withDescription(R.string.SubstratumSwitch_Slide1_Description).
                withColorResource(R.color.slide_1).withImage(R.drawable.appintro0));
        if(omsCompatible){
            slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide2_Title).withDescription(R.string.SubstratumSwitch_Slide2_Description)
                    .withColorResource(R.color.slide_2).withImage(R.drawable.appintro6));
            slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide_Overlays_Title).withDescription(R.string.SubstratumSwitch_Slide_Overlays_Description_OMS)
                    .withColorResource(R.color.slide_3).withImage(R.drawable.appintro1));
            slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide_Fonts_Title).withDescription(R.string.SubstratumSwitch_Slide_Fonts_Description_OMS)
                    .withColorResource(R.color.slide_4).withImage(R.drawable.appintro2));
            slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide_Bootanimations_Title).withDescription(R.string.SubstratumSwitch_Slide_Bootanimations_Description_OMS)
                    .withColorResource(R.color.slide_5).withImage(R.drawable.appintro3));
            slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide_Sounds_Title).withDescription(R.string.SubstratumSwitch_Slide_Sounds_Description_OMS)
                    .withColorResource(R.color.slide_6).withImage(R.drawable.appintro4));
        }else{
            slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide3_Title).withDescription(R.string.SubstratumSwitch_Slide3_Description)
                    .withColorResource(R.color.slide_6).withImage(R.drawable.appintro5));
            slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide4_Title).withDescription(R.string.SubstratumSwitch_Slide4_Description)
                    .withColorResource(R.color.slide_2).withImage(R.drawable.appintro6));
            slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide_Overlays_Title).withDescription(R.string.SubstratumSwitch_Slide_Overlays_Description_NotOMS)
                    .withColorResource(R.color.slide_3).withImage(R.drawable.appintro1));
            slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide_Fonts_Title).withDescription(R.string.SubstratumSwitch_Slide_Fonts_Description_NotOMS)
                    .withColorResource(R.color.slide_4).withImage(R.drawable.appintro2));
            slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide_Bootanimations_Title).withDescription(R.string.SubstratumSwitch_Slide_Bootanimations_Description_NotOMS)
                    .withColorResource(R.color.slide_5).withImage(R.drawable.appintro3));
            slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide_Sounds_Title).withDescription(R.string.SubstratumSwitch_Slide_Sounds_Description_NotOMS)
                    .withColorResource(R.color.slide_6).withImage(R.drawable.appintro4));
        }
        slides.add(new Slide().withTitle(R.string.SubstratumSwitch_Slide5_Title).withOption(new Option(getString(R.string.SubstratumSwitch_Slide5_Description)))
                .withColorResource(R.color.slide_1).withImage(R.drawable.appintro6));
        return slides;
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IntroductionBuilder.INTRODUCTION_REQUEST_CODE &&
                resultCode == RESULT_OK) {

            changeFragment(1);
            for (Option option : data.<Option>getParcelableArrayListExtra(IntroductionActivity.
                    OPTION_RESULT)) {
                if (option.getPosition() == 5) {
                    //HIDE LAUNCHER ICON CHECKBOX
                    if (option.isActivated()) {
                        SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                        myprefs.edit().putBoolean("switch1", true).commit();
                    }
                    //TUTORIAL SHOWN
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("tutorialShown", true).commit();
                }

                if (option.getPosition() == 6) {
                    //HIDE OVERLAYS CHECKBOX OR GET SUBSTRATUM CHECKBOX
                    if (option.getTitle().equals("Do not install Substratum!")) {
                        //NOT GET SUBSTRATUM CHECKBOX
                        if (!option.isActivated()) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/apps/testing/projekt.substratum"));
                            startActivity(browserIntent);
                        }
                    } else {
                        //HIDE OVERLAYS CHECKBOX
                        if (option.isActivated()) {
                            SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                            myprefs.edit().putBoolean("disableNotInstalledApps", true).commit();
                        }
                        //TUTORIAL SHOWN
                        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("tutorialShown", true).commit();

                    }
                }

                if (option.getPosition() == 7) {
                    //NOT GET SUBSTRATUM CHECKBOX
                    if (!option.isActivated()) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/apps/testing/projekt.substratum"));
                        startActivity(browserIntent);
                    }
                }
            }
        }else{
            if (resultCode == RESULT_CANCELED){
                loadTutorial(this);
            }
        }

    }

    private void loadToolbarNavDrawer() {
        //set toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_menu_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //set NavigationDrawer
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }


    //navigationDrawerIcon Onclick
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
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
                        drawerLayout.closeDrawers();
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.anni1, R.anim.anni2).toBundle();
                        int id = menuItem.getItemId();
                        switch (id) {
                            //Home
                            case R.id.nav_home:
                                menuItem.setChecked(true);
                                changeFragment(1);
                                break;
                            //Uninstall
                            case R.id.nav_delete:
                                menuItem.setChecked(true);
                                changeFragment(2);
                                break;
                            //Backup & Restore
                            case R.id.nav_restore:
                                menuItem.setChecked(true);
                                changeFragment(3);
                                break;
                            //Showcase
                            case R.id.nav_showcase:
                                boolean installed = Commands.appInstalledOrNot(menu.this,"com.lovejoy777.showcase");
                                if (installed) {
                                    //This intent will help you to launch if the package is already installed
                                    Intent intent = new Intent();
                                    intent.setComponent(new ComponentName("com.lovejoy777.showcase", "com.lovejoy777.showcase.MainActivity1"));
                                    startActivity(intent);
                                    break;
                                } else {
                                    Toast.makeText(menu.this, "Please install the layers showcase plugin", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.lovejoy777.showcase")), bndlanimation);
                                    break;
                                }
                            //PlayStore
                            case R.id.nav_playStore:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=Layers+Theme&c=apps&docType=1&sp=CAFiDgoMTGF5ZXJzIFRoZW1legIYAIoBAggB:S:ANO1ljK_ZAY")), bndlanimation);
                                break;
                            //Tutorial
                            case R.id.nav_tutorial:
                                Intent tutorial = new Intent(menu.this, TutorialActivity.class);
                                startActivity(tutorial, bndlanimation);
                                break;
                            //Team
                            case R.id.nav_the_team:
                                Intent about = new Intent(menu.this, AboutActivity.class);
                                startActivity(about, bndlanimation);
                                break;
                            //About
                            case R.id.nav_about:
                                new LibsBuilder()
                                        //Pass the fields of your application to the lib so it can find all external lib information
                                        .withFields(R.string.class.getFields())
                                        //provide a style (optional) (LIGHT, DARK, LIGHT_DARK_TOOLBAR)
                                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                                        //start the activity
                                        .withAboutIconShown(true)
                                        .withAboutVersionShown(true)
                                        .withAboutDescription("<b>Layers - the best way to theme your device!</b>")
                                        .withActivityTitle("About")
                                        .start(menu.this);
                                break;
                            //Settings
                            case R.id.nav_settings:
                                Intent settings = new Intent(menu.this, SettingsActivity.class);
                                startActivity(settings, bndlanimation);
                                break;
                            case R.id.nav_substratum:
                                if(sdkVersion>22){
                                    loadSubstarumTutorial(menu.this);
                                }else {
                                    AlertDialog.Builder installdialog = new AlertDialog.Builder(menu.this);
                                    installdialog.setCancelable(false);
                                    installdialog.setTitle(R.string.SubstratumSwitch_Dialog_NotSupported_Title);
                                    installdialog.setMessage(getString(R.string.SubstratumSwitch_Dialog_NotSupported_Description_Part1)+Build.VERSION.RELEASE+getString(R.string.SubstratumSwitch_Dialog_NotSupported_Description_Part2));
                                    installdialog.setPositiveButton("Oh...", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                                    installdialog.setNeutralButton("Show nevertheless", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            loadSubstarumTutorial(menu.this);
                                        }
                                    });
                                    installdialog.show();
                                }

                        }
                        return false;
                    }
                });
    }

    public void changeFragment(int position) {
        Fragment fragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        switch (position) {
            case 1:
                fragment = new PluginList();
                break;
            case 2:
                fragment = new Uninstall();
                break;
            case 3:
                fragment = new BackupRestore();
                break;
        }


        fragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();


    }

    public void changeFragment2(Layer layer) {
        Bundle args = new Bundle();
        args.putString("PackageName", layer.getPackageName());

        Intent intent = new Intent(this, OverlayDetailActivity.class);

        intent.putExtra("PackageName", layer.getPackageName());

        startActivity(intent);

    }





    private void createImportantDirectories() {


        String sdOverlays1 = Environment.getExternalStorageDirectory() + "/Overlays/Backup";
        // CREATES /SDCARD/OVERLAYS/BACKUP
        File dir1 = new File(sdOverlays1);

        dir1.mkdirs();

        Utils.remount("rw");
        String vendover = DeviceSingleton.getInstance().getOverlayFolder();
        // CREATES /VENDOR/OVERLAY
        File dir2 = new File(vendover);
        if (!dir2.exists()) {
            Utils.createFolder(dir2);
        }
        Utils.remount("ro");
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = menu.this.getFragmentManager().findFragmentById(R.id.fragment_container);

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
            return;
        }

        if (currentFragment instanceof BackButtonListener && !((BackButtonListener) currentFragment).onBackButton()) {
            return;
        }

        FragmentManager fm = getFragmentManager();

        //First commit is omitted
        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
            return;
        }


        super.onBackPressed();
    }
}

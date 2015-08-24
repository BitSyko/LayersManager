package com.lovejoy777.rroandlayersmanager;

import android.app.ActivityOptions;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.bitsyko.liblayers.Layer;
import com.lovejoy777.rroandlayersmanager.activities.*;
import com.lovejoy777.rroandlayersmanager.fragments.*;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class menu extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_container);

        if (!RootTools.isAccessGiven()) {
        }

        loadToolbarNavDrawer();

        createImportantDirectories();

        changeFragment(1, 0);

        loadTutorial();

    }
 private ViewPagerAdapter adapter;

    private void setupViewPager(ViewPager viewPager,int mode) {
        viewPager.removeAllViews();
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        if (mode==0){
            adapter.addFrag(new PluginFragment(),"Overlays");
            adapter.addFrag(new PluginFragment(), "Icon Overlays");
        }else {
            System.out.println("TEST");
            adapter.removeAllFrags();
            adapter.notifyDataSetChanged();

            UninstallFragment uninstallOverlays = new UninstallFragment();
            UninstallFragment uninstallOverlays2 = new UninstallFragment();
            Bundle args1 = new Bundle();
            Bundle args2 = new Bundle();
            args1.putInt("Mode", 0);
            uninstallOverlays.setArguments(args1);
            adapter.addFrag(uninstallOverlays, "Overlays");
            args2.putInt("Mode", 1);
            uninstallOverlays2.setArguments(args2);
            adapter.addFrag(uninstallOverlays2,"Icon Overlays");
            //adapter.addFrag(new UninstallFragment(), "Icon Overlays");

        }


        viewPager.setAdapter(adapter);
    }


    class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final List<android.support.v4.app.Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        public ViewPagerAdapter(android.support.v4.app.FragmentManager manager) {
            super(manager);
        }
        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            return mFragmentList.get(position);
        }
        @Override
        public int getCount() {
            return mFragmentList.size();
        }
        public void addFrag(android.support.v4.app.Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        public void removeAllFrags() {
            mFragmentList.clear();
            mFragmentTitleList.clear();
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }



    private void loadTutorial() {

        Boolean tutorialShown = PreferenceManager.getDefaultSharedPreferences(menu.this).getBoolean("tutorialShown", false);

        if (!tutorialShown) {
            Intent intent = new Intent(this, IntroActivity.class);
            startActivityForResult(intent, 1);
            PreferenceManager.getDefaultSharedPreferences(menu.this).edit().putBoolean("tutorialShown", true).commit();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            changeFragment(1, 0);
            this.finish();
        }
    }

    private void loadToolbarNavDrawer() {
        //set Toolbar
        final android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
                android.support.v4.app.Fragment currentFragment = menu.this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof InstallFragment) {
                    changeFragment(1, 1);
                } else {
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
                        switch (id) {
                            case R.id.nav_home:
                                changeFragment(1, 0);
                                break;
                            case R.id.nav_about:
                                Intent about = new Intent(menu.this, AboutActivity.class);
                                startActivity(about, bndlanimation);
                                break;
                            case R.id.nav_delete:
                                changeFragment(2, 0);
                                break;
                            case R.id.nav_tutorial:
                                Intent tutorial = new Intent(menu.this, DetailedTutorialActivity.class);
                                startActivity(tutorial, bndlanimation);
                                break;
                            case R.id.nav_restore:
                                changeFragment(3, 0);
                                getSupportActionBar().setElevation(0);
                                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu);
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                                break;
                            case R.id.nav_showcase:
                                boolean installed = appInstalledOrNot("com.lovejoy777.showcase");
                                if (installed) {
                                    //This intent will help you to launch if the package is already installed
                                    Intent intent = new Intent();
                                    intent.setComponent(new ComponentName("com.lovejoy777.showcase", "com.lovejoy777.showcase.MainActivity1"));
                                    startActivity(intent);
                                    break;
                                } else {
                                    Toast.makeText(menu.this, "Please install the layers showcase plugin", Toast.LENGTH_LONG).show();
                                    System.out.println("App is currently not installed on your phone");
                                    break;
                                }
                            case R.id.nav_settings:
                                Intent settings = new Intent(menu.this, SettingsActivity.class);
                                startActivity(settings, bndlanimation);
                                break;
                            case R.id.nav_playStore:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=Layers+Theme&c=apps&docType=1&sp=CAFiDgoMTGF5ZXJzIFRoZW1legIYAIoBAggB:S:ANO1ljK_ZAY")), bndlanimation);
                                break;
                        }
                        return false;
                    }
                });
    }

    public void changeFragment(int position, int mode) {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        android.support.v4.app.Fragment fragment = null;
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        ViewPager viewPager = (ViewPager) findViewById(R.id.tabanim_viewpager);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        switch (position) {
            case 1:
                setupViewPager(viewPager,0);
                tabLayout.setupWithViewPager(viewPager);
                break;
            case 2:
                setupViewPager(viewPager,1);
                tabLayout.setupWithViewPager(viewPager);
                //fragment = new UninstallFragment();
                break;
            case 3:
                fragment = new BackupRestoreFragment();
                break;
            case 4:
                fragment = new InstallFragment();
                break;
        }


        if (position >2){
            fragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment,"TAG")
                    .addToBackStack(null)
                    .commit();
        } else{
            android.support.v4.app.Fragment test = getSupportFragmentManager().findFragmentByTag("TAG");
            if (test!=null){
                fragmentManager
                        .beginTransaction()
                        .remove(getSupportFragmentManager().findFragmentByTag("TAG"))
                        .commit();
            }
        }
    }

    public void changeFragment2(Layer layer) {
        Bundle args = new Bundle();
        args.putString("PackageName", layer.getPackageName());

        Intent intent = new Intent(this, OverlayDetailActivity.class);

        intent.putExtra("PackageName", layer.getPackageName());

        startActivity(intent);

    }


    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }


    private void createImportantDirectories() {

        String sdOverlays1 = Environment.getExternalStorageDirectory() + "/Overlays/Backup";
        // CREATES /SDCARD/OVERLAYS/BACKUP
        File dir1 = new File(sdOverlays1);

        dir1.mkdirs();

        RootTools.remount("/system", "RW");
        String vendover = "/system/vendor/overlay";
        // CREATES /VENDOR/OVERLAY
        File dir2 = new File(vendover);
        if (!dir2.exists()) {
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

        RootTools.remount("/system", "RO");

    }

    @Override
    public void onBackPressed() {
        android.support.v4.app.Fragment currentFragment = menu.this.getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        if (currentFragment instanceof InstallFragment) {
            changeFragment(1, 1);
        }

        //if (currentFragment instanceof BackButtonListener && !((BackButtonListener) currentFragment).onBackButton()) {
        //    return;
        //}

        FragmentManager fm = getFragmentManager();

        //First commit is omitted
        //if (fm.getBackStackEntryCount() > 1) {
        //    fm.popBackStack();
        //    return;
        //}


        super.onBackPressed();
    }

}

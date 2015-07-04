package com.lovejoy777.rroandlayersmanager.actions;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.afollestad.materialcab.MaterialCab;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.activities.AboutActivity;
import com.lovejoy777.rroandlayersmanager.activities.DetailedTutorialActivity;
import com.lovejoy777.rroandlayersmanager.activities.SettingsActivity;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;
import com.lovejoy777.rroandlayersmanager.menu;
import com.stericson.RootTools.RootTools;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by lovejoy777 on 13/06/15.
 */
public class Delete extends AppCompatActivity implements MaterialCab.Callback {

    private ArrayList<String> Files = new ArrayList<String>();
    FloatingActionButton fab2;
    int atleastOneIsClicked = 0;
    private RecyclerView mRecyclerView;
    private CardViewAdapter3 mAdapter;
    private MaterialCab mCab = null;
    List<Integer> InstallOverlayList = new ArrayList<Integer>();
    private DrawerLayout mDrawerLayout;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_delete);

            // Handle Toolbar
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_action_menu);
            setSupportActionBar(toolbar);
            mRecyclerView = (RecyclerView) findViewById(R.id.cardList);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());

            fab2 = (android.support.design.widget.FloatingActionButton) findViewById(R.id.fab6);
            fab2.setVisibility(View.INVISIBLE);
            fab2.animate().translationY(218).setInterpolator(new AccelerateInterpolator(2)).start();
            fab2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println(InstallOverlayList);
                    fab2.animate().translationY(fab2.getHeight() + 48).setInterpolator(new AccelerateInterpolator(2)).start();
                    new DeleteOverlays().execute();
                }
            });

            //set NavigationDrawer
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            if (navigationView != null) {
                setupDrawerContent(navigationView);
            }

            Commands command= new Commands();
            Files = command.loadFiles("/system/vendor/overlay");

            ImageView noOverlays = (ImageView) findViewById(R.id.imageView);
            TextView noOverlaysText = (TextView) findViewById(R.id.textView7);
            if (Files.isEmpty()){
                noOverlays.setVisibility(View.VISIBLE);
                noOverlaysText.setVisibility(View.VISIBLE);
            }
            mAdapter = new CardViewAdapter3(Files, R.layout.adapter_listlayout, Delete.this);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(mAdapter);

            for (int i=0; i<Files.size();i++){
                InstallOverlayList.add(0);
            }
        }


    //set NavigationDrawerContent
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.anni1, R.anim.anni2).toBundle();
                        int id = menuItem.getItemId();
                        switch (id){
                            case R.id.nav_home:
                                Intent menu = new Intent(Delete.this, com.lovejoy777.rroandlayersmanager.menu.class);

                                startActivity(menu, bndlanimation);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_about:
                                Intent about = new Intent(Delete.this, AboutActivity.class);

                                startActivity(about, bndlanimation);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_tutorial:
                                Intent tutorial = new Intent(Delete.this, DetailedTutorialActivity.class);
                                startActivity(tutorial, bndlanimation);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_restore:
                                Intent restore = new Intent(Delete.this, Restore.class);
                                startActivity(restore, bndlanimation);
                                mDrawerLayout.closeDrawers();
                                break;



                            case R.id.nav_showcase:

                                boolean installed = appInstalledOrNot("com.lovejoy777.showcase");
                                if(installed) {
                                    //This intent will help you to launch if the package is already installed
                                    Intent showcase = getPackageManager().getLaunchIntentForPackage("com.lovejoy777.showcase");
                                    startActivity(showcase, bndlanimation);
                                    mDrawerLayout.closeDrawers();

                                    break;
                                } else {
                                    Toast.makeText(Delete.this, "Please install the layers showcase plugin", Toast.LENGTH_LONG).show();
                                    System.out.println("App is not currently installed on your phone");
                                }
                            case R.id.nav_settings:
                                Intent settings = new Intent(Delete.this, SettingsActivity.class);
                                startActivity(settings, bndlanimation);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_playStore:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=Layers+Theme&c=apps&docType=1&sp=CAFiDgoMTGF5ZXJzIFRoZW1legIYAIoBAggB:S:ANO1ljK_ZAY")),bndlanimation);
                                break;
                        }
                        return false;
                    }
                });
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }


    //Adapter
    private class CardViewAdapter3 extends RecyclerView.Adapter<CardViewAdapter3.ViewHolder>{

        private ArrayList<String> themes;
        private int rowLayout;
        private Context mContext;

        public CardViewAdapter3(ArrayList<String> themes, int rowLayout, Context context) {
            this.themes = themes;
            this.rowLayout = rowLayout;
            this.mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int i) {

            viewHolder.themeName.setText(themes.get(i).replace(".apk","").replace("_"," "));
            viewHolder.themeName.setTag(themes.get(i));
            viewHolder.themeName.setId(i);
            if (InstallOverlayList.get(i)==1){
                viewHolder.themeName.setChecked(true);
            }else{
                viewHolder.themeName.setChecked(false);
            }
            viewHolder.themeName.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    System.out.println(v.getTag());
                    if (cb.isChecked()) {
                        InstallOverlayList.set(i, 1);
                        atleastOneIsClicked = atleastOneIsClicked + 1;

                    } else {
                        InstallOverlayList.set(i, 0);
                        atleastOneIsClicked = atleastOneIsClicked - 1;
                    }
                    if (mCab == null)
                        mCab = new MaterialCab(Delete.this, R.id.cab_stub)
                                .reset()
                                .setCloseDrawableRes(R.drawable.ic_action_check)
                                .setMenu(R.menu.overflow)
                                .start(Delete.this);
                    else if (!mCab.isActive())
                        mCab
                                .reset().start(Delete.this)
                                .setCloseDrawableRes(R.drawable.ic_action_check)
                                .setMenu(R.menu.overflow);
                    mCab.setTitle(atleastOneIsClicked + " Overlays selected");
                    if (atleastOneIsClicked > 0) {
                        fab2.setVisibility(View.VISIBLE);
                        fab2.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                    } else {
                        mCab.finish();
                        mCab = null;
                        fab2.animate().translationY(fab2.getHeight() + 48).setInterpolator(new AccelerateInterpolator(2)).start();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return themes == null ? 0 : themes.size();
        }
        public  class ViewHolder extends RecyclerView.ViewHolder {
            public CheckBox themeName;


            public ViewHolder(View itemView) {
                super(itemView);
                themeName = (CheckBox) itemView.findViewById(R.id.deletecheckbox);
            }
        }
    }




    //Delete Overlays
    private class DeleteOverlays extends AsyncTask<Void,Void,Void> {
        ProgressDialog progressDelete;

        protected void onPreExecute() {

            progressDelete = ProgressDialog.show(Delete.this, "Uninstall Overlays",
                    "Uninstalling...", true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            RootTools.remount("/system", "RW");
            for (int i=0; i< Files.size();i++){
                if (InstallOverlayList.get(i)==1){
                    RootCommands.DeleteFileRoot("system/vendor/overlay/"+Files.get(i));
                }
            }
            return null;

        }

        protected void onPostExecute(Void result) {
            progressDelete.dismiss();
            RootTools.remount("/system", "RO");

            CoordinatorLayout coordinatorLayoutView = (CoordinatorLayout) findViewById(R.id.main_content3);
            Snackbar.make(coordinatorLayoutView, "Uninstalled selected Overlays", Snackbar.LENGTH_LONG)
                    .setAction("Reboot", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder progressDialogReboot = new AlertDialog.Builder(Delete.this);
                            progressDialogReboot.setTitle("Reboot");
                            progressDialogReboot.setMessage("Perform a soft reboot?");
                            progressDialogReboot.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                //when Cancel Button is clicked
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            progressDialogReboot.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                //when Cancel Button is clicked
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        Process proc = Runtime.getRuntime()
                                                .exec(new String[]{"su", "-c", "busybox killall system_server"});
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    dialog.dismiss();
                                }
                            });
                            progressDialogReboot.show();
                        }
                    })
                    .show();

            LoadAndSet();

            ImageView noOverlays = (ImageView) findViewById(R.id.imageView);
            TextView noOverlaysText = (TextView) findViewById(R.id.textView7);
            if (Files.isEmpty()){
                noOverlays.setVisibility(View.VISIBLE);
                noOverlaysText.setVisibility(View.VISIBLE);
            }
            mCab.finish();
            ActivityCompat.invalidateOptionsMenu(Delete.this);

        }
    }

    public  void LoadAndSet(){
        Files.clear();
        Commands command= new Commands();
        Files = command.loadFiles("/system/vendor/overlay");
        int t = InstallOverlayList.size();
        InstallOverlayList.clear();
        for (int i =0; i< t;i++){
            InstallOverlayList.add(0);
        }
        atleastOneIsClicked =0;
        mAdapter = new CardViewAdapter3(Files, R.layout.adapter_listlayout, Delete.this);
        mRecyclerView.setAdapter(mAdapter);
    }



    //Check and Uncheck all Checkboxes
    private void checkAll(){
        InstallOverlayList.clear();
        for (int i =0; i< Files.size();i++){
            InstallOverlayList.add(1);
        }
        atleastOneIsClicked = InstallOverlayList.size();
        System.out.println(atleastOneIsClicked);
        mAdapter.notifyDataSetChanged();
        fab2.setVisibility(View.VISIBLE);
        fab2.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
        if (mCab == null)
            mCab = new MaterialCab(Delete.this, R.id.cab_stub)
                    .reset()
                    .setCloseDrawableRes(R.drawable.ic_action_check)
                    .setMenu(R.menu.overflow)
                    .start(Delete.this);
        else if (!mCab.isActive())
            mCab
                    .reset().start(Delete.this)
                    .setCloseDrawableRes(R.drawable.ic_action_check)
                    .setMenu(R.menu.overflow);

        mCab.setTitle(atleastOneIsClicked + " Overlays selected");
    }

    private void UncheckAll(){
        InstallOverlayList.clear();
        for (int i =0; i< Files.size();i++){
            InstallOverlayList.add(0);
        }
        atleastOneIsClicked = 0;
        mAdapter.notifyDataSetChanged();
        fab2.setVisibility(View.INVISIBLE);
        fab2.animate().translationY(fab2.getHeight() + 48).setInterpolator(new AccelerateInterpolator(2)).start();
    }




    //CAB methods
    @Override
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        return true;
    }
    @Override
    public boolean onCabItemClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.menu_selectall:
                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);
                checkAll();

        }
        return true;
    }
    @Override
    public boolean onCabFinished(MaterialCab materialCab) {
        UncheckAll();
        return true;
    }


    //Overflow Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!Files.isEmpty()) {
            getMenuInflater().inflate(R.menu.overflow, menu);
        }
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_selectall:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                checkAll();
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
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
}
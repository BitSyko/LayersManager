package com.lovejoy777.rroandlayersmanager.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;
import com.lovejoy777.rroandlayersmanager.menu;
import com.lovejoy777.rroandlayersmanager.filepicker.FilePickerActivity;
import com.stericson.RootTools.RootTools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by lovejoy777 on 10/06/15.
 */
public class Install extends Fragment {

    private ArrayList<String> Files = new ArrayList<>();
    private ArrayList<String> Directories = new ArrayList<>();
    FloatingActionButton fab2;
    int atleastOneIsClicked = 0;
    private RecyclerView mRecyclerView;
    private CardViewAdapter3 mAdapter;
    private MaterialCab mCab = null;
    List<Integer> InstallOverlayList = new ArrayList<Integer>();
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout cordLayout = null;
    String currentDir= null;
    String BaseDir=null;
    ArrayList<String> Filedirectories = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        BaseDir = Environment.getExternalStorageDirectory()+"";
        currentDir =null;
        Filedirectories.add("/Overlays");
        FragmentActivity faActivity  = (FragmentActivity)    super.getActivity();
        cordLayout = (CoordinatorLayout)    inflater.inflate(R.layout.fragment_delete, container, false);

        setHasOptionsMenu(true);

        loadToolbarRecylcerViewFab();

        new LoadAndSet().execute();

        return cordLayout;
    }


    private class LoadAndSet extends AsyncTask<String,String,Void> {
        ProgressDialog progressBackup;

        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {

            Files.clear();
            Directories.clear();
            currentDir = "";
            Commands command= new Commands();
            for (int i=0; i<Filedirectories.size();i++){
                currentDir = currentDir+Filedirectories.get(i);
            }
            currentDir = BaseDir +currentDir;
            System.out.println("DIR: "+currentDir);

            File f = new File(currentDir);

            f.mkdirs();
            File[] files = f.listFiles();
            if (files.length == 0)
                return null;
            else {
                for (int i=0; i<files.length; i++) {
                    System.out.println(files[i].getName());
                    if (files[i].isDirectory()) {
                        Directories.add(files[i].getName());
                    } else {
                        Files.add(files[i].getName());
                    }
                }
            }
        //}
            //Files = command.loadFiles(currentDir+directories.get(0));

            return null;

        }

        protected void onPostExecute(Void result) {

            ImageView noOverlays = (ImageView) cordLayout.findViewById(R.id.imageView);
            TextView noOverlaysText = (TextView) cordLayout.findViewById(R.id.textView7);
            //if (Files.isEmpty()){
           //     noOverlays.setVisibility(View.VISIBLE);
           //     noOverlaysText.setVisibility(View.VISIBLE);
           // }
            InstallOverlayList.clear();
            for (int i =0; i< Files.size();i++){
                InstallOverlayList.add(0);
            }

            atleastOneIsClicked =0;
            mAdapter = new CardViewAdapter3(Files,Directories, R.layout.adapter_install_layout, getActivity());
            mRecyclerView.setAdapter(mAdapter);
            ActivityCompat.invalidateOptionsMenu(getActivity());
        }
    }

    private void loadToolbarRecylcerViewFab() {
        // Handle Toolbar
        Toolbar toolbar = (Toolbar) cordLayout.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView) cordLayout.findViewById(R.id.cardList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        fab2 = (android.support.design.widget.FloatingActionButton) cordLayout.findViewById(R.id.fab6);
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
    }


    //Adapter
    private class CardViewAdapter3 extends RecyclerView.Adapter<CardViewAdapter3.ViewHolder>{

        private ArrayList<String> themes;
        private ArrayList<String> directories;
        private int rowLayout;
        private Context mContext;

        public CardViewAdapter3(ArrayList<String> themes,ArrayList<String> directories, int rowLayout, Context context) {
            this.directories = directories;
            this.themes = themes;
            this.rowLayout = rowLayout;
            this.mContext = context;
            //themes.addAll(directories);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int i) {
            System.out.print(i);
            if (i< themes.size()) {
                viewHolder.image.setImageResource(R.drawable.ic_file);
                viewHolder.themeName.setText(themes.get(i).replace(".apk", "").replace("_", " "));
                viewHolder.rel.setTag(i);
                viewHolder.themeName.setId(i);
            } else{
                viewHolder.image.setImageResource(R.drawable.ic_folder);
                viewHolder.themeName.setText(directories.get(i-themes.size()));
                viewHolder.rel.setTag(i);
                viewHolder.themeName.setId(i);
            }

            //if (InstallOverlayList.get(i)==1){
                //viewHolder.themeName.setChecked(true);
            //}else{
                //viewHolder.themeName.setChecked(false);
            //}
            viewHolder.rel.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    /*CheckBox cb = (CheckBox) v;
                    //System.out.println(v.getTag());
                    /if (cb.isChecked()) {
                        InstallOverlayList.set(i, 1);
                        atleastOneIsClicked = atleastOneIsClicked + 1;

                    } else {
                        InstallOverlayList.set(i, 0);
                        atleastOneIsClicked = atleastOneIsClicked - 1;
                    }

                    if (atleastOneIsClicked > 0) {
                        fab2.setVisibility(View.VISIBLE);
                        fab2.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                    } else {
                        fab2.animate().translationY(fab2.getHeight() + 48).setInterpolator(new AccelerateInterpolator(2)).start();
                    } */
                    if (Integer.parseInt(v.getTag().toString())>=themes.size()){
                        Filedirectories.add("/"+directories.get(i-themes.size()));
                        new LoadAndSet().execute();

                    }else{
                        v.setBackgroundColor(getResources().getColor(R.color.divider));
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return themes.size() + directories.size() /*themes == null ? 0 : themes.size()*/;
        }
        public  class ViewHolder extends RecyclerView.ViewHolder {
            public TextView themeName;
            public ImageView image;
            public RelativeLayout rel;


            public ViewHolder(View itemView) {
                super(itemView);
                themeName = (TextView) itemView.findViewById(R.id.txt);
                image =  (ImageView)itemView.findViewById(R.id.img);
                rel = (RelativeLayout)itemView.findViewById(R.id.rel);
            }
        }
    }




    //Delete Overlays
    private class DeleteOverlays extends AsyncTask<Void,Void,Void> {
        ProgressDialog progressDelete;

        protected void onPreExecute() {

            progressDelete = ProgressDialog.show(getActivity(), "Uninstall Overlays",
                    "Uninstalling...", true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            RootTools.remount("/system", "RW");
            for (int i=0; i< Files.size();i++){
                if (InstallOverlayList.get(i)==1){
                    RootCommands.DeleteFileRoot("system/vendor/overlay/" + Files.get(i));
                }
            }
            return null;

        }

        protected void onPostExecute(Void result) {
            progressDelete.dismiss();
            RootTools.remount("/system", "RO");

            CoordinatorLayout coordinatorLayoutView = (CoordinatorLayout) cordLayout.findViewById(R.id.main_content3);
            Snackbar.make(coordinatorLayoutView, "Uninstalled selected Overlays", Snackbar.LENGTH_LONG)
                    .setAction("Reboot", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder progressDialogReboot = new AlertDialog.Builder(getActivity());
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

            new LoadAndSet().execute();

            ImageView noOverlays = (ImageView) cordLayout.findViewById(R.id.imageView);
            TextView noOverlaysText = (TextView) cordLayout.findViewById(R.id.textView7);
            if (Files.isEmpty()){
                noOverlays.setVisibility(View.VISIBLE);
                noOverlaysText.setVisibility(View.VISIBLE);
            }
            mCab.finish();
            ActivityCompat.invalidateOptionsMenu(getActivity());

        }
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







    //Overflow Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!Files.isEmpty()) {
            inflater.inflate(R.menu.overflow, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_selectall:
               Filedirectories.remove(Filedirectories.size()-1);
                new LoadAndSet().execute();
                return true;
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    /*
    static final String TAG = "Install";
    private String previewimageszip = null;

    final String startDirInstall = Environment.getExternalStorageDirectory() +  "/Overlays";
    private static final int CODE_SD = 0;
    private static final int CODE_DB = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        previewimageszip = getApplicationInfo().dataDir + "/overlay/previewimages.zip";
        // GET STRING SZP
        final Intent extras = getIntent();
        String SZP = null;
        if (extras != null) {
            SZP = extras.getStringExtra("key1");
        }

        if (SZP != null) {

            installmultiplecommand();

        } else {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            // Set these depending on your use case. These are the defaults.
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, true);
            i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
            i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
            i.putExtra(FilePickerActivity.EXTRA_START_PATH, startDirInstall);
            i.putExtra("FilePickerMode","Install Overlays");

            // start filePicker forResult
            startActivityForResult(i, CODE_SD);
        }
    } // ends onCreate

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if ((CODE_SD == requestCode || CODE_DB == requestCode) &&
                resultCode == Activity.RESULT_OK) {
            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE,
                    false)) {
                ArrayList<String> paths = data.getStringArrayListExtra(
                        FilePickerActivity.EXTRA_PATHS);
                StringBuilder sb = new StringBuilder();

                if (paths != null) {
                    for (String path : paths) {
                        if (path.startsWith("file://")) {
                            path = path.substring(7);
                            sb.append(path);
                        }
                    }

                    String SZP = (sb.toString());
                    Intent iIntent = new Intent(this, Install.class);
                    iIntent.putExtra("key1", SZP);
                    iIntent.putStringArrayListExtra("key2", paths);
                    startActivity(iIntent);
                    finish();

                }

            } else {
                // Get the File path from the Uri
                String SZP = (data.getData().toString());
                if (SZP.startsWith("file://")) {
                    SZP = SZP.substring(7);
                    Intent iIntent = new Intent(this, Install.class);
                    iIntent.putExtra("key1", SZP);
                    startActivity(iIntent);
                    finish();
                }
            }
        }
    } // ends onActivityForResult

    /**
     * **********************************************************************************************************
     * UNZIP UTIL
     * ************
     * Unzip a zip file.  Will overwrite existing files.
     *
     * @param zipFile  Full path of the zip file you'd like to unzip.
     * @param location Full path of the directory you'd like to unzip to (will be created if it doesn't exist).
     * @throws java.io.IOException *************************************************************************************************************

    public void unzip(String zipFile, String location) throws IOException {

        int size;
        byte[] buffer = new byte[1024];

        try {

            if (!location.endsWith("/")) {
                location += "/";
            }
            File f = new File(location);
            if (!f.isDirectory()) {
                f.mkdirs();
            }
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile), 1024));
            try {
                ZipEntry ze;
                while ((ze = zin.getNextEntry()) != null) {
                    String path = location + ze.getName();
                    File unzipFile = new File(path);

                    if (ze.isDirectory()) {
                        if (!unzipFile.isDirectory()) {
                            unzipFile.mkdirs();
                        }
                    } else {

                        // check for and create parent directories if they don't exist
                        File parentDir = unzipFile.getParentFile();
                        if (null != parentDir) {
                            if (!parentDir.isDirectory()) {
                                parentDir.mkdirs();
                            }
                        }
                        // unzip the file
                        FileOutputStream out = new FileOutputStream(unzipFile, false);
                        BufferedOutputStream fout = new BufferedOutputStream(out, 1024);
                        try {
                            while ((size = zin.read(buffer, 0, 1024)) != -1) {
                                fout.write(buffer, 0, size);
                            }
                            zin.closeEntry();
                        } finally {
                            fout.flush();
                            fout.close();
                            out.close();
                        }
                    }
                }
            } finally {
                zin.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Unzip exception", e);
        }
    }
        private ArrayList<String> paths;
        public void installmultiplecommand () {

            //ArrayList<String> paths;
            paths = getIntent().getStringArrayListExtra("key2");
            new InstallOverlays().execute();

        }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back2, R.anim.back1);
    }

    private class InstallOverlays extends AsyncTask<Void,Void,Void> {
        ProgressDialog progressDelete;

        protected void onPreExecute() {

            progressDelete = ProgressDialog.show(Install.this, "Install Overlays",
                    "installing...", true);
        }

        @Override
        protected Void doInBackground(Void... params) {

            Commands command = new Commands();
            command.InstallOverlays(Install.this, paths);
            return null;
        }

        protected void onPostExecute(Void result) {

            progressDelete.dismiss();
            finish();
            // LAUNCH LAYERS.CLASS
            overridePendingTransition(R.anim.back2, R.anim.back1);
            Intent iIntent = new Intent(Install.this, menu.class);
            iIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            iIntent.putExtra("ShowSnackbar", true);
            iIntent.putExtra("SnackbarText","Installed selected Overlays");
            startActivity(iIntent);

        }
    } */
}

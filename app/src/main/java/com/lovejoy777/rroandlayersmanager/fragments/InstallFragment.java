package com.lovejoy777.rroandlayersmanager.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcab.MaterialCab;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.beans.UninstallFile;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;
import com.stericson.RootTools.RootTools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lovejoy777 on 10/06/15.
 */
public class InstallFragment extends Fragment {

    private ArrayList<UninstallFile> Files = new ArrayList<>();
    //private ArrayList<String> Files = new ArrayList<>();
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
        Filedirectories.add("SD Card");
        Filedirectories.add("/Overlays");
        FragmentActivity faActivity  = (FragmentActivity)    super.getActivity();
        cordLayout = (CoordinatorLayout)    inflater.inflate(R.layout.fragment_install, container, false);

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

            if (Files!=null){
                Files.clear();
            }
            if (Directories!=null) {
                Directories.clear();
            }
            currentDir = "";
            Commands command= new Commands();
            for (int i=1; i<Filedirectories.size();i++){
                currentDir = currentDir+Filedirectories.get(i);
            }
            currentDir = BaseDir +currentDir;

            ArrayList<String> loadedFiles = new ArrayList<String>();


            loadedFiles.addAll(command.loadFiles(currentDir));

            System.out.println(loadedFiles);

            for (String /*file*/ currentDir : loadedFiles) {
                Files.add(new UninstallFile(/*file*/currentDir));

            }
            //Files = command.loadFiles(currentDir);
            Directories = command.loadFolders(currentDir);

            if (Directories!=null){
                Collections.sort(Directories, String.CASE_INSENSITIVE_ORDER);
            }


            return null;

        }

        protected void onPostExecute(Void result) {


            //InstallOverlayList.clear();
            //for (int i =0; i< Files.size();i++){
            //    InstallOverlayList.add(0);
            //}

            atleastOneIsClicked =0;
            mAdapter = new CardViewAdapter3(Files,Directories, R.layout.adapter_install_layout,R.layout.adapter_listlayout, getActivity());
            mRecyclerView.setAdapter(mAdapter);
            ActivityCompat.invalidateOptionsMenu(getActivity());

            LinearLayout HscrollView = (LinearLayout) cordLayout.findViewById(R.id.horizontalScrollView2);
            HscrollView.removeAllViews();



            for (int i =0; i <Filedirectories.size();i++) {


                TextView tv = new TextView(getActivity().getApplicationContext());
                tv.setText(Filedirectories.get(i).replaceAll("/", "").toUpperCase());
                tv.setTextColor(getResources().getColor(R.color.white));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                //int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getActivity().getResources().getDisplayMetrics());
                //params.setMarginEnd(margin);
                //params.setMarginStart(margin);

                tv.setLayoutParams(params);
                tv.setTag(Filedirectories.get(i));
                tv.setBackground(getActivity().getResources().getDrawable(R.drawable.ripple));

                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getActivity().getResources().getDisplayMetrics());
                tv.setPadding(padding, padding, padding, padding);
                HscrollView.addView(tv);

                tv.setOnClickListener(onclicklistener);


                final HorizontalScrollView scroller = (HorizontalScrollView) cordLayout.findViewById(R.id.horizontalScrollView3);

                scroller.post(new Runnable() {

                    @Override
                    public void run() {
                        scroller.fullScroll(View.FOCUS_RIGHT);
                    }
                });
                if (Filedirectories.size() > 1 && i != Filedirectories.size()-1){
                    ImageView img = new ImageView(getActivity().getApplicationContext());
                    img.setBackgroundResource(R.drawable.ic_action_up2);
                    HscrollView.addView(img);
                    ViewGroup.LayoutParams iv_params_b = img.getLayoutParams();
                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    params2.gravity = Gravity.CENTER;

                    img.setLayoutParams(params2);
                }
            }
            System.out.println("DIR: " + currentDir);
        }
    }

    View.OnClickListener onclicklistener = new View.OnClickListener() {
        public void onClick(View v) {
            Object clickedOn = v.getTag()/*.toString()*/;
            Filedirectories.subList(Filedirectories.indexOf(clickedOn)+1, Filedirectories.size()).clear();
            //System.out.println(Filedirectories.indexOf(clickedOn));
            LinearLayout HscrollView = (LinearLayout) cordLayout.findViewById(R.id.horizontalScrollView2);

new LoadAndSet().execute();
        }
    };

    private void loadToolbarRecylcerViewFab() {
        // Handle Toolbar
        Toolbar toolbar = (Toolbar) cordLayout.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRecyclerView = (RecyclerView) cordLayout.findViewById(R.id.cardList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        fab2 = (android.support.design.widget.FloatingActionButton) cordLayout.findViewById(R.id.fab6);
        fab2.hide();
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> test = new ArrayList<String>();
                for (UninstallFile file : Files) {
                    if (file.isChecked()) {
                        test.add(file.getLocation());
                    }
                }
                fab2.hide();
                new InstallOverlays().execute();
                System.out.println(test);

                //fab2.animate().translationY(fab2.getHeight() + 48).setInterpolator(new AccelerateInterpolator(2)).start();
                //new DeleteOverlays().execute();
            }
        });
    }


    //Adapter
    private class CardViewAdapter3 extends RecyclerView.Adapter<CardViewAdapter3.MyViewHolder>{

        private ArrayList<UninstallFile> themes;
        //private ArrayList<String> themes;
        private ArrayList<String> directories;
        private int rowLayout;
        private  int checkboxLayout;
        private Context mContext;

        public CardViewAdapter3(ArrayList<UninstallFile> themes,ArrayList<String> directories, int rowLayout, int checkboxLayout, Context context) {
            this.directories = directories;
            this.themes = themes;
            this.rowLayout = rowLayout;
            this.mContext = context;
            this.checkboxLayout = checkboxLayout;
            //themes.addAll(directories);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        MyViewHolder myViewholder;
            View v;
            Context context = viewGroup.getContext();

            if (viewType == 1) {
                v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
                myViewholder = new MyViewHolder(v, 1);
            } else {
                v = LayoutInflater.from(viewGroup.getContext()).inflate(checkboxLayout, viewGroup, false);
                myViewholder = new MyViewHolder(v, 0);
            }

            return myViewholder;



            /*View v = null;
            switch (getItemViewType(i)) {
                case 0:
                    v = LayoutInflater.from(viewGroup.getContext()).inflate(checkboxLayout, viewGroup, false);

                case 1:
                    //v = LayoutInflater.from(viewGroup.getContext()).inflate(checkboxLayout, viewGroup, false);
                    v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
            }
           // System.out.println(getItemViewType(i));
            return new ViewHolder(v); */
        }


       // private UninstallFile theme2 = null;
        @Override
        public void onBindViewHolder(MyViewHolder viewHolder, final int i) {





            if (isFolder(i)) {
                viewHolder.image.setImageResource(R.drawable.ic_folder);
                viewHolder.themeName.setText(directories.get(i));
                viewHolder.rel.setTag(i);
                viewHolder.themeName.setId(i);
                viewHolder.rel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {

                        Filedirectories.add("/"+directories.get(i));
                        new LoadAndSet().execute();
                    }
                });
            } else{
                final UninstallFile theme2 = themes.get(i - directories.size());
                viewHolder.check.setText(theme2.getFullName());
                viewHolder.check.setTag(i);
                viewHolder.check.setId(i);
                viewHolder.check.setChecked(theme2.isChecked());
                viewHolder.check.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        CheckBox cb = (CheckBox) v;
                        if (cb.isChecked()) {
                            theme2.setChecked(true);
                            atleastOneIsClicked = atleastOneIsClicked + 1;

                        } else {
                            theme2.setChecked(false);
                            atleastOneIsClicked = atleastOneIsClicked - 1;
                        }

                        if (atleastOneIsClicked > 0) {
                            fab2.show();
                        } else {
                           fab2.hide();
                        }
                        System.out.println(theme2.getName()+" Is checked "+theme2.isChecked());
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            if (themes!=null) {
                return themes.size() + directories.size() /*themes == null ? 0 : themes.size()*/;
            } else
                return 0;
        }

        public boolean isFolder(int i){
            if (i< directories.size()){
                return true;
            }else{
                return false;
            }
        }
        public  class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView themeName;
            public ImageView image;
            public RelativeLayout rel;
            public CheckBox check;



            public MyViewHolder(View itemView, int type) {
                super(itemView);

                if (type == 1) {
                    themeName = (TextView) itemView.findViewById(R.id.txt);
                    image =  (ImageView)itemView.findViewById(R.id.img);
                    rel = (RelativeLayout)itemView.findViewById(R.id.rel);
                } else if (type == 0) {
                    check = (CheckBox)itemView.findViewById(R.id.deletecheckbox);
                }


            }


        }

        @Override
        public int getItemViewType(int position) {
            System.out.println(isFolder(position));
            if (isFolder(position)){
                return 1;
            } else{
                return 0;
            }


        }
    }








    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
*/
    private class InstallOverlays extends AsyncTask<Void,Void,Void> {
        ProgressDialog progressDelete;

        protected void onPreExecute() {
System.out.println("TEST");
            progressDelete = ProgressDialog.show(getActivity(), "Install Overlays",
                    "Installing...", true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<String> paths = new ArrayList<String>();
            Commands command = new Commands();
            for (UninstallFile file : Files) {
                if (file.isChecked()) {
                    paths.add(currentDir+"/"+file.getFullName());
                }
            }
System.out.println(paths);
            command.InstallOverlays(getActivity(), paths);
            return null;
        }

        protected void onPostExecute(Void result) {

            progressDelete.dismiss();


        }
    }
}

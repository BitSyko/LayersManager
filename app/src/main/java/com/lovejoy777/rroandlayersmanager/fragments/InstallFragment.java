package com.lovejoy777.rroandlayersmanager.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.beans.FileBean;
import com.lovejoy777.rroandlayersmanager.commands.Commands;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lovejoy777 on 10/06/15.
 */
public class InstallFragment extends Fragment implements FABProgressListener {

    private ArrayList<FileBean> Files = new ArrayList<>();
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
    private FABProgressCircle fabProgressCircle = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        BaseDir = Environment.getExternalStorageDirectory()+"";
        currentDir =null;
        Filedirectories.add("SD Card");
        Filedirectories.add("/Overlays");
        FragmentActivity faActivity  = (FragmentActivity)    super.getActivity();
        cordLayout = (CoordinatorLayout)    inflater.inflate(R.layout.fragment_install, container, false);

        fabProgressCircle = (FABProgressCircle) cordLayout.findViewById(R.id.fabProgressCircle);
        fabProgressCircle.attachListener(this);

        setHasOptionsMenu(true);

        loadToolbarRecylcerViewFab();

        new LoadAndSet().execute();

        return cordLayout;
    }




    private class LoadAndSet extends AsyncTask<String,String,Void> {

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
            for (int i=1; i<Filedirectories.size();i++){
                currentDir = currentDir+Filedirectories.get(i);
            }
            currentDir = BaseDir +currentDir;

            ArrayList<String> loadedFiles = new ArrayList<String>();


            try {
                loadedFiles.addAll(Commands.loadFiles(currentDir));
            } catch(NullPointerException e){
            }



            for (String currentDir : loadedFiles) {
                Files.add(new FileBean(currentDir));

            }
            Directories = Commands.loadFolders(currentDir);

            if (Directories!=null){
                Collections.sort(Directories, String.CASE_INSENSITIVE_ORDER);
            }


            return null;

        }

        protected void onPostExecute(Void result) {


            atleastOneIsClicked =0;
            mAdapter = new CardViewAdapter3(Files,Directories, R.layout.adapter_install_layout,R.layout.adapter_listlayout, getActivity());
            mRecyclerView.setAdapter(mAdapter);
            ActivityCompat.invalidateOptionsMenu(getActivity());

            LinearLayout HscrollView = (LinearLayout) cordLayout.findViewById(R.id.horizontalScrollView2);
            HscrollView.removeAllViews();

            if (mAdapter.getItemCount() == 0) {
                cordLayout.findViewById(R.id.noItems).setVisibility(View.VISIBLE);
            } else {
                cordLayout.findViewById(R.id.noItems).setVisibility(View.INVISIBLE);
            }



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
                tv.setBackground(getActivity().getResources().getDrawable(R.drawable.rippleprimarys));

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
                    img.setBackgroundResource(R.drawable.ic_action_up);
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
                fabProgressCircle.show();
                fab2.setClickable(false);
                new InstallOverlays().execute();
            }
        });
    }


    //Adapter
    private class CardViewAdapter3 extends RecyclerView.Adapter<CardViewAdapter3.MyViewHolder>{

        private ArrayList<FileBean> themes;
        //private ArrayList<String> themes;
        private ArrayList<String> directories;
        private int rowLayout;
        private  int checkboxLayout;
        private Context mContext;

        public CardViewAdapter3(ArrayList<FileBean> themes,ArrayList<String> directories, int rowLayout, int checkboxLayout, Context context) {
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
                final FileBean theme2 = themes.get(i - directories.size());
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
            int Size = 0;
            if (themes!=null) {
                Size = Size + themes.size();
            }
            if (directories != null){
                Size = Size + directories.size();
            }
                return Size;
        }

        public boolean isFolder(int i){
            return i < directories.size();
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



    private class InstallOverlays extends AsyncTask<Void,Void,Void> {

        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<String> paths = new ArrayList<String>();
            for (FileBean file : Files) {
                if (file.isChecked()) {
                    paths.add(currentDir+"/"+file.getFullName());
                }
            }

            Commands.InstallOverlays(getActivity(), paths);
            return null;
        }

        protected void onPostExecute(Void result) {
            fabProgressCircle.beginFinalAnimation();
        }
    }

    @Override
    public void onFABProgressAnimationEnd() {
        fab2.hide();
        fab2.setClickable(true);
        UncheckAll();
        CoordinatorLayout coordinatorLayoutView = (CoordinatorLayout) cordLayout.findViewById(R.id.main_content3);
        Snackbar.make(fabProgressCircle, R.string.installed, Snackbar.LENGTH_LONG)
                .setAction(R.string.Reboot, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder progressDialogReboot = new AlertDialog.Builder(getActivity());
                        progressDialogReboot.setTitle(R.string.Reboot);
                        progressDialogReboot.setMessage(R.string.PreformReboot);
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
    }

    private void UncheckAll() {

        for (FileBean file : Files) {
            file.setChecked(false);
        }

        atleastOneIsClicked = 0;
        mAdapter.notifyDataSetChanged();

    }
}

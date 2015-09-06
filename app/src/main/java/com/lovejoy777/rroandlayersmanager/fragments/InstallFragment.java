package com.lovejoy777.rroandlayersmanager.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import com.lovejoy777.rroandlayersmanager.AsyncResponse;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.beans.FileBean;
import com.lovejoy777.rroandlayersmanager.commands.Commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class InstallFragment extends android.support.v4.app.Fragment implements AsyncResponse, BackButtonListener {

    private ArrayList<String> fileDirectories = new ArrayList<>();
    private FloatingActionButton fab2;
    private int atleastOneIsClicked = 0;
    private String currentDir = null;
    private String BaseDir = null;
    private ArrayList<FileBean> files = new ArrayList<>();
    private ArrayList<String> directories = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private CardViewAdapter3 mAdapter;
    private CoordinatorLayout cordLayout = null;
    private Stack<ArrayList<String>> directoriesStack = new Stack<>();

    private View.OnClickListener onclicklistener = new View.OnClickListener() {
        public void onClick(View v) {
            String clickedOn = (String) v.getTag();
            directoriesStack.push(new ArrayList<>(fileDirectories));
            fileDirectories.subList(fileDirectories.indexOf(clickedOn) + 1, fileDirectories.size()).clear();
            new LoadAndSet().execute();
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        BaseDir = Environment.getExternalStorageDirectory() + "";
        currentDir = null;
        fileDirectories.add("SD Card");
        fileDirectories.add("/Overlays");

        cordLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_install, container, false);

        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) getActivity().findViewById(R.id.toolbar);

        int elevation = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 96, getResources().getDisplayMetrics());
        toolbar.setNavigationIcon(R.drawable.ic_action_back);


        TextView toolbarTitle = (TextView) getActivity().findViewById(R.id.title2);
        toolbarTitle.setText(getString(R.string.InstallOverlays2));

        AppBarLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height
        );

        toolbar.setElevation(elevation);
        toolbar.setLayoutParams(layoutParams);


        setHasOptionsMenu(true);

        loadToolbarRecylcerViewFab();

        new LoadAndSet().execute();

        setHasOptionsMenu(true);

        return cordLayout;
    }

    private void loadToolbarRecylcerViewFab() {


        mRecyclerView = (RecyclerView) cordLayout.findViewById(R.id.cardList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        fab2 = (android.support.design.widget.FloatingActionButton) cordLayout.findViewById(R.id.fab6);
        fab2.hide();
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab2.setClickable(false);
                InstallAsyncOverlays();
            }
        });
    }

    private void InstallAsyncOverlays() {

        ArrayList<String> paths = new ArrayList<>();
        for (FileBean file : files) {
            if (file.isChecked()) {
                paths.add(currentDir + "/" + file.getFullName());
            }
        }

        new Commands.InstallZipBetterWay(getActivity(), this).execute(paths.toArray(new String[paths.size()]));
    }

    public void processFinish() {
        fab2.hide();
        fab2.setClickable(true);
        UncheckAll();
        Snackbar.make(cordLayout, R.string.installed, Snackbar.LENGTH_LONG)
                .setAction(R.string.Reboot, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Commands.reboot(getActivity());
                    }
                })
                .show();
    }

    private void UncheckAll() {

        for (FileBean file : files) {
            file.setChecked(false);
        }

        atleastOneIsClicked = 0;
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onBackButton() {

        if (directoriesStack.empty()) {
            return true;
        }

        fileDirectories = directoriesStack.pop();

        new LoadAndSet().execute();

        return false;

    }

    private class LoadAndSet extends AsyncTask<Void, String, Void> {

        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... params) {

            files.clear();

            directories.clear();

            currentDir = "";
            for (int i = 1; i < fileDirectories.size(); i++) {
                currentDir = currentDir + fileDirectories.get(i);
            }
            currentDir = BaseDir + currentDir;

            ArrayList<String> loadedFiles = new ArrayList<>();

            loadedFiles.addAll(Commands.loadFiles(currentDir));

            for (String currentDir : loadedFiles) {
                files.add(new FileBean(currentDir));

            }
            directories = Commands.loadFolders(currentDir);

            Collections.sort(directories, String.CASE_INSENSITIVE_ORDER);


            return null;

        }

        protected void onPostExecute(Void result) {


            atleastOneIsClicked = 0;
            mAdapter = new CardViewAdapter3(files, directories, R.layout.adapter_install_layout, R.layout.adapter_listlayout);
            mRecyclerView.setAdapter(mAdapter);
            ActivityCompat.invalidateOptionsMenu(getActivity());

            LinearLayout HscrollView = (LinearLayout) cordLayout.findViewById(R.id.horizontalScrollView2);
            HscrollView.removeAllViews();

            if (mAdapter.getItemCount() == 0) {
                cordLayout.findViewById(R.id.noItems).setVisibility(View.VISIBLE);
            } else {
                cordLayout.findViewById(R.id.noItems).setVisibility(View.INVISIBLE);
            }


            for (int i = 0; i < fileDirectories.size(); i++) {


                TextView tv = new TextView(getActivity().getApplicationContext());
                tv.setText(fileDirectories.get(i).replaceAll("/", "").toUpperCase());
                tv.setTextColor(getResources().getColor(R.color.white));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                //int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getActivity().getResources().getDisplayMetrics());
                //params.setMarginEnd(margin);
                //params.setMarginStart(margin);

                tv.setLayoutParams(params);
                tv.setTag(fileDirectories.get(i));
                tv.setBackground(getActivity().getResources().getDrawable(R.drawable.rippleprimarys, null));

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
                if (fileDirectories.size() > 1 && i != fileDirectories.size() - 1) {
                    ImageView img = new ImageView(getActivity().getApplicationContext());
                    img.setBackgroundResource(R.drawable.ic_action_up);
                    HscrollView.addView(img);
                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                    params2.gravity = Gravity.CENTER;

                    img.setLayoutParams(params2);
                }
            }
            Log.d("DIR", currentDir);
        }
    }

    //Adapter
    private class CardViewAdapter3 extends RecyclerView.Adapter<CardViewAdapter3.MyViewHolder> {

        private ArrayList<FileBean> themes;
        private ArrayList<String> directories;
        private int rowLayout;
        private int checkboxLayout;

        public CardViewAdapter3(ArrayList<FileBean> themes, ArrayList<String> directories, int rowLayout, int checkboxLayout) {
            this.directories = directories;
            this.themes = themes;
            this.rowLayout = rowLayout;
            this.checkboxLayout = checkboxLayout;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            MyViewHolder myViewholder;
            View v;
            Context context = viewGroup.getContext();

            if (viewType == 1) {
                v = LayoutInflater.from(context).inflate(rowLayout, viewGroup, false);
                myViewholder = new MyViewHolder(v, 1);
            } else {
                v = LayoutInflater.from(context).inflate(checkboxLayout, viewGroup, false);
                myViewholder = new MyViewHolder(v, 0);
            }

            return myViewholder;
        }


        @Override
        public void onBindViewHolder(MyViewHolder viewHolder, final int i) {

            if (isFolder(i)) {
                viewHolder.image.setImageResource(R.drawable.ic_folder);
                viewHolder.themeName.setText(directories.get(i));
                viewHolder.rel.setTag(i);
                viewHolder.themeName.setId(i);
                viewHolder.rel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        fab2.hide();
                        directoriesStack.push(new ArrayList<>(fileDirectories));
                        fileDirectories.add("/" + directories.get(i));
                        new LoadAndSet().execute();
                    }
                });
            } else {
                final FileBean theme2 = themes.get(i - directories.size());
                viewHolder.check.setText(theme2.getFullName());
                viewHolder.check.setTag(i);
                viewHolder.check.setId(i);

                viewHolder.check.setEnabled(theme2.isInstallable());

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

                        System.out.println(theme2.getName() + " Is checked " + theme2.isChecked());
                    }
                });
            }

        }

        @Override
        public int getItemCount() {
            return themes.size() + directories.size();
        }

        public boolean isFolder(int i) {
            return i < directories.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (isFolder(position)) {
                return 1;
            } else {
                return 0;
            }
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView themeName;
            public ImageView image;
            public RelativeLayout rel;
            public CheckBox check;

            public MyViewHolder(View itemView, int type) {
                super(itemView);

                if (type == 1) {
                    themeName = (TextView) itemView.findViewById(R.id.txt);
                    image = (ImageView) itemView.findViewById(R.id.img);
                    rel = (RelativeLayout) itemView.findViewById(R.id.rel);
                } else if (type == 0) {
                    check = (CheckBox) itemView.findViewById(R.id.deletecheckbox);
                }
            }
        }
    }
}

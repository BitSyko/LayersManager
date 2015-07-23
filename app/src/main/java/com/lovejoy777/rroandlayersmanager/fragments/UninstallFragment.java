package com.lovejoy777.rroandlayersmanager.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.view.*;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.afollestad.materialcab.MaterialCab;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.helper.AdvancedFile;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by lovejoy777 on 13/06/15.
 */
public class UninstallFragment extends Fragment implements MaterialCab.Callback {

    private ArrayList<AdvancedFile> files = new ArrayList<>();
    FloatingActionButton fab2;
    int atleastOneIsClicked = 0;
    private RecyclerView mRecyclerView;
    private CardViewAdapter3 mAdapter;
    private MaterialCab mCab = null;
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout cordLayout = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FragmentActivity faActivity = (FragmentActivity) super.getActivity();
        cordLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_delete, container, false);

        setHasOptionsMenu(true);

        loadToolbarRecylcerViewFab();

        new LoadAndSet().execute();

        return cordLayout;
    }


    private class LoadAndSet extends AsyncTask<String, String, Void> {
        ProgressDialog progressBackup;

        protected void onPreExecute() {
            files.clear();
        }

        @Override
        protected Void doInBackground(String... params) {

            ArrayList<String> loadedFiles = new ArrayList<String>();
            Commands command = new Commands();

            loadedFiles.addAll(command.RootloadFiles(getActivity(),getActivity(),"/system/vendor/overlay"));

            for (String file : loadedFiles) {
                files.add(new AdvancedFile(file));
            }

            return null;

        }

        protected void onPostExecute(Void result) {



            atleastOneIsClicked = 0;
            mAdapter = new CardViewAdapter3(files, R.layout.adapter_listlayout, getActivity());
            mRecyclerView.setAdapter(mAdapter);
            ActivityCompat.invalidateOptionsMenu(getActivity());

            ImageView noOverlays = (ImageView) cordLayout.findViewById(R.id.imageView);
            TextView noOverlaysText = (TextView) cordLayout.findViewById(R.id.textView7);
            if (files.isEmpty()) {
                noOverlays.setVisibility(View.VISIBLE);
                noOverlaysText.setVisibility(View.VISIBLE);
            }
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
        fab2.hide();
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println(InstallOverlayList);
                fab2.hide();
                new DeleteOverlays().execute();
            }
        });
    }


    //Adapter
    private class CardViewAdapter3 extends RecyclerView.Adapter<CardViewAdapter3.ViewHolder> {

        private ArrayList<AdvancedFile> themes;
        private int rowLayout;
        private Context mContext;

        public CardViewAdapter3(ArrayList<AdvancedFile> themes, int rowLayout, Context context) {
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

            final AdvancedFile theme = themes.get(i);

            viewHolder.themeName.setText(theme.getName());
            viewHolder.themeName.setTag(theme.getLocation());
            viewHolder.themeName.setId(i);
            viewHolder.themeName.setChecked(theme.isChecked());

            viewHolder.themeName.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    CheckBox cb = (CheckBox) v;
                    System.out.println(v.getTag());
                    if (cb.isChecked()) {
                        theme.setChecked(true);
                        atleastOneIsClicked = atleastOneIsClicked + 1;

                    } else {
                        theme.setChecked(false);
                        atleastOneIsClicked = atleastOneIsClicked - 1;
                    }
                    if (mCab == null) {
                        mCab = new MaterialCab((AppCompatActivity) getActivity(), R.id.cab_stub)
                                .reset()
                                .setCloseDrawableRes(R.drawable.ic_action_check)
                                .setMenu(R.menu.overflow)
                                .start(UninstallFragment.this);
                    } else if (!mCab.isActive()) {
                        mCab
                                .reset().start(UninstallFragment.this)
                                .setCloseDrawableRes(R.drawable.ic_action_check)
                                .setMenu(R.menu.overflow);
                    }

                    mCab.setTitle(atleastOneIsClicked + " "+getResources().getString(R.string.OverlaysSelected));
                    if (atleastOneIsClicked > 0) {
                        fab2.show();
                    } else {
                        mCab.finish();
                        mCab = null;
                        fab2.hide();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return themes == null ? 0 : themes.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public CheckBox themeName;


            public ViewHolder(View itemView) {
                super(itemView);
                themeName = (CheckBox) itemView.findViewById(R.id.deletecheckbox);
            }
        }
    }


    //Delete Overlays
    private class DeleteOverlays extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDelete;

        protected void onPreExecute() {

            progressDelete = ProgressDialog.show(getActivity(), getString(R.string.UninstallOverlays),
                    getString(R.string.uninstalling)+"...", true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            RootTools.remount("/system", "RW");
            for (AdvancedFile file : files) {
                if (file.isChecked()) {
                    RootCommands.DeleteFileRoot("system/vendor/overlay/" + file.getLocation());
                }
            }
            return null;

        }

        protected void onPostExecute(Void result) {
            progressDelete.dismiss();
            RootTools.remount("/system", "RO");

            CoordinatorLayout coordinatorLayoutView = (CoordinatorLayout) cordLayout.findViewById(R.id.main_content3);
            Snackbar.make(coordinatorLayoutView, R.string.uninstalled, Snackbar.LENGTH_LONG)
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

            new LoadAndSet().execute();
            mCab.finish();
            ActivityCompat.invalidateOptionsMenu(getActivity());

        }
    }


    //Check and Uncheck all Checkboxes
    private void checkAll() {

        for (AdvancedFile file : files) {
            file.setChecked(true);
        }

        atleastOneIsClicked = files.size();
        // System.out.println(atleastOneIsClicked);
        mAdapter.notifyDataSetChanged();
        fab2.show();
        if (mCab == null)
            mCab = new MaterialCab((AppCompatActivity) getActivity(), R.id.cab_stub)
                    .reset()
                    .setCloseDrawableRes(R.drawable.ic_action_check)
                    .setMenu(R.menu.overflow)
                    .start(UninstallFragment.this);
        else if (!mCab.isActive())
            mCab
                    .reset().start(UninstallFragment.this)
                    .setCloseDrawableRes(R.drawable.ic_action_check)
                    .setMenu(R.menu.overflow);

        mCab.setTitle(atleastOneIsClicked + " "+getResources().getString(R.string.OverlaysSelected));
    }

    private void UncheckAll() {

        for (AdvancedFile file : files) {
            file.setChecked(false);
        }

        atleastOneIsClicked = 0;
        mAdapter.notifyDataSetChanged();
        fab2.hide();
    }


    //CAB methods
    @Override
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!files.isEmpty()) {
            inflater.inflate(R.menu.overflow, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
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
}
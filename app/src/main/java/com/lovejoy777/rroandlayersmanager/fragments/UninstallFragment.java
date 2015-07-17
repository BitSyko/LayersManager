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
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.afollestad.materialcab.MaterialCab;
import com.jereksel.listviewslide.SlidableListView;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.beans.UninstallFile;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UninstallFragment extends Fragment implements MaterialCab.Callback {

    private ArrayList<UninstallFile> files = new ArrayList<>();
    FloatingActionButton fab2;
    int atleastOneIsClicked = 0;
    private SlidableListView mListView;
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

        }

        @Override
        protected Void doInBackground(String... params) {

            ArrayList<String> loadedFiles = new ArrayList<String>();
            Commands command = new Commands();

            loadedFiles.addAll(command.loadFiles("/system/vendor/overlay"));

            for (String file : loadedFiles) {
                files.add(new UninstallFile(file));
            }

            return null;

        }

        protected void onPostExecute(Void result) {

            ImageView noOverlays = (ImageView) cordLayout.findViewById(R.id.imageView);
            TextView noOverlaysText = (TextView) cordLayout.findViewById(R.id.textView7);
            if (files.isEmpty()) {
                noOverlays.setVisibility(View.VISIBLE);
                noOverlaysText.setVisibility(View.VISIBLE);
            }

            atleastOneIsClicked = 0;
            mAdapter = new CardViewAdapter3(getActivity(), R.layout.adapter_listlayout, files);
            mListView.setAdapter(mAdapter);
            mListView.addClass(android.support.v7.widget.AppCompatCheckBox.class);
            //   mListView.setMode(SlidableListView.Mode.TWO_FINGERS);

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

        mListView = (SlidableListView) cordLayout.findViewById(R.id.cardList);
        //mListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //  mListView.setItemAnimator(new DefaultItemAnimator());

        fab2 = (android.support.design.widget.FloatingActionButton) cordLayout.findViewById(R.id.fab6);
        fab2.setVisibility(View.INVISIBLE);
        fab2.animate().translationY(218).setInterpolator(new AccelerateInterpolator(2)).start();
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //System.out.println(InstallOverlayList);
                fab2.animate().translationY(fab2.getHeight() + 48).setInterpolator(new AccelerateInterpolator(2)).start();
                new DeleteOverlays().execute();
            }
        });
    }

    private class CardViewAdapter3 extends ArrayAdapter<UninstallFile> {

        private final Context context;
        private final List<UninstallFile> themes;
        private int layout;

        public CardViewAdapter3(Context activity, int layout, List<UninstallFile> themes) {
            super(activity, layout, themes);
            this.context = activity;
            this.themes = themes;
            this.layout = layout;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            // reuse views
            if (rowView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                rowView = inflater.inflate(layout, null);
                // configure view holder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.button = (CheckBox) rowView
                        .findViewById(R.id.deletecheckbox);
                viewHolder.text = (TextView) rowView
                        .findViewById(R.id.textcheckbox);
                rowView.setTag(viewHolder);
            }


            // fill data
            ViewHolder viewHolder = (ViewHolder) rowView.getTag();

            final UninstallFile theme = themes.get(position);

            //viewHolder.button.setText(theme.getName());
            viewHolder.text.setText(theme.getName());
            viewHolder.button.setTag(theme.getLocation());
            viewHolder.button.setId(position);
            viewHolder.button.setChecked(theme.isChecked());

            viewHolder.button.setOnClickListener(new View.OnClickListener() {
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

            return rowView;
        }

    }

    static class ViewHolder {
        public CheckBox button;
        public TextView text;
    }


    //Delete Overlays
    private class DeleteOverlays extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDelete;

        protected void onPreExecute() {

            progressDelete = ProgressDialog.show(getActivity(), "Uninstall Overlays",
                    "Uninstalling...", true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            RootTools.remount("/system", "RW");
            for (UninstallFile file : files) {
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
            if (files.isEmpty()) {
                noOverlays.setVisibility(View.VISIBLE);
                noOverlaysText.setVisibility(View.VISIBLE);
            }
            mCab.finish();
            ActivityCompat.invalidateOptionsMenu(getActivity());

        }
    }


    //Check and Uncheck all Checkboxes
    private void checkAll() {

        for (UninstallFile file : files) {
            file.setChecked(true);
        }

        atleastOneIsClicked = files.size();
        // System.out.println(atleastOneIsClicked);
        mAdapter.notifyDataSetChanged();
        fab2.setVisibility(View.VISIBLE);
        fab2.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
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

        mCab.setTitle(atleastOneIsClicked + " Overlays selected");
    }

    private void UncheckAll() {

        for (UninstallFile file : files) {
            file.setChecked(false);
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
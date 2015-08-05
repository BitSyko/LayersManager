package com.lovejoy777.rroandlayersmanager.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.*;
import com.afollestad.materialcab.MaterialCab;
import com.lovejoy777.rroandlayersmanager.AsyncResponse;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.beans.FileBean;
import com.lovejoy777.rroandlayersmanager.commands.Commands;

import java.util.ArrayList;
import java.util.List;

public class UninstallFragment extends Fragment implements MaterialCab.Callback, AsyncResponse {

    private FloatingActionButton fab2;
    private LinearLayout mLinearLayout;
    private MaterialCab mCab = null;
    private CoordinatorLayout cordLayout = null;
    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        cordLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_delete, container, false);

        setHasOptionsMenu(true);

        loadToolbarRecylcerViewFab();

        new LoadAndSet().execute();

        return cordLayout;
    }

    @Override
    public void processFinish() {
        fab2.hide();
        fab2.setClickable(true);
        CoordinatorLayout coordinatorLayoutView = (CoordinatorLayout) cordLayout.findViewById(R.id.main_content3);
        Snackbar.make(coordinatorLayoutView, R.string.uninstalled, Snackbar.LENGTH_LONG)
                .setAction(R.string.Reboot, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Commands.reboot(getActivity());
                    }
                })
                .show();

        new LoadAndSet().execute();
        mCab.finish();
        ActivityCompat.invalidateOptionsMenu(getActivity());
    }

    private void loadToolbarRecylcerViewFab() {

        mLinearLayout = (LinearLayout) cordLayout.findViewById(R.id.cardList);

        fab2 = (android.support.design.widget.FloatingActionButton) cordLayout.findViewById(R.id.fab6);
        fab2.hide();
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab2.setClickable(false);
                AsyncUninstallOverlays();
            }
        });
    }

    private void AsyncUninstallOverlays() {

        ArrayList<String> paths = new ArrayList<>();
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                paths.add("/system/vendor/overlay/" + ((FileBean) checkBox.getTag()).getLocation());
            }
        }

        Commands.UnInstallOverlays asyncTask = new Commands.UnInstallOverlays(paths, getActivity(), this);
        asyncTask.execute();
    }

    //Check and Uncheck all Checkboxes
    private void checkAll() {

        for (CheckBox checkBox : checkBoxes) {
            if (!checkBox.isChecked()) {
                checkBox.performClick();
            }
        }

        refreshFab();
    }

    private void UncheckAll(boolean calledFromCabFinished) {

        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                checkBox.performClick();
            }
        }

        if (calledFromCabFinished) {
            fab2.hide();
        } else {
            refreshFab();
        }

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
        UncheckAll(true);
        return true;
    }

    //Overflow Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overflow, menu);
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
            case R.id.menu_reboot:
                Commands.reboot(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class LoadAndSet extends AsyncTask<Void, Void, List<FileBean>> {

        @Override
        protected List<FileBean> doInBackground(Void... params) {

            List<FileBean> files = new ArrayList<>();

            ArrayList<String> loadedFiles = new ArrayList<>();

            loadedFiles.addAll(Commands.RootloadFiles(getActivity(), getActivity(), "/system/vendor/overlay"));

            for (String file : loadedFiles) {
                files.add(new FileBean(file));
            }

            return files;

        }

        @Override
        protected void onPostExecute(List<FileBean> result) {

            checkBoxes.clear();
            mLinearLayout.removeAllViews();

            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

            layoutParams.topMargin = 8;

            for (FileBean fileBean : result) {

                TableRow row = new TableRow(getActivity());
                row.setLayoutParams(layoutParams);

                CheckBox check = new CheckBox(getActivity());

                check.setText(fileBean.getName());
                check.setTag(fileBean);

                row.addView(check);

                mLinearLayout.addView(row);

                final FileBean finalFileBean = fileBean;

                check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        finalFileBean.setChecked(!finalFileBean.isChecked());
                        refreshFab();
                    }
                });

                checkBoxes.add(check);

            }


            ImageView noOverlays = (ImageView) cordLayout.findViewById(R.id.imageView);
            TextView noOverlaysText = (TextView) cordLayout.findViewById(R.id.textView7);
            if (result.isEmpty()) {
                noOverlays.setVisibility(View.VISIBLE);
                noOverlaysText.setVisibility(View.VISIBLE);
            }

            refreshFab();
        }
    }

    private void refreshFab() {

        int checkedItems = 0;

        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                checkedItems++;
            }
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

        mCab.setTitle(checkedItems + " " + getResources().getString(R.string.OverlaysSelected));


        if (checkedItems > 0) {
            fab2.show();
        } else {
            if (mCab != null) {
                mCab.finish();
                mCab = null;
            }
            fab2.hide();
        }
    }
}
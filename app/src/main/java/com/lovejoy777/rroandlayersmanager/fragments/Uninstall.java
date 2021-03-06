package com.lovejoy777.rroandlayersmanager.fragments;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import com.afollestad.materialcab.MaterialCab;
import com.bitsyko.liblayers.layerfiles.SimpleOverlay;
import com.lovejoy777.rroandlayersmanager.AsyncResponse;
import com.lovejoy777.rroandlayersmanager.DeviceSingleton;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.beans.FileBean;
import com.lovejoy777.rroandlayersmanager.commands.Commands;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class Uninstall extends Fragment implements MaterialCab.Callback, AsyncResponse {

    private MaterialCab mCab = null;
    private CoordinatorLayout cl_root = null;
    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();
    private  TextView tv_toolbarTitle;


    @Bind(R.id.ll_uninstall_installedOverlayList) LinearLayout ll_installedOverlays;

    @Bind(R.id.fab_uninstall_delete) FloatingActionButton fab_uninstall;

    @Bind(R.id.iv_uninstall_noOverlays) ImageView iv_noOverlays;
    @Bind(R.id.tv_uninstall_noOverlays) TextView tv_noOverlays;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cl_root = (CoordinatorLayout) inflater.inflate(R.layout.fragment_delete, container, false);
        ButterKnife.bind(this, cl_root);

        //Drawer
        NavigationView navigationView = ButterKnife.findById(getActivity(), R.id.navigationView_menu);
        navigationView.getMenu().getItem(1).setChecked(true);
        //Toolbar
        android.support.v7.widget.Toolbar toolbar = ButterKnife.findById(getActivity(),R.id.toolbar_fragmentContainer);
        int elevation = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 156, getResources().getDisplayMetrics());
        toolbar.setNavigationIcon(R.drawable.ic_menu_menu_white_24dp);
        tv_toolbarTitle = ButterKnife.findById(getActivity(),R.id.tv_fragmentContainer_toolbarTitle);
        tv_toolbarTitle.setText(getString(R.string.uninstall_toolbar_title));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height
        );
        toolbar.setElevation(elevation);
        toolbar.setLayoutParams(layoutParams);

        setHasOptionsMenu(true);

        loadToolbarRecylcerViewFab();

        new LoadAndSet().execute();

        return cl_root;
    }

    @Override
    public void processFinish() {
        fab_uninstall.hide();
        fab_uninstall.setClickable(true);
        Snackbar.make(cl_root, R.string.uninstall_snackbar_uninstallFinished, Snackbar.LENGTH_LONG)
                .setAction(R.string.commands_rebootdialog_title, new View.OnClickListener() {
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

        fab_uninstall.hide();
        fab_uninstall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab_uninstall.setClickable(false);
                AsyncUninstallOverlays();
            }
        });
    }

    private void AsyncUninstallOverlays() {

        ArrayList<String> paths = new ArrayList<>();
        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                paths.add(DeviceSingleton.getInstance().getOverlayFolder() + "/" + ((FileBean) checkBox.getTag()).getLocation());
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
            fab_uninstall.hide();
        } else {
            refreshFab();
        }

    }

    //CAB methods
    @Override
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        tv_toolbarTitle.setText("");
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
        tv_toolbarTitle.setText(getString(R.string.uninstall_toolbar_title));
        return true;
    }

    //Overflow Menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_uninstall, menu);
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

            Collection<File> files = FileUtils.listFiles(new File(DeviceSingleton.getInstance().getOverlayFolder()), new String[]{"apk"}, false);

            List<FileBean> fileBeans = new ArrayList<>();

            for (File file : files) {
                fileBeans.add(new FileBean(file));
            }

            Collections.sort(fileBeans, new Comparator<FileBean>() {
                @Override
                public int compare(FileBean lhs, FileBean rhs) {
                    return lhs.getName().compareTo(rhs.getName());
                }
            });

            return fileBeans;

        }

        @Override
        protected void onPostExecute(List<FileBean> result) {

            checkBoxes.clear();
            ll_installedOverlays.removeAllViews();

            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);

            layoutParams.topMargin = 12;

            for (FileBean fileBean : result) {

                TableRow row = new TableRow(getActivity());
                row.setLayoutParams(layoutParams);

                // Implemented LinearLayout because a TableRow can't have more than one View added to it. LinearLayout can have, so we add a
                // LinearLayout to TableRow
                LinearLayout rowlayout = new LinearLayout(getActivity());
                rowlayout.setOrientation(LinearLayout.VERTICAL);

                CheckBox check = new CheckBox(getActivity());

                TextView summary = new TextView(getActivity());

                check.setText(fileBean.getName());
                summary.setText(new SimpleOverlay(fileBean.getFile()).getRelatedPackage());

                check.setTag(fileBean);
                check.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                summary.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
                check.setPadding(padding, 0, padding, 0);
                check.setMinHeight(0);
                summary.setPadding(5 * padding, 0, padding, 0);

                rowlayout.addView(check);
                rowlayout.addView(summary);

                row.addView(rowlayout);

                ll_installedOverlays.addView(row);

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

            if (result.isEmpty()) {
                iv_noOverlays.setVisibility(View.VISIBLE);
                tv_noOverlays.setVisibility(View.VISIBLE);
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
                    .setCloseDrawableRes(R.drawable.ic_menu_done_white_24dp)
                    .setMenu(R.menu.menu_uninstall)
                    .start(Uninstall.this);
        } else if (!mCab.isActive()) {
            mCab
                    .reset().start(Uninstall.this)
                    .setCloseDrawableRes(R.drawable.ic_menu_done_white_24dp)
                    .setMenu(R.menu.menu_uninstall);
        }

        mCab.setTitle(checkedItems + " " + getResources().getString(R.string.uninstall_toolbar_title_overlaysselected));


        if (checkedItems > 0) {
            fab_uninstall.show();
        } else {
            if (mCab != null) {
                mCab.finish();
                mCab = null;
            }
            fab_uninstall.hide();
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}
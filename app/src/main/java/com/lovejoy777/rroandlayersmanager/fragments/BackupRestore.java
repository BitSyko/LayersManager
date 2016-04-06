package com.lovejoy777.rroandlayersmanager.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.*;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import com.lovejoy777.rroandlayersmanager.DeviceSingleton;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.Utils;
import com.lovejoy777.rroandlayersmanager.commands.Commands;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BackupRestore extends Fragment {

    private ArrayList<String> files = new ArrayList<>();
    private CoordinatorLayout cl_root = null;

    @Bind(R.id.rv_backupRestore_backupList) RecyclerView rv_backupList;

    @OnClick(R.id.fab_backupRestore_newBackup)
        void onClick(){
            showNewBackupDialog();
        }

    @Bind(R.id.iv_backupRestore_noBackups) ImageView iv_noBackups;
    @Bind(R.id.tv_backupRestore_noBackups) TextView tv_noBackups;



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cl_root = (CoordinatorLayout) inflater.inflate(R.layout.fragment_backuprestore, container, false);
        ButterKnife.bind(this, cl_root);

        //Toolbar
        android.support.v7.widget.Toolbar toolbar = ButterKnife.findById (getActivity(),R.id.toolbar_fragmentContainer);
        int elevation = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 156, getResources().getDisplayMetrics());
        toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        TextView tv_toolbarTitle = ButterKnife.findById(getActivity(),R.id.tv_fragmentContainer_toolbarTitle);
        tv_toolbarTitle.setText(getString(R.string.BackupRestore));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height
        );
        toolbar.setElevation(elevation);
        toolbar.setLayoutParams(layoutParams);
        //NavigationView
        NavigationView navigationView = ButterKnife.findById(getActivity(), R.id.navigationView_menu);
        navigationView.getMenu().getItem(2).setChecked(true);
        //Permissions
        if (ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            askForPermission(1);
        } else {
            new LoadAndSet().execute();
        }

        rv_backupList.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv_backupList.setItemAnimator(new DefaultItemAnimator());

        setHasOptionsMenu(true);

        return cl_root;
    }


    void showNewBackupDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        final EditText input = new EditText(getActivity());
        alert.setTitle(R.string.backupInstalledOverlays);
        alert.setView(input);
        input.setHint(R.string.enterBackupName);

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        // get editText String
                        String backupName = input.getText().toString().replace(" ", "");

                        if (backupName.length() <= 1) {
                            Toast.makeText(getActivity(), R.string.noInputName, Toast.LENGTH_LONG).show();
                        } else {
                            File overlayFolder = new File(DeviceSingleton.getInstance().getOverlayFolder());

                            if (overlayFolder.exists()){
                                File[] overlays = overlayFolder.listFiles();
                                // Folder is empty
                                if (overlays == null || overlays.length == 0) {
                                    Toast.makeText(getActivity(), R.string.nothingToBackup, Toast.LENGTH_LONG).show();
                                } else {
                                    // CREATES /SDCARD/OVERLAYS/BACKUP/BACKUPNAME
                                    String backupDirectory = Environment.getExternalStorageDirectory() + "/Overlays/Backup/";
                                    File dir2 = new File(backupDirectory+ backupName);
                                    if (!dir2.exists() && !dir2.isDirectory()) {
                                        System.out.println("MKDIR");
                                        dir2.mkdir();
                                    }
                                    //Async Task to backup Overlays
                                    new BackupOverlays().execute(backupName);
                                }
                            } else {
                                Toast.makeText(getActivity(), R.string.nothingToBackup, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
        );

        alert.setNegativeButton(android.R.string.cancel, null);
        alert.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reboot:
                Commands.reboot(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu);
    }

    //Adapter
    private class BackupRestore_CardViewAdapter extends RecyclerView.Adapter<BackupRestore_CardViewAdapter.ViewHolder> {

        private ArrayList<String> themes;
        private int rowLayout;

        public BackupRestore_CardViewAdapter(ArrayList<String> themes, int rowLayout) {
            this.themes = themes;
            this.rowLayout = rowLayout;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {

            final String layerBackupName = themes.get(i);

            viewHolder.backupName.setText(layerBackupName);
            viewHolder.backupName.setId(i);
            viewHolder.backupName.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    AlertDialog.Builder installdialog = new AlertDialog.Builder(getActivity());
                    installdialog.setTitle(layerBackupName);
                    installdialog.setMessage(Html.fromHtml(getResources().getString(R.string.DoYouWantToRestore)));
                    installdialog.setPositiveButton(R.string.restore, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new Commands.InstallZipBetterWay(BackupRestore.this.getActivity(), null)
                                    .execute(Environment.getExternalStorageDirectory() + "/Overlays/Backup/" + layerBackupName + "/overlay.zip");
                        }
                    });
                    installdialog.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new DeleteBackup().execute(layerBackupName);
                        }
                    });
                    installdialog.setNeutralButton(R.string.show, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder contentDialog = new AlertDialog.Builder(getActivity());
                            contentDialog.setTitle(layerBackupName + " " + getString(R.string.contains));
                            try {

                                String overlays = "";

                                ArrayList<String> files = Commands.fileNamesFromZip(new File(Environment.getExternalStorageDirectory() + "/Overlays/Backup/" + layerBackupName + "/overlay.zip"));

                                for (int i = 0; i < files.size(); i++) {
                                    if (i == 0) {
                                        overlays = files.get(i);
                                    } else {
                                        overlays = overlays + "\n" + files.get(i);
                                    }
                                }

                                contentDialog.setMessage(overlays.replace(".apk", "").replaceAll("_", " "));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            contentDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            contentDialog.show();
                        }
                    });
                    installdialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return themes.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView backupName;


            public ViewHolder(View itemView) {
                super(itemView);
                backupName = ButterKnife.findById(itemView,R.id.tv_aboutadapter_title);
            }
        }
    }

    private class DeleteBackup extends AsyncTask<String, String, Void> {
        ProgressDialog progressBackup;

        protected void onPreExecute() {

            progressBackup = ProgressDialog.show(getActivity(), getString(R.string.DeleteBackup),
                    getString(R.string.deleting) + "...", true);
        }

        @Override
        protected Void doInBackground(String... params) {
            String backupName = params[0];
            backupName = Environment.getExternalStorageDirectory() + "/Overlays/Backup/" + backupName;

            try {
                FileUtils.deleteDirectory(new File(backupName));
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;

        }

        protected void onPostExecute(Void result) {

            progressBackup.dismiss();
            Snackbar.make(cl_root, R.string.deletedBackup, Snackbar.LENGTH_LONG)
                    .show();
            new LoadAndSet().execute();
        }
    }

    private class BackupOverlays extends AsyncTask<String, String, Void> {
        ProgressDialog progressBackup;

        protected void onPreExecute() {

            progressBackup = ProgressDialog.show(getActivity(), getString(R.string.BackupOverlays),
                    getString(R.string.backingUp) + "...", true);
        }

        @Override
        protected Void doInBackground(String... params) {
            Utils.remount("rw");
            String backupName = params[0];
            String sdOverlays = Environment.getExternalStorageDirectory() + "/Overlays";
            zipFolder(DeviceSingleton.getInstance().getOverlayFolder(), sdOverlays + "/Backup/" + backupName + "/overlay.zip");
            Utils.remount("ro");
            return null;

        }

        protected void onPostExecute(Void result) {

            progressBackup.dismiss();
            Snackbar.make(cl_root, R.string.backupComplete, Snackbar.LENGTH_LONG)
                    .show();
            new LoadAndSet().execute();
        }
    }

    private class LoadAndSet extends AsyncTask<String, String, Void> {


        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... params) {

            files.clear();
            files = Commands.loadFolders(Environment.getExternalStorageDirectory() + "/Overlays/Backup");

            return null;

        }

        protected void onPostExecute(Void result) {

            BackupRestore_CardViewAdapter mAdapter = new BackupRestore_CardViewAdapter(files, R.layout.adapter_backups);
            rv_backupList.setAdapter(mAdapter);
            if (files.size() ==0) {
                iv_noBackups.setVisibility(View.VISIBLE);
                tv_noBackups.setVisibility(View.VISIBLE);
            }
        }
    }

    public void askForPermission(int mode) {
        // Should we show an explanation?
        if (FragmentCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            // Explain to the user why we need to read the contacts
        }

        FragmentCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, mode);

        return;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    new LoadAndSet().execute();

                } else {

                    AlertDialog.Builder noPermissionDialog = new AlertDialog.Builder(getActivity());
                    noPermissionDialog.setTitle(R.string.noPermission);
                    noPermissionDialog.setMessage(R.string.noPermissionDescription);
                    noPermissionDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().onBackPressed();
                        }
                    });
                    noPermissionDialog.show();

                }
                return;
            }
        }
    }

    private static void zipFolder(String inputFolderPath, String outZipPath) {

        try {
            FileOutputStream fos = new FileOutputStream(outZipPath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            File srcFile = new File(inputFolderPath);
            File[] files = srcFile.listFiles();
            Log.d("", "Zip directory: " + srcFile.getName());
            for (File file : files) {
                Log.d("", "Adding file: " + file.getName());
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(file);
                zos.putNextEntry(new ZipEntry(file.getName()));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
        } catch (IOException ioe) {
            Log.e("", ioe.getMessage());
        }
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}

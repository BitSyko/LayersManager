package com.lovejoy777.rroandlayersmanager.fragments;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.*;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bitsyko.liblayers.Layer;
import com.lovejoy777.rroandlayersmanager.AsyncResponse;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.Utils;
import com.lovejoy777.rroandlayersmanager.adapters.CardViewAdapter;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.helper.RecyclerItemClickListener;
import com.lovejoy777.rroandlayersmanager.menu;

import java.util.*;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PluginList extends Fragment implements AsyncResponse {

    CardViewAdapter cardViewAdapter = null;
    public int sortMode;
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Remove swiped item from list and notify the RecyclerView
            String packageName = cardViewAdapter.getLayerFromPosition(viewHolder.getAdapterPosition()).getPackageName();
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            startActivityForResult(uninstallIntent, 1);
        }
    };
    private Boolean noPluginsInstalled = false;
    private CoordinatorLayout cl_root = null;

    @Bind(R.id.rv_backupRestore_backupList) RecyclerView rv_installedOverlaysList;

    @OnClick(R.id.fab_pluginList_install)
        void onClick(){
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                askForPermission(1);
            } else{
                openFileManager();
            }
        }

    @Bind(R.id.srl_pluginList) SwipeRefreshLayout swipeRefreshLayout;



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        cl_root = (CoordinatorLayout) inflater.inflate(R.layout.fragment_pluginlist, container, false);
        ButterKnife.bind(this, cl_root);

        //Drawer
        DrawerLayout drawerLayout = ButterKnife.findById(getActivity(), R.id.drawerLayout_fragmentContainer);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        NavigationView navigationView = ButterKnife.findById(getActivity(), R.id.navigationView_menu);
        navigationView.getMenu().getItem(0).setChecked(true);

        //Toolbar
        Toolbar toolbar = ButterKnife.findById(getActivity(),R.id.toolbar_fragmentContainer);
        TextView tv_toolbarTitle = ButterKnife.findById(getActivity(),R.id.tv_fragmentContainer_toolbarTitle);
        tv_toolbarTitle.setText(getString(R.string.pluginlist_toolbar_title));
        int elevation = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());
        toolbar.setNavigationIcon(R.drawable.ic_menu_menu_white_24dp);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height
        );
        toolbar.setElevation(elevation);
        toolbar.setLayoutParams(layoutParams);

        LoadRecyclerViewFabToolbar();

        sortMode = Commands.getSortMode(getActivity());

        new fillPluginList().execute();

        setHasOptionsMenu(true);

        return cl_root;
    }

    private void LoadRecyclerViewFabToolbar() {
        //create RecyclerView
        rv_installedOverlaysList.setHasFixedSize(true);
        rv_installedOverlaysList.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        onListItemClick(position);
                    }
                })
        );

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv_installedOverlaysList.setLayoutManager(llm);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(rv_installedOverlaysList);

        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new fillPluginList().execute();
            }
        });
    }

    //create list if no plugins are installed
    private List<Layer> createList2() {

        List<Layer> result = new ArrayList<>();
        result.add(new Layer(getString(R.string.pluginList_noplugincard_title), getString(R.string.pluginList_noplugincard_description), getResources().getDrawable(R.drawable.ic_noplugin, null)));
        result.add(new Layer(getString(R.string.pluginList_showcasecard_title), getString(R.string.pluginList_showcasecard_description), getResources().getDrawable(R.mipmap.ic_launcher, null)));
        result.add(new Layer(getString(R.string.pluginList_playstorecard_title), getString(R.string.pluginList_playstorecard_description), getResources().getDrawable(R.drawable.playstore, null)));
        return result;
    }


    //open Plugin page after clicked on a cardview
    protected void onListItemClick(int position) {
        if (!noPluginsInstalled) {
            ((menu) getActivity()).changeFragment2(cardViewAdapter.getLayerFromPosition(position));
        } else {
            //PlayStore
            if (position == 2) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=Layers+Theme&c=apps&docType=1&sp=CAFiDgoMTGF5ZXJzIFRoZW1legIYAIoBAggB:S:ANO1ljK_ZAY")));
            }
            //Showcase
            if (position == 1) {
                Bundle bndlanimation =
                        ActivityOptions.makeCustomAnimation(getActivity().getApplicationContext(), R.anim.anni1, R.anim.anni2).toBundle();
                boolean installed = Commands.appInstalledOrNot(getActivity(), "com.lovejoy777.showcase");
                if (installed) {
                    //This intent will help you to launch if the package is already installed
                    Intent intent = new Intent();
                    intent.setComponent(new ComponentName("com.lovejoy777.showcase", "com.lovejoy777.showcase.MainActivity1"));
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), "Please install the layers showcase plugin", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.lovejoy777.showcase")), bndlanimation);
                }
            }

        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            new fillPluginList().execute();
        }
        if (requestCode == 2  && data != null && (data.getData() != null || data.getClipData() != null)) {

            ArrayList<String> paths = new ArrayList<>();


            //IF multiple files selected
            if (data.getClipData() != null){
                ClipData clipdata = data.getClipData();
                for (int i=0; i<clipdata.getItemCount();i++) {
                    String path = Utils.getPath(getActivity(), clipdata.getItemAt(i).getUri());
                    if (path.endsWith(".apk") || path.endsWith(".zip")) {
                        paths.add(path);
                    }
                    else {
                        Toast.makeText(getActivity(),"File "+ path +" is not supported",Toast.LENGTH_SHORT).show();
                    }
                }

                if (paths.size() != 0) {
                    new Commands.InstallZipBetterWay(getActivity(), this).execute(paths.toArray(new String[paths.size()]));
                }
            }
            else {
                Uri uri = data.getData();
                String path = Utils.getPath(getActivity(), uri);
                System.out.println(Utils.getMimeType(path));
                if (path.endsWith(".apk") || path.endsWith(".zip")){
                    paths.add(Utils.getPath(getActivity(), uri));
                    new Commands.InstallZipBetterWay(getActivity(), this).execute(paths.toArray(new String[paths.size()]));
                }else {
                        Toast.makeText(getActivity(),"File type not supported: "+Utils.getMimeType(path),Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_pluginlist, menu);
        switch (sortMode) {
            default:
                menu.findItem(R.id.menu_sortName).setChecked(true);
                break;
            case 2:
                menu.findItem(R.id.menu_sortDeveloper).setChecked(true);
                break;
            case 3:
                menu.findItem(R.id.menu_sortRandom).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reboot:
                Commands.reboot(getActivity());
                break;
            case R.id.menu_sortName:
                item.setChecked(true);
                Commands.setSortMode(getActivity(), 1);
                new fillPluginList().execute();
                break;
            case R.id.menu_sortDeveloper:
                item.setChecked(true);
                Commands.setSortMode(getActivity(), 2);
                new fillPluginList().execute();
                break;
            case R.id.menu_sortRandom:
                item.setChecked(true);
                Commands.setSortMode(getActivity(), 3);
                new fillPluginList().execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void processFinish() {
        Snackbar.make(cl_root, R.string.pluginlist_snackbar_installationFinished, Snackbar.LENGTH_LONG)
                .setAction(R.string.commands_rebootdialog_title, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Commands.reboot(getActivity());
                    }
                })
                .show();
    }

    private class fillPluginList extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            swipeRefreshLayout.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... params) {

            List<Layer> layerList = Layer.getLayersInSystem(PluginList.this.getActivity());

            sortMode = Commands.getSortMode(getActivity());
            if (sortMode == 1 || sortMode == 0) {
                //Alphabetically NAME
                Collections.sort(layerList, new Comparator<Layer>() {
                    public int compare(Layer layer1, Layer layer2) {
                        return layer1.getName().compareToIgnoreCase(layer2.getName());
                    }
                });
            }
            if (sortMode == 2) {
                //Alphabetically DEVELOPER
                Collections.sort(layerList, new Comparator<Layer>() {
                    public int compare(Layer layer1, Layer layer2) {
                        return layer1.getDeveloper().compareToIgnoreCase(layer2.getDeveloper());
                    }
                });
            }
            if (sortMode == 3) {
                //RANDOM
                long seed = System.nanoTime();
                Collections.shuffle(layerList, new Random(seed));
                Collections.shuffle(layerList, new Random(seed));

            }

            if (layerList.size() > 0) {
                cardViewAdapter = new CardViewAdapter(layerList);
            } else {
                cardViewAdapter = new CardViewAdapter(createList2());
                noPluginsInstalled = true;
            }

            return null;

        }

        protected void onPostExecute(Void result) {
            rv_installedOverlaysList.setHasFixedSize(true);
            rv_installedOverlaysList.setAdapter(cardViewAdapter);
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    public void askForPermission(int mode){
        // Should we show an explanation?
        if (FragmentCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)) {
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
                    openFileManager();
                } else {
                    AlertDialog.Builder noPermissionDialog = new AlertDialog.Builder(getActivity());
                    noPermissionDialog.setTitle(R.string.permission_nopermissiondialog_title);
                    noPermissionDialog.setMessage(R.string.permission_nopermissiondialog_message);
                    noPermissionDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    noPermissionDialog.show();
                }
                return;
            }
        }
    }

    private void openFileManager() {

        Intent chooseFiles = new Intent();
        chooseFiles.setAction(Intent.ACTION_GET_CONTENT);
        chooseFiles.setType("*/*");
        chooseFiles.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(chooseFiles,"Choose Overlays"),2);
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }
}

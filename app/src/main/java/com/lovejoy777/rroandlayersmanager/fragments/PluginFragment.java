package com.lovejoy777.rroandlayersmanager.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bitsyko.liblayers.Layer;

import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.adapters.CardViewAdapter;
import com.lovejoy777.rroandlayersmanager.beans.CardBean;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.helper.RecyclerItemClickListener;
import com.lovejoy777.rroandlayersmanager.menu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class PluginFragment extends Fragment {

    private PackageBroadcastReceiver packageBroadcastReceiver;
    private IntentFilter packageFilter;
    private ArrayList<HashMap<String, String>> services;
    private ArrayList<String> categories;
    private String[] packages = new String[100];

    public static final String ACTION_PICK_PLUGIN = "com.layers.plugins.PICK_OVERLAYS";
    static final String KEY_PKG = "pkg";
    static final String KEY_SERVICENAME = "servicename";
    static final String KEY_ACTIONS = "actions";
    static final String KEY_CATEGORIES = "categories";
    private Boolean TestBoolean = false;
    static final String LOG_TAG = "PluginApp";
    RecyclerView recList = null;
    CardViewAdapter ca = null;
    static final String BUNDLE_EXTRAS_CATEGORY = "category";
    static final String BUNDLE_EXTRAS_PACKAGENAME = "packageName";
    private CoordinatorLayout cordLayout = null;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FragmentActivity faActivity = (FragmentActivity) super.getActivity();
        cordLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_plugins, container, false);


        LoadRecyclerViewFabToolbar();

        //createImportantDirectories();

        new fillPluginList().execute();

        packageBroadcastReceiver = new PackageBroadcastReceiver();
        packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        packageFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        packageFilter.addCategory(Intent.CATEGORY_DEFAULT);
        packageFilter.addDataScheme("package");

        setHasOptionsMenu(true);

        return cordLayout;
    }

    private void LoadRecyclerViewFabToolbar() {
        //create RecyclerView
        RecyclerView recyclerCardViewList = (RecyclerView) cordLayout.findViewById(R.id.cardList);
        recyclerCardViewList.setHasFixedSize(true);
        recyclerCardViewList.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        onListItemClick(position);
                    }
                })
        );

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerCardViewList.setLayoutManager(llm);


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerCardViewList);

        //create FAB
        FloatingActionButton fab = (FloatingActionButton) cordLayout.findViewById(R.id.fab3);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent Installactivity = new Intent(getActivity(), Install.class);

                //Bundle bndlanimation =
                //        ActivityOptions.makeCustomAnimation(getActivity().getApplicationContext(), R.anim.anni1, R.anim.anni2).toBundle();
                //startActivity(Installactivity, bndlanimation);
                ((menu) getActivity()).changeFragment(4,0);
            }


        });

        final SwipeRefreshLayout mSwipeRefresh = (SwipeRefreshLayout) cordLayout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefresh.setColorSchemeResources(R.color.accent);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                services.clear();
                new fillPluginList().execute();
                onItemsLoadComplete();
            }

            void onItemsLoadComplete() {
                ca.notifyDataSetChanged();
                mSwipeRefresh.setRefreshing(false);
            }
        });


        //Toolbar toolbar = (Toolbar) cordLayout.findViewById(R.id.toolbar);
        //toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        //AppCompatActivity activity = (AppCompatActivity) getActivity();
        //activity.setSupportActionBar(toolbar);
        //activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Remove swiped item from list and notify the RecyclerView
            //System.out.println(viewHolder.getAdapterPosition());
            String packageName = packages[viewHolder.getAdapterPosition()];
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            startActivityForResult(uninstallIntent, 1);

        }
    };

    //create list if no plugins are installed
    private List<Layer> createList2() {

        List<Layer> result = new ArrayList<>();
        result.add(new Layer(getString(R.string.tooBad), getString(R.string.noPlugins), getResources().getDrawable(R.drawable.ic_noplugin), null));
        result.add(new Layer(getString(R.string.Showcase), getString(R.string.ShowCaseMore), getResources().getDrawable(R.mipmap.ic_launcher), null));
        result.add(new Layer(getString(R.string.PlayStore), getString(R.string.PlayStoreMore), getResources().getDrawable(R.drawable.playstore), null));
        return result;
    }


    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
        getActivity().registerReceiver(packageBroadcastReceiver, packageFilter);
    }


    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        getActivity().unregisterReceiver(packageBroadcastReceiver);
    }

    //open Plugin page after clicked on a cardview
    protected void onListItemClick(int position) {
        if (!TestBoolean) {
            String package2 = packages[position];
            String category = categories.get(position);
            if (category.length() > 0) {


                 ((menu) getActivity()).changeFragment2(category,package2);

             }
        } else {
            if (position == 2) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.PlaystoreSearch))));
            }
            if (position == 1) {
                NotAvailableSnackbar();
            }

        }
    }


    class PackageBroadcastReceiver extends BroadcastReceiver {
        private static final String LOG_TAG = "PackageBroadcastReceiver";

        //when a new Plugin is installed
        @SuppressLint("LongLogTag")
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive: " + intent);
            services.clear();
            new fillPluginList().execute();
            ca.notifyDataSetChanged();
        }
    }

    private void NotAvailableSnackbar() {
        final View coordinatorLayoutView = cordLayout.findViewById(R.id.main_content2);
        Snackbar.make(coordinatorLayoutView, "Sorry, not available yet.", Snackbar.LENGTH_SHORT)
                .show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == 1) {
            services.clear();
            new fillPluginList().execute();
        }
    }

    private class fillPluginList extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            List<Layer> layerList = Layer.getLayersInSystem(PluginFragment.this.getActivity());

            if (layerList.size() > 0) {
                ca = new CardViewAdapter(layerList);
            } else {
                ca = new CardViewAdapter(createList2());
                TestBoolean = true;
            }

            return null;

        }

        protected void onPostExecute(Void result) {

            recList = (RecyclerView) cordLayout.findViewById(R.id.cardList);
            recList.setHasFixedSize(true);
            recList.setAdapter(ca);
        }
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu);
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
}

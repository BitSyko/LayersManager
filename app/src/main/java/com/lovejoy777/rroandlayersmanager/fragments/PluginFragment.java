package com.lovejoy777.rroandlayersmanager.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.*;
import android.widget.TextView;

import com.bitsyko.ApplicationInfo;
import com.bitsyko.Placeholder;
import com.bitsyko.libicons.IconPack;
import com.bitsyko.liblayers.Layer;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.adapters.CardViewAdapter;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.helper.RecyclerItemClickListener;
import com.lovejoy777.rroandlayersmanager.menu;

import java.util.*;

public class PluginFragment extends Fragment implements AppBarLayout.OnOffsetChangedListener {

    RecyclerView recList = null;
    CardViewAdapter ca = null;
    public int sortMode;
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Remove swiped item from list and notify the RecyclerView
            String packageName = ca.getLayerFromPosition(viewHolder.getAdapterPosition()).getPackageName();
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            startActivityForResult(uninstallIntent, 1);
        }
    };
    private Boolean noOverlays = false;
    private CoordinatorLayout cordLayout = null;
    private SwipeRefreshLayout mSwipeRefresh;
    private Mode mode;

    private enum Mode {
        Layer,
        IconPack
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        cordLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_plugins, container, false);

        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        ((NavigationView) getActivity().findViewById(R.id.nav_view)).getMenu().getItem(0).setChecked(true);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        toolbar.setTitle(getString(R.string.InstallOverlays));

        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.tabanim_viewpager);
        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        viewPager.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.VISIBLE);


        switch (getArguments().getInt("Mode")) {
            case 0:
                mode = Mode.Layer;
                break;
            case 1:
                mode = Mode.IconPack;
                break;
        }

        TextView toolbarTitle = (TextView) getActivity().findViewById(R.id.title2);
        toolbarTitle.setText("");

        int elevation = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());


        AppBarLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height
        );

        toolbar.setElevation(elevation);
        toolbar.setLayoutParams(layoutParams);

        LoadRecyclerViewFabToolbar();

        sortMode = Commands.getSortMode(getActivity());

        refreshList();

        setHasOptionsMenu(true);

        return cordLayout;
    }


    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            mSwipeRefresh.setEnabled(true);
        } else {
            mSwipeRefresh.setEnabled(false);
        }
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
                ((menu) getActivity()).changeFragment(4, 0);
            }
        });

        mSwipeRefresh = (SwipeRefreshLayout) cordLayout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefresh.setColorSchemeResources(R.color.accent);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshList();
            }
        });
    }

    //create list if no plugins are installed
    private List<Placeholder> createList2() {

        List<Placeholder> result = new ArrayList<>();
        result.add(new Placeholder(getString(R.string.tooBad), getString(R.string.noPlugins), getResources().getDrawable(R.drawable.ic_noplugin, null)));
        result.add(new Placeholder(getString(R.string.Showcase), getString(R.string.ShowCaseMore), getResources().getDrawable(R.mipmap.ic_launcher, null)));
        result.add(new Placeholder(getString(R.string.PlayStore), getString(R.string.PlayStoreMore), getResources().getDrawable(R.drawable.playstore, null)));
        return result;
    }


    //open Plugin page after clicked on a cardview
    protected void onListItemClick(int position) {
        if (!noOverlays) {
            if (mode == Mode.Layer) {
                ((menu) getActivity()).openOverlayDetailActivity((Layer) ca.getLayerFromPosition(position));
            } else if (mode == Mode.IconPack) {
                ((menu) getActivity()).openIconPackDetailActivity((IconPack) ca.getLayerFromPosition(position));
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

    private void NotAvailableSnackbar() {
        final View coordinatorLayoutView = cordLayout.findViewById(R.id.main_content2);
        Snackbar.make(coordinatorLayoutView, "Sorry, not available yet.", Snackbar.LENGTH_SHORT)
                .show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            refreshList();
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
                refreshList();
                break;
            case R.id.menu_sortDeveloper:
                item.setChecked(true);
                Commands.setSortMode(getActivity(), 2);
                refreshList();
                break;
            case R.id.menu_sortRandom:
                item.setChecked(true);
                Commands.setSortMode(getActivity(), 3);
                refreshList();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void refreshList() {

        switch (mode) {
            case Layer:
                new FillPluginList().execute();
                break;
            case IconPack:
                new FillIconPackList().execute();
                break;
            default:
                throw new RuntimeException("Mode not selected");
        }

    }


    private abstract class LoadStuff extends AsyncTask<Void, Void, List<? extends ApplicationInfo>> {

        protected void onPreExecute() {
            mSwipeRefresh.setRefreshing(true);
        }


        protected void onPostExecute(List<? extends ApplicationInfo> result) {

            if (result.size() > 0) {
                ca = new CardViewAdapter(result);
            } else {
                ca = new CardViewAdapter(createList2());
                noOverlays = true;
            }

            recList = (RecyclerView) cordLayout.findViewById(R.id.cardList);
            recList.setHasFixedSize(true);
            recList.setAdapter(ca);
            mSwipeRefresh.setRefreshing(false);
        }

    }


    private class FillPluginList extends LoadStuff {

        @Override
        protected List<? extends ApplicationInfo> doInBackground(Void... params) {

            List<Layer> layerList = Layer.getLayersInSystem(PluginFragment.this.getActivity());

            sortMode = Commands.getSortMode(getActivity());
            if (sortMode == 1) {
                //Alphabetically NAME
                Collections.sort(layerList, ApplicationInfo.compareName);
            } else if (sortMode == 2) {
                //Alphabetically DEVELOPER
                Collections.sort(layerList, ApplicationInfo.compareDev);
            } else if (sortMode == 3) {
                //RANDOM
                Collections.shuffle(layerList, new Random());
            }

            return layerList;

        }

    }

    private class FillIconPackList extends LoadStuff {

        @Override
        protected List<? extends ApplicationInfo> doInBackground(Void... params) {

            List<IconPack> layerList = IconPack.getIconPacksInSystem(PluginFragment.this.getActivity());

            sortMode = Commands.getSortMode(getActivity());

            //We don't have developer in icon pack
            if (sortMode == 1 || sortMode == 2) {
                //Alphabetically NAME
                Collections.sort(layerList, ApplicationInfo.compareName);
            } else if (sortMode == 3) {
                //RANDOM
                Collections.shuffle(layerList, new Random());
            }

            return layerList;
        }

    }

}


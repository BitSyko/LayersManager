package com.lovejoy777.rroandlayersmanager.fragments;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lovejoy777.rroandlayersmanager.OverlayDetailActivity;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.adapters.CardViewAdapter;
import com.lovejoy777.rroandlayersmanager.helper.CardViewContent;
import com.lovejoy777.rroandlayersmanager.helper.RecyclerItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.lovejoy777.rroandlayersmanager.menu;

/**
 * Created by Niklas on 07.07.2015.
 */

public class PluginFragment extends Fragment {

    private PackageBroadcastReceiver packageBroadcastReceiver;
    private IntentFilter packageFilter;
    private ArrayList<HashMap<String,String>> services;
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


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        FragmentActivity faActivity  = (FragmentActivity)    super.getActivity();
        cordLayout = (CoordinatorLayout)    inflater.inflate(R.layout.fragment_plugins, container, false);


        LoadRecyclerViewFabToolbar();

        //createImportantDirectories();

        new fillPluginList().execute();

        packageBroadcastReceiver = new PackageBroadcastReceiver();
        packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction( Intent.ACTION_PACKAGE_REPLACED );
        packageFilter.addAction( Intent.ACTION_PACKAGE_REMOVED );
        packageFilter.addCategory( Intent.CATEGORY_DEFAULT );
        packageFilter.addDataScheme( "package" );

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
                ((menu) getActivity()).changeFragment(4);
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
            void onItemsLoadComplete(){
                ca.notifyDataSetChanged();
                mSwipeRefresh.setRefreshing(false);
            }
        });


        Toolbar toolbar = (Toolbar) cordLayout.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
            Uri packageURI = Uri.parse("package:"+packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            startActivityForResult(uninstallIntent, 1);

        }
    };

    //create List with all Plugins
    private List createList(int size, String name[], String developer[], String packages[]) {

        List result = new ArrayList();
        for (int i=1; i <= size; i++) {
            CardViewContent ci = new CardViewContent();
            ci.themeName = name[i-1];
            ci.themeDeveloper = developer[i-1];

            final String packName = packages[i-1];
            String mDrawableName = "icon";
            PackageManager manager = getActivity().getPackageManager();
            Resources mApk1Resources = null;
            try {
                mApk1Resources = manager.getResourcesForApplication(packName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            int mDrawableResID = 0;
            if (mApk1Resources != null) {
                mDrawableResID = mApk1Resources.getIdentifier(mDrawableName, "drawable", packName);
            }
            Drawable myDrawable = mApk1Resources.getDrawable(mDrawableResID);

            ci.themeImage = myDrawable ;

            result.add(ci);
        }
        return result;
    }

    //create list if no plugins are installed
    private List createList2(int size, String[] message1, String[] message2) {

        List result = new ArrayList();
        Drawable Image[] = new Drawable[size];
        Drawable myDrawable;
        for (int i=1; i <= size; i++) {
            CardViewContent ci = new CardViewContent();
            ci.message1 = message1[i-1];
            ci.message2 = message2[i-1];
            Image[0] = getResources().getDrawable(R.drawable.ic_noplugin);
            Image[1] = getResources().getDrawable(R.mipmap.ic_launcher);
            Image[2] = getResources().getDrawable(R.drawable.playstore);
            myDrawable = Image[i-1];
            ci.themeImage = myDrawable ;
            result.add(ci);
        }
        return result;
    }


    public void onStart() {
        super.onStart();
               Log.d(LOG_TAG, "onStart");
              getActivity().registerReceiver( packageBroadcastReceiver, packageFilter );
    }


    public void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        getActivity().unregisterReceiver( packageBroadcastReceiver );
    }

    //open Plugin page after clicked on a cardview
    protected void onListItemClick (int position) {
        if (!TestBoolean){
            String package2 = packages[position];
            String category = categories.get(position);
            if( category.length() > 0 ) {



                TransitionSet transitionSet = new TransitionSet();
                transitionSet.addTransition(new ChangeImageTransform());
                transitionSet.addTransition(new ChangeBounds());
                transitionSet.addTransition(new ChangeTransform());
                transitionSet.setDuration(300);

                Fragment fragment2 = new OverlayDetailActivity();
                fragment2.setSharedElementEnterTransition(transitionSet);
                fragment2.setSharedElementReturnTransition(transitionSet);


                Bundle args = new Bundle();
                args.putString(BUNDLE_EXTRAS_CATEGORY, category);
                args.putString(BUNDLE_EXTRAS_PACKAGENAME, package2);
                //fragment = new OverlayDetailActivity();
                FloatingActionButton fab = (FloatingActionButton) cordLayout.findViewById(R.id.fab3);
                fragment2.setArguments(args);
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment2)
                        .addSharedElement(fab, "fab")
                        .addToBackStack("test")
                        .commit();
            }

           //     ((menu) getActivity()).changeFragment2(category,package2);

           // }
        }
        else{
            if(position==2) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.PlaystoreSearch))));
            } if(position==1){
                NotAvailableSnackbar();
            }

        }
    }


    class PackageBroadcastReceiver extends BroadcastReceiver {
        private static final String LOG_TAG = "PackageBroadcastReceiver";

        //when a new Plugin is installed
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

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==1)
        {
            services.clear();
            new fillPluginList().execute();
        }
    }

    private class fillPluginList extends AsyncTask<Void,Void,Void> {

        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(Void... params) {

            services = new ArrayList<HashMap<String,String>>();
            categories = new ArrayList<String>();

            PackageManager packageManager = getActivity().getPackageManager();
            Intent baseIntent = new Intent( ACTION_PICK_PLUGIN );
            baseIntent.setFlags(Intent.FLAG_DEBUG_LOG_RESOLUTION);
            ArrayList<ResolveInfo> list = (ArrayList<ResolveInfo>) packageManager.queryIntentServices(baseIntent,
                    PackageManager.GET_RESOLVED_FILTER );

            final String name[] = new String[list.size()];
            final String developer[] = new String[list.size()];

            for( int i = 0 ; i < list.size() ; ++i ) {

                ResolveInfo info = list.get( i );
                ServiceInfo sinfo = info.serviceInfo;
                IntentFilter filter = info.filter;
                Log.d(LOG_TAG, "fillPluginList: i: " + i + "; sinfo: " + sinfo + ";filter: " + filter);

                ApplicationInfo ai = null;
                try {
                    ai = getActivity().getPackageManager().getApplicationInfo(sinfo.packageName, PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                Bundle bundle = null;
                if (ai != null) {
                    bundle = ai.metaData;
                }
                name[i] = bundle.getString("Layers_Name");
                developer[i] = bundle.getString("Layers_Developer");

                if( sinfo != null ) {
                    HashMap<String,String> item = new HashMap<String,String>();
                    item.put( KEY_PKG, name[i] );
                    item.put( KEY_SERVICENAME, developer[i] );

                    String firstCategory = null;
                    if( filter != null ) {
                        StringBuilder actions = new StringBuilder();
                        for( Iterator<String> actionIterator = filter.actionsIterator() ; actionIterator.hasNext() ; ) {
                            String action = actionIterator.next();
                            if( actions.length() > 0 )
                                actions.append( "," );
                            actions.append( action );
                        }
                        StringBuilder categories = new StringBuilder();
                        for( Iterator<String> categoryIterator = filter.categoriesIterator() ;
                             categoryIterator.hasNext() ; ) {
                            String category = categoryIterator.next();
                            if( firstCategory == null )
                                firstCategory = category;
                            if( categories.length() > 0 )
                                categories.append( "," );
                            categories.append( category );
                        }
                        try {
                            packages[i] = getActivity().getPackageManager().getApplicationInfo(sinfo.packageName,0).packageName;
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        item.put(KEY_ACTIONS, "<null>");
                        item.put(KEY_CATEGORIES, "<null>");
                    }
                    if( firstCategory == null )
                        firstCategory = "";
                    categories.add( firstCategory );
                    services.add( item );
                }
            }

            String Test1[] = new String[3];
            String Test2[] = new String[3];

            Test1[0] = getString(R.string.tooBad);
            Test2[0] = getString(R.string.noPlugins);
            Test1[1] = getString(R.string.Showcase);
            Test2[1] = getString(R.string.ShowCaseMore);
            Test1[2] = getString(R.string.PlayStore);
            Test2[2] = getString(R.string.PlayStoreMore);

            if (list.size()>0) {
                ca = new CardViewAdapter(createList(list.size(), name, developer, packages));
            }else {
                ca = new CardViewAdapter(createList2(3, Test1, Test2));
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
}

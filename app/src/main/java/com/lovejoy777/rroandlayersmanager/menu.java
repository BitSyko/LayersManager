package com.lovejoy777.rroandlayersmanager;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.Toast;


import com.lovejoy777.rroandlayersmanager.actions.Delete;
import com.lovejoy777.rroandlayersmanager.actions.Install;
import com.lovejoy777.rroandlayersmanager.actions.Restore;
import com.lovejoy777.rroandlayersmanager.activities.AboutActivity;
import com.lovejoy777.rroandlayersmanager.activities.DetailedTutorialActivity;
import com.lovejoy777.rroandlayersmanager.activities.Intro;
import com.lovejoy777.rroandlayersmanager.activities.SettingsActivity;
import com.lovejoy777.rroandlayersmanager.adapters.CardViewAdapter;
import com.lovejoy777.rroandlayersmanager.helper.CardViewContent;
import com.lovejoy777.rroandlayersmanager.helper.RecyclerItemClickListener;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class menu extends AppCompatActivity
{

    public static final String ACTION_PICK_PLUGIN = "com.layers.plugins.PICK_OVERLAYS";
    static final String KEY_PKG = "pkg";
    static final String KEY_SERVICENAME = "servicename";
    static final String KEY_ACTIONS = "actions";
    static final String KEY_CATEGORIES = "categories";
    static final String BUNDLE_EXTRAS_CATEGORY = "category";
    static final String BUNDLE_EXTRAS_PACKAGENAME = "packageName";
    private Boolean TestBoolean = false;
    static final String LOG_TAG = "PluginApp";
    private DrawerLayout mDrawerLayout;
    RecyclerView recList = null;
    CardViewAdapter ca = null;
    /** Called when the activity is first created. */




    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!RootTools.isAccessGiven()) {

            final View coordinatorLayoutView = findViewById(R.id.main_content2);
            Snackbar.make(coordinatorLayoutView, "No root access available", Snackbar.LENGTH_LONG)
                    .setAction("Get Root", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=eu.chainfire.supersu")));
                        }
                    })
                    .show();
        }

        // Get the app's shared preferences
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Get the value for the run counter
        int counter = app_preferences.getInt("counter", 0);

        if (counter < 1){

            Intent intent = new Intent(menu.this,Intro.class);
            startActivity(intent);

        }

        // Increment the counter
        SharedPreferences.Editor editor = app_preferences.edit();
        editor.putInt("counter", ++counter);
        editor.commit();



        //create RecyclerView
        RecyclerView recyclerCardViewList = (RecyclerView) findViewById(R.id.cardList);
        recyclerCardViewList.setHasFixedSize(true);
        recyclerCardViewList.addOnItemTouchListener(
                new RecyclerItemClickListener(menu.this, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        onListItemClick(position);
                    }
                })
        );

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerCardViewList.setLayoutManager(llm);



        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerCardViewList);

        //create FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab3);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent Installactivity = new Intent(menu.this, Install.class);

                Bundle bndlanimation =
                        ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.anni1, R.anim.anni2).toBundle();
                startActivity(Installactivity, bndlanimation);
            }


        });


        //set Toolbar
        final android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //set NavigationDrawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }


        //Get SuperSU permissions


        createImportantDirectories();

        //Load plugins
        fillPluginList();


        //initialize swipetorefresh
        final SwipeRefreshLayout mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefresh.setColorSchemeResources(R.color.accent);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                services.clear();
                fillPluginList();
                onItemsLoadComplete();
            }
            void onItemsLoadComplete(){
                ca.notifyDataSetChanged();
                mSwipeRefresh.setRefreshing(false);
            }
        });


        packageBroadcastReceiver = new PackageBroadcastReceiver();
        packageFilter = new IntentFilter();
        packageFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        packageFilter.addAction( Intent.ACTION_PACKAGE_REPLACED );
        packageFilter.addAction( Intent.ACTION_PACKAGE_REMOVED );
        packageFilter.addCategory( Intent.CATEGORY_DEFAULT );
        packageFilter.addDataScheme( "package" );


        Intent g = getIntent();
        if (g != null) {
            boolean snackbar = g.getBooleanExtra("ShowSnackbar",false);
            String snackbarText = g.getStringExtra("SnackbarText");
            if (snackbar){
                final View coordinatorLayoutView = findViewById(R.id.main_content2);
                Snackbar.make(coordinatorLayoutView, snackbarText, Snackbar.LENGTH_LONG)
                        .setAction("Reboot", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder progressDialogReboot = new AlertDialog.Builder(menu.this);
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
            }
        }
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

    //navigationDrawerIcon Onclick
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //set NavigationDrawerContent
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.anni1, R.anim.anni2).toBundle();
                        int id = menuItem.getItemId();
                        switch (id){
                            case R.id.nav_about:
                                Intent about = new Intent(menu.this, AboutActivity.class);

                                startActivity(about, bndlanimation);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_delete:
                                Intent delete = new Intent(menu.this, Delete.class);
                                startActivity(delete, bndlanimation);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_tutorial:
                                Intent tutorial = new Intent(menu.this, DetailedTutorialActivity.class);
                                startActivity(tutorial, bndlanimation);
                                mDrawerLayout.closeDrawers();
                                break;
                            case R.id.nav_restore:
                                Intent restore = new Intent(menu.this, Restore.class);
                                startActivity(restore, bndlanimation);
                                mDrawerLayout.closeDrawers();
                                break;



                            case R.id.nav_showcase:

                                boolean installed = appInstalledOrNot("com.lovejoy777.showcase");
                                if(installed) {
                                    //This intent will help you to launch if the package is already installed
                                    Intent intent = new Intent();
                                    intent.setComponent(new ComponentName("com.lovejoy777.showcase", "com.lovejoy777.showcase.MainActivity1"));
                                    startActivity(intent);


                                    mDrawerLayout.closeDrawers();

                                    break;
                                } else {
                                    Toast.makeText(menu.this, "Please install the layers showcase plugin", Toast.LENGTH_LONG).show();
                                    System.out.println("App is not currently installed on your phone");
                                }

                            case R.id.nav_settings:
                                Intent settings = new Intent(menu.this, SettingsActivity.class);
                                startActivity(settings, bndlanimation);
                                mDrawerLayout.closeDrawers();
                                break;

                        }
                        return false;
                    }
        });
    }

    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==1)
        {
            services.clear();
            fillPluginList();
        }
    }

    //create List with all Plugins
    private List createList(int size, String name[], String developer[], String packages[]) {

        List result = new ArrayList();
        for (int i=1; i <= size; i++) {
            CardViewContent ci = new CardViewContent();
            ci.themeName = name[i-1];
            ci.themeDeveloper = developer[i-1];

            final String packName = packages[i-1];
            String mDrawableName = "icon";
            PackageManager manager = getPackageManager();
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
            Image[0] = getResources().getDrawable(R.drawable.toobad);
            Image[1] = getResources().getDrawable(R.mipmap.ic_launcher);
            Image[2] = getResources().getDrawable(R.drawable.playstore);
            myDrawable = Image[i-1];
            ci.themeImage = myDrawable ;
            result.add(ci);
        }
        return result;
    }


    private void createImportantDirectories(){
        String sdOverlays = Environment.getExternalStorageDirectory() + "/Overlays";
        String sdcard = Environment.getExternalStorageDirectory() + "";

        RootTools.remount(sdcard, "RW");

        // CREATES /SDCARD/OVERLAYS
        File dir = new File(sdOverlays);
        if (!dir.exists() && !dir.isDirectory()) {
            CommandCapture command3 = new CommandCapture(0, "mkdir " + sdOverlays);
            try {
                RootTools.getShell(true).add(command3);
                while (!command3.isFinished()) {
                    Thread.sleep(1);
                }

            } catch (IOException | TimeoutException | InterruptedException | RootDeniedException e) {
                e.printStackTrace();
            }
        }

            String sdOverlays1 = Environment.getExternalStorageDirectory() + "/Overlays/Backup";
            // CREATES /SDCARD/OVERLAYS/BACKUP
            File dir1 = new File(sdOverlays1);
            if (!dir1.exists() && !dir1.isDirectory()) {
                CommandCapture command4 = new CommandCapture(0, "mkdir " + sdOverlays1);
                try {
                    RootTools.getShell(true).add(command4);
                    while (!command4.isFinished()) {
                        Thread.sleep(1);
                    }

                } catch (IOException | TimeoutException | InterruptedException | RootDeniedException e) {
                    e.printStackTrace();
                }
            }

        RootTools.remount("/system", "RW");
        String vendover = "/vendor/overlay";
        // CREATES /VENDOR/OVERLAY
        File dir2 = new File(vendover);
        if (!dir2.exists() && !dir2.isDirectory()) {
            CommandCapture command5 = new CommandCapture(0, "mkdir " + vendover);
            try {
                RootTools.getShell(true).add(command5);
                while (!command5.isFinished()) {
                    Thread.sleep(1);
                }

            } catch (IOException | TimeoutException | InterruptedException | RootDeniedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
        registerReceiver( packageBroadcastReceiver, packageFilter );
    }


    protected void onStop() {
        super.onStop();
        Log.d(LOG_TAG, "onStop");
        unregisterReceiver( packageBroadcastReceiver );
    }

    //open Plugin page after clicked on a cardview
    protected void onListItemClick (int position) {
        if (!TestBoolean){
            String package2 = packages[position];
            String category = categories.get(position);
            if( category.length() > 0 ) {

                Intent intent = new Intent(menu.this, Delete.class);
                intent.setClassName("com.lovejoy777.rroandlayersmanager",
                        "com.lovejoy777.rroandlayersmanager.OverlayDetailActivity");
                intent.putExtra(BUNDLE_EXTRAS_CATEGORY, category);
                intent.putExtra(BUNDLE_EXTRAS_PACKAGENAME, package2);
                Bundle bndlanimation =
                        ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.anni1, R.anim.anni2).toBundle();
                startActivity(intent, bndlanimation);
                System.out.println(package2);

            }
        }
        else{
            if(position==2) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=Layers+Theme&c=apps&docType=1&sp=CAFiDgoMTGF5ZXJzIFRoZW1legIYAIoBAggB:S:ANO1ljK_ZAY")));
            } if(position==1){
                NotAvailableSnackbar();
            }

        }
    }


    //fill the list containing all Plugins
    private void fillPluginList()  {

        services = new ArrayList<HashMap<String,String>>();
        categories = new ArrayList<String>();

        PackageManager packageManager = getPackageManager();
        Intent baseIntent = new Intent( ACTION_PICK_PLUGIN );
        baseIntent.setFlags( Intent.FLAG_DEBUG_LOG_RESOLUTION );
        List<ResolveInfo> list = packageManager.queryIntentServices(baseIntent,
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
                ai = getPackageManager().getApplicationInfo(sinfo.packageName, PackageManager.GET_META_DATA);
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
                        packages[i] = getPackageManager().getApplicationInfo(sinfo.packageName,0).packageName;
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

        Test1[0] = "Too bad.";
        Test2[0] = "You don't have any compatible plugins yet, Use the FAB button for none plugin type overlays.";
        Test1[1] = "Layers Overlays Showcase";
        Test2[1] = "Find some new beautiful overlays. Coming Soon.";
        Test1[2] = "Play Store";
        Test2[2] = "Have a look on the PlayStore.";

        if (list.size()>0) {
            ca = new CardViewAdapter(createList(list.size(), name, developer, packages));
        }else {
            ca = new CardViewAdapter(createList2(3, Test1, Test2));
            TestBoolean = true;
        }
        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        recList.setAdapter(ca);
    }

    private PackageBroadcastReceiver packageBroadcastReceiver;
    private IntentFilter packageFilter;
    private ArrayList<HashMap<String,String>> services;
    private ArrayList<String> categories;
    private SimpleAdapter itemAdapter;
    private String[] packages = new String[100];

    class PackageBroadcastReceiver extends BroadcastReceiver {
        private static final String LOG_TAG = "PackageBroadcastReceiver";

        //when a new Plugin is installed
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive: " + intent);
            services.clear();
            fillPluginList();

            ca.notifyDataSetChanged();
            System.out.println("TEST");
        }
    }

    private void NotAvailableSnackbar() {
        final View coordinatorLayoutView = findViewById(R.id.main_content2);
        Snackbar.make(coordinatorLayoutView, "Sorry, not available yet.", Snackbar.LENGTH_SHORT)
                .show();
    }
}

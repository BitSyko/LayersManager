package com.lovejoy777.rroandlayersmanager.activities;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleAdapter;
import android.widget.Toast;


import com.lovejoy777.rroandlayersmanager.actions.Delete;
import com.lovejoy777.rroandlayersmanager.actions.Install;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.actions.Restore;
import com.lovejoy777.rroandlayersmanager.adapters.CardViewAdapter;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;
import com.lovejoy777.rroandlayersmanager.helper.CardViewContent;
import com.lovejoy777.rroandlayersmanager.helper.RecyclerItemClickListener;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private RecyclerView recyclerCardViewList = null;
    RecyclerView recList = null;
    CardViewAdapter ca = null;
    /** Called when the activity is first created. */




    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Intent intent = new Intent(menu.this, ProductTour2Activity.class);
        //startActivity(intent);

        //create RecyclerView
        recyclerCardViewList = (RecyclerView) findViewById(R.id.cardList);
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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //set NavigationDrawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }


        //Get SuperSU permissions
        (new SuperUser()).execute();


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
                                        ;
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
                            case R.id.nav_backup:

                                final AlertDialog.Builder alert = new AlertDialog.Builder(menu.this);
                                final EditText input = new EditText(menu.this);
                                alert.setTitle("Backup");
                                alert.setView(input);
                                input.setHint("Backup name");
                                alert.setInverseBackgroundForced(true);

                                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int whichButton) {

                                                // get editText String
                                                String backupname = input.getText().toString();

                                                if (backupname.length() <= 1) {

                                                    Toast.makeText(menu.this, "input a name", Toast.LENGTH_LONG).show();

                                                    finish();

                                                } else {
                                                    File directory = new File("/vendor/overlay");
                                                    File[] contents = directory.listFiles();

                                                    // Folder is empty
                                                    if (contents.length == 0) {

                                                        Toast.makeText(menu.this, "nothing to backup", Toast.LENGTH_LONG).show();

                                                        finish();
                                                    } else {
                                                        // CREATES /SDCARD/OVERLAYS/BACKUP/BACKUPNAME
                                                        String sdOverlays = Environment.getExternalStorageDirectory() + "/Overlays";
                                                        File dir2 = new File(sdOverlays + "/Backup/" + backupname);
                                                        if (!dir2.exists() && !dir2.isDirectory()) {
                                                            CommandCapture command1 = new CommandCapture(0, "mkdir " + sdOverlays + "/Backup/" + backupname);
                                                            try {
                                                                RootTools.getShell(true).add(command1);
                                                                while (!command1.isFinished()) {
                                                                    Thread.sleep(1);
                                                                }
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            } catch (TimeoutException e) {
                                                                e.printStackTrace();
                                                            } catch (RootDeniedException e) {
                                                                e.printStackTrace();
                                                            } catch (InterruptedException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                        //Async Task to backup Overlays
                                                        new BackupOverlays().execute(backupname);
                                                    }
                                                }
                                            }
                                        }

                                    );

                                    alert.setNegativeButton(android.R.string.cancel,new DialogInterface.OnClickListener()
                                    {
                                        public void onClick (DialogInterface dialog,int whichButton)
                                        {
                                            dialog.cancel();
                                        }
                                    }

                                    );

                                    alert.show();

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
                        }
                        return false;
                    }
        });
    }


    //create List with all Plugins
    private List createList(int size, String name[], String developer[], String packages[]) {

        List result = new ArrayList();
        Drawable Image[] = new Drawable[size];
        for (int i=1; i <= size; i++) {
            CardViewContent ci = new CardViewContent();
            ci.themeName = name[i-1];
            ci.themeDeveloper = developer[i-1];

            int j = i + 1;
            final String packName = packages[i-1];
            String mDrawableName = "icon";
            PackageManager manager = getPackageManager();
            Resources mApk1Resources = null;
            try {
                mApk1Resources = manager.getResourcesForApplication(packName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            int mDrawableResID = mApk1Resources.getIdentifier(mDrawableName, "drawable", packName);
            Drawable myDrawable = mApk1Resources.getDrawable(mDrawableResID);
            Image[i-1] = myDrawable;

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
            Image[1] = getResources().getDrawable(R.drawable.ic_launcher);
            Image[2] = getResources().getDrawable(R.drawable.playstore);
            myDrawable = Image[i-1];
            ci.themeImage = myDrawable ;
            result.add(ci);
        }
        return result;
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
                        "com.lovejoy777.rroandlayersmanager.activities.OverlayDetailActivity");
                intent.putExtra(BUNDLE_EXTRAS_CATEGORY, category);
                intent.putExtra(BUNDLE_EXTRAS_PACKAGENAME, package2);
                Bundle bndlanimation =
                        ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.anni1, R.anim.anni2).toBundle();
                startActivity(intent, bndlanimation);

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
            Bundle bundle = ai.metaData;
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

    /**
     * **********************************************************************************************************
     * ZIP
     * ********
     * zip a zip file.
     *
     * @throws java.io.IOException *******************************************************************************
     */
    private static void zipFolder(String inputFolderPath, String outZipPath) {
        try {
            FileOutputStream fos = new FileOutputStream(outZipPath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            File srcFile = new File(inputFolderPath);
            File[] files = srcFile.listFiles();
            Log.d("", "Zip directory: " + srcFile.getName());
            for (int i = 0; i < files.length; i++) {
                Log.d("", "Adding file: " + files[i].getName());
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(files[i]);
                zos.putNextEntry(new ZipEntry(files[i].getName()));
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



    private class SuperUser extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressSuperSU;

        protected void onPreExecute() {

            progressSuperSU = ProgressDialog.show(menu.this, "Superuser Permission",
                    "Gaining SuperUser permissions...", true);
        }

        @Override
        protected Void doInBackground(Void... params) {

            // IF ROOT ACCESS IS GIVEN / ELSE LAUNCH PLAYSTORE FOR SUPERUSER APP
            if (!RootTools.isAccessGiven()) {

                runOnUiThread(new Runnable() {
                    public void run() {
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
                });

            } else {

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

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (TimeoutException e) {
                        e.printStackTrace();
                    } catch (RootDeniedException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();

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

                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        } catch (RootDeniedException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
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


                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        } catch (RootDeniedException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }

                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            progressSuperSU.dismiss();
        }
    }



    private class BackupOverlays extends AsyncTask<String,String,Void> {
        ProgressDialog progressBackup;

        protected void onPreExecute() {

            progressBackup = ProgressDialog.show(menu.this, "Backup Overlays",
                    "Backing up...", true);
        }

        @Override
        protected Void doInBackground(String... params) {

                String backupname = params[0];
            try {

                    String sdOverlays = Environment.getExternalStorageDirectory() + "/Overlays";

                    // CREATES /SDCARD/OVERLAYS/BACKUP/TEMP
                    File dir1 = new File(sdOverlays + "/Backup/temp");
                    if (!dir1.exists() && !dir1.isDirectory()) {
                        CommandCapture command = new CommandCapture(0, "mkdir " + sdOverlays + "/Backup/temp");
                        try {
                            RootTools.getShell(true).add(command);
                            while (!command.isFinished()) {
                                Thread.sleep(1);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (TimeoutException e) {
                            e.printStackTrace();
                        } catch (RootDeniedException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }



                    RootTools.remount("/system", "RW");

                    // CHANGE PERMISSIONS OF /VENDOR/OVERLAY && /SDCARD/OVERLAYS/BACKUP
                    CommandCapture command2 = new CommandCapture(0,
                            "chmod -R 755 /vendor/overlay",
                            "chmod -R 755 " + Environment.getExternalStorageDirectory() + "/Overlays/Backup/",
                            "cp -fr /vendor/overlay " + Environment.getExternalStorageDirectory() + "/Overlays/Backup/temp/");
                    RootTools.getShell(true).add(command2);
                    while (!command2.isFinished()) {
                        Thread.sleep(1);
                    }

                    // ZIP OVERLAY FOLDER
                    zipFolder(Environment.getExternalStorageDirectory() + "/Overlays/Backup/temp/overlay", Environment.getExternalStorageDirectory() + "/Overlays/Backup/" + backupname + "/overlay.zip");

                    // CHANGE PERMISSIONS OF /VENDOR/OVERLAY/ 666  && /VENDOR/OVERLAY 777 && /SDCARD/OVERLAYS/BACKUP/ 666
                    CommandCapture command18 = new CommandCapture(0, "chmod 777 " + Environment.getExternalStorageDirectory() + "/Overlays/Backup/temp");
                    RootTools.getShell(true).add(command18);
                    while (!command18.isFinished()) {
                        Thread.sleep(1);
                    }
                    // DELETE /SDCARD/OVERLAYS/BACKUP/TEMP FOLDER
                    // RootTools.deleteFileOrDirectory(Environment.getExternalStorageDirectory() + "/Overlays/Backup/temp", true);

                    RootCommands.DeleteFileRoot(Environment.getExternalStorageDirectory() + "/Overlays/Backup/temp");
                    // CHANGE PERMISSIONS OF /VENDOR/OVERLAY/ 666  && /VENDOR/OVERLAY 777 && /SDCARD/OVERLAYS/BACKUP/ 666
                    CommandCapture command17 = new CommandCapture(0, "chmod -R 666 /vendor/overlay", "chmod 755 /vendor/overlay", "chmod -R 666" + Environment.getExternalStorageDirectory() + "/Overlays/Backup/");
                    RootTools.getShell(true).add(command17);
                    while (!command17.isFinished()) {
                        Thread.sleep(1);
                    }

                    // CLOSE ALL SHELLS
                    RootTools.closeAllShells();



                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RootDeniedException e) {
                    e.printStackTrace();
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            return null;

        }

        protected void onPostExecute(Void result) {

            progressBackup.dismiss();
            Toast.makeText(menu.this, "Backup complete", Toast.LENGTH_LONG).show();

        }
    }
}

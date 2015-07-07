package com.lovejoy777.rroandlayersmanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;

import com.lovejoy777.rroandlayersmanager.actions.Install;
import com.lovejoy777.rroandlayersmanager.helper.CopyUnzipHelper;
import com.lovejoy777.rroandlayersmanager.helper.RootCommandsInstallationHelper;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static com.lovejoy777.rroandlayersmanager.R.*;

/**
 * Created by Niklas on 02.06.2015.
 */
public class OverlayDetailActivity extends AppCompatActivity {

    int NumberOfOverlays = 0;
    int NumberOfColorOverlays = 0;

    //Variables you SHOULD NOT CHANGE!

    private final static int BUFFER_SIZE = 1024;

    Bitmap bitmap[] = new Bitmap[NumberOfScreenshotsMain];

    public static final int NumberOfScreenshotsMain = 3;


    private String whichColor = null;

    final String TargetPath = "/system/vendor/overlay/";


    final ImageView ScreenshotimageView[] = new ImageView[NumberOfScreenshotsMain];

    public CheckBox dontShowAgain;

    int atleastOneIsClicked = 0;

    List<Integer> InstallOverlayList = new ArrayList<Integer>();
    List<String> InstalledOverlays = new ArrayList<String>();
    List<String> OverlayPathList = new ArrayList<String>();
    List<String> OverlayColorListPublic = new ArrayList<String>();

    private String ThemeName;
    private String ThemeFolder =null;
    private String ThemeFolderGeneral = null;
    private int NumberOfColors = 0;

    private View mFab2;

    private String category;
    private String package2;
    private IOperation opService;

    private Switch installEverything = null;
    private FloatingActionButton  fab2 = null;
    private List<String> OverlayNameList = null;
    public OverlayDetailActivity() {
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overlaydetail);

        //get Intent from Main Activity
        Intent g = getIntent();
        if (g != null) {
            category = g.getStringExtra(com.lovejoy777.rroandlayersmanager.menu.BUNDLE_EXTRAS_CATEGORY);
            package2 = g.getStringExtra(menu.BUNDLE_EXTRAS_PACKAGENAME);
        }

        //"connect" to selected Plugin
        bindOpService();

        //switch
        installEverything = (Switch) findViewById(id.allswitch);
        //Hide the FAB
        fab2 = (android.support.design.widget.FloatingActionButton) findViewById(R.id.fab2);
        fab2.setVisibility(View.INVISIBLE);
        fab2.animate().translationY(218).setInterpolator(new AccelerateInterpolator(2)).start();


        //Initialize Layout
        final android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        //get important data from Plugin´s Manifest
        String Description;
        String OverlayNameString;
        String NormalOverlayNameString;
        String StyleSpecificOverlayString;
        String WhatsNew;
        String OverlayColorString;
        ApplicationInfo ai = null;
        try {
            ai = getPackageManager().getApplicationInfo(package2, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Bundle bundle = null;
        if (ai != null) {
            bundle = ai.metaData;
        }
        ThemeName = bundle.getString("Layers_Name");
        Description = bundle.getString("Layers_Description");
        OverlayNameString = bundle.getString("Layers_OverlayNames");
        OverlayColorString = bundle.getString("Layers_Colors");
        WhatsNew = bundle.getString("Layers_WhatsNew");
        NormalOverlayNameString = bundle.getString("Layers_NormalOverlays");
        StyleSpecificOverlayString = bundle.getString("Layers_StyleSpecificOverlays");

        //Use the received data
        ThemeFolder = Environment.getExternalStorageDirectory()+"/Overlays/"+ThemeName.replaceAll(" ", "")+"/";
        ThemeFolderGeneral = ThemeFolder+"General/";


        if (OverlayNameString != null) {
            OverlayNameList = new ArrayList<>(Arrays.asList(OverlayNameString.split(",")));
        } else{
            if (!NormalOverlayNameString.isEmpty()){
                OverlayNameList = new ArrayList<>(Arrays.asList(NormalOverlayNameString.split(",")));
                OverlayNameList.add(" ");
                if (!StyleSpecificOverlayString.isEmpty()){
                    OverlayNameList.addAll(Arrays.asList(StyleSpecificOverlayString.split(",")));
                }
            } else{
                if (StyleSpecificOverlayString != null){
                    OverlayNameList = new ArrayList<>();
                    OverlayNameList.add(" ");
                    OverlayNameList.addAll(Arrays.asList(StyleSpecificOverlayString.split(",")));
                }
            }
        }
        System.out.println(OverlayNameList);
        System.out.println(StyleSpecificOverlayString);
        List<String> OverlayColorList = null;
        if (OverlayColorString != null) {
            OverlayColorList = new ArrayList<>(Arrays.asList(OverlayColorString.split(",")));
        }

        if (OverlayNameList != null){
            NumberOfOverlays = OverlayNameList.indexOf(" ");
            NumberOfColorOverlays = OverlayNameList.size() - OverlayNameList.indexOf(" ")-1;
        }


        NumberOfColors = OverlayColorList.size();

        TextView tv_description = (TextView)findViewById(R.id.HeX1);
        tv_description.setText(Description);

        TextView tv_whatsNew = (TextView) findViewById(R.id.tv_whatsNew);
        tv_whatsNew.setText(WhatsNew);

        collapsingToolbar.setTitle(ThemeName);

        for (int i= 0; i < OverlayColorList.size(); i++){
            OverlayColorListPublic.add(OverlayColorList.get(i));
        }
        for (int i = 0; i < NumberOfColorOverlays +NumberOfOverlays+1;i++) {
            OverlayPathList.add(null);
            InstallOverlayList.add(0);
        }

        //set Toolbar
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        View mFab = findViewById(id.fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installTheme();
            }


        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installTheme();
            }
        });


        //Generate filepaths of normal Overlays
        if (NumberOfOverlays==0){
            for (int i = 1; i <NumberOfOverlays+NumberOfColorOverlays+1;i++){
                String CurrentOverlyName = OverlayNameList.get(i).replaceAll(" ", "");
                OverlayPathList.set(i,ThemeName.replaceAll(" ", "")+"_"+CurrentOverlyName+".apk");
            }
        }else {
            for (int i = 0; i < NumberOfOverlays + NumberOfColorOverlays + 1; i++) {
                String CurrentOverlyName = OverlayNameList.get(i).replaceAll(" ", "");
                OverlayPathList.set(i,ThemeName.replaceAll(" ", "")+"_"+CurrentOverlyName+".apk");
            }
        }

        //Scroll view with screenshots
        LinearLayout screenshotLayout = (LinearLayout)findViewById(R.id.LinearLayoutScreenshots);

        for (int i=0; i<NumberOfScreenshotsMain;i++){
            LinearLayout linear = new LinearLayout(this);

            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

            LinearLayout.LayoutParams params
                    = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            params.rightMargin = margin;

            ScreenshotimageView[i]= new ImageView(this);
            ScreenshotimageView[i].setBackgroundColor(getResources().getColor(R.color.accent));

            linear.setLayoutParams(params);

            linear.addView(ScreenshotimageView[i]);
            screenshotLayout.addView(linear);
        }
        loadScreenshots();

        //sort cardviews by number of Overlays
        LinearLayout CardViewCategory1, CardViewCategory2 ;
        TextView Category1Name, Category2Name;
        if (NumberOfOverlays >= NumberOfColorOverlays) {
            CardViewCategory1 = (LinearLayout) findViewById(R.id.LinearLayoutCategory1);
            CardViewCategory2 = (LinearLayout) findViewById(R.id.LinearLayoutCategory2);
            Category1Name = (TextView) findViewById(id.Tv_Category1Name);
            Category1Name.setText(getResources().getString(R.string.Category1Name));
            Category2Name = (TextView) findViewById(id.Tv_Category2Name);
            Category2Name.setText(getResources().getString(R.string.Category2Name));
        } else {
            CardViewCategory1 = (LinearLayout) findViewById(R.id.LinearLayoutCategory2);
            CardViewCategory2 = (LinearLayout) findViewById(R.id.LinearLayoutCategory1);
            Category1Name = (TextView) findViewById(id.Tv_Category2Name);
            Category1Name.setText(getResources().getString(R.string.Category1Name));
            Category2Name = (TextView) findViewById(id.Tv_Category1Name);
            Category2Name.setText(getResources().getString(R.string.Category2Name));
        }

        //Cardview with normal Overlays
        for (int i = 0; i < NumberOfOverlays; i++)
        {
            TableRow row =new TableRow(this);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            final CheckBox[] check = new CheckBox[NumberOfOverlays];
            check[i]= new CheckBox(this);
            check[i].setText(OverlayNameList.get(i));
            check[i].setTag(i);
            check[i].setId(i);
            check[i].setTextColor(getResources().getColor(R.color.chooser_text_color));
            check[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                //check if checkbox is clicked
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    for (int c=0; c<NumberOfOverlays; c++) {
                        if (buttonView.getTag().equals(c)) {
                            if (buttonView.isChecked()){
                                InstallOverlayList.set(c, 1);

                                    atleastOneIsClicked = atleastOneIsClicked + 1;

                            }
                            else {

                                InstallOverlayList.set(c, 0);
                                //InstallOverlay[c] = 0;
                                atleastOneIsClicked = atleastOneIsClicked -1;
                            }
                            if (atleastOneIsClicked> 0) {
                                fab2.setVisibility(View.VISIBLE);
                                fab2.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();


                            } else {

                                fab2.animate().translationY(fab2.getHeight()+48).setInterpolator(new AccelerateInterpolator(2)).start();
                                //fab2.setVisibility(View.INVISIBLE);

                            }
                        }
                    }
                }
            });
            row.addView(check[i]);
            CardViewCategory1.addView(row);
        }

        //CardView with color specific Overlays
        for (int i = NumberOfOverlays+1; i < NumberOfColorOverlays+NumberOfOverlays+1; i++)
        {
            TableRow row =new TableRow(this);

            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            final CheckBox[] check = new CheckBox[NumberOfColorOverlays+NumberOfOverlays+1];
            check[i]= new CheckBox(this);
            check[i].setText(OverlayNameList.get(i));
            check[i].setTag(i);
            check[i].setId(i);
            check[i].setTextColor(getResources().getColor(R.color.chooser_text_color));
            check[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    for (int c = NumberOfOverlays+1; c <  NumberOfColorOverlays+NumberOfOverlays+1; c++) {

                        if (buttonView.getTag().equals(c)) {

                            if (buttonView.isChecked()) {
                                    InstallOverlayList.set(c, 1);

                                    atleastOneIsClicked = atleastOneIsClicked + 1;

                            } else {
                                atleastOneIsClicked = atleastOneIsClicked - 1;
                                InstallOverlayList.set(c, 0);
                            }
                            if (atleastOneIsClicked> 0) {

                                fab2.setVisibility(View.VISIBLE);
                                fab2.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
                            }
                            else {
                                fab2.animate().translationY(fab2.getHeight()+48).setInterpolator(new AccelerateInterpolator(2)).start();
                               // fab2.setVisibility(View.INVISIBLE);

                            }
                        }
                    }
                }
            });
            row.addView(check[i]);
            CardViewCategory2.addView(row);
        }


        //If there arent any color specific Overlays, hide the cardview
        if(NumberOfColorOverlays==0 || NumberOfOverlays==0) {
            CardView CardViewCategory = (CardView)findViewById(R.id.CardViewCategory2);
            CardViewCategory.setVisibility(View.GONE);
        }

        installEverything.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    checkall();
                } else{
                    UncheckAllCheckBoxes("Uncheck");
                }
            }
        });

        //create the Theme folder
        File ThemeDirectory = new File(Environment.getExternalStorageDirectory()+"/Overlays/"+ThemeName.replaceAll(" ", "")+"/");
        ThemeDirectory.mkdirs();

        fab2.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                installTheme();
            }
        }));

    }





    //Glide Methods
    private void loadBackdrop() {
        final String packName = package2;
        String mDrawableName = "heroimage";
        PackageManager manager = getPackageManager();
        Resources mApk1Resources = null;
        try {
            mApk1Resources = manager.getResourcesForApplication(packName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int mDrawableResID = 0;
        if (mApk1Resources != null) {
            mDrawableResID = mApk1Resources.getIdentifier(mDrawableName, "drawable",packName);
        }
        Drawable myDrawable = mApk1Resources.getDrawable( mDrawableResID );
        ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        if( myDrawable != null ) {
            imageView.setImageDrawable(myDrawable);
        }
    }

    private void loadBackdrop2() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        imageView.setBackgroundResource(R.drawable.no_heroimage);

    }

    private void loadScreenshots() {

        (new LoadDrawables()).execute();

    }

    public void onDestroy() {
        super.onDestroy();
        releaseOpService();
    }

    private void bindOpService() {
        if (category != null) {
            opServiceConnection = new OpServiceConnection();
            Intent i = new Intent(menu.ACTION_PICK_PLUGIN);
            ResolveInfo info = this.getPackageManager().resolveService(i, Context.BIND_AUTO_CREATE);
            i.setComponent(new ComponentName(package2,package2+"."+category));
            i.addCategory(category);
            bindService(i, opServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void releaseOpService() {
        unbindService(opServiceConnection);
        opServiceConnection = null;
    }

    private OpServiceConnection opServiceConnection;

    private static final String LOG_TAG = "InvokeOp";

    class OpServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName className,
                                       IBinder boundService) {
            opService = IOperation.Stub.asInterface(boundService);
            Log.d(LOG_TAG, "onServiceConnected");
            loadBackdrop();

        }

        public void onServiceDisconnected(ComponentName className) {
            opService = null;
            Log.d(LOG_TAG, "onServiceDisconnected");
            //onBackPressed();
            loadBackdrop2();
        }
    }

    //If FAB is clicked
    public void installTheme() {

        int NumberOfSelectedNormalOverlays = 0;
        for (int i = 0; i < NumberOfOverlays; i++) {
            NumberOfSelectedNormalOverlays = NumberOfSelectedNormalOverlays + InstallOverlayList.get(i);
        }

        int NumberOfSelectedColorOverlays = 0;

        for (int i = NumberOfOverlays + 1; i < NumberOfOverlays + NumberOfColorOverlays + 1; i++) {
            NumberOfSelectedColorOverlays = NumberOfSelectedColorOverlays + InstallOverlayList.get(i);
        }

        //No checkBox is checked
        if (NumberOfSelectedNormalOverlays == 0 & NumberOfSelectedColorOverlays == 0 ) {

            selectOverlaysFirstSnackbar();

        } else {

            //when a color checkbox is checked
            if (NumberOfSelectedColorOverlays != 0 ) {
                colorDialog();
            }
            //if only normal Overlays are selected
            else {
            installDialog();
            }
        }
    }





    //Preperation of Installation Of Overlays
    protected void InstallOverlays() throws InterruptedException {
        String sSUCommand;
        String sSuCommand2;

        //install Normal Overlays
        String appPath = null;
        for (int i=0; i <NumberOfOverlays; i++){
            if (InstallOverlayList.get(i) == 1) {
                appPath = OverlayPathList.get(i);
                InstallOverlayList.set(i,0);
                sSUCommand = "cp " + ThemeFolderGeneral + appPath + " " + TargetPath;
                sSuCommand2 = "chmod 666 " + TargetPath + appPath;
                installAPK(sSUCommand, sSuCommand2);
            }
        }


        //install Color Specific Overlays
        for (int i4=NumberOfOverlays+1; i4 <NumberOfOverlays+NumberOfColorOverlays+1; i4++){

            if (InstallOverlayList.get(i4) == 1) {
                appPath = OverlayPathList.get(i4);
                InstallOverlayList.set(i4,0);
                sSUCommand = "cp " + ThemeFolder + whichColor + "/"+ appPath + " " +  TargetPath;
                sSuCommand2 = "chmod 666 " + TargetPath + appPath;
                installAPK(sSUCommand, sSuCommand2);
            }
        }
    }


    public void installAPK(String sSUCommand, String sSuCommand2) throws InterruptedException {

        RootCommandsInstallationHelper cls2= new RootCommandsInstallationHelper();
        cls2.installOverlays(sSUCommand, sSuCommand2);

    }



    private void CopyFolderToSDCard(){
        Context otherContext = null;
        final String packName = package2;
        try {
            otherContext = OverlayDetailActivity.this.createPackageContext(packName,0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        AssetManager am = null;
        if (otherContext != null) {
            am = otherContext.getAssets();
        }
        System.out.println(am);

        String ThemeNameNoSpace = ThemeName.replaceAll(" ","");
        ApplicationInfo ai = null;
        try {
            ai = getPackageManager().getApplicationInfo(packName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        String destinationGeneral = Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeNameNoSpace + "/General/";
        String destinationColor = Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeNameNoSpace + "/";
        File ThemeDirectory = new File(Environment.getExternalStorageDirectory()+"/Overlays/"+ThemeNameNoSpace+"/");
        ThemeDirectory.mkdirs();
        CopyFolderToSDCard(OverlayDetailActivity.this, ThemeNameNoSpace,am);
    }



    //copy files to sd card
    public void CopyFolderToSDCard(Context context, String ThemeNameNoSpace, AssetManager assetFiles) {
        Context mContext;
        //RootTools.deleteFileOrDirectory("/sdcard/Overlays/"+ThemeName, true);
        mContext= context;
        try {

            //AssetManager assetFiles = mContext.getAssets();

            String[] files = assetFiles.list("Files");

            //initialize streams
            InputStream in;
            OutputStream out;

            for (String file : files) {

                if (file.toString().equalsIgnoreCase("images")
                        || file.toString().equalsIgnoreCase("js")) {
                    //nothing
                } else {
                    in = assetFiles.open("Files/" + file);
                    System.out.println(file);
                    out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeNameNoSpace + "/" + file);
                    copyAssetFiles(in, out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private  void copyAssetFiles(InputStream in, OutputStream out) {

        try{
            byte[] buffer = new byte [BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }





    //unzip ..... the zip files :DD
    public void unzip() {

        int NumberOfSelectedNormalOverlays = 0;
        for (int i = 0; i < NumberOfOverlays; i++)
        {
            NumberOfSelectedNormalOverlays = NumberOfSelectedNormalOverlays + InstallOverlayList.get(i);
        }


        int NumberOfSelectedColorOverlays = 0;
        for (int i = NumberOfOverlays+1; i < NumberOfColorOverlays+NumberOfOverlays+1; i++)
        {
            NumberOfSelectedColorOverlays = NumberOfSelectedColorOverlays + InstallOverlayList.get(i);
        }


        ApplicationInfo ai = null;
        try {
            ai = getPackageManager().getApplicationInfo(package2, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        CopyUnzipHelper cls2= new CopyUnzipHelper();
        cls2.unzip(ThemeName.replaceAll(" ", ""), NumberOfSelectedNormalOverlays, NumberOfSelectedColorOverlays, whichColor/*,NumberOfSelectedAdditionalOveerlays*/);

    }




    private void UncheckAllCheckBoxes(String Mode) {

        if (Mode.equals("Uncheck")) {
            //fab2.animate().translationY(fab2.getHeight()+48).setInterpolator(new AccelerateInterpolator(2)).start();

            fab2.setVisibility(View.INVISIBLE);

            for (int i = 0; i < NumberOfOverlays; i++){
                CheckBox checkBox = (CheckBox)findViewById(i);
                checkBox.setChecked(false);
                InstallOverlayList.set(i,0);
            }

            for (int i = NumberOfOverlays+1; i < NumberOfColorOverlays+NumberOfOverlays+1; i++){
                CheckBox checkBox = (CheckBox)findViewById(i);
                checkBox.setChecked(false);
                InstallOverlayList.set(i,0);
            }
        }

        if (Mode.equals("Disable")){
            //disable all Checkboxes
            for (int i = 0; i < NumberOfOverlays; i++){
                CheckBox checkBox = (CheckBox)findViewById(i);
                checkBox.setEnabled(false);
            }

            for (int i = NumberOfOverlays+1; i < NumberOfColorOverlays+NumberOfOverlays+1; i++)
            {
                CheckBox checkBox = (CheckBox)findViewById(i);
                checkBox.setEnabled(false);
            }
        }
    }

    private void checkall() {

        fab2.setVisibility(View.VISIBLE);
        fab2.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();

            for (int i = 0; i < NumberOfOverlays; i++){
                CheckBox checkBox = (CheckBox)findViewById(i);
                checkBox.setChecked(true);
                InstallOverlayList.set(i,1);
            }

            for (int i = NumberOfOverlays+1; i < NumberOfColorOverlays+NumberOfOverlays+1; i++){
                CheckBox checkBox = (CheckBox)findViewById(i);
                checkBox.setChecked(true);
                InstallOverlayList.set(i,1);
            }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.back2, R.anim.back1);
    }




    ///////////
    //Snackbars
    private void selectOverlaysFirstSnackbar() {

        Snackbar.make(getWindow().getDecorView().findViewById(android.R.id.content), string.selectOverlayFirst, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void installationFinishedSnackBar() {

        //show SnackBar after sucessfull installation of the overlays
        final View coordinatorLayoutView = findViewById(id.main_content);
        Snackbar.make(coordinatorLayoutView, string.OverlaysInstalled, Snackbar.LENGTH_LONG)
                .setAction(R.string.Reboot, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        (new Reboot()).execute();
                    }
                })
                .show();
    }



    /////////
    //Dialogs
    public void installDialog() {

        //if (showInstallationConfirmDialog()) {
        AlertDialog.Builder installdialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = this.getLayoutInflater();
        View dontShowAgainLayout = inflater.inflate(R.layout.dialog_installation, null);
        dontShowAgain = (CheckBox) dontShowAgainLayout.findViewById(R.id.skip);

        installdialog.setView(dontShowAgainLayout);
        installdialog.setTitle(R.string.MoveOverlays);
        installdialog.setMessage(R.string.CreateHeXFolder);
        installdialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (dontShowAgain.isChecked()) {
                    SharedPreferences myprefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = myprefs.edit();
                    editor.putString("ConfirmInstallationDialog", "checked");
                    editor.commit();
                }

                //start async task to install the Overlays
                (new InstallOverlays()).execute();
            }
        });
        installdialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                UncheckAllCheckBoxes("Uncheck");
                installEverything.setChecked(false);
            }
        });

        SharedPreferences myprefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
        String skipMessage = myprefs.getString("ConfirmInstallationDialog", "unchecked");
        if (!skipMessage.equals("checked")) {
            installdialog.show();
        } else {
            (new InstallOverlays()).execute();
        }
    }


    //Dialog to choose color
    public void colorDialog() {


        final AlertDialog.Builder colorDialog = new AlertDialog.Builder(OverlayDetailActivity.this);
        final LayoutInflater inflater = this.getLayoutInflater();
        colorDialog.setTitle(string.pick_color);
        View colordialogView = inflater.inflate(layout.dialog_colors, null);
        colorDialog.setView(colordialogView);

        for (int i = 0; i < NumberOfColors; i++) {

            RadioGroup my_layout = (RadioGroup) colordialogView.findViewById(id.radiogroup);

            RadioGroup.LayoutParams params
                    = new RadioGroup.LayoutParams(OverlayDetailActivity.this, null);

            params.leftMargin= 66;
            params.topMargin= 2;
            params.bottomMargin= 2;
            params.width= RadioGroup.LayoutParams.MATCH_PARENT;

            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, getResources().getDisplayMetrics());
            params.height=height;


            RadioButton radioButton = new RadioButton(this);

            radioButton.setText(OverlayColorListPublic.get(i));
            radioButton.setId(i);
            radioButton.setTag("r"+i);
            radioButton.setLayoutParams(params);
            radioButton.setTextSize(18);

            my_layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    whichColor = OverlayColorListPublic.get(checkedId);
                }
            });

            if (i==0){
                radioButton.setChecked(true);
            }
            my_layout.addView(radioButton);
        }

        colorDialog.setCancelable(false);
        colorDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int whichRadioButton = 0;
                SharedPreferences myPrefs = OverlayDetailActivity.this.getSharedPreferences("myPrefs", MODE_PRIVATE);
                for (int e = 0; e < NumberOfColors; e++){
                    if (whichColor.equals(OverlayColorListPublic.get(e))){
                        whichRadioButton = e;

                    }
                }
                SharedPreferences.Editor editor = myPrefs.edit();
                editor.putInt("ColorDialogRadioButton", whichRadioButton);
                editor.commit();
                installDialog();
            }
        });
        colorDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                UncheckAllCheckBoxes("Uncheck");
                installEverything.setChecked(false);
                dialog.dismiss();
            }


        });
        colorDialog.create();
        colorDialog.show();
    }



    /////////////
    //Async Tasks
    private class InstallOverlays extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress2;


        protected void onPreExecute() {

            progress2 = ProgressDialog.show(OverlayDetailActivity.this, "Install overlays",
                    "Installing...", true);
        }

        @Override
        protected Void doInBackground(Void... params) {

            //Mount System to Read / Write
            RootTools.remount("/system/", "RW");

            //initialize last part of root Commands
            String SuperuserCommandOverlayFolderPermission = "chmod 777 /vendor/overlay";
            String SuperuserCommandCreateOverlayFolder = "mkdir /vendor/overlay";

            CommandCapture command4 = new CommandCapture(0, SuperuserCommandCreateOverlayFolder);
            try {
                RootTools.getShell(true).add(command4);
            } catch (IOException | TimeoutException | RootDeniedException e) {
                e.printStackTrace();
            }
            while (!command4.isFinished()){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            CopyFolderToSDCard();  //copy Overlay Files to SD Card

            unzip();  //unzip Overlay ZIP´s

            try {
                InstallOverlays();  //open Method to install Overlays
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            CommandCapture command3 = new CommandCapture(0, SuperuserCommandOverlayFolderPermission);
            try {
                RootTools.getShell(true).add(command3);
            } catch (IOException | TimeoutException | RootDeniedException e) {
                e.printStackTrace();
            }
            while (!command3.isFinished()){
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            RootTools.remount("/system/", "RO");  //remount /system back to RO

            return null;

        }

        protected void onPostExecute(Void result) {

            UncheckAllCheckBoxes("Uncheck");
            installEverything.setChecked(false);
            //appendLog(OverlayNameList);

            progress2.dismiss();
            installationFinishedSnackBar(); //show snackbar with option to reboot

        }
    }




    //Async Task to reboot device///////////////////////////////////////////////////////////////////
    private class Reboot extends AsyncTask<Void, Void, Void> {
        final ProgressDialog progressDialogReboot = new ProgressDialog(OverlayDetailActivity.this);

        protected void onPreExecute() {
            //progressDialog rebooting / 10 seconds
            progressDialogReboot.setTitle(R.string.rebooting);
            progressDialogReboot.setMessage("Rebooting in 10 seconds...");
            progressDialogReboot.setCanceledOnTouchOutside(false);
            progressDialogReboot.setButton(DialogInterface.BUTTON_NEGATIVE, "CANCEL", new DialogInterface.OnClickListener() {
                //when Cancel Button is clicked
                @Override
                public  void onClick(DialogInterface dialog, int which) {
                    Reboot.this.cancel(true);
                    dialog.dismiss();
                }
            });

            progressDialogReboot.setButton(DialogInterface.BUTTON_POSITIVE, "Reboot Now", new DialogInterface.OnClickListener() {
                //when Cancel Button is clicked
                @Override
                public  void onClick(DialogInterface dialog, int which) {
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

        //wait 10 seconds to reboot
        @Override
        protected Void doInBackground(Void... params) {
            //wait 10 seconds
            int i = 0;
            while (i<10) {
                i++;
                //cancel AsyncTask if Cancel Button is pressed
                if (isCancelled()) {
                    break;
                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            return null;
        }

        protected void onPostExecute(Void result) {
            //close Dialog
            progressDialogReboot.dismiss();

            //softreboot phone
            try {
                Process proc = Runtime.getRuntime()
                        .exec(new String[]{"su", "-c", "busybox killall system_server"});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class LoadDrawables extends AsyncTask<Void, Void, Void> {


        protected void onPreExecute() {

        }


        @Override
        protected Void doInBackground(Void... params) {

            for (int i = 0; i < NumberOfScreenshotsMain; i++) {
                Drawable Screenshots[] = new Drawable[NumberOfScreenshotsMain];
                int j = i + 1;
                final String packName = package2;
                String mDrawableName = "screenshot" + j;
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
                Drawable myDrawable = null;
                if (mApk1Resources != null) {
                    myDrawable = mApk1Resources.getDrawable(mDrawableResID);
                }
                Screenshots[i] = myDrawable;
                bitmap[i] = ((BitmapDrawable) Screenshots[i]).getBitmap();

            }
            return null;

        }

        protected void onPostExecute(Void result) {

            for (int i = 0; i < NumberOfScreenshotsMain; i++) {
                ScreenshotimageView[i].setImageBitmap(Bitmap.createScaledBitmap(bitmap[i], (int) (bitmap[i].getWidth() * 0.4), (int) (bitmap[i].getHeight() * 0.4), true));
            }
        }
    }


    /*public void appendLog(List<String> text)
    {
        String filename = "OverlayLog";
        File logFile = new File(OverlayDetailActivity.this.getFilesDir(), filename);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(" // "+ThemeName + " ,"+text.toString()+" // "/*.replace(ThemeName+"_","").replace(" ", ""));
            //buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        readLog();
    }

    public  void readLog() {
        String filename = "OverlayLog";

        //Get the text file
        File logFile = new File(OverlayDetailActivity.this.getFilesDir(), filename);

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(logFile));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
            //You'll need to add proper error handling here
        }
        String test = text.toString().replaceAll("\\[","").replaceAll("\\]","").replaceAll(" , ","");
        System.out.println("Test "+test);
        InstalledOverlays = new ArrayList<>(Arrays.asList(test.split("//")));
        System.out.println(InstalledOverlays.get(0));
        //String Splitted = InstalledOverlays.toString();
        //int position = -1;
        for (int i = 0; i < InstalledOverlays.size()-2; i++){
            String Splitted = InstalledOverlays.get(i+1).toString();
            List<String> SplitedList = new ArrayList<>(Arrays.asList(Splitted.replaceAll(" ","").split(",")));
            if (SplitedList.get(0).contains(ThemeName.replaceAll(" ",""))){
                i = InstalledOverlays.size()-2;
                SplitedList.remove(0);
                System.out.println("YEAHHH"+SplitedList);
            }
        }



    }*/
}
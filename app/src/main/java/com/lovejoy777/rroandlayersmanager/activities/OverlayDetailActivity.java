package com.lovejoy777.rroandlayersmanager.activities;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.bitsyko.liblayers.Layer;
import com.bitsyko.liblayers.LayerFile;
import com.lovejoy777.rroandlayersmanager.AsyncResponse;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.helper.ExpandableTextView;
import com.lovejoy777.rroandlayersmanager.interfaces.Callback;
import com.lovejoy777.rroandlayersmanager.interfaces.StoppableAsyncTask;
import com.lovejoy777.rroandlayersmanager.loadingpackages.CreateList;
import com.lovejoy777.rroandlayersmanager.loadingpackages.ShowAllPackagesFromLayer;
import com.lovejoy777.rroandlayersmanager.loadingpackages.ShowPackagesFromList;
import com.lovejoy777.rroandlayersmanager.views.CheckBoxHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


public class OverlayDetailActivity extends AppCompatActivity implements AsyncResponse, AppBarLayout.OnOffsetChangedListener {

    //New header thing :D
    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private boolean mIsTheTitleVisible          = false;
    private boolean mIsTheTitleContainerVisible = true;

    private LinearLayout mTitleContainer;
    private TextView mTitle;
    private AppBarLayout mAppBarLayout;
    private ImageView mImageparallax;
    private FrameLayout mFrameParallax;
    private Toolbar mToolbar;


    private CheckBox dontShowAgain;
    private ArrayList<CheckBox> checkBoxesGeneral = new ArrayList<>();
    private ArrayList<CheckBox> checkBoxesStyle = new ArrayList<>();
    private String choosedStyle = "";
    private Layer layer;
    private Switch installAllGeneral;
    private Switch installAllStyle;
    private FloatingActionButton installationFAB;
    private CoordinatorLayout cordLayout;
    private LoadDrawables imageLoader;
    private List<StoppableAsyncTask<Void, ?, ?>> loadLayerApks = new ArrayList<>();

    private CheckBoxHolder.CheckBoxHolderCallback checkBoxHolderCallback = new CheckBoxHolder.CheckBoxHolderCallback() {
        @Override
        public void onClick() {
            refreshFab();
        }
    };

    private Callback<CheckBox> checkBoxCallback = new Callback<CheckBox>() {

        @Override
        public void callback(CheckBox item) {
            if (((LayerFile) item.getTag()).isColor()){
                checkBoxesStyle.add(item);
            }else{
                checkBoxesGeneral.add(item);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_plugindetail);


        bindActivity();
        mToolbar.setTitle("");
        mAppBarLayout.addOnOffsetChangedListener(this);

        setSupportActionBar(mToolbar);
        startAlphaAnimation(mTitle, 0, View.INVISIBLE);
        initParallaxValues();

        cordLayout = (CoordinatorLayout) findViewById(R.id.main_content);

        receiveIntent();

        cordLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                loadBackdrop();
            }
        });


        createLayouts();

        Log.d("Colors", String.valueOf(layer.getColors()));

    }



    private boolean isAnyCheckboxEnabled() {

        for (CheckBox checkBox : checkBoxesGeneral) {
            if (checkBox.isChecked()) {
                return true;
            }
        }
        for (CheckBox checkBox : checkBoxesStyle){
            if (checkBox.isChecked()){
                return true;
            }
        }
        return false;
    }


    private void refreshFab() {

        if (isAnyCheckboxEnabled()) {
            installationFAB.show();
        } else {
            installationFAB.hide();
        }

    }

    private void loadScreenshotCardview() {
        loadScreenshots();
    }

    private void receiveAndUseData() {
        //Description
        TextView tv_description = (TextView) cordLayout.findViewById(R.id.tv_description);
        tv_description.setText(layer.getDescription());

        //Whats New

        ExpandableTextView tv_whatsNew = (ExpandableTextView) cordLayout.findViewById(R.id.tv_whatsNew);
        tv_whatsNew.setText(layer.getWhatsNew());

        //Title
        TextView titleBig = (TextView) cordLayout.findViewById(R.id.tv_appNameBig);
        TextView titleSmall = (TextView) cordLayout.findViewById(R.id.main_textview_title);
        titleBig.setText(layer.getName());
        titleSmall.setText(layer.getName());

    }

    private void createLayouts() {

        //switch to select all Checkboxes
        installAllGeneral = (Switch) cordLayout.findViewById(R.id.Tv_Category1Name);
        installAllStyle = (Switch) cordLayout.findViewById(R.id.Tv_Category2Name);



        //Hide the FAB
        installationFAB = (android.support.design.widget.FloatingActionButton) cordLayout.findViewById(R.id.fab2);
        installationFAB.hide();

        //Initialize Layout


       mToolbar.setNavigationIcon(R.drawable.ic_action_back);
       setSupportActionBar(mToolbar);
       getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back);


        installationFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installTheme();
            }
        });

        installAllStyle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkall(1);
                } else {
                    uncheckAllCheckBoxes(1);
                }
            }
        });

        installAllGeneral.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkall(0);
                } else {
                    uncheckAllCheckBoxes(0);
                }
            }
        });

    }

    private void receiveIntent() {
        String layerPackageName = getIntent().getStringExtra("PackageName");
        try {
            layer = Layer.layerFromPackageName(layerPackageName, getApplicationContext());
            //We're removing previous apks
            layer.close();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("PackageName: ", layerPackageName);
    }

    private void loadBackdrop() {

        ImageView imageView = (ImageView) cordLayout.findViewById(R.id.main_imageview_placeholder);
        //CircleImageView appIconImageView = (CircleImageView) cordLayout.findViewById(R.id.img_appIcon);

        Drawable promo = layer.getPromo();
        Drawable appIcon = layer.getIcon();

        imageView.setImageDrawable(promo);
       // appIconImageView.setImageDrawable(appIcon);


        Animator reveal = ViewAnimationUtils.createCircularReveal(imageView,
                imageView.getWidth() / 2,
                imageView.getHeight() / 2,
                0,
                imageView.getHeight() * 2);
        reveal.setDuration(750);
        reveal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                loadScreenshotCardview();
                receiveAndUseData();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                loadOverlayCardviews();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        reveal.start();
    }

    private void loadBackdrop2() {
        final ImageView imageView = (ImageView) cordLayout.findViewById(R.id.main_imageview_placeholder);
        imageView.setBackgroundResource(R.drawable.no_heroimage);
    }

    private void loadScreenshots() {
        imageLoader = new LoadDrawables();
        imageLoader.execute();
    }

    //If FAB is clicked
    private void installTheme() {

        boolean isThereColorOverlay = false;

        for (CheckBox checkBox : checkBoxesStyle) {
            if (checkBox.isChecked()) {
                isThereColorOverlay = true;
                break;
            }
        }

        //when a color checkbox is checked
        if (isThereColorOverlay) {
            colorDialog();
        }
        //if only normal Overlays are selected
        else {
            installDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_plugindetail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
            case R.id.menu_selectall:
                if (isAnyCheckboxEnabled()){
                    uncheckAllCheckBoxes(2);
                    installAllGeneral.setChecked(false);
                    installAllStyle.setChecked(false);
                } else {
                    checkall(2);
                    installAllGeneral.setChecked(true);
                    installAllStyle.setChecked(true);
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void uncheckAllCheckBoxes(int mode) {
        //Mode: 0 = uncheck General
        //      1 = uncheck Style
        //      2 = uncheck both

        if (mode==1){
            for (CheckBox checkBox : checkBoxesStyle) {
                if (checkBox.isChecked()) {
                    checkBox.performClick();
                }
            }
        }else {
            if (mode == 0) {
                for (CheckBox checkBox : checkBoxesGeneral) {
                    if (checkBox.isChecked()) {
                        checkBox.performClick();
                    }
                }
            }else {
                for (CheckBox checkBox : checkBoxesGeneral) {
                    if (checkBox.isChecked()) {
                        checkBox.performClick();
                    }
                }
                for (CheckBox checkBox : checkBoxesStyle) {
                    if (checkBox.isChecked()) {
                        checkBox.performClick();
                    }
                }
            }
        }

        refreshFab();

    }

    private void checkall(int mode) {
        //Mode: 0 = uncheck General
        //      1 = uncheck Style
        //      2 = uncheck both
        if (mode==1){
            for (CheckBox checkBox : checkBoxesStyle) {
                if (!checkBox.isChecked() && checkBox.isEnabled()) {
                    checkBox.performClick();
                }
            }
        }else{
            if (mode==0){
                for (CheckBox checkBox : checkBoxesGeneral) {
                    if (!checkBox.isChecked() && checkBox.isEnabled()) {
                        checkBox.performClick();
                    }
                }
            } else{
                for (CheckBox checkBox : checkBoxesStyle) {
                    if (!checkBox.isChecked() && checkBox.isEnabled()) {
                        checkBox.performClick();
                    }
                }
                for (CheckBox checkBox : checkBoxesGeneral) {
                    if (!checkBox.isChecked() && checkBox.isEnabled()) {
                        checkBox.performClick();
                    }
                }
            }
        }


        refreshFab();

    }




    ///////////
    //Snackbars

    private void installationFinishedSnackBar() {

        //show SnackBar after successful installation of the overlays
        final View coordinatorLayoutView = cordLayout.findViewById(R.id.main_content);
        Snackbar.make(coordinatorLayoutView, R.string.OverlaysInstalled, Snackbar.LENGTH_LONG)
                .setAction(R.string.Reboot, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Commands.reboot(OverlayDetailActivity.this);
                    }
                })
                .show();
    }


    /////////
    //Dialogs
    private void installDialog() {

        //if (showInstallationConfirmDialog()) {
        AlertDialog.Builder installdialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = getLayoutInflater();
        View dontShowAgainLayout = inflater.inflate(R.layout.dialog_donotshowagain, null);
        dontShowAgain = (CheckBox) dontShowAgainLayout.findViewById(R.id.skip);

        installdialog.setView(dontShowAgainLayout);
        installdialog.setTitle(R.string.MoveOverlays);
        installdialog.setMessage(R.string.ApplyOverlays);
        installdialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (dontShowAgain.isChecked()) {
                    SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = myprefs.edit();
                    editor.putString("ConfirmInstallationDialog", "checked");
                    editor.apply();
                }

                //start async task to install the Overlays
                InstallAsyncOverlays();
            }
        });
        installdialog.setNegativeButton(android.R.string.cancel, null);

        SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String skipMessage = myprefs.getString("ConfirmInstallationDialog", "unchecked");
        if (!skipMessage.equals("checked")) {
            installdialog.show();
        } else {
            InstallAsyncOverlays();
        }
    }

    private void InstallAsyncOverlays() {

        List<LayerFile> layersToInstall = new ArrayList<>();

        for (CheckBox checkBox : checkBoxesGeneral) {

            if (checkBox.isChecked()) {
                LayerFile layerFile = (LayerFile) checkBox.getTag();
                layersToInstall.add(layerFile);
            }

        }

        Log.d("Choosed color", choosedStyle);

        new Commands.InstallOverlaysBetterWay(layersToInstall, choosedStyle, this, this).execute();


    }

    public void processFinish() {
        installationFinishedSnackBar();
        uncheckAllCheckBoxes(2);
        installAllStyle.setChecked(false);
        installAllGeneral.setChecked(false);
    }

    //Dialog to choose color
    private void colorDialog() {

        final AlertDialog.Builder colorDialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = getLayoutInflater();
        colorDialog.setTitle(R.string.pick_color);
        View colordialogView = inflater.inflate(R.layout.dialog_colors, null);
        colorDialog.setView(colordialogView);

        final RadioGroup radioGroup = (RadioGroup) colordialogView.findViewById(R.id.radiogroup);

        RadioGroup.LayoutParams params
                = new RadioGroup.LayoutParams(this, null);

        params.leftMargin = 66;
        params.topMargin = 2;
        params.bottomMargin = 2;
        params.width = RadioGroup.LayoutParams.MATCH_PARENT;

        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, getResources().getDisplayMetrics());
        params.height = height;


        final List<String> colors = layer.getColors();

        for (final String color : colors) {

            final RadioButton radioButton = new RadioButton(this);

            radioButton.setText(color);
            radioButton.setLayoutParams(params);
            radioButton.setTextSize(18);
            radioButton.setTag(color);

            radioGroup.addView(radioButton);

            radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    choosedStyle = (String) v.getTag();
                }
            });

            if (colors.indexOf(color) == 0) {
                radioButton.performClick();
            }

        }

        colorDialog.setCancelable(false);
        colorDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                installDialog();
            }
        });
        colorDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }


        });
        colorDialog.create();
        colorDialog.show();
    }

    @Override
    public void onDestroy() {
        loadBackdrop2();
        if (imageLoader != null && imageLoader.getStatus() != AsyncTask.Status.FINISHED) {
            imageLoader.cancel(true);
        }


        for (StoppableAsyncTask asyncTask : loadLayerApks) {

            if (asyncTask != null && asyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                asyncTask.stop();
                asyncTask.cancel(true);
            }

        }

        try {
            layer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    private void loadOverlayCardviews() {

        //We're checking if progress dialog is required

        boolean disableNotInstalledApps = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                .getBoolean("disableNotInstalledApps", false);


        /**
         * We need 3 asynctasks:
         * 1. When we create list
         * 2. When we create from existing list
         * 3. When we don't care about list
         */

        SharedPreferences myprefs = getSharedPreferences("layersData", Context.MODE_PRIVATE);

        Set<String> filesToGreyOut = myprefs.getStringSet(layer.getPackageName(), null);

        boolean createList = (filesToGreyOut == null || !filesToGreyOut.contains(layer.getVersionCode()));

        loadLayerApks.clear();

        if (disableNotInstalledApps) {

            if (createList) {
                loadLayerApks.add(new CreateList(this, layer));
            }

            loadLayerApks.add(new ShowPackagesFromList(this, cordLayout, layer, checkBoxCallback, checkBoxHolderCallback));

        } else {
            loadLayerApks.add(new ShowAllPackagesFromLayer(this, cordLayout, layer, checkBoxCallback, checkBoxHolderCallback));
        }


        for (AsyncTask<Void, ?, ?> asyncTask : loadLayerApks) {
            asyncTask.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
        }

    }

    private class LoadDrawables extends AsyncTask<Void, Drawable, Void> {

        LinearLayout screenshotLayout;

        @Override
        protected void onPreExecute() {

            screenshotLayout = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutScreenshots);
        }

        @Override
        protected void onProgressUpdate(Drawable... values) {

            for (Drawable screenshot : values) {

                ImageView screenshotImageView;

                try {
                    screenshotImageView = new ImageView(OverlayDetailActivity.this);
                } catch (NullPointerException e) {
                    continue;
                }

                Bitmap bitmap = ((BitmapDrawable) screenshot).getBitmap();

                //TODO: Rewrite
                if (bitmap.getHeight() > 1000) {
                    screenshotImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.4), (int) (bitmap.getHeight() * 0.4), true));
                } else {
                    screenshotImageView.setImageBitmap(bitmap);
                }

                LinearLayout linear = new LinearLayout(OverlayDetailActivity.this);

                linear.addView(screenshotImageView);
                screenshotLayout.addView(linear);
            }

        }

        @Override
        protected Void doInBackground(Void... params) {

            Pair<Integer, Drawable> pair;

            while ((pair = layer.getNextScreenshot()).first != 0) {

                if (isCancelled()) {
                    break;
                }

                publishProgress(pair.second);

            }

            return null;
        }

    }







    private void bindActivity() {
        mToolbar        = (Toolbar) findViewById(R.id.main_toolbar);
        mTitle          = (TextView) findViewById(R.id.main_textview_title);
        mTitleContainer = (LinearLayout) findViewById(R.id.main_linearlayout_title);
        mAppBarLayout   = (AppBarLayout) findViewById(R.id.main_appbar);
        mImageparallax  = (ImageView) findViewById(R.id.main_imageview_placeholder);
        mFrameParallax  = (FrameLayout) findViewById(R.id.main_framelayout_title);
    }

    private void initParallaxValues() {
        CollapsingToolbarLayout.LayoutParams petDetailsLp =
                (CollapsingToolbarLayout.LayoutParams) mImageparallax.getLayoutParams();

        CollapsingToolbarLayout.LayoutParams petBackgroundLp =
                (CollapsingToolbarLayout.LayoutParams) mFrameParallax.getLayoutParams();

        petDetailsLp.setParallaxMultiplier(0.9f);
        petBackgroundLp.setParallaxMultiplier(0.3f);

        mImageparallax.setLayoutParams(petDetailsLp);
        mFrameParallax.setLayoutParams(petBackgroundLp);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }



/*
    private class LoadLayerApks extends StoppableAsyncTask<Void, Pair<Boolean, TableRow>, Pair<Set<String>, Set<String>>> {

        private Context context;
        private CoordinatorLayout cordLayout;
        private LinearLayout linearLayoutCategory1, linearLayoutCategory2;
        private CardView cardViewCategory1, cardViewCategory2;
        private boolean stop;
        boolean newSet = false;
        boolean showNotInstalledApps;

        public LoadLayerApks(Context context, CoordinatorLayout cordLayout) {
            this.context = context;
            this.cordLayout = cordLayout;
        }

        @Override
        protected void onPreExecute() {
            linearLayoutCategory1 = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory1);
            linearLayoutCategory2 = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory2);
            cardViewCategory1 = (CardView) cordLayout.findViewById(R.id.CardViewCategory1);
            cardViewCategory2 = (CardView) cordLayout.findViewById(R.id.CardViewCategory2);
        }

        @Override
        protected Pair<Set<String>, Set<String>> doInBackground(Void... params) {

            SharedPreferences myprefs = getSharedPreferences("layersData", Context.MODE_PRIVATE);
            Set<String> filesToGreyOut = myprefs.getStringSet(layer.getPackageName(), null);
            Set<String> filesThatDontExist = myprefs.getStringSet(layer.getPackageName() + "_dontExist", null);

            showNotInstalledApps = !getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
                    .getBoolean("disableNotInstalledApps", false);


            if (filesToGreyOut == null || !filesToGreyOut.contains(layer.getVersionCode())) {
                newSet = true;
                filesToGreyOut = new HashSet<>();
                filesThatDontExist = new HashSet<>();
                filesToGreyOut.add(layer.getVersionCode());
            }

            List<LayerFile> layerFiles = layer.getLayersInPackage();
            List<String> packages = new ArrayList<>(Helpers.allPackagesInSystem(OverlayDetailActivity.this));

            if (newSet && !showNotInstalledApps) {
                Log.d("Installed packages", String.valueOf(packages));
            }

            for (LayerFile layerFile : layerFiles) {

                if (isCancelled() || stop) {
                    return null;
                }

                TableRow row = new TableRow(context);
                row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                CheckBox check = new CheckBox(context);

                check.setText(layerFile.getNiceName());
                check.setTag(layerFile);

                FrameLayout frameLayout = new CheckBoxHolder(OverlayDetailActivity.this, check, new CheckBoxHolder.CheckBoxHolderCallback() {
                    @Override
                    public void onClick() {
                        refreshFab();
                    }
                });

                frameLayout.addView(check);
                row.addView(frameLayout);

                if (newSet && !showNotInstalledApps) {

                    try {

                        if (layerFile.isColor()) {
                            layerFile.getFile(layer.getColors().get(0));
                        } else {
                            layerFile.getFile();
                        }

                        Log.d("Manifest " + layerFile.getName(), layerFile.getRelatedPackage());

                        if (!packages.contains(layerFile.getRelatedPackage())) {
                            filesToGreyOut.add(layerFile.getName());
                        }

                    } catch (IOException | NoFileInZipException e) {
                        e.printStackTrace();
                        filesThatDontExist.add(layerFile.getName());
                    }

                }

                if (!showNotInstalledApps) {

                    check.setEnabled(!filesToGreyOut.contains(layerFile.getName()));

                    if (filesThatDontExist.contains(layerFile.getName())) {
                        check.setEnabled(false);
                        check.setTextColor(getResources().getColor(R.color.accent));
                    }
                }

                Pair<Boolean, TableRow> pair = new Pair<>(layerFile.isColor(), row);

                //noinspection unchecked
                publishProgress(pair);

                checkBoxesGeneral.add(check);

            }


            return new Pair<>(filesToGreyOut, filesThatDontExist);

        }


        @SafeVarargs
        @Override
        protected final void onProgressUpdate(Pair<Boolean, TableRow>... values) {

            for (Pair<Boolean, TableRow> row : values) {

                if (row.first) {
                    linearLayoutCategory2.addView(row.second);
                    linearLayoutCategory2.invalidate();
                } else {
                    linearLayoutCategory1.addView(row.second);
                    linearLayoutCategory1.invalidate();
                }
            }
        }

        @Override
        protected void onPostExecute(Pair<Set<String>, Set<String>> pair) {

            //No styleSpecific Overlays
            if (linearLayoutCategory2.getChildCount() == 0) {
                cardViewCategory2.setVisibility(View.GONE);
            }
            //No normal Overlays
            if (linearLayoutCategory1.getChildCount() == 0) {
                cardViewCategory1.setVisibility(View.GONE);
            }

            if (newSet && !showNotInstalledApps) {
                SharedPreferences myprefs = getSharedPreferences("layersData", Context.MODE_PRIVATE);
                myprefs.edit().putStringSet(layer.getPackageName(), pair.first).apply();
                myprefs.edit().putStringSet(layer.getPackageName() + "_dontExist", pair.second).apply();
                Toast.makeText(OverlayDetailActivity.this, "Generating complete", Toast.LENGTH_LONG).show();
            }

        }

        @Override
        public void stop() {
            cancel(true);
            stop = true;
        }

    }

    */

}
package com.lovejoy777.rroandlayersmanager.activities;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import android.widget.*;

import com.bitsyko.liblayers.Layer;
import com.bitsyko.liblayers.LayerFile;
import com.lovejoy777.rroandlayersmanager.AsyncResponse;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.interfaces.Callback;
import com.lovejoy777.rroandlayersmanager.interfaces.StoppableAsyncTask;
import com.lovejoy777.rroandlayersmanager.loadingpackages.CreateList;
import com.lovejoy777.rroandlayersmanager.loadingpackages.ShowAllPackagesFromLayer;
import com.lovejoy777.rroandlayersmanager.loadingpackages.ShowPackagesFromList;
import com.lovejoy777.rroandlayersmanager.views.CheckBoxHolder;

import java.io.IOException;
import java.util.*;

public class OverlayDetailActivity extends AppCompatActivity implements AsyncResponse {

    private CheckBox dontShowAgain;
    private ArrayList<CheckBox> checkBoxesGeneral = new ArrayList<>();
    private ArrayList<CheckBox> checkBoxesStyle = new ArrayList<>();
    private String choosedStyle = "";
    private String NewchoosedStyle;
    private Layer layer;
    private Switch installAllGeneral;
    private Switch installAllStyle;
    private FloatingActionButton installationFAB;
    private CoordinatorLayout cordLayout;
    private LoadDrawables imageLoader;
    private List<StoppableAsyncTask<Void, ?, ?>> loadLayerApks = new ArrayList<>();

    private CheckBoxHolder.CheckBoxHolderCallback checkBoxHolderCallback = new CheckBoxHolder.CheckBoxHolderCallback() {
        @Override
        public void onClick(CheckBox which, boolean checked) {

            if (layer.getPluginVersion()==3 &&(((LayerFile) which.getTag()).hasStyles()) && which.isChecked()){
                styleDialog((LayerFile) which.getTag(),which);
            }else{
                refreshFab();
            }

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




    private void styleDialog(final LayerFile item, final CheckBox whichcheckbox) {

        final AlertDialog.Builder colorDialog = new AlertDialog.Builder(this);
        final LayoutInflater inflater = getLayoutInflater();
        colorDialog.setTitle(getString(R.string.pick_color) + " for " + item.getName());
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


        final List<String> colors = item.getColors();

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
                    NewchoosedStyle = (String) v.getTag();
                    refreshFab();
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
                //installDialog();
                item.setSelectedStyle(NewchoosedStyle);
                whichcheckbox.setText(item.getName() + ": " + NewchoosedStyle);
            }
        });
        colorDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                whichcheckbox.setChecked(false);
                whichcheckbox.setText(item.getName());
                refreshFab();
            }


        });
        colorDialog.create();
        colorDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_plugindetail);

        getWindow().setStatusBarColor(getResources().getColor(R.color.transparent));

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
        Log.d("PluginVersion", String.valueOf(layer.getPluginVersion()));
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

        TextView tv_description = (TextView) cordLayout.findViewById(R.id.HeX1);
        tv_description.setText(layer.getDescription());

        TextView tv_whatsNew = (TextView) cordLayout.findViewById(R.id.tv_whatsNew);
        tv_whatsNew.setText(layer.getWhatsNew());

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) cordLayout.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(layer.getName());

    }

    private void createLayouts() {
        //switch to select all Checkboxes
        installAllGeneral = (Switch) cordLayout.findViewById(R.id.Tv_Category1Name);
        installAllStyle = (Switch) cordLayout.findViewById(R.id.Tv_Category2Name);


        //Hide the FAB
        installationFAB = (android.support.design.widget.FloatingActionButton) cordLayout.findViewById(R.id.fab2);
        installationFAB.hide();

        //Initialize Layout
        Toolbar toolbar = (Toolbar) cordLayout.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        setSupportActionBar(toolbar);
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

        ImageView imageView = (ImageView) cordLayout.findViewById(R.id.backdrop);

        Drawable promo = layer.getPromo();

        imageView.setImageDrawable(promo);

        final CollapsingToolbarLayout collapsingToolbar = (CollapsingToolbarLayout) cordLayout.findViewById(R.id.collapsing_toolbar);

        Palette.from(((BitmapDrawable) promo).getBitmap()).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch != null) {
                    collapsingToolbar.setContentScrimColor(vibrantSwatch.getRgb());
                    float[] hsv = new float[3];
                    Color.colorToHSV(vibrantSwatch.getRgb(), hsv);
                    hsv[2] *= 0.8f;
                    collapsingToolbar.setStatusBarScrimColor(Color.HSVToColor(hsv));
                    //int colorPrimaryDark = Color.HSVToColor(hsv);
                    //  Window window = getWindow();
                    // window.setStatusBarColor(Color.HSVToColor(hsv));
                }
            }
        });
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
                //  Animation fadeInAnimation = AnimationUtils.loadAnimation(OverlayDetailActivity.this, R.anim.fadein);
                LinearLayout WN = (LinearLayout) cordLayout.findViewById(R.id.lin2);
                WN.setVisibility(View.VISIBLE);
                // WN.startAnimation(fadeInAnimation);
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
        final ImageView imageView = (ImageView) cordLayout.findViewById(R.id.backdrop);
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
                return true;
            case R.id.menu_refresh:
                clearOverlaysData();
                loadOverlayCardviews();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearOverlaysData() {
        checkBoxesGeneral.clear();
        checkBoxesStyle.clear();

        ((LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory1)).removeAllViews();
        ((LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory2)).removeAllViews();

        SharedPreferences myprefs = getSharedPreferences("layersData", Context.MODE_PRIVATE);
        myprefs.edit().remove(layer.getPackageName()).commit();
    }

    private void changeCheckBoxCheckedStatus(int mode, boolean checked) {
        //Mode: 0 = uncheck General
        //      1 = uncheck Style
        //      2 = uncheck both

        //Checkboxes which checking status will be changed
        List<CheckBox> checkBoxList = new ArrayList<>();

        switch (mode) {
            case 0:
                checkBoxList.addAll(checkBoxesGeneral);
                break;

            case 1:
                checkBoxList.addAll(checkBoxesStyle);
                break;

            case 2:
                checkBoxList.addAll(checkBoxesGeneral);
                checkBoxList.addAll(checkBoxesStyle);
                break;
        }

        for (CheckBox checkBox : checkBoxList) {
            if (checkBox.isEnabled()) {
                checkBox.setChecked(checked);
            }
        }

        refreshFab();

    }


    private void uncheckAllCheckBoxes(int mode) {
        //Mode: 0 = uncheck General
        //      1 = uncheck Style
        //      2 = uncheck both

        changeCheckBoxCheckedStatus(mode, false);

    }

    private void checkall(int mode) {
        //Mode: 0 = uncheck General
        //      1 = uncheck Style
        //      2 = uncheck both

        changeCheckBoxCheckedStatus(mode, true);

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

        for (CheckBox checkBox : checkBoxesStyle) {

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


}
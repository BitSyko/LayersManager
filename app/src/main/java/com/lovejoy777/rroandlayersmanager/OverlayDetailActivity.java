package com.lovejoy777.rroandlayersmanager;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Fragment;
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
import android.util.TypedValue;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.bitsyko.liblayers.Callback;
import com.bitsyko.liblayers.Layer;
import com.bitsyko.liblayers.LayerFile;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;
import com.stericson.RootTools.RootTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OverlayDetailActivity extends Fragment implements AsyncResponse {

    List<Integer> InstallOverlayList = new ArrayList<>();
    List<String> OverlayPathList = new ArrayList<>();
    List<String> OverlayColorListPublic = new ArrayList<>();
    private ArrayList<String> paths = new ArrayList<>();
    private ArrayList<CheckBox> checkBoxes = new ArrayList<>();

    private String themeName;
    private String ThemeFolder;
    private String ThemeFolderGeneral;
    private String package2;
    private String whichColor;
    int atleastOneIsClicked;

    private Layer layer;

    private int NumberOfColors;
    int NumberOfOverlays;
    int NumberOfColorOverlays;

    private Switch installEverything;
    private FloatingActionButton fab2;
    private CoordinatorLayout cordLayout;
    private LoadDrawables imageLoader;
    public CheckBox dontShowAgain;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        cordLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_plugindetail, container, false);

        setHasOptionsMenu(true);

        getIntent();

        cordLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                loadBackdrop();
            }
        });


        createLayouts();

        Log.d("Colors", String.valueOf(layer.getColors()));

        return cordLayout;
    }

    @Override
    public void onDestroy() {
        if (imageLoader.getStatus() != AsyncTask.Status.FINISHED) {
            imageLoader.cancel(true);
        }
        super.onDestroy();
    }

    private boolean isAnyCheckboxEnabled() {

        for (CheckBox checkBox : checkBoxes) {
            if (checkBox.isChecked()) {
                return true;
            }
        }
        return false;
    }


    private void refreshFab() {

        if (isAnyCheckboxEnabled()) {
            fab2.show();
        } else {
            fab2.hide();
        }

    }

    private void loadOverlayCardviews() {
        //sort cardviews by number of Overlays
        LinearLayout CardViewCategory1, CardViewCategory2;
        TextView Category1Name, Category2Name;
        if (NumberOfOverlays >= NumberOfColorOverlays) {
            CardViewCategory1 = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory1);
            CardViewCategory2 = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory2);
            Category1Name = (TextView) cordLayout.findViewById(R.id.Tv_Category1Name);
            Category1Name.setText(getResources().getString(R.string.Category1Name));
            Category2Name = (TextView) cordLayout.findViewById(R.id.Tv_Category2Name);
            Category2Name.setText(getResources().getString(R.string.Category2Name));
        } else {
            CardViewCategory1 = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory2);
            CardViewCategory2 = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutCategory1);
            Category1Name = (TextView) cordLayout.findViewById(R.id.Tv_Category2Name);
            Category1Name.setText(getResources().getString(R.string.Category1Name));
            Category2Name = (TextView) cordLayout.findViewById(R.id.Tv_Category1Name);
            Category2Name.setText(getResources().getString(R.string.Category2Name));
        }


        List<LayerFile> layerFiles = layer.getLayersInPackage();


        for (LayerFile layerFile : layerFiles) {

            TableRow row = new TableRow(getActivity());
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            CheckBox check = new CheckBox(getActivity());

            check.setText(layerFile.getName());
            check.setTag(layerFile);

            row.addView(check);

            if (layerFile.isColor()) {
                CardViewCategory2.addView(row);
            } else {
                CardViewCategory1.addView(row);
            }

            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    refreshFab();
                }
            });

            checkBoxes.add(check);

        }

        /*

        //If there arent any color specific Overlays, hide the cardview
        if (NumberOfColorOverlays == 0 || NumberOfOverlays == 0) {
            CardView CardViewCategory = (CardView) cordLayout.findViewById(R.id.CardViewCategory2);
            CardViewCategory.setVisibility(View.GONE);
        }

        */
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
        //switch
        installEverything = (Switch) cordLayout.findViewById(R.id.allswitch);

        //Hide the FAB
        fab2 = (android.support.design.widget.FloatingActionButton) cordLayout.findViewById(R.id.fab2);
        fab2.hide();

        //Initialize Layout
        Toolbar toolbar = (Toolbar) cordLayout.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_back);


        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                installTheme();
            }
        });

        installEverything.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkall();
                } else {
                    uncheckAllCheckBoxes();
                }
            }
        });

    }

    private void getIntent() {
        Bundle bundle2 = this.getArguments();
        package2 = bundle2.getString("PackageName");
        try {
            layer = Layer.layerFromPackageName(package2, getActivity().getApplicationContext());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
        Log.d("PackageName: ", package2);
    }

    private void loadBackdrop() {

        ImageView imageView = (ImageView) cordLayout.findViewById(R.id.backdrop);

        Drawable promo = layer.getPromo();

        imageView.setImageDrawable(promo);

        final CollapsingToolbarLayout Collapsingtoolbar = (CollapsingToolbarLayout) cordLayout.findViewById(R.id.collapsing_toolbar);

        Palette.from(((BitmapDrawable) promo).getBitmap()).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch != null) {
                    Collapsingtoolbar.setContentScrimColor(vibrantSwatch.getRgb());
                    float[] hsv = new float[3];
                    Color.colorToHSV(vibrantSwatch.getRgb(), hsv);
                    hsv[2] *= 0.8f;
                    //int colorPrimaryDark = Color.HSVToColor(hsv);
                    Window window = getActivity().getWindow();
                    window.setStatusBarColor(Color.HSVToColor(hsv));
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
                Animation fadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fadein);
                LinearLayout WN = (LinearLayout) cordLayout.findViewById(R.id.lin2);
                WN.setVisibility(View.VISIBLE);
                WN.startAnimation(fadeInAnimation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isAdded()) {
                    loadOverlayCardviews();
                }
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
    public void installTheme() {

       boolean isThereColorOverlay = false;

        for (CheckBox checkBox : checkBoxes) {
            if (((LayerFile)checkBox.getTag()).isColor()) {
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void uncheckAllCheckBoxes() {

        for (CheckBox checkBox : checkBoxes) {

            if (checkBox.isChecked()) {
                checkBox.performClick();
            }

        }

    }

    private void checkall() {

        fab2.show();

        for (CheckBox checkBox : checkBoxes) {
            if (!checkBox.isChecked()) {
                checkBox.performClick();
            }
        }

    }


    ///////////
    //Snackbars

    private void installationFinishedSnackBar() {

        //show SnackBar after sucessfull installation of the overlays
        final View coordinatorLayoutView = cordLayout.findViewById(R.id.main_content);
        Snackbar.make(coordinatorLayoutView, R.string.OverlaysInstalled, Snackbar.LENGTH_LONG)
                .setAction(R.string.Reboot, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Commands.reboot(getActivity());
                    }
                })
                .show();
    }


    /////////
    //Dialogs
    public void installDialog() {

        //if (showInstallationConfirmDialog()) {
        AlertDialog.Builder installdialog = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        View dontShowAgainLayout = inflater.inflate(R.layout.dialog_donotshowagain, null);
        dontShowAgain = (CheckBox) dontShowAgainLayout.findViewById(R.id.skip);

        installdialog.setView(dontShowAgainLayout);
        installdialog.setTitle(R.string.MoveOverlays);
        installdialog.setMessage(R.string.ApplyOverlays);
        installdialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                if (dontShowAgain.isChecked()) {
                    SharedPreferences myprefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = myprefs.edit();
                    editor.putString("ConfirmInstallationDialog", "checked");
                    editor.apply();
                }

                //start async task to install the Overlays
                InstallAsyncOverlays();
            }
        });
        installdialog.setNegativeButton(android.R.string.cancel, null);

        SharedPreferences myprefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String skipMessage = myprefs.getString("ConfirmInstallationDialog", "unchecked");
        if (!skipMessage.equals("checked")) {
            installdialog.show();
        } else {
            InstallAsyncOverlays();
        }
    }

    private void InstallAsyncOverlays() {

        List<LayerFile> layersToInstall = new ArrayList<>();

        for (CheckBox checkBox : checkBoxes) {

            if (checkBox.isChecked()) {
                LayerFile layerFile = (LayerFile) checkBox.getTag();
                layersToInstall.add(layerFile);
            }

        }

        new Commands.InstallOverlaysBetterWay(layersToInstall, whichColor, getActivity()).execute();


    }

    public void processFinish() {
        fab2.setClickable(true);
        installationFinishedSnackBar();
        uncheckAllCheckBoxes();
        paths.clear();
        installEverything.setChecked(false);
    }

    //Dialog to choose color
    public void colorDialog() {

        final AlertDialog.Builder colorDialog = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        colorDialog.setTitle(R.string.pick_color);
        View colordialogView = inflater.inflate(R.layout.dialog_colors, null);
        colorDialog.setView(colordialogView);

        for (String color : layer.getColors()) {

            RadioGroup my_layout = (RadioGroup) colordialogView.findViewById(R.id.radiogroup);

            RadioGroup.LayoutParams params
                    = new RadioGroup.LayoutParams(getActivity(), null);

            params.leftMargin = 66;
            params.topMargin = 2;
            params.bottomMargin = 2;
            params.width = RadioGroup.LayoutParams.MATCH_PARENT;

            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, getResources().getDisplayMetrics());
            params.height = height;


            final RadioButton radioButton = new RadioButton(getActivity());

            radioButton.setText(color);
            radioButton.setLayoutParams(params);
            radioButton.setTextSize(18);

            my_layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    whichColor = (String) radioButton.getText();
                }
            });

            my_layout.addView(radioButton);
        }

        colorDialog.setCancelable(false);
        colorDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int whichRadioButton = 0;
                SharedPreferences myPrefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                for (int e = 0; e < NumberOfColors; e++) {
                    if (whichColor.equals(OverlayColorListPublic.get(e))) {
                        whichRadioButton = e;

                    }
                }
                SharedPreferences.Editor editor = myPrefs.edit();
                editor.putInt("ColorDialogRadioButton", whichRadioButton);
                editor.apply();
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


    private class LoadDrawables extends AsyncTask<Void, Drawable, Void> {

        LinearLayout screenshotLayout;

        @Override
        protected void onPreExecute() {
            screenshotLayout = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutScreenshots);
        }

        @Override
        protected void onProgressUpdate(Drawable... values) {

            for (Drawable screenshot : values) {

                ImageView screenshotImageView = new ImageView(getActivity());

                Bitmap bitmap = ((BitmapDrawable) screenshot).getBitmap();

                //TODO: Rewrite
                if (bitmap.getHeight() > 1000) {
                    screenshotImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.4), (int) (bitmap.getHeight() * 0.4), true));
                } else {
                    screenshotImageView.setImageBitmap(bitmap);
                }

                LinearLayout linear = new LinearLayout(getActivity());

                linear.addView(screenshotImageView);
                screenshotLayout.addView(linear);
            }


        }

        @Override
        protected Void doInBackground(Void... params) {
            layer.getScreenShots(new Callback<Drawable>() {
                @Override
                public void callback(Drawable object) {
                    publishProgress(object);
                }
            });
            return null;
        }

    }

    @Override
    public void onDestroyView() {
        loadBackdrop2();
        try {
            layer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onDestroyView();
    }

    public void onInstallationFinished() {
        //cordLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_plugindetail, container, false);
        //fab2 = (android.support.design.widget.FloatingActionButton) cordLayout.findViewById(R.id.fab2);
        fab2.setClickable(true);
        installationFinishedSnackBar();
        uncheckAllCheckBoxes();
        installEverything.setChecked(false);
    }
}
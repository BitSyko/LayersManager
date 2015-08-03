package com.lovejoy777.rroandlayersmanager;

import android.animation.Animator;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.bitsyko.liblayers.Callback;
import com.bitsyko.liblayers.Layer;
import com.lovejoy777.rroandlayersmanager.commands.Commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OverlayDetailActivity extends Fragment implements AsyncResponse {

    List<Integer> InstallOverlayList = new ArrayList<>();
    List<String> OverlayPathList = new ArrayList<>();
    List<String> OverlayColorListPublic = new ArrayList<>();
    List<String> OverlayNameList = null;
    private ArrayList<String> paths = new ArrayList<>();

    private String ThemeName;
    private String ThemeFolder;
    private String ThemeFolderGeneral;
    private String package2;
    private String whichColor;
    int atleastOneIsClicked;

    private Layer layer;

    private int NumberOfColors;
    int NumberOfOverlays;
    int NumberOfColorOverlays;
    public static final int NumberOfScreenshotsMain = 3;

    private Switch installEverything;
    private FloatingActionButton fab2;
    private CoordinatorLayout cordLayout;
    private LoadDrawables imageLoader;
    final ImageView ScreenshotimageView[] = new ImageView[NumberOfScreenshotsMain];
    public CheckBox dontShowAgain;

    Bitmap bitmap[] = new Bitmap[NumberOfScreenshotsMain];


    /**
     * Called when the activity is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        cordLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_plugindetail, container, false);

        setHasOptionsMenu(true);

        getIntent();

        //loadBackdrop();

        cordLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                v.removeOnLayoutChangeListener(this);
                loadBackdrop();
            }
        });


        createLayouts();

        return cordLayout;
    }

    @Override
    public void onDestroy() {
        if (imageLoader.getStatus() != AsyncTask.Status.FINISHED) {
            imageLoader.cancel(true);
        }
        super.onDestroy();
    }


    private void createThemeFolder() {
        //create the Theme folder
        File ThemeDirectory = new File(Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeName.replaceAll(" ", "") + "/");
        ThemeDirectory.mkdirs();
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

        //Cardview with normal Overlays
        for (int i = 0; i < NumberOfOverlays; i++) {
            TableRow row = new TableRow(getActivity());
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            final CheckBox[] check = new CheckBox[NumberOfOverlays];
            check[i] = new CheckBox(getActivity());
            check[i].setText(OverlayNameList.get(i));
            check[i].setTag(i);
            check[i].setId(i);
            check[i].setTextColor(getResources().getColor(R.color.chooser_text_color));
            check[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                //check if checkbox is clicked
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    for (int c = 0; c < NumberOfOverlays; c++) {
                        if (buttonView.getTag().equals(c)) {
                            if (buttonView.isChecked()) {
                                InstallOverlayList.set(c, 1);

                                atleastOneIsClicked = atleastOneIsClicked + 1;

                            } else {

                                InstallOverlayList.set(c, 0);
                                atleastOneIsClicked = atleastOneIsClicked - 1;
                            }
                            if (atleastOneIsClicked > 0) {
                                fab2.show();
                            } else {
                                fab2.hide();
                            }
                        }
                    }
                }
            });
            row.addView(check[i]);
            CardViewCategory1.addView(row);
        }

        //CardView with color specific Overlays
        for (int i = NumberOfOverlays + 1; i < NumberOfColorOverlays + NumberOfOverlays + 1; i++) {
            TableRow row = new TableRow(getActivity());

            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            final CheckBox[] check = new CheckBox[NumberOfColorOverlays + NumberOfOverlays + 1];
            check[i] = new CheckBox(getActivity());
            check[i].setText(OverlayNameList.get(i));
            check[i].setTag(i);
            check[i].setId(i);
            check[i].setTextColor(getResources().getColor(R.color.chooser_text_color));
            check[i].setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    for (int c = NumberOfOverlays + 1; c < NumberOfColorOverlays + NumberOfOverlays + 1; c++) {

                        if (buttonView.getTag().equals(c)) {

                            if (buttonView.isChecked()) {
                                InstallOverlayList.set(c, 1);

                                atleastOneIsClicked = atleastOneIsClicked + 1;

                            } else {
                                atleastOneIsClicked = atleastOneIsClicked - 1;
                                InstallOverlayList.set(c, 0);
                            }
                            if (atleastOneIsClicked > 0) {
                                fab2.show();
                            } else {
                                fab2.hide();
                            }
                        }
                    }
                }
            });
            row.addView(check[i]);
            CardViewCategory2.addView(row);
        }


        //If there arent any color specific Overlays, hide the cardview
        if (NumberOfColorOverlays == 0 || NumberOfOverlays == 0) {
            CardView CardViewCategory = (CardView) cordLayout.findViewById(R.id.CardViewCategory2);
            CardViewCategory.setVisibility(View.GONE);
        }
    }

    private void loadScreenshotCardview() {
        loadScreenshots();
    }

    private void generateFilepaths() {
        //Generate filepaths of normal Overlays
        if (NumberOfOverlays == 0) {
            for (int i = 1; i < NumberOfOverlays + NumberOfColorOverlays + 1; i++) {
                String CurrentOverlyName = OverlayNameList.get(i).replaceAll(" ", "");
                OverlayPathList.add(i, ThemeName.replaceAll(" ", "") + "_" + CurrentOverlyName + ".apk");
            }
        } else {
            for (int i = 0; i < NumberOfOverlays + NumberOfColorOverlays + 1; i++) {
                String CurrentOverlyName = OverlayNameList.get(i).replaceAll(" ", "");
                OverlayPathList.add(i, ThemeName.replaceAll(" ", "") + "_" + CurrentOverlyName + ".apk");
            }
        }

    }


    private void receiveAndUseData() {
        //get important data from Plugin´s Manifest
        String Description;
        String OverlayNameString;
        String NormalOverlayNameString;
        String StyleSpecificOverlayString;
        String WhatsNew;
        String OverlayColorString;
        ApplicationInfo ai = null;
        try {
            ai = getActivity().getPackageManager().getApplicationInfo(package2, PackageManager.GET_META_DATA);
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
        ThemeFolder = Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeName.replaceAll(" ", "") + "/";
        ThemeFolderGeneral = ThemeFolder + "General/";


        if (OverlayNameString != null) {
            OverlayNameList = new ArrayList<>(Arrays.asList(OverlayNameString.split(",")));
        } else {
            if (!NormalOverlayNameString.isEmpty()) {
                OverlayNameList = new ArrayList<>(Arrays.asList(NormalOverlayNameString.split(",")));
                OverlayNameList.add(" ");
                if (!StyleSpecificOverlayString.isEmpty()) {
                    OverlayNameList.addAll(Arrays.asList(StyleSpecificOverlayString.split(",")));
                }
            } else {
                if (StyleSpecificOverlayString != null) {
                    OverlayNameList = new ArrayList<>();
                    OverlayNameList.add(" ");
                    OverlayNameList.addAll(Arrays.asList(StyleSpecificOverlayString.split(",")));
                }
            }
        }

        List<String> OverlayColorList = null;
        if (OverlayColorString != null) {
            OverlayColorList = new ArrayList<>(Arrays.asList(OverlayColorString.split(",")));
        }

        if (OverlayNameList != null) {
            NumberOfOverlays = OverlayNameList.indexOf(" ");
            NumberOfColorOverlays = OverlayNameList.size() - OverlayNameList.indexOf(" ") - 1;
        }


        NumberOfColors = OverlayColorList.size();

        TextView tv_description = (TextView) cordLayout.findViewById(R.id.HeX1);
        tv_description.setText(Description);

        TextView tv_whatsNew = (TextView) cordLayout.findViewById(R.id.tv_whatsNew);
        tv_whatsNew.setText(WhatsNew);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) cordLayout.findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(ThemeName);

        for (int i = 0; i < OverlayColorList.size(); i++) {
            OverlayColorListPublic.add(OverlayColorList.get(i));
        }
        for (int i = 0; i < NumberOfColorOverlays + NumberOfOverlays + 1; i++) {
            //OverlayPathList.add(null);
            InstallOverlayList.add(0);
        }
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
                fab2.setClickable(false);
                installTheme();
            }
        });

        installEverything.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkall();
                } else {
                    UncheckAllCheckBoxes("Uncheck");
                }
            }
        });

    }

    private void getIntent() {
        Bundle bundle2 = this.getArguments();
        package2 = bundle2.getString("PackageName");
        try {
            layer = Layer.layerFromPackageName(package2, getActivity());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Log.d("PackageName: ", package2);
    }

    private void loadBackdrop() {

        ImageView imageView = (ImageView) cordLayout.findViewById(R.id.backdrop);
/*
        final String packName = package2;
        String mDrawableName = "heroimage";
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
        //ImageView imageView = (ImageView) cordLayout.findViewById(R.id.backdrop);
        if (myDrawable != null) {
            imageView.setImageDrawable(myDrawable);
        }
        */

        Drawable promo = layer.getPromo();

        imageView.setImageDrawable(promo);


        final CollapsingToolbarLayout Collapsingtoolbar = (CollapsingToolbarLayout) cordLayout.findViewById(R.id.collapsing_toolbar);


        Palette.from(((BitmapDrawable) promo).getBitmap()).generate(new Palette.PaletteAsyncListener() {
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                Palette.Swatch test = palette.getLightVibrantSwatch();
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
                    createThemeFolder();
                    generateFilepaths();
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

    private static final String LOG_TAG = "InvokeOp";

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

        //when a color checkbox is checked
        if (NumberOfSelectedColorOverlays != 0) {
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

    private void UncheckAllCheckBoxes(String Mode) {

        if (Mode.equals("Uncheck")) {

            fab2.hide();

            for (int i = 0; i < NumberOfOverlays; i++) {
                CheckBox checkBox = (CheckBox) cordLayout.findViewById(i);
                checkBox.setChecked(false);
                InstallOverlayList.set(i, 0);
            }

            for (int i = NumberOfOverlays + 1; i < NumberOfColorOverlays + NumberOfOverlays + 1; i++) {
                CheckBox checkBox = (CheckBox) cordLayout.findViewById(i);
                checkBox.setChecked(false);
                InstallOverlayList.set(i, 0);
            }
        }

        if (Mode.equals("Disable")) {
            //disable all Checkboxes
            for (int i = 0; i < NumberOfOverlays; i++) {
                CheckBox checkBox = (CheckBox) cordLayout.findViewById(i);
                checkBox.setEnabled(false);
            }

            for (int i = NumberOfOverlays + 1; i < NumberOfColorOverlays + NumberOfOverlays + 1; i++) {
                CheckBox checkBox = (CheckBox) cordLayout.findViewById(i);
                checkBox.setEnabled(false);
            }
        }
    }

    private void checkall() {

        fab2.show();

        for (int i = 0; i < NumberOfOverlays; i++) {
            CheckBox checkBox = (CheckBox) cordLayout.findViewById(i);
            checkBox.setChecked(true);
            InstallOverlayList.set(i, 1);
        }

        for (int i = NumberOfOverlays + 1; i < NumberOfColorOverlays + NumberOfOverlays + 1; i++) {
            CheckBox checkBox = (CheckBox) cordLayout.findViewById(i);
            checkBox.setChecked(true);
            InstallOverlayList.set(i, 1);
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
                //(new InstallOverlays()).execute();
                InstallAsyncOverlays();
            }
        });
        installdialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                UncheckAllCheckBoxes("Uncheck");
                installEverything.setChecked(false);
            }
        });

        SharedPreferences myprefs = getActivity().getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String skipMessage = myprefs.getString("ConfirmInstallationDialog", "unchecked");
        if (!skipMessage.equals("checked")) {
            installdialog.show();
        } else {
            InstallAsyncOverlays();
            //(new InstallOverlays()).execute();
        }
    }

    private void InstallAsyncOverlays() {

        ArrayList<Integer> OldInstallOverlay = new ArrayList<>(InstallOverlayList);

        //install Normal Overlays
        for (int i = 0; i < NumberOfOverlays; i++) {
            if (InstallOverlayList.get(i) == 1) {
                InstallOverlayList.set(i, 0);
                paths.add(ThemeFolderGeneral + OverlayPathList.get(i));
            }
        }

        //install Color Specific Overlays
        for (int i4 = NumberOfOverlays + 1; i4 < NumberOfOverlays + NumberOfColorOverlays + 1; i4++) {
            if (InstallOverlayList.get(i4) == 1) {
                InstallOverlayList.set(i4, 0);
                paths.add(ThemeFolder + whichColor + "/" + OverlayPathList.get(i4));
            }
        }
        Commands.InstallOverlays asyncTask = new Commands.InstallOverlays("Plugin", getActivity(), ThemeName.replace(" ", ""), paths, package2, NumberOfOverlays, NumberOfColorOverlays, OldInstallOverlay, whichColor, this);
        asyncTask.execute();
    }

    public void processFinish() {
        fab2.setClickable(true);
        installationFinishedSnackBar();
        UncheckAllCheckBoxes("Uncheck");
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

        for (int i = 0; i < NumberOfColors; i++) {

            RadioGroup my_layout = (RadioGroup) colordialogView.findViewById(R.id.radiogroup);

            RadioGroup.LayoutParams params
                    = new RadioGroup.LayoutParams(getActivity(), null);

            params.leftMargin = 66;
            params.topMargin = 2;
            params.bottomMargin = 2;
            params.width = RadioGroup.LayoutParams.MATCH_PARENT;

            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 44, getResources().getDisplayMetrics());
            params.height = height;


            RadioButton radioButton = new RadioButton(getActivity());

            radioButton.setText(OverlayColorListPublic.get(i));
            radioButton.setId(i);
            radioButton.setTag("r" + i);
            radioButton.setLayoutParams(params);
            radioButton.setTextSize(18);

            my_layout.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {

                    whichColor = OverlayColorListPublic.get(checkedId);
                }
            });

            if (i == 0) {
                radioButton.setChecked(true);
            }
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
                UncheckAllCheckBoxes("Uncheck");
                installEverything.setChecked(false);
                dialog.dismiss();
            }


        });
        colorDialog.create();
        colorDialog.show();
    }


    private class LoadDrawables extends AsyncTask<Void, Drawable, Void> {

        LinearLayout screenshotLayout;
        LinearLayout.LayoutParams params;

        @Override
        protected void onPreExecute() {
            screenshotLayout = (LinearLayout) cordLayout.findViewById(R.id.LinearLayoutScreenshots);

            int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());

            params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            params.rightMargin = margin;


        }

        @Override
        protected void onProgressUpdate(Drawable... values) {

            for (Drawable screenshot : values) {

                ImageView screenshotImageView = new ImageView(getActivity());
                screenshotImageView.setBackgroundColor(getResources().getColor(R.color.accent));

                Bitmap bitmap = ((BitmapDrawable) screenshot).getBitmap();

                //TODO: Rewrite
                if (bitmap.getHeight() > 1000) {
                    screenshotImageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.4), (int) (bitmap.getHeight() * 0.4), true));
                } else {
                    screenshotImageView.setImageBitmap(bitmap);
                }

                LinearLayout linear = new LinearLayout(getActivity());
                linear.setLayoutParams(params);
                
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

    @Override
    public void onDestroyView() {
        loadBackdrop2();
        super.onDestroyView();
    }

    public void onInstallationFinished() {
        //cordLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_plugindetail, container, false);
        //fab2 = (android.support.design.widget.FloatingActionButton) cordLayout.findViewById(R.id.fab2);
        fab2.setClickable(true);
        installationFinishedSnackBar();
        UncheckAllCheckBoxes("Uncheck");
        installEverything.setChecked(false);
    }
}
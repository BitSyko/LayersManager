package com.lovejoy777.rroandlayersmanager.loadingpackages;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.bitsyko.liblayers.Layer;
import com.bitsyko.liblayers.LayerFile;
import com.bitsyko.liblayers.NoFileInZipException;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.activities.OverlayDetailActivity;
import com.lovejoy777.rroandlayersmanager.helper.Helpers;
import com.lovejoy777.rroandlayersmanager.interfaces.StoppableAsyncTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateList extends StoppableAsyncTask<Void, Void, Pair<Set<String>, Set<String>>> {

    private Context context;
    private Layer layer;
    private boolean stop;
    private ProgressDialog progress;

    public CreateList(Context context, Layer layer) {
        this.context = context;
        this.layer = layer;
        this.stop = false;
    }

    @Override
    protected void onPreExecute() {
        progress = new ProgressDialog(context);
        progress.setTitle(R.string.generatingList);
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setMax(layer.getLayersInPackage().size());
        progress.setProgress(0);
        progress.setCancelable(false);
        progress.show();
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    protected Pair<Set<String>, Set<String>> doInBackground(Void... params) {

        Set<String> filesToGreyOut = new HashSet<>();
        Set<String> filesThatDontExist = new HashSet<>();
        filesToGreyOut.add(layer.getVersionCode());
        Collection<String> packages = Helpers.allPackagesInSystem(context);
        List<LayerFile> layerFiles = layer.getLayersInPackage();


        for (LayerFile layerFile : layerFiles) {
            if (layer.getPluginVersion()==3 && layerFile.hasStyles() && !layerFile.isColor()){
                    publishProgress();
            }
            else{

                    publishProgress();

                    if (isCancelled() || stop) {
                        return null;
                    }

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
            }
        try {
            layer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SharedPreferences myprefs = context.getSharedPreferences("layersData", Context.MODE_PRIVATE);


        myprefs.edit().putStringSet(layer.getPackageName(), filesToGreyOut).commit();
        myprefs.edit().putStringSet(layer.getPackageName() + "_dontExist", filesThatDontExist).commit();


        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        progress.incrementProgressBy(1);
    }

    @Override
    protected void onPostExecute(Pair<Set<String>, Set<String>> pair) {
        progress.dismiss();
    }

    @Override
    public void stop() {
        stop = false;
    }
}

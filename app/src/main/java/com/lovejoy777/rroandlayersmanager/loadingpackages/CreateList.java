package com.lovejoy777.rroandlayersmanager.loadingpackages;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;

import com.bitsyko.liblayers.Layer;
import com.bitsyko.liblayers.NoFileInZipException;
import com.bitsyko.liblayers.layerfiles.ColorOverlay;
import com.bitsyko.liblayers.layerfiles.LayerFile;
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
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false);
        progress.show();
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    protected Pair<Set<String>, Set<String>> doInBackground(Void... params) {

        Set<String> filesToGreyOut = new HashSet<>();
        filesToGreyOut.add(layer.getVersionCode());
        Collection<String> packages = Helpers.allPackagesInSystem(context);
        List<LayerFile> layerFiles = layer.getLayersInPackage();


        for (LayerFile layerFile : layerFiles) {
            //We don't check custom style overlays
            if (!layerFile.isCustom()) {

                if (isCancelled() || stop) {
                    return null;
                }

                try {

                    if (layerFile.isColor()) {
                        ((ColorOverlay) layerFile).setColor(layer.getColors().get(0));
                        layerFile.getFile(context);
                        ((ColorOverlay) layerFile).setColor(null);
                    } else {
                        layerFile.getFile(context);
                    }

                    Log.d("Manifest " + layerFile.getName(), layerFile.getRelatedPackage());

                    if (!packages.contains(layerFile.getRelatedPackage())) {
                        filesToGreyOut.add(layerFile.getName());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    //Never remove files
                    filesToGreyOut.add(layerFile.getName());
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

        return null;
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

package com.lovejoy777.rroandlayersmanager.loadingpackages;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.CoordinatorLayout;
import android.util.Pair;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TableRow;

import com.bitsyko.liblayers.Layer;
import com.bitsyko.liblayers.LayerFile;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.interfaces.Callback;
import com.lovejoy777.rroandlayersmanager.views.CheckBoxHolder;

import java.util.List;
import java.util.Set;

//When we have list already generated
public class ShowPackagesFromList extends AbsLoadPackagesAsyncTask<Void, Pair<LayerFile, Boolean>, Void> {

    public ShowPackagesFromList(Context context, CoordinatorLayout cordLayout, Layer layer,
                                Callback<CheckBox> checkBoxCallback,
                                CheckBoxHolder.CheckBoxHolderCallback checkBoxHolderCallback) {
        super(context, cordLayout, layer, checkBoxCallback, checkBoxHolderCallback);
    }

    @Override
    protected Void doInBackground(Void... params) {

        List<LayerFile> layerFiles = layer.getLayersInPackage();

        SharedPreferences myprefs = context.getSharedPreferences("layersData", Context.MODE_PRIVATE);
        Set<String> filesToGreyOut = myprefs.getStringSet(layer.getPackageName(), null);
        Set<String> filesThatDontExist = myprefs.getStringSet(layer.getPackageName() + "_dontExist", null);

        assert filesToGreyOut != null;
        assert filesThatDontExist != null;

        for (LayerFile layerFile : layerFiles) {

            if (isCancelled() || stop) {
                return null;
            }

            if (filesThatDontExist.contains(layerFile.getName())) {
                continue;
            }

            //noinspection unchecked
            publishProgress(new Pair<>(layerFile, filesToGreyOut.contains(layerFile.getName())));
        }


        return null;

    }


    @SafeVarargs
    @Override
    //Layer | if greyed out
    protected final void onProgressUpdate(Pair<LayerFile, Boolean>... values) {

        for (Pair<LayerFile, Boolean> pair : values) {

            LayerFile layerFile = pair.first;

            TableRow row = new TableRow(context);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            CheckBox check = new CheckBox(context);

            check.setText(layerFile.getNiceName());
            check.setTag(layerFile);
            check.setEnabled(!pair.second);

            FrameLayout frameLayout = new CheckBoxHolder(context, check, checkBoxHolderCallback);

            frameLayout.addView(check);
            row.addView(frameLayout);

            if (layerFile.isColor()) {
                linearLayoutCategory2.addView(row);
            } else {
                linearLayoutCategory1.addView(row);
            }


            checkBoxCallback.callback(check);

        }


    }

    /*
    @Override
    protected void onPostExecute(Void aVoid) {

        //No styleSpecific Overlays
        if (linearLayoutCategory2.getChildCount() == 0) {
            cardViewCategory2.setVisibility(View.GONE);
        }
        //No normal Overlays
        if (linearLayoutCategory1.getChildCount() == 0) {
            cardViewCategory1.setVisibility(View.GONE);
        }

        linearLayoutCategory1.invalidate();
        linearLayoutCategory2.invalidate();

    }
*/

}
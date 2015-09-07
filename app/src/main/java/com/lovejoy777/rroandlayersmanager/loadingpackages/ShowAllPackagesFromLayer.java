package com.lovejoy777.rroandlayersmanager.loadingpackages;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TableRow;

import com.bitsyko.liblayers.Layer;
import com.bitsyko.liblayers.LayerFile;
import com.lovejoy777.rroandlayersmanager.interfaces.Callback;
import com.lovejoy777.rroandlayersmanager.views.CheckBoxHolder;

import java.util.List;

//We don't hide/check anything
public class ShowAllPackagesFromLayer extends AbsLoadPackagesAsyncTask<Void, LayerFile, Void> {

    public ShowAllPackagesFromLayer(Context context, CoordinatorLayout cordLayout, Layer layer,
                                    Callback<CheckBox> checkBoxCallback,
                                    CheckBoxHolder.CheckBoxHolderCallback checkBoxHolderCallback) {
        super(context, cordLayout, layer, checkBoxCallback, checkBoxHolderCallback);
    }

    @Override
    protected Void doInBackground(Void... params) {

        List<LayerFile> layerFiles = layer.getLayersInPackage();

        for (LayerFile layerFile : layerFiles) {

            if (isCancelled() || stop) {
                return null;
            }

            publishProgress(layerFile);
        }


        return null;

    }


    @Override
    protected void onProgressUpdate(LayerFile... values) {

        for (LayerFile layerFile : values) {

            TableRow row = new TableRow(context);
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            CheckBox check = new CheckBox(context);

            check.setText(layerFile.getNiceName());
            check.setTag(layerFile);

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
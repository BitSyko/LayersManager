package com.lovejoy777.rroandlayersmanager.loadingpackages;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TableRow;

import com.bitsyko.liblayers.Layer;
import com.bitsyko.liblayers.layerfiles.CustomStyleOverlay;
import com.bitsyko.liblayers.layerfiles.LayerFile;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.interfaces.Callback;
import com.lovejoy777.rroandlayersmanager.views.CheckBoxHolder;

import java.util.List;

public abstract class ShowPackagesWithFilter extends AbsLoadPackagesAsyncTask<Void, Pair<LayerFile, Boolean>, Void> {
    public ShowPackagesWithFilter(Context context, CoordinatorLayout cordLayout, Layer layer, Callback<CheckBox> checkBoxCallback, CheckBoxHolder.CheckBoxHolderCallback checkBoxHolderCallback) {
        super(context, cordLayout, layer, checkBoxCallback, checkBoxHolderCallback);
    }

    abstract boolean isEnabled(LayerFile layerFile);


    @Override
    protected Void doInBackground(Void... params) {

        List<LayerFile> layerFiles = layer.getLayersInPackage();

        for (LayerFile layerFile : layerFiles) {

            if (isCancelled() || stop) {
                return null;
            }

            //noinspection unchecked
            publishProgress(new Pair<>(layerFile, isEnabled(layerFile)));
        }


        return null;

    }


    @SafeVarargs
    @Override
    protected final void onProgressUpdate(Pair<LayerFile, Boolean>... values) {

        for (Pair<LayerFile, Boolean> pair : values) {

            LayerFile layerFile = pair.first;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TableRow row = (TableRow) inflater.inflate(R.layout.tablerow_detailedview, null);

            final CheckBox check = (CheckBox) row.findViewById(R.id.CheckBox);

            check.setEnabled(pair.second);

            check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    checkBoxHolderCallback.onClick(check, isChecked);
                }
            });

            check.setText(layerFile.getNiceName());
            check.setTag(layerFile);

            if (layerFile.isCustom()) {

                Spinner spinner = (Spinner) row.findViewById(R.id.Spinner);
                spinner.setVisibility(View.VISIBLE);

                ArrayAdapter adapter = new ArrayAdapter<>(context, R.layout.simple_spinner_item_align_right, ((CustomStyleOverlay) layerFile).getStyles());

                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);

                ll_category1.addView(row);

            } else if (layerFile.isColor()) {
                ll_category2.addView(row);
            } else {
                ll_category1.addView(row);
            }


            checkBoxCallback.callback(check);

        }


    }

    @Override
    protected void onPostExecute(Void nothing) {
        super.onPostExecute(nothing);
        Spinner spinner = (Spinner) cv_category2.findViewById(R.id.sp_plugindetail_styleOverlays);
        ArrayAdapter adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, layer.getColors());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

}

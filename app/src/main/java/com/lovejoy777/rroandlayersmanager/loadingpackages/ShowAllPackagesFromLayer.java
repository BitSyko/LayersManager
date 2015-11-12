package com.lovejoy777.rroandlayersmanager.loadingpackages;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TableRow;

import com.bitsyko.liblayers.Layer;
import com.bitsyko.liblayers.layerfiles.LayerFile;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.adapters.ColorSpinnerAdapter;
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

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TableRow row = (TableRow) inflater.inflate(R.layout.tablerow_detailedview, null);


            // TableRow row = new TableRow(context);
            //  row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            CheckBox check = (CheckBox) row.findViewById(R.id.tableRowCheckBox);

            check.setText(layerFile.getNiceName());
            check.setTag(layerFile);

            //  FrameLayout frameLayout = new CheckBoxHolder(context, check, checkBoxHolderCallback);
            // FrameLayout frameLayout = new FrameLayout(context);


            // frameLayout.addView(check);
            //  frameLayout.addView(spinner);
            //  row.addView(frameLayout);

            //if (layerFile.isColor()) {
            if (layerFile.isColor()) {
                linearLayoutCategory2.addView(row);
            } else {
                linearLayoutCategory1.addView(row);
            }


            checkBoxCallback.callback(check);

        }


    }

    @Override
    protected void onPostExecute(Void nothing) {
        super.onPostExecute(nothing);

        //Color spinner

        Spinner spinner = (Spinner) cardViewCategory2.findViewById(R.id.Tv_Category2Spinner);

        ArrayAdapter adapter = new ColorSpinnerAdapter(context, android.R.layout.simple_spinner_item, layer.getColors());

    //    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
         //       R.array.planets_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spinner.setAdapter(adapter);


    }
}
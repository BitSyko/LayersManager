package com.lovejoy777.rroandlayersmanager.loadingpackages;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.CoordinatorLayout;
import android.util.Pair;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TableRow;

import com.bitsyko.liblayers.Layer;
import com.bitsyko.liblayers.layerfiles.LayerFile;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.interfaces.Callback;
import com.lovejoy777.rroandlayersmanager.views.CheckBoxHolder;

import java.util.List;
import java.util.Set;

//When we have list already generated
public class ShowPackagesFromList extends ShowPackagesWithFilter {

    public ShowPackagesFromList(Context context, CoordinatorLayout cordLayout, Layer layer,
                                Callback<CheckBox> checkBoxCallback,
                                CheckBoxHolder.CheckBoxHolderCallback checkBoxHolderCallback) {
        super(context, cordLayout, layer, checkBoxCallback, checkBoxHolderCallback);
    }

    Set<String> filesToGreyOut;

    @Override
    boolean isEnabled(LayerFile layerFile) {
        return !filesToGreyOut.contains(layerFile.getName());
    }

    @Override
    protected Void doInBackground(Void... params) {

        SharedPreferences myprefs = context.getSharedPreferences("layersData", Context.MODE_PRIVATE);
        filesToGreyOut = myprefs.getStringSet(layer.getPackageName(), null);

        return super.doInBackground(params);

    }

}

package com.bitsyko.liblayers.layerfiles;

import android.content.Context;

import java.io.File;

//Only for UninstallFragment
public class SimpleOverlay extends LayerFile {

    public SimpleOverlay(File file) {
        super(null, null);
        this.file = file;
    }

    @Override
    public File getFile(Context context) {
        return file;
    }
}

package com.bitsyko.liblayers.layerfiles;

import com.bitsyko.liblayers.Layer;

import java.io.File;

public class ColorOverlay extends LayerFile {
    public ColorOverlay(Layer parentLayer, String name) {
        super(parentLayer, name);
    }

    @Override
    public File getFile(Object... additionalData) {

        String zipFile = (String) additionalData[0];



        return null;
    }
}

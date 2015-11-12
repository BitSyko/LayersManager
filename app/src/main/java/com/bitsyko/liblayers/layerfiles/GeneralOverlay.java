package com.bitsyko.liblayers.layerfiles;

import com.bitsyko.liblayers.Layer;

import java.io.File;

public class GeneralOverlay extends LayerFile {
    public GeneralOverlay(Layer parentLayer, String name) {
        super(parentLayer, name);
    }

    @Override
    public File getFile(Object... additionalData) {
        return null;
    }
}

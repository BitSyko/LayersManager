package com.bitsyko.liblayers.layerfiles;

import com.bitsyko.liblayers.Layer;

import java.io.File;

public class CustomStyleOverlay extends LayerFile {
    public CustomStyleOverlay(Layer parentLayer, String name) {
        super(parentLayer, name);
    }

    @Override
    public File getFile(Object... additionalData) {
        return null;
    }
}

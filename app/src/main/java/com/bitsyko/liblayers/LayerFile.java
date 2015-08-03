package com.bitsyko.liblayers;

import java.io.File;

public class LayerFile {

    private Layer layer;
    private String name;
    private boolean color;

    public LayerFile(Layer layer, String name, boolean color) {
        this.layer = layer;
        this.name = name;
        this.color = color;
    }

    public File getFile() {
        return null;
    }

    public String getName() {
        return name;
    }

    public boolean isColor() {
        return color;
    }
}

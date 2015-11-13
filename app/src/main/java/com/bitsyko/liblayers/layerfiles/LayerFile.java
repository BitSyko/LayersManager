package com.bitsyko.liblayers.layerfiles;

import android.content.Context;
import android.support.annotation.NonNull;

import com.bitsyko.liblayers.Layer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class LayerFile implements Comparable<LayerFile> {

    protected Layer parentLayer;
    protected String name;

    public LayerFile(Layer parentLayer, String name) {
        this.parentLayer = parentLayer;
        this.name = name;
    }

    public abstract File getFile(Context context);

    public String getNiceName() {
        return StringUtils.strip(StringUtils.removeStartIgnoreCase(name.replaceAll("_", " ").replace(".apk", "").replace(".zip", ""), StringUtils.deleteWhitespace(parentLayer.getName())));
    }

    public String getName() {
        return name;
    }

    public boolean isColor() {
        return this instanceof ColorOverlay;
    }

    public boolean isCustom() {
        return this instanceof CustomStyleOverlay;
    }

    public Layer getLayer() {
        return parentLayer;
    }

    @Override
    public String toString() {
        return getNiceName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        LayerFile layerFile = (LayerFile) o;

        return new EqualsBuilder()
                .append(parentLayer, layerFile.parentLayer)
                .append(name, layerFile.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(parentLayer)
                .append(name)
                .toHashCode();
    }

    @Override
    public int compareTo(@NonNull LayerFile another) {
        return getNiceName().compareTo(another.getNiceName());
    }
}

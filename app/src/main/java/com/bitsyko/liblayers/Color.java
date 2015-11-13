package com.bitsyko.liblayers;

import android.support.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;

//To hold layer's colors (to hold .zip name with name to display)
public class Color implements Comparable<Color> {

    String zip;
    Layer parentLayer;

    public Color(String zip, Layer parentLayer) {
        this.zip = zip;
        this.parentLayer = parentLayer;
    }

    public String getNiceName() {
        return StringUtils.strip(StringUtils.removeStartIgnoreCase(zip.replaceAll("_", " ").replace(".zip", ""), StringUtils.deleteWhitespace(parentLayer.getName())));
    }

    public String getZip() {
        return zip;
    }

    @Override
    public String toString() {
        return getNiceName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Color color = (Color) o;

        return new EqualsBuilder()
                .append(zip, color.zip)
                .append(parentLayer, color.parentLayer)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(zip)
                .append(parentLayer)
                .toHashCode();
    }

    @Override
    public int compareTo(@NonNull Color another) {
        return getNiceName().compareTo(another.getNiceName());
    }

}

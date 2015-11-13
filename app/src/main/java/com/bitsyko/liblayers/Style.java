package com.bitsyko.liblayers;

import org.apache.commons.lang3.StringUtils;

//Just to override printing method
public class Style extends Color {
    public Style(String zip, Layer parentLayer) {
        super(zip, parentLayer);
    }

    @Override
    public String getNiceName() {
        return StringUtils.substringBefore(super.getNiceName(), "/");
    }

}

package com.bitsyko.libicons.shader.TargetChannel;

import com.bitsyko.libicons.shader.DataHolder;

public class Red implements TargetChannelInterface {

    @Override
    public void save(DataHolder dataHolder, int value) {
        dataHolder.R = value;
    }

    @Override
    public int load(DataHolder dataHolder) {
        return dataHolder.R;
    }

}

package com.bitsyko.liblayers.layerfiles;

import android.content.Context;

import com.bitsyko.liblayers.Color;
import com.bitsyko.liblayers.Layer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class ColorOverlay extends LayerFile {

    private Color selectedColor;

    public ColorOverlay(Layer parentLayer, String name) {
        super(parentLayer, name);
    }

    @Override
    public File getFile(Context context) {

        String cacheDir = context.getCacheDir() + File.separator + StringUtils.deleteWhitespace(parentLayer.getName()) + File.separator;

        if (!new File(cacheDir).exists()) {
            new File(cacheDir).mkdirs();
        }

        File colorZipFile = new File(cacheDir + selectedColor.getZip());

        if (!colorZipFile.exists()) {

            try {
                InputStream inputStream = parentLayer.getResources().getAssets().open("Files" + File.separator + selectedColor.getZip());
                FileUtils.copyInputStreamToFile(inputStream, colorZipFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        File apkFile = new File(cacheDir + name);


        try {
            ZipFile generalZipFileAsZip = new ZipFile(colorZipFile);
            InputStream inputStream = generalZipFileAsZip.getInputStream(generalZipFileAsZip.getEntry(name));

            FileUtils.copyInputStreamToFile(inputStream, apkFile);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return apkFile;
    }

    public void setColor(Color color) {
        selectedColor = color;
    }


}

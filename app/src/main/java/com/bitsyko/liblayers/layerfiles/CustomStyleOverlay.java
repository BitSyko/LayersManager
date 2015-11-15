package com.bitsyko.liblayers.layerfiles;

import android.content.Context;

import com.bitsyko.liblayers.Color;
import com.bitsyko.liblayers.Layer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipFile;

public class CustomStyleOverlay extends LayerFile {

    private List<Color> styles;
    private Color selectedColor;

    public CustomStyleOverlay(Layer parentLayer, String name, List<Color> styles) {
        super(parentLayer, name);
        this.styles = styles;
        Collections.sort(this.styles);
    }

    public List<Color> getStyles() {
        return styles;
    }

    public void setColor(Color color) {
        selectedColor = color;
    }

    @Override
    public File getFile(Context context) {

        String cacheDir = context.getCacheDir() + File.separator + StringUtils.deleteWhitespace(parentLayer.getName()) + File.separator;

        if (!new File(cacheDir).exists()) {
            new File(cacheDir).mkdirs();
        }

        File customStyleZipFile = new File(cacheDir + name);

        if (!customStyleZipFile.exists()) {

            try {
                InputStream inputStream = parentLayer.getResources().getAssets().open("Files" + File.separator + name);
                FileUtils.copyInputStreamToFile(inputStream, customStyleZipFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        File apkFile = new File(cacheDir + selectedColor.getZip());


        try {
            ZipFile generalZipFileAsZip = new ZipFile(customStyleZipFile);
            InputStream inputStream = generalZipFileAsZip.getInputStream(generalZipFileAsZip.getEntry(selectedColor.getZip()));

            FileUtils.copyInputStreamToFile(inputStream, apkFile);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        file = apkFile;

        return apkFile;
    }
}

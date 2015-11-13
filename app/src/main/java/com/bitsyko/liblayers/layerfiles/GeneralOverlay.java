package com.bitsyko.liblayers.layerfiles;

import android.content.Context;

import com.bitsyko.liblayers.Layer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipFile;

public class GeneralOverlay extends LayerFile {

    public GeneralOverlay(Layer parentLayer, String name) {
        super(parentLayer, name);
    }

    @Override
    public File getFile(Context context) {

        String generalZip = parentLayer.getGeneralZip();

        String cacheDir = context.getCacheDir() + File.separator + StringUtils.deleteWhitespace(parentLayer.getName()) + File.separator;

        if (!new File(cacheDir).exists()) {
            new File(cacheDir).mkdirs();
        }

        File generalZipFile = new File(cacheDir + generalZip);

        if (!generalZipFile.exists()) {

            try {
                InputStream inputStream = parentLayer.getResources().getAssets().open("Files" + File.separator + generalZip);
                FileUtils.copyInputStreamToFile(inputStream, generalZipFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        File apkFile = new File(cacheDir + name);


        try {
            ZipFile generalZipFileAsZip = new ZipFile(generalZipFile);
            InputStream inputStream = generalZipFileAsZip.getInputStream(generalZipFileAsZip.getEntry(name));

            FileUtils.copyInputStreamToFile(inputStream, apkFile);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        file = apkFile;

        return apkFile;
    }
}

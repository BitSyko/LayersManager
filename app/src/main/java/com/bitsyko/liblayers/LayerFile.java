package com.bitsyko.liblayers;

import android.content.res.AssetManager;
import android.util.Pair;

import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.helper.AndroidXMLDecompress;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class LayerFile {

    private Layer layer;
    private String name;
    private boolean color;
    private String colorName;
    private File file;

    private static final String PACKAGE_REGEX = "targetPackage=\"(.*?)\"";

    public LayerFile(Layer layer, String name, boolean color) {
        this.layer = layer;
        this.name = name;
        this.color = color;
    }

    public String getRelatedPackage() {

        if (file == null) {
            throw new RuntimeException("No file to work on");
        }

        ZipFile zip;
        InputStream manifestInputStream;
        byte[] array;

        try {
            zip = new ZipFile(file);
            manifestInputStream = zip.getInputStream(zip.getEntry("AndroidManifest.xml"));
            array = IOUtils.toByteArray(manifestInputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }


        Pattern pattern = Pattern.compile(PACKAGE_REGEX, Pattern.DOTALL);

        Matcher matcher = pattern.matcher(AndroidXMLDecompress.decompressXML(array));

        matcher.find();

        return matcher.group(1);

    }

    public File getFile(String color) throws IOException {

        if (file != null && file.exists() && color.equals(colorName)) {
            return file;
        }

        this.colorName = color;

        String fileName = name.replaceAll(" ", "");

        String tempDir = layer.getCacheDir() + File.separator + layer.getName();

        //We're creating cache directory
        File cacheFolder = new File(tempDir);

        if (!cacheFolder.exists()) {
            cacheFolder.mkdirs();
        }


        //We're checking if zip with apk is in temp
        File zipFile = new File(tempDir + File.separator + layer.getName() + "_" + color + ".zip");

        if (!zipFile.exists()) {
            AssetManager assetManager = layer.getResources().getAssets();

            InputStream in = assetManager.open(("Files" + File.separator + layer.getName() + "_" + color + ".zip").replaceAll(" ", ""));
            FileUtils.copyInputStreamToFile(in, zipFile);
            in.close();

        }


        ZipFile zipFile1;

        if (!layer.mapHasFile(zipFile.getName())) {
            zipFile1 = new ZipFile(zipFile);
            layer.putFileToMap(zipFile1, zipFile.getName());
        } else {
            zipFile1 = layer.getFileFromMap(zipFile.getName());
        }


        //We're extracting apk from zip
        File zipTempDir = new File(tempDir + File.separator + layer.getName() + "_" + color);

        if (!zipTempDir.exists()) {
            zipTempDir.mkdirs();
        }


        File destFile = new File(tempDir +
                File.separator + layer.getName() + "_" + color +
                File.separator + (layer.getName() + "_" + fileName + ".apk").replaceAll(" ", ""));


        if (!destFile.exists()) {
            FileUtils.copyInputStreamToFile(Commands.fileFromZip(zipFile1, layer.getName() + "_" + fileName + ".apk"), destFile);
        }

        file = destFile;

        return destFile;

    }

    public File getFile() throws IOException {
        if (color) {
            throw new RuntimeException("Don't use getFile() for color layers");
        }
        return getFile("General");
    }

    public String getName() {
        return name;
    }

    public String getNiceName() {
        return name.replaceAll("_", " ");
    }

    public boolean isColor() {
        return color;
    }

    public Layer getLayer() {
        return layer;
    }
}

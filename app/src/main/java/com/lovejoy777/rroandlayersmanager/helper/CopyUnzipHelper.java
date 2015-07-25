package com.lovejoy777.rroandlayersmanager.helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Niklas on 01.05.2015.
 */
public class CopyUnzipHelper {
    Context mContext;
    private final static int BUFFER_SIZE = 1024;


    //copy files to sd card
    public void CopyFolderToSDCard(Context context, String ThemeName) {

        //RootTools.deleteFileOrDirectory("/sdcard/Overlays/"+ThemeName, true);
        mContext = context;
        try {

            AssetManager assetFiles = mContext.getAssets();

            String[] files = assetFiles.list("Files");

            //initialize streams
            InputStream in;
            OutputStream out;

            for (String file : files) {

                if (file.toString().equalsIgnoreCase("images")
                        || file.toString().equalsIgnoreCase("js")) {
                    //nothing
                } else {
                    in = assetFiles.open("Files/" + file);
                    out = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeName + "/" + file);
                    copyAssetFiles(in, out);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    private static void copyAssetFiles(InputStream in, OutputStream out) {

        try{
            byte[] buffer = new byte [BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    //unzip
    public void unzip(String ThemeName,  int NumberOfSelectedNormalOverlays, int NumberOfSelectedColorOverlays, String whichColor /*, int NumberOfSelectedAdditionalOverlays*/) {

        System.out.println("NumberSelected "+NumberOfSelectedNormalOverlays);
        String filePath;
        String destinationGeneral = Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeName + "/General/";
        String destinationColor = Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeName + "/";

        if (NumberOfSelectedNormalOverlays > 0 /*|| NumberOfSelectedAdditionalOverlays > 0*/) {

            File GeneralDirectory = new File(Environment.getExternalStorageDirectory() +"/Overlays/"+ThemeName+"/General");
            GeneralDirectory.mkdirs();
            filePath = Environment.getExternalStorageDirectory() + "/Overlays/"+ThemeName+"/"+ThemeName+"_General.zip";

            try {
                FileInputStream inputStream = new FileInputStream(filePath);
                ZipInputStream zipStream = new ZipInputStream(inputStream);
                ZipEntry zEntry;
                while ((zEntry = zipStream.getNextEntry()) != null) {

                    FileOutputStream fout = new FileOutputStream(
                            destinationGeneral + "/" + zEntry.getName());
                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = zipStream.read(buffer)) != -1) {
                        bufout.write(buffer, 0, read);
                    }
                    zipStream.closeEntry();
                    bufout.close();
                    fout.close();

                }
                zipStream.close();
                Log.d("Unzip", "Unzipping complete. path :  " + destinationGeneral);
            } catch (Exception e) {
                Log.d("Unzip", "Unzipping failed");
                e.printStackTrace();
            }
        }

        if (NumberOfSelectedColorOverlays > 0 /*|| NumberOfSelectedAdditionalOverlays > 0*/) {

            File ColorDirectory = new File(Environment.getExternalStorageDirectory() +"/Overlays/"+ThemeName+"/" + whichColor);
            ColorDirectory.mkdirs();

            destinationColor = destinationColor + whichColor;

            filePath = Environment.getExternalStorageDirectory() + "/Overlays/"+ThemeName+"/"+ThemeName+"_" +whichColor + ".zip";
            try {
                FileInputStream inputStream = new FileInputStream(filePath);
                ZipInputStream zipStream = new ZipInputStream(inputStream);
                ZipEntry zEntry;
                while ((zEntry = zipStream.getNextEntry()) != null) {

                    FileOutputStream fout = new FileOutputStream(
                            destinationColor + "/" + zEntry.getName());
                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = zipStream.read(buffer)) != -1) {
                        bufout.write(buffer, 0, read);
                    }
                    zipStream.closeEntry();
                    bufout.close();
                    fout.close();

                }
                zipStream.close();
            } catch (Exception e) {
                Log.d("Unzip", "Unzipping failed");
                e.printStackTrace();
            }

        }
    }
}
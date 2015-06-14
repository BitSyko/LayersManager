package com.lovejoy777.rroandlayersmanager.helper;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    //final String ThemeName = MainActivity.ThemeName;
    private String destinationGeneral = null;
    private String destinationColor = null;




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

            for (int i=0; i < files.length; i++) {

                if (files[i].toString().equalsIgnoreCase("images")
                        || files[i].toString().equalsIgnoreCase("js")) {
                    //nothing
                } else {
                    in= assetFiles.open("Files/" + files[i]);
                    out = new FileOutputStream(Environment.getExternalStorageDirectory()+"/Overlays/"+ThemeName+"/"+files[i]);
                    copyAssetFiles(in, out);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();

        }catch (NullPointerException e) {
            e.printStackTrace();
        }catch (Exception e) {
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

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    //unzip
    public void unzip(String ThemeName,  int NumberOfSelectedNormalOverlays, int NumberOfSelectedColorOverlays, String whichColor /*, int NumberOfSelectedAdditionalOverlays*/) {

        System.out.println("NumberSelected "+NumberOfSelectedNormalOverlays);
        String filePath;
        destinationGeneral = Environment.getExternalStorageDirectory() + "/Overlays/" + ThemeName+"/General/";
        destinationColor = Environment.getExternalStorageDirectory() + "/Overlays/"+ThemeName+"/";

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
                            this.destinationGeneral + "/" + zEntry.getName());
                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read = 0;
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

            destinationColor = destinationColor   + whichColor;

            filePath = Environment.getExternalStorageDirectory() + "/Overlays/"+ThemeName+"/"+ThemeName+"_" +whichColor + ".zip";
            try {
                FileInputStream inputStream = new FileInputStream(filePath);
                ZipInputStream zipStream = new ZipInputStream(inputStream);
                ZipEntry zEntry = null;
                while ((zEntry = zipStream.getNextEntry()) != null) {

                    FileOutputStream fout = new FileOutputStream(
                            this.destinationColor + "/" + zEntry.getName());
                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read = 0;
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
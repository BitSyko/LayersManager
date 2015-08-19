package com.lovejoy777.rroandlayersmanager.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.lovejoy777.rroandlayersmanager.AsyncResponse;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoogleDriveActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("LayersManager", "Google Drive activity");

        URL url = null;

        try {
            url = new URL(getIntent().getDataString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            finish();
            return;
        }


        String path = url.getPath();

        //Get id

        String id = path.replace("/file/d/", "").replace("/view", "");

        URL downloadUrl = null;

        try {
            downloadUrl = new URL("https://docs.google.com/uc?id=" + id + "&export=download");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        final URL finalUrl = url;
        final URL finalDownloadUrl = downloadUrl;
        new Thread(new Runnable() {
            @Override
            public void run() {

                //Get file path from website title

                String fileName = null;

                try {
                    String html = IOUtils.toString(finalUrl);

                    Pattern p = Pattern.compile("<title>(.*?)</title>");
                    Matcher m = p.matcher(html);

                    m.find();

                    fileName = m.group(1).split(" ")[0];

                } catch (IOException e) {
                    e.printStackTrace();
                }

                File downloadCache = new File(GoogleDriveActivity.this.getCacheDir().getAbsolutePath() + File.separator + "downloadCache");
                final File downloadFile = new File(GoogleDriveActivity.this.getCacheDir().getAbsolutePath() + File.separator + "downloadCache" + File.separator + fileName);

                RootCommands.DeleteFileRoot(downloadCache.getAbsolutePath());

                downloadCache.mkdirs();


                try {
                    FileUtils.copyURLToFile(finalDownloadUrl, downloadFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new Commands.InstallZipBetterWay(GoogleDriveActivity.this, new AsyncResponse() {
                            @Override
                            public void processFinish() {
                                finish();
                            }
                        }).execute(downloadFile.getAbsolutePath());
                    }
                });



            }
        }).start();



    }


}

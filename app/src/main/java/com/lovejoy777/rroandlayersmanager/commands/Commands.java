package com.lovejoy777.rroandlayersmanager.commands;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Created by Niklas on 29.06.2015.
 */
public class Commands {
    private ArrayList<String> Files = new ArrayList<String>();


   public ArrayList<String> loadFiles(String directory){
        try {
            String line;
            Process process = Runtime.getRuntime().exec("su");
            OutputStream stdin = process.getOutputStream();
            InputStream stderr = process.getErrorStream();
            InputStream stdout = process.getInputStream();

            stdin.write(("ls -a "+directory+"\n").getBytes());

            stdin.write("exit\n".getBytes());
            stdin.flush();   //flush stream
            stdin.close(); //close stream

            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));

            while ((line = br.readLine()) != null) {

                Files.add(line);
            }
            br.close();
            br =
                    new BufferedReader(new InputStreamReader(stderr));
            while ((line = br.readLine()) != null) {
                Log.e("[Error]", line);
            }
            process.waitFor();//wait for process to finish
            process.destroy();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Files;
    }

}

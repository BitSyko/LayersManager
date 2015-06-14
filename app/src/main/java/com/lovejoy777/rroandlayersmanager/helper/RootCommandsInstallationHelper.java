package com.lovejoy777.rroandlayersmanager.helper;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Created by Niklas on 01.05.2015.
 */
public class RootCommandsInstallationHelper {



    public void installOverlays(String sSUCommand, String sSuCommand2) throws InterruptedException {
        //move Overlays to /system/vendor/overlay
        CommandCapture command = new CommandCapture(0, sSUCommand);
        try {
            RootTools.getShell(true).add(command);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        }
        while (!command.isFinished()){
            Thread.sleep(1);
        }

        CommandCapture command2 = new CommandCapture(0, sSuCommand2);
        try {
            RootTools.getShell(true).add(command2);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (RootDeniedException e) {
            e.printStackTrace();
        }
        while (!command.isFinished()){
            Thread.sleep(1);
        }
    }
}

package com.lovejoy777.rroandlayersmanager.commands;

import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.execution.CommandCapture;

public class RootCommands {

    ////////////
    //Code that belongs to Simple Explorer (licensed under GNU)
    private static final String UNIX_ESCAPE_EXPRESSION = "(\\(|\\)|\\[|\\]|\\s|\'|\"|`|\\{|\\}|&|\\\\|\\?)";

    public static String getCommandLineString(String input) {
        return input.replaceAll(UNIX_ESCAPE_EXPRESSION, "\\\\$1");
    }
    ////////////

    public static void moveRoot(String old, String newDir) {

        try {
            CommandCapture command3 = new CommandCapture(0, "mv -f " + old + " " + newDir);

            RootTools.getShell(true).add(command3);
            while (!command3.isFinished()) {
                Thread.sleep(1);
            }

            RootTools.closeAllShells();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void DeleteFileRoot(String path) {

        try {
            RootTools.deleteFileOrDirectory(getCommandLineString(path), false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

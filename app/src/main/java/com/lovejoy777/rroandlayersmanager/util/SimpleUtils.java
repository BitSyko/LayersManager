/*
 * Copyright (C) 2014 Simple Explorer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package com.lovejoy777.rroandlayersmanager.util;

import android.content.Context;
import android.media.MediaScannerConnection;

import com.lovejoy777.rroandlayersmanager.activities.SettingsActivity;
import com.lovejoy777.rroandlayersmanager.activities.SettingsActivity;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;

public class SimpleUtils {

    private static final int BUFFER = 4096;
    private static final long ONE_KB = 1024;
    private static final BigInteger KB_BI = BigInteger.valueOf(ONE_KB);
    private static final BigInteger MB_BI = KB_BI.multiply(KB_BI);
    private static final BigInteger GB_BI = KB_BI.multiply(MB_BI);
    private static final BigInteger TB_BI = KB_BI.multiply(GB_BI);

    // scan file after move/copy
    public static void requestMediaScanner(final Context context,
                                           final File... files) {
        final String[] paths = new String[files.length];
        int i = 0;
        for (final File file : files) {
            paths[i] = file.getPath();
            i++;
        }
        MediaScannerConnection.scanFile(context, paths, null, null);
    }

    // TODO: fix search with root
    private static void search_file(String dir, String fileName,
                                    ArrayList<String> n) {
        File root_dir = new File(dir);
        String[] list = root_dir.list();

        if (list != null && root_dir.canRead()) {
            for (String aList : list) {
                File check = new File(dir + "/" + aList);
                String name = check.getName();

                if (check.isFile()
                        && name.toLowerCase().contains(fileName.toLowerCase())) {
                    n.add(check.getPath());
                } else if (check.isDirectory()) {
                    if (name.toLowerCase().contains(fileName.toLowerCase())) {
                        n.add(check.getPath());

                        // change this!
                    } else if (check.canRead() && !dir.equals("/")) {
                        search_file(check.getAbsolutePath(), fileName, n);
                    } else if (check.getName().contains("data")) {
                        n.addAll(RootCommands.findFiles(check.getAbsolutePath(), fileName));
                    }
                }
            }
        } else {
            n.addAll(RootCommands.findFiles(dir, fileName));
        }
    }



    public static void moveToDirectory(String old, String newDir) {
        String file_name = old.substring(old.lastIndexOf("/"), old.length());
        File old_file = new File(old);
        File cp_file = new File(newDir + file_name);

        if (!old_file.renameTo(cp_file)) {
            copyToDirectory(old, newDir);
            deleteTarget(old);
        }
    }

    public static void copyToDirectory(String old, String newDir) {
        File old_file = new File(old);
        File temp_dir = new File(newDir);
        byte[] data = new byte[BUFFER];
        int read;

        if (old_file.canWrite() && temp_dir.isDirectory()
                && temp_dir.canWrite()) {
            if (old_file.isFile()) {
                String file_name = old.substring(old.lastIndexOf("/"),
                        old.length());
                File cp_file = new File(newDir + file_name);

                try {
                    BufferedOutputStream o_stream = new BufferedOutputStream(
                            new FileOutputStream(cp_file));
                    BufferedInputStream i_stream = new BufferedInputStream(
                            new FileInputStream(old_file));

                    while ((read = i_stream.read(data, 0, BUFFER)) != -1)
                        o_stream.write(data, 0, read);

                    o_stream.flush();
                    i_stream.close();
                    o_stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (old_file.isDirectory()) {
                String files[] = old_file.list();
                String dir = newDir
                        + old.substring(old.lastIndexOf("/"), old.length());

                if (!new File(dir).mkdir())
                    return;

                for (String file : files) copyToDirectory(old + "/" + file, dir);
            }
        } else {
            if (SettingsActivity.rootAccess())
                RootCommands.moveCopyRoot(old, newDir);
        }
    }

    // filePath = currentDir + "/" + item
    // newName = new name
    public static boolean renameTarget(String filePath, String newName) {
        File src = new File(filePath);

        String temp = filePath.substring(0, filePath.lastIndexOf("/"));
        File dest = new File(temp + "/" + newName);

        return src.renameTo(dest);
    }

    // path = currentDir
    // name = new name
    public static boolean createDir(String path, String name) {
        File folder = new File(path, name);
        boolean success = false;

        if (folder.exists())
            success = false;

        if (folder.mkdir())
            success = true;
        else if (SettingsActivity.rootAccess()) {
            success = RootCommands.createRootdir(folder, path);
        }

        return success;
    }

    public static void deleteTarget(String path) {
        File target = new File(path);

        if (target.isFile() && target.canWrite()) {
            target.delete();
        } else {
            if (target.isDirectory() && target.canRead()) {
                String[] file_list = target.list();

                if (file_list != null && file_list.length == 0) {
                    target.delete();
                    return;
                } else if (file_list != null && file_list.length > 0) {

                    for (String aFile_list : file_list) {
                        File temp_f = new File(target.getAbsolutePath() + "/"
                                + aFile_list);

                        if (temp_f.isDirectory())
                            deleteTarget(temp_f.getAbsolutePath());
                        else if (temp_f.isFile()) {
                            temp_f.delete();
                        }
                    }
                }

                if (target.exists())
                    target.delete();
            } else if (target.exists() && !target.delete()) {
                if (SettingsActivity.rootAccess())
                    RootCommands.DeleteFileRoot(path);
            }
        }
    }





    private static byte[] createChecksum(String filename) throws Exception {
        InputStream fis = new FileInputStream(filename);

        byte[] buffer = new byte[2 * BUFFER];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    // a byte array to a HEX string
    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        String result = "";

        for (byte aB : b) {
            result += Integer.toString((aB & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }







    public static long getDirectorySize(File directory) {
        final File[] files = directory.listFiles();
        long size = 0;

        if (files == null) {
            return 0L;
        }

        for (final File file : files) {
            try {
                if (!isSymlink(file)) {
                    size += sizeOf(file);
                    if (size < 0) {
                        break;
                    }
                }
            } catch (IOException ioe) {
                // ignore exception when asking for symlink
            }
        }

        return size;
    }

    private static boolean isSymlink(File file) throws IOException {
        File fileInCanonicalDir;

        if (file.getParent() == null) {
            fileInCanonicalDir = file;
        } else {
            File canonicalDir = file.getParentFile().getCanonicalFile();
            fileInCanonicalDir = new File(canonicalDir, file.getName());
        }

        return !fileInCanonicalDir.getCanonicalFile().equals(fileInCanonicalDir.getAbsoluteFile());
    }

    private static long sizeOf(File file) {
        if (file.isDirectory()) {
            return getDirectorySize(file);
        } else {
            return file.length();
        }
    }

    public static String getExtension(String name) {
        String ext;

        if (name.lastIndexOf(".") == -1) {
            ext = "";

        } else {
            int index = name.lastIndexOf(".");
            ext = name.substring(index + 1, name.length());
        }
        return ext;
    }
}
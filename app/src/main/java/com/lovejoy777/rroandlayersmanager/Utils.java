package com.lovejoy777.rroandlayersmanager;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v13.app.FragmentCompat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.apache.commons.io.IOUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Utils {

    public static class CommandOutput {
        public String output;
        public String error;
        public int exitCode;
    }

    public static boolean deleteFile(String path) {
        if (!isRootAvailable()) return false;
        try {
            if (new File(path).isDirectory()) {
                runCommand("rm -rf '" + path + "'\n", true);
            } else {
                runCommand("rm -rf '" + path + "'\n", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return !new File(path).exists();
    }

    public static boolean createFolder(File folder) {
        if (!isRootAvailable()) return false;
        runCommand("mkdir " + folder.getPath(), true);
        return true;
    }

    public static boolean applyPermissions(String file, String perms) {
        try {
            runCommand("chmod " + perms + " " + file, true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean applyPermissionsRecursive(String file, String perms) {
        try {
            runCommand("chmod -R " + perms + " " + file, true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean moveFile(String old, String newDir) {
        if (!isRootAvailable()) return false;
        old = "\"" + old + "\"";
        try {
            runCommand("mv -f " + old + " " + newDir, true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return new File(newDir).exists();
    }

    public static boolean copyFile(String old, String newFile) {
        if (!isRootAvailable()) return false;
        try {
            runCommand("cp " + old + " " + newFile, true);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return new File(newFile).exists();
    }

    public static CommandOutput runCommand(String cmd, boolean useRoot) {
        if (!isRootAvailable()) return null;
        CommandOutput output = new CommandOutput();

        Log.d("TEST", "command=" + cmd);
        try {
            Process process = Runtime.getRuntime().exec(useRoot ? "su" : "sh");
            DataOutputStream os = new DataOutputStream(
                    process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();

            output.exitCode = process.waitFor();
            output.output = IOUtils.toString(process.getInputStream());
            output.error = IOUtils.toString(process.getErrorStream());
            Log.d("TEST", "error=\n" + output.error + "\nout=\n" + output.output);
            if (output.exitCode != 0 || (!"".equals(output.error) && null != output.error)) {
                Log.e("Root Error, cmd: " + cmd, output.error);
                return output;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return output;
        }
        return output;
    }

    public static boolean remount(String mountType) {
        if (!isRootAvailable()) return false;
        String folder = DeviceSingleton.getInstance().getMountFolder();
        CommandOutput out =runCommand("mount -o "+mountType +",remount "+ folder + "\n", true);

        return true;
    }

    public static boolean isRootAvailable() {
        return true;
    }

    public static boolean copyAssetFolder(AssetManager assetManager,
                                          String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            if (!new File(toPath).exists() && !new File(toPath).mkdirs()) {
                throw new RuntimeException("cannot create directory: " + toPath);
            }
            boolean res = true;
            for (String file : files) {
                Log.d("TEST", "file=" + file);
                if (assetManager.list(fromAssetPath + "/" + file).length == 0) {
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                } else {
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                }
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean copyAsset(AssetManager assetManager,
                                    String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;

        File parent = new File(toPath).getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new RuntimeException();
        }

        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

}
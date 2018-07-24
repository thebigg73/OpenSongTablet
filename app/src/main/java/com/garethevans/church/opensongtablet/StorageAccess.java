package com.garethevans.church.opensongtablet;

// This class is used to expose storage locations and permissions used by the app (in the near future).
// Still a work in progress

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.net.Uri.encode;

class StorageAccess {

    InputStream getInputStream(Context c, String where, String subfolder, String filename) {
        Uri uri = getSongLocationAsUri(c, where, subfolder, filename);
        InputStream is;
        if (uri==null) {
            return null;
        }
        try {
            is = c.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            is = null;
        }
        return is;
    }

    OutputStream getOutputStream(Context c, String where, String subfolder, String filename) {
        Uri uri = getSongLocationAsUri(c, where, subfolder, filename);
        OutputStream os;
        if (uri==null) {
            return null;
        }try {
            os = c.getContentResolver().openOutputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            os = null;
        }
        return os;
    }

    private Uri getSongLocationAsUri(Context c, String where, String subfolder, String filename) {
        Uri uri;
        // Firstly get the storage location
        Preferences mPreferences = new Preferences();
        uri = Uri.parse(mPreferences.getMyPreferenceString(c, "chosenstorage", null));
        if (uri!=null) {
            try {
                // Define the root folder
                DocumentFile rootFolder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Log.d("StorageAccess", "Root folder=" + uri);
                    rootFolder = DocumentFile.fromTreeUri(c, uri);
                } else {
                    // Kit-Kat doesn't allow document tree
                    rootFolder = DocumentFile.fromSingleUri(c, uri);
                }

                // Get the OpenSong folder
                DocumentFile openSongFolder = rootFolder.findFile("TestOpenSong");

                // Get the required subfolder of OpenSong using 'where'.
                // This could be the Songs, Sets, Variables, etc. folder
                DocumentFile folderLocation = openSongFolder.findFile(where);

                // Go through the folders (may be lots of sub folders, or none) and get the folder uri
                String[] folders = subfolder.split("/");
                for (String folder:folders) {
                    if (!folder.equals(c.getString(R.string.mainfoldername))) {
                        DocumentFile newFolderLocation = folderLocation.findFile(folder);
                        if (newFolderLocation==null) {
                            // Create the folder
                            folderLocation.createDirectory(folder);
                            newFolderLocation = folderLocation.findFile(folder);
                        }
                        folderLocation = newFolderLocation;
                    }
                }

                DocumentFile df = folderLocation.findFile(filename);

                // If the file doesn't exist, create
                if (df==null) {
                    folderLocation.createFile(null, filename);
                    df = folderLocation.findFile(filename);
                }

                return df.getUri();

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }

    DocumentFile getLocation(Context c, String location, Uri appUri) {
        Uri newUri = Uri.withAppendedPath(appUri, encode(location));
        return DocumentFile.fromSingleUri(c,newUri);
    }

}

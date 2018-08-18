package com.garethevans.church.opensongtablet;

// This class is used to expose storage locations and permissions used by the app (in the near future).
// Still a work in progress


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.provider.DocumentFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class StorageAccess {

    private String appFolder = "OpenSongApp";

    Uri homeFolder(Context c) {
        // The user specified a storage folder when they started the app
        // However, this might not be the OpenSong folder, but the folder containing it
        // This function is called once when the app starts and saves the correct uri to a static field
        // The value is also stored in the preferences

        Preferences preferences = new Preferences();
        Uri uri = Uri.parse(preferences.getMyPreferenceString(c,"uriTree", null));
        DocumentFile df = documentFileFromUri(c, uri);
        Uri returnUri;
        if (df!=null && df.exists() &&
                df.getParentFile()!=null && df.getParentFile().exists() && df.getParentFile().getName().equals(appFolder)) {
            // We are already inside in the OpenSong folder
            returnUri = uri;
        } else if (df!=null && df.findFile(appFolder)!=null){
            // We are in a directory that contains the app folder, so we need to move inside that
            df = df.findFile(appFolder);
            returnUri = df.getUri();
        } else if (df!=null){
            // No sign of the App folder, so create it here
            df = df.createDirectory(appFolder);
            returnUri = df.getUri();
        } else {
            returnUri = null;
        }

        // Save the preference
        if (returnUri!=null) {
            preferences.setMyPreferenceString(c,"uriTree",returnUri.toString());
        }
        FullscreenActivity.uriTree = returnUri;
        return returnUri;
    }

    Uri getUriFromPath(String folder, String subfolder, String filename) {
        // The base/tree Uri is the static field in FullscreenActivity
        Uri uri = FullscreenActivity.uriTree;

        // Now point to the specific folder (Songs, Sets, Backgrounds, etc.)
        if (folder!=null) {
            uri = Uri.withAppendedPath(uri,Uri.encode(folder));
        }

        // Now go through the subfolder(s)
        if (subfolder!=null) {
            String[] sfs = subfolder.split("/");
            for (String sf : sfs) {
                if (sf != null && !sf.equals("")) {
                    uri = Uri.withAppendedPath(uri, Uri.encode(sf));
                }
            }
        }

        // Now add the filename
        if (filename!=null && !filename.equals("")) {
            // Might have sent subfolder info
            String[] sfs = filename.split("/");
            for (String sf : sfs) {
                if (sf != null && !sf.equals("")) {
                    uri = Uri.withAppendedPath(uri, Uri.encode(sf));
                }
            }
        }

        // Now return the Uri
        return uri;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    void listSongs(Context c) {

        FullscreenActivity.songIds = new ArrayList<>();

        // The base/tree Uri is the static field in FullscreenActivity
        Uri uri = FullscreenActivity.uriTree;

        // Now point to the Songs folder
        uri = Uri.withAppendedPath(uri,"Songs");

        // Now get a documents contract at this location
        String songFolderId = getDocumentsContractId(uri);

        // Get the child folders
        Uri children = getChildren(uri,songFolderId);

        ContentResolver contentResolver = c.getContentResolver();

        // Keep track of our directory hierarchy
        List<Uri> dirNodes = new LinkedList<>();
        dirNodes.add(children);

        while(!dirNodes.isEmpty()) {
            children = dirNodes.remove(0); // get the item from top
            Cursor cursor = contentResolver.query(children, new String[] {
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);
            try {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        final String docId = cursor.getString(0);
                        final String mime = cursor.getString(2);
                        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                            final Uri newNode = getChildren(children, docId);
                            dirNodes.add(newNode);
                        } else {
                            FullscreenActivity.songIds.add(docId);
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Now extract the song folders from the song ids
        FullscreenActivity.folderIds = new ArrayList<>();
        for (String f:FullscreenActivity.songIds) {
            // Remove the last path
            int endoffolder = f.lastIndexOf("/");
            if (endoffolder>0) {
                f = f.substring(0,endoffolder-1);
            }
            FullscreenActivity.folderIds.add(f);
        }
    }

    private DocumentFile documentFileFromUri(Context c, Uri uri) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
            return DocumentFile.fromTreeUri(c, uri);
        } else {
            return DocumentFile.fromSingleUri(c, uri);
        }
    }

    private String getDocumentsContractId(Uri uri) {
        if (lollipopOrLater()) {
            return DocumentsContract.getTreeDocumentId(uri);
        } else {
            return DocumentsContract.getDocumentId(uri);
        }
    }

    private Uri getChildren(Uri uri, String id) {
        if (lollipopOrLater()) {
            return DocumentsContract.buildChildDocumentsUriUsingTree(uri, id);
        } else {
            return DocumentsContract.buildChildDocumentsUri(uri.getAuthority(),id);
        }
    }

    private boolean lollipopOrLater() {
        return Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP;
    }

    InputStream getInputStream(Context c, Uri uri) {
        try {
            return c.getContentResolver().openInputStream(uri);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    OutputStream getOutputStream(Context c, Uri uri) {
        try {
            return c.getContentResolver().openOutputStream(uri);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}

package com.garethevans.church.opensongtablet;

// This class is used to expose storage locations and permissions used by the app (in the near future).
// Still a work in progress

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.provider.DocumentFile;

import java.util.ArrayList;

import static android.net.Uri.encode;

class StorageAccess {

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    void getStoragePermission(Context c, Uri treeUri) {
        DocumentFile pickedDir = DocumentFile.fromTreeUri(c, treeUri);
        c.grantUriPermission(c.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        c.getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    DocumentFile getLocation(Context c, String location, Uri appUri) {
        Uri newUri = Uri.withAppendedPath(appUri, encode(location));
        return DocumentFile.fromSingleUri(c,newUri);
    }

    DocumentFile createFolder(Context c, String folder, Uri appUri) {
        Uri newUri = Uri.withAppendedPath(appUri,encode(folder));
        DocumentFile u_df = DocumentFile.fromSingleUri(c, appUri);
        DocumentFile df = DocumentFile.fromSingleUri(c,newUri);
        if (!df.exists()) {
            u_df.createDirectory(folder);
            df = DocumentFile.fromSingleUri(c,newUri);
        }
        return df;
    }

    ArrayList<String> listSubDirectories(Context c, String folder, Uri appUri) {
        Uri newUri = Uri.withAppendedPath(appUri, encode(folder));
        DocumentFile df = DocumentFile.fromSingleUri(c, newUri);
        ArrayList<String> l_df = new ArrayList<>();
        DocumentFile[] dfs = df.listFiles();
        for (DocumentFile dfz : dfs) {
            if  (dfz.isDirectory()) {
                l_df.add(dfz.getName());
            }
        }
        return l_df;
    }

}

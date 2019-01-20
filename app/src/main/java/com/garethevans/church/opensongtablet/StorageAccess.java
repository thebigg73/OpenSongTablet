package com.garethevans.church.opensongtablet;

// This class deals with accessing the app storage
// There are two sets of methods
// Newer versions of Android (Lollipop and later) will use content uris
// These are based on the ACTION_OPEN_DOCUMENT_TREE location
// KitKat and below will use file uris based on built in folder chooser


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class StorageAccess {

    String appFolder = "OpenSong";
    private Uri uriTree = null;
    private String[] rootFolders = {"Backgrounds", "Export", "Highlighter", "Images", "Media",
            "Notes", "OpenSong Scripture", "Pads", "Profiles", "Received", "Scripture",
            "Sets", "Settings", "Slides", "Songs", "Variations"};
    private String[] cacheFolders = {"Backgrounds/_cache", "Images/_cache", "Notes/_cache",
            "OpenSong Scripture/_cache", "Scripture/_cache", "Slides/_cache"};


    // These are used primarily on start up to initialise stuff
    String getStoragePreference(Context c, Preferences preferences) {
        return preferences.getMyPreferenceString(c, "uriTree", null);
    }

    Uri homeFolder(Context c, Preferences preferences) {
        // The user specified a storage folder when they started the app
        // However, this might not be the OpenSong folder, but the folder containing it
        // This function is called once when the app starts
        String uriTree = getStoragePreference(c, preferences);
        Uri uri;

        try {
            if (uriTree != null) {
                if (lollipopOrLater()) {
                    uri = Uri.parse(uriTree);
                    if (uri != null) {
                        DocumentFile df = documentFileFromRootUri(c, uri, uriTree);
                        if (df == null || !df.exists()) {
                            uri = null;
                        }
                    }
                } else {
                    File f = new File(uriTree);
                    uri = Uri.fromFile(f);
                }
            } else {
                uri = null;
            }

        } catch (Exception e) {
            // Could be called if the uri stored is for a different version of Android. e.g. after upgrade
            uri = null;
        }
        FullscreenActivity.uriTree = uri;  // This is the chosen route
        return uri;
    }

    private DocumentFile getAppFolderDocumentFile(Context c, Preferences preferences) {
        // This simply gets a documentfile location for the OpenSongApp folder
        // It is then saved to FullscreenActivity.
        // FullscreenActivity.uriTree is already valid and set

        String uriTree = getStoragePreference(c, preferences);
        //Make sure FullscreenActivity.uriTree is set
        homeFolder(c, preferences);
        FullscreenActivity.appHome = null;
        DocumentFile df = documentFileFromRootUri(c, FullscreenActivity.uriTree, uriTree);

        if (df != null) {
            // Check if we are in the app folder
            if (df.getName() != null && df.getName().equals(appFolder)) {
                // Already in the app folder
                FullscreenActivity.appHome = df;
            } else if (df.findFile(appFolder) != null) {
                // Need to move into the app folder
                FullscreenActivity.appHome = df.findFile(appFolder);
            } else {
                // No app folder, so create it
                df.createDirectory(appFolder);
                FullscreenActivity.appHome = df.findFile(appFolder);
            }
        }
        return FullscreenActivity.appHome;
    }

    String createOrCheckRootFolders(Context c, Preferences preferences) {
        try {
            // The OpenSong folder
            DocumentFile root_df = getAppFolderDocumentFile(c, preferences);

            // Go through each folder and check or create the folder
            for (String folder : rootFolders) {
                DocumentFile thisfolder = root_df.findFile(folder);
                if (thisfolder == null) {
                    root_df.createDirectory(folder);
                }
            }

            // Now for the _cache folders
            for (String folder : cacheFolders) {
                String[] bits = folder.split("/");
                DocumentFile firstbit_df = root_df.findFile(bits[0]);
                DocumentFile cache_df = firstbit_df.findFile(bits[1]);
                if (cache_df == null) {
                    firstbit_df.createDirectory(bits[1]);
                }
            }

            // Now copy the assets if they aren't already there
            copyAssets(c);
            return "Success";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error";
        }
    }

    private void copyAssets(Context c) {
        try {
            AssetManager assetManager = c.getAssets();
            String[] files = new String[2];
            files[0] = "backgrounds/ost_bg.png";
            files[1] = "backgrounds/ost_logo.png";
            DocumentFile df = FullscreenActivity.appHome.findFile("Backgrounds");
            for (String filename : files) {
                String filetocopy = filename.replace("backgrounds/", "");
                // See if they are already there first
                if (df != null && df.findFile(filetocopy) == null) {
                    DocumentFile df_image = df.createFile("image/png", filetocopy);
                    Uri outputUri = df_image.getUri();
                    OutputStream out = getOutputStream(c, outputUri);
                    try {
                        InputStream in = assetManager.open(filename);
                        copyFile(in, out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String stringForFile(String folderpath) {
        if (uriTree==null && FullscreenActivity.uriTree!=null) {
            String uriTree_String = FullscreenActivity.uriTree.getPath();
            if (uriTree_String!=null) {
                uriTree = Uri.parse(uriTree_String);
            }
        }
        if (uriTree!=null) {
            String file = uriTree.getPath();
            if (file!=null && !file.endsWith(appFolder)) {
                file = file + "/" + appFolder;
            }
            return file + "/" + folderpath;
        } else {
            return folderpath;
        }
    }

    // These are used to return Uris, Ids, DocumentFiles, etc. for files
    private Uri getUriFromFilePath(Context c, String folder, String subfolder, String filename) {
        String s = stringForFile(folder);
        File f = new File(s);
        if (subfolder != null && !subfolder.isEmpty() && !subfolder.equals(c.getString(R.string.mainfoldername))) {
            f = new File(f, subfolder);
        }
        if (filename != null && !filename.isEmpty() && !filename.equals(c.getString(R.string.mainfoldername))) {
            f = new File(f, filename);
        }
        return Uri.fromFile(f);
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Uri getUriFromPath(Context c, Preferences preferences, String folder, String subfolder, String filename) {
        // Use the appHome uri
        if (FullscreenActivity.appHome==null) {
            FullscreenActivity.appHome = getAppFolderDocumentFile(c, preferences);
        }

        Uri uri = FullscreenActivity.appHome.getUri();

        if (uri != null) {
            // Now point to the specific folder (Songs, Sets, Backgrounds, etc.)
            if (folder != null && !folder.isEmpty()) {
                uri = Uri.withAppendedPath(uri, Uri.encode(folder));
            }

            // Now go through the subfolder(s)
            if (subfolder != null && !subfolder.equals(c.getString(R.string.mainfoldername))) {
                String[] sfs = subfolder.split("/");
                for (String sf : sfs) {
                    if (sf != null && !sf.equals("") && !sf.equals(c.getString(R.string.mainfoldername))) {
                        uri = Uri.withAppendedPath(uri, Uri.encode(sf));
                    }
                }
            }

            // Now add the filename
            if (filename != null && !filename.equals("")) {
                // Might have sent subfolder info
                String[] sfs = filename.split("/");
                for (String sf : sfs) {
                    if (sf != null && !sf.equals("") && !sf.equals(c.getString(R.string.mainfoldername))) {
                        uri = Uri.withAppendedPath(uri, Uri.encode(sf));
                    }
                }
            }
        }

        // Now return the Uri
        if (uri!=null) {

            String uristring = uri.toString();

            int pos = uristring.lastIndexOf("OpenSong/");
            if (pos > 0) {
                String start = uristring.substring(0, pos);
                String end = uristring.substring(pos);
                end = end.replace("/", "%2F");
                uristring = start + end;
                uri = Uri.parse(uristring);
            }
        }
        return uri;
    }

    Uri getFileProviderUri(Context c, Preferences preferences, String folder, String subfolder, String filename) {
        if (lollipopOrLater()) {
            return getFileProviderUri_SAF(c, preferences, folder, subfolder, filename);
        } else {
            return getFileProviderUri_File(c,folder,subfolder,filename);
        }
    }

    private Uri getFileProviderUri_SAF(Context c, Preferences preferences, String folder, String subfolder, String filename) {
        // No real need as we are using content uris anyway
        return getUriForItem(c, preferences, folder, subfolder, filename);
    }
    private Uri getFileProviderUri_File(Context c, String folder, String subfolder, String filename) {
        String s = stringForFile(folder);
        File f = new File(s);
        if (subfolder != null && !subfolder.isEmpty() && !subfolder.equals(c.getString(R.string.mainfoldername))) {
            f = new File(f, subfolder);
        }
        if (filename != null && !filename.isEmpty() && !filename.equals(c.getString(R.string.mainfoldername))) {
            f = new File(f, filename);
        }
        // Convert to a FileProvider uri
        return FileProvider.getUriForFile(c,"OpenSongAppFiles",f);
    }

    DocumentFile documentFileFromRootUri(Context c, Uri uri, String path) {
        if (uri != null && lollipopOrLater()) {
            return DocumentFile.fromTreeUri(c, uri);
        } else if (path != null) {
            File f = new File(path);
            return DocumentFile.fromFile(f);
        } else {
            return null;
        }
    }
    private DocumentFile documentFileFromUri(Context c, Uri uri, String path) {
        if (uri != null && lollipopOrLater()) {
            return DocumentFile.fromSingleUri(c, uri);
        } else if (path != null) {
            File f = new File(path);
            return DocumentFile.fromFile(f);
        } else {
            return null;
        }
    }

    @SuppressLint("NewApi")
    String getDocumentsContractId(Uri uri) {
        if (lollipopOrLater()) {
            return DocumentsContract.getDocumentId(uri);
        } else {
            return null;
        }
    }

    @SuppressLint("NewApi")
    Uri documentUriFromId(String id) {
        if (lollipopOrLater()) {
            return DocumentsContract.buildDocumentUriUsingTree(FullscreenActivity.uriTree, id);
        } else {
            return DocumentsContract.buildDocumentUri(FullscreenActivity.uriTree.getAuthority(), id);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Uri getChildren(Uri uri, String id) {
        if (lollipopOrLater()) {
            return DocumentsContract.buildChildDocumentsUriUsingTree(uri, id);
        } else {

            return DocumentsContract.buildChildDocumentsUri(uri.getAuthority(), id);
        }
    }


    // Basic file actions
    boolean copyFile(InputStream in, OutputStream out) {
        if (in != null && out != null) {
            try {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.flush();
                out.close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    boolean writeFileFromString(String s, OutputStream os) {
        try {
            os.write(s.getBytes());
            os.flush();
            os.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    void writeFileFromDecodedImageString(OutputStream os, byte[] bytes) {
        try {
            os.write(bytes);
            os.flush();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void writeImage(OutputStream outputStream, Bitmap bmp, Bitmap.CompressFormat compressFormat) {
        try {
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            bmp.recycle();
        } catch (Exception | OutOfMemoryError e) {
            e.printStackTrace();
        }
    }
    String readTextFileToString(InputStream in) {
        if (in != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte buf[] = new byte[1024];
            int len;
            try {
                while ((len = in.read(buf)) != -1) {
                    outputStream.write(buf, 0, len);
                }
                outputStream.close();
                in.close();
            } catch (Exception e) {
                Log.d("StorageAccess", "Error reading text file");
                e.printStackTrace();
                return "";
            } catch (OutOfMemoryError e2) {
                e2.printStackTrace();
                return "";
            }
            return outputStream.toString();
        } else {
            return "";
        }
    }

    // Input and output streams for reading and writing files.
    InputStream getInputStream(Context c, Uri uri) {
        try {
            return c.getContentResolver().openInputStream(uri);
        } catch (Exception e) {
            return null;
        }
    }
    OutputStream getOutputStream(Context c, Uri uri) {
        if (uriExists(c, uri) || !lollipopOrLater()) {
            try {
                return c.getContentResolver().openOutputStream(uri);
            } catch (Exception e) {
                Log.d("StorageAccess", "error getting outputstream");
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    void lollipopCreateFileForOutputStream(Context c, Preferences preferences, Uri uri, String mimeType, String folder, String subfolder, String filename) {
        if (lollipopOrLater() && !uriExists(c, uri)) {
            // Only need to do this for Lollipop or later
            createFile(c, preferences, mimeType, folder, subfolder, filename);
        }
    }

    // Used to decide on the best storage method (using tree or not)
    //TODO
    boolean lollipopOrLater() {
        boolean testingKitKat = false;
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !testingKitKat;
        //return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    // This builds an index of all the songs on the device
    @SuppressLint("NewApi")
    void listSongs(Context c, Preferences preferences) {
        try {
            // Decide if we are using storage access framework or not
            if (lollipopOrLater()) {
                listSongs_SAF(c, preferences);
            } else {
                listSongs_File(c, preferences);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void listSongs_SAF(Context c, Preferences preferences) {
        // This gets all songs (including any subfolders)
        FullscreenActivity.songIds = new ArrayList<>();
        FullscreenActivity.songIds.clear();
        FullscreenActivity.folderIds = new ArrayList<>();
        FullscreenActivity.folderIds.clear();

        if (FullscreenActivity.appHome==null) {
            FullscreenActivity.appHome = getAppFolderDocumentFile(c, preferences);
        }
        DocumentFile df_songs = FullscreenActivity.appHome.findFile("Songs");
        Uri uri = df_songs.getUri();

        // Now get a documents contract at this location
        String songFolderId = getDocumentsContractId(uri);

        // Get the child folders
        Uri children = getChildren(uri, songFolderId);
        ContentResolver contentResolver = c.getContentResolver();

        // Keep track of our directory hierarchy
        List<Uri> dirNodes = new LinkedList<>();
        dirNodes.add(children);

        while (!dirNodes.isEmpty()) {
            children = dirNodes.remove(0); // get the item from top
            Cursor cursor = contentResolver.query(children, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);
            try {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        final String docId = cursor.getString(0);
                        final String mime = cursor.getString(2);
                        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime) && docId.contains("OpenSong")) {
                            final Uri newNode = getChildren(children, docId);
                            dirNodes.add(newNode);
                            if (docId.contains("OpenSong/Songs/")) {
                                FullscreenActivity.folderIds.add(docId);
                                FullscreenActivity.songIds.add(docId+"/"); // In case the folder is empty add it as a songId
                            }
                        } else if (docId.contains("OpenSong/Songs")) {
                            FullscreenActivity.songIds.add(docId);
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        getSongFolderNames(c, preferences);

        // Remove everything before the actual songs folder
        ArrayList<String> tempfolders = new ArrayList<>();
        for (String s:FullscreenActivity.folderIds) {
            if (s.contains("OpenSong/Songs/")) {
                s=s.substring(s.indexOf("OpenSong/Songs/")+15);
            }
            if (!tempfolders.contains(s)) {
                // Add this new folder/subfolder
                tempfolders.add(s);
            }
        }

        FullscreenActivity.folderIds.clear();
        FullscreenActivity.folderIds = tempfolders;
    }

    private void listSongs_File(Context c, Preferences preferences) {
        // We must be using an older version of Android, so stick with File access
        FullscreenActivity.songIds = new ArrayList<>();  // These will be the file locations
        FullscreenActivity.folderIds = new ArrayList<>();

        String songsFolder = stringForFile("Songs");
        ArrayList<String> foldersToIndex = new ArrayList<>();

        // Add the main songs folder to the foldersToIndex.  More will be added for subfolders
        foldersToIndex.add(songsFolder);
        int num = foldersToIndex.size();
        // Go through all the folders
        for (int i = 0; i < num; i++) {
            File folder = new File(foldersToIndex.get(i));
            File[] contents = folder.listFiles();
            // Go through each item and add songs or folders
            for (File item : contents) {
                if (item.isDirectory()) {
                    foldersToIndex.add(item.getPath());
                    FullscreenActivity.folderIds.add(item.getPath());
                    num = foldersToIndex.size();
                } else if (item.isFile()) {
                    FullscreenActivity.songIds.add(item.getPath());

                }
            }
        }
        getSongFolderNames(c, preferences);
    }

    private void getSongFolderNames(Context c, Preferences preferences) {
        // Use the folderIds to get the song folders found
        ArrayList<String> folders = new ArrayList<>();
        folders.clear();
        FullscreenActivity.mSongFolderNames = null;
        String bittoremove = getUriForItem(c, preferences, "Songs", FullscreenActivity.whichSongFolder, "").getPath() + "/";
        for (String s:FullscreenActivity.folderIds) {
            folders.add(s.replace(bittoremove, ""));
        }

        // Remove any duplicates
        Set<String> hs = new HashSet<>(folders);
        folders.clear();
        folders.addAll(hs);

        // Sort the list
        Collator collator = Collator.getInstance(FullscreenActivity.locale);
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(folders, collator);

        // Add the main folder to the top
        folders.add(0,c.getString(R.string.mainfoldername));
        FullscreenActivity.mSongFolderNames = folders.toArray(new String[0]).clone();
    }

    @SuppressLint("NewApi")
    Uri getUriFromId(Uri uri, String id) {
        if (lollipopOrLater()) {
            return getUriFromId_SAF(uri, id);
        } else {
            return getUriFromId_File(id);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Uri getUriFromId_SAF(Uri uri, String id) {
        return DocumentsContract.buildDocumentUriUsingTree(uri,id);
    }
    private Uri getUriFromId_File(String id) {
        File f = new File (id);
        return Uri.fromFile(f);
    }

    // Here are all the file accesses used in the app!!!
    Uri getUriForItem(Context c, Preferences preferences, String folder, String subfolder, String filename) {
        String[] fixedfolders = fixFoldersAndFiles(c, folder, subfolder, filename);
        if (lollipopOrLater()) {
            return getUriForItem_SAF(c, preferences, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        } else {
            return getUriForItem_File(c, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        }
    }
    @SuppressLint("NewApi")
    private Uri getUriForItem_SAF(Context c, Preferences preferences, String folder, String subfolder, String filename) {
        return getUriFromPath(c, preferences, folder, subfolder, filename);
    }
    private Uri getUriForItem_File(Context c, String folder, String subfolder, String filename) {
        return getUriFromFilePath(c, folder, subfolder, filename);
    }

    private String[] fixFoldersAndFiles(Context c, String folder, String subfolder, String filename) {
        // This fixes incorrect folders that would cause problems
        String[] returnvals = new String[3];
        if (subfolder.startsWith("**")) {
            // This is used when custom slides are created as part of a set, making the folder look more obvious
            subfolder = subfolder.replace("**","../");
            subfolder = subfolder.replace(c.getResources().getString(R.string.image), "Images/_cache");
            subfolder = subfolder.replace(c.getResources().getString(R.string.slide),"Slides/_cache");
            subfolder = subfolder.replace(c.getResources().getString(R.string.scripture),"Scripture/_cache");
            subfolder = subfolder.replace(c.getResources().getString(R.string.variation), "Variations");
            subfolder = subfolder.replace(c.getResources().getString(R.string.note),"Notes/_cache");
        }
        switch (subfolder) {
            case "../Images/_cache":
                folder = "Images";
                subfolder = "_cache";
                break;
            case "../Scripture/_cache":
                folder = "Scripture";
                subfolder = "_cache";
                break;
            case "../Slides/_cache":
                folder = "Slides";
                subfolder = "_cache";
                break;
            case "../Variations":
                folder = "Variations";
                subfolder = "";
                break;
            case "../Notes/_cache":
                folder = "Notes";
                subfolder = "_cache";
                break;
        }
        if (folder==null || folder.equals(c.getResources().getString(R.string.mainfoldername))) {
            folder = "";
        }

        if (subfolder.equals(c.getResources().getString(R.string.mainfoldername))) {
            subfolder = "";
        }

        if (filename!=null && filename.contains("/")) {
            // Filename is everything after the last one
            int indexforfilename = filename.lastIndexOf("/");
            subfolder = subfolder + "/" + filename.substring(0, indexforfilename);
            if (subfolder.endsWith("/")) {
                subfolder = subfolder.substring(0, subfolder.length() - 1);
            }
            filename = filename.substring(indexforfilename);
            if (filename.contains("/")) {
                filename = filename.replace("/", "");
            }
        }

        returnvals[0] = folder;
        returnvals[1] = subfolder;
        returnvals[2] = filename;
        return returnvals;
    }

    boolean createFile(Context c, Preferences preferences, String mimeType, String folder, String subfolder, String filename) {
        String[] fixedfolders = fixFoldersAndFiles(c, folder, subfolder, filename);
        if (lollipopOrLater()) {
            return createFile_SAF(c, preferences, mimeType, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        } else {
            return createFile_File(fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        }
    }

    private boolean createFile_SAF(Context c, Preferences preferences, String mimeType, String folder, String subfolder, String filename) {
        if (FullscreenActivity.appHome==null) {
            FullscreenActivity.appHome = getAppFolderDocumentFile(c, preferences);
        }
        DocumentFile df = FullscreenActivity.appHome;
        DocumentFile df_parent;

        boolean createdStuff = false;
        if (df != null && df.findFile(folder) != null && !folder.equals("")) {
            df_parent = df.findFile(folder);
        } else if (df != null && !folder.equals("")) {
            // Create the folder if it isn't there
            df_parent = df.createDirectory(folder);
        } else {
            df_parent = df;
        }

        if (df!=null) {
            // Split the subfolder up if required
            if (subfolder != null && !subfolder.equals("")) {
                String[] subfolders = subfolder.split("/");
                for (String sf : subfolders) {
                    if (sf != null && !sf.isEmpty()) {
                        if (df_parent != null && df_parent.findFile(sf) == null) {
                            // Create this folder
                            df_parent.createDirectory(sf);
                            createdStuff = true;
                        } else if (df_parent != null && df_parent.findFile(sf) != null) {
                            df_parent = df_parent.findFile(sf);
                        }
                    }
                }
            }

            if (df_parent != null && filename!=null && !filename.equals("") &&df_parent.findFile(filename) == null) {
                df_parent.createFile(mimeType, filename);
                createdStuff = true;
            }

        }
        return createdStuff;
    }
    private boolean createFile_File(String folder, String subfolder, String filename) {
        boolean stuffCreated = false;
        String filepath = stringForFile(folder);
        File f = new File(filepath);
        if (subfolder != null && !subfolder.isEmpty()) {
            f = new File(f, subfolder);
        }

        if (!f.exists()) {
            try {
                stuffCreated = f.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (filename != null && !filename.isEmpty()) {
            f = new File(f, filename);
            try {
                stuffCreated = f.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
                stuffCreated = false;
            }
        }
        return stuffCreated;
    }

    String getUTFEncoding (Context c, Uri uri) {
        // Try to determine the BOM for UTF encoding
        String utf = "UTF-8";
        InputStream is = null;
        UnicodeBOMInputStream ubis = null;
        try {
            is = getInputStream(c, uri);
            ubis = new UnicodeBOMInputStream(is);
            utf = ubis.getBOM().toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (is != null) {
                is.close();
            }
            if (ubis != null) {
                ubis.close();
            }
        } catch (Exception e) {
            // Error closing
        }
        return utf;
    }

    float getFileSizeFromUri(Context c, Uri uri) {
        if (lollipopOrLater()) {
            return getFileSizeFromUri_SAF(c, uri);
        } else {
            return getFileSizeFromUri_File(uri);
        }
    }

    private float getFileSizeFromUri_SAF(Context c, Uri uri) {
        DocumentFile df = documentFileFromUri(c,uri,uri.getPath());
        if (df!=null && df.exists()) {
            return (float) df.length() / (float) 1024;
        } else {
            return 0;
        }
    }

    private float getFileSizeFromUri_File(Uri uri) {
        File df = null;
        if (uri!=null && uri.getPath()!=null) {
            df = new File(uri.getPath());
        }
        if (df!=null && df.exists()) {
            return (float) df.length() / (float) 1024;
        } else {
            return 0;
        }
    }

    boolean uriExists(Context c, Uri uri) {
        if (lollipopOrLater()) {
            return uriExists_SAF(c, uri);
        } else {
            return uriExists_File(uri);
        }
    }
    private boolean uriExists_SAF(Context c, Uri uri) {
        try {
            c.getContentResolver().openInputStream(uri);
            return true;
        } catch (Exception e){
            return false;
        }
    }
    private boolean uriExists_File(Uri uri) {
        File df = null;
        if (uri!=null && uri.getPath()!=null) {
            df = new File(uri.getPath());
        }
        if (df!=null) {
            return df.exists();
        } else {
            return false;
        }
    }

    boolean uriIsFile(Context c, Uri uri) {
        if (lollipopOrLater()) {
            return uriIsFile_SAF(c,uri);
        } else {
            return uriIsFile_File(uri);
        }
    }
    private boolean uriIsFile_SAF(Context c, Uri uri) {
        DocumentFile df = DocumentFile.fromSingleUri(c,uri);
        return df.isFile();
    }
    private boolean uriIsFile_File(Uri uri) {
        if (uri!=null && uri.getPath()!=null) {
            File f = new File(uri.getPath());
            return f.isFile();
        } else {
            return false;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    Uri createDocument(Context c, Uri uri, String name) {
        try {
            return DocumentsContract.createDocument(c.getContentResolver(), uri, null, name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    Uri fixLocalisedUri(Context c, Preferences preferences, String uriString) {
        // This checks for localised filenames first and fixes the Uri
        if (uriString.startsWith("../OpenSong/Media/")) {
            // Remove this and get the proper location
            uriString = uriString.replace("../OpenSong/Media/","");
            // Now get the actual uri
            return getUriForItem(c, preferences, "Media", "", uriString);
        } else {
            return Uri.parse(uriString);
        }
    }

    @SuppressLint("NewApi")
    ArrayList<String> listFilesInFolder(Context c, Preferences preferences, String folder, String subfolder) {
        String[] fixedfolders = fixFoldersAndFiles(c,folder,subfolder,"");
        if (lollipopOrLater()) {
            return listFilesInFolder_SAF(c, preferences, fixedfolders[0], fixedfolders[1]);
        } else {
            return listFilesInFolder_File(fixedfolders[0], fixedfolders[1]);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<String> listFilesInFolder_SAF(Context c, Preferences preferences, String folder, String subfolder) {
        Log.d("StorageAccess","Started listing files in folder");
        ArrayList<String> al = new ArrayList<>();

        Uri locationtoindex = getUriForItem(c, preferences, folder, subfolder, "");

        //Log.d("StorageAccess", "locationtoindex=" + locationtoindex);

        // Now get a documents contract at this location
        String id = getDocumentsContractId(locationtoindex);

        //Log.d("StorageAccess", "id=" + id);

        // Get the children
        Uri children = getChildren(locationtoindex, id);
        ContentResolver contentResolver = c.getContentResolver();

        // Keep track of our directory hierarchy
        List<Uri> dirNodes = new LinkedList<>();
        dirNodes.add(children);

        while (!dirNodes.isEmpty()) {
            children = dirNodes.remove(0); // get the item from top
            Cursor cursor = contentResolver.query(children, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);
            try {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        final String name = cursor.getString(1);
                        final String mime = cursor.getString(2);
                        if (!DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                            al.add(name);
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d("StorageAccess","Ended listing files in folder");
        return al;
    }
    private ArrayList<String> listFilesInFolder_File(String folder, String subfolder) {
        ArrayList<String> al = new ArrayList<>();
        String filebuilder = stringForFile(folder);
        File f = new File(filebuilder);
        if (subfolder!=null && !subfolder.isEmpty()) {
            f = new File (f,subfolder);
        }
        File[] fs = f.listFiles();
        if (fs!=null && fs.length>0) {
            for (File fi : fs) {
                if (fi.isFile()) {
                    al.add(fi.getName());
                }
            }
        }
        return al;
    }

    void extractZipFile(Context c, Preferences preferences, Uri zipUri, String folder, String subfolder, ArrayList<String> zipfolders) {
        // This bit could be slow, so it will likely be called in an async task
        ZipInputStream zis = null;
        try {
            InputStream inputStream = getInputStream(c, zipUri);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            zis = new ZipInputStream(bufferedInputStream);
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                // Look to see if ze is in one of the folders we are wanting to import
                boolean oktoimportthisone = false;
                if (zipfolders != null && zipfolders.contains(ze.getName()) ||
                        (!ze.getName().contains("/") && zipfolders!=null &&
                                zipfolders.contains(c.getString(R.string.mainfoldername)))) {
                    oktoimportthisone = true;
                } else if (zipfolders == null) {
                    // Just import everthing
                    oktoimportthisone = true;
                }

                if (oktoimportthisone) {
                    createFile(c, preferences, null, folder, subfolder, ze.getName());
                    Uri newUri = getUriForItem(c, preferences, folder, subfolder, ze.getName());
                    OutputStream outputStream = getOutputStream(c, newUri);

                    try {
                        while ((count = zis.read(buffer)) != -1)
                            outputStream.write(buffer, 0, count);
                    } finally {
                        try {
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zis != null) {
                try {
                    zis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    String extractOnSongZipFile(Context c, Preferences preferences, Uri zipUri) {

        // This bit will take a while, so will be called in an async task
        String message = c.getResources().getString(R.string.success);

        // Remove any existing sqlite3 files
        Uri dbfile = getUriForItem(c, preferences, "", "", "OnSong.Backup.sqlite3");

        if (uriExists(c,dbfile)) {
            deleteFile(c,dbfile);
        }

        createFile(c, preferences, null, "Songs", "OnSong", "");

        InputStream is;
        ZipArchiveInputStream zis;
        String filename;

        try {
            is = getInputStream(c,zipUri);
            zis = new ZipArchiveInputStream(new BufferedInputStream(is),"UTF-8",false);

            ZipArchiveEntry ze;
            while ((ze = (ZipArchiveEntry) zis.getNextEntry()) != null) {
                final byte[] buffer = new byte[2048];
                int count;
                filename = ze.getName();
                if (!filename.startsWith("Media")) {
                    // The Media folder throws errors (it has zero length files sometimes
                    // It also contains stuff that is irrelevant for OpenSongApp importing
                    // Only process stuff that isn't in that folder!
                    // It will also ignore any song starting with 'Media' - not worth a check for now!

                    OutputStream out;
                    if (filename.equals("OnSong.Backup.sqlite3") || filename.equals("OnSong.sqlite3")) {
                        Uri outuri = getUriForItem(c, preferences, "", "", "OnSong.Backup.sqlite3");
                        out = getOutputStream(c,outuri);
                    } else {
                        Uri outuri = getUriForItem(c, preferences, "Songs", "OnSong", filename);
                        out = getOutputStream(c,outuri);
                    }

                    final BufferedOutputStream bout = new BufferedOutputStream(out);

                    try {
                        while ((count = zis.read(buffer)) != -1) {
                            bout.write(buffer, 0, count);
                        }
                        bout.flush();
                    } catch (Exception e) {
                        message = c.getResources().getString(R.string.file_type_unknown);
                        e.printStackTrace();
                    } finally {
                        try {
                            bout.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            zis.close();

        } catch (Exception e) {
            e.printStackTrace();
            message = c.getResources().getString(R.string.import_onsong_error);
            return message;
        }

        if (uriExists(c,dbfile) && dbfile!=null && dbfile.getPath()!=null) {
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(new File(dbfile.getPath()), null);
            // Go through each row and read in the content field
            // Save the files with the .onsong extension

            String query = "SELECT * FROM Song";

            //Cursor points to a location in your results
            Cursor cursor;
            message = c.getResources().getString(R.string.import_onsong_done);
            String str_title;
            String str_content;

            try {
                cursor = db.rawQuery(query, null);

                // Move to first row
                cursor.moveToFirst();

                while (cursor.moveToNext()) {
                    // Extract data.
                    str_title = cursor.getString(cursor.getColumnIndex("title"));
                    // Make sure title doesn't have /
                    str_title = str_title.replace("/", "_");
                    str_title = TextUtils.htmlEncode(str_title);
                    str_content = cursor.getString(cursor.getColumnIndex("content"));

                    try {
                        // Now write the modified song
                        Uri newsong = getUriForItem(c, preferences, "Songs", "OnSong", str_title + ".onsong");
                        OutputStream overWrite = getOutputStream(c,newsong);
                        writeFileFromString(str_content,overWrite);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                cursor.close();



            } catch (Exception e) {
                // Error with sql database
                e.printStackTrace();
                message = c.getResources().getString(R.string.import_onsong_error);
            }
        }

        return message;
    }

    boolean deleteFile(Context c, Uri uri) {
        if (lollipopOrLater()) {
            return deleteFile_SAF(c, uri);
        } else {
            return deleteFile_File(uri);
        }
    }
    private boolean deleteFile_SAF(Context c, Uri uri) {
        try {
            DocumentFile df = DocumentFile.fromSingleUri(c, uri);
            return df.delete();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private boolean deleteFile_File(Uri uri) {
        try {
            if (uri != null && uri.getPath() != null) {
                File f = new File(uri.getPath());
                // If this is a directory, empty it first
                if (f.isDirectory()) {
                    for (File child : f.listFiles()) {
                        Log.d("StorageAccess", "Deleting " + child + " = " + child.delete());
                    }
                }
                return f.delete();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    boolean renameFile(Context c, Preferences preferences, String folder, String oldsubfolder, String newsubfolder, String oldname, String newname) {
        Uri olduri = getUriForItem(c, preferences, folder, oldsubfolder, oldname);
        InputStream inputStream = getInputStream(c, olduri);
        Uri newuri = getUriForItem(c, preferences, folder, newsubfolder, newname);
        if (!uriExists(c, newuri)) {
            // Create a new blank file ready
            createFile(c, preferences, null, folder, newsubfolder, newname);
        }
        OutputStream outputStream = getOutputStream(c, newuri);
        try {
            // Copy the file
            copyFile(inputStream, outputStream);
            // All is good, so delete the old one
            DocumentFile.fromSingleUri(c, olduri).delete();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    boolean fileIsEmpty(Context c, Uri uri) {
        if (lollipopOrLater()) {
            return fileIsEmpty_SAF(c,uri);
        } else {
            return fileIsEmpty_File(uri);
        }
    }
    private boolean fileIsEmpty_SAF(Context c, Uri uri) {
        DocumentFile df = DocumentFile.fromSingleUri(c, uri);
        return df != null && df.exists() && df.length() > 0;
    }
    private boolean fileIsEmpty_File(Uri uri) {
        if (uri!=null && uri.getPath()!=null) {
            File f = new File(uri.getPath());
            return f.exists() && f.length()>0;
        } else {
            return false;
        }
    }

    boolean canWrite(Context c, Uri uri) {
        if (lollipopOrLater()) {
            return canWrite_SAF(c, uri);
        } else {
            return canWrite_File(uri);
        }
    }
    private boolean canWrite_SAF(Context c, Uri uri) {
        DocumentFile df = DocumentFile.fromSingleUri(c,uri);
        return df.canWrite();
    }
    private boolean canWrite_File(Uri uri) {
        if (uri!=null && uri.getPath()!=null) {
            File f = new File(uri.getPath());
            return f.canWrite();
        } else {
            return false;
        }
    }

    void wipeFolder(Context c, Preferences preferences, String folder, String subfolder) {
        Uri uri = getUriForItem(c, preferences, folder, subfolder, "");
        // Delete the contents of this folder
        deleteFile(c,uri);
        // Recreate it (now empty)
        createFile(c, preferences, DocumentsContract.Document.MIME_TYPE_DIR, folder, subfolder, "");
    }

    boolean isXML(Uri uri) {
        boolean isxml = true;
        if (uri != null && uri.getLastPathSegment()!=null) {
            String name = uri.getLastPathSegment().toLowerCase(FullscreenActivity.locale);
            if (name.endsWith(".pdf") || name.endsWith(".doc") ||
                    name.endsWith(".jpg") || name.endsWith(".png") ||
                    name.endsWith(".bmp") || name.endsWith(".gif") ||
                    name.endsWith(".jpeg") || name.endsWith(".apk") ||
                    name.endsWith(".txt") || name.endsWith(".zip")) {
                isxml = false;
            }
        } else {
            isxml = false;
        }
        return isxml;
    }

    boolean containsXMLTags(Context c, Uri uri) {
        try {
            boolean found = false;
            String utf = getUTFEncoding(c, uri);
            InputStream inputStream = getInputStream(c, uri);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(inputStream, utf);
            int eventType;
            eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT && !found) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("lyrics")) {
                        found = true; // It's a song
                        FullscreenActivity.myToastMessage = "foundsong";
                    } else if (xpp.getName().equals("set")) {
                        found = true; // It's a set
                        FullscreenActivity.myToastMessage = "foundset";
                    }
                }
                // If it isn't an xml file, an error is about to be thrown
                try {
                    eventType = xpp.next();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return found;
        } catch (Exception e) {
            return false;
        }
    }

    boolean isTextFile(Uri uri) {
        boolean istext = false;
        if (uri!=null && uri.getLastPathSegment()!=null) {
            String name = uri.getLastPathSegment().toLowerCase(FullscreenActivity.locale);
            if ((!name.contains(".pdf") && !name.contains(".PDF") &&
                    !name.contains(".doc") && !name.contains(".DOC") &&
                    !name.contains(".docx") && !name.contains(".DOCX") &&
                    !name.contains(".png") && !name.contains(".PNG") &&
                    !name.contains(".jpg") && !name.contains(".JPG") &&
                    !name.contains(".gif") && !name.contains(".GIF") &&
                    !name.contains(".jpeg") && !name.contains(".JPEG")) ||
                    name.endsWith(".txt")) {
                istext = true;
            }
        }
        return istext;
    }

    String getPartOfUri(Uri uri, String from) {
        // This gets the filename
        String path = uri.getPath();
        if (path!=null && path.contains(from)) {
            path = path.substring(path.lastIndexOf(from));
        }
        return path;
    }

    boolean checkFileExtensionValid(Uri uri) {
        // This lets us know if the file is appropriate to read the title/author/key from during indexing
        String filename;
        if (uri!=null && uri.getLastPathSegment()!=null) {
            filename = uri.getLastPathSegment().toLowerCase();
        } else {
            filename = "";
        }
        boolean isvalid = true;
        String type = null;
        if (filename.lastIndexOf(".")>1 && filename.lastIndexOf(".")<filename.length()-1) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            int index = filename.lastIndexOf('.')+1;
            String ext = filename.substring(index).toLowerCase();
            type = mime.getMimeTypeFromExtension(ext);
        }

        if (type!=null && !type.equals("")) {
            if (type.contains("image") || type.contains("application") || type.contains("pdf") || type.contains("video") || type.contains("audio")) {
                isvalid = false;
            }
        }

        if (filename.endsWith(".pdf") || filename.endsWith(".jpg") ||
                filename.endsWith(".png") || filename.endsWith(".gif") ||
                filename.endsWith(".doc") || filename.endsWith(".docx") ||
                filename.endsWith(".zip") || filename.endsWith(".apk") ||
                filename.endsWith(".tar")  || filename.endsWith(".backup")) {
            isvalid = false;
        }
        return isvalid;
    }
    boolean determineFileTypeByExtension() {
        // Determines if we can load song as text, image or pdf
        String file_ext = FullscreenActivity.songfilename;
        FullscreenActivity.isImage = false;
        FullscreenActivity.isPDF = false;

        if (file_ext!=null) {
            file_ext = file_ext.toLowerCase();
        } else {
            file_ext = "";
        }
        if (file_ext.endsWith(".pdf")) {
            FullscreenActivity.isPDF = true;
        } else if (file_ext.endsWith(".jpg") || file_ext.endsWith(".bmp") ||
                file_ext.endsWith(".png") || file_ext.endsWith(".gif")) {
            FullscreenActivity.isImage = true;
        }

        return FullscreenActivity.isPDF || FullscreenActivity.isImage;
    }

    String getImageSlide(Context c, String loc) {
        String b = "";
        byte[] bytes;
        Uri uri = Uri.parse(loc);
        if (uriExists(c, uri)) {
            try {
                InputStream inputStream = getInputStream(c, uri);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                b = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("StorageAccess", "error getting image bytes");
            }
        }
        return b;
    }

}
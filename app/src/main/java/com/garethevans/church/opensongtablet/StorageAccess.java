package com.garethevans.church.opensongtablet;

// This class deals with accessing the app storage
// There are two sets of methods
// Newer versions of Android (Lollipop and later) should use the will use content uris
// These are based on the ACTION_OPEN_DOCUMENT_TREE location
// KitKat and below will use file uris based on built in folder chooser


import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

class StorageAccess {

    String appFolder = "OpenSong";
    private String[] rootFolders = {"Backgrounds", "Export", "Fonts", "Highlighter", "Images", "Media",
            "Notes", "OpenSong Scripture", "Pads", "Profiles", "Received", "Scripture",
            "Sets", "Settings", "Slides", "Songs", "Variations"};
    private String[] cacheFolders = {"Backgrounds/_cache", "Images/_cache", "Notes/_cache",
            "OpenSong Scripture/_cache", "Scripture/_cache", "Slides/_cache"};


    private boolean testingKitKat = true;

    // These are used primarily on start up to initialise stuff
    String getStoragePreference(Context c) {
        Preferences preferences = new Preferences();
        return preferences.getMyPreferenceString(c, "uriTree", null);
    }

    Uri homeFolder(Context c) {
        // The user specified a storage folder when they started the app
        // However, this might not be the OpenSong folder, but the folder containing it
        // This function is called once when the app starts
        String uriTree = getStoragePreference(c);
        Uri uri;
        if (uriTree != null) {
            if (lollipopOrLater()) {
                uri = Uri.parse(uriTree);
                if (uri != null) {
                    DocumentFile df = documentFileFromUri(c, uri, uriTree);
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

        FullscreenActivity.uriTree = uri;  // This is the chosen route
        return uri;
    }

    private DocumentFile getAppFolderDocumentFile(Context c) {
        // This simply gets a documentfile location for the OpenSongApp folder
        // It is then saved to FullscreenActivity.
        // FullscreenActivity.uriTree is already valid and set

        String uriTree = getStoragePreference(c);
        //Make sure FullscreenActivity.uriTree is set
        homeFolder(c);
        FullscreenActivity.appHome = null;
        Log.d("d", "About to create the documentfile");
        Log.d("d", "FullscreenActivity.uriTree=" + FullscreenActivity.uriTree);
        Log.d("d", "uriTree=" + uriTree);
        DocumentFile df = documentFileFromUri(c, FullscreenActivity.uriTree, uriTree);
        Log.d("d", "df=" + df);

        if (df != null) {
            // Check if we are in the app folder
            if (df.getName() != null && df.getName().equals(appFolder)) {
                // Already in the app folder
                Log.d("d", "Already in the app folder");
                FullscreenActivity.appHome = df;
            } else if (df.findFile(appFolder) != null) {
                // Need to move into the app folder
                Log.d("d", "Above the app folder, so move inside");
                FullscreenActivity.appHome = df.findFile(appFolder);
            } else {
                // No app folder, so create it
                Log.d("d", "Can't find app folder so creating it");
                df.createDirectory(appFolder);
                FullscreenActivity.appHome = df.findFile(appFolder);
            }
        }
        return FullscreenActivity.appHome;
    }

    String createOrCheckRootFolders(Context c) {
        try {
            // The OpenSong folder
            DocumentFile root_df = getAppFolderDocumentFile(c);

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

    String stringForFile(String folderpath) {
        String file = FullscreenActivity.customStorage;
        if (!file.endsWith(appFolder)) {
            file = file + "/" + appFolder;
        }
        return file + "/" + folderpath;
    }

    // These are used to return Uris, Ids, DocumentFiles, etc. for files
    Uri getUriFromFilePath(Context c, String folder, String subfolder, String filename) {
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

    Uri getUriFromPath(Context c, String folder, String subfolder, String filename) {
        // Use the appHome uri
        Uri uri = null;
        if (FullscreenActivity.appHome != null) {
            uri = FullscreenActivity.appHome.getUri();

            // Now point to the specific folder (Songs, Sets, Backgrounds, etc.)
            if (folder != null) {
                uri = Uri.withAppendedPath(uri, Uri.encode(folder));
            }

            // Now go through the subfolder(s)
            if (subfolder != null && !subfolder.equals(c.getString(R.string.mainfoldername))) {
                String[] sfs = subfolder.split("/");
                for (String sf : sfs) {
                    if (sf != null && !sf.equals("")) {
                        uri = Uri.withAppendedPath(uri, Uri.encode(sf));
                    }
                }
            }

            // Now add the filename
            if (filename != null && !filename.equals("")) {
                // Might have sent subfolder info
                String[] sfs = filename.split("/");
                for (String sf : sfs) {
                    if (sf != null && !sf.equals("") && !subfolder.equals(c.getString(R.string.mainfoldername))) {
                        uri = Uri.withAppendedPath(uri, Uri.encode(sf));
                    }
                }
            }
        }
        // Now return the Uri
        return uri;
    }

    DocumentFile documentFileFromUri(Context c, Uri uri, String path) {
        if (uri != null && lollipopOrLater()) {
            return DocumentFile.fromTreeUri(c, uri);
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
            Log.d("d", "Setting for Lollipop");
            return DocumentsContract.getTreeDocumentId(uri);
        } else {
            return null;
        }
    }

    Uri documentUriFromId(String id) {
        if (lollipopOrLater()) {
            return DocumentsContract.buildDocumentUriUsingTree(FullscreenActivity.uriTree, id);
        } else {
            return DocumentsContract.buildDocumentUri(FullscreenActivity.uriTree.getAuthority(), id);
        }
    }

    Uri getUriFromId(Uri uri, String id) {
        if (lollipopOrLater()) {
            return DocumentsContract.buildDocumentUriUsingTree(uri, id);
        } else {
            return DocumentsContract.buildDocumentUri(uri.getAuthority(), id);
        }
    }

    private Uri getChildren(Uri uri, String id) {
        Log.d("d", "uri=" + uri);
        Log.d("d", "id=" + id);
        if (lollipopOrLater()) {
            return DocumentsContract.buildChildDocumentsUriUsingTree(uri, id);
        } else {

            return DocumentsContract.buildChildDocumentsUri(uri.getAuthority(), id);
        }
    }


    // Basic file actions
    private void copyFile(InputStream in, OutputStream out) {
        try {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
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

    String readTextFileToString(InputStream in) {
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
            Log.d("d", "Error reading text file");
        } catch (OutOfMemoryError e2) {
            e2.printStackTrace();
        }
        return outputStream.toString();
    }


    // Input and output streams for reading and writing files.
    InputStream getInputStream(Context c, Uri uri) {
        try {
            return c.getContentResolver().openInputStream(uri);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    OutputStream getOutputStream(Context c, Uri uri) {
        Log.d("d", "uri=" + uri);
        try {
            return c.getContentResolver().openOutputStream(uri);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // Used to decide on the best storage method (using tree or not)
    boolean lollipopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !testingKitKat;
    }


    // This builds an index of all the songs on the device
    void listSongs(Context c) {
        // Decide if we are using storage access framework or not
        if (lollipopOrLater()) {
            listSongs_SAF(c);
        } else {
            listSongs_File();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void listSongs_SAF(Context c) {
        FullscreenActivity.songIds = new ArrayList<>();

        // The base/tree Uri is the static field in FullscreenActivity
        // Now extract the song folders from the song ids
        FullscreenActivity.folderIds = new ArrayList<>();
        for (String f : FullscreenActivity.songIds) {
            // Remove the last path
            int endoffolder = f.lastIndexOf("/");
            if (endoffolder > 0) {
                f = f.substring(0, endoffolder - 1);
            }
            FullscreenActivity.folderIds.add(f);
        }
        DocumentFile df_songs = FullscreenActivity.appHome.findFile("Songs");
        Uri uri;

        uri = df_songs.getUri();

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
                        if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mime)) {
                            final Uri newNode = getChildren(children, docId);
                            dirNodes.add(newNode);
                        } else {
                            FullscreenActivity.songIds.add(docId);
                            Log.d("d", "songId=" + docId);
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
        for (String f : FullscreenActivity.songIds) {
            // Remove the last path
            int endoffolder = f.lastIndexOf("/");
            if (endoffolder > 0) {
                f = f.substring(0, endoffolder - 1);
            }
            FullscreenActivity.folderIds.add(f);
        }
    }

    private void listSongs_File() {
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
                    foldersToIndex.add(item.getAbsolutePath());
                    FullscreenActivity.folderIds.add(item.getAbsolutePath());
                    num = foldersToIndex.size();
                    Log.d("d", "Folder found: " + item);
                } else if (item.isFile()) {
                    FullscreenActivity.songIds.add(item.getAbsolutePath());
                    Log.d("d", "Song found: " + item);
                }
            }
        }
    }


    // Here are all the file accesses used in the app!!!
    Uri getUriForItem(Context c, String folder, String subfolder, String filename) {
        String[] fixedfolders = fixFoldersAndFiles(c, folder, subfolder, filename);
        if (lollipopOrLater()) {
            return getUriForItem_SAF(c, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        } else {
            return getUriForItem_File(c, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        }
    }

    private String[] fixFoldersAndFiles(Context c, String folder, String subfolder, String filename) {
        // This fixes incorrect folders that would cause problems
        String[] returnvals = new String[3];
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
        }
        if (folder.equals(c.getString(R.string.mainfoldername))) {
            folder = "";
        }

        if (filename.contains("/")) {
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

    private Uri getUriForItem_SAF(Context c, String folder, String subfolder, String filename) {
        return getUriFromPath(c, folder, subfolder, filename);
    }

    private Uri getUriForItem_File(Context c, String folder, String subfolder, String filename) {
        return getUriFromFilePath(c, folder, subfolder, filename);
    }

    boolean createFile(Context c, String mimeType, String folder, String subfolder, String filename) {
        String[] fixedfolders = fixFoldersAndFiles(c, folder, subfolder, filename);
        if (lollipopOrLater()) {
            return createFile_SAF(mimeType, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        } else {
            return createFile_File(fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        }
    }
    private boolean createFile_SAF(String mimeType, String folder, String subfolder, String filename) {
        DocumentFile df = FullscreenActivity.appHome;
        DocumentFile df_parent;
        boolean createdStuff = false;
        if (df != null && df.findFile(folder) != null) {
            df_parent = df.findFile(folder);

            // Split the subfolder up if required
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

            if (df_parent != null && df_parent.findFile(filename) == null) {
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
        if (filename != null && !filename.isEmpty()) {
            f = new File(f, filename);
        }
        if (!f.exists()) {
            try {
                stuffCreated = f.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (filename != null && !filename.equals("")) {
                try {
                    stuffCreated = f.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                    stuffCreated = false;
                }
            }
        }
        return stuffCreated;
    }
    String getUTFEncoding (Context c, Uri uri) {
        // Try to determine the BOM for UTF encoding
        String utf = "";
        InputStream is = null;
        UnicodeBOMInputStream ubis = null;
        try {
            is = getInputStream(c, uri);
            ubis = new UnicodeBOMInputStream(is);
            utf = ubis.getBOM().toString();

        } catch (Exception e) {
            FullscreenActivity.myXML = "<title>Love everlasting</title>\n<author></author>\n<lyrics>"
                    + c.getResources().getString(R.string.songdoesntexist) + "\n\n" + "</lyrics>";
            FullscreenActivity.myLyrics = "ERROR!";
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
    long getFileSizeFromUri(Context c, Uri uri) {
        if (lollipopOrLater()) {
            return getFileSizeFromUri_SAF(c, uri);
        } else {
            return getFileSizeFromUri_File(uri);
        }
    }
    private long getFileSizeFromUri_SAF(Context c, Uri uri) {
        DocumentFile df = documentFileFromUri(c,uri,uri.getPath());
        if (df!=null && df.exists()) {
            return df.length();
        } else {
            return 0;
        }
    }
    private long getFileSizeFromUri_File(Uri uri) {
        File f = null;
        if (uri!=null) {
            f = new File(uri.getPath());
        }
        if (uri != null && f.exists()) {
            return Long.parseLong(String.valueOf(f.length() / 1024));
        } else {
            return 0;
        }
    }
}

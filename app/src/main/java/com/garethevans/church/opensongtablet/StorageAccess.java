package com.garethevans.church.opensongtablet;

// This class deals with accessing the app storage
// There are two sets of methods
// Newer versions of Android (Lollipop and later) will use content uris
// These are based on the ACTION_OPEN_DOCUMENT_TREE location
// KitKat and below will use file uris based on built in folder chooser

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;
import android.util.Base64;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class StorageAccess {

    final String appFolder = "OpenSong";
    private Uri uriTree = null, uriTreeHome = null; // This is the home folder.  Set as required from preferences.
    private final String[] rootFolders = {"Backgrounds", "Export", "Highlighter", "Images", "Media",
            "Notes", "OpenSong Scripture", "Pads", "Profiles", "Received", "Scripture",
            "Sets", "Settings", "Slides", "Songs", "Variations"};
    private final String[] cacheFolders = {"Backgrounds/_cache", "Images/_cache", "Notes/_cache",
            "OpenSong Scripture/_cache", "Scripture/_cache", "Slides/_cache"};

    // These are used primarily on start up to initialise stuff
    private String getStoragePreference(Context c, Preferences preferences) {
        return preferences.getMyPreferenceString(c, "uriTree", null);
    }


    // This gets the uri for the uriTreeHome (the uri of the ..../OpenSong folder
    // This may or may not be the same as uriTree as this could be the parent folder
    Uri homeFolder(Context c, Uri uri, Preferences preferences) {
        // The user specified a storage folder when they started the app
        // However, this might not be the OpenSong folder, but the folder containing it
        // This function is called once when the app starts and fixes that

        String uriTree_String;
        if (uri==null) {
            // No uri has been sent, so retrieve it from the preferences
            uriTree_String = getStoragePreference(c,preferences);
        } else {
            // Use the uri sent as the base start point
            uriTree_String = uri.toString();
        }

        try {
            if (uriTree_String != null) {
                if (lollipopOrLater()) {
                    uri = homeFolder_SAF(c, uriTree_String);
                } else {
                    uri = homeFolder_File(uriTree_String);
                }
            } else {
                uri = null;
            }

        } catch (Exception e) {
            // Could be called if the uri stored is for a different version of Android. e.g. after upgrade
            uri = null;
        }
        return uri;
    }
    private Uri homeFolder_SAF(Context c, String uriTree_String) {
        // When using a document tree, the uri needed for DocumentsContract is more complex than the uri chosen.
        // Create a document file to get a contract uri

        Uri uri = Uri.parse(uriTree_String);
        if (uri != null) {
            DocumentFile df = documentFileFromRootUri(c, uri, uriTree_String);
            if (df == null || !df.exists()) {
                uri = null;
            } else {
                uri = df.getUri();
                uriTree = uri;
            }

            // If uri doesn't end with /OpenSong/, fix that
            if (uri!=null && uri.getLastPathSegment()!=null && !uri.getLastPathSegment().endsWith("OpenSong")) {
                String s = uri.toString();
                s = s + "%2FOpenSong";
                uri = Uri.parse(s);
            }
        }
        uriTreeHome = uri;
        return uri;
    }
    private Uri homeFolder_File(String uriTree_String) {
        File f;
        if (!uriTree_String.endsWith("OpenSong") && !uriTree_String.endsWith("OpenSong/")) {
            uriTree_String = uriTree_String + "/OpenSong/";
            uriTree_String = uriTree_String.replace("//OpenSong/","/OpenSong/");
            f = new File(uriTree_String);
            if (f.mkdirs()) {
                Log.d("StorageAccess","Created or identified OpenSong folder");
            }
        } else {
            f = new File(uriTree_String);
        }
        return Uri.fromFile(f);
    }

    // This gets the File location for the app as a String (for appending).  PreLollipop only
    private String stringForFile(Context c, Preferences preferences, String folderpath) {
        if (uriTreeHome==null) {
            String uriTreeHome_String = preferences.getMyPreferenceString(c, "uriTreeHome", "");
            uriTreeHome = Uri.parse(uriTreeHome_String);
        }

        if (uriTreeHome!=null) {
            String file = uriTreeHome.getPath();
            if (file!=null && !file.endsWith(appFolder)) {
                file = file + "/" + appFolder;
            }
            return file + "/" + folderpath;
        } else {
            return folderpath;
        }
    }

/*    // This gets a DocumentFile for the app folder, ready to create files and folders
    private DocumentFile getAppFolderDocumentFile(Context c, Preferences preferences) {
        // This simply gets a documentfile location for the OpenSongApp folder
        // It is then saved to FullscreenActivity.
        // FullscreenActivity.uriTree is already valid and set

        String uriTree = getStoragePreference(c, preferences);
        //Make sure FullscreenActivity.uriTree is set
        homeFolder(c, null,preferences);
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
    }*/

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    String createOrCheckRootFolders(Context c, Uri uri, Preferences preferences) {
        // uri if the uriTree.  If not sent/null, load from preferences
        if (uri==null) {
            uri = Uri.parse(preferences.getMyPreferenceString(c,"uriTree",""));
        }

        if (lollipopOrLater()) {
            return createOrCheckRootFolders_SAF(c, uri, preferences);
        } else {
            return createOrCheckRootFolders_File(c, preferences);
        }
    }
    private String createOrCheckRootFolders_File(Context c, Preferences preferences) {
        File rootFolder = new File(stringForFile(c, preferences,""));
        // Go through the main folders and try to create
        for (String folder : rootFolders) {
            File nf = new File(rootFolder, folder);
            if (!nf.exists()) {
                if (!nf.mkdirs()) {
                    Log.d("d", "Error creating folder: " + folder);
                }
            }
        }

        // Go through the sub folders and try to create
        for (String subfolder : cacheFolders) {
            File nf = new File(rootFolder, subfolder);
            if (!nf.exists()) {
                if (!nf.mkdirs()) {
                    Log.d("d", "Error creating subfolder: " + subfolder);
                }
            }
        }
        copyAssets(c, preferences);
        return "Success";
    }
    @SuppressWarnings("SameReturnValue")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private String createOrCheckRootFolders_SAF(Context c, Uri uri, Preferences preferences) {
        uriTreeHome = homeFolder(c,uri,preferences);

        // Check the OpenSong folder exists (may be specified in the uriTree and/or uriTreeHome
        if (uriTree!=null && uriTree.getLastPathSegment()!=null &&
                (!uriTree.getLastPathSegment().endsWith("OpenSong") || !uriTree.getLastPathSegment().endsWith("OpenSong/"))) {
            // We need to check the uri points to the OpenSong folder
            if (!uriExists(c,uriTreeHome)) {
                Log.d("StorageAccess","Attempting to create OpenSong folder at "+uriTree);
                try {
                    uriTreeHome = DocumentsContract.createDocument(c.getContentResolver(), uriTree, DocumentsContract.Document.MIME_TYPE_DIR, "OpenSong");
                } catch (Exception e) {
                    Log.d("StorageAccess", "Unable to create OpenSong folder at "+uriTree);
                }
            }
        }

        // Go through the main folders and try to create them
        for (String folder : rootFolders) {
            try {
                Uri thisFolder = getUriForItem(c, preferences, folder, "", "");
                if (!uriExists(c, thisFolder)) {
                    DocumentsContract.createDocument(c.getContentResolver(), uriTreeHome, DocumentsContract.Document.MIME_TYPE_DIR, folder);
                }
            } catch (Exception e) {
                Log.d("d", folder + " error creating");
            }
        }

        // Now for the cache folders
        for (String folder : cacheFolders) {
            String[] bits = folder.split("/");
            try {
                Uri dirUri = getUriForItem(c, preferences, bits[0], "", "");
                Uri thisFolder = getUriForItem(c, preferences, bits[0], bits[1], "");
                if (!uriExists(c, thisFolder)) {
                    try {
                        DocumentsContract.createDocument(c.getContentResolver(), dirUri, DocumentsContract.Document.MIME_TYPE_DIR, bits[1]);
                    } catch (Exception e3) {
                        Log.d("StorageAccess","Error creating folder at "+thisFolder);
                    }
                }
            } catch (Exception e2) {
                Log.d("d", "error creating cache: " + folder);
            }
        }

        // Now copy the assets if they aren't already there
        copyAssets(c, preferences);
            return "Success";
    }

    private void copyAssets(Context c, Preferences preferences) {
        try {
            // Copies the background assets
            AssetManager assetManager = c.getAssets();
            String[] files = new String[2];
            files[0] = "backgrounds/ost_bg.png";
            files[1] = "backgrounds/ost_logo.png";
            Uri backgrounds = getUriForItem(c,preferences,"Backgrounds","","");

            DocumentFile df = documentFileFromUri(c,backgrounds,backgrounds.getPath());
            for (String filename : files) {
                String filetocopy = filename.replace("backgrounds/", "");
                // See if they are already there first
                Uri uritocheck = getUriForItem(c,preferences,"Backgrounds","",filetocopy);
                if (!uriExists(c, uritocheck)) {
                    if (lollipopOrLater()) {
                        DocumentsContract.createDocument(c.getContentResolver(),backgrounds,"image/png",filetocopy);
                    } else {
                        df.createFile("image/png", filetocopy);
                    }
                    if (uritocheck!=null) {
                        OutputStream out = getOutputStream(c, uritocheck);
                        try {
                            InputStream in = assetManager.open(filename);
                            copyFile(in, out);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // These are used to return Uris, Ids, DocumentFiles, etc. for files


    Uri getFileProviderUri(Context c, Preferences preferences, String folder, String subfolder, String filename) {
        if (lollipopOrLater()) {
            return getFileProviderUri_SAF(c, preferences, folder, subfolder, filename);
        } else {
            return getFileProviderUri_File(c,preferences, folder,subfolder,filename);
        }
    }
    private Uri getFileProviderUri_SAF(Context c, Preferences preferences, String folder, String subfolder, String filename) {
        // No real need as we are using content uris anyway
        return getUriForItem(c, preferences, folder, subfolder, filename);
    }
    private Uri getFileProviderUri_File(Context c, Preferences preferences, String folder, String subfolder, String filename) {
        String s = stringForFile(c,preferences, folder);
        File f = new File(s);
        if (subfolder != null && !subfolder.isEmpty() && !subfolder.equals(c.getString(R.string.mainfoldername)) && !subfolder.equals("MAIN")) {
            f = new File(f, subfolder);
        }
        if (filename != null && !filename.isEmpty() && !filename.equals(c.getString(R.string.mainfoldername)) && !filename.equals("MAIN")) {
            f = new File(f, filename);
        }
        // Convert to a FileProvider uri
        return FileProvider.getUriForFile(c,"OpenSongAppFiles",f);
    }

    DocumentFile documentFileFromRootUri(Context c, Uri uri, String path) {
        if (uri != null && lollipopOrLater()) {
            return DocumentFile.fromTreeUri(c, uri);
        } else if (path != null && !lollipopOrLater()) {
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
    private String getDocumentsContractId(Uri uri) {
        try {
            if (lollipopOrLater()) {
                return DocumentsContract.getDocumentId(uri);
            } else {
                return null;
            }
        }
        catch (Exception e) {
            return null;
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

    void writeImage(OutputStream outputStream, Bitmap bmp) {
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
            byte[] buf = new byte[1024];
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
            try {
                return outputStream.toString();
            } catch (Exception | OutOfMemoryError e) {
                return "";
            }

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
        } else if (!lollipopOrLater() && !uriExists(c, uri)){
            // Check it exists
            try {
                if (uri!=null && uri.getPath()!=null) {
                    File f = new File(uri.getPath());
                    if (!f.exists()) {
                        if (mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR)) {
                            if (!f.mkdirs()) {
                                Log.d("StorageAccess", "Unable to create file " + f);
                            }
                        } else {
                            if (!f.createNewFile()) {
                                Log.d("StorageAccess", "Unable to create file " + f);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Used to decide on the best storage method (using tree or not)
    boolean lollipopOrLater() {
        //boolean testingKitKat = true;
        //return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !testingKitKat;
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    // This builds an index of all the songs on the device
    @SuppressLint("NewApi")
    ArrayList<String> listSongs(Context c, Preferences preferences) {
        ArrayList<String> noSongs = new ArrayList<>();
        try {
            // Decide if we are using storage access framework or not
            if (lollipopOrLater()) {
                return listSongs_SAF(c, preferences);
            } else {
                return listSongs_File(c, preferences);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return noSongs;
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<String> listSongs_SAF(Context c, Preferences preferences) {
        // This gets all songs (including any subfolders)
        ArrayList<String> songIds = new ArrayList<>();
        Uri uri = getUriForItem(c,preferences,"Songs","","");

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
                                songIds.add(songFolderAndFileOnly(c,docId+"/")); // In case the folder is empty add it as a songId
                            }
                        } else if (docId.contains("OpenSong/Songs/")) {
                            songIds.add(songFolderAndFileOnly(c,docId));
                        }
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return songIds;
    }
    private ArrayList<String> listSongs_File(Context c, Preferences preferences) {
        // We must be using an older version of Android, so stick with File access
        ArrayList<String> songIds = new ArrayList<>();  // These will be the file locations

        String songsFolder = stringForFile(c,preferences,"Songs");
        ArrayList<String> foldersToIndex = new ArrayList<>();

        // Add the main songs folder to the foldersToIndex.  More will be added for subfolders
        foldersToIndex.add(songsFolder);
        int num = foldersToIndex.size();
        // Go through all the folders
        for (int i = 0; i < num; i++) {
            File folder = new File(foldersToIndex.get(i));
            File[] contents = folder.listFiles();
            // Go through each item and add songs or folders
            assert contents != null;
            for (File item : Objects.requireNonNull(contents)) {
                if (item.isDirectory()) {
                    foldersToIndex.add(item.getPath());
                    songIds.add(songFolderAndFileOnly(c,item.getPath())+"/");
                    num = foldersToIndex.size();
                } else if (item.isFile()) {
                    songIds.add(songFolderAndFileOnly(c,item.getPath()));
                }
            }
        }
        return songIds;
    }

    /*@SuppressLint("NewApi")
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
    }*/

    // Here are all the file accesses used in the app!!!
    Uri getUriForItem(Context c, Preferences preferences, String folder, String subfolder, String filename) {
        String[] fixedfolders = fixFoldersAndFiles(c, folder, subfolder, filename);
        if (lollipopOrLater()) {
            return getUriForItem_SAF(c, preferences, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        } else {
            return getUriForItem_File(c, preferences, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        }
    }
    @SuppressLint("NewApi")
    private Uri getUriForItem_SAF(Context c, Preferences preferences, String folder, String subfolder, String filename) {
        // Get the home folder as our start point
        if (uriTreeHome==null) {
            uriTreeHome = homeFolder(c,null,preferences);
        }
        Uri uri = uriTreeHome;

        // Now point to the specific folder (Songs, Sets, Backgrounds, etc.)
        if (folder != null && !folder.isEmpty() && uri!=null) {
            uri = Uri.withAppendedPath(uri, Uri.encode(folder));
        }

        // Now go through the subfolder(s)
        if (subfolder != null && !subfolder.equals(c.getString(R.string.mainfoldername)) && !subfolder.equals("MAIN") && uri!=null) {
            String[] sfs = subfolder.split("/");
            for (String sf : sfs) {
                if (sf != null && !sf.equals("") && !sf.equals(c.getString(R.string.mainfoldername)) && !sf.equals("MAIN")) {
                    uri = Uri.withAppendedPath(uri, Uri.encode(sf));
                }
            }
        }

        // Now add the filename
        if (filename != null && !filename.equals("") && uri!=null) {
            // Might have sent subfolder info
            String[] sfs = filename.split("/");
            for (String sf : sfs) {
                if (sf != null && !sf.equals("") && !sf.equals(c.getString(R.string.mainfoldername)) && !sf.equals("MAIN")) {
                    uri = Uri.withAppendedPath(uri, Uri.encode(sf));
                }
            }
        }

        // Now return the Uri in encoded format
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
    private Uri getUriForItem_File(Context c, Preferences preferences, String folder, String subfolder, String filename) {
        String s = stringForFile(c,preferences,folder);
        File f = new File(s);
        if (subfolder != null && !subfolder.isEmpty() && !subfolder.equals(c.getString(R.string.mainfoldername)) && !subfolder.equals("MAIN")) {
            f = new File(f, subfolder);
        }
        if (filename != null && !filename.isEmpty() && !filename.equals(c.getString(R.string.mainfoldername)) && !filename.equals("MAIN")) {
            f = new File(f, filename);
        }
        return Uri.fromFile(f);
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
            case "../Received":
                folder = "Received";
                subfolder = "";
                break;
        }
        if (folder==null || folder.equals(c.getResources().getString(R.string.mainfoldername)) || folder.equals("MAIN")) {
            folder = "";
        }

        if (subfolder.equals(c.getResources().getString(R.string.mainfoldername)) || subfolder.equals("MAIN")) {
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    boolean createFile(Context c, Preferences preferences, String mimeType, String folder, String subfolder, String filename) {
        String[] fixedfolders = fixFoldersAndFiles(c, folder, subfolder, filename);
        if (lollipopOrLater()) {
            return createFile_SAF(c, preferences, mimeType, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        } else {
            return createFile_File(c, preferences, fixedfolders[0], fixedfolders[1], fixedfolders[2]);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean createFile_SAF(Context c, Preferences preferences, String mimeType, String folder, String subfolder, String filename) {
        // Try this instead
        Uri parentUri;
        folder = removeStartAndEndSlashes(folder);
        subfolder = removeStartAndEndSlashes(subfolder);
        filename = removeStartAndEndSlashes(filename);

        Uri uritest = getUriForItem(c, preferences, folder, subfolder, filename);

        if (mimeType != null && mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR)) {
            // Check the filename is empty.  If not, add it to the subfolder
            // If something is sent as a filename, add it to the subfolder
            if (filename != null && !filename.isEmpty()) {
                subfolder = subfolder + "/" + filename;
                subfolder = subfolder.replace("//", "/");
            }

            // We first attempt to create the folder as it is (assuming all parent directories exist)
            // Split the subfolder up into parent director (may include lots of subfolders) and child to create

            String foldertocreate = subfolder;
            if (foldertocreate.contains("/")) {
                // We only want the last part to be the one to create, so split the subfolder up
                subfolder = foldertocreate.substring(0, foldertocreate.lastIndexOf("/"));
                foldertocreate = foldertocreate.replace(subfolder + "/", "");
            } else {
                // We need to create the subfolder, so it shouldn't be part of the parent
                subfolder = "";
            }

            subfolder = removeStartAndEndSlashes(subfolder);
            foldertocreate = removeStartAndEndSlashes(foldertocreate);

            parentUri = getUriForItem(c, preferences, folder, subfolder, "");

            // Only create if it doesn't exist
            if (!uriExists(c, uritest)) {
                if (!docContractCreate(c, parentUri, mimeType, foldertocreate)) {
                    // Error (likely parent directory doesn't exist
                    // Go through each folder and create the ones we need starting
                    String[] bits = subfolder.split("/");
                    String bit = "";
                    for (String s : bits) {
                        parentUri = getUriForItem(c, preferences, folder, bit, "");
                        docContractCreate(c, parentUri, mimeType, s);
                        bit = bit + "/" + s;
                    }
                }
            }
            return true;
        } else {
            // Must be a file

            // Filename could be sent as Band/temp/Song 1
            // Subfolder could be sent as Secular
            // Giving total subfolder as Secular/Band/temp and song as Song 1

            String completesubfolder = subfolder;
            String completefilename = filename;

            if (completefilename.contains("/")) {
                completesubfolder = completesubfolder + "/" + completefilename.substring(0, completefilename.lastIndexOf("/"));
                completefilename = completefilename.substring(completefilename.lastIndexOf("/"));

                // Replace double //
                completesubfolder = completesubfolder.replace("//", "/");
                completefilename = completefilename.replace("//", "/");
            }

            // Remove start and end /
            completesubfolder = removeStartAndEndSlashes(completesubfolder);
            completefilename = removeStartAndEndSlashes(completefilename);

            parentUri = getUriForItem(c, preferences, folder, completesubfolder, "");

            if (uriTreeHome==null) {
                uriTreeHome = homeFolder(c,null,preferences);
            }

            if (folder.isEmpty() && completesubfolder.isEmpty()) {
                // A temp file in the root folder
                docContractCreate(c,uriTreeHome,null,filename);

            } else if (!uriExists(c, uritest)) {

                if (!docContractCreate(c, parentUri, mimeType, completefilename)) {
                    // Error (likely parent directory doesn't exist)
                    // Go through each folder and create the ones we need starting at the 'folder'
                    String[] bits = completesubfolder.split("/");
                    String bit = "";
                    for (String s : bits) {
                        parentUri = getUriForItem(c, preferences, folder, bit, "");
                        docContractCreate(c, parentUri, DocumentsContract.Document.MIME_TYPE_DIR, s);
                        bit = bit + "/" + s;
                    }
                    // Try again!
                    parentUri = getUriForItem(c, preferences, folder, completesubfolder, "");
                    return docContractCreate(c, parentUri, mimeType, completefilename);
                }
            }
            return true;
        }
    }
    private boolean createFile_File(Context c, Preferences preferences, String folder, String subfolder, String filename) {
        boolean stuffCreated = false;
        String filepath = stringForFile(c,preferences,folder);
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean docContractCreate(Context c, Uri uri, String mimeType, String name) {
        try {
            return DocumentsContract.createDocument(c.getContentResolver(), uri, mimeType, name) != null;
        } catch (Exception e) {
            Log.d("StorageAccess", "Error creating " + name + " at " + uri);
            //e.printStackTrace();
            return false;
        }
    }

    private String removeStartAndEndSlashes(String s) {
        if (s!=null && s.startsWith("/")) {
            s = s.replaceFirst("/", "");
        }
        if (s!=null && s.endsWith("/")) {
            s = s.substring(0, s.lastIndexOf("/"));
        }
        return s;
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
            Log.d("d", "Unable to get encoding for " + uri);
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
            return uriExists_File(c, uri);
        }
    }
    private boolean uriExists_SAF(Context c, Uri uri) {
        try {
            InputStream is = c.getContentResolver().openInputStream(uri);
            if (is != null) {
                is.close();
            }
            return true;
        } catch (Exception e){
            return false;
        }
    }

    private boolean uriExists_File(Context c, Uri uri) {
        if (uri != null && uri.getScheme()!=null && uri.getScheme().equals("file")) {
            File df = null;
            if (uri.getPath() != null) {
                df = new File(uri.getPath());
            }
            if (df != null) {
                return df.exists();
            } else {
                return false;
            }
        } else {
            try {
                if (uri != null) {
                    c.getContentResolver().openInputStream(uri);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
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
        if (df==null) {
            return false;
        } else {
            try {
                return df.isFile();
            } catch (Exception e) {
                return false;
            }
        }
    }
    private boolean uriIsFile_File(Uri uri) {
        if (uri!=null && uri.getPath()!=null) {
            File f = new File(uri.getPath());
            return f.isFile();
        } else {
            return false;
        }
    }

    Uri fixLocalisedUri(Context c, Preferences preferences, String uriString) {
        // This checks for localised filenames first and fixes the Uri
        if (uriString.equals("ost_logo.png") || uriString.equals("ost_bg.png")) {
            uriString = "../OpenSong/Backgrounds/" + uriString;
        }

        if (uriString.startsWith("../OpenSong/Media/")) {
            // Remove this and get the proper location
            uriString = uriString.replace("../OpenSong/Media/","");
            uriString = uriString.replace("%20"," ");
            uriString = uriString.replace("%2F","/");
            // Now get the actual uri
            return getUriForItem(c, preferences, "Media", "", uriString);
        } else if (uriString.startsWith("../OpenSong/Pads/")) {
            uriString = uriString.replace("../OpenSong/Pads/", "");
            uriString = uriString.replace("%20"," ");
            uriString = uriString.replace("%2F","/");
            // Now get the actual uri
            return getUriForItem(c, preferences, "Pads", "", uriString);
        } else if (uriString.startsWith("../OpenSong/Backgrounds/")) {
            uriString = uriString.replace("../OpenSong/Backgrounds/", "");
            uriString = uriString.replace("%20"," ");
            uriString = uriString.replace("%2F","/");
            // Now get the actual uri
            return getUriForItem(c, preferences, "Backgrounds", "", uriString);
        } else if (uriString.startsWith("../OpenSong/Songs/")) {
            uriString = uriString.replace("../OpenSong/Songs/", "");
            uriString = uriString.replace("%20"," ");
            uriString = uriString.replace("%2F","/");
            // Now get the actual uri
            return getUriForItem(c, preferences, "Songs", "", uriString);
        } else {
            // Now get the actual uri
            return Uri.parse(uriString);
        }
    }

    String fixUriToLocal(Uri uri) {
        // If a file is in the OpenSong/ folder, let's localise it (important for sync)
        String path = "";
        if (uri!=null && uri.getPath()!=null) {
            path = uri.getPath();
            Log.d("StorageAccess","path="+path);
            if (path.contains("OpenSong/Media/") || path.contains("OpenSong/Pads/") || path.contains("OpenSong/Backgrounds/")) {
                path = path.substring(path.lastIndexOf("OpenSong/") + 9);
                path = "../OpenSong/" + path;
            } else {
                path = uri.toString();
            }
        }
        return path;
    }

    @SuppressLint("NewApi")
    ArrayList<String> listFilesInFolder(Context c, Preferences preferences, String folder, String subfolder) {
        String[] fixedfolders = fixFoldersAndFiles(c,folder,subfolder,"");
        if (lollipopOrLater()) {
            return listFilesInFolder_SAF(c, preferences, fixedfolders[0], fixedfolders[1]);
        } else {
            return listFilesInFolder_File(c, preferences, fixedfolders[0], fixedfolders[1]);
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ArrayList<String> listFilesInFolder_SAF(Context c, Preferences preferences, String folder, String subfolder) {
        ArrayList<String> al = new ArrayList<>();

        Uri locationtoindex = getUriForItem(c, preferences, folder, subfolder, "");

        // Now get a documents contract at this location
        String id = getDocumentsContractId(locationtoindex);

        // Get the children
        Uri children = getChildren(locationtoindex, id);
        ContentResolver contentResolver = c.getContentResolver();

        // Keep track of our directory hierarchy
        List<Uri> dirNodes = new LinkedList<>();
        dirNodes.add(children);

        while (!dirNodes.isEmpty()) {
            try {

                children = dirNodes.remove(0); // get the item from top
                Cursor cursor = contentResolver.query(children, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                    DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);

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
    private ArrayList<String> listFilesInFolder_File(Context c, Preferences preferences, String folder, String subfolder) {
        ArrayList<String> al = new ArrayList<>();
        String filebuilder = stringForFile(c, preferences, folder);
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

    // This is used to extract downloaded bible XML files from the zip
    void extractBibleZipFile(Context c, Preferences preferences, Uri zipUri) {
        String folder = "OpenSong Scripture";
        String subfolder = "";

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
                zis.closeEntry();
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
            if (df!=null) {
                return df.delete();
            } else {
                return false;
            }
        } catch (Exception e) {
            Log.d("StorageAccess","Unable to delete "+uri);
            return false;
        }
    }
    private boolean deleteFile_File(Uri uri) {
        try {
            if (uri != null && uri.getPath() != null) {
                File f = new File(uri.getPath());
                // If this is a directory, empty it first
                if (f.isDirectory() && f.listFiles() != null) {
                    for (File child : Objects.requireNonNull(f.listFiles())) {
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

    boolean renameFolder(Context c, Preferences preferences, String oldsubfolder, String newsubfolder) {
        if (lollipopOrLater()) {
            return renameFolder_SAF(c, preferences, oldsubfolder, newsubfolder);
        } else {
            return renameFolder_File(c, preferences, oldsubfolder, newsubfolder);
        }
    }

    private boolean renameFolder_File(Context c, Preferences preferences, String oldsubfolder, String newsubfolder) {
        // Now the long bit.  Go through the original folder and copy the files to the new location
        Uri oldUri = getUriForItem(c, preferences, "Songs", oldsubfolder, "");
        Uri newUri = getUriForItem(c, preferences, "Songs", newsubfolder, "");

        if (!uriExists(c, newUri)) {
            if (oldUri != null && newUri != null && oldUri.getPath() != null && newUri.getPath() != null) {
                File oldfile = new File(oldUri.getPath());
                File newfile = new File(newUri.getPath());
                if (oldfile.renameTo(newfile)) {
                    StaticVariables.myToastMessage = c.getString(R.string.rename) + " - " +
                            c.getString(R.string.ok);
                    StaticVariables.whichSongFolder = newsubfolder;
                    return true;
                } else {
                    StaticVariables.myToastMessage = c.getString(R.string.rename) + " - " +
                            c.getString(R.string.createfoldererror);
                    return false;
                }
            } else {
                StaticVariables.myToastMessage = c.getString(R.string.rename) + " - " +
                        c.getString(R.string.createfoldererror);
                return false;
            }
        } else {
            StaticVariables.myToastMessage = c.getString(R.string.rename) +
                    " - " + c.getString(R.string.folderexists);
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean renameFolder_SAF(Context c, Preferences preferences, String oldsubfolder, String newsubfolder) {
        // SAF can only rename final name (can't move within directory structure) - No / allowed!
        Uri oldUri = getUriForItem(c, preferences, "Songs", oldsubfolder, "");
        if (!newsubfolder.contains("/")) {
            try {
                DocumentsContract.renameDocument(c.getContentResolver(), oldUri, newsubfolder);
                StaticVariables.myToastMessage = c.getString(R.string.rename) + " - " +
                        c.getString(R.string.ok);
                StaticVariables.whichSongFolder = newsubfolder;
                return true;
            } catch (Exception e) {
                StaticVariables.myToastMessage = c.getString(R.string.rename) + " - " +
                        c.getString(R.string.createfoldererror);
                return false;
            }
        } else {
            // TODO write a script that iterates through the directory and subdirectories it contains
            // And copy them to the new location one at a time, then delete the old folder
            StaticVariables.myToastMessage = c.getString(R.string.rename) + " - " +
                    c.getString(R.string.createfoldererror);
            return false;
        }
    }

    boolean renameSetFile(Context c, Preferences preferences, String oldname, String newname) {
        String folder = "Sets";
        String oldsubfolder = "";
        String newsubfolder = "";
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
            if (lollipopOrLater()) {
                DocumentFile df = DocumentFile.fromSingleUri(c, olduri);
                if (df!=null) {
                    return df.delete();
                } else {
                    return false;
                }
            } else {
                if (olduri.getPath() != null) {
                    File f = new File(olduri.getPath());
                    return f.delete();
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
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
        if (df!=null) {
            return df.canWrite();
        } else {
            return false;
        }
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
    }

    /*boolean isXML(Uri uri) {
        boolean isxml = true;
        if (uri != null && uri.getLastPathSegment()!=null) {
            String name = uri.getLastPathSegment().toLowerCase(StaticVariables.locale);
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
    }*/

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
                        StaticVariables.myToastMessage = "foundsong";
                    } else if (xpp.getName().equals("set")) {
                        found = true; // It's a set
                        StaticVariables.myToastMessage = "foundset";
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
            String name = uri.getLastPathSegment().toLowerCase(StaticVariables.locale);
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

    String getPartOfUri(Uri uri) {
        // This gets the filename
        String path = uri.getPath();
        if (path!=null && path.contains("OpenSong/Songs")) {
            path = path.substring(path.lastIndexOf("OpenSong/Songs"));
        }
        return path;
    }

    boolean checkFileExtensionValid(Uri uri) {
        // This lets us know if the file is appropriate to read the title/author/key from during indexing
        String filename;
        if (uri!=null && uri.getLastPathSegment()!=null) {
            filename = uri.getLastPathSegment().toLowerCase(Locale.ROOT);
        } else {
            filename = "";
        }
        boolean isvalid = true;
        String type = null;
        if (filename.lastIndexOf(".")>1 && filename.lastIndexOf(".")<filename.length()-1) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            int index = filename.lastIndexOf('.')+1;
            String ext = filename.substring(index).toLowerCase(Locale.ROOT);
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
        String file_ext = StaticVariables.songfilename;
        FullscreenActivity.isImage = false;
        FullscreenActivity.isPDF = false;

        if (file_ext!=null) {
            file_ext = file_ext.toLowerCase(Locale.ROOT);
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

    private String songFolderAndFileOnly(Context c, String uriString) {
        // Get rid of all the uri info up to the end of /OpenSong/Songs
        // Also adds mainfoldername if the song isn't in a subfolder
        if (uriString.contains("OpenSong/Songs/")) {
            uriString = uriString.substring(uriString.indexOf("OpenSong/Songs/")+15);
        }
        if (!uriString.contains("/")) {
            uriString = c.getString(R.string.mainfoldername) + "/" + uriString;
        }
        uriString = uriString.replace("//","/");
        return uriString;
    }

    void writeSongIDFile(Context c, Preferences preferences, ArrayList<String> songIds) {
        // This creates a file in the app storage with a list of song folders/filenames
        StringBuilder stringBuilder = new StringBuilder();

        // Sort the array
        Locale locale = new Locale(preferences.getMyPreferenceString(c,"locale","en"));
        Collator collator = Collator.getInstance(locale);
        collator.setStrength(Collator.SECONDARY);
        Collections.sort(songIds,collator);
        for (String songId:songIds) {
            stringBuilder.append(songId).append("\n");
        }
        File songIDFile = new File(c.getExternalFilesDir("Database"),"SongIds.txt");
        Uri uri = Uri.fromFile(songIDFile);
        boolean fileexists;
        if (!uriExists(c,uri)) {
            try {
                fileexists = songIDFile.createNewFile();
            } catch (Exception e) {
                fileexists = false;
            }
        } else {
            fileexists = true;
        }

        if (fileexists) {
            OutputStream outputStream = getOutputStream(c, Uri.fromFile(songIDFile));
            if (outputStream!=null) {
                writeFileFromString(stringBuilder.toString(), outputStream);
            }
        }
    }

}
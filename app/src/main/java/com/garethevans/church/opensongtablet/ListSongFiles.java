package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

public class ListSongFiles {

    static Collator coll;

    public static void getAllSongFolders() {
        FullscreenActivity.allfilesforsearch.clear();

        File songfolder = new File(FullscreenActivity.dir.getAbsolutePath());
        File[] tempmyitems = null;
        if (songfolder.isDirectory()) {
            tempmyitems = songfolder.listFiles();
        }
        //Now set the size of the temp arrays
        ArrayList<String> firstleveldirectories = new ArrayList<>();
        ArrayList<String> secondleveldirectories = new ArrayList<>();
        ArrayList<String> tempProperDirectories = new ArrayList<>();

        //Now read the folder names for the first level directories
        if (tempmyitems!=null) {
            for (File tempmyitem : tempmyitems) {
                if (tempmyitem != null && tempmyitem.isDirectory()) {
                    firstleveldirectories.add(tempmyitem.getName());
                }
            }
        }

        //Now go through the firstlevedirectories and look for subfolders
        for (int x = 0; x < firstleveldirectories.size(); x++) {
            File folder = new File(FullscreenActivity.dir.getAbsolutePath() + "/" + firstleveldirectories.get(x));
            File[] subfoldersearch = folder.listFiles();
            if (subfoldersearch!=null) {
                for (File aSubfoldersearch : subfoldersearch) {
                    if (firstleveldirectories.get(x)!=null && aSubfoldersearch != null && aSubfoldersearch.isDirectory()) {
                        secondleveldirectories.add(firstleveldirectories.get(x) + "/" + aSubfoldersearch.getName());
                    }
                }
            }
        }

        // Now combine the two arrays and save them as a string array
        tempProperDirectories.addAll(firstleveldirectories);
        tempProperDirectories.addAll(secondleveldirectories);
        try {
            coll = Collator.getInstance(FullscreenActivity.locale);
            coll.setStrength(Collator.SECONDARY);
            Collections.sort(tempProperDirectories, coll);
        } catch (Exception e) {
            // Error sorting
        }

        // Add the main directory - +1 to add the MAIN folder as position 0
        FullscreenActivity.mSongFolderNames = new String[tempProperDirectories.size()+1];
        FullscreenActivity.mSongFolderNames[0] = FullscreenActivity.mainfoldername;
        for (int z=0; z<tempProperDirectories.size(); z++) {
            FullscreenActivity.mSongFolderNames[z+1] = tempProperDirectories.get(z);
        }
    }

    static void getAllSongFiles() {
        try {
            File foldertoindex;
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                foldertoindex = FullscreenActivity.dir;
            } else {
                foldertoindex = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder);
            }
            File[] tempmyFiles = foldertoindex.listFiles();

            ArrayList<String> tempProperSongFiles = new ArrayList<>();
            if (tempmyFiles != null) {
                for (File tempmyFile : tempmyFiles) {
                    if (tempmyFile != null && tempmyFile.isFile()) {
                        tempProperSongFiles.add(tempmyFile.getName());
                    }
                }
            }

            // Sort the files alphabetically using locale
            coll = Collator.getInstance(FullscreenActivity.locale);
            coll.setStrength(Collator.SECONDARY);
            try {
                Collections.sort(tempProperSongFiles, coll);
            } catch (Exception e) {
                // Problem sorting
            }

            FullscreenActivity.mSongFileNames = new String[tempProperSongFiles.size()];

            FullscreenActivity.mSongFileNames = tempProperSongFiles.toArray(FullscreenActivity.mSongFileNames);
        } catch (Exception e) {
            // Some error occured
        }
    }

    static void getSongDetails(Context c) {
        // Go through each song in the current folder and extract the title, key and author
        // If not a valid song, just return the file name
        try {
            FullscreenActivity.songDetails = new String[FullscreenActivity.mSongFileNames.length][3];
            boolean fileextensionok;
            String s_f;
            String utf;
            for (int r = 0; r < FullscreenActivity.mSongFileNames.length; r++) {
                String s = FullscreenActivity.mSongFileNames[r];
                String[] vals = new String[3];
                if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername) ||
                        FullscreenActivity.whichSongFolder.equals("")) {
                    s_f = FullscreenActivity.dir + "/" + s;
                } else {
                    s_f = FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + s;
                }
                File f = new File(s_f);
                if (f.exists()) {
                    fileextensionok = checkFileExtension(s);
                    utf = checkUtfEncoding(s_f, c);
                    if (fileextensionok) {
                        vals = getSongDetailsXML(f, s, utf);
                    } else {
                        // Non OpenSong
                        vals[0] = s;
                        vals[1] = "";
                        vals[2] = "";
                    }
                    if (vals[2] == null || vals[2].equals("")) {
                        vals[2] = "";
                    }
                    try {
                        FullscreenActivity.songDetails[r][0] = vals[0];
                        FullscreenActivity.songDetails[r][1] = vals[1];
                        FullscreenActivity.songDetails[r][2] = vals[2];
                    } catch (Exception e) {
                        // Error trying to get song details
                    }
                }
            }
        } catch (Exception e) {
            // Ooops, error
        }
    }

    private static String[] getSongDetailsXML(File f, String s, String utf) {
        String vals[] = new String[3];
        vals[0] = s;
        vals[1] = "";
        vals[2] = "";

        try {
            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance();

            factory.setNamespaceAware(true);
            XmlPullParser xpp;
            xpp = factory.newPullParser();

            InputStream inputStream = new FileInputStream(f);
            xpp.setInput(inputStream, utf);

            if (f.length()>0 && f.length()<500000) {
                int eventType;
                eventType = xpp.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("author")) {
                            vals[1] = LoadXML.parseFromHTMLEntities(xpp.nextText());
                        } else if (xpp.getName().equals("key")) {
                            vals[2] = LoadXML.parseFromHTMLEntities(xpp.nextText());
                        }
                    }
                    try {
                        eventType = xpp.next();
                    } catch (Exception e) {
                        // Oops!
                    }
                }
            } else {
                vals[1] = "";
                vals[2] = "";
            }
        } catch (Exception e) {
            vals[0] = s;
            vals[1] = "";
            vals[2] = "";
        }
         return vals;
    }

    private static boolean checkFileExtension(String s) {
        boolean isxml = true;
        s = s.toLowerCase();
        String type = null;
        if (s.lastIndexOf(".")>1 && s.lastIndexOf(".")<s.length()-1) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            int index = s.lastIndexOf('.')+1;
            String ext = s.substring(index).toLowerCase();
            type = mime.getMimeTypeFromExtension(ext);
        }

        if (type!=null && !type.equals("")) {
            if (type.contains("image") || type.contains("application") || type.contains("video") || type.contains("audio")) {
                return false;
            }
        }

        if (s.endsWith(".pdf") ||
                s.endsWith(".doc") || s.endsWith(".docx") ||
                s.endsWith(".jpg") || s.endsWith(".png") || s.endsWith(".gif") ||
                s.endsWith(".zip") || s.endsWith(".apk") || s.endsWith(".tar")  || s.endsWith(".backup")) {
            isxml = false;
        }
        return isxml;
    }

    private static String checkUtfEncoding(String s_f, Context c) {
        String utf = "";
        File file = new File (s_f);
        if (file.exists()) {
            utf = LoadXML.getUTFEncoding(file, c);
        }
        return utf;
    }

    static boolean clearAllSongs() {
        // Clear all songs in the songs folder
        File delPath = FullscreenActivity.dir;
        if (delPath.exists()) {
            try {
                FileUtils.deleteDirectory(delPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return delPath.mkdirs();
    }

    static void getCurrentSongIndex() {
        // Find the current song index from the song filename
        // Set them all to 0
        FullscreenActivity.currentSongIndex = 0;
        FullscreenActivity.nextSongIndex = 0;
        FullscreenActivity.previousSongIndex = 0;

        // Go through the array
        try {
            if (FullscreenActivity.mSongFileNames != null && FullscreenActivity.songfilename != null) {
                for (int s = 0; s < FullscreenActivity.mSongFileNames.length; s++) {
                    if (FullscreenActivity.mSongFileNames != null &&
                            FullscreenActivity.songfilename != null &&
                            FullscreenActivity.mSongFileNames[s] != null &&
                            FullscreenActivity.mSongFileNames[s].equals(FullscreenActivity.songfilename)) {
                        FullscreenActivity.currentSongIndex = s;
                        if (s > 0) {
                            FullscreenActivity.previousSongIndex = s - 1;
                        } else {
                            FullscreenActivity.previousSongIndex = s;
                        }
                        if (s < FullscreenActivity.mSongFileNames.length - 1) {
                            FullscreenActivity.nextSongIndex = s + 1;
                        } else {
                            FullscreenActivity.nextSongIndex = s;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.d("d","Some error with the song list");
        }
    }

    static void deleteSong(Context c) {
        FullscreenActivity.setView = false;
        String setFileLocation;
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            setFileLocation = FullscreenActivity.dir + "/" + FullscreenActivity.songfilename;
        } else {
            setFileLocation = FullscreenActivity.dir + "/" +
                    FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename;
        }

        File filetoremove = new File(setFileLocation);
        if (filetoremove.delete()) {
            FullscreenActivity.myToastMessage = "\"" + FullscreenActivity.songfilename + "\" "
                    + c.getString(R.string.songhasbeendeleted);
        } else {
            FullscreenActivity.myToastMessage = c.getString(R.string.deleteerror_start)
                    + " \"" + FullscreenActivity.songfilename + "\" "
                    + c.getString(R.string.deleteerror_end_song);
        }
    }

    static boolean blacklistFileType(String s) {
        s = s.toLowerCase();
        String type = null;
        if (s.lastIndexOf(".")>1 && s.lastIndexOf(".")<s.length()-1) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            int index = s.lastIndexOf('.')+1;
            String ext = s.substring(index).toLowerCase();
            type = mime.getMimeTypeFromExtension(ext);
        }

        if (type!=null && !type.equals("")) {
            if (type.contains("pdf")) {
                return false;
            } else if (type.contains("audio") || type.contains("application") || type.contains("video")) {
                return true;
            }
        }

        return false;
    }
}
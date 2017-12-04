package com.garethevans.church.opensongtablet;

import android.content.Context;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;

public class ListSongFiles {

    static Collator coll;
    private static ArrayList<String> filelist;

    public static void getAllSongFolders() {
        FullscreenActivity.allfilesforsearch.clear();
        FullscreenActivity.mSongFolderNames = FullscreenActivity.songfilelist.getFolderList();
    }

    /*
        incorporated new class here.
     */
    static void getAllSongFiles() {
        try {
            FullscreenActivity.mSongFileNames = FullscreenActivity.songfilelist.getSongFileListasArray();
            int j = 0;
        }catch (Exception e){
            Log.d(e.getMessage(), "Error caught in getAllSongFiles() in ListSongFiles.java");
        }
    }

    /*TODO why use a multidimensional array, when you could use an xml object?
    I've been reading about performance and I guess its because of performance
    limitations?  Is maintaining an object in memory expensive
    in terms of performance?  So, the class I created is essentially worse
    than reading directly from the file system?  I don't think so personally,
    as I don't think the garbage collector will be dereference either of the objects
    internal to the songfilelist class, and the songfilelist class persists for the
    lifetime of the app, so there shouldn't be any extra work, and the memory overhead
    is low and speed of access of cached variable is faster than file access, at
    least I guess.
     */
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
            InputStreamReader lineReader = new InputStreamReader(inputStream);
            BufferedReader buffreader = new BufferedReader(lineReader);

            String line;
            try {
                line = buffreader.readLine();
                if (line.contains("encoding=\"")) {
                    int startpos = line.indexOf("encoding=\"")+10;
                    int endpos = line.indexOf("\"",startpos);
                    String enc = line.substring(startpos,endpos);
                    if (enc!=null && enc.length()>0 && !enc.equals("")) {
                        utf = enc.toUpperCase();
                    }
                }
            } catch (Exception e) {
                Log.d("d","No encoding included in line 1");
            }

            inputStream.close();

            inputStream = new FileInputStream(f);
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
            Log.d(e.getMessage(),"Some error with the song list");
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
package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

public class ListSongFiles extends Activity {

    static Collator coll;

    // OLD ONE
    public static void listSongFolders() {
        FullscreenActivity.allfilesforsearch.clear();
        File songfolder = new File(FullscreenActivity.dir.getAbsolutePath());
        File[] tempmyitems = null;
        if (songfolder.isDirectory()) {
            tempmyitems = songfolder.listFiles();
        }
        // Go through this list and check if the item is a directory or a file.
        int tempnumitems;
        if (tempmyitems != null && tempmyitems.length>0) {
            tempnumitems = tempmyitems.length;
        } else {
            tempnumitems = 0;
        }
        int numactualdirs  = 0;
        for (int x=0; x<tempnumitems; x++) {
            if (tempmyitems[x] != null && tempmyitems[x].isDirectory()){
                numactualdirs ++;
            }
        }

        //Now set the size of the temp arrays
        ArrayList<String> tempProperDirectories = new ArrayList<>();

        //Now read the stuff into the temp array
        for (int x=0; x<tempnumitems; x++) {
            if (tempmyitems[x] != null && tempmyitems[x].isDirectory()) {
                tempProperDirectories.add(tempmyitems[x].getName());
            }
        }

        //Sort these arrays
        // Add locale sort
        coll = Collator.getInstance(FullscreenActivity.locale);
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(tempProperDirectories, coll);

        // Because the MAIN folder doesn't exist as a listed folder, it is just the root,
        // We need to add it as the first folder manually.
        // All other folders go as an index 1 higher
        FullscreenActivity.mSongFolderNames = new String[numactualdirs+1];
        FullscreenActivity.mSongFolderNames = tempProperDirectories.toArray(FullscreenActivity.mSongFolderNames);

        // Now go through each folder and add the file names to these arrays
        FullscreenActivity.childSongs = new String[numactualdirs+1][];

        // Add the MAIN folder first
        File[] temp_mainfiles = songfolder.listFiles();
        int main_numfiles = 0;
        if (songfolder.isDirectory() && temp_mainfiles != null) {
            main_numfiles = temp_mainfiles.length;
        }

        // Go through this list and check if the item is a directory or a file.
        if (temp_mainfiles != null && main_numfiles>0 && songfolder.isDirectory()) {
            main_numfiles = temp_mainfiles.length;
        } else {
            main_numfiles = 0;
        }
        //Now set the size of the temp arrays
        ArrayList<String> tempMainProperFiles= new ArrayList<>();
        int temp_mainnumfilescount = 0;
        for (int x=0; x<main_numfiles; x++) {
            if (temp_mainfiles[x] != null && !temp_mainfiles[x].isDirectory() && temp_mainfiles[x].isFile()){
                tempMainProperFiles.add(temp_mainfiles[x].getName());
                FullscreenActivity.allfilesforsearch.add(temp_mainfiles[x].getName() + " %%% " + FullscreenActivity.mainfoldername);
                temp_mainnumfilescount++;
            }
        }

        //Sort these arrays
        // Add locale sort
        Collections.sort(tempMainProperFiles,coll);
        //Collections.sort(tempMainProperFiles, String.CASE_INSENSITIVE_ORDER);

        FullscreenActivity.childSongs[0] = new String[temp_mainnumfilescount];
        FullscreenActivity.childSongs[0] = tempMainProperFiles.toArray(FullscreenActivity.childSongs[0]);

        for (int w=0;w<numactualdirs;w++) {
            File currsongfolder = new File(FullscreenActivity.dir.getAbsolutePath() + "/"+FullscreenActivity.mSongFolderNames[w]);
            File[] tempmyfiles = currsongfolder.listFiles();
            // Go through this list and check if the item is a directory or a file.
            int tempnumfiles;
            if (tempmyfiles != null && tempmyfiles.length>0) {
                tempnumfiles = tempmyfiles.length;
            } else {
                tempnumfiles = 0;
            }
            int numactualfiles  = 0;
            for (int x=0; x<tempnumfiles; x++) {
                if (tempmyfiles[x] != null && tempmyfiles[x].isFile()){
                    numactualfiles ++;
                }
            }

            //Now set the size of the temp arrays
            ArrayList<String> tempProperFiles= new ArrayList<>();

            //Now read the stuff into the temp array
            for (int x=0; x<numactualfiles; x++) {
                if (tempmyfiles[x] != null && tempmyfiles[x].isFile()) {
                    tempProperFiles.add(tempmyfiles[x].getName());
                }
            }

            //Sort these arrays
            // Add locale sort
            Collections.sort(tempProperFiles,coll);
            //Collections.sort(tempProperFiles, String.CASE_INSENSITIVE_ORDER);

            FullscreenActivity.childSongs[w+1] = new String[numactualfiles];
            FullscreenActivity.childSongs[w+1] = tempProperFiles.toArray(FullscreenActivity.childSongs[w+1]);

            for (int f=0; f<tempProperFiles.size(); f++) {
                try {
                    if (FullscreenActivity.mSongFolderNames[w]!=null) {
                        FullscreenActivity.allfilesforsearch_folder.add(FullscreenActivity.mSongFolderNames[w]);
                    }
                    if (tempProperFiles.get(f)!=null) {
                        FullscreenActivity.allfilesforsearch_song.add(tempProperFiles.get(f));
                        FullscreenActivity.allfilesforsearch.add(tempProperFiles.get(f) + " %%% " + FullscreenActivity.mSongFolderNames[w]);
                    }
                } catch (Exception e) {
                    //Something went wrong here
                    e.printStackTrace();
                }
            }
        }
    }

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

    public static void getAllSongFiles() {
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

    public static void getSongDetails() {
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
                    utf = checkUtfEncoding(s_f);
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

    public static void getNextSongInSetInfo(Context c) {
        // Get the filename of the nextsong in set
        int i = FullscreenActivity.indexSongInSet;
        if (i<FullscreenActivity.mSetList.length-1) {
            FullscreenActivity.nextSongInSet = FullscreenActivity.mSetList[i+1];
            // Now load this song quickly and extract the key
            String s_f = FullscreenActivity.dir + "/" + FullscreenActivity.nextSongInSet;
            File f = new File(s_f);
            String utf = checkUtfEncoding(s_f);
            String[] vals = getSongDetailsXML(f,FullscreenActivity.nextSongInSet,utf);
            if (!FullscreenActivity.nextSongInSet.contains("/")) {
                FullscreenActivity.nextSongInSet = "/" + FullscreenActivity.nextSongInSet;
            }
            String[] parts = FullscreenActivity.nextSongInSet.split("/");
            FullscreenActivity.nextSongInSet = parts[parts.length-1];
            FullscreenActivity.nextSongKeyInSet = vals[2];
        } else {
            FullscreenActivity.nextSongInSet = c.getString(R.string.lastsong);
            FullscreenActivity.nextSongKeyInSet = "";
        }
    }

    public static String[] getSongDetailsXML(File f, String s, String utf) {
        String vals[] = new String[3];
        vals[0] = s;

        try {
            XmlPullParserFactory factory;
            factory = XmlPullParserFactory.newInstance();

            factory.setNamespaceAware(true);
            XmlPullParser xpp;
            xpp = factory.newPullParser();

            InputStream inputStream = new FileInputStream(f);
            xpp.setInput(inputStream, utf);

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
        } catch (Exception e) {
            vals[0] = s;
            vals[1] = "";
            vals[2] = "";
        }
         return vals;
    }

    public static boolean checkFileExtension(String s) {
        boolean isxml = true;
        if (s.endsWith(".pdf") || s.endsWith(".PDF")) {
            isxml = false;
        }
        if (s.endsWith(".doc") || s.endsWith(".DOC") || s.endsWith(".docx") || s.endsWith(".docx")) {
            isxml = false;
        }
        if (s.endsWith(".jpg") || s.endsWith(".JPG") || s.endsWith(".png") || s.endsWith(".PNG") || s.endsWith(".gif") || s.endsWith(".GIF")) {
            isxml = false;
        }
        return isxml;
    }

    public static String checkUtfEncoding(String s_f) {
        String utf = "";
        File file = new File (s_f);
        if (file.exists()) {
            utf = LoadXML.getUTFEncoding(file);
        }
        return utf;
    }

    @SuppressWarnings("unused")
    public static boolean clearAllSongs(Context c) {
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

    // OLD ONE
    public static void listSongs() {
        // A temporary array to put the file names into.  Do this, sort the array
        // List the items in the main storage location into a temporary array.
        // What song folder is being viewed?
        // If it is MAIN then it is the main one
        File foldertoindex;
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            foldertoindex = FullscreenActivity.dir;
        } else {
            foldertoindex = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder);
        }
        File[] tempmyFiles = foldertoindex.listFiles();
        // Go through this list and check if the item is a directory or a file.
        // Add these to the correct array
        int tempnumfiles;
        if (tempmyFiles != null && tempmyFiles.length>0) {
            tempnumfiles = tempmyFiles.length;
        } else {
            tempnumfiles = 0;
        }
        int numactualfiles = 0;
        int numactualdirs  = 0;
        for (int x=0; x<tempnumfiles; x++) {
            if (tempmyFiles[x] != null && tempmyFiles[x].isFile()) {
                numactualfiles ++;
            } else if (tempmyFiles[x] != null){
                numactualdirs ++;
            }
        }

        //Now set the size of the temp arrays
        ArrayList<String> tempProperSongFiles = new ArrayList<>();
        ArrayList<String> tempProperDirectories = new ArrayList<>();

        //Now read the stuff into the temp arrays
        for (int x=0; x<tempnumfiles; x++) {
            if (tempmyFiles[x] != null && tempmyFiles[x].isFile()) {
                tempProperSongFiles.add(tempmyFiles[x].getName());
            } else if (tempmyFiles[x] != null && tempmyFiles[x].isDirectory()) {
                tempProperDirectories.add(tempmyFiles[x].getName());
            }
        }

        //Sort these arrays
        // Add locale sort
        coll = Collator.getInstance(FullscreenActivity.locale);
        coll.setStrength(Collator.SECONDARY);
        try {
            Collections.sort(tempProperSongFiles, coll);
            Collections.sort(tempProperDirectories,coll);
        } catch (Exception e) {
            // Error sorting for some reason
        }


        //Add folder name to first item of songlist
        if (FullscreenActivity.whichSongFolder!=null) {
            tempProperSongFiles.add(0, FullscreenActivity.whichSongFolder);
        }
        if (FullscreenActivity.mainfoldername!=null) {
            tempProperDirectories.add(0, FullscreenActivity.mainfoldername);
        }

        //Make the real arrays one bigger
        FullscreenActivity.mSongFileNames = new String[numactualfiles+1];
        //FullscreenActivity.mSongFileNames = new String[numactualfiles];
        FullscreenActivity.mSongFolderNames = new String[numactualdirs+1];
        //FullscreenActivity.mSongFolderNames = new String[numactualdirs];

        FullscreenActivity.mSongFileNames = tempProperSongFiles.toArray(FullscreenActivity.mSongFileNames);
        FullscreenActivity.mSongFolderNames = tempProperDirectories.toArray(FullscreenActivity.mSongFolderNames);
    }

    public static void getCurrentSongIndex() {
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

    public static void deleteSong(Context c) {
        FullscreenActivity.setView = false;
        Log.d("d","whichSongFolder="+FullscreenActivity.whichSongFolder);
        Log.d("d","songfilename="+FullscreenActivity.songfilename);

        String setFileLocation;
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            setFileLocation = FullscreenActivity.dir + "/" + FullscreenActivity.songfilename;
        } else {
            setFileLocation = FullscreenActivity.dir + "/" +
                    FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename;
        }

        Log.d("d","setFileLocation="+setFileLocation);

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

}
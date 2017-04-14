package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

// This class is called asynchronously
public class IndexSongs extends Activity {

    public interface MyInterface {
        void indexingDone();
    }

    public static MyInterface mListener;

    public static void doIndex(Context c) throws XmlPullParserException, IOException {
        FullscreenActivity.safetosearch = false;
        FullscreenActivity.search_database = null;
        FullscreenActivity.search_database = new ArrayList<>();

        // Prepare a blank log file to show the search index progress
/*
        searchindexlog = new File(FullscreenActivity.homedir+"/searchindexlog.txt");
*/

        FullscreenActivity.indexlog = "Search index progress.\n\n" +
                "If the last song shown in this list is not the last song in your directory, there was an error indexing it.\n" +
                "Please manually check that the file is a correctly formatted OpenSong file.\n\n\n";

/*
        FileOutputStream overWrite = new FileOutputStream(searchindexlog, false);
        overWrite.write(defaultlogtext.getBytes());
        overWrite.flush();
        overWrite.close();
*/

        // Get all the folders that are available
        File songfolder = new File(FullscreenActivity.dir.getAbsolutePath());
        File[] tempmyitems = null;
        if (songfolder.isDirectory()) {
            tempmyitems = songfolder.listFiles();
        }
        //Now set the size of the temp arrays
        ArrayList<String> fixedfolders = new ArrayList<>();
        ArrayList<String> firstleveldirectories = new ArrayList<>();
        ArrayList<String> secondleveldirectories = new ArrayList<>();

        //Now read the folder names for the first level directories
        if (tempmyitems!=null) {
            for (File tempmyitem : tempmyitems) {
                if (tempmyitem != null && tempmyitem.isDirectory() && tempmyitem.getName() != null) {
                    firstleveldirectories.add(tempmyitem.getName());
                }
            }
        }

        //Now go through the firstlevedirectories and look for subfolders
        for (int x = 0; x < firstleveldirectories.size(); x++) {
            File foldtosearch = new File(FullscreenActivity.dir.getAbsolutePath() + "/" + firstleveldirectories.get(x));
            File[] subfoldersearch = foldtosearch.listFiles();
            if (subfoldersearch!=null) {
                for (File aSubfoldersearch : subfoldersearch) {
                    if (firstleveldirectories.get(x) != null && aSubfoldersearch != null && aSubfoldersearch.isDirectory()) {
                        secondleveldirectories.add(firstleveldirectories.get(x)+"/"+aSubfoldersearch.getName());
                    }
                }
            }
        }

        // Now combine the two arrays and save them as a string array

        fixedfolders.add("");
        if (firstleveldirectories!=null) {
            fixedfolders.addAll(firstleveldirectories);

        }
        if (secondleveldirectories!=null) {
            fixedfolders.addAll(secondleveldirectories);
        }

        /*File songfolder = new File(FullscreenActivity.dir.getAbsolutePath());
        File[] tempmyitems = null;
        if (songfolder.isDirectory()) {
            tempmyitems = songfolder.listFiles();
        }

        // Go through each folder and add subfolders
        ArrayList<String> firstleveldirectories = new ArrayList<>();
        ArrayList<String> secondleveldirectories = new ArrayList<>();
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
            File folder = new File(FullscreenActivity.dir.getAbsolutePath() + "/" + firstlevedirectories.get(x));
            File[] subfoldersearch = folder.listFiles();
            if (subfoldersearch!=null) {
                for (File aSubfoldersearch : subfoldersearch) {
                    if (aSubfoldersearch != null && aSubfoldersearch.isDirectory()) {
                        secondlevedirectories.add(firstlevedirectories.get(x) + "/" + aSubfoldersearch.getName());
                    }
                }
            }
        }*/


        /*// Need to add MAIN folder still......
        ArrayList<File> fixedfolders = new ArrayList<>();
        fixedfolders.add(FullscreenActivity.dir);
        fixedfolders.addAll(firstleveldirectories);
        tempProperDirectories.addAll(secondleveldirectories);*/

        /*if (tempmyitems!=null) {
            for (File temp : tempmyitems) {
                if (temp.isDirectory()) {
                    fixedfolders.add(temp);
                }
            }
        }*/

        // Prepare the xml pull parser
        XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
        xppf.setNamespaceAware(true);
        XmlPullParser xpp = xppf.newPullParser();
        String filename;
        String folder;
        String title;
        String author;
        String lyrics;
        String theme;
        String copyright;
        String user1;
        String user2;
        String user3;
        String aka;
        String alttheme;
        String ccli;
        String key;
        String hymnnumber;

        // Now go through each folder and load each song in turn and then add it to the array
        for (String currfolder : fixedfolders) {
            // Removes start bit for subfolders
            // String foldername = currfolder.replace(songfolder.toString()+"/", "");
            String foldername = currfolder;


            File foldtosplit = new File(FullscreenActivity.dir.getAbsolutePath() + "/" + currfolder);
            File files[] = foldtosplit.listFiles();
            // Go through each file
            for (File file : files) {

                if (file.isFile() && file.exists() && file.canRead()) {

                    filename = file.getName();
                    // If in the main folder
                    if (foldername.equals("")) {
                        foldername = FullscreenActivity.mainfoldername;
                    }
                    folder = foldername;
                    author = "";
                    lyrics = "";
                    theme = "";
                    key = "";
                    hymnnumber = "";
                    copyright = "";
                    alttheme = "";
                    aka = "";
                    user1 = "";
                    user2 = "";
                    user3 = "";
                    ccli = "";

                    // Set the title as the filename by default in case this isn't an OpenSong xml
                    title = filename;

                    // Try to get the file length
                    long filesize;
                    try {
                        filesize = file.length();
                        filesize = filesize/1024;
                    } catch (Exception e) {
                        filesize = 1000000;
                    }

                    FileInputStream fis = new FileInputStream(file);
                    xpp.setInput(fis, null);

                    // Extract the title, author, key, lyrics, theme
                    int eventType = xpp.getEventType();
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("author")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    author = text;
                                }
                            } else if (xpp.getName().equals("title")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    title = text;
                                }
                            } else if (xpp.getName().equals("lyrics")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    lyrics = text;
                                }
                            } else if (xpp.getName().equals("key")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    key = text;
                                }
                            } else if (xpp.getName().equals("theme")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    theme = text;
                                }
                            } else if (xpp.getName().equals("copyright")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    copyright = text;
                                }
                            } else if (xpp.getName().equals("ccli")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    ccli = text;
                                }
                            } else if (xpp.getName().equals("alttheme")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    alttheme = text;
                                }
                            } else if (xpp.getName().equals("user1")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    user1 = text;
                                }
                            } else if (xpp.getName().equals("user2")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    user2 = text;
                                }
                            } else if (xpp.getName().equals("user3")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    user3 = text;
                                }
                            } else if (xpp.getName().equals("aka")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    aka = text;
                                }
                            } else if (xpp.getName().equals("hymn_number")) {
                                String text = xpp.nextText();
                                if (!text.equals("")) {
                                    hymnnumber = text;
                                }
                            }
                        }
                        try {
                            eventType = xpp.next();
                        } catch (Exception e) {
                            eventType = XmlPullParser.END_DOCUMENT;
                            // This wasn't an xml, so grab the file contents instead
                            // By default, make the lyrics the content, unless it is a pdf, image, etc.
                            if (filesize<250 &&
                                    !filename.contains(".pdf") && !filename.contains(".PDF") &&
                                    !filename.contains(".doc") && !filename.contains(".DOC") &&
                                    !filename.contains(".docx") && !filename.contains(".DOCX") &&
                                    !filename.contains(".png") && !filename.contains(".PNG") &&
                                    !filename.contains(".jpg") && !filename.contains(".JPG") &&
                                    !filename.contains(".gif") && !filename.contains(".GIF") &&
                                    !filename.contains(".jpeg") && !filename.contains(".JPEG")) {
                                FileInputStream grabFileContents = new FileInputStream(file);
                                lyrics = LoadXML.readTextFile(grabFileContents);
                            }
                        }
                    }

                    // Remove chord lines, empty lines and setions in lyrics (to save memory) - only line that start with " "
                    String lyricslines[] = lyrics.split("\n");
                    String shortlyrics = "";
                    for (String line : lyricslines) {
                        if (!line.startsWith(".") && !line.startsWith("[") && !line.equals("")) {
                            if (line.startsWith(";")) {
                                line = line.substring(1);
                            }
                            shortlyrics = shortlyrics + line;
                        }
                    }

                    shortlyrics = filename.trim() + " " + folder.trim() + " " + title.trim() + " " + author.trim() + " " +
                            c.getString(R.string.edit_song_key) + " " + key.trim() + " " + copyright.trim() + " " + ccli.trim() + " " +
                            user1.trim() + " " + user2.trim() + " " + user3.trim() + " " + alttheme.trim() + " " + aka.trim() + " " +
                            theme.trim() + " " + hymnnumber.trim() + " " + shortlyrics.trim();

                    // Replace unwanted symbols
                    shortlyrics = ProcessSong.removeUnwantedSymbolsAndSpaces(shortlyrics);

                    String item_to_add = filename + " _%%%_ " + folder + " _%%%_ " + title + " _%%%_ " + author + " _%%%_ " + shortlyrics + " _%%%_ " +
                            theme + " _%%%_ " + key + " _%%%_ " + hymnnumber;

                    if (item_to_add!=null) {
                        FullscreenActivity.search_database.add(item_to_add);
                    }

                    String line_to_add = folder + "/" + filename+"\n";

                    FullscreenActivity.indexlog += line_to_add;
                    /*FileOutputStream logoutput = new FileOutputStream (new File(searchindexlog.getAbsolutePath()), true); // true will be same as Context.MODE_APPEND
                    logoutput.write(line_to_add.getBytes());
                    logoutput.flush();
                    logoutput.close();*/
                }
            }
        }
        //FileOutputStream logoutput = new FileOutputStream (new File(searchindexlog.getAbsolutePath()), true); // true will be same as Context.MODE_APPEND
        int totalsongsindexed = FullscreenActivity.search_database.size();
        //int totalsongsfound = FullscreenActivity.mSongFileNames.length;
/*
        String status;
        if (totalsongsfound==totalsongsindexed) {
            status = "No errors found";
        } else {
            status = "Something is wrong with the last file listed";
        }
*/

        FullscreenActivity.indexlog += "\n\nTotal songs indexed="+totalsongsindexed+"\n\n";
/*
        logoutput.write(extra.getBytes());
        logoutput.flush();
        logoutput.close();
*/

        FullscreenActivity.safetosearch = true;
    }

    public static void ListAllSongs() {
        // get a list of all song subfolders
        ArrayList<String> allsongfolders = new ArrayList<>();
        ArrayList<String> allsongsinfolders = new ArrayList<>();

        // Add the MAIN folder
        if (FullscreenActivity.dir != null) {
            allsongfolders.add(FullscreenActivity.dir.toString());
        }

        File[] songsubfolders = FullscreenActivity.dir.listFiles();
        for (File isfolder:songsubfolders) {
            if (isfolder!=null && isfolder.isDirectory()) {
                allsongfolders.add(isfolder.toString());
            }
        }

        // Now we have all the directories, iterate through them adding each song
        for (String thisfolder:allsongfolders) {
            File currentfolder = new File(thisfolder);
            File[] thesesongs = currentfolder.listFiles();
            for (File thissong:thesesongs) {
                if (thissong.isFile()) {
                    String simplesong = thissong.toString().replace(FullscreenActivity.dir.toString()+"/","");
                    simplesong = simplesong.replace(FullscreenActivity.dir.toString(),"");
                    if (!simplesong.contains("/")) {
                        simplesong = "/" + simplesong;
                    }
                    allsongsinfolders.add(simplesong);
                }
            }
        }

        // Add this list to the main array
        FullscreenActivity.allfilesforsearch = allsongsinfolders;
    }

    static class IndexMySongs extends AsyncTask<Object,Void,String> {

        Context context;

        IndexMySongs(Context c) {
            context = c;
            mListener = (MyInterface) c;
        }

        @Override
        protected void onPreExecute() {
            FullscreenActivity.myToastMessage = context.getString(R.string.search_index_start);
            ShowToast.showToast(context);
        }

        @Override
        protected String doInBackground(Object... params) {
            String val;
            try {
                doIndex(context);
                val = "ok";
            } catch (Exception e) {
                e.printStackTrace();
                val = "error";
            }
            return val;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("error")) {
                FullscreenActivity.myToastMessage = context.getString(R.string.search_index_error)+"\n"+
                        context.getString(R.string.search_log);
                ShowToast.showToast(context);
                FullscreenActivity.safetosearch = true;
                SharedPreferences indexSongPreferences = context.getSharedPreferences("indexsongs",MODE_PRIVATE);
                SharedPreferences.Editor editor_index = indexSongPreferences.edit();
                editor_index.putBoolean("buildSearchIndex", true);
                editor_index.apply();
            } else {
                FullscreenActivity.myToastMessage = context.getString(R.string.search_index_end);
                ShowToast.showToast(context);
                mListener.indexingDone();
            }
        }
    }

}
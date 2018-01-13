package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
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
import java.util.Arrays;

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

        FullscreenActivity.indexlog = "Search index progress.\n\n" +
                "If the last song shown in this list is not the last song in your directory, there was an error indexing it.\n" +
                "Please manually check that the file is a correctly formatted OpenSong file.\n\n\n";

        // Get all the folders that are available
        ArrayList<String> fixedfolders = new ArrayList<>(Arrays.asList(FullscreenActivity.mSongFolderNames));
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
        String errmsg;
        errmsg = "";

        // Now go through each folder and load each song in turn and then add it to the array
        for (String currfolder : fixedfolders) {
            // Removes start bit for subfolders
            // String foldername = currfolder.replace(songfolder.toString()+"/", "");
            String foldername = currfolder;
            File foldtosplit;
            if (currfolder.equals(c.getString(R.string.mainfoldername))) {
                foldtosplit = new File(FullscreenActivity.dir.getAbsolutePath());
            } else {
                foldtosplit = new File(FullscreenActivity.dir.getAbsolutePath() + "/" + currfolder);
            }
            File files[] = foldtosplit.listFiles();
            // Go through each file
            if(files != null) {
                for (File file : files) {

                    if (file.isFile() && file.exists() && file.canRead()) {

                        filename = file.getName();
                        boolean isxml = true;
                        if (filename.toLowerCase().contains(".pdf") || filename.toLowerCase().contains(".doc") ||
                                filename.toLowerCase().contains(".jpg") || filename.toLowerCase().contains(".png") ||
                                filename.toLowerCase().contains(".bmp") || filename.toLowerCase().contains(".gif") ||
                                filename.toLowerCase().contains(".jpeg") || filename.toLowerCase().contains(".apk") ||
                                filename.toLowerCase().contains(".txt") || filename.toLowerCase().contains(".zip")) {
                            isxml = false;
                        }

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
                            filesize = filesize / 1024;
                        } catch (Exception e) {
                            filesize = 1000000;
                        }


                        if (isxml) {
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
                                    errmsg += "File with error = " + filename + "\n";
                                    // This wasn't an xml, so grab the file contents instead
                                    // By default, make the lyrics the content, unless it is a pdf, image, etc.
                                    if (filesize < 250 &&
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

                        if (item_to_add != null) {
                            FullscreenActivity.search_database.add(item_to_add);
                        }

                        String line_to_add = folder + "/" + filename + "\n";

                        FullscreenActivity.indexlog += line_to_add;
                    }
                }
            }
        }

        int totalsongsindexed = FullscreenActivity.search_database.size();

        FullscreenActivity.indexlog += "\n\nTotal songs indexed="+totalsongsindexed+"\n\n";
        FullscreenActivity.indexlog += "\n\nErrors in importing files\n\nThese songs are either not XML or have invalid XML\n\n" + errmsg;
        FullscreenActivity.safetosearch = true;
    }

    static class IndexMySongs extends AsyncTask<Object,Void,String> {

        @SuppressLint("StaticFieldLeak")
        Context context;

        IndexMySongs(Context c) {
            context = c;
            mListener = (MyInterface) c;
        }

        @Override
        protected void onPreExecute() {
            try {
                FullscreenActivity.myToastMessage = context.getString(R.string.search_index_start);
                ShowToast.showToast(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Object... params) {
            Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
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
            try {
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
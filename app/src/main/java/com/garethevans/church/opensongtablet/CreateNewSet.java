package com.garethevans.church.opensongtablet;

import android.app.Activity;
import org.xmlpull.v1.XmlPullParserException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CreateNewSet extends Activity {

    public static void doCreation() {
        FullscreenActivity.newSetContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n"
                + "<set name=\""
                + FullscreenActivity.settoload
                + "\">\r\n<slide_groups>\r\n";

        for (int x = 0; x < FullscreenActivity.mSetList.length; x++) {
            // Only add the lines that aren't back to options,
            // save this set, clear this set, load set, edit set, export set or blank line
            // Check if song is in subfolder
            if (!FullscreenActivity.mSetList[x].contains("/")) {
                FullscreenActivity.mSetList[x] = "/" + FullscreenActivity.mSetList[x];
            }

            // Split the string into two
            String[] songparts;
            songparts = FullscreenActivity.mSetList[x].split("/");
            // If the path isn't empty, add a forward slash to the end
            if (songparts[0].length() > 0) {
                songparts[0] = songparts[0] + "/";
            }
            if (!songparts[0].contains("Scripture") && !songparts[0].contains("Slide")) {
                // Adding a song
                FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                        + "  <slide_group name=\""
                        + songparts[1]
                        + "\" type=\"song\" presentation=\"\" path=\""
                        + songparts[0] + "\"/>\n";
            } else if (songparts[0].contains("Scripture") & !songparts[0].contains("Slide")) {
                // Adding a scripture
                // Load the scripture file up
                // Keep the songfile as a temp
                String tempsongfilename = FullscreenActivity.songfilename;
                File tempdir = FullscreenActivity.dir;
                FullscreenActivity.dir = FullscreenActivity.dirbibleverses;
                FullscreenActivity.songfilename = songparts[1];
                try {
                    LoadXML.loadXML();
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }

                FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

                String scripture_lyrics = FullscreenActivity.mLyrics;

                // Parse the lyrics into individual slides;
                scripture_lyrics = scripture_lyrics.replace("[]", "_SPLITHERE_");
                scripture_lyrics = scripture_lyrics.replace("\\n ", " ");
                scripture_lyrics = scripture_lyrics.replace("\n ", " ");
                scripture_lyrics = scripture_lyrics.replace("\n", " ");
                scripture_lyrics = scripture_lyrics.replace("\\n", " ");
                scripture_lyrics = scripture_lyrics.replace("  ", " ");
                scripture_lyrics = scripture_lyrics.replace(". ", ". ");

                String[] mySlides = scripture_lyrics.split("_SPLITHERE_");

                FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                        + "  <slide_group type=\"scripture\" name=\""
                        + songparts[1] + "|" + FullscreenActivity.mAuthor
                        + "\" print=\"true\">\r\n"
                        + "  <title>" + songparts[1] + "</title>\r\n"
                        + "  <slides>\r\n";

                for (int w = 1; w < mySlides.length; w++) {
                    if (mySlides[w] != null && mySlides[w].length() > 0) {
                        FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                                + "  <slide>\r\n"
                                + "  <body>" + mySlides[w].trim() + "</body>\r\n"
                                + "  </slide>\r\n";
                    }
                }
                FullscreenActivity.newSetContents = FullscreenActivity.newSetContents + "</slides>\r\n"
                        + "  <subtitle>" + "</subtitle>\r\n"
                        + "  <notes />\r\n"
                        + "</slide_group>\r\n";
                //Put the original songfilename back
                FullscreenActivity.songfilename = tempsongfilename;
                FullscreenActivity.dir = tempdir;
                try {
                    LoadXML.loadXML();
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }

                FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

            } else if (songparts[0].contains("Slide") && !songparts[0].contains("Scripture")) {
                // Adding a custom slide
                // Load the slide file up
                // Keep the songfile as a temp
                String tempsongfilename = FullscreenActivity.songfilename;
                File tempdir = FullscreenActivity.dir;
                FullscreenActivity.dir = FullscreenActivity.dircustomslides;
                FullscreenActivity.songfilename = songparts[1];
                try {
                    LoadXML.loadXML();
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }

                FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

                String slide_lyrics = FullscreenActivity.mLyrics;

                // Parse the lyrics into individual slides;
                slide_lyrics = slide_lyrics.replace("[]", "_SPLITHERE_");
                slide_lyrics = slide_lyrics.replace("\\n ", " ");
                slide_lyrics = slide_lyrics.replace("\n ", " ");
                slide_lyrics = slide_lyrics.replace("\n", " ");
                slide_lyrics = slide_lyrics.replace("\\n", " ");
                slide_lyrics = slide_lyrics.replace("  ", " ");
                slide_lyrics = slide_lyrics.replace(". ", ".  ");

                String[] mySlides = slide_lyrics.split("_SPLITHERE_");

                FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                        + "  <slide_group name=\"" + songparts[1]
                        + "\" type=\"custom\" print=\"true\""
                        + " seconds=\"" + FullscreenActivity.mUser1 + "\""
                        + " loop=\"" + FullscreenActivity.mUser2 + "\""
                        + " transition=\"" + FullscreenActivity.mUser3 + "\">\r\n"
                        + "<title>" + FullscreenActivity.mTitle + "</title>\r\n"
                        + "<subtitle>" + FullscreenActivity.mCopyright + "</subtitle>\r\n"
                        + "<notes>" + FullscreenActivity.mKeyLine + "</notes>\r\n"
                        + "<slides>\r\n";

                for (int w = 1; w < mySlides.length; w++) {
                    if (mySlides[w] != null && mySlides[w].length() > 0) {
                        FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                                + "  <slide>\r\n"
                                + "  <body>" + mySlides[w].trim() + "</body>\r\n"
                                + "  </slide>\r\n";
                    }
                }
                FullscreenActivity.newSetContents = FullscreenActivity.newSetContents + "</slides>\r\n"
                        + "</slide_group>\r\n";
                //Put the original songfilename back
                FullscreenActivity.songfilename = tempsongfilename;
                FullscreenActivity.dir = tempdir;
                try {
                    LoadXML.loadXML();
                } catch (XmlPullParserException | IOException e) {
                    e.printStackTrace();
                }

                FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
            }

        }
        FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                + "</slide_groups>\r\n</set>";

        // Write the string to the file
        FileOutputStream newFile;
        try {
            newFile = new FileOutputStream(FullscreenActivity.dirsets + "/"
                    + FullscreenActivity.settoload, false);
            newFile.write(FullscreenActivity.newSetContents.getBytes());
            newFile.flush();
            newFile.close();
            FullscreenActivity.myToastMessage = "yes";
        } catch (IOException e) {
            FullscreenActivity.myToastMessage = "no";
            e.printStackTrace();
        }
    }
}

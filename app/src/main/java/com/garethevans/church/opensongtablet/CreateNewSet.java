package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.util.Base64;
import org.xmlpull.v1.XmlPullParserException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class CreateNewSet extends Activity {

    public static String tempsongfilename;
    public static String tempdir;

    public static boolean doCreation() {

        // Keep the current song and directory aside for now
        tempsongfilename = FullscreenActivity.songfilename;
        tempdir = FullscreenActivity.whichSongFolder;

        // Only do this is the mSetList isn't empty
        if (FullscreenActivity.mSetList!=null && FullscreenActivity.mSetList.length>0) {
            FullscreenActivity.newSetContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<set name=\""
                    + FullscreenActivity.settoload
                    + "\">\n<slide_groups>\n";

            for (int x = 0; x < FullscreenActivity.mSetList.length; x++) {
                // Check if song is in subfolder
                if (!FullscreenActivity.mSetList[x].contains("/")) {
                    FullscreenActivity.mSetList[x] = "/" + FullscreenActivity.mSetList[x];
                }

                // Split the string into two
                String[] songparts;
                songparts = FullscreenActivity.mSetList[x].split("/");

                if (songparts.length<1) {
                    return false;
                }

                // If the path isn't empty, add a forward slash to the end
                if (songparts[0].length() > 0) {
                    songparts[0] = songparts[0] + "/";
                }
                if (!songparts[0].contains("**"+FullscreenActivity.image) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_variation) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_scripture) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_slide) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_note)) {
                    // Adding a song
                    FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                            + "  <slide_group name=\""
                            + songparts[1]
                            + "\" type=\"song\" presentation=\"\" path=\""
                            + songparts[0] + "\"/>\n";


                } else if (songparts[0].contains("**"+FullscreenActivity.text_scripture) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_variation) &&
                        !songparts[0].contains("**"+FullscreenActivity.image) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_slide) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_note)) {
                    // Adding a scripture
                    // Load the scripture file up
                    FullscreenActivity.whichSongFolder = "../Scripture/_cache";
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

                    String[] mySlides = scripture_lyrics.split("_SPLITHERE_");

                    String newname = songparts[1];
                    if (FullscreenActivity.mAuthor!="") {
                        newname = newname+"|"+FullscreenActivity.mAuthor;
                    }
                    FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                            + "  <slide_group type=\"scripture\" name=\""
                            + newname
                            + "\" print=\"true\">\n"
                            + "    <title>" + songparts[1] + "</title>\n"
                            + "    <slides>\n";

                    for (String mySlide : mySlides) {
                        if (mySlide != null && mySlide.length() > 0) {
                            FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                                    + "      <slide>\n"
                                    + "      <body>" + mySlide.trim() + "</body>\n"
                                    + "      </slide>\n";
                        }
                    }
                    FullscreenActivity.newSetContents = FullscreenActivity.newSetContents + "    </slides>\n"
                            + "    <subtitle>" + "</subtitle>\n"
                            + "    <notes />\n"
                            + "  </slide_group>\n";

                } else if (songparts[0].contains("**"+FullscreenActivity.text_slide) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_variation) &&
                        !songparts[0].contains("**"+FullscreenActivity.image) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_note) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_scripture)) {
                    // Adding a custom slide
                    // Load the slide file up
                    // Keep the songfile as a temp
                    FullscreenActivity.whichSongFolder = "../Slides/_cache";
                    FullscreenActivity.songfilename = songparts[1];
                    try {
                        LoadXML.loadXML();
                    } catch (XmlPullParserException | IOException e) {
                        e.printStackTrace();
                    }

                    FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

                    String slide_lyrics = FullscreenActivity.mLyrics;

                    if (slide_lyrics.indexOf("---\n") == 0) {
                        slide_lyrics = slide_lyrics.replaceFirst("---\n", "");
                    }
                    // Parse the lyrics into individual slides;
                    slide_lyrics = slide_lyrics.replace("---", "_SPLITHERE_");

                    String[] mySlides = slide_lyrics.split("_SPLITHERE_");

                    FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                            + "  <slide_group name=\"" + songparts[1]
                            + "\" type=\"custom\" print=\"true\""
                            + " seconds=\"" + FullscreenActivity.mUser1 + "\""
                            + " loop=\"" + FullscreenActivity.mUser2 + "\""
                            + " transition=\"" + FullscreenActivity.mUser3 + "\">\n"
                            + "    <title>" + FullscreenActivity.mTitle + "</title>\n"
                            + "    <subtitle>" + FullscreenActivity.mCopyright + "</subtitle>\n"
                            + "    <notes>" + FullscreenActivity.mKeyLine + "</notes>\n"
                            + "    <slides>\n";

                    for (String mySlide : mySlides) {
                        if (mySlide != null && mySlide.length() > 0) {
                            FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                                    + "      <slide>\n"
                                    + "        <body>" + mySlide.trim() + "</body>\n"
                                    + "      </slide>\n";
                        }
                    }

                    FullscreenActivity.newSetContents = FullscreenActivity.newSetContents + "    </slides>\n"
                            + "  </slide_group>\n";


                } else if (songparts[0].contains("**"+FullscreenActivity.text_note) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_variation) &&
                        !songparts[0].contains("**"+FullscreenActivity.image) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_slide) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_scripture)) {
                    // Adding a custom note

                    // Load the note up to grab the contents
                    FullscreenActivity.whichSongFolder = "../Notes/_cache";
                    FullscreenActivity.songfilename = songparts[1];
                    try {
                        LoadXML.loadXML();
                    } catch (XmlPullParserException | IOException e) {
                        e.printStackTrace();
                    }

                    FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

                    String slide_lyrics = FullscreenActivity.mLyrics;

                    FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                            + "  <slide_group name=\"# " + FullscreenActivity.text_note + " # - " + songparts[1] + "\""
                            + " type=\"custom\" print=\"true\" seconds=\"\" loop=\"\" transition=\"\">\n"
                            + "    <title></title>\n"
                            + "    <subtitle></subtitle>\n"
                            + "    <notes>" + slide_lyrics + "</notes>\n"
                            + "    <slides></slides>\n"
                            + "  </slide_group>\n";



                } else if (songparts[0].contains("**"+FullscreenActivity.text_variation) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_note) &&
                        !songparts[0].contains("**"+FullscreenActivity.image) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_slide) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_scripture)) {
                    // Adding a variation
                    // The entire song is copied to the notes, and a simplified version is copied to the text

                    // Load the variation song up to grab the contents
                    FullscreenActivity.whichSongFolder = "../Variations";
                    FullscreenActivity.songfilename = songparts[1];
                    try {
                        LoadXML.loadXML();
                    } catch (XmlPullParserException | IOException e) {
                        e.printStackTrace();
                    }

                    FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
                    String slide_lyrics = FullscreenActivity.mLyrics;
                    try {
                        byte[] data = FullscreenActivity.myXML.getBytes("UTF-8");
                        slide_lyrics = Base64.encodeToString(data, Base64.DEFAULT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Prepare the slide contents so it remains compatible with the desktop app
                    // Split the lyrics into individual lines
                    String[] lyrics_lines = FullscreenActivity.myLyrics.split("\n");
                    String currentslide = "";
                    ArrayList<String> newslides = new ArrayList<>();

                    for (String thisline:lyrics_lines) {
                        if (!thisline.equals("") && !thisline.startsWith(".") && !thisline.startsWith("[") && !thisline.startsWith(";")) {
                            // Add the current line into the new slide
                            // Replace any new line codes | with \n
                            thisline = thisline.replace("||","\n");
                            thisline = thisline.replace("---","\n");
                            thisline = thisline.replace("|","\n");
                            currentslide = currentslide + thisline.trim()  + "\n";
                        } else if (thisline.startsWith("[")) {
                            // Save the current slide and create a new one
                            currentslide = currentslide.trim();
                            newslides.add(currentslide);
                            currentslide = "";
                        }
                    }
                    newslides.add(currentslide);
                    // Now go back through the currentslides and write the slide text
                    String slidetexttowrite = "";
                    for (int z=0; z<newslides.size();z++) {
                        if (!newslides.get(z).equals("")) {
                            slidetexttowrite = slidetexttowrite
                                    + "      <slide>\n"
                                    + "        <body>" + newslides.get(z).trim() +"\n"
                                    + "        </body>\n"
                                    + "      </slide>\n";
                        }
                    }

                    FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                            + "  <slide_group name=\"# " + FullscreenActivity.text_variation + " # - " + songparts[1] + "\""
                            + " type=\"custom\" print=\"true\" seconds=\"\" loop=\"\" transition=\"\">\n"
                            + "    <title>"+songparts[1]+"</title>\n"
                            + "    <subtitle>"+FullscreenActivity.mAuthor+"</subtitle>\n"
                            + "    <notes>" + slide_lyrics + "</notes>\n"
                            + "    <slides>\n" + slidetexttowrite
                            + "    </slides>\n"
                            + "  </slide_group>\n";


                } else if (songparts[0].contains("**"+FullscreenActivity.image) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_variation) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_note) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_slide) &&
                        !songparts[0].contains("**"+FullscreenActivity.text_scripture)) {
                    // Adding a custom image slide

                    // Load the image slide up to grab the contents
                    FullscreenActivity.whichSongFolder = "../Images/_cache";
                    FullscreenActivity.songfilename = songparts[1];
                    try {
                        LoadXML.loadXML();
                    } catch (XmlPullParserException | IOException e) {
                        e.printStackTrace();
                    }

                    FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;

                    // Break all the images into the relevant slides
                    String[] separate_slide = FullscreenActivity.mUser3.split("\n");
                    String slide_code = "";

                    for (String aSeparate_slide : separate_slide) {
                        slide_code = slide_code
                                + "      <slide>\n"
                                + "        <filename>" + aSeparate_slide + "</filename>\n"
                                + "        <description>" + aSeparate_slide + "</description>\n"
                                + "      </slide>\n";
                    }

                    slide_code = slide_code.trim();

                    FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                            + "  <slide_group name=\"" + FullscreenActivity.songfilename
                            + "\" type=\"image\" print=\"true\" seconds=\"" + FullscreenActivity.mUser1
                            + "\" loop=\"" + FullscreenActivity.mUser2 + "\" transition=\"0\""
                            + " resize=\"screen\" keep_aspect=\"false\" link=\"true\">\n"
                            + "    <title>" + FullscreenActivity.mTitle + "</title>\n"
                            + "    <subtitle></subtitle>\n"
                            + "    <notes></notes>\n"
                            + "    <slides>\n"
                            + slide_code + "\n"
                            + "    </slides>\n"
                            + "  </slide_group>\n";
                }
            }

            FullscreenActivity.newSetContents = FullscreenActivity.newSetContents
                    + "</slide_groups>\n</set>";

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

            // Now we are finished, put the original songfilename back
            FullscreenActivity.songfilename = tempsongfilename;
            FullscreenActivity.whichSongFolder = tempdir;
            try {
                LoadXML.loadXML();
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
            }

            FullscreenActivity.myLyrics = FullscreenActivity.mLyrics;
        }
        return true;
    }
}

package com.garethevans.church.opensongtablet.songsandsets;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.SongXML;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

class CreateNewSet {

    boolean doCreation(Context c, Preferences preferences,
                       StorageAccess storageAccess, ProcessSong processSong, LoadSong loadSong,
                       SongXML songXML, ConvertOnSong convertOnSong, ConvertChoPro convertChoPro,
                       SQLiteHelper sqLiteHelper, ShowToast showToast) {

        // Keep the current song and directory aside for now
        String tempsongfilename = StaticVariables.songfilename;
        String tempdir = StaticVariables.whichSongFolder;

        StringBuilder sb = new StringBuilder();

        // Only do this is the mSetList isn't empty
        if (StaticVariables.mSetList!=null && StaticVariables.mSetList.length>0) {
            StaticVariables.newSetContents = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<set name=\""
                    + processSong.parseToHTMLEntities(StaticVariables.settoload)
                    + "\">\n<slide_groups>\n";

            for (int x = 0; x < StaticVariables.mSetList.length; x++) {
                // Check if song is in subfolder
                if (!StaticVariables.mSetList[x].contains("/")) {
                    StaticVariables.mSetList[x] = "/" + StaticVariables.mSetList[x];
                }

                // Split the string into two
                String[] songparts;
                songparts = StaticVariables.mSetList[x].split("/");

                if (songparts.length<1) {
                    return false;
                }

                // If the path isn't empty, add a forward slash to the end
                if (songparts[0].length() > 0) {
                    songparts[0] = songparts[0] + "/";
                }
                if (!songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note))) {
                    // Adding a song
                    // If we are not in a subfolder, we have two parts, otherwise, we have subfolders
                    String s_name;
                    StringBuilder f_name = new StringBuilder();
                    for (int pieces=0;pieces<(songparts.length - 1); pieces++) {
                        f_name.append("/").append(songparts[pieces]);
                    }
                    f_name = new StringBuilder(f_name.toString().replace("///", "/"));
                    f_name = new StringBuilder(f_name.toString().replace("//", "/"));
                    try {
                        s_name = songparts[songparts.length-1];
                    } catch (Exception e) {
                        e.printStackTrace();
                        s_name = songparts[1];
                    }

                    sb.append("  <slide_group name=\"")
                            .append(processSong.parseToHTMLEntities(s_name))
                            .append("\" type=\"song\" presentation=\"\" path=\"")
                            .append(processSong.parseToHTMLEntities(f_name.toString()))
                            .append("\"/>\n");

                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.scripture)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note))) {
                    // Adding a scripture
                    // Load the scripture file up
                    StaticVariables.whichSongFolder = "../Scripture/_cache";
                    StaticVariables.songfilename = songparts[1];
                    try {
                        loadSong.doLoadSong(c, storageAccess, preferences, songXML, processSong,
                                showToast, sqLiteHelper, convertOnSong, convertChoPro);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    String scripture_lyrics = StaticVariables.mLyrics;

                    // Parse the lyrics into individual slides;
                    scripture_lyrics = scripture_lyrics.replace("[]", "_SPLITHERE_");

                    String[] mySlides = scripture_lyrics.split("_SPLITHERE_");

                    String newname = songparts[1];
                    if (!StaticVariables.mAuthor.equals("")) {
                        newname = newname+"|"+ StaticVariables.mAuthor;
                    }
                    sb.append("  <slide_group type=\"scripture\" name=\"")
                            .append(processSong.parseToHTMLEntities(newname))
                            .append("\" print=\"true\">\n")
                            .append("    <title>")
                            .append(processSong.parseToHTMLEntities(songparts[1]))
                            .append("</title>\n");

                    sb.append("    <slides>\n");

                    for (String mySlide : mySlides) {
                        if (mySlide != null && mySlide.length() > 0) {
                            sb.append("      <slide>\n")
                                    .append("      <body>")
                                    .append(processSong.parseToHTMLEntities(mySlide.trim()))
                                    .append("</body>\n")
                                    .append("      </slide>\n");
                        }
                    }
                    sb.append("    </slides>\n")
                            .append("    <subtitle></subtitle>\n")
                            .append( "    <notes />\n")
                            .append("  </slide_group>\n");

                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture))) {
                    // Adding a custom slide
                    // Load the slide file up
                    // Keep the songfile as a temp
                    StaticVariables.whichSongFolder = "../Slides/_cache";
                    StaticVariables.songfilename = songparts[1];
                    try {
                        loadSong.doLoadSong(c, storageAccess, preferences, songXML, processSong,
                                showToast, sqLiteHelper, convertOnSong, convertChoPro);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    
                    String slide_lyrics = StaticVariables.mLyrics;

                    if (slide_lyrics.indexOf("---\n") == 0) {
                        slide_lyrics = slide_lyrics.replaceFirst("---\n", "");
                    }
                    // Parse the lyrics into individual slides;
                    slide_lyrics = slide_lyrics.replace("---", "_SPLITHERE_");

                    String[] mySlides = slide_lyrics.split("_SPLITHERE_");

                    sb.append("  <slide_group name=\"")
                            .append(processSong.parseToHTMLEntities(songparts[1]))
                            .append("\" type=\"custom\" print=\"true\" seconds=\"")
                            .append(processSong.parseToHTMLEntities(StaticVariables.mUser1))
                            .append("\" loop=\"")
                            .append(processSong.parseToHTMLEntities(StaticVariables.mUser2))
                            .append("\" transition=\"")
                            .append(processSong.parseToHTMLEntities(StaticVariables.mUser3))
                            .append("\">\n    <title>")
                            .append(processSong.parseToHTMLEntities(StaticVariables.mTitle))
                            .append("</title>\n    <subtitle>")
                            .append(processSong.parseToHTMLEntities(StaticVariables.mCopyright))
                            .append("</subtitle>\n    <notes>")
                            .append("")
                            .append("</notes>\n    <slides>\n");

                    for (String mySlide : mySlides) {
                        if (mySlide != null && mySlide.length() > 0) {
                            sb.append("      <slide>\n        <body>")
                                    .append(processSong.parseToHTMLEntities(mySlide.trim()))
                                    .append("</body>\n      </slide>\n");
                        }
                    }

                    sb.append("    </slides>\n  </slide_group>\n");

                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.note)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture))) {
                    // Adding a custom note

                    // Load the note up to grab the contents
                    StaticVariables.whichSongFolder = "../Notes/_cache";
                    StaticVariables.songfilename = songparts[1];
                    try {
                        loadSong.doLoadSong(c, storageAccess, preferences, songXML, processSong,
                                showToast, sqLiteHelper, convertOnSong, convertChoPro);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String slide_lyrics = StaticVariables.mLyrics;

                    sb.append("  <slide_group name=\"# ")
                            .append(processSong.parseToHTMLEntities(c.getResources().getString(R.string.note)))
                            .append(" # - ")
                            .append(songparts[1])
                            .append("\" type=\"custom\" print=\"true\" seconds=\"\" loop=\"\" transition=\"\">\n")
                            .append("    <title></title>\n")
                            .append("    <subtitle></subtitle>\n")
                            .append("    <notes>")
                            .append(processSong.parseToHTMLEntities(slide_lyrics))
                            .append("</notes>\n")
                            .append("    <slides></slides>\n")
                            .append("  </slide_group>\n");



                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture))) {
                    // Adding a variation
                    // The entire song is copied to the notes, and a simplified version is copied to the text

                    // Load the variation song up to grab the contents
                    StaticVariables.whichSongFolder = "../Variations";
                    StaticVariables.songfilename = songparts[1];
                    try {
                        loadSong.doLoadSong(c, storageAccess, preferences, songXML, processSong,
                                showToast, sqLiteHelper, convertOnSong, convertChoPro);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String slide_lyrics = StaticVariables.mLyrics;
                    try {
                        byte[] data = StaticVariables.myNewXML.getBytes(StandardCharsets.UTF_8);
                        slide_lyrics = Base64.encodeToString(data, Base64.DEFAULT);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // Prepare the slide contents so it remains compatible with the desktop app
                    // Split the lyrics into individual lines
                    String[] lyrics_lines = StaticVariables.mLyrics.split("\n");
                    StringBuilder currentslide = new StringBuilder();
                    ArrayList<String> newslides = new ArrayList<>();

                    for (String thisline:lyrics_lines) {
                        if (!thisline.equals("") && !thisline.startsWith(".") && !thisline.startsWith("[") && !thisline.startsWith(";")) {
                            // Add the current line into the new slide
                            // Replace any new line codes | with \n
                            thisline = thisline.replace("||","\n");
                            thisline = thisline.replace("---","\n");
                            thisline = thisline.replace("|","\n");
                            currentslide.append(thisline.trim()).append("\n");
                        } else if (thisline.startsWith("[")) {
                            // Save the current slide and create a new one
                            currentslide = new StringBuilder(currentslide.toString().trim());
                            newslides.add(currentslide.toString());
                            currentslide = new StringBuilder();
                        }
                    }
                    newslides.add(currentslide.toString());
                    // Now go back through the currentslides and write the slide text
                    StringBuilder slidetexttowrite = new StringBuilder();
                    for (int z=0; z<newslides.size();z++) {
                        if (!newslides.get(z).equals("")) {
                            slidetexttowrite.append("      <slide>\n")
                                    .append("        <body>")
                                    .append(processSong.parseToHTMLEntities(newslides.get(z).trim()))
                                    .append("\n")
                                    .append("        </body>\n")
                                    .append("      </slide>\n");
                        }
                    }

                    sb.append("  <slide_group name=\"# ")
                            .append(processSong.parseToHTMLEntities(c.getResources().getString(R.string.variation)))
                            .append(" # - ")
                            .append(songparts[1])
                            .append("\"")
                            .append(" type=\"custom\" print=\"true\" seconds=\"\" loop=\"\" transition=\"\">\n")
                            .append("    <title>")
                            .append(processSong.parseToHTMLEntities(songparts[1]))
                            .append("</title>\n")
                            .append("    <subtitle>")
                            .append(processSong.parseToHTMLEntities(StaticVariables.mAuthor))
                            .append("</subtitle>\n")
                            .append("    <notes>")
                            .append(processSong.parseToHTMLEntities(slide_lyrics))
                            .append("</notes>\n")
                            .append("    <slides>\n")
                            .append(slidetexttowrite)
                            .append("    </slides>\n")
                            .append("  </slide_group>\n");


                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture))) {
                    // Adding a custom image slide

                    // Load the image slide up to grab the contents
                    StaticVariables.whichSongFolder = "../Images/_cache";
                    StaticVariables.songfilename = songparts[1];
                    try {
                        loadSong.doLoadSong(c, storageAccess, preferences, songXML, processSong,
                                showToast, sqLiteHelper, convertOnSong, convertChoPro);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // The mUser3 field should contain all the images
                    // Break all the images into the relevant slides
                    String[] separate_slide = StaticVariables.mUser3.split("\n");

                    StringBuilder slide_code = new StringBuilder();

                    for (String aSeparate_slide : separate_slide) {
                        String imglinetext;
                        // Try to get the image into bytes
                        String imgcode = storageAccess.getImageSlide(c, aSeparate_slide);
                        if (!imgcode.isEmpty()) {
                            imglinetext = "        <image>" + imgcode.trim() + "</image>\n";
                        } else {
                            imglinetext = "        <filename>" + aSeparate_slide + "</filename>\n";
                        }
                        slide_code.append("      <slide>\n")
                                .append(imglinetext)
                                .append("        <description>")
                                .append(aSeparate_slide)
                                .append("</description>\n")
                                .append("      </slide>\n");
                    }

                    sb.append("  <slide_group name=\"")
                            .append(processSong.parseToHTMLEntities(StaticVariables.mAka))
                            .append("\" type=\"image\" print=\"true\" seconds=\"")
                            .append(processSong.parseToHTMLEntities(StaticVariables.mUser1))
                            .append("\" loop=\"")
                            .append(processSong.parseToHTMLEntities(StaticVariables.mUser2))
                            .append("\" transition=\"0\" resize=\"screen\" keep_aspect=\"false\" link=\"false\">\n")
                            .append("    <title>")
                            .append(processSong.parseToHTMLEntities(StaticVariables.mTitle))
                            .append("</title>\n")
                            .append("    <subtitle>")
                            .append(processSong.parseToHTMLEntities(StaticVariables.mAuthor))
                            .append("</subtitle>\n")
                            .append("    <notes>")
                            .append("")
                            .append("</notes>\n")
                            .append("    <slides>\n")
                            .append(slide_code)
                            .append("\n")
                            .append("    </slides>\n")
                            .append("  </slide_group>\n");
                }
            }

            sb.append("</slide_groups>\n</set>");

            StaticVariables.newSetContents = StaticVariables.newSetContents + sb.toString();
            // Write the string to the file
            Uri uri = storageAccess.getUriForItem(c, preferences, "Sets", "", StaticVariables.settoload);

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, uri, null, "Sets", "", StaticVariables.settoload);

            Log.d("CreateNewSet","StaticVariables.settoload="+StaticVariables.settoload);

            OutputStream outputStream = storageAccess.getOutputStream(c,uri);
            if (storageAccess.writeFileFromString(StaticVariables.newSetContents,outputStream)) {
                StaticVariables.myToastMessage = "yes";
                // Update the last loaded set now it is saved.
                preferences.setMyPreferenceString(c,"setCurrentBeforeEdits",preferences.getMyPreferenceString(c,"setCurrent",""));
            } else {
                StaticVariables.myToastMessage = "no";
            }

            preferences.setMyPreferenceString(c,"setCurrentLastName",StaticVariables.settoload);

            // Now we are finished, put the original songfilename back
            StaticVariables.songfilename = tempsongfilename;
            StaticVariables.whichSongFolder = tempdir;
            try {
                loadSong.doLoadSong(c, storageAccess, preferences, songXML, processSong,
                        showToast, sqLiteHelper, convertOnSong, convertChoPro);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            return false;
        }

        // Load the set again - to make sure everything is parsed
        SetActions setActions = new SetActions();
        setActions.prepareSetList(c,preferences);
        return true;
    }
}

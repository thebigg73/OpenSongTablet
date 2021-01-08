package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.setprocessing.SetActions;
import com.garethevans.church.opensongtablet.songprocessing.ConvertChoPro;
import com.garethevans.church.opensongtablet.songprocessing.ConvertOnSong;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.sqlite.CommonSQL;
import com.garethevans.church.opensongtablet.sqlite.SQLiteHelper;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;

public class CreateNewSet {

    boolean doCreation(Context c, Preferences preferences, StorageAccess storageAccess,
                       SongListBuildIndex songListBuildIndex, ProcessSong processSong, LoadSong loadSong,
                       ConvertOnSong convertOnSong, ConvertChoPro convertChoPro, Song song,
                       CurrentSet currentSet, SetActions setActions, SQLiteHelper sqLiteHelper,
                       CommonSQL commonSQL, ShowToast showToast, Locale locale) {

        StringBuilder sb = new StringBuilder();

        // Only do this if the current set isn't empty
        if (currentSet.getCurrentSet() != null && currentSet.getCurrentSet().size() > 0) {
            // Check all arrays are the same size!!
            setActions.checkArraysMatch(c, storageAccess, preferences, processSong, loadSong, currentSet);
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n").
                    append("<set name=\"").
                    append(processSong.parseToHTMLEntities(currentSet.getSetName())).
                    append("\">\n<slide_groups>\n");

            for (int x = 0; x < currentSet.getCurrentSet().size(); x++) {
                String path = currentSet.getCurrentSet_Folder().get(x);
                // If the path isn't empty, add a forward slash to the end
                if (!path.isEmpty()) {
                    path = path + "/";
                }
                String name = currentSet.getCurrentSet_Filename().get(x);
                boolean isImage = name.contains("**" + c.getString(R.string.image));
                boolean isVariation = name.contains("**" + c.getString(R.string.variation));
                boolean isScripture = name.contains("**" + c.getString(R.string.scripture));
                boolean isSlide = name.contains("**" + c.getString(R.string.slide));
                boolean isNote = name.contains("**" + c.getString(R.string.note));

                if (isImage) {
                    // Adding an image
                    Song tempSong = getTempSong(c,storageAccess,preferences,locale,
                            commonSQL, sqLiteHelper, song, processSong, convertChoPro, convertOnSong,
                            loadSong, showToast, songListBuildIndex, "../Images/_cache", name);
                    sb.append(buildImage(c,storageAccess,tempSong,processSong));

                } else if (isScripture) {
                    // Adding a scripture
                    Song tempSong = getTempSong(c,storageAccess,preferences,locale,
                            commonSQL, sqLiteHelper, song, processSong, convertChoPro, convertOnSong,
                            loadSong, showToast, songListBuildIndex, "../Scripture/_cache", name);
                    sb.append(buildScripture(tempSong,processSong));

                } else if (isVariation) {
                    // Adding a variation
                    Song tempSong = getTempSong(c,storageAccess,preferences,locale,
                            commonSQL, sqLiteHelper, song, processSong, convertChoPro, convertOnSong,
                            loadSong, showToast, songListBuildIndex, "../Variations", name);
                    sb.append(buildVariation(c,tempSong,processSong));

                } else if (isSlide) {
                    // Adding a slide
                    Song tempSong = getTempSong(c,storageAccess,preferences,locale,
                            commonSQL, sqLiteHelper, song, processSong, convertChoPro, convertOnSong,
                            loadSong, showToast, songListBuildIndex, "../Slides/_cache", name);
                    sb.append(buildSlide(tempSong,processSong));

                } else if (isNote) {
                    // Adding a note
                    Song tempSong = getTempSong(c,storageAccess,preferences,locale,
                            commonSQL, sqLiteHelper, song, processSong, convertChoPro, convertOnSong,
                            loadSong, showToast, songListBuildIndex, "../Notes/_cache", name);
                    sb.append(buildNote(c,tempSong,processSong));
                } else {
                    // Adding a song
                    sb.append(buildSong(processSong,path,name));

                }
            }
            sb.append("</slide_groups>\n</set>");

            currentSet.setCurrentSetXML(sb.toString());

            // Write the string to the file
            Uri uri = storageAccess.getUriForItem(c, preferences, "Sets", "", currentSet.getSetFile());

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, uri, null, "Sets", "", currentSet.getSetFile());

            OutputStream outputStream = storageAccess.getOutputStream(c,uri);
            if (storageAccess.writeFileFromString(currentSet.getCurrentSetXML(),outputStream)) {
                // Update the last loaded set now it is saved.
                preferences.setMyPreferenceString(c,"setCurrentBeforeEdits",preferences.getMyPreferenceString(c,"setCurrent",""));
            }

            preferences.setMyPreferenceString(c,"setCurrentLastName",currentSet.getSetFile());
            return true;
        } else {
            return false;
        }
    }


    private StringBuilder buildSong(ProcessSong processSong, String path, String name) {
        StringBuilder sb = new StringBuilder();
        sb.append("  <slide_group name=\"")
                .append(processSong.parseToHTMLEntities(name))
                .append("\" type=\"song\" presentation=\"\" path=\"")
                .append(processSong.parseToHTMLEntities(path))
                .append("\"/>\n");
        return sb;
    }
    private StringBuilder buildScripture(Song tempSong, ProcessSong processSong) {
        StringBuilder sb = new StringBuilder();

        // The scripture is loaded to a new, temp song object
        String scripture_lyrics = tempSong.getLyrics();

        // Parse the lyrics into individual slides;
        scripture_lyrics = scripture_lyrics.replace("[]", "_SPLITHERE_");

        String[] mySlides = scripture_lyrics.split("_SPLITHERE_");

        String newname = tempSong.getFilename();
        if (!tempSong.getAuthor().equals("")) {
            newname = newname+"|"+ tempSong.getAuthor();
        }
        sb.append("  <slide_group type=\"scripture\" name=\"")
                .append(processSong.parseToHTMLEntities(newname))
                .append("\" print=\"true\">\n")
                .append("    <title>")
                .append(processSong.parseToHTMLEntities(tempSong.getFilename()))
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

        return sb;
    }
    private StringBuilder buildVariation(Context c, Song tempSong, ProcessSong processSong) {
        StringBuilder sb = new StringBuilder();

        // The variation is loaded to a new, temp song object
        // The entire song is copied to the notes as an encrypted string, and a simplified version is copied to the text

        String slide_lyrics = tempSong.getLyrics();
        try {
            byte[] data = processSong.getXML(tempSong).getBytes(tempSong.getEncoding());
            slide_lyrics = Base64.encodeToString(data, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Prepare the slide contents so it remains compatible with the desktop app
        // Split the lyrics into individual lines
        String[] lyrics_lines = tempSong.getLyrics().split("\n");
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
                .append(processSong.parseToHTMLEntities(c.getString(R.string.variation)))
                .append(" # - ")
                .append(tempSong.getFilename())
                .append("\"")
                .append(" type=\"custom\" print=\"true\" seconds=\"\" loop=\"\" transition=\"\">\n")
                .append("    <title>")
                .append(processSong.parseToHTMLEntities(tempSong.getTitle()))
                .append("</title>\n")
                .append("    <subtitle>")
                .append(processSong.parseToHTMLEntities(tempSong.getAuthor()))
                .append("</subtitle>\n")
                .append("    <notes>")
                .append(processSong.parseToHTMLEntities(slide_lyrics))
                .append("</notes>\n")
                .append("    <slides>\n")
                .append(slidetexttowrite)
                .append("    </slides>\n")
                .append("  </slide_group>\n");

        return sb;
    }
    private StringBuilder buildSlide(Song tempSong, ProcessSong processSong) {
        StringBuilder sb = new StringBuilder();
        // Adding a custom slide
        String slide_lyrics = tempSong.getLyrics();

        if (slide_lyrics.indexOf("---\n") == 0) {
            slide_lyrics = slide_lyrics.replaceFirst("---\n", "");
        }
        // Parse the lyrics into individual slides;
        slide_lyrics = slide_lyrics.replace("---", "_SPLITHERE_");

        String[] mySlides = slide_lyrics.split("_SPLITHERE_");

        sb.append("  <slide_group name=\"")
                .append(processSong.parseToHTMLEntities(tempSong.getFilename()))
                .append("\" type=\"custom\" print=\"true\" seconds=\"")
                .append(processSong.parseToHTMLEntities(tempSong.getUser1()))
                .append("\" loop=\"")
                .append(processSong.parseToHTMLEntities(tempSong.getUser2()))
                .append("\" transition=\"")
                .append(processSong.parseToHTMLEntities(tempSong.getUser3()))
                .append("\">\n    <title>")
                .append(processSong.parseToHTMLEntities(tempSong.getTitle()))
                .append("</title>\n    <subtitle>")
                .append(processSong.parseToHTMLEntities(tempSong.getCopyright()))
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

        return sb;
    }
    private StringBuilder buildNote(Context c, Song tempSong, ProcessSong processSong) {
        StringBuilder sb = new StringBuilder();
        // Adding a note

        String slide_lyrics = tempSong.getLyrics();

        sb.append("  <slide_group name=\"# ")
                .append(processSong.parseToHTMLEntities(c.getResources().getString(R.string.note)))
                .append(" # - ")
                .append(tempSong.getFilename())
                .append("\" type=\"custom\" print=\"true\" seconds=\"\" loop=\"\" transition=\"\">\n")
                .append("    <title></title>\n")
                .append("    <subtitle></subtitle>\n")
                .append("    <notes>")
                .append(processSong.parseToHTMLEntities(slide_lyrics))
                .append("</notes>\n")
                .append("    <slides></slides>\n")
                .append("  </slide_group>\n");

        return sb;
    }
    private StringBuilder buildImage(Context c, StorageAccess storageAccess, Song tempSong, ProcessSong processSong) {
        // Adding a custom image slide
        StringBuilder sb = new StringBuilder();

        // The mUser3 field should contain all the images
        // Break all the images into the relevant slides
        String[] separate_slide = tempSong.getUser3().split("\n");

        StringBuilder slideCode = new StringBuilder();
        for (String aSeparate_slide : separate_slide) {
            String imglinetext;
            // Try to get the image into bytes
            String imgcode = storageAccess.getImageSlide(c, aSeparate_slide);
            if (!imgcode.isEmpty()) {
                imglinetext = "        <image>" + imgcode.trim() + "</image>\n";
            } else {
                imglinetext = "        <filename>" + aSeparate_slide + "</filename>\n";
            }
            slideCode.append("      <slide>\n")
                    .append(imglinetext)
                    .append("        <description>")
                    .append(aSeparate_slide)
                    .append("</description>\n")
                    .append("      </slide>\n");
        }

        sb.append("  <slide_group name=\"")
                .append(processSong.parseToHTMLEntities(tempSong.getAka()))
                .append("\" type=\"image\" print=\"true\" seconds=\"")
                .append(processSong.parseToHTMLEntities(tempSong.getUser1()))
                .append("\" loop=\"")
                .append(processSong.parseToHTMLEntities(tempSong.getUser2()))
                .append("\" transition=\"0\" resize=\"screen\" keep_aspect=\"false\" link=\"false\">\n")
                .append("    <title>")
                .append(processSong.parseToHTMLEntities(tempSong.getTitle()))
                .append("</title>\n")
                .append("    <subtitle>")
                .append(processSong.parseToHTMLEntities(tempSong.getAuthor()))
                .append("</subtitle>\n")
                .append("    <notes>")
                .append("")
                .append("</notes>\n")
                .append("    <slides>\n")
                .append(slideCode)
                .append("\n")
                .append("    </slides>\n")
                .append("  </slide_group>\n");

        return sb;
    }


    private Song getTempSong(Context c, StorageAccess storageAccess, Preferences preferences,
                             Locale locale, CommonSQL commonSQL, SQLiteHelper sqLiteHelper, Song song,
                             ProcessSong processSong, ConvertChoPro convertChoPro,
                             ConvertOnSong convertOnSong, LoadSong loadSong, ShowToast showToast,
                             SongListBuildIndex songListBuildIndex, String folder, String name) {
        Song tempSong = processSong.initialiseSong(commonSQL,folder, name, song);
        try {
            tempSong = loadSong.doLoadSong(c, storageAccess, preferences, processSong, showToast, locale,
                    songListBuildIndex, sqLiteHelper, commonSQL, song, convertOnSong, convertChoPro,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tempSong;
    }
    /*

            for (int x = 0; x < curren; x++) {




                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.scripture)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note))) {
                    // Adding a scripture


                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture))) {


                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.note)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture))) {
                    // Adding a custom note





                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture))) {
                    // Adding a variation


                } else if (songparts[0].contains("**"+c.getResources().getString(R.string.image)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.variation)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.note)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.slide)) &&
                        !songparts[0].contains("**"+c.getResources().getString(R.string.scripture))) {

            }


    }
*/

}

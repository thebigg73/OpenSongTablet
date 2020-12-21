package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import androidx.core.content.FileProvider;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class ExportPreparer {

    private String song_title;
    private String song_author;
    private String song_hymnnumber;
    private String song_key;
    private String folderstoexport = "";
    private final ArrayList<String> filesinset = new ArrayList<>();
    private final ArrayList<String> filesinset_ost = new ArrayList<>();
    private Backup_Create_Selected backup_create_selected;
    private ZipOutputStream outSelected;

    Intent exportSet(Context c, Preferences preferences, StorageAccess storageAccess, ProcessSong processSong, MakePDF makePDF, SQLiteHelper sqLiteHelper) {
        String nicename = StaticVariables.settoload;

        // This is the actual set file
        Uri seturi = storageAccess.getFileProviderUri(c, preferences, "Sets", "", StaticVariables.settoload);

        // These get set later if the user wants them
        Uri texturi = null;
        Uri desktopuri = null;
        Uri ostsuri = null;

        // Read in the set
        setParser(c, preferences, storageAccess);

        // Make the set name nicer (add the category in brackets)
        if (StaticVariables.settoload.contains("__")) {
            String[] bits = StaticVariables.settoload.split("__");
            String category = "";
            String name = StaticVariables.settoload;
            if (bits[0]!=null && !bits[0].equals("")) {
                category = " (" + bits[0] + ")";
            }
            if (bits[1]!=null && !bits[1].equals("")) {
                name = bits[1];
            }
            nicename = name + category;
        }

        // Prepare the email intent
        Intent emailIntent = setEmailIntent(nicename,nicename,nicename + "\n\n" + FullscreenActivity.emailtext);

        // If the user has requested to attach a .txt version of the set
        if (preferences.getMyPreferenceBoolean(c,"exportText",true)) {
            texturi = storageAccess.getFileProviderUri(c, preferences, "Export", "",
                    storageAccess.safeFilename(StaticVariables.settoload)+".txt");

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, texturi, null, "Export", "", storageAccess.safeFilename(StaticVariables.settoload) + ".txt");

            // Write the set text file
            OutputStream outputStream = storageAccess.getOutputStream(c,texturi);
            storageAccess.writeFileFromString(FullscreenActivity.emailtext,outputStream);
        }

        // Reset the text version of the set
        FullscreenActivity.emailtext = "";

        // If the user wants to attach the normal set (desktop file) without and xml extenstion, set the uri
        if (preferences.getMyPreferenceBoolean(c,"exportDesktop",false)) {
            desktopuri = seturi;
        }

        // If the user wants to add the OpenSongApp version of the set (same as desktop with .osts extension)
        if (preferences.getMyPreferenceBoolean(c,"exportOpenSongAppSet",true)) {
            // Copy the set file to an .osts file
            InputStream inputStream = storageAccess.getInputStream(c,seturi);

            ostsuri = storageAccess.getFileProviderUri(c, preferences, "Export", "", storageAccess.safeFilename(StaticVariables.settoload) + ".osts");

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, ostsuri, null, "Export", "", storageAccess.safeFilename(StaticVariables.settoload) + ".osts");

            OutputStream outputStream = storageAccess.getOutputStream(c,ostsuri);
            storageAccess.copyFile(inputStream,outputStream);
        }



        // Add the uris for each file requested
        ArrayList<Uri> uris = new ArrayList<>();
        if (texturi!=null) {
            uris.add(texturi);
        }
        if (ostsuri!=null) {
            uris.add(ostsuri);
        }
        if (desktopuri!=null) {
            uris.add(desktopuri);
        }

        // Go through each song in the set and attach them (assuming they exist!)
        // Also try to attach a copy of the song ending in .ost, as long as they aren't images if the user requested that
        if (preferences.getMyPreferenceBoolean(c,"exportOpenSongApp",true)) {
            for (int q = 0; q < FullscreenActivity.exportsetfilenames.size(); q++) {
                // Remove any subfolder from the exportsetfilenames_ost.get(q)
                String tempsong_ost = FullscreenActivity.exportsetfilenames_ost.get(q);
                tempsong_ost = tempsong_ost.substring(tempsong_ost.lastIndexOf("/") + 1);
                Uri songtoload = storageAccess.getFileProviderUri(c, preferences, "Songs", "",
                        FullscreenActivity.exportsetfilenames.get(q));


                String s = songtoload.toString();
                try {
                    if (songtoload.getLastPathSegment() != null) {
                        s = songtoload.getLastPathSegment().toLowerCase(StaticVariables.locale);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


                // Copy the song
                if (!storageAccess.lollipopOrLater() || storageAccess.uriExists(c, songtoload)) {

                    // We want to add a .ost file (OpenSongApp song)
                    if (preferences.getMyPreferenceBoolean(c,"exportOpenSongApp",true) && !isImgOrPDF(s)) {
                        InputStream inputStream = storageAccess.getInputStream(c, songtoload);
                        if (inputStream != null) {
                            Uri ostsongcopy = storageAccess.getFileProviderUri(c, preferences, "Notes", "_cache",
                                    storageAccess.safeFilename(tempsong_ost) + ".ost");
                            // Check the uri exists for the outputstream to be valid
                            storageAccess.lollipopCreateFileForOutputStream(c, preferences, ostsongcopy, null, "Notes", "_cache", storageAccess.safeFilename(tempsong_ost) + ".ost");
                            OutputStream outputStream = storageAccess.getOutputStream(c, ostsongcopy);
                            storageAccess.copyFile(inputStream, outputStream);
                            uris.add(ostsongcopy);
                        }
                    }
                }
            }
        }

        // Add the standard song file (desktop version) - if it exists
        if (preferences.getMyPreferenceBoolean(c,"exportDesktop",false)) {
            for (int q = 0; q < FullscreenActivity.exportsetfilenames.size(); q++) {
                if (!isImgOrPDF(FullscreenActivity.exportsetfilenames.get(q))) {
                    Uri uri = storageAccess.getFileProviderUri(c, preferences, "Songs", "", FullscreenActivity.exportsetfilenames.get(q));
                    if (storageAccess.uriExists(c, uri)) {
                        uris.add(uri);
                    }
                }
            }
        }

        // Add a pdf
        if (preferences.getMyPreferenceBoolean(c,"exportPDF",false)) {
            for (int q = 0; q < FullscreenActivity.exportsetfilenames.size(); q++) {
                String s = FullscreenActivity.exportsetfilenames.get(q);

                if (!isImgOrPDF(s)) {
                    if (s.startsWith("/# Variation # - ")) {
                        s = s.replace("# Variation # - ","../Variations/");
                    }
                    if (s.startsWith("/") && !s.contains(c.getResources().getString(R.string.mainfoldername))) {
                        s = c.getResources().getString(R.string.mainfoldername) + s;
                    }
                    if (!s.contains("/")) {
                        s = c.getResources().getString(R.string.mainfoldername) + "/" + s;
                    }
                    Log.d("ExportPreparer","s="+s);
                    SQLite thisSong = sqLiteHelper.getSong(c,s);
                    Uri pdfuri = makePDF.createPDF(c,preferences,storageAccess,processSong,thisSong);
                    uris.add(pdfuri);
                } else if (isPDF(s)) {
                    Uri pdfuri = storageAccess.getFileProviderUri(c, preferences, "Songs", "", FullscreenActivity.exportsetfilenames.get(q));
                    if (storageAccess.uriExists(c, pdfuri)) {
                        uris.add(pdfuri);
                    }
                }
            }
        }

        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return emailIntent;
    }

    Intent exportSong(Context c, Preferences preferences, Bitmap bmp, StorageAccess storageAccess, ProcessSong processSong, MakePDF makePDF, SQLiteHelper sqLiteHelper) {
        // Prepare the appropriate attachments
        String emailcontent = "";
        Uri text = null;
        Uri ost = null;
        Uri desktop = null;
        Uri chopro = null;
        Uri onsong = null;
        Uri image = null;
        Uri pdf = null;
        // IV - Moved pdfbmp into relevant PDF section

        // Prepare the song uri and input stream
        Uri uriinput = storageAccess.getUriForItem(c, preferences, "Songs", StaticVariables.whichSongFolder,
                StaticVariables.songfilename);
        InputStream inputStream;

        // Prepare a txt version of the song.
        String exportText_String = prepareTextFile(c,preferences, processSong);

        emailcontent += exportText_String;
        if (preferences.getMyPreferenceBoolean(c,"exportText",true)) {
            text = storageAccess.getUriForItem(c, preferences, "Export", "",
                    storageAccess.safeFilename(StaticVariables.songfilename)+".txt");

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, text, null, "Export", "", storageAccess.safeFilename(StaticVariables.songfilename) + ".txt");

            OutputStream outputStream = storageAccess.getOutputStream(c,text);
            storageAccess.writeFileFromString(exportText_String,outputStream);
        }

        if (preferences.getMyPreferenceBoolean(c,"exportOpenSongApp",true)) {
            // Prepare an ost version of the song.
            ost = storageAccess.getUriForItem(c, preferences, "Export", "",
                    storageAccess.safeFilename(StaticVariables.songfilename)+".ost");

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, ost, null, "Export", "", storageAccess.safeFilename(StaticVariables.songfilename) + ".ost");

            inputStream = storageAccess.getInputStream(c, uriinput);
            OutputStream outputStream = storageAccess.getOutputStream(c,ost);

            storageAccess.copyFile(inputStream,outputStream);
        }

        if (preferences.getMyPreferenceBoolean(c,"exportDesktop",false)) {
            // Prepare a desktop version of the song.
            desktop = storageAccess.getUriForItem(c, preferences, "Export", "",
                    storageAccess.safeFilename(StaticVariables.songfilename));

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, desktop, null, "Export", "", storageAccess.safeFilename(StaticVariables.songfilename));

            inputStream = storageAccess.getInputStream(c, uriinput);
            OutputStream outputStream = storageAccess.getOutputStream(c,desktop);
            storageAccess.copyFile(inputStream,outputStream);
        }

        if (preferences.getMyPreferenceBoolean(c,"exportChordPro",false)) {
            // Prepare a chordpro version of the song.
            String exportChordPro_String = prepareChordProFile(c,processSong);
            chopro = storageAccess.getUriForItem(c, preferences, "Export", "",
                    storageAccess.safeFilename(StaticVariables.songfilename)+".chopro");

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, chopro, null, "Export", "", storageAccess.safeFilename(StaticVariables.songfilename) + ".chopro");

            OutputStream outputStream = storageAccess.getOutputStream(c,chopro);
            storageAccess.writeFileFromString(exportChordPro_String,outputStream);
        }

        if (preferences.getMyPreferenceBoolean(c,"exportOnSong",false)) {
            // Prepare an onsong version of the song.
            String exportOnSong_String = prepareOnSongFile(c, processSong);
            onsong = storageAccess.getUriForItem(c, preferences, "Export", "",
                    storageAccess.safeFilename(StaticVariables.songfilename)+".onsong");

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, onsong, null, "Export", "", storageAccess.safeFilename(StaticVariables.songfilename) + ".onsong");

            OutputStream outputStream = storageAccess.getOutputStream(c,onsong);
            storageAccess.writeFileFromString(exportOnSong_String,outputStream);
        }

        // GE Improved PDF method (text based)
        // Uses SQLite, so song doesn't need to be displayed on the screen
        if (preferences.getMyPreferenceBoolean(c,"exportPDF",false)) {
            String songid;
            if (StaticVariables.whichSongFolder.equals("")) {
                songid = c.getResources().getString(R.string.mainfoldername) + "/" + StaticVariables.songfilename;
            } else {
                songid = StaticVariables.whichSongFolder + "/" + StaticVariables.songfilename;
            }

            // GE This won't wor for variations, notes, custom slides, etc.
            // That's because the aren't and shouldn't be in the database
            // These items are temporary files that are created when importing sets
            // They will be dealt with in the new material app as this deals with each song as an object
            SQLite thisSong = sqLiteHelper.getSong(c,songid);
            pdf = makePDF.createPDF(c,preferences,storageAccess,processSong,thisSong);
        }

        if (StaticVariables.whichMode.equals("Performance") &&
                preferences.getMyPreferenceBoolean(c,"exportImage",false)) {
            // Prepare an image/png version of the song.
            image = storageAccess.getUriForItem(c, preferences, "Export", "",
                    storageAccess.safeFilename(StaticVariables.songfilename)+".png");

            // Check the uri exists for the outputstream to be valid
            storageAccess.lollipopCreateFileForOutputStream(c, preferences, image, null, "Export", "", storageAccess.safeFilename(StaticVariables.songfilename) + ".png");

            OutputStream outputStream = storageAccess.getOutputStream(c,image);
            storageAccess.writeImage(outputStream, bmp);
        }

        Intent emailIntent = setEmailIntent(StaticVariables.songfilename, StaticVariables.songfilename,
                emailcontent);

        FullscreenActivity.emailtext = "";

        // Add the attachments to the intent and make them readable uris
        ArrayList<Uri> uris = addUris(ost,text,desktop,chopro,onsong,image,pdf);
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return emailIntent;
    }

    private Intent exportBackup(Context c, Uri uri) {
        Intent emailIntent = setEmailIntent(c.getString(R.string.backup_info),c.getString(R.string.backup_info),
                c.getString(R.string.backup_info));
        FullscreenActivity.emailtext = "";
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(uri);
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return emailIntent;
    }

    Intent exportActivityLog(Context c, Preferences preferences, StorageAccess storageAccess) {
        String title = c.getString(R.string.app_name) + ": " + c.getString(R.string.edit_song_ccli);
        String subject = title + " - " + c.getString(R.string.ccli_view);
        String text = c.getString(R.string.ccli_church) + ": " +
                preferences.getMyPreferenceString(c,"ccliChurchName","") + "\n";
        text += c.getString(R.string.ccli_licence) + ": " +
                preferences.getMyPreferenceString(c,"ccliLicence","")+ "\n\n";
        Intent emailIntent = setEmailIntent(subject,title,text);

        // Add the attachments
        Uri uri = storageAccess.getUriForItem(c, preferences, "Settings", "", "ActivityLog.xml");
        ArrayList<Uri> uris = new ArrayList<>();
        if (!storageAccess.uriExists(c,uri)) {
            PopUpCCLIFragment.createBlankXML(c, preferences);
        }
        // Add the uri
        uris.add(uri);
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return emailIntent;
    }

    private Intent setEmailIntent(String subject, String title, String content) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TITLE, title);
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        return emailIntent;
    }

    private ArrayList<Uri> addUris(Uri ost, Uri text, Uri desktop, Uri chopro, Uri onsong, Uri image, Uri pdf) {
        ArrayList<Uri> uris = new ArrayList<>();
        if (ost != null) {
            uris.add(ost);
        }
        if (text != null) {
            uris.add(text);
        }
        if (desktop != null) {
            uris.add(desktop);
        }
        if (chopro != null) {
            uris.add(chopro);
        }
        if (onsong != null) {
            uris.add(onsong);
        }
        if (image != null) {
            uris.add(image);
        }
        if (pdf != null) {
            uris.add(pdf);
        }
        return uris;
    }

    private void setParser(Context c, Preferences preferences, StorageAccess storageAccess) {
        StringBuilder sb = new StringBuilder();

        FullscreenActivity.exportsetfilenames.clear();
        FullscreenActivity.exportsetfilenames_ost.clear();
        filesinset.clear();
        filesinset_ost.clear();

		// First up, load the set

        Uri seturi = storageAccess.getUriForItem(c, preferences, "Sets", "", StaticVariables.settoload);
        InputStream inputStream = storageAccess.getInputStream(c,seturi);
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(inputStream, "UTF-8");
            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("slide_group")) {
                        switch (xpp.getAttributeValue(null, "type")) {
                            case "song":
                            case "custom":
                                // Process a song/song variation, note or slide (others)
                                if (xpp.getAttributeValue(null, "name").contains("# " + c.getResources().getString(R.string.variation) + " # - ") || xpp.getAttributeValue(null, "type").equals("song")) {
                                    String fname;
                                    Uri songuri;
                                    String thisline;

                                    fname = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "name"));
                                    if (fname.contains("# Variation # - ")) {
                                        fname = fname.replace("# Variation # - ", "");
                                        songuri = storageAccess.getUriForItem(c, preferences, "Songs", "../Variations", fname);
                                    } else {
                                        songuri = storageAccess.getUriForItem(c, preferences, "Songs",
                                                LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "path")), fname);
                                    }

                                    // Ensure there is a folder '/'
                                    if (LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "path")).equals("")) {
                                        thisline = "/" + LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "name"));
                                    } else {
                                        thisline = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "path")) + LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "name"));
                                    }
                                    filesinset.add(thisline);
                                    filesinset_ost.add(thisline);

                                    // Set the default values exported with the text for the set
                                    song_title = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "name"));
                                    song_author = "";
                                    song_hymnnumber = "";
                                    song_key = "";
                                    // Now try to improve on this info
                                    if (storageAccess.uriExists(c, songuri) && !isImgOrPDF(fname)) {
                                        // Read in the song title, author, copyright, hymnnumber, key
                                        getSongData(c, songuri, storageAccess);
                                    }
                                    sb.append(song_title);
                                    if (!song_author.isEmpty()) {
                                        sb.append(", ").append(song_author);
                                    }
                                    if (!song_hymnnumber.isEmpty()) {
                                        sb.append(", #").append(song_hymnnumber);
                                    }
                                    if (!song_key.isEmpty()) {
                                        sb.append(" (").append(song_key).append(")");
                                    }
                                    sb.append("\n");
                                } else if (xpp.getAttributeValue(null, "name").contains("# " + c.getResources().getString(R.string.note) + " # - ")) {
                                    String nametemp = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "name"));
                                    nametemp = nametemp.replace("# " + c.getResources().getString(R.string.note) + " # - ", "");
                                    sb.append(nametemp).append("\n");
                                } else {
                                    sb.append(LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "name"))).append("\n");
                                }
                                break;
                            case "scripture":
                                sb.append(LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null,"name"))).append("\n");
                                break;
                            case "image":
                                // Go through the descriptions of each image and extract the absolute file locations
                                boolean allimagesdone = false;
                                ArrayList<String> theseimages = new ArrayList<>();
                                String imgname;
                                imgname = LoadXML.parseFromHTMLEntities(xpp.getAttributeValue(null, "name"));
                                while (!allimagesdone) { // Keep iterating unless the current eventType is the end of the document
                                    if (eventType == XmlPullParser.START_TAG) {
                                        if (xpp.getName().equals("description")) {
                                            xpp.next();
                                            theseimages.add(LoadXML.parseFromHTMLEntities(xpp.getText()));
                                            filesinset.add(LoadXML.parseFromHTMLEntities(xpp.getText()));
                                            filesinset_ost.add(LoadXML.parseFromHTMLEntities(xpp.getText()));
                                        }

                                    } else if (eventType == XmlPullParser.END_TAG) {
                                        if (xpp.getName().equals("slide_group")) {
                                            allimagesdone = true;
                                        }
                                    }

                                    eventType = xpp.next(); // Set the current event type from the return value of next()
                                }
                                // Go through each of these images and add a line for each one
                                sb.append(imgname).append("\n");
                                for (int im = 0; im < theseimages.size(); im++) {
                                    sb.append("     - ").append(theseimages.get(im)).append("\n");
                                }
                                break;
                        }
                    }
                }
                try {
                    eventType = xpp.next();
                } catch (Exception e) {
                    Log.d("ExportPreparer", "Error moving to the next tag");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send the settext back to the FullscreenActivity as emailtext
        FullscreenActivity.emailtext = sb.toString();
        FullscreenActivity.exportsetfilenames = filesinset;
        FullscreenActivity.exportsetfilenames_ost = filesinset_ost;
	}

	private void getSongData(Context c, Uri uri, StorageAccess storageAccess) {
		// Parse the song xml.
		// Grab the title, author, lyrics_withchords, lyrics_withoutchords, copyright, hymnnumber, key
		// Initialise all the xml tags a song should have that we want
		song_title = "";
		song_author = "";
        song_hymnnumber = "";
		song_key = "";


		// Get inputtream for the song
        InputStream inputStream = storageAccess.getInputStream(c,uri);

        try {
            XmlPullParserFactory factorySong = XmlPullParserFactory.newInstance();
            factorySong.setNamespaceAware(true);
            XmlPullParser xppSong = factorySong.newPullParser();
            xppSong.setInput(inputStream, "UTF-8");
            int eventType = xppSong.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    switch (xppSong.getName()) {
                        case "author":
                            song_author = LoadXML.parseFromHTMLEntities(xppSong.nextText());
                            break;
                        case "title":
                            song_title = LoadXML.parseFromHTMLEntities(xppSong.nextText());
                            break;
                        /*case "lyrics":
                            song_lyrics_chords = LoadXML.parseFromHTMLEntities(xppSong.nextText());
                            break;*/
                        case "hymn_number":
                            song_hymnnumber = LoadXML.parseFromHTMLEntities(xppSong.nextText());
                            break;
                        case "key":
                            song_key = LoadXML.parseFromHTMLEntities(xppSong.nextText());
                            break;
                    }
                }
                eventType = xppSong.next();
            }
            /*// Remove the chord lines from the song lyrics
            String[] templyrics = song_lyrics_chords.split("\n");

            // Only add the lines that don't start with a .
            int numlines = templyrics.length;
            if (numlines>0) {
                StringBuilder song_lyrics_withoutchords = new StringBuilder();
                for (String templyric : templyrics) {
                    if (!templyric.startsWith(".")) {
                        song_lyrics_withoutchords.append(templyric).append("\n");
                    }
                }
                song_lyrics = song_lyrics_withoutchords.toString();
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
	}

    void createSelectedOSB(Context c, Preferences preferences, String selected, StorageAccess storageAccess) {
        folderstoexport = selected;
        if (backup_create_selected!=null) {
            backup_create_selected.cancel(true);
        }
        backup_create_selected = new Backup_Create_Selected(c, preferences, storageAccess);
        backup_create_selected.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private String makeBackupZipSelected(Context c, Preferences preferences, StorageAccess storageAccess) {
        // Get the date for the file
        Calendar cal = Calendar.getInstance();
        System.out.println("Current time => " + cal.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd", StaticVariables.locale);
        String formattedDate = df.format(cal.getTime());
        String backup = "OpenSongBackup_" + formattedDate + ".osb";
        zipDirSelected(c, preferences, backup, storageAccess);
        return backup;
    }

    private void zipDirSelected(Context c, Preferences preferences, String zipFileName, StorageAccess storageAccess) {
        File tempbackup = new File(c.getExternalFilesDir("Backup"),zipFileName);
        //Uri uri = storageAccess.getUriForItem(c, preferences, "", "", zipFileName);
        //Uri uri = Uri.fromFile(tempbackup);
        // Check the uri exists for the outputstream to be valid
        //storageAccess.lollipopCreateFileForOutputStream(c, preferences, uri, null, "", "", zipFileName);

        try {
            FileOutputStream outputStream = new FileOutputStream(tempbackup);
            outSelected = new ZipOutputStream(outputStream);

            // Go through each of the selected folders and add them to the zip file
            String[] whichfolders = folderstoexport.split("__%%__");
            for (int i = 0; i < whichfolders.length; i++) {
                if (!whichfolders[i].equals("")) {
                    whichfolders[i] = whichfolders[i].replace("%__", "");
                    whichfolders[i] = whichfolders[i].replace("__%", "");
                    addDirSelected(c, preferences, whichfolders[i], storageAccess);
                }
            }
            outSelected.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //TODO not sure if subfolders (e.g. Band/Temp/Inner are added to the zipfile
    private void addDirSelected(Context c, Preferences preferences, String subfolder, StorageAccess storageAccess) {
        ArrayList<String> files = storageAccess.listFilesInFolder(c, preferences, "Songs", subfolder);
        byte[] tmpBuf = new byte[1024];
        for (String s:files) {
            Uri uri = storageAccess.getUriForItem(c, preferences, "Songs", subfolder, s);
            if (storageAccess.uriIsFile(c,uri)) {
                try {
                    InputStream inputStream = storageAccess.getInputStream(c, uri);
                    ZipEntry ze;
                    if (subfolder.equals(c.getString(R.string.mainfoldername)) || subfolder.equals("MAIN")) {
                        ze = new ZipEntry(s);
                    } else {
                        ze = new ZipEntry(subfolder + "/" + s);
                    }
                    outSelected.putNextEntry(ze);
                    int len;
                    while ((len = inputStream.read(tmpBuf)) > 0) {
                        outSelected.write(tmpBuf, 0, len);
                    }
                    outSelected.closeEntry();
                    inputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class Backup_Create_Selected extends AsyncTask<String, Void, String> {
        final Context c;
        Intent emailIntent;
        final StorageAccess storageAccess;
        final Preferences preferences;

        Backup_Create_Selected(Context context, Preferences p, StorageAccess sA) {
            c = context;
            storageAccess = sA;
            preferences = p;
        }

        @Override
        protected String doInBackground(String... strings) {
            return makeBackupZipSelected(c, preferences, storageAccess);
        }

        boolean cancelled = false;

        @Override
        protected void onCancelled() {
            cancelled = true;
        }

        @Override
        public void onPostExecute(String s) {
            if (!cancelled) {
                try {
                    //Uri uri = storageAccess.getUriForItem(c, preferences, "", "", s);
                    File tempbackup = new File(c.getExternalFilesDir("Backup"),s);
                    Uri uri = FileProvider.getUriForFile(c,"OpenSongAppFiles",tempbackup);

                    StaticVariables.myToastMessage = c.getString(R.string.backup_success);
                    ShowToast.showToast(c);
                    emailIntent = exportBackup(c, uri);
                    ((Activity) c).startActivityForResult(Intent.createChooser(emailIntent, c.getString(R.string.backup_info)), 12345);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String prepareChordProFile(Context c, ProcessSong processSong) {
        // This converts an OpenSong file into a ChordPro file
        StringBuilder s = new StringBuilder("{new_song}\n");
        String[] headersAndFooters = getHeadersAndFooters("choPro");
        s.append(headersAndFooters[0]);
        s.append("\n");

	    // Go through each song section and add the ChordPro formatted chords
        for (int f = 0; f< StaticVariables.songSections.length; f++) {
            // IV - Quick exit if extra information heading or Note
            if (!StaticVariables.songSections[f].startsWith(" B_") && !StaticVariables.songSections[f].startsWith(";__")) {
                s.append(processSong.songSectionChordPro(c, f, false));
            }
	    }

        s.append("\n");
        s.append(headersAndFooters[1]);

        return tidyReturnedString(s.toString(),true);
    }
    private String prepareOnSongFile(Context c, ProcessSong processSong) {
        // This converts an OpenSong file into a OnSong file
        StringBuilder s = new StringBuilder();
        String[] headersAndFooters = getHeadersAndFooters("onSong");
        s.append(headersAndFooters[0]);
        s.append("\n");
	
	    // Go through each song section and add the ChordPro formatted chords
        for (int f = 0; f< StaticVariables.songSections.length; f++) {
            // IV - Quick exit if Heading or Note
            if (!StaticVariables.songSections[f].startsWith(" B_") && !StaticVariables.songSections[f].startsWith(";__")) {
                s.append(processSong.songSectionChordPro(c, f, true));
            }
        }

        s.append("\n");
        s.append(headersAndFooters[1]);

        return tidyReturnedString(s.toString(),true);
    }
    private String prepareTextFile(Context c, Preferences preferences, ProcessSong processSong) {
        // This converts an OpenSong file into a text file
        StringBuilder s = new StringBuilder();
        String[] headersAndFooters = getHeadersAndFooters("textOnly");
        s.append(headersAndFooters[0]);
        s.append("\n");
	
	    // Go through each song section and add the text trimmed lines
        for (int f = 0; f< StaticVariables.songSections.length; f++) {
            // IV - Quick exit if Heading or Note
            if (!StaticVariables.songSections[f].startsWith(" B_") && !StaticVariables.songSections[f].startsWith(";__")) {
                // IV - Separate sections by a line breaks - to correctly layout txt output
                s.append(processSong.songSectionText(c, preferences, f)).append("\n\n");
            }
        }

        s.append("\n");
        s.append(headersAndFooters[1]);

        return tidyReturnedString(s.toString(),false);
    }

    private String[] getHeadersAndFooters(String type) {
        String[] starttag = new String[] {"", "", "Key:", "Tempo:", "Time:", "Copyright:", "CCLI: "};
        String endtag = "\n";
        StringBuilder header = new StringBuilder();
        StringBuilder footer = new StringBuilder();

        if (type.equals("choPro")) {
            starttag = new String[] {"{title: ", "{artist: ", "{key: ", "{tempo: ", "{time: ", "{copyright: ", "{ccli:"};
            endtag = "}\n";
        }

        header.append(getNonEmptyTag(starttag[0],StaticVariables.mTitle,endtag));
        header.append(getNonEmptyTag(starttag[1],StaticVariables.mAuthor,endtag));
        header.append(getNonEmptyTag(starttag[2],StaticVariables.mKey,endtag));
        header.append(getNonEmptyTag(starttag[3],StaticVariables.mTempo,endtag));
        header.append(getNonEmptyTag(starttag[4],StaticVariables.mTimeSig,endtag));
        footer.append(getNonEmptyTag(starttag[5],StaticVariables.mCopyright,endtag));
        footer.append(getNonEmptyTag(starttag[6],StaticVariables.mCCLI,endtag));

        return new String[] {header.toString(),footer.toString()};
    }

    private String getNonEmptyTag(String start, String content, String end) {
        if (content==null || content.isEmpty()) {
            return "";
        } else {
            return start + content + end;
        }
    }

    private String tidyReturnedString(String string, boolean comments) {
        if (comments) {
            // IV - remove empty comments
            string = string.replaceAll("\\Q{c:}\\E\n", "");
            string = string.replaceAll("\\Q{comment:}\\E\n", "");
            string = string.replaceAll("\\Q{comments:}\\E\n", "");
        }
        // IV - Replace multiple blank lines with a single blank line and remove empty items
        return string.replaceAll("\n\n\n", "\n\n");
    }

    private boolean isImgOrPDF(String s) {
        return isImg(s) || isPDF(s);
    }
    private boolean isImg(String s) {
        s = s.toLowerCase(StaticVariables.locale);
        return s.endsWith(".jpg") || s.endsWith(".jpeg") || s.endsWith(".gif") || s.endsWith(".png") || s.endsWith(".bmp");
    }
    private boolean isPDF(String s) {
        return s.toLowerCase(StaticVariables.locale).endsWith(".pdf");
    }
}
package com.garethevans.church.opensongtablet.export;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.util.ArrayList;

public class ExportActions {

    private final String TAG = "SetActions";

    public Intent setShareIntent(String content, String type, Uri uri, ArrayList<Uri> uris) {
        Intent intent = new Intent();
        if (uris==null || uris.size()==0) {
            intent.setAction(Intent.ACTION_SEND);
        } else {
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }

        if (content!=null) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }
        if (uri!=null) {
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (type!=null) {
            intent.setType(type);
        }
        return intent;

    }
    public Intent setIntent(String subject, String title, String content) {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        return intent;
    }

    public Intent exportBackup(Context c, Uri uri, String filename) {
        Intent intent = setIntent(c.getString(R.string.backup_info),filename, filename);
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(uri);
        String type = "*/*";
        if (filename.endsWith(".pdf")) {
            type = "application/pdf";
        } else if (filename.endsWith(".png") || filename.endsWith(".jpg")) {
            type = "image/*";
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.setType(type);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return intent;
    }

    public String[] getFolderAndFile(Context c, String songId) {
        String[] location = new String[2];
        if (songId.contains("/")) {
            location[0] = songId.substring(0,songId.lastIndexOf("/")).replace("/","_");
            location[1] = songId.replace(location[0],"").replace("/","");
        } else {
            location[0] = c.getString(R.string.mainfoldername);
            location[1] = songId;
        }
        return location;
    }

    public Uri getActualSongFile(Context c, MainActivityInterface mainActivityInterface,
                                 String subfolder, String filename) {
        return mainActivityInterface.getStorageAccess().getUriForItem(c,
                mainActivityInterface, "Songs", subfolder, filename);
    }
    public Uri getActualSongFile(Context c, MainActivityInterface mainActivityInterface, Song song) {
        return mainActivityInterface.getStorageAccess().getUriForItem(c,
                mainActivityInterface, "Songs",
                song.getFolder(), song.getFilename());
    }
    public Uri getActualSetFile(Context c, MainActivityInterface mainActivityInterface, String filename) {
        return mainActivityInterface.getStorageAccess().getUriForItem(c,
                mainActivityInterface, "Sets","", filename);
    }

    public ArrayList<String> getListOfSets(String setToExport) {
        String[] bits = setToExport.split("%_%");
        ArrayList<String> setNames = new ArrayList<>();
        for (String bit : bits) {
            if (!bit.isEmpty()) {
                setNames.add(bit);
            }
        }
        return setNames;
    }

    public ArrayList<Uri> addOpenSongAppSetsToUris(Context c, MainActivityInterface mainActivityInterface,
                                                   ArrayList<String> setNames) {
        ArrayList<Uri> extraUris = new ArrayList<>();
        for (String setName : setNames) {
            extraUris.add(mainActivityInterface.getStorageAccess().copyFromTo(c,
                    mainActivityInterface, "Sets", "", setName,
                    "Export", "", setName + ".osts"));
        }
        return extraUris;
    }

    public ArrayList<Uri> addOpenSongSetsToUris(Context c, MainActivityInterface mainActivityInterface,
                                                ArrayList<String> setNames) {
        ArrayList<Uri> extraUris = new ArrayList<>();
        for (String setName : setNames) {
            extraUris.add(mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface,
                    "Sets", "", setName));
        }
        return extraUris;
    }


    public String[] parseSets(Context c, MainActivityInterface mainActivityInterface, ArrayList<String> setNames) {
        String[] setData = new String[2];
        setData[0] = ""; // The ids of any songs
        setData[1] = ""; // A text line for display/email/etc.

        for (String setName:setNames) {
            String[] thisSet = setParser(c,mainActivityInterface,setName);
            if (!thisSet[0].trim().isEmpty()) {
                setData[0] = setData[0] + thisSet[0].trim() + "\n";
            }
            if (!thisSet[1].trim().isEmpty()) {
                setData[1] = setData[1] + thisSet[1].trim() + "\n";
            }
        }

        // Trim
        setData[0] = setData[0].trim();
        setData[1] = setData[1].trim();
        return setData;
    }

    private String[] setParser(Context c, MainActivityInterface mainActivityInterface, String setName) {
        // bits[0] will be the song ids split by new line
        // bits[1] will be a text version of the set list
        String[] bits = new String[2];
        StringBuilder stringBuilderIDs = new StringBuilder();
        StringBuilder stringBuilderSet = new StringBuilder();

        // First up, load the set
        Uri setUri = mainActivityInterface.getStorageAccess().getUriForItem(c,mainActivityInterface,"Sets","",setName);
        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            String utf = mainActivityInterface.getStorageAccess().getUTFEncoding(c, setUri);
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(c, setUri);
            if (inputStream != null) {
                xpp.setInput(inputStream, utf);
                int eventType;
                eventType = xpp.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.getName().equals("slide_group")) {
                            // Look for the type attribute and see what type of slide it is
                            switch (xpp.getAttributeValue(null, "type")) {
                                case "song":
                                case "custom":
                                    // Process a song/song variation, note or slide (others)
                                    if (xpp.getAttributeValue(null, "name").contains("# " +
                                            c.getResources().getString(R.string.variation) + " # - ") ||
                                            xpp.getAttributeValue(null, "name").contains("# " +
                                                    c.getResources().getString(R.string.note) + " # - ") ||
                                            xpp.getAttributeValue(null, "type").equals("song")){
                                        String folder;
                                        String filename = stripSlashes(mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null, "name")));
                                        String title = "";
                                        String key = "";
                                        String author = "";
                                        String hymn = "";
                                        String ccli = "";
                                        String custom = "";
                                        String id;

                                        if (filename.contains("# " + c.getString(R.string.variation) + " # - ")) {
                                            filename = filename.replace("# " + c.getString(R.string.variation) + " # - ", "");
                                            id = "../Variations/" + filename;
                                            custom = c.getString(R.string.variation);

                                        } else if (filename.contains("# " + c.getResources().getString(R.string.note) + " # - ")) {
                                            filename = filename.replace("# " + c.getResources().getString(R.string.note) + " # - ", "");
                                            id = "../Notes/" + filename;
                                            custom = c.getString(R.string.note);

                                        } else {
                                            // This is a song, which should be in the database
                                            folder = stripSlashes(mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null, "path")));
                                            if (xpp.getAttributeCount() > 2) {
                                                // Assume a key has been set as well
                                                key = xpp.getAttributeValue("", "prefKey");
                                            }
                                            if (folder.isEmpty()) {
                                                folder = c.getString(R.string.mainfoldername);
                                            }
                                            Song thisSong = mainActivityInterface.getSQLiteHelper().getSpecificSong(c, mainActivityInterface, folder, filename);
                                            if (key == null) {
                                                // Not stored in the set, so look for the song value
                                                key = thisSong.getKey();
                                            }
                                            title = thisSong.getTitle();
                                            author = thisSong.getAuthor();
                                            hymn = thisSong.getHymnnum();
                                            ccli = thisSong.getCcli();
                                            id = thisSong.getSongid();
                                        }

                                        key = fixNull(key);
                                        if (!key.isEmpty()) {
                                            key = " (" + key + ")";
                                        }

                                        author = fixNull(author);
                                        if (!author.isEmpty()) {
                                            author = "¬ " + author;
                                        }

                                        hymn = fixNull(hymn);
                                        if (!hymn.isEmpty()) {
                                            hymn = "¬ #" + hymn;
                                        }

                                        ccli = fixNull(ccli);
                                        if (!ccli.isEmpty() && mainActivityInterface.getPreferences().getMyPreferenceBoolean(c,"ccliAutomaticLogging",false)) {
                                            ccli = "¬ CCLI Song #" + ccli;
                                        }

                                        title = fixNull(title);
                                        if (title.isEmpty()) {
                                            title = filename;
                                        }

                                        if (!custom.isEmpty()) {
                                            custom = " (" + custom + ")";
                                        }
                                        String bittoadd = title + custom + author + hymn + ccli + key + "\n";

                                        // IV - , (comma) is the delimiter so use within content is replaced with " |" and the the temporary delimeter ¬ replaced with ,
                                        stringBuilderSet.append(bittoadd.replace(","," |").replace("¬", ","));
                                        stringBuilderIDs.append(id).append("\n");
                                    }
                                    break;
                                case "scripture":
                                    // Get Scripture
                                    boolean scripture_finished = false;
                                    String scripture_title = "";
                                    String scripture_translation = "";
                                    while (!scripture_finished) {
                                        switch (xpp.getName()) {
                                            case "title":
                                                scripture_title = safeNextText(xpp);
                                                break;
                                            case "subtitle":
                                                scripture_translation = mainActivityInterface.getProcessSong().parseHTML(safeNextText(xpp));
                                                break;
                                        }

                                        xpp.nextTag();

                                        if (xpp.getEventType()==XmlPullParser.END_TAG) {
                                            if (xpp.getName().equals("slides")) {
                                                scripture_finished = true;
                                            }
                                        }
                                    }
                                    if (!scripture_translation.isEmpty()) {
                                        scripture_translation = " (" + scripture_translation + ")";
                                    }

                                    stringBuilderSet.append(scripture_title).append(scripture_translation).append("\n");
                                    break;

                                case "image":
                                    // Get the Image(s)
                                    String filename = stripSlashes(mainActivityInterface.getProcessSong().parseHTML(xpp.getAttributeValue(null, "name")));
                                    stringBuilderSet.append(filename).append("\n");
                                    break;
                            }
                        }
                    }
                    eventType = xpp.next();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        bits[0] = stringBuilderIDs.toString();
        bits[1] = stringBuilderSet.toString();

        return bits;
    }

    private String stripSlashes(String string) {
        if (string.startsWith("/")) {
            string = string.replaceFirst("/", "");
        }
        if (string.endsWith("/")) {
            string = string.substring(0, string.lastIndexOf("/"));
        }
        return string;
    }

    private String fixNull(String string) {
        if (string==null) {
            return "";
        } else {
            return string;
        }
    }

    private String safeNextText(XmlPullParser xpp) {
        try {
            if (!xpp.isEmptyElementTag()) {
                String result = xpp.nextText();
                if (xpp.getEventType() != XmlPullParser.END_TAG) {
                    xpp.nextTag();
                }
                return result;
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }
    }
}

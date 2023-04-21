package com.garethevans.church.opensongtablet.importsongs;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chordinator {

    // Chord extraction kindly provided by Paul Evans
    // Used in Chordinator Augmented App: https://github.com/pkbevans/Chordinator
    // This file is mostly taken from /search/SearchActivity.java
    // It also pulls some functions form SongUtils.java and /conversion/SongConverter.java
    // Used by permission

    private final String TAG = "Chordinator";
    private String foundSongText;
    public final int MAX_LINES_TO_CHECK = 40;
    private String artist="", title="";
    private String songText;

    // Chord Reader stuff - START
    private final Pattern textAreaPattern = Pattern.compile(
            "(" + "<\\s*textarea.*?>.*?<\\s*/textarea\\s*>" + ")",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // html tag or html escaped character

    private final Pattern htmlObjectPattern = Pattern.compile(
            "(" + "<\\s*style.*?>.*?<\\s*/style\\s*>" + // style span
                    "|" + // OR
                    "<\\s*script.*?>.*?<\\s*/script\\s*>" + // script span
                    "|" + // OR
                    "<\\s*head.*?>.*?<\\s*/head\\s*>" + // head span
                    "|" + // OR
                    "<[^>]++>" + // html tag, such as '<br/>' or '<a href="www.google.com">'
                    "|" + // OR
                    "&[^; \n\t]++;" + // escaped html character, such as '&amp;' or '&#0233;'
                    ")", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // HTML newline tag, such as '<p>' or '<br/>'
    private final Pattern htmlNewlinePattern = Pattern.compile(
            "<(?:p|br)\\s*+(?:/\\s*+)?>", Pattern.CASE_INSENSITIVE);

    private final Pattern prePattern = Pattern.compile("<pre[^>]*>(.*?)</pre>",
            Pattern.DOTALL|Pattern.CASE_INSENSITIVE);

    private final Pattern multipleNewlinePattern = Pattern.compile("([ \t\r]*\n[\t\r ]*) {2,}");
    // Chord Reader stuff - END


    // Receive the string for processing from the ImportOnline Fragment and process it
    public void processHTML(ImportOnlineFragment importOnlineFragment,
                            MainActivityInterface mainActivityInterface, String content) {
        final String html = "<head>"+content+"</head>";
        if (content.length()>20) {
            Log.d(TAG, "HELLO got [" + content.substring(0, 10) + "..." + content.substring(content.length() - 10));
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            // Look for start of song (i.e. {title:
            int start;
            int end;
            // See if there is a Chopro song in there
            String textArea = findTextArea(html);
            if (!textArea.isEmpty() && (start=findChoproStart(textArea))>-1) {
                songText = textArea.substring(start);
                //Log.d(TAG, "HELLO searchPage2["+songText+"]");
                // if start found then find end
                if ((end=findChoproEnd(songText))>-1) {
                    // clean up
                    foundSongText = songText.substring(0, end);
                } else {
                    // Assume its all part of the song if no end is found
                    foundSongText = songText;
                }
                //Log.d(TAG, "HELLO searchPage got[" + foundSongText + "]");
            } else {
                // If not see if there is a UG-style song
                // Extract the contents of the <pre> </pre> tag if there is one
                foundSongText = findUGSong(content);
            }

            Log.d(TAG,"foundSongText:"+foundSongText);
            Log.d(TAG,"songText:"+songText);

            // Look for the title and artist
            if (foundSongText!=null) {
                title = tagValue(foundSongText, "title", "t", title);
                artist = tagValue(foundSongText, "subtitle", "st", artist);
                foundSongText = cleanSong(foundSongText);
            }

            if (foundSongText!=null && !foundSongText.isEmpty()) {
                mainActivityInterface.chordinatorResult(importOnlineFragment,foundSongText);
            }
        });
    }

    public void setTitle(String title) {
        this.title = title;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }
    public String getTitle() {
        return title;
    }
    public String getArtist() {
        return artist;
    }

    private String findTextArea(String html) {
        Matcher matcher = textAreaPattern.matcher(html);

        if (matcher.find()) {
            //Log.d(TAG, "HELLO found <textArea> tag"+matcher.group());
            return matcher.group();
        } else {
            return "";
        }
    }

    private int findChoproStart(String line) {
        String [] startTags = {"{title:", "{t:"};// MUST BE LOWERCASE
        String [] badTags = {"{t:{start:","{t: t, o:"};
        int ret=-1;
        int index=0;
        for (String tag: startTags) {
            index=0;
            while ((ret=line.substring(index).toLowerCase().indexOf(tag))>-1) {
                int ret2=-1;
                // Found a possible start tag, but need to check its not one of the known BAD tags
                for (String badTag: badTags) {
                    if ((ret2=line.substring(index).substring(ret, ret+20).toLowerCase().indexOf(badTag))>-1) {
                        //Log.d(TAG, "HELLO found BAD tag: ["+badTag+"] index=["+ret2+"]");
                        index = ret+1; // Continue search for a matching tag
                        break;
                    }
                }
                if (ret2 == -1) {
                    //Log.d(TAG, "HELLO findSongStart: ["+tag+"] index=["+ret+"]");
                    break;
                }
            }
        }
        return index+ret;
    }

    private int findChoproEnd(String line) {
        // IF any of the "end" tags are found then we have found the end
        String [] endTags={"\"<", "<a href=","</textarea>","</form>", "<input", "</pre>","</body>"};// MUST BE LOWERCASE
        int ret=-1;
        int index;
        for (String tag: endTags) {
            if ((index=line.toLowerCase().indexOf(tag))>-1) {
                if (ret == -1 || index < ret) {
                    ret = index;
                }
            }
        }
        if (ret>-1) {
            Log.d(TAG,"findSongEnd["+ret+"]");
        }
        return ret;
    }

    private String findUGSong(String html) {
        //Log.d(TAG, "HELLO findUGSong");
        Matcher matcher = prePattern.matcher(html);

        while (matcher.find()) {
            //Log.d(TAG, "HELLO findUGSong found <pre> tag");
            String preHtml = matcher.group(1);
            //Log.d(TAG, "HELLO preHTML=["+preHtml.substring(0, preHtml.length()>100?100:preHtml.length())+"]");//2.4.0 Bug fix - remove hard coded 100
            String preTxt = convertHtmlToText(preHtml);
            //Log.d(TAG, "HELLO: preTxt=["+preTxt.substring(0, preTxt.length()>100?100:preTxt.length())+"]");//2.4.0 Bug fix - remove hard coded 100
            if (containsChordLines(preTxt)) {
                return cleanUpText(preTxt);
            }
        }
        return null;
    }

    private String cleanUpText(String text) {
        if (text == null) {
            return null;
        }
        text = text.trim();

        // get rid of \r
        text = text.replaceAll("\r", "");

        // replace multiple newlines with just two newlines
        text = multipleNewlinePattern.matcher(text).replaceAll("\n\n");

        return text;
    }

    private String cleanSong(String songText) {
        if (songText!=null && !songText.isEmpty()) {
            //Log.d(TAG, "HELLO cleanSong- IN:"+songText);
            String[][] reps = {
                    {".*\\{title:", "{title:"},
                    {"\\\\'", "'"},
                    {"&nbsp;", " "},
                    {"&amp;nbsp;", " "},
                    {"&lt;", "<"},
                    {"&gt;", ">"},
                    {"\">", ""},
                    {"&quot;", "\""},
                    {"<a href.*", ""}
            };
            for (String[] rep : reps) {
                songText = songText.replaceAll(rep[0], rep[1]);
            }
            //Log.d(TAG, "HELLO cleanSong-OUT:" + songText);
        }
        return songText;
    }

    private String convertHtmlToText(String htmlText) {
        StringBuilder plainText = new StringBuilder();
        // replace HTML tags with spaces and unescape HTML characters
        Matcher matcher = htmlObjectPattern.matcher(htmlText);
        int searchIndex = 0;
        while (matcher.find(searchIndex)) {
            int start = matcher.start();
            int end = matcher.end();
            String htmlObject = matcher.group();

            String replacementString;
            if (htmlText.charAt(start) == '&') { // html escaped character
                if (htmlObject.equalsIgnoreCase("&nbsp;")) {
                    replacementString = " "; // apache replaces nbsp with unicode \xc2\xa0, but we prefer just " "
                } else {
                    replacementString = Html.fromHtml(htmlObject).toString();
                }

                if (TextUtils.isEmpty(replacementString)) { // ensure non-empty - otherwise offsets would be screwed up
                    //Log.d(TAG, "Warning: couldn't escape html string: '" + htmlObject + "'");
                    replacementString = " ";
                }
            } else if (htmlNewlinePattern.matcher(htmlObject).matches()) { // newline tag
                if (htmlObject.toLowerCase().contains("p")) { // paragraph break
                    replacementString = "\n\n";
                } else { // 'br' (carriage return)
                    replacementString = "\n";
                }
            } else { // html tag or <style>/<script> span
                replacementString = "";
            }
            plainText.append(htmlText.substring(searchIndex, start));
            plainText.append(replacementString);
            searchIndex = end;
        }
        plainText.append(htmlText.substring(searchIndex));
        return plainText.toString();
    }

    private boolean containsChordLines(String inputText) {
        // Read thru each line and try and work out if it contains any definite chord lines
        // First split the text up into lines (an array of Strings)..
        String[] songLine2 = inputText.split("\n");
        int i = 0;
        for (String xx : songLine2) {
            if (isChordLine(xx)) {
                //Log.d(TAG, "HELLO FOUND CHORD LINE: " + xx);
                return true;
            }
            if (++i > MAX_LINES_TO_CHECK) {
                return false;
            }

        }
        return false;
    }

    private boolean isChordLine(String songLine) {
        // Split the line into items based on whitespace
        String[] item = songLine.split("\\s");
        // If we find 5 in a row that look like chords then assume
        // the whole line contains chords. Likewise if we find 2 non-blank items
        // that are not chords then its probably not a chord line.
        int chordCount = 0;
        for (int y = 0; chordCount > -2 && chordCount < 5 && y < item.length; y++) {
            if (item[y].length() == 0) {
                // Ignore
                Log.d(TAG,"ignore this line");
            } else if (isChord(item[y])) {
                //Log.d(TAG, "Item " + y + ": [" + item[y] + "] - Chord");
                ++chordCount;
            } else {
                //Log.d(TAG, "Item " + y + ": [" + item[y] + "] - NOT Chord");
                --chordCount;
            }
        }
        return (chordCount > 0);

    }

    private boolean isChord(String text) {
        boolean ischord = true;
        int arbitaryNumber = 9;    // Maximum length of a chord

        // Assume any long items are not chords
        if (text.length() > arbitaryNumber) {
            ischord = false;
        }

        // Ignore case
        String item = text.toUpperCase();
        char[] x = item.toCharArray();
        for (int i = 0; ischord && i < x.length; i++) {
            switch (i) {
                case 0:// 1st char must be C-B
                    if (!isCharIn(x[i], "CDEFGAB")) {
                        // Not a chord so break and return false
                        ischord = false;
                    }
                    break;
                case 1: // 2nd char must be .....
                    if (!isCharIn(x[i], "ABDMS1579/#")) {
                        // Not a chord so break and return false
                        ischord = false;
                    }
                    break;
                default:    // Once we've got here look for no-nos
                    if (isCharIn(x[i], "HIKLNPQRTVWXYZ,.<>?;':@~[]{}=_*&^%$£\"!|\\")) {
                        // Not a chord so break and return false
                        ischord = false;
                    }
                    break;
            }
        }

        return ischord;
    }

    private boolean isCharIn(char x, String y) {
        boolean ret = false;
        for (int i = 0; i < y.length(); i++) {
            if (x == y.charAt(i)) {
                ret = true;
                break;
            }
        }
        return ret;
    }

    private String tagValue(String text, String tag) {
        int x, y;
        String myTag = "{"+tag+":";
        String ret = "";
        if( (x = text.indexOf(myTag)+myTag.length())>=myTag.length() &&
                (y = text.indexOf('}', x)) >=0 ){
            ret = text.substring(x, y);
            //				Log.d(TAG,tag + "=[" + ret + "]");
        }
        return ret.trim();
    }
    private String tagValue( String text, String tag1, String tag2, String defaultRet  ) {
        // Log.d(TAG, "HELLO - tagvalue ["+text+"]["+tag1+"]["+tag2+"]");
        // Try get tag1. If not found try tag2, if this isn't found return default
        String ret=tagValue(text, tag1);
        if(ret.equals(""))
        {
            ret=tagValue(text, tag2);
            if(ret.equals(""))
            {
                ret = defaultRet;
            }
        }
        //		Log.d(TAG,tag1 + "/" + tag2 + "=[" + ret + "]");
        return ret;
    }


}

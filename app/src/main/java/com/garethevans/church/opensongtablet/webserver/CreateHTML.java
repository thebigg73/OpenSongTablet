package com.garethevans.church.opensongtablet.webserver;

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songprocessing.SongId;

import java.util.ArrayList;

public class CreateHTML {

    // Each time the user sends a request, we also receive their preferences to put back into the HTML page
    // Their preference is showChords and showHostSong

    // The host running OpenSongApp determines the following variables:
    // - allowWebNavigation: Boolean - This decides if they can use the song/set menus
    // - ipAddress: String - The server web address (and port) Ktor is running on
    // - minSize: Boolean - If the song is already at its minimum scale size
    // -

    private static final String TAG = "CreateHTML";

    /**
     * Generates the full HTML for a song to be served to web clients.
     */

    public static String getSplashHTML(Context context, Song song, String ipAddress) {
        MainActivityInterface mainActivityInterface = (MainActivityInterface) context;

        StringBuilder html = new StringBuilder();
        html.append(getCommonHTMLTop(mainActivityInterface,song, ipAddress, false, false, false, mainActivityInterface.getWebServer().getPreviousAndNextSongForArrows(song)));
        html.append("<div id=\"content\" style=\"width:fit-content; transform-origin: top left;\">\n");

        // Now add the splash image
        Uri splashUri = mainActivityInterface.getStorageAccess().getUriForItem("Backgrounds","","OpenSongApp_Logo.png");
        html.append(mainActivityInterface.getWebServer().getEmbeddedImageString(context,splashUri));

        // Finish with the common bottom bit
        html.append("</div>\n");

        // Add the reload trigger
        html.append("<script>\nfunction delayer(){\n");
        html.append("  window.location = \"")
                .append("http://")
                .append(ipAddress)
                .append(":8080/")
                .append(mainActivityInterface.getWebServer().getHostSongString())
                .append("/\";\n");
        html.append("}\nsetTimeout('delayer()', 2000);\n</script>\n");
        html.append("</body>\n");
        html.append("</html>");
        return html.toString();
    }

    public static String getSongHTML(Context context, Song song, String ipAddress,
                                     boolean allowWebNavigation, boolean showChords,
                                     ArrayList<SongId>prevAndNext) {

        if (song == null || context==null) {
            return "<html><body><h1>No song selected</h1></body></html>";
        }

        MainActivityInterface mainActivityInterface = (MainActivityInterface) context;

        StringBuilder html = new StringBuilder();
        html.append(getCommonHTMLTop(mainActivityInterface,song,ipAddress,allowWebNavigation,showChords,false, prevAndNext));

        // Now we add the menu bar for the songs
        html.append(getMenuBarHTML(context, mainActivityInterface, song,allowWebNavigation, !allowWebNavigation,false,false));
        html.append("<div id=\"content\" style=\"width:fit-content; transform-origin: top left;\">\n");

        // Check that the song has been processed into sections already
        if (song.getPresoOrderSongSections().isEmpty()) {
            mainActivityInterface.getProcessSong().processSongIntoSections(song,false);
        }

        // Now get the song
        String imgPDFSong = getImgPDFSong(context, mainActivityInterface, song);
        html.append(imgPDFSong);
        html.append(getSongContent(mainActivityInterface,song, imgPDFSong));

        // Finish with the common bottom bit
        html.append("</div>\n</body>\n");
        html.append("</html>");
        return html.toString();
    }

    public static String getSongMenuHTML(Context context, Song song, String ipAddress,
                                     boolean allowWebNavigation, ArrayList<SongId> prevAndNext) {
        // Prepares a list of song for the user
        MainActivityInterface mainActivityInterface = (MainActivityInterface) context;

        StringBuilder html = new StringBuilder();
        html.append(getCommonHTMLTop(mainActivityInterface,song,ipAddress,allowWebNavigation,true,true, prevAndNext));

        // Now we add the menu bar for the songs
        html.append(getMenuBarHTML(context, mainActivityInterface, song, allowWebNavigation, true, true, false));

        // Now get a list of all the host's songs (folder, filename, author, key)
        html.append("<div id=\"content\" style=\"width:fit-content; transform-origin: top left;\">\n");
        ArrayList<SongId> songIds = mainActivityInterface.getSQLiteHelper().getSongIds();

        for (int x = 0; x < songIds.size(); x++) {
            SongId songId = songIds.get(x);
            html.append("<div class=\"");
            html.append(songId.getFolder());
            html.append(" item clickable\" onclick=\"javascript:goToSong('").append(songId.getFolder().replace("'","\\'").replace("\"","\\")).append("','").append(songId.getFilename().replace("'","\\'").replace("\"","\\")).append("')\">");
            html.append(songId.getFilename());
            if (songId.getKey()!=null && !songId.getKey().isEmpty()) {
                html.append(" (");
                html.append(songId.getKey());
                html.append(")");
            }
            if ((songId.getFolder()!=null && !songId.getFolder().isEmpty()) || (songId.getAuthor()!=null && !songId.getAuthor().isEmpty())) {
                html.append("<br><div class=\"itemSub\">");
                if (songId.getFolder()!=null && !songId.getFolder().isEmpty()) {
                    html.append("(");
                    html.append(songId.getFolder());
                    html.append(") ");
                }
                if (songId.getAuthor()!=null && !songId.getAuthor().isEmpty()) {
                    html.append(songId.getAuthor());
                }
                html.append("</div>");
            }
            html.append("<hr/></div>\n");
        }

        // Finish with the common bottom bit
        html.append("</div>\n</body>\n");
        html.append("</html>");
        return html.toString();
    }

    public static String getSetMenuHTML(Context context, Song song, String ipAddress,
                                        boolean allowWebNavigation, ArrayList<SongId> prevAndNext) {
        // Prepares the current setlist for the user
        MainActivityInterface mainActivityInterface = (MainActivityInterface) context;

        StringBuilder html = new StringBuilder();
        html.append(getCommonHTMLTop(mainActivityInterface,song,ipAddress,allowWebNavigation,true,false, prevAndNext));

        // Now we add the menu bar for the songs
        html.append(getMenuBarHTML(context, mainActivityInterface, song, allowWebNavigation, true, false, true));

        // Now get a list of all the host's set items
        html.append("<div id=\"content\" style=\"width:fit-content; transform-origin: top left;\">\n");
        ArrayList<SetItemInfo> setItemInfos = mainActivityInterface.getCurrentSet().getSetItemInfos();

        for (int x = 0; x < setItemInfos.size(); x++) {
            SetItemInfo setItemInfo = setItemInfos.get(x);
            html.append("<div class=\"item clickable\" onclick=\"javascript:goToSong('").append(setItemInfo.songfolder.replace("'","\\'").replace("\"","\\")).append("','").append(setItemInfo.songfilename.replace("'","\\'").replace("\"","\\")).append("')\">");
            html.append(x+1);
            html.append(". ");
            html.append(setItemInfo.songfilename);
            if (setItemInfo.songkey != null && !setItemInfo.songkey.isEmpty()) {
                html.append(" (");
                html.append(setItemInfo.songkey);
                html.append(")");
            }
            if (setItemInfo.songfolder != null && !setItemInfo.songfolder.isEmpty()) {
                html.append("<br><div class=\"itemSub\">");
                html.append("(");
                html.append(setItemInfo.songfolder);
                html.append(")");
                html.append("</div>");
            }
            html.append("<hr/></div>\n");
        }

        // Finish with the common bottom bit
        html.append("</div>\n</body>\n");
        html.append("</html>");
        return html.toString();
    }



    private static String getSongFolderChooser(Context c, MainActivityInterface mainActivityInterface, Song song) {
        StringBuilder returnString = new StringBuilder();
        returnString.append("<label for=\"folderChooser\" style=\"background-color:");
        returnString.append(String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor())));
        returnString.append("\">");
        returnString.append(c.getString(R.string.folder));
        returnString.append("</label>\n");
        returnString.append("<select class=\"folderChooser\" id=\"folderChooser\" name=\"folderChooser\" onchange=\"javascript:filterByFolder()\">\n");

        ArrayList<String> songFolders = mainActivityInterface.getSQLiteHelper().getFolders();
        for (String folder : songFolders) {
            returnString.append("<option value=\"");
            returnString.append(folder);
            if (folder.equals(song.getFolder())) {
                returnString.append("\" selected>");
            } else {
                returnString.append("\">");
            }
            returnString.append(folder);
            returnString.append("</option>\n");
        }

        returnString.append("</select>\n");
        return returnString.toString();
    }

    private static String getCommonHTMLTop(MainActivityInterface mainActivityInterface, Song song,
                                           String ip, boolean allowWebNavigation,
                                           boolean isMenu, boolean isSongMenu, ArrayList<SongId> prevAndNext) {
        return "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n<title>OpenSongApp</title>\n" +
                "<style>\n" +
                getHTMLFontImports(mainActivityInterface) +
                "#content {\n" +
                "    display: inline-block;\n" +
                "    position: absolute;\n" +
                "    top: 50px; /* Offset to start below the fixed menu */\n" +
                "    left: 0;\n" +
                "    transform-origin: top left;\n" +
                "    padding-bottom: 100px;\n" +
                "}\n" +
                "body {\n" +
                "    margin: 0;\n" +
                "    padding: 0;\n" +
                "    overflow-x: hidden; /* Prevent the 'wiggle' during scaling */\n" +
                "}\n" +
                ".clickable,a             {-webkit-user-select: none; /* Safari */\n" +
                "                          -ms-user-select: none;      /* IE 10 and 11 */\n" +
                "                          user-select: none;          /* Standard syntax */" +
                "                          cursor: pointer;}\n" +
                ".page                    {color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) +
                "; background-color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor())) +
                ";}\n" +
                ".lyrictable              {border-spacing:0; border-collapse: collapse; border:0px;}\n" +
                getMenuBarCSS(mainActivityInterface, allowWebNavigation) +
                ".folderChooser           {width: fit-content; margin:4px; border-collapse: collapse; background-color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor())) +
                "; font-size:12pt}\n" +
                ".itemSub                 {font-size: 60%; font-style:italic; display:block; border-collapse:collapse; color:"+
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) +
                "; opacity:75%;}\n" +
                "hr                       {width: 100%; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) +
                " margin: 10px auto;}\n" +
                "body,select,option    {width:100%; font-family:"+mainActivityInterface.getMyFonts().getLyricFontName()+", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) +
                "; background-color:"+
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor())) +
                ";}\n" +
                "</style>\n" +
                "<script>\n" +
                getGlobalJSVariables(ip, song, allowWebNavigation, isMenu, prevAndNext) +
                getChordFunctionsJS() +
                getResizeJS() +
                getGoToSongJS(mainActivityInterface) +
                getNavigateJS(mainActivityInterface) +
                getAbcJSIfRequired(mainActivityInterface,song.getLyrics().contains(mainActivityInterface.getAbcNotation().getInlineAbcLineIndicator())) +
                getFolderChooserJS(isSongMenu) +
                getWebSocket(ip) +
                "  var lastWidth = window.innerWidth;\n" +
                "window.onresize = function() {\n" +
                "    if (window.innerWidth !== lastWidth) {\n" +
                "        lastWidth = window.innerWidth;\n" +
                "        measure();\n" +
                "    }\n" +
                "};" +
                "</script>\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" id=\"viewport-meta\">\n" +
                "</head>\n" +
                "<body class=\"page\" onload=\"javascript:measure(); filterByFolder();\">\n";
    }
    // Repeatable bits of code to save on duplication
    private static String getHTMLFontImports(MainActivityInterface mainActivityInterface) {
        // This prepares the import code for the top of the html file that locates the fonts from Google
        // If they've been downloaded already, they are cached on the device, so no need to redownload.
        String base1 = "@import url('https://fonts.googleapis.com/css?family=";
        String base2 = "&swap=true');\n";
        String importString = base1+mainActivityInterface.getMyFonts().getLyricFontName()+base2;
        importString += base1+mainActivityInterface.getMyFonts().getChordFontName()+base2;
        importString += ".menu                    {font-family:"+mainActivityInterface.getMyFonts().getLyricFontName()+", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color:white; " +
                "font-size:14.0pt; width: fit-content;}\n";
        importString += ".item                    {font-family:"+mainActivityInterface.getMyFonts().getLyricFontName()+", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color:white; " +
                "padding:0px;}\n";
        importString += ".lyric                   {font-family:"+mainActivityInterface.getMyFonts().getLyricFontName()+", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) + "; " +
                "padding: 0px; font-size:14.0pt; white-space:nowrap; width: fit-content;}\n";
        importString += ".chord                   {font-family:"+mainActivityInterface.getMyFonts().getChordFontName()+", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsChordsColor())) + "; " +
                "padding: 0px; font-size:"+(14.0f*mainActivityInterface.getProcessSong().scaleChords)+"pt; white-space:nowrap;width: fit-content;}\n";
        importString += ".capo                    {font-family:"+mainActivityInterface.getMyFonts().getChordFontName()+", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsCapoColor())) + "; " +
                "padding: 0px; font-size:"+(14.0f*mainActivityInterface.getProcessSong().scaleChords)+"pt; white-space:nowrap;width: fit-content;}\n";
        importString += ".titlemain               {font-family:"+mainActivityInterface.getMyFonts().getLyricFontName()+", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) + "; " +
                "padding: 0px; font-size:"+(14.0f*1.1f)+"pt; " +
                "text-decoration:underline;}\n";
        importString += ".titleextras             {font-family:"+mainActivityInterface.getMyFonts().getLyricFontName()+", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) + "; " +
                "padding: 0px; font-size:"+(14.0f*0.6f)+"pt; " +
                "text-decoration:none;}\n";
        importString += ".heading                 {font-family:"+mainActivityInterface.getMyFonts().getLyricFontName()+", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) + "; " +
                "padding: 0px; font-size:"+(14.0f*mainActivityInterface.getProcessSong().scaleHeadings)+"pt; " +
                "text-decoration:underline;}\n";
        importString += ".mono                    {font-family:"+mainActivityInterface.getMyFonts().getMonoFontName()+", 'Courier New', monospace; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) + "; " +
                "padding: 0px; font-size:"+(14.0f*mainActivityInterface.getProcessSong().scaleTabs)+"pt; " +
                "text-decoration:none;}\n";
        return importString;
    }
    private static String getMenuBarCSS(MainActivityInterface mainActivityInterface, boolean allowWebNavigation) {
        if (allowWebNavigation) {
            return  "#menu {\n" +
                    "    position: fixed;\n" +
                    "    top: 0;\n" +
                    "    left: 0;\n" +
                    "    width: 100%;\n" +
                    "    z-index: 1000;\n" +
                    "    background-color: #000000;\n" +
                    "    display: flex;\n" +
                    "    overflow-x: auto; /* Allows the buttons to slide horizontally if too many */\n" +
                    "    white-space: nowrap;\n" +
                    "    border-bottom: 1px solid #444;\n" +
                    "}" +
                    "\n" +
                    "label                    {margin-right:4px; padding:2px 2px 2px 2px; float:left; background-color:#294959; font-size:12pt;}\n" +
                    "a                        {margin-right:4px; padding:4px 2px 4px 2px; float:left; display:inline-block; color:white; background-color:#294959; font-size:14pt;}\n" +
                    "a:link                   {color:white; text-decoration:none; font-size:12pt;}\n" +
                    "a:visited                {color:white; text-decoration:none; font-size:12pt;}\n" +
                    "a:hover                  {color:white; text-decoration:none; font-size:12pt;}\n" +
                    "a:active                 {color:white; text-decoration:none; font-size:12pt;}\n";
        } else {
            return "";
        }
    }
    private static String getGlobalJSVariables(String ip,
                                               Song song, boolean allowWebNavigation,
                                               boolean isMenu, ArrayList<SongId> prevAndNext) {
        return  "  var contentWidth;\n" +
                "  var menuWidth;\n" +
                "  var menuscaleratio = 1;\n" +
                "  var listenToHost = localStorage.getItem('userListenToHost') !== 'false'; // default to true if not set\n" +
                "  var chords = localStorage.getItem('userShowChords') !== 'false'; // defaults to true if not set\n" +
                "  var menusized = false;\n" +
                "  var allowWebNavigation="+allowWebNavigation+";\n" +
                "  var minSize=" + isMenu+";\n" +
                "  var maxSize=true;\n" +
                "  var splash=false;\n" +
                "  var serverAddress = \"http://" + ip + ":8080/\";\n" +
                "  var currFolder =\"" + song.getFolder().replace("'","\\'").replace("\"","\\") + "\";\n" +
                "  var currFilename = \"" + song.getFilename().replace("'","\\'").replace("\"","\\") + "\";\n" +
                "  var prevFolder =\"" + prevAndNext.get(0).getFolder().replace("'","\\'").replace("\"","\\") + "\";\n" +
                "  var prevFilename = \"" + prevAndNext.get(0).getFilename().replace("'","\\'").replace("\"","\\") + "\";\n" +
                "  var nextFolder =\"" + prevAndNext.get(1).getFolder().replace("'","\\'").replace("\"","\\") + "\";\n" +
                "  var nextFilename = \"" + prevAndNext.get(1).getFilename().replace("'","\\'").replace("\"","\\") + "\";\n";
    }
    private static String getChordFunctionsJS() {
        return "  function toggleChords() {\n" +
                "    chords = !chords;\n" +
                "    localStorage.setItem('userShowChords', chords); // Save it!\n" +
                "    reloadSong();\n" +
                "  }\n";
    }
    private static String getResizeJS() {
        return "function measure() {\n" +
                "    var content = document.getElementById(\"content\");\n" +
                "    \n" +
                "    // 1. Critical: Reset scaling so we can measure the NATURAL width\n" +
                "    content.style.transform = \"scale(1)\";\n" +
                "    content.style.width = \"max-content\"; \n" +
                "\n" +
                "    // 2. Handle chord visibility before measuring\n" +
                "    var chordlines = document.getElementsByClassName('chord');\n" +
                "    for (var i = 0; i < chordlines.length; i++) {\n" +
                "        chordlines[i].style.display = chords ? 'table-cell' : 'none';\n" +
                "    }\n" +
                "\n" +
                "    // 3. Give the browser a split second to calculate the new table widths\n" +
                "    // then capture the true width\n" +
                "    contentWidth = content.offsetWidth;\n" +
                "    \n" +
                "    // 4. Run the actual resizing\n" +
                "    resize();\n" +
                "}\n" +
                "\n" +
                "  function resize() {\n" +
                "    var viewportWidth = window.innerWidth - 20; // 20px padding\n" +
                "    var content = document.getElementById(\"content\");\n" +
                "    var menu = document.getElementById(\"menu\");\n" +
                "    \n" +
                "    if (contentWidth > viewportWidth) {\n" +
                "        // Content is too wide, so we scale DOWN\n" +
                "        var scaleratio = viewportWidth / contentWidth;\n" +
                "        content.style.transform = \"scale(\" + scaleratio + \")\";\n" +
                "    } else {\n" +
                "        // Content fits, keep at 100%\n" +
                "        content.style.transform = \"scale(1)\";\n" +
                "    }\n" +
                "\n" +
                "    // Adjust the body height so we can scroll to the bottom of the scaled content\n" +
                "    // We add the menu height and some extra padding\n" +
                "    var scaledHeight = content.offsetHeight * (viewportWidth / contentWidth);\n" +
                "    document.body.style.height = (scaledHeight + 200) + \"px\";\n" +
                "}\n" +
                "  function offsetAnchor() {\n" +
                "    if (location.hash.length !== 0) {\n" +
                "       window.scrollTo(window.scrollX, window.scrollY - (document.getElementById('menu').clientHeight) * menuscaleratio);\n" +
                "    }\n" +
                "    if (chords && document.getElementById('chordbutton')!=null) {\n" +
                "      document.getElementById('chordbutton').style.textDecoration = \"none\";\n" +
                "    } else if (document.getElementById('chordbutton')!=null) {\n" +
                "      document.getElementById('chordbutton').style.textDecoration = \"line-through\";\n" +
                "    }\n" +
                "  }\n" +
                "  window.addEventListener(\"hashchange\", offsetAnchor);\n" +
                "  window.setTimeout(offsetAnchor, 1); // The delay of 1 is arbitrary and may not always work right (although it did in my testing).\n\n";
    }
    private static String getGoToSongJS(MainActivityInterface mainActivityInterface) {
        return  "  function goToNextSong() {\n" +
                "    if (nextFolder.length>0 && nextFilename.length>0) {\n" +
                "      window.location.href = serverAddress + \"song/?chords=\" + chords + \"&folder=\" + nextFolder + \"&filename=\" + nextFilename;\n" +
                "    }\n" +
                "  }\n" +
                "  function goToPrevSong() {\n" +
                "    if (prevFolder.length>0 && prevFilename.length>0) {\n" +
                "      window.location.href = serverAddress + \"song/?chords=\" + chords + \"&folder=\" + prevFolder + \"&filename=\" + prevFilename;\n" +
                "    }\n" +
                "  }\n" +
                "  function reloadSong() {\n" +
                "    if (currFolder.length>0 && currFilename.length>0) {\n" +
                "      window.location.href = serverAddress + \"song/?chords=\" + chords + \"&folder=\" + currFolder + \"&filename=\" + currFilename;\n" +
                "    }\n" +
                "  }\n" +
                "  function goToSong(folder,filename) {\n" +
                "    if (folder.length>0 && filename.length>0) {\n" +
                "      window.location.href = serverAddress + \"" + mainActivityInterface.getWebServer().getManualSongString() + "/?chords=\" + chords + \"&folder=\" + folder + \"&filename=\" + filename;\n" +
                "    }\n" +
                "  }\n";
    }
    private static String getNavigateJS(MainActivityInterface mainActivityInterface) {
        return  "  function songMenu() {\n" +
                "    window.location.href = serverAddress + \"" + mainActivityInterface.getWebServer().getSongMenuString() + "/?folder=\"+currFolder+\"&filename=\"+currFilename;\n" +
                "  }\n" +
                "  function setMenu() {\n" +
                "    window.location.href = serverAddress + \"" + mainActivityInterface.getWebServer().getSetMenuString() + "/?folder=\"+currFolder+\"&filename=\"+currFilename;\n" +
                "  }\n" +
                "  function hostSong() {\n" +
                "    var listenToHost = document.getElementById(\"listenToHost\").checked;" +
                "    localStorage.setItem('listenToHost', listenToHost); // Save it!/n" +
                "    window.location.href = serverAddress + \"" + mainActivityInterface.getWebServer().getHostSongString() + "/?chords=\"+chords;\n" +
                "  }\n";
    }
    private static String getFolderChooserJS(boolean songMenu) {
        if (!songMenu) {
            return "  function filterByFolder() {}\n";
        } else {
            return  "  function filterByFolder() {\n" +
                    "    const folderName = document.getElementById('folderChooser').value;\n\n" +
                    "    // Loop ONLY through elements that have the 'item' class\n" +
                    "    document.querySelectorAll('.item').forEach(div => {\n" +
                    "      // If folderName is \"Band\", it checks if the div has the \"Band\" class\n" +
                    "      if (div.classList.contains(folderName)) {\n" +
                    "        div.style.display = 'block';\n" +
                    "      } else {\n" +
                    "        div.style.display = 'none';\n" +
                    "      }\n" +
                    "    });\n" +
                    "  }\n";
        }
    }
    private static String getWebSocket(String ipAddress) {
        return "  var socket = new WebSocket('ws://' + window.location.host + '/updates');\n" +
                "  socket.onmessage = function(event) {\n" +
                "    if (event.data === 'REFRESH') {\n" +
                "      // Should we listen to the host update?" +
                "      var listenToHost = localStorage.getItem('userListenToHost') !== 'false'; // default to true\n" +
                "      if (listenToHost) {" +
                "        // 1. Check the local record\n" +
                "        var showChords = localStorage.getItem('userShowChords') !== 'false'; // default to true\n\n" +
                "        // 2. Request the specific version using a Query Parameter\n" +
                "        // This keeps the \"Path\" the same but tells the server the preferences\n" +
                "        window.location.href = serverAddress + \"hostsong/?chords=\" + showChords;\n" +
                "      }\n" +
                "    }\n" +
                "  };" +
                "  // Auto-reconnect if Wi-Fi drops briefly\n" +
                "  socket.onclose = function() { setTimeout(function() { location.reload(); }, 5000); };\n";
    }
    private static String getAbcJSIfRequired(MainActivityInterface mainActivityInterface, boolean hasAbc) {
        if (hasAbc) {
            return mainActivityInterface.getWebServer().getAbcJSFromAsset();
        } else {
            return "";
        }
    }
    private static String getMenuBarHTML(Context c, MainActivityInterface mainActivityInterface,
                                         Song song, boolean allowWebNavigation, boolean hideArrows,
                                         boolean songMenu, boolean setMenu) {
        String text = "";
        if (allowWebNavigation) {
            String songmenuJS = "songMenu()";
            String setmenuJS = "setMenu()";
            if (songMenu) {
                songmenuJS = "javascript:reloadSong()";
            }
            if (setMenu) {
                setmenuJS = "javascript:reloadSong()";
            }
            text = "<span id=\"menu\">\n<a id=\"songs\" href=\"javascript:" + songmenuJS + "\">&nbsp; " + c.getString(R.string.songs) + "&nbsp; </a>\n" +
                    "<a id=\"set\" href=\"javascript:" + setmenuJS + "\">&nbsp; " + c.getString(R.string.set) + "&nbsp; </a>\n";
            if (songMenu) {
                text += getSongFolderChooser(c,mainActivityInterface,song);
            }
            if (hideArrows) {
                text += "</span>\n";
            } else {
                text += "<label>&nbsp; " + c.getString(R.string.web_server_host_song) + " &nbsp;<input type=\"checkbox\" id=\"listenToHost\" onchange=\"javascript:hostSong()\"></label>\n" +
                        "<a href=\"javascript:toggleChords()\">&nbsp; <span id=\"chordbutton\">" + c.getString(R.string.chords) + "</span>&nbsp; </a>\n" +
                        "<a href=\"javascript:goToPrevSong()\">&nbsp; &nbsp; &lt;&nbsp; &nbsp; </a>\n" +
                        "<a href=\"javascript:goToNextSong()\">&nbsp; &nbsp; &gt;&nbsp; &nbsp; </a>\n</span>\n" +
                        "<script>document.getElementById(\"listenToHost\").checked = listenToHost;</script>\n";
            }
        }
        return text;
    }
    private static String getImgPDFSong(Context c, MainActivityInterface mainActivityInterface, Song songForHTML) {
        String imgPDFSong = "";
        Uri uriForItem = mainActivityInterface.getStorageAccess().getUriForItem("Songs",songForHTML.getFolder(),songForHTML.getFilename());
        if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("image",songForHTML.getFilename())) {
            imgPDFSong = mainActivityInterface.getWebServer().getEmbeddedImageString(c,uriForItem);

        } else if (mainActivityInterface.getStorageAccess().isSpecificFileExtension("pdf",songForHTML.getFilename())) {
            imgPDFSong = mainActivityInterface.getWebServer().getEmbeddedPDFString(c, uriForItem);
        }
        return imgPDFSong;
    }
    private static String getSongContent(MainActivityInterface mainActivityInterface, Song song,
                                         String imgPDFSong) {
        String songContent = "";
        StringBuilder stringBuilder = new StringBuilder();
        if (imgPDFSong.isEmpty()) {
            for (int sect = 0; sect < song.getPresoOrderSongSections().size(); sect++) {
                String section = song.getPresoOrderSongSections().get(sect);
                if (!section.isEmpty()) {
                    section = section.replace(mainActivityInterface.getProcessSong().columnbreak_string, "");
                    if (mainActivityInterface.getProcessSong().trimSections) {
                        // IV - End trim only as a section may start with a lyric line and have no header
                        section = ("¬" + section).trim().replace("¬", "");
                    }
                    // Add this section to the array (so it can be called later for presentation)
                    if (!section.trim().isEmpty()) {
                        // Now split by line, but keeping empty ones
                        String[] lines = section.split("\n", -1);
                        for (String line : lines) {
                            // IV - Do not process an empty group line or empty header line
                            if (!line.equals(mainActivityInterface.getProcessSong().groupline_string) && !line.equals("[]")) {
                                // Get the text stylings
                                String linetype = mainActivityInterface.getProcessSong().getLineType(line);
                                if (line.contains(mainActivityInterface.getProcessSong().groupline_string)) {
                                    // Has lyrics and chords
                                    stringBuilder.append(mainActivityInterface.getProcessSong().groupTableHTML(song, line));
                                } else {
                                    stringBuilder.append(mainActivityInterface.getProcessSong().lineTextHTML(song, linetype, line));
                                }
                            }
                        }
                    }
                }
            }
            songContent = mainActivityInterface.getSongSheetHeaders().getSongSheetTitleMainHTML(song) +
                    mainActivityInterface.getSongSheetHeaders().getSongSheetTitleExtrasHTML(song) +
                    stringBuilder;
        }
        return songContent;
    }

}
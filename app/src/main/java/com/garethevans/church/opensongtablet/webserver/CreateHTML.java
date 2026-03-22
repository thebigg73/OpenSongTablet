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

    @SuppressWarnings({"unused","FieldCanBeLocal"})
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
                .append(":").append(mainActivityInterface.getWebServer().getPortNumber()).append("/")
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
        html.append(getCommonHTMLTop(mainActivityInterface,song,ipAddress,allowWebNavigation,false,false, prevAndNext));

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

        html.append(getCommonHTMLTop(mainActivityInterface,song,ipAddress,true,true,true, prevAndNext));

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
        returnString.append(String.format("#%06X", (0xFFFFFF & mainActivityInterface.getPalette().background)));
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
        String string = "";
        string += "<!DOCTYPE html>\n";
        string += "<html lang=\"en\">\n";
        string += "<head>\n";
        string += "<title>OpenSongApp</title>\n";
        string += getStyles(mainActivityInterface);
        string += getJavascript(mainActivityInterface, song, prevAndNext, ip, isMenu, isSongMenu);
        string += "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" id=\"viewport-meta\">\n";
        string += "</head>\n";
        string += "<body class=\"page\" onload=\"javascript:initPage();\">\n";
        string += "<div id=\"alert-box\"></div>\n";
        return string;
    }

    private static String getStyles(MainActivityInterface mainActivityInterface) {
        // Prepare some basic reusable variables:
        // This prepares the import code for the top of the html file that locates the fonts from Google
        // If they've been downloaded already, they are cached on the device, so no need to redownload.
        // We also create all of the other css classes
        String base1 = "@import url('https://fonts.googleapis.com/css?family=";
        String base2 = "&swap=true');\n";
        String lyricsFontName = mainActivityInterface.getMyFonts().getLyricFontName();
        String chordsFontName = mainActivityInterface.getMyFonts().getChordFontName();
        String monoFontName = mainActivityInterface.getMyFonts().getMonoFontName();
        String lyricsTextColor = String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor()));
        String chordsTextColor = String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsChordsColor()));
        String capoTextColor = String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsCapoColor()));
        String pageTextColor = String.format("#%06X", (0xFFFFFF & mainActivityInterface.getPalette().textColor));
        String pageBackgroundColor = String.format("#%06X", (0xFFFFFF & mainActivityInterface.getPalette().background));
        String secondaryColor = String.format("#%06X", (0xFFFFFF & mainActivityInterface.getPalette().secondary));

                String string = "<style>\n";
        string += base1 + lyricsFontName + base2;
        string += base1 + chordsFontName + base2;
        string += ".menu                    {font-family:" + lyricsFontName + ", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color: " + pageTextColor + "; font-size:14.0pt; width: fit-content;}\n";
        string += ".item                    {font-family:" + lyricsFontName + ", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color: " + pageTextColor + "; font-size:14.0pt; padding:0px;}\n";
        string += ".itemSub                 {font-family:" + lyricsFontName + ", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color: " + pageTextColor + "; font-size: 60%; font-style:italic; display:block; border-collapse:collapse; opacity:75%;}\n";
        string += ".lyric                   {font-family:" + lyricsFontName + ", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color: " + lyricsTextColor + "; font-size:14.0pt; padding: 0px; white-space:nowrap; width: fit-content;}\n";
        string += ".chord                   {font-family:" + chordsFontName + ", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color: " + chordsTextColor + "; font-size:"+(14.0f*mainActivityInterface.getProcessSong().scaleChords)+"pt; padding: 0px; white-space:nowrap; width: fit-content;}\n";
        string += ".capo                    {font-family:" + chordsFontName + ", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color: " + capoTextColor + "; font-size:"+(14.0f*mainActivityInterface.getProcessSong().scaleChords)+"pt; padding: 0px; white-space: nowrap; width: fit-content;}\n";
        string += ".titlemain               {font-family:" + lyricsFontName + ", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color: " + lyricsTextColor + "; font-size:"+(14.0f*1.1f)+"pt; padding: 0px; text-decoration:underline;}\n";
        string += ".titleextras             {font-family:" + lyricsFontName + ", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color: " + lyricsTextColor + "; font-size:"+(14.0f*0.6f)+"pt; padding: 0px; text-decoration:none;}\n";
        string += ".heading                 {font-family:" + lyricsFontName + ", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color: " + lyricsTextColor + "; font-size:"+(14.0f*mainActivityInterface.getProcessSong().scaleHeadings)+"pt; padding: 0px; text-decoration:underline;}\n";
        string += ".mono                    {font-family:" + monoFontName + ", 'Courier New', monospace; color: " + lyricsTextColor + "; font-size:"+(14.0f*mainActivityInterface.getProcessSong().scaleTabs)+"pt; padding: 0px; text-decoration:none;}\n";
        string += "#content            {display: inline-block; position: absolute; top: 50px; left: 0; transform-origin: top left; padding-bottom: 100px;}\n";
        string += "#status-dot         {height: 12px; width: 12px; background-color: #bbb; border-radius: 50%; display: inline-block; margin-left: 10px; margin-top: 6px; margin-bottom: 6px; box-shadow: 0 0 5px rgba(0,0,0,0.5);}\n";
        string += ".connected          {background-color: #00ff00; }\n";
        string += ".disconnected       {background-color: #ff0000; }\n";
        string += ".clickable,a        {-webkit-user-select: none; -ms-user-select: none; user-select: none; cursor: pointer;}\n";
        string += ".page               {color:" + pageTextColor + "; background-color: " + pageBackgroundColor + ";}\n";
        string += ".lyrictable         {border-spacing:0; border-collapse: collapse; border:0px;}\n";
        string += "#menu               {position: fixed; top: 0; left: 0; width: 100%; z-index: 1000; background-color: " + pageBackgroundColor + "; display: flex; overflow-x: auto; white-space: nowrap; border-bottom: 1px solid #444;}\n";
        string += "label               {margin-right: 4px; padding: 4px 2px 2px 2px; float: left; background-color:" + secondaryColor + "; font-size:12pt; color:" + pageTextColor + ";}\n";
        string += "a                   {margin-right: 4px; padding: 4px 2px 4px 2px; float: left; display: inline-block; color:" + pageTextColor + "; background-color: " + secondaryColor + "; font-size:14pt;}\n";
        string += "a:link              {color: " + pageTextColor + "; text-decoration:none; font-size:12pt;}\n";
        string += "a:visited           {color: " + pageTextColor + "; text-decoration:none; font-size:12pt;}\n";
        string += "a:hover             {color: " + pageTextColor + "; text-decoration:none; font-size:12pt;}\n";
        string += "a:active            {color: " + pageTextColor + "; text-decoration:none; font-size:12pt;}\n";
        string += ".folderChooser      {width: fit-content; margin: 4px; border-collapse: collapse; background-color: " + pageBackgroundColor + "; font-size: 12pt; color:" + pageTextColor + ";}\n";
        string += "hr                  {width: 100%; color:" + pageTextColor + "; margin: 10px auto;}\n";
        string += "#alert-box          {position: fixed; bottom: 20px; left: 50%; transform: translateX(-50%); background-color: " + secondaryColor + "; color: " + pageTextColor + "; padding: 15px 25px; border-radius: 30px; box-shadow: 0 4px 15px rgba(0,0,0,0.4); display: none; z-index: 5000; font-family: font-family:" + lyricsFontName + ", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; text-align: center; animation: slideUp 0.4s ease-out;}\n";
        string += "@keyframes slideUp  {from { bottom: -100px; opacity: 0; } to { bottom: 20px; opacity: 1; }}\n";
        string += "body,select,option  {width:100%; font-family:" + lyricsFontName + ", -apple-system, BlinkMacSystemFont, Tahoma, Verdana, sans-serif; color:" + pageTextColor + "; background-color: " + pageBackgroundColor + ";}\n";
        string += "</style>\n";
        return string;
    }
    private static String getJavascript(MainActivityInterface mainActivityInterface, Song song, ArrayList<SongId> prevAndNext, String ip, boolean isMenu, boolean songMenu) {
        String string = "<script>\n";
        string += "  var contentWidth;\n";
        string += "  var menuWidth;\n";
        string += "  var menuscaleratio = 1;\n";
        string += "  var listenToHost = localStorage.getItem('userListenToHost') !== 'false'; // default to true if not set\n";
        string += "  var chords = localStorage.getItem('userShowChords') !== 'false'; // defaults to true if not set\n";
        string += "  var menusized = false;\n";
        string += "  var allowWebNavigation = "+mainActivityInterface.getWebServer().getAllowWebNavigation()+";\n";
        string += "  var minSize = " + isMenu+";\n";
        string += "  var maxSize = true;\n";
        string += "  var splash = false;\n";
        string += "  var serverAddress = \"http://" + ip + ":" + mainActivityInterface.getWebServer().getPortNumber() + "/\";\n";
        string += "  var currFolder =\"" + song.getFolder().replace("'","\\'").replace("\"","\\") + "\";\n";
        string += "  var currFilename = \"" + song.getFilename().replace("'","\\'").replace("\"","\\") + "\";\n";
        string += "  var prevFolder =\"" + prevAndNext.get(0).getFolder().replace("'","\\'").replace("\"","\\") + "\";\n";
        string += "  var prevFilename = \"" + prevAndNext.get(0).getFilename().replace("'","\\'").replace("\"","\\") + "\";\n";
        string += "  var nextFolder =\"" + prevAndNext.get(1).getFolder().replace("'","\\'").replace("\"","\\") + "\";\n";
        string += "  var nextFilename = \"" + prevAndNext.get(1).getFilename().replace("'","\\'").replace("\"","\\") + "\";\n";
        string += "  var socket;\n";
        string += "  var dot; // Define it here, but don't assign yet\n\n";

        string += "  function toggleChords() {\n";
        string += "    chords = !chords;\n";
        string += "    localStorage.setItem('userShowChords', chords); // Save it!\n";
        string += "    reloadSong();\n";
        string += "  }\n";

        string += "  function goToNextSong() {\n";
        string += "    if (nextFolder.length>0 && nextFilename.length>0) {\n";
        string += "      window.location.href = serverAddress + \"song/?chords=\" + chords + \"&folder=\" + nextFolder + \"&filename=\" + nextFilename;\n";
        string += "    }\n";
        string += "  }\n";

        string += "  function goToPrevSong() {\n";
        string += "    if (prevFolder.length>0 && prevFilename.length>0) {\n";
        string += "      window.location.href = serverAddress + \"song/?chords=\" + chords + \"&folder=\" + prevFolder + \"&filename=\" + prevFilename;\n";
        string += "    }\n";
        string += "  }\n";

        string += "  function reloadSong() {\n";
        string += "    if (currFolder.length>0 && currFilename.length>0) {\n";
        string += "      window.location.href = serverAddress + \"song/?chords=\" + chords + \"&folder=\" + currFolder + \"&filename=\" + currFilename;\n";
        string += "    }\n";
        string += "  }\n";

        string += "  function goToSong(folder,filename) {\n";
        string += "    if (folder.length>0 && filename.length>0) {\n";
        string += "      window.location.href = serverAddress + \"" + mainActivityInterface.getWebServer().getManualSongString() + "/?chords=\" + chords + \"&folder=\" + folder + \"&filename=\" + filename;\n";
        string += "    }\n";
        string += "  }\n";

        string += "  function songMenu() {\n";
        string += "    window.location.href = serverAddress + \"" + mainActivityInterface.getWebServer().getSongMenuString() + "/?folder=\"+currFolder+\"&filename=\"+currFilename;\n";
        string += "  }\n";
        string += "  function setMenu() {\n";
        string += "    window.location.href = serverAddress + \"" + mainActivityInterface.getWebServer().getSetMenuString() + "/?folder=\"+currFolder+\"&filename=\"+currFilename;\n";
        string += "  }\n";

        string += "  function hostSong() {\n";
        string += "    var listenToHost = document.getElementById(\"listenToHost\").checked;";
        string += "    localStorage.setItem('listenToHost', listenToHost); // Save it!\n";
        string += "    if (listenToHost) {\n";
        string += "      window.location.href = serverAddress + \"" + mainActivityInterface.getWebServer().getHostSongString() + "/?chords=\"+chords;\n";
        string += "    }\n";
        string += "  }\n";

        if (!songMenu) {
            string += "  function filterByFolder() {}\n";
        } else {
            string += "  function filterByFolder() {\n";
            string += "    const folderName = document.getElementById('folderChooser').value;\n";
            string += "    // Loop ONLY through elements that have the 'item' class\n";
            string += "    document.querySelectorAll('.item').forEach(div => {\n";
            string += "      // If folderName is \"Band\", it checks if the div has the \"Band\" class\n";
            string += "      if (div.classList.contains(folderName)) {\n";
            string += "        div.style.display = 'block';\n";
            string += "      } else {\n";
            string += "        div.style.display = 'none';\n";
            string += "      }\n";
            string += "    });\n";
            string += "  }\n";
        }

        if (song.getLyrics()!=null && song.getLyrics().contains(mainActivityInterface.getAbcNotation().getInlineAbcLineIndicator())) {
            string += mainActivityInterface.getWebServer().getAbcJSFromAsset();
        }

        string += "  function connect() {\n";
        string += "    dot = document.getElementById(\"status-dot\");\n";
        string += "    // Use the dynamic host to avoid hardcoding IPs\n";
        string += "    socket = new WebSocket('ws://' + window.location.host + '/updates');\n";
        string += "    socket.onopen = function() {\n";
        string += "        console.log(\"WebSocket Connected\");\n";
        string += "        if (dot) dot.className = \"connected\";\n";
        string += "        // Heartbeat to prevent Safari suspension\n";
        string += "        if (window.heartbeat) clearInterval(window.heartbeat);\n";
        string += "        window.heartbeat = setInterval(function() {\n";
        string += "          if (socket.readyState === WebSocket.OPEN) {\n";
        string += "            socket.send(\"keep-alive\");\n";
        string += "          }\n";
        string += "        }, 10000);\n";
        string += "    };\n";
        string += "    socket.onclose = function() {\n";
        string += "      console.log(\"WebSocket Disconnected. Retrying...\");\n";
        string += "      if (dot) dot.className = \"disconnected\";\n";
        string += "      if (window.heartbeat) clearInterval(window.heartbeat);\n";
        string += "      setTimeout(connect, 2000); // Reconnect loop\n";
        string += "    };\n";
        string += "    socket.onmessage = function(event) {\n";
        string += "      if (event.data === 'REFRESH') {\n";
        string += "        if (localStorage.getItem('userListenToHost') !== 'false') { hostSong(); }\n";
        string += "      } else if (event.data.startsWith('MSG:')) {\n";
        string += "        var msg = event.data.substring(4);\n";
        string += "        var box = document.getElementById('alert-box');\n";
        string += "        box.innerText = msg;\n";
        string += "        box.style.display = 'block';\n";
        string += "        setTimeout(function() { box.style.display = 'none'; }, 10000);\n";
        string += "      }\n";
        string += "    };\n";
        string += "  }\n";

        string += "  function measure() {\n";
        string += "    var content = document.getElementById(\"content\");\n";
        string += "    // 1. Critical: Reset scaling so we can measure the NATURAL width\n";
        string += "    content.style.transform = \"scale(1)\";\n";
        string += "    content.style.width = \"max-content\"; \n";
        string += "    // 2. Handle chord visibility before measuring\n";
        string += "    var chordlines = document.getElementsByClassName('chord');\n";
        string += "    for (var i = 0; i < chordlines.length; i++) {\n";
        string += "      chordlines[i].style.display = chords ? 'table-cell' : 'none';\n";
        string += "    }\n";
        string += "    // 3. Give the browser a split second to calculate the new table widths\n";
        string += "    // then capture the true width\n";
        string += "    contentWidth = content.offsetWidth;\n";
        string += "    // 4. Run the actual resizing\n";
        string += "    resize();\n";
        string += "  }\n";

        string += "  function resize() {\n";
        string += "    var viewportWidth = window.innerWidth - 20; // 20px padding\n";
        string += "    var content = document.getElementById(\"content\");\n";
        string += "    var menu = document.getElementById(\"menu\");\n";
        string += "    if (contentWidth > viewportWidth) {\n";
        string += "      // Content is too wide, so we scale DOWN\n";
        string += "      var scaleratio = viewportWidth / contentWidth;\n";
        string += "      content.style.transform = \"scale(\" + scaleratio + \")\";\n";
        string += "    } else {\n";
        string += "      // Content fits, keep at 100%\n";
        string += "      content.style.transform = \"scale(1)\";\n";
        string += "    }\n";
        string += "    // Adjust the body height so we can scroll to the bottom of the scaled content\n";
        string += "    // We add the menu height and some extra padding\n";
        string += "    var scaledHeight = content.offsetHeight * (viewportWidth / contentWidth);\n";
        string += "    document.body.style.height = (scaledHeight + 200) + \"px\";\n";
        string += "  }\n";

        string += "  function offsetAnchor() {\n";
        string += "    if (location.hash.length !== 0) {\n";
        string += "      window.scrollTo(window.scrollX, window.scrollY - (document.getElementById('menu').clientHeight) * menuscaleratio);\n";
        string += "    }\n";
        string += "    if (chords && document.getElementById('chordbutton')!=null) {\n";
        string += "      document.getElementById('chordbutton').style.textDecoration = \"none\";\n";
        string += "    } else if (document.getElementById('chordbutton')!=null) {\n";
        string += "      document.getElementById('chordbutton').style.textDecoration = \"line-through\";\n";
        string += "    }\n";
        string += "  }\n";

        string += "  var lastWidth = window.innerWidth;\n";
        string += "  window.onresize = function() {\n";
        string += "    if (window.innerWidth !== lastWidth) {\n";
        string += "      lastWidth = window.innerWidth;\n";
        string += "      measure();\n";
        string += "    }\n";
        string += "  };";

        string += "  // Create one clean init function\n";
        string += "  function initPage() {\n";
        string += "    measure();\n";
        string += "    filterByFolder();\n";
        string += "    connect();\n";
        string += "  };\n";

        string += "  window.addEventListener(\"hashchange\", offsetAnchor);\n";
        string += "  window.setTimeout(offsetAnchor, 1); // The delay of 1 is arbitrary and may not always work right (although it did in my testing).\n";

        string += "</script>\n";
        return string;
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
            text = "<span id=\"menu\">\n<span id=\"status-dot\"></span>\n<a id=\"songs\" href=\"javascript:" + songmenuJS + "\">&nbsp; " + c.getString(R.string.songs) + "&nbsp; </a>\n" +
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
package com.garethevans.church.opensongtablet.webserver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    private MainActivityInterface mainActivityInterface;
    private Context c;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "WebServer";
    private String ip;
    private boolean runWebServer;
    private final String base1 = "@import url('https://fonts.googleapis.com/css?family=";
    private final String base2 = "&swap=true');\n";
    private final String resizeJS = "" +
            "  var contentWidth;\n" +
            "  function measure() {\n" +
            "    contentWidth = document.getElementById(\"content\").clientWidth;\n" +
            "    resize();\n" +
            "  }\n" +
            "  function resize() {\n" +
            "    var viewportWidth = document.body.clientWidth - 24;\n" +
            "    var padding = document.body.style.padding;\n" +
            "    var scaleSize = ((viewportWidth/contentWidth)*100) + \"%\";\n" +
            "    document.body.style.zoom = scaleSize;\n" +
            "  }\n";

    private final String goToSong = "" +
            "  function getSong(how,index) {\n" +
            "      if (how==\"set\") {\n" +
            "        window.location.href = serverAddress + \"/setitem/\" + index;\n" +
            "      } else {\n" +
            "        window.location.href = serverAddress + \"/songitem/\" + index;\n" +
            "      }\n" +
            "  }\n";
    private final String navigateJS = "" +
            "  function songMenu() {\n" +
            "    window.location.href = serverAddress + \"/songmenu/\"" +
            "  }\n" +
            "  function setMenu() {\n" +
            "    window.location.href = serverAddress + \"/setmenu/\"" +
            "  }\n";

    private final String menuBarCSS = "" +
            "ul {list-style-type:none; margin:0; padding: 0; overflow:hidden; color:white;}\n" +
            "li {float:left;background-color:#294959; margin-right:8px;}\n" +
            "a {display:block; padding:8px; color:white;background-color:#294959;}\n" +
            "a:link {color:white; text-decoration:none;}\n" +
            "a:visited {color:white; text-decoration:none;}\n" +
            "a:hover {color:white; text-decoration:none;}\n" +
            "a:active {color:white; text-decoration:none;}\n";

    public WebServer() {
        super(8080);
    }

    @SuppressLint("DefaultLocale")
    public void initialiseVariables(Context c) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        WifiManager wifiMan = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
        runWebServer = mainActivityInterface.getPreferences().getMyPreferenceBoolean("runWebServer",false);
        callRunWebServer();
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.d(TAG,"session.getUri():"+session.getUri());
        String pagerequest = session.getUri();
        String webpage = "";
        if (pagerequest.contains("/setitem/")) {
            // We want to load a song in the set at the position afterwards
            pagerequest = pagerequest.replace("/setitem/","");
            pagerequest = pagerequest.replaceAll("\\D","");
            if (!pagerequest.isEmpty()) {
                // Get the song to load
                int setItemNum = Integer.parseInt(pagerequest);
                String folder = mainActivityInterface.getCurrentSet().getFolder(setItemNum);
                String filename = mainActivityInterface.getCurrentSet().getFilename(setItemNum);
                Song songForHTML = new Song();
                songForHTML.setFolder(folder);
                songForHTML.setFilename(filename);
                songForHTML = mainActivityInterface.getLoadSong().doLoadSong(songForHTML,false);
                webpage = getProcessedSongHTML(songForHTML);
            }
        } else if (pagerequest.contains("/songitem/")) {
            // We want to load a song in the song menu at the position afterwards
            pagerequest = pagerequest.replace("/songmenu/","");
            pagerequest = pagerequest.replaceAll("\\D","");
            if (!pagerequest.isEmpty()) {
                // Get the song to load
                int songItemNum = Integer.parseInt(pagerequest);
                String folder = mainActivityInterface.getSongsInMenu().get(songItemNum).getFolder();
                String filename = mainActivityInterface.getSongsInMenu().get(songItemNum).getFilename();
                Song songForHTML = new Song();
                songForHTML.setFolder(folder);
                songForHTML.setFilename(filename);
                songForHTML = mainActivityInterface.getLoadSong().doLoadSong(songForHTML,false);
                webpage = getProcessedSongHTML(songForHTML);
            }
        } else if (pagerequest.contains("/setmenu/")) {
            webpage = createSetSongListHTML(true);

        } else if (pagerequest.contains("/songmenu/")) {
            webpage = createSetSongListHTML(false);
        } else {
            // TODO Splash screen
            webpage = createSetSongListHTML(true);
        }

        // TODO temporarily write the page to a file for testing
        mainActivityInterface.getStorageAccess().doStringWriteToFile("Settings","","index.html",webpage);

        return newFixedLengthResponse(webpage);
    }

    private String getProcessedSongHTML(Song songForHTML) {
        mainActivityInterface.getProcessSong().processSongIntoSections(songForHTML,false);
        Log.d(TAG,"songLyrics:"+songForHTML.getLyrics());
        boolean showChords = mainActivityInterface.getProcessSong().getDisplayChords();
        // IV - Initialise transpose capo key  - might be needed
        mainActivityInterface.getTranspose().capoKeyTranspose(songForHTML);

        StringBuilder stringBuilder = new StringBuilder();
        for (int sect = 0; sect < songForHTML.getPresoOrderSongSections().size(); sect++) {
            String section = songForHTML.getPresoOrderSongSections().get(sect);
            if (!section.isEmpty()) {
                section = section.replace(mainActivityInterface.getProcessSong().columnbreak_string,"");
                if (mainActivityInterface.getProcessSong().trimSections) {
                    // IV - End trim only as a section may start with a lyric line and have no header
                    section = ("¬" + section).trim().replace("¬","");
                }
                // Add this section to the array (so it can be called later for presentation)
                if (!section.trim().isEmpty()) {
                    // Now split by line, but keeping empty ones
                    String[] lines = section.split("\n",-1);
                    for (String line : lines) {
                        // IV - Do not process an empty group line or empty header line
                        if (!line.equals(mainActivityInterface.getProcessSong().groupline_string) && !line.equals("[]")) {
                            // Get the text stylings
                            String linetype = mainActivityInterface.getProcessSong().getLineType(line);
                            if (line.contains(mainActivityInterface.getProcessSong().groupline_string)) {
                                // Has lyrics and chords
                                stringBuilder.append(mainActivityInterface.getProcessSong().groupTableHTML(songForHTML, line));
                            } else {
                                stringBuilder.append(mainActivityInterface.getProcessSong().lineTextHTML(songForHTML, linetype, line));
                            }
                        }
                    }
                }
            }
        }

        return  "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                getHTMLFontImports() +
                ".page       {background-color:" + String.format("#%06X", (0xfff & mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor())) + ";}\n" +
                ".lyrictable {border-spacing:0; border-collapse: collapse; border:0px;}\n" +
                menuBarCSS +
                "body        {width:100%;}\n" +
                "</style>\n" +
                "<script>\n" +
                "  var minSize=false;\n" +
                resizeJS +
                "  var serverAddress = \"" +
                getIP() +
                "\";\n" +
                navigateJS +
                "</script>" +
                "</head>\n" +
                "<body class=\"page\" onload=\"javascript:measure()\">\n" +
                "<ul class=\"menu\"><li><a href=\"javascript:songMenu()\">" + c.getString(R.string.songs) + "</a></li>" +
                "<li><a href=\"javascript:setMenu()\">" + c.getString(R.string.set) + "</a></li></ul>\n" +
                "<div id=\"content\" style=\"width:fit-content\">\n" +
                mainActivityInterface.getSongSheetHeaders().getSongSheetTitleMainHTML(songForHTML) +
                mainActivityInterface.getSongSheetHeaders().getSongSheetTitleExtrasHTML(songForHTML) +
                stringBuilder +
                "</div>\n</body>\n" +
                "</html>";
    }

    public String getIP() {
        return "http://" + ip + ":8080";
    }

    public Bitmap getIPQRCode() {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(getIP(), BarcodeFormat.QR_CODE, 200, 200);

            int w = bitMatrix.getWidth();
            int h = bitMatrix.getHeight();
            int[] pixels = new int[w * h];
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    pixels[y * w + x] = bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }

            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, w, 0, 0, w, h);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean getRunWebServer() {
        return runWebServer;
    }
    public void setRunWebServer(boolean runWebServer) {
        this.runWebServer = runWebServer;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("runWebServer",runWebServer);
        callRunWebServer();
    }

    public void callRunWebServer() {
        try {
            if (runWebServer) {
                this.start();
                Log.d(TAG,"Web server started");
            } else {
                this.stop();
                Log.d(TAG,"Web server stopped");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createSetSongListHTML(boolean setlist) {
        // This uses the current set list to create a web page
        StringBuilder setSongListHTML = new StringBuilder();
        setSongListHTML.append("<!DOCTYPE html>\n<html>\n")
                .append("<head>\n")
                .append("<style>\n")
                .append(base1)
                .append(mainActivityInterface.getMyFonts().getLyricFontName())
                .append(base2)
                .append(".page {background-color:")
                .append(String.format("#%06X", (0xfff & mainActivityInterface.getMyThemeColors().getLyricsBackgroundColor())))
                .append(";}\n")
                .append(".item {font-family:")
                .append(mainActivityInterface.getMyFonts().getLyricFontName())
                .append("; color:")
                .append(String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())))
                .append("; padding:0px; font-size:14.0pt; white-space:nowrap; width: fit-content;}\n")
                .append("body {width:100%;}\n")
                .append("</style>\n")
                .append("<script>\n")
                .append("var minSize=true;\n")
                .append(resizeJS)
                .append("  var serverAddress = \"")
                .append(getIP())
                .append("\";\n")
                .append(goToSong)
                .append("</script>\n")
                .append("</head>\n")
                .append("<body class=\"page\" onload=\"javascript:measure()\" onresize=\"javascript:resize()\">\n")
                .append("<div id=\"content\" style=\"width:fit-content\">\n");

        if (setlist) {
            // Now cycle through our set list and add a new div element for each one
            for (int x=0; x < mainActivityInterface.getCurrentSet().getSetItems().size(); x++) {
                String title = mainActivityInterface.getCurrentSet().getTitle(x);
                setSongListHTML.append("<div class=\"item\" onclick=\"javascript:getSong('set','")
                        .append(x).append("')\">").append(title).append("</div>\n");
            }
        } else {
            // Now cycle through the song list and add a new div element for each one
            for (int x=0; x<mainActivityInterface.getSongsInMenu().size(); x++) {
                String title = mainActivityInterface.getSongsInMenu().get(x).getTitle();
                setSongListHTML.append("<div class=\"item\" onclick=\"javascript:getSong('song','")
                        .append(x).append("')\">").append(title).append("</div>\n");
            }
        }

        setSongListHTML.append("</div>\n</body>\n</html>");

        return setSongListHTML.toString();
    }

    private String getHTMLFontImports() {
        // This prepares the import code for the top of the html file that locates the fonts from Google
        // If they've been downloaded already, they are cached on the device, so no need to redownload.
        String base1 = "@import url('https://fonts.googleapis.com/css?family=";
        String base2 = "&swap=true');\n";
        String importString = base1+mainActivityInterface.getMyFonts().getLyricFontName()+base2;
        importString += base1+mainActivityInterface.getMyFonts().getChordFontName()+base2;
        importString += ".menu {font-family:"+mainActivityInterface.getMyFonts().getLyricFontName()+"; color:white; " +
                "font-size:14.0pt;}\n";
        importString += ".lyric {font-family:"+mainActivityInterface.getMyFonts().getLyricFontName()+"; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) + "; " +
                "padding: 0px; font-size:14.0pt; white-space:nowrap; width: fit-content;}\n";
        importString += ".chord {font-family:"+mainActivityInterface.getMyFonts().getChordFontName()+"; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsChordsColor())) + "; " +
                "padding: 0px; font-size:"+(14.0f*mainActivityInterface.getProcessSong().scaleChords)+"pt; white-space:nowrap;width: fit-content;}\n";
        importString += ".capo {font-family:"+mainActivityInterface.getMyFonts().getChordFontName()+"; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsCapoColor())) + "; " +
                "padding: 0px; font-size:"+(14.0f*mainActivityInterface.getProcessSong().scaleChords)+"pt; white-space:nowrap;width: fit-content;}\n";
        importString += ".titlemain {font-family:"+mainActivityInterface.getMyFonts().getLyricFontName()+"; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) + "; " +
                "padding: 0px; font-size:"+(14.0f*1.1f)+"pt; " +
                "text-decoration:underline;}\n";
        importString += ".titleextras {font-family:"+mainActivityInterface.getMyFonts().getLyricFontName()+"; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) + "; " +
                "padding: 0px; font-size:"+(14.0f*0.6f)+"pt; " +
                "text-decoration:none;}\n";
        importString += ".heading {font-family:"+mainActivityInterface.getMyFonts().getLyricFontName()+"; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) + "; " +
                "padding: 0px; font-size:"+(14.0f*mainActivityInterface.getProcessSong().scaleHeadings)+"pt; " +
                "text-decoration:underline;}\n";
        importString += ".mono {font-family:"+mainActivityInterface.getMyFonts().getMonoFontName()+"; color:" +
                String.format("#%06X", (0xFFFFFF & mainActivityInterface.getMyThemeColors().getLyricsTextColor())) + "; " +
                "padding: 0px; font-size:"+(14.0f*mainActivityInterface.getProcessSong().scaleComments)+"pt; " +
                "text-decoration:underline;}\n";
        return importString;
    }

}
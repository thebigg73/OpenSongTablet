package com.garethevans.church.opensongtablet.webserver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.garethevans.church.opensongtablet.songprocessing.SongId;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;

public class WebServer {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "WebServer";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private String abcJSFromAsset;
    private boolean runWebServer;
    private boolean allowWebNavigation;
    private String webServerPort;
    private WebServerFragment webServerFragment;
    private String ipAddress;
    private String webServerMessage1, webServerMessage2, webServerMessage3, webServerMessage4,
            webServerMessage5, webServerMessageTemp;

    // The strings used in the JavaScript and Ktor to identify what we want
    private final String hostsong="hostsong", songmenu="songmenu", setmenu = "setmenu", manualsong="song";

    public WebServer(Context c) {
        this.c = c;
        this.mainActivityInterface = (MainActivityInterface) c;
        abcJSFromAsset = "";
        try {
            InputStream inputStream = c.getAssets().open("ABC/abcjs-basic-min.js");
            abcJSFromAsset = "\n" + mainActivityInterface.getStorageAccess().readTextFileToString(inputStream) + "\n";
        } catch (Exception e) {
            e.printStackTrace();
        }
        getUpdatedPreferences();
    }

    // If we change load in a profile, this is called
    public void getUpdatedPreferences() {
        runWebServer = mainActivityInterface.getPreferences().getMyPreferenceBoolean("runWebServer",false);
        allowWebNavigation = mainActivityInterface.getPreferences().getMyPreferenceBoolean("allowWebNavigation",false);
        webServerPort = mainActivityInterface.getPreferences().getMyPreferenceString("webServerPort","8080");
        // If we have Wi-Fi permissions, we can go ahead and get the required info and start the server if needed automatically
        if (mainActivityInterface.getAppPermissions().hasWebServerPermission()) {
            callRunWebServer();
        }

        // webServerMessage1-5/Temp are used to send messages to connected clients
        webServerMessage1 = mainActivityInterface.getPreferences().getMyPreferenceString("webServerMessage1","");
        webServerMessage2 = mainActivityInterface.getPreferences().getMyPreferenceString("webServerMessage2","");
        webServerMessage3 = mainActivityInterface.getPreferences().getMyPreferenceString("webServerMessage3","");
        webServerMessage4 = mainActivityInterface.getPreferences().getMyPreferenceString("webServerMessage4","");
        webServerMessage5 = mainActivityInterface.getPreferences().getMyPreferenceString("webServerMessage5","");
        webServerMessageTemp = mainActivityInterface.getPreferences().getMyPreferenceString("webServerMessageTemp","");
    }

    // Keep a reference for the webServerFragment
    public void setWebServerFragment(WebServerFragment webServerFragment) {
        this.webServerFragment = webServerFragment;
    }

    public void callRunWebServer() {
        getIP();
        try {
            if (runWebServer) {
                KtorServer.INSTANCE.start(c, Integer.parseInt(webServerPort));

            } else {
                KtorServer.INSTANCE.stopServerExternal();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopWebServer() {
        try {
            Log.d(TAG,"stopWebServer()");
            ipAddress = null;
            KtorServer.INSTANCE.stopServerExternal();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get IP address and QR code to match
    @SuppressLint("DefaultLocale")
    public String getIP() {
        ipAddress = "0.0.0.0";
        if (mainActivityInterface.getAppPermissions().hasWebServerPermission()) {
            // METHOD 1: Try WifiManager first (most reliable for Wi-Fi)
            try {
                WifiManager wifiMan = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiMan != null && wifiMan.isWifiEnabled()) {
                    WifiInfo wifiInf = wifiMan.getConnectionInfo();
                    if (wifiInf != null) {
                        int ip = wifiInf.getIpAddress();
                        // ip of 0 means not connected to Wi-Fi
                        if (ip != 0) {
                            ipAddress = String.format("%d.%d.%d.%d",
                                    (ip & 0xff),
                                    (ip >> 8 & 0xff),
                                    (ip >> 16 & 0xff),
                                    (ip >> 24 & 0xff));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainActivityInterface.getStorageAccess().updateCrashLog(e.toString());
            }

            // METHOD 2: If WifiManager failed, enumerate network interfaces
            // but filter out mobile networks and prioritize WiFi interfaces
            if (ipAddress.equals("0.0.0.0")) {
                try {
                    String wifiIp = null;
                    String fallbackIp = null;

                    Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                            .getNetworkInterfaces();
                    while (enumNetworkInterfaces.hasMoreElements()) {
                        NetworkInterface networkInterface = enumNetworkInterfaces
                                .nextElement();
                        String interfaceName = networkInterface.getName().toLowerCase();

                        // Skip mobile/cellular interfaces explicitly
                        if (interfaceName.startsWith("rmnet") ||    // Qualcomm mobile
                                interfaceName.startsWith("ccmni") ||    // MediaTek mobile
                                interfaceName.startsWith("pdp") ||      // Legacy mobile
                                interfaceName.startsWith("dummy") ||    // Dummy interfaces
                                interfaceName.startsWith("p2p")) {      // WiFi Direct (not local network)
                            continue;
                        }

                        Enumeration<InetAddress> enumInetAddress = networkInterface
                                .getInetAddresses();
                        while (enumInetAddress.hasMoreElements()) {
                            InetAddress inetAddress = enumInetAddress.nextElement();

                            if (inetAddress.getHostAddress() != null &&
                                    !inetAddress.getHostAddress().contains(":") &&
                                    !inetAddress.getHostAddress().contains("127.0")) {

                                String ipAddr = inetAddress.getHostAddress();

                                // Filter out carrier-grade NAT addresses (100.64.0.0/10)
                                // These are commonly used by mobile networks
                                if (ipAddr.startsWith("100.")) {
                                    String[] parts = ipAddr.split("\\.");
                                    if (parts.length == 4) {
                                        try {
                                            int secondOctet = Integer.parseInt(parts[1]);
                                            // Skip 100.64.x.x through 100.127.x.x
                                            if (secondOctet >= 64 && secondOctet <= 127) {
                                                continue;
                                            }
                                        } catch (NumberFormatException ignored) {
                                        }
                                    }
                                }

                                // Prioritize WiFi interfaces (wlan) and Ethernet (eth)
                                if (interfaceName.startsWith("wlan") || interfaceName.startsWith("eth")) {
                                    wifiIp = ipAddr;
                                    break;
                                } else {
                                    // Save as fallback (e.g., tethering, VPN)
                                    if (fallbackIp == null) {
                                        fallbackIp = ipAddr;
                                    }
                                }
                            }
                        }

                        // If we found a WiFi IP, use it
                        if (wifiIp != null) {
                            break;
                        }
                    }

                    // Prefer WiFi, then fallback, then default
                    if (wifiIp != null) {
                        ipAddress = wifiIp;
                    } else if (fallbackIp != null) {
                        ipAddress = fallbackIp;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    mainActivityInterface.getStorageAccess().updateCrashLog(e.toString());
                    ipAddress = "0.0.0.0";
                }
            }
        }
        return ipAddress;
    }

    public Bitmap getIPQRCode() {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode("http://"+getIP()+":"+webServerPort+"/", BarcodeFormat.QR_CODE, 800, 800);

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
            mainActivityInterface.getStorageAccess().updateCrashLog(e.toString());
            return null;
        }
    }

    // The preferences for running and options for web server
    public boolean getRunWebServer() {
        return runWebServer;
    }
    public void setRunWebServer(boolean runWebServer) {
        this.runWebServer = runWebServer;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("runWebServer",runWebServer);
        if (!runWebServer) {
            ipAddress = null;
        } else {
            getIP();
        }
        callRunWebServer();
    }
    public boolean getAllowWebNavigation() {
        return allowWebNavigation;
    }
    public void setAllowWebNavigation(boolean allowWebNavigation) {
        this.allowWebNavigation = allowWebNavigation;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("allowWebNavigation",allowWebNavigation);
    }

    public String getAbcJSFromAsset() {
        return abcJSFromAsset;
    }

    // Used for debug in Performance Fragment
    public void runKtorTemp() {
        // This changes if we are using the host song or the user choice
        Song songForHTML = mainActivityInterface.getSong();
        mainActivityInterface.getProcessSong().processSongIntoSections(songForHTML,false);

        String newSplashPage = CreateHTML.getSplashHTML(c,songForHTML,ipAddress);
        mainActivityInterface.getStorageAccess().writeFileFromString("Settings","","newSplashPage.html", newSplashPage);

        String newWebPage = CreateHTML.getSongHTML(c,songForHTML,ipAddress,true,true, getPreviousAndNextSongForArrows(songForHTML));
        mainActivityInterface.getStorageAccess().writeFileFromString("Settings","","newWebPage.html", newWebPage);

        String songMenuPage = CreateHTML.getSongMenuHTML(c,songForHTML,ipAddress,true, getPreviousAndNextSongForArrows(songForHTML));
        mainActivityInterface.getStorageAccess().writeFileFromString("Settings","","songMenuPage.html", songMenuPage);

        String setMenuPage = CreateHTML.getSetMenuHTML(c,songForHTML,ipAddress,true, getPreviousAndNextSongForArrows(songForHTML));
        mainActivityInterface.getStorageAccess().writeFileFromString("Settings","","setMenuPage.html", setMenuPage);
    }

    // Called when we load a song to push a refresh to connected web clients
    public void updateKtor() {
        if (runWebServer) {
            KtorServer.INSTANCE.pushRefresh();
        }
    }

    public String getHostSongString() {
        return hostsong;
    }
    public String getSongMenuString() {
        return songmenu;
    }
    public String getSetMenuString() {
        return setmenu;
    }
    public String getManualSongString() {
        return manualsong;
    }

    public ArrayList<SongId> getPreviousAndNextSongForArrows(Song song) {
        // The array to hold the previous and next songs
        ArrayList<SongId> prevAndNext = new ArrayList<>();

        // These are blank and if not available, will remain blank
        SongId previousSong = new SongId();
        SongId nextSong = new SongId();

        // Work out if this song is in the current setlist
        int indexOfSongInSet = mainActivityInterface.getSetActions().indexSongInSet(song);
        if (indexOfSongInSet!=-1) {
            // Ok, if index of song is >0, then previous is the one before
            if (indexOfSongInSet>0) {
                SetItemInfo prev = mainActivityInterface.getCurrentSet().getSetItemInfo(indexOfSongInSet - 1);
                previousSong.setFolder(prev.songfolder);
                previousSong.setFilename(prev.songfilename);
            }

            if (indexOfSongInSet<mainActivityInterface.getCurrentSet().getCurrentSetSize()-1) {
                SetItemInfo next = mainActivityInterface.getCurrentSet().getSetItemInfo(indexOfSongInSet + 1);
                nextSong.setFolder(next.songfolder);
                nextSong.setFilename(next.songfilename);
            }

        } else {
            // The song must exist in the song menu.  Let's index this in the song folder
            ArrayList<Song> songsInFolder = mainActivityInterface.getSQLiteHelper().
                    getSongsByFilters(true,false,false,
                            false,false,false, song.getFolder(),
                            null,null,null,null,null,false);
            int indexSongInFolder = songsInFolder.indexOf(song);
            if (indexSongInFolder>0) {
                Song prev = songsInFolder.get(indexSongInFolder-1);
                previousSong.setFolder(prev.getFolder());
                previousSong.setFilename(prev.getFilename());
            }
            if (indexSongInFolder<songsInFolder.size()-1) {
                Song next = songsInFolder.get(indexSongInFolder+1);
                nextSong.setFolder(next.getFolder());
                nextSong.setFilename(next.getFilename());
            }

        }
        // Add the previous/next back to the array and return
        prevAndNext.add(previousSong);
        prevAndNext.add(nextSong);
        return prevAndNext;
    }

    public String getEmbeddedImageString(Context c, Uri imageUri) {
        byte[] bytes;
        try {
            // 1. Open an InputStream from the ContentResolver
            InputStream inputStream = c.getContentResolver().openInputStream(imageUri);

            // 2. Decode the stream into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            int maxWidth = 360;
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            // Only resize if the image is actually wider than 800px
            if (width > maxWidth) {
                float aspectRatio = (float) height / (float) width;
                int newHeight = Math.round(maxWidth * aspectRatio);

                // Create the scaled version
                bitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
            }

            // 3. Compress the bitmap into a ByteArrayOutputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

            bytes = baos.toByteArray();

            // 1. Convert the byte array into a Base64 encoded string
            String base64String = Base64.getEncoder().encodeToString(bytes);

            // 2. Return the HTML img tag with the Data URI scheme
            return "<img src=\"data:image/png;base64," + base64String + "\" width=\"360px\"/>";

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    public String getEmbeddedPDFString(Context c, Uri pdfUri) {
        byte[] bytes;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ParcelFileDescriptor pfd = mainActivityInterface.getProcessSong().getPDFParcelFileDescriptor(pdfUri);
                PdfRenderer pdfRenderer = mainActivityInterface.getProcessSong().getPDFRenderer(pfd);
                int pageCount = mainActivityInterface.getProcessSong().getPDFPageCount(pdfRenderer);
                StringBuilder stringBuilder = new StringBuilder();
                if (pageCount > 0) {
                    // Go through each page, convert to an image and encode it
                    for (int i = 0; i < pageCount; i++) {
                        PdfRenderer.Page page = pdfRenderer.openPage(i);
                        Bitmap bitmap = mainActivityInterface.getProcessSong().createBitmapFromPage(
                                mainActivityInterface.getProcessSong().getPDFPageSize(page, true),
                                page, true, true);

                        int maxWidth = 360;
                        int width = bitmap.getWidth();
                        int height = bitmap.getHeight();

                        // Only resize if the image is actually wider than 800px
                        if (width > maxWidth) {
                            float aspectRatio = (float) height / (float) width;
                            int newHeight = Math.round(maxWidth * aspectRatio);

                            // Create the scaled version
                            bitmap = Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
                        }

                        // 3. Compress the bitmap into a ByteArrayOutputStream
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

                        bytes = baos.toByteArray();

                        // 1. Convert the byte array into a Base64 encoded string
                        String base64String = Base64.getEncoder().encodeToString(bytes);

                        // 2. Return the HTML img tag with the Data URI scheme
                        stringBuilder.append("<img src=\"data:image/png;base64,");
                        stringBuilder.append(base64String);
                        stringBuilder.append("\" width=\"360px\"/>\n<p/>\n");
                    }
                    return stringBuilder.toString();
                } else {
                    return c.getString(R.string.error) + "\n";
                }
            } else {
                return c.getString(R.string.pdf_preview_not_allowed);
            }
        } catch (Exception e) {
            return c.getString(R.string.error) + "\n";
        }

    }

    public String getPortNumber() {
        return webServerPort;
    }
    public void setPortNumber(String webServerPort) {
        this.webServerPort = webServerPort;
        mainActivityInterface.getPreferences().setMyPreferenceString("webServerPort",webServerPort);
        // Stop the server and start it again
        KtorServer.INSTANCE.start(c, Integer.parseInt(webServerPort));
    }

    /**
     * Get the string saved to the webServerMessage1-5/Temp
     * @param messageNumber - the number of the message (1-5).  If 0, then send webServerMessageTemp
     * @return the message String
     */
    public String getWebServerMessage(int messageNumber) {
        switch (messageNumber) {
            case 1:
                return webServerMessage1;
            case 2:
                return webServerMessage2;
            case 3:
                return webServerMessage3;
            case 4:
                return webServerMessage4;
            case 5:
                return webServerMessage5;
            default:
                return webServerMessageTemp;
        }
    }
    /**
     * Get the string saved to the webServerMessage1-5/Temp
     * @param messageNumber - the number of the message (1-5).  If 0, then webServerMessageTemp
     * @param message - the message to save to the preference
     */
    public void setWebServerMessage(int messageNumber, String message) {
        String prefString = "webServerMessage"+message;
        switch (messageNumber) {
            case 1:
                webServerMessage1 = message;
                break;
            case 2:
                webServerMessage2 = message;
                break;
            case 3:
                webServerMessage3 = message;
                break;
            case 4:
                webServerMessage4 = message;
                break;
            case 5:
                webServerMessage5 = message;
                break;
            default:
                webServerMessageTemp = message;
                prefString = "webServerMessageTemp";
                break;
        }
        mainActivityInterface.getPreferences().setMyPreferenceString(prefString,message);
    }
    public void sendWebServerMessage(int message) {
        try {
            KtorServer.INSTANCE.pushPreferenceMessage(message, mainActivityInterface);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

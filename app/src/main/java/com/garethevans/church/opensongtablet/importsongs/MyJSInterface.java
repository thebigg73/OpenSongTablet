package com.garethevans.church.opensongtablet.importsongs;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;

import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MyJSInterface {

    @SuppressLint("StaticFieldLeak")
    private static Context c = null;
    private static MainActivityInterface mainActivityInterface = null;
    private static Uri saveFile = null, saveFileTxt = null;
    private static Fragment fragment = null;
    private static final String TAG = "MyJSInterface";
    private String filename;
    private static String shadowHTML="";

    public MyJSInterface(Context c, Fragment fragment) {
        MyJSInterface.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        MyJSInterface.fragment = fragment;
        saveFile = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", "SongSelect.pdf");
        saveFileTxt = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", "SongSelect.txt");
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" MyJSInterface Create Received/SongSelect.pdf  deleteOld=true");
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, saveFile, null, "Received", "", "SongSelect.pdf");
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, saveFileTxt, null, "Received", "", "SongSelect.txt");
    }

    public static String getBase64StringFromBlobUrl(String blobUrl) {
        if (blobUrl.startsWith("blob")) {
            return "javascript: var xhr = new XMLHttpRequest();" +
                    "xhr.open('GET', '" + blobUrl + "', true);" +
                    "xhr.setRequestHeader('Content-type','application/pdf');" +
                    "xhr.responseType = 'blob';" +
                    "xhr.onload = function(e) {" +
                    "    if (this.status == 200) {" +
                    "        var blobPdf = this.response;" +
                    "        var reader = new FileReader();" +
                    "        reader.readAsDataURL(blobPdf);" +
                    "        reader.onloadend = function() {" +
                    "            base64data = reader.result;" +
                    "            HTMLOUT.getBase64FromBlobData(base64data);" +
                    "        }" +
                    "    }" +
                    "};" +
                    "xhr.send();";
        }
        return "javascript: console.log('It is not a Blob URL');";
    }

    @JavascriptInterface
    public void getBase64FromBlobData(String base64Data) throws IOException {
        convertBase64StringToPdfAndStoreIt(base64Data);
    }

    @JavascriptInterface
    public String getHighlightedText(String highlightedText) {
        // Do nothing yet
        Log.d(TAG,"highlightedText:"+highlightedText);
        return mainActivityInterface.getConvertTextSong().convertText(highlightedText);
    }

    @JavascriptInterface
    public static String flattenShadowRoot() {
        // IV - Return all content with the shadow DOM expanded
        return "var shadowHTML = '';\n" +
                "// Returns HTML of given shadow DOM.\n" +
                "function getShadowDomHtml(shadowRoot) {\n" +
                "    shadowHTML = '';\n" +
                "    for (let el of shadowRoot.childNodes) {\n" +
                "        shadowHTML += el.nodeValue || el.innerHTML || el.outerHTML;\n" +
                "    }\n" +
                "    return shadowHTML;\n" +
                "};\n" +
                "\n" +
                "// Recursively replaces shadow DOMs with their HTML.\n" +
                "function replaceShadowDomsWithHtml(rootElement) {\n" +
                "    for (let el of rootElement.querySelectorAll('*')) {\n" +
                "        if (el.shadowRoot) {\n" +
                "            replaceShadowDomsWithHtml(el.shadowRoot);\n" +
                "            el.innerHTML += getShadowDomHtml(el.shadowRoot);\n" +
                "        }\n" +
                "    }\n" +
                "   shadowHTML = document.documentElement.outerHTML;\n" +
                "   HTMLOUT.setShadowHTML(shadowHTML);\n" +
                "}\n" +
                "replaceShadowDomsWithHtml(document.body);";
    }

    @JavascriptInterface
    public static String setShadowHTML(String shadowHTML) {
        Log.d(TAG,"shadowHTML:"+shadowHTML);
        MyJSInterface.shadowHTML = shadowHTML;
        return shadowHTML;
    }

    public static String getFlattenedWebString() {
        return shadowHTML;
    }
    public static void resetFlattenedWebString() {
        shadowHTML = "";
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    private void convertBase64StringToPdfAndStoreIt(String base64PDf) throws IOException {
        Log.d(TAG, base64PDf);
        String songFilename = "SongSelect.pdf";
        if (base64PDf.contains("data:text/plain;base64,")) {
            saveFile = saveFileTxt;
            songFilename = "SongSelect.txt";
        }
        String baseCode = base64PDf.replaceFirst("^data:application/pdf;base64,", "").
                replaceFirst("^data:text/plain;base64,","");
        byte[] pdfAsBytes = Base64.decode(baseCode, 0);
        Log.d(TAG, "saveFile=" + saveFile);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(saveFile);
        outputStream.write(pdfAsBytes);
        outputStream.flush();

        Log.d(TAG, "Download blob complete");

        mainActivityInterface.songSelectDownload(fragment, R.id.importOnlineFragment,saveFile,songFilename);
    }

    public static String doNormalDownLoad(String url, String filename) {
        return "javascript: HTMLOUT.getHighlightedText(\""+url+"\",\""+filename+"\");";
    }

    public String doExtractFromSelected(String selected) {
        return "javascript: HTMLOUT.getHighlighterText(\""+selected+"\");";
    }

    @JavascriptInterface
    public void getHTML(String html) {
        String[] lines = html.split("\n");
        for (String line:lines) {
            Log.d(TAG,line);
        }
    }

    @JavascriptInterface
    public static void setDownload(String url, String filename) {
        String cookie = CookieManager.getInstance().getCookie(url);
        Log.d("d", "url=" + url);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
        Uri downloadedFile = Uri.fromFile(file);
        DownloadManager dm = (DownloadManager) c.getSystemService(DOWNLOAD_SERVICE);
        request.addRequestHeader("Cookie", cookie);
        if (dm != null) {
            dm.enqueue(request);
        }

        Log.d(TAG, "downloadedFile:" + downloadedFile);
            try {
                if (filename.endsWith(".pdf")) {
                    InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(downloadedFile);
                    OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(saveFile);
                    mainActivityInterface.getStorageAccess().copyFile(inputStream, outputStream);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        mainActivityInterface.songSelectDownload(fragment, R.id.importOnlineFragment, saveFile, filename);

    }
}
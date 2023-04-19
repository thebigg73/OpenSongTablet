package com.garethevans.church.opensongtablet.importsongs;

import static android.content.Context.DOWNLOAD_SERVICE;

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

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final Uri saveFile;
    private final Fragment fragment;
    private final String TAG = "MyJSInterface";
    private String filename;

    public MyJSInterface(Context c, Fragment fragment) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        this.fragment = fragment;
        saveFile = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", "SongSelect.pdf");
        mainActivityInterface.getStorageAccess().updateFileActivityLog(TAG+" MyJSInterface Create Received/SongSelect.pdf  deleteOld=true");
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false, saveFile, null, "Received", "", "SongSelect.pdf");
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

    public void setFilename(String filename) {
        this.filename = filename;
    }

    private void convertBase64StringToPdfAndStoreIt(String base64PDf) throws IOException {
        Log.d(TAG, base64PDf);
        byte[] pdfAsBytes = Base64.decode(base64PDf.replaceFirst("^data:application/pdf;base64,", ""), 0);
        Log.d(TAG, "saveFile=" + saveFile);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(saveFile);
        outputStream.write(pdfAsBytes);
        outputStream.flush();

        Log.d(TAG, "Download blob complete");

        mainActivityInterface.songSelectDownloadPDF(fragment, R.id.importOnlineFragment,saveFile,"SongSelect.pdf");
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
    public void setDownload(String url, String filename) {
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
            mainActivityInterface.songSelectDownloadPDF(fragment, R.id.importOnlineFragment, saveFile, filename);

    }
}
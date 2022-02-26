package com.garethevans.church.opensongtablet.importsongs;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;

import androidx.fragment.app.Fragment;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.io.IOException;
import java.io.OutputStream;


public class MyJSInterface {

    private final Context context;
    private final MainActivityInterface mainActivityInterface;
    private final Uri saveFile;
    private final Fragment fragment;
    private final String TAG = "MyJSInterface";

    public MyJSInterface(Context context, MainActivityInterface mainActivityInterface, Fragment fragment) {
        this.context = context;
        this.mainActivityInterface = mainActivityInterface;
        this.fragment = fragment;
        saveFile = mainActivityInterface.getStorageAccess().getUriForItem("Received", "", "SongSelect.pdf");
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(false,saveFile, null, "Received", "", "SongSelect.pdf");
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

    private void convertBase64StringToPdfAndStoreIt(String base64PDf) throws IOException {
        Log.d(TAG, base64PDf);
        byte[] pdfAsBytes = Base64.decode(base64PDf.replaceFirst("^data:application/pdf;base64,", ""), 0);
        Log.d(TAG, "saveFile=" + saveFile);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(saveFile);
        outputStream.write(pdfAsBytes);
        outputStream.flush();

        Log.d(TAG, "Download blob complete");

        mainActivityInterface.songSelectDownloadPDF(fragment, R.id.importOnlineFragment,saveFile);
    }

    public static String doNormalDownLoad(String url) {
        return "javascript: HTMLOUT.setDownload(\""+url+"\");";
    }

    @JavascriptInterface
    public void getHTML(String html) {
        String[] lines = html.split("\n");
        for (String line:lines) {
            Log.d(TAG,line);
        }
    }

    @JavascriptInterface
    public void setDownload(String url) {
        String cookie = CookieManager.getInstance().getCookie(url);
        //String myName = "SongSelect";
        Log.d("d", "url=" + url);
        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(url));

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!

        request.setDestinationUri(saveFile);
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        request.addRequestHeader("Cookie", cookie);
        if (dm != null) {
            dm.enqueue(request);
        }
        Log.d("d", "Download complete");


        mainActivityInterface.songSelectDownloadPDF(fragment, R.id.importOnlineFragment,saveFile);
    }
}
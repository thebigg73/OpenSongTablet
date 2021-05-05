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

    public MyJSInterface(Context context, MainActivityInterface mainActivityInterface, Fragment fragment) {
        this.context = context;
        this.mainActivityInterface = mainActivityInterface;
        this.fragment = fragment;
        saveFile = mainActivityInterface.getStorageAccess().getUriForItem(context, mainActivityInterface.getPreferences(), "Received", "", "SongSelect.pdf");
        mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(context, mainActivityInterface.getPreferences(), saveFile, null, "Received", "", "SongSelect.pdf");
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
        Log.e("BASE 64", base64PDf);
        //final int notificationId = 1;
        //String currentDateTime = DateFormat.getDateTimeInstance().format(new Date());
        //final File dwldsPath = new File(context.getExternalFilesDir("Downloads"), "SongSelect.pdf");
        /*final File dwldsPath = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS) + "/YourFileName_" + currentDateTime + "_.pdf");
        */
        byte[] pdfAsBytes = Base64.decode(base64PDf.replaceFirst("^data:application/pdf;base64,", ""), 0);
        Log.d("d", "saveFile=" + saveFile);
        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(context, saveFile);
        outputStream.write(pdfAsBytes);
        outputStream.flush();

        Log.d("d", "Download blob complete");

        mainActivityInterface.songSelectDownloadPDF(fragment, R.id.importOnlineFragment,saveFile);
    }

    public static String doNormalDownLoad(String url) {
        return "javascript: HTMLOUT.setDownload(\""+url+"\");";
    }

    @JavascriptInterface
    public void getHTML(String html) {
        String[] lines = html.split("\n");
        for (String line:lines) {
            Log.d("getHTML",line);
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
        //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,myName);

        request.setDestinationUri(saveFile);
        //File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
        //downloadedFile = Uri.fromFile(file);
        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        request.addRequestHeader("Cookie", cookie);
        if (dm != null) {
            dm.enqueue(request);
        }
        Log.d("d", "Download complete");


        mainActivityInterface.songSelectDownloadPDF(fragment, R.id.importOnlineFragment,saveFile);
    }
}
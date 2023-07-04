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
    private static Uri saveFile = null;
    private static Fragment fragment = null;
    private static final String TAG = "MyJSInterface";
    private String filename;
    private static String shadowHTML="";

    public MyJSInterface(Context c, Fragment fragment) {
        MyJSInterface.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        MyJSInterface.fragment = fragment;
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

    @JavascriptInterface
    public static String flattenShadowRoot() {
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
                "   HTMLOUT.getShadowHTML(shadowHTML);\n" +
                "};";

        /*return "function getShadow() {\n" +
                "        var shadowHTML = '';\n" +
                "        var nodes = document.querySelector('#ChordSheetViewerContainer').shadowRoot.querySelectorAll('*');\n" +
                "        for (let el of nodes) {\n" +
                "            shadowHTML += el.outerHTML;\n" +
                "        }\n" +
                "        HTMLOUT.getShadowHTML(shadowHTML);\n" +
                "    }";*/
        /*return "var shadowHTML='';\n" +
                "var foundShadow = document.getElementById('ChordSheetViewerContainer);\n" +
                "if (foundShadow!=null) {\n" +
                "   var slots = foundShadow.shadowRoot.querySelectorAll('div');\n" +
                "   var nodes = slots[0].assignedNodes({flatten: true});\n" +
                "   for (let el of nodes) {\n" +
                "       shadowHTML += el.outerHTML;\n" +
                "   }\n" +
                "   HTMLOUT.getShadowHTML(shadowHTML);\n" +
                "}\n" +
                "function getShadowHTML() {\n" +
                "   HTMLOUT.getShadowHTML(shadowHTML);\n" +
                "}\n";
*/
        /*return  "\nvar shadowHTML = '';\n" +
                "var getShadowDomHtml = (shadowRoot) => {\n" +
                "    shadowHTML = '';\n" +
                "    for (let el of shadowRoot.childNodes) {\n" +
                "        shadowHTML += el.nodeValue || el.outerHTML;\n" +
                "    }\n" +
                "    console.log('shadowHTML:'+shadowHTML);\n" +
                "HTMLOUT.getShadowHTML(shadowHTML);"+
                "};\n" +
                "\n" +
                "// Recursively replaces shadow DOMs with their HTML.\n" +
                "var replaceShadowDomsWithHtml = (rootElement) => {\n" +
                "    for (let el of rootElement.querySelectorAll('*')) {\n" +
                "        if (el.shadowRoot) {\n" +
                "            replaceShadowDomsWithHtml(shadowRoot);\n" +
                "            el.innerHTML += getShadowDomHtml(el.shadowRoot);\n" +
                "        }\n" +
                "    }\n" +
                "};\n" +
                "function getShadowHTML() {\n" +
                "   HTMLOUT.getShadowHTML(shadowHTML);" +
                "}\n" +
                "replaceShadowDomsWithHtml(document.body);";
*/
        /*
        const getShadowDomHtml = (shadowRoot) => {
    let shadowHTML = '';
    for (let el of shadowRoot.childNodes) {
        shadowHTML += el.nodeValue || el.outerHTML;
    }
    console.log(shadowHTML);
};

// Recursively replaces shadow DOMs with their HTML.
const replaceShadowDomsWithHtml = (rootElement) => {
    for (let el of rootElement.querySelectorAll('*')) {
        if (el.shadowRoot) {
            replaceShadowDomsWithHtml(shadowRoot);
            el.innerHTML += getShadowDomHtml(el.shadowRoot);
        }
    }
};
         */
    }

    @JavascriptInterface
    public static String getShadowHTML(String shadowHTML) {
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

        /*

      const recursiveWalk = (node, func) => {
    const done = func(node);
    if (done) {
        return true;
    }

    if ('shadowRoot' in node && node.shadowRoot) {
        const done = recursiveWalk(node.shadowRoot, func);
        if (done) {
            return true;
        }
    }
    node = node.firstChild;

    while (node) {
        const done = recursiveWalk(node, func);
        if (done) {
            return true;
        }
        node = node.nextSibling;
    }
}

let html = '';

recursiveWalk(document.body, function (node) {
    html += node.nodeValue || node.outerHTML;
});

console.log(html);
         */

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

        mainActivityInterface.songSelectDownload(fragment, R.id.importOnlineFragment,saveFile,"SongSelect.pdf");
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
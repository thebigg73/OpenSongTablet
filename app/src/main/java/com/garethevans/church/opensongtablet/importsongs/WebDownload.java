package com.garethevans.church.opensongtablet.importsongs;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebDownload {

    // This class is used to download files from the internet
    // These can be my sample songs, or files from UG, SongSelect, etc.
    // This will be called in a new thread somewhere!

    public String[] doDownload(Context c, StorageAccess storageAccess, String address, String filename) {
        String[] returnMessages = new String[2];

        InputStream input = null;
        FileOutputStream outputStream = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            Log.d("d", "address=" + address);
            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                returnMessages[0] = "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
                returnMessages[1] = null;
                return returnMessages;

            } else {
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                Log.d("d", "fileLength=" + fileLength);

                // download the file
                input = connection.getInputStream();
                Log.d("d", "input=" + input);
                Log.d("d", "filename=" + filename);

                if (input != null) {

                    File tempfile = new File(c.getExternalFilesDir("Files"),filename);
                    Uri uri = Uri.fromFile(tempfile);

                    Log.d("DownloadTask", "uri=" + uri);
                    Log.d("DownloadTask", "filename=" + filename);

                    outputStream = new FileOutputStream(tempfile);
                    Log.d("DownloadTask", "outputStream=" + outputStream);
                    Log.d("DownloadTask", "c="+c);
                    Log.d("DownloadTask", "c.getContentResolver()="+c.getContentResolver());

                    byte[] data = new byte[4096];
                    int count;
                    while ((count = input.read(data)) != -1) {
                        outputStream.write(data, 0, count);
                    }
                    returnMessages[1] = uri.toString();

                } else {
                    returnMessages[0] = c.getResources().getString(R.string.network_error);
                    returnMessages[1] = null;
                    return returnMessages;
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            returnMessages[0] = c.getResources().getString(R.string.network_error);
            returnMessages[1] = null;
            return returnMessages;

        } finally {
            try {
                if (outputStream != null)
                    outputStream.close();
                if (input != null)
                    input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (connection != null) {
                connection.disconnect();
            }
        }
        return returnMessages;
    }

}
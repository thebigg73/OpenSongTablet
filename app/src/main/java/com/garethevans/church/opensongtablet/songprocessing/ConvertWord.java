package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.net.Uri;

import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import org.zwobble.mammoth.DocumentConverter;
import org.zwobble.mammoth.Result;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ConvertWord {

    // Try to convert content of Word douments into text

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "ConvertWord";
    private final MainActivityInterface mainActivityInterface;

    public ConvertWord(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
    }

    public String convertDocxToText(Uri uri, String filename) {
        // Because we need to have a file rather than a uri, make a temp version
        String text = "";
        try {
            InputStream inputStream = mainActivityInterface.getStorageAccess().getInputStream(uri);
            File tempFile = mainActivityInterface.getStorageAccess().getAppSpecificFile("Temp", "", filename);
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            boolean success = mainActivityInterface.getStorageAccess().copyFile(inputStream, fileOutputStream);
            if (success) {
                DocumentConverter converter = new DocumentConverter();

                // We could just extract text, but using HTML we can get headings
                Result<String> resultHTML = converter.convertToHtml(tempFile);
                text = resultHTML.getValue();
                text = text.replace("<h1>","[").replace("</h1>","]");
                text = text.replace("<h2>","[").replace("</h2>","]");
                text = text.replace("<h3>","[").replace("</h3>","]");
                text = text.replace("<p>","\n").replace("</p>","\n");
                text = text.replace("<br>","\n").replace("</br>","\n");

                text = mainActivityInterface.getProcessSong().removeHTMLTags(text);
                String[] lines = text.split("\n");
                StringBuilder stringBuilder = new StringBuilder();
                for (String line : lines) {
                    stringBuilder.append(line.trim()).append("\n");
                }
                text = mainActivityInterface.getConvertTextSong().convertText(stringBuilder.toString());

            }

        } catch (Exception e) {
            e.printStackTrace();
            mainActivityInterface.getStorageAccess().updateFileActivityLog(e.toString());
        }
        return text;
    }
}

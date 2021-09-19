package com.garethevans.church.opensongtablet.export;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.garethevans.church.opensongtablet.R;

import java.util.ArrayList;

public class ExportActions {

    public Intent setShareIntent(String content, String type, Uri uri, ArrayList<Uri> uris) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        if (content!=null) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }
        if (uri!=null) {
            intent.putExtra(Intent.EXTRA_STREAM, uri);
        }
        if (uris!=null && uris.size()>0) {
            for (Uri extraUri:uris) {
                intent.putExtra(Intent.EXTRA_STREAM, extraUri);
            }
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if (type!=null) {
            intent.setType(type);
        }
        return intent;

    }
    public Intent setIntent(String subject, String title, String content) {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_TEXT, content);
        return intent;
    }

    public Intent exportBackup(Context c, Uri uri, String filename) {
        Intent intent = setIntent(c.getString(R.string.backup_info),filename, filename);
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(uri);
        String type = "*/*";
        if (filename.endsWith(".pdf")) {
            type = "application/pdf";
        } else if (filename.endsWith(".png") || filename.endsWith(".jpg")) {
            type = "image/*";
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.setType(type);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        return intent;
    }

}

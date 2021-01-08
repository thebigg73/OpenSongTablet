package com.garethevans.church.opensongtablet.export;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.garethevans.church.opensongtablet.R;

import java.util.ArrayList;

public class ExportActions {

    public Intent setEmailIntent(String subject, String title, String content) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TITLE, title);
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        return emailIntent;
    }

    public Intent exportBackup(Context c, Uri uri, String filename) {
        Intent emailIntent = setEmailIntent(c.getString(R.string.backup_info),filename, filename);
        ArrayList<Uri> uris = new ArrayList<>();
        uris.add(uri);
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return emailIntent;
    }

}

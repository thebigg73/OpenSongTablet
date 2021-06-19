package com.garethevans.church.opensongtablet.filemanagement;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class ExportFiles {

    public Intent exportActivityLog(Context c, MainActivityInterface mainActivityInterface) {
        String title = c.getString(R.string.app_name) + ": " + c.getString(R.string.ccli);
        String subject = title + " - " + c.getString(R.string.ccli_view);
        String text = c.getString(R.string.ccli_church) + ": " +
                mainActivityInterface.getPreferences().getMyPreferenceString(c,"ccliChurchName","") + "\n";
        text += c.getString(R.string.ccli_licence) + ": " +
                mainActivityInterface.getPreferences().getMyPreferenceString(c,"ccliLicence","")+ "\n\n";
        Intent emailIntent = setEmailIntent(subject,title,text);

        // Add the attachments
        Uri uri = mainActivityInterface.getStorageAccess().getUriForItem(c, mainActivityInterface, "Settings", "", "ActivityLog.xml");
        ArrayList<Uri> uris = new ArrayList<>();
        if (!mainActivityInterface.getStorageAccess().uriExists(c,uri)) {
            mainActivityInterface.getCCLILog().createBlankXML(c, mainActivityInterface, uri);
        }
        // Add the uri
        uris.add(uri);
        emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return emailIntent;
    }

    private Intent setEmailIntent(String subject, String title, String content) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TITLE, title);
        emailIntent.putExtra(Intent.EXTRA_TEXT, content);
        return emailIntent;
    }
}

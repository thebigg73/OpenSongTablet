package com.garethevans.church.opensongtablet.screensetup;

import android.view.View;
import android.widget.TextView;

import com.garethevans.church.opensongtablet.preferences.StaticVariables;

public class AppActionBar {

    public void setActionBar(TextView title, TextView author, TextView key, String newtitle) {

        if (newtitle == null) {
            // We are in the Performance/Stage mode
            if (title != null && StaticVariables.mTitle != null) {
                title.setText(StaticVariables.mTitle);
            }
            if (author != null && StaticVariables.mAuthor != null && !StaticVariables.mAuthor.isEmpty()) {
                author.setText(StaticVariables.mAuthor);
                hideView(author, false);
            } else {
                hideView(author, true);
            }
            if (key != null && StaticVariables.mKey != null && !StaticVariables.mKey.isEmpty()) {
                String k = " (" + StaticVariables.mKey + ")";
                key.setText(k);
                hideView(key, false);
            } else {
                hideView(key, true);
            }
        } else {
            // We are in a different fragment, so hide the song info stuff
            if (title != null) {
                title.setText(newtitle);
                hideView(author, true);
                hideView(key, true);
            }
        }
    }

    public void setActionBarCapo(TextView capo, String string) {
        capo.setText(string);
    }

    private void hideView(View v, boolean hide) {
        if (hide) {
            v.setVisibility(View.GONE);
        } else {
            v.setVisibility(View.VISIBLE);
        }
    }
}

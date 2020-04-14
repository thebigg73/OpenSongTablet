package com.garethevans.church.opensongtablet.screensetup;

import android.app.ActionBar;
import android.view.View;
import android.widget.TextView;

import com.garethevans.church.opensongtablet.StaticVariables;

public class AppActionBar {

    public void setActionBar(TextView title, TextView author, TextView key) {

        if (StaticVariables.mTitle != null) {
            title.setText(StaticVariables.mTitle);
        }
        if (StaticVariables.mAuthor != null && !StaticVariables.mAuthor.isEmpty()) {
            author.setText(StaticVariables.mAuthor);
            author.setVisibility(View.VISIBLE);
        } else {
            author.setVisibility(View.GONE);
        }

        if (StaticVariables.mKey != null && !StaticVariables.mKey.isEmpty()) {
            String k = " (" + StaticVariables.mKey + ")";
            key.setText(k);
            key.setVisibility(View.VISIBLE);
        } else {
            key.setVisibility(View.GONE);
        }
    }

    public void setActionBarCapo(TextView capo, String string) {
        capo.setText(string);
    }
}

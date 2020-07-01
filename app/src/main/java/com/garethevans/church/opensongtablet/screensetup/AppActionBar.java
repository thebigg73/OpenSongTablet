package com.garethevans.church.opensongtablet.screensetup;

import android.app.ActionBar;
import android.view.View;
import android.widget.TextView;

import com.garethevans.church.opensongtablet.StaticVariables;

public class AppActionBar {

    public void setActionBar(TextView title, TextView author, TextView key) {

        if (title!=null && StaticVariables.mTitle != null) {
            title.setText(StaticVariables.mTitle);
        }
        if (author!=null && StaticVariables.mAuthor != null && !StaticVariables.mAuthor.isEmpty()) {
            author.setText(StaticVariables.mAuthor);
            author.setVisibility(View.VISIBLE);
        } else if (author!=null){
            author.setVisibility(View.GONE);
        }

        if (key!=null && StaticVariables.mKey != null && !StaticVariables.mKey.isEmpty()) {
            String k = " (" + StaticVariables.mKey + ")";
            key.setText(k);
            key.setVisibility(View.VISIBLE);
        } else if (key!=null){
            key.setVisibility(View.GONE);
        }
    }

    public void setActionBarCapo(TextView capo, String string) {
        capo.setText(string);
    }
}

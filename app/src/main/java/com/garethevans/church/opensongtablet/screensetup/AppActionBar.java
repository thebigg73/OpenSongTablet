package com.garethevans.church.opensongtablet.screensetup;

import android.view.View;
import android.widget.TextView;

import com.garethevans.church.opensongtablet.songprocessing.Song;

public class AppActionBar {

    public void setActionBar(TextView title, TextView author, TextView key, Song song, String newtitle) {

        if (song != null) {
            // We are in the Performance/Stage mode
            if (title != null && song.getTitle() != null) {
                title.setText(song.getTitle());
            }
            if (author != null && song.getAuthor() != null && !song.getAuthor().isEmpty()) {
                author.setText(song.getAuthor());
                hideView(author, false);
            } else {
                hideView(author, true);
            }
            if (key != null && song.getKey() != null && !song.getKey().isEmpty()) {
                String k = " (" + song.getKey() + ")";
                key.setText(k);
                hideView(key, false);
            } else {
                hideView(key, true);
            }
        } else if (newtitle !=null ){
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

package com.garethevans.church.opensongtablet;

@SuppressWarnings("unused")
public class _CODESNIPPETS {
    //static String[] songHTML;

    // HTML and WebView
    //LinearLayout webframe;

    //AsyncTask<Object, Void, String> preparesonghtml_async;

    //@SuppressLint("SetJavaScriptEnabled")
/*
    public void setUpViews() {

        // Go through each section and create the WebViews
        //webframe.removeAllViews();
        for (String string : songHTML) {
            final WebView myView = new WebView(this);
            myView.setVisibility(View.INVISIBLE);
            myView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            myView.setBackgroundColor(0xff000000);
            myView.setWebViewClient(new WebViewClient());
            myView.getSettings().setAppCacheEnabled(false);
            myView.getSettings().setJavaScriptEnabled(true);
            myView.getSettings().setDomStorageEnabled(false);
            myView.getSettings().setDatabaseEnabled(false);
            myView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            myView.getSettings().setSupportZoom(false);
            //myView.setFocusable(true);
            myView.getSettings().setDefaultFontSize(18);
            //myView.setFocusableInTouchMode(true);
            //myView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            //myView.setScrollbarFadingEnabled(false);
            //myView.getSettings().setBuiltInZoomControls(true);
            //myView.getSettings().setDisplayZoomControls(false);
            //myView.getSettings().setLoadWithOverviewMode(true);
            //myView.getSettings().setUseWideViewPort(true);
            //myView.getSettings().setTextZoom(100);
            myView.getSettings().setJavaScriptEnabled(true);
            myView.getSettings().setSupportZoom( true );
            myView.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR)
            //myView.setMinimumHeight(50);
            myView.loadDataWithBaseURL("file:///android_asset/", string, "text/html", "utf-8", null);


            //myView.setText(string);
            webframe.addView(myView);
            myView.setVisibility(View.VISIBLE);

            ImageView newLine = new ImageView(this);
            newLine.setImageResource(R.drawable.grey_line);
            newLine.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            webframe.addView(newLine);

        }

        TextView mytext = new TextView(this);
        mytext.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        mytext.setText(songHTML[0]);
        mytext.setBackgroundColor(0xffffffff);
        mytext.setTextColor(0xff000000);
        mytext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSongTEST();
            }
        });
        webframe.addView(mytext);
    }
*/

    //public void prepareHTML() {
        //preparesonghtml_async = new PrepareSongHTML();
        //preparesonghtml_async.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    }

    /*

    private class PrepareSongHTML extends AsyncTask<Object, Void, String> {
        @Override
        protected String doInBackground(Object... params) {
            // Go through each line and create the HTML
            // This gets stored in an array
            songHTML = new String[songSections.length];
            for (int x = 0; x < songSections.length; x++) {
                String mHTML = "<div class=\"heading\">" + ProcessSong.beautifyHeadings(songSectionsLabels[x]).toString() + "</div>\n";
                for (int y = 0; y < sectionContents[x].length; y++) {
                    // If this is a chord line followed by a lyric line.
                    if (y < sectionLineTypes[x].length - 1 && sectionLineTypes[x][y].equals("chord") && (sectionLineTypes[x][y + 1].equals("lyric") || sectionLineTypes[x][y + 1].equals("comment"))) {
                        // Check the length of the lyric line at least matches the chord line length
                        if (sectionContents[x][y].length() > sectionContents[x][y + 1].length()) {
                            sectionContents[x][y + 1] = ProcessSong.fixLineLength(sectionContents[x][y + 1], sectionContents[x][y].length());
                        }
                        String[] positions_returned = ProcessSong.getChordPositions(sectionContents[x][y]);
                        String[] chords_returned = ProcessSong.getChordSections(sectionContents[x][y], positions_returned);
                        String[] lyrics_returned = ProcessSong.getLyricSections(sectionContents[x][y + 1], positions_returned);
                        mHTML += "<table class=\"lyrictable\">\n";
                        mHTML += "<tr>" + ProcessSong.chordlinetoHTML(chords_returned) + "</tr>\n";
                        mHTML += "<tr>" + ProcessSong.lyriclinetoHTML(lyrics_returned) + "</tr>\n";
                        mHTML += "</table>\n";
                    }
                }
                songHTML[x] = ProcessSong.songHTML(mHTML);
            }
            return songHTML[0];
        }

        protected void onPostExecute(String s) {
            setUpViews();
        }
    }

*/


}
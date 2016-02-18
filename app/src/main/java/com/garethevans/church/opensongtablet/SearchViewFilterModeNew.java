package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Shows a list that can be filtered in-place with a SearchView in non-iconified mode.
 */

public class SearchViewFilterModeNew extends Activity implements SearchView.OnQueryTextListener {

    private SearchView mSearchView;
    private ListView mListView;
    @SuppressWarnings("unused")
    private Menu menu;

    // Vibrate to indicate something has happened
    Vibrator vb;

    SearchViewAdapter adapter;
    public final ArrayList<String> mFileName = new ArrayList<>();
    public final ArrayList<String> mFolder = new ArrayList<>();
    public final ArrayList<String> mTitle = new ArrayList<>();
    public final ArrayList<String> mAuthor = new ArrayList<>();
    public final ArrayList<String> mShortLyrics = new ArrayList<>();
    public final ArrayList<String> mTheme = new ArrayList<>();
    public final ArrayList<String> mKey = new ArrayList<>();
    public final ArrayList<String> mHymnNumber = new ArrayList<>();
    ArrayList<SearchViewItems> searchlist = new ArrayList<>();

    public static List<Map<String, String>> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

        setContentView(R.layout.searchview_filter);

        vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mSearchView = (SearchView) findViewById(R.id.search_view);
        mListView = (ListView) findViewById(R.id.list_view);

        // Add locale sort
        Collator coll = Collator.getInstance(FullscreenActivity.locale);
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(FullscreenActivity.search_database, coll);

        // Copy the full search string, now it is sorted, into a song and folder array
        mFileName.clear();
        mFolder.clear();
        mTitle.clear();
        mAuthor.clear();
        mShortLyrics.clear();
        mTheme.clear();
        mKey.clear();
        mHymnNumber.clear();


        for (int d=0;d<FullscreenActivity.search_database.size();d++) {
            String[] songbits = FullscreenActivity.search_database.get(d).split("_%%%_");
            mFileName.add(d, songbits[0].trim());
            mFolder.add(d, songbits[1].trim());
            mTitle.add(d, songbits[2].trim());
            mAuthor.add(d, songbits[3].trim());
            mShortLyrics.add(d, songbits[4].trim());
            mTheme.add(d, songbits[5].trim());
            mKey.add(d, songbits[6].trim());
            mHymnNumber.add(d,songbits[7].trim());
        }

        mListView.setTextFilterEnabled(true);
        mListView.setFastScrollEnabled(true);
        setupSearchView();


        for (int i = 0; i < FullscreenActivity.search_database.size(); i++) {
            SearchViewItems song = new SearchViewItems(mTitle.get(i) , mFolder.get(i), mAuthor.get(i), mKey.get(i), mTheme.get(i), mShortLyrics.get(i), mHymnNumber.get(i));
            searchlist.add(song);
        }

        adapter = new SearchViewAdapter(getApplicationContext(), searchlist );
        mListView.setAdapter(adapter);
        mListView.setTextFilterEnabled(true);
        mListView.setFastScrollEnabled(true);
        setupSearchView();

        mSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_actions, menu);
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.chordie_websearch:
                String thissearch = mSearchView.getQuery().toString();
                Intent intent = new Intent();
                intent.setClass(SearchViewFilterModeNew.this, Chordie.class);
                intent.putExtra("thissearch", thissearch);
                intent.putExtra("engine", "chordie");
                startActivity(intent);
                finish();
                return true;

            case R.id.ultimateguitar_websearch:
                String thissearch2 = mSearchView.getQuery().toString();
                Intent intent2 = new Intent();
                intent2.setClass(SearchViewFilterModeNew.this, Chordie.class);
                intent2.putExtra("thissearch", thissearch2);
                intent2.putExtra("engine", "ultimate-guitar");
                startActivity(intent2);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent viewsong = new Intent(this, FullscreenActivity.class);
        startActivity(viewsong);
        finish();
    }

    private void setupSearchView() {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQueryHint(getResources().getText(R.string.search_here).toString());
        mListView.setOnItemClickListener(new SongClickListener());
        mListView.setOnItemLongClickListener(new SongLongClickListener());
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        adapter.getFilter().filter(newText);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        mListView.requestFocus();
        return true;
    }

    private class SongClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // Vibrate to indicate that something has happened.
            vb.vibrate(25);

            TextView mFilename = (TextView) view.findViewById(R.id.cardview_songtitle);
            TextView mFoldername = (TextView) view.findViewById(R.id.cardview_folder);
            String tsong = mFilename.getText().toString();
            String tfolder = mFoldername.getText().toString();

            Editor editor = FullscreenActivity.myPreferences.edit();
            editor.putString("songfilename",tsong);
            editor.putString("whichSongFolder",tfolder);
            editor.apply();
            Intent viewsong2;
            if (FullscreenActivity.whattodo.equals("presentermodesearchreturn")) {
                FullscreenActivity.whattodo = "";
                viewsong2 = new Intent(SearchViewFilterModeNew.this, PresenterMode.class);
            } else {
                viewsong2 = new Intent(SearchViewFilterModeNew.this, FullscreenActivity.class);
            }
            startActivity(viewsong2);
            FullscreenActivity.setView = "N";
            finish();

        }
    }

    // This listener listens for long clicks in the song menu
    private class SongLongClickListener implements
            ListView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            // Each song is saved in the set string as $**_Love everlasting_**$
            // Vibrate to indicate that something has happened.
            vb.vibrate(50);

            TextView mFilename = (TextView) view.findViewById(R.id.cardview_songtitle);
            TextView mFoldername = (TextView) view.findViewById(R.id.cardview_folder);
            String tsong = mFilename.getText().toString();
            String tfolder = mFoldername.getText().toString();

            // We need to figure out the file name and the folder (if any) it is in
            if (tfolder.equals(FullscreenActivity.mainfoldername) || tfolder.equals("")) {
                FullscreenActivity.whatsongforsetwork = "$**_" + tsong + "_**$";
            } else {
                FullscreenActivity.whatsongforsetwork = "$**_" + tfolder + "/"	+ tsong + "_**$";
            }

            // Allow the song to be added, even if it is already there
            FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;
            // Tell the user that the song has been added.
            Toast toast = Toast.makeText(SearchViewFilterModeNew.this, "\""+tsong+"\" "+getResources().getString(R.string.addedtoset), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            // Save the set and other preferences
            Preferences.savePreferences();

            Editor editor = FullscreenActivity.myPreferences.edit();
            editor.putString("songfilename",tsong);
            editor.putString("whichSongFolder",tfolder);
            editor.apply();

            Intent viewsong2;
            if (FullscreenActivity.whattodo.equals("presentermodesearchreturn")) {
                FullscreenActivity.whattodo = "";
                viewsong2 = new Intent(SearchViewFilterModeNew.this, PresenterMode.class);
            } else {
                viewsong2 = new Intent(SearchViewFilterModeNew.this, FullscreenActivity.class);
            }
            startActivity(viewsong2);
            FullscreenActivity.setView = "N";
            finish();
            return true;
        }
    }
}
package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows a list that can be filtered in-place with a SearchView in non-iconified mode.
 */
//public class SearchViewFilterMode extends Activity implements SearchView.OnQueryTextListener {
public class SearchViewFilterMode extends Activity implements SearchView.OnQueryTextListener {

	private SearchView mSearchView;
	private ListView mListView;
	@SuppressWarnings("unused")
	private Menu menu;

    // Vibrate to indicate something has happened
    Vibrator vb;

    //public ArrayAdapter<String> songlistadapter;
    public SimpleAdapter songlistadapter;

    private static String[] mStrings;
	private static String[] mTempStrings;

    public final ArrayList<String> mSongName = new ArrayList<>();
    public final ArrayList<String> mFolderName = new ArrayList<>();
    public static List<Map<String, String>> data;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

		setContentView(R.layout.searchview_filter);

        vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// Remove the first item as it is the folder name
		mTempStrings = FullscreenActivity.mSongFileNames;
		int templength=0;
		if (mTempStrings!=null) {
			templength=mTempStrings.length-1;
		}
		mStrings = new String[templength];
		for (int i = 0; i < templength; i++) {
			mStrings[i] = mTempStrings[i+1];
		}


		mSearchView = (SearchView) findViewById(R.id.search_view);
		mListView = (ListView) findViewById(R.id.list_view);

        // Add locale sort
        Collator coll = Collator.getInstance(FullscreenActivity.locale);
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(FullscreenActivity.allfilesforsearch, coll);

		// Copy the full search string, now it is sorted, into a song and folder array
        mSongName.clear();
        mFolderName.clear();

        for (int d=0;d<FullscreenActivity.allfilesforsearch.size();d++) {
            String[] songbits = FullscreenActivity.allfilesforsearch.get(d).split(" %%% ");
            mSongName.add(d,songbits[0]);
            mFolderName.add(d,songbits[1]);
        }

        data = new ArrayList<>();

        for(int i = 0; i < mSongName.size(); i++)
        {
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put("mSongName", mSongName.get(i));
            datum.put("mFolderName", mFolderName.get(i));
            data.add(datum);
        }

        //mListView.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_2,mStrings));

        songlistadapter = new SimpleAdapter(SearchViewFilterMode.this, data, android.R.layout.simple_list_item_2, new String[] {"mSongName", "mFolderName"}, new int[] {android.R.id.text1, android.R.id.text2});
        //songlistadapter = new SimpleCursorAdapter(SearchViewFilterMode.this, android.R.layout.simple_list_item_2, new String[] {"mSongName", "mFolderName"}, new int[] {android.R.id.text1, android.R.id.text2});

/*        songlistadapter = new ArrayAdapter<String>(SearchViewFilterMode.this, android.R.layout.simple_list_item_2, android.R.id.text1, data) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
				TextView text1 = (TextView) view.findViewById(android.R.id.text1);
				TextView text2 = (TextView) view.findViewById(android.R.id.text2);
				text1.setTextColor(FullscreenActivity.lyricsTextColor);
				text2.setTextColor(FullscreenActivity.lyricsChordsColor);

                //text1.setText(data.);
                //text2.setText(songbits[1]);
				return view;
			}
		};*/


        mListView.setAdapter(songlistadapter);
        mListView.setTextFilterEnabled(true);
		mListView.setFastScrollEnabled(true);

		setupSearchView();
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
			intent.setClass(SearchViewFilterMode.this, Chordie.class);
			intent.putExtra("thissearch", thissearch);
			intent.putExtra("engine", "chordie");
			startActivity(intent);
			finish();
			return true;

		case R.id.ultimateguitar_websearch:
			String thissearch2 = mSearchView.getQuery().toString();
			Intent intent2 = new Intent();
			intent2.setClass(SearchViewFilterMode.this, Chordie.class);
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
        if (newText.contains(" ")) {
            // Vibrate to indicate that something has happened.
            vb.vibrate(80);
            newText = newText.replace(" ","");
            mSearchView.setQuery(newText,false);
            FullscreenActivity.myToastMessage = "Only single word searches are working for now";
            ShowToast.showToast(SearchViewFilterMode.this);
        } else {
            CharSequence cs = newText.toUpperCase();
            SearchViewFilterMode.this.songlistadapter.getFilter().filter(cs);
        }
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {

/*		if (mListView.getCount() == 0) {
			// finish();
			Intent viewsong = new Intent(this, FullscreenActivity.class);
			startActivity(viewsong);
			finish();
			return false;
		} else {
			String topitem = (String) mListView.getItemAtPosition(0);
			Editor editor = FullscreenActivity.myPreferences.edit();
			editor.putString("songfilename",topitem);
			editor.apply();
			Intent viewsong = new Intent(this, FullscreenActivity.class);
			startActivity(viewsong);
			FullscreenActivity.setView = "N";
			Toast.makeText(getApplicationContext(), getResources().getText(R.string.search_top).toString()+" - "+topitem, Toast.LENGTH_SHORT).show();
			finish();
			return false;
		}*/
        return true;
	}

	private class SongClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Map<String, String> map = (Map<String, String>) songlistadapter.getItem(position);
            String tsong = map.get("mSongName");
            String tfolder = map.get("mFolderName");

            // Vibrate to indicate that something has happened.
            vb.vibrate(50);

            Editor editor = FullscreenActivity.myPreferences.edit();
			editor.putString("songfilename",tsong);
            editor.putString("whichSongFolder",tfolder);
            editor.apply();
			Intent viewsong2 = new Intent(SearchViewFilterMode.this, FullscreenActivity.class);
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

            Map<String, String> map = (Map<String, String>) songlistadapter.getItem(position);
            String tsong = map.get("mSongName");
            String tfolder = map.get("mFolderName");

            // Vibrate to indicate that something has happened.
            vb.vibrate(50);

            // We need to figure out the file name and the folder (if any) it is in
            if (tfolder.equals(FullscreenActivity.mainfoldername)) {
                FullscreenActivity.whatsongforsetwork = "$**_" + tsong + "_**$";
            } else {
                FullscreenActivity.whatsongforsetwork = "$**_" + tfolder + "/"	+ tsong + "_**$";
            }

            // Allow the song to be added, even if it is already there
            FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;
            // Tell the user that the song has been added.
            Toast toast = Toast.makeText(SearchViewFilterMode.this, "\""+tsong+"\" "+getResources().getString(R.string.addedtoset), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            // Save the set and other preferences
            Preferences.savePreferences();

            Editor editor = FullscreenActivity.myPreferences.edit();
            editor.putString("songfilename",tsong);
            editor.putString("whichSongFolder",tfolder);
            editor.apply();
            Intent viewsong2 = new Intent(SearchViewFilterMode.this, FullscreenActivity.class);
            startActivity(viewsong2);
            FullscreenActivity.setView = "N";
            finish();
            return true;
		}
	}
}
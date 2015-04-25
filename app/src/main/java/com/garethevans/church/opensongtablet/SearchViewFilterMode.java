package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Shows a list that can be filtered in-place with a SearchView in non-iconified mode.
 */
public class SearchViewFilterMode extends Activity implements SearchView.OnQueryTextListener {

	private SearchView mSearchView;
	private ListView mListView;
	@SuppressWarnings("unused")
	private Menu menu;

	private static String[] mStrings;
	private static String[] mTempStrings;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_ACTION_BAR);

		setContentView(R.layout.searchview_filter);


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
		mListView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1,
				mStrings));


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
		return;
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
	public boolean onQueryTextChange(String newText){
		if (TextUtils.isEmpty(newText)) {
			mListView.clearTextFilter();
		} else {
			mListView.setFilterText(newText);
		}
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {

		if (mListView.getCount() == 0) {
			// finish();
			Intent viewsong = new Intent(this, FullscreenActivity.class);
			startActivity(viewsong);
			finish();
			return false;
		} else {
			String topitem = (String) mListView.getItemAtPosition(0);
			Editor editor = FullscreenActivity.myPreferences.edit();
			editor.putString("songfilename",topitem);
			editor.commit();
			Intent viewsong = new Intent(this, FullscreenActivity.class);
			startActivity(viewsong);
			FullscreenActivity.setView = "N";
			Toast.makeText(getApplicationContext(), getResources().getText(R.string.search_top).toString()+" - "+topitem, Toast.LENGTH_SHORT).show();
			finish();
			return false;
		}
	}



	private class SongClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			//String query = (String) ((TextView) view).getText();

			String query = (String) mListView.getItemAtPosition(position);

			Editor editor = FullscreenActivity.myPreferences.edit();
			editor.putString("songfilename",query);
			editor.commit();
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
			//String linkclicked = (String) ((TextView) view).getText();

			String linkclicked = (String) mListView.getItemAtPosition(position);


			// Each song is saved in the set string as $**_Love everlasting_**$
			// Check if this song is already there. If it isn't, add it.
			// We need to figure out the file name and the folder (if any) it is in
			if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername) || FullscreenActivity.whichSongFolder.isEmpty()) {
				FullscreenActivity.whatsongforsetwork = "$**_" + linkclicked + "_**$";
			} else {
				FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.whichSongFolder + "/"	+ linkclicked + "_**$";
			}
			// Allow the song to be added, even if it is already there
			FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;
			// Tell the user that the song has been added.
			Toast toast = Toast.makeText(SearchViewFilterMode.this, "\""+linkclicked+"\" "+getResources().getString(R.string.addedtoset), Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();

			// Save the set and other preferences
			Preferences.savePreferences();

			String query = (String) ((TextView) view).getText();
			Editor editor = FullscreenActivity.myPreferences.edit();
			editor.putString("songfilename",query);
			editor.commit();
			Intent viewsong2 = new Intent(SearchViewFilterMode.this, FullscreenActivity.class);
			startActivity(viewsong2);
			FullscreenActivity.setView = "N";
			finish();
			return true;

		}
	}



}
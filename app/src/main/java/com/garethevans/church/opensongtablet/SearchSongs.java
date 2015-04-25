package com.garethevans.church.opensongtablet;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

public class SearchSongs extends ListActivity { 
	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState); 
		handleIntent(getIntent()); 
	} 

	@Override
	public void onNewIntent(Intent intent) { 
		setIntent(intent); 
		handleIntent(intent); 
	} 

	@Override
	public void onListItemClick(ListView l, 
			View v, int position, long id) { 
		// call detail activity for clicked entry 
	} 

	private void handleIntent(Intent intent) { 
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) { 
			String query = 
					intent.getStringExtra(SearchManager.QUERY); 
			doSearch(query); 
		} 
	}    

	private void doSearch(String queryStr) { 
		// get a Cursor, prepare the ListAdapter
		// and set it

	} 
}
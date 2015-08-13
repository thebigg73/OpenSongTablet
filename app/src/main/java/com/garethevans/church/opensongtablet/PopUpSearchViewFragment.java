package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

public class PopUpSearchViewFragment extends DialogFragment implements SearchView.OnQueryTextListener {

    private SearchView mSearchView;
    private ListView mListView;

    static PopUpSearchViewFragment newInstance() {
        PopUpSearchViewFragment frag;
        frag = new PopUpSearchViewFragment();
        return frag;
    }

    public interface MyInterface {
        void searchResults();
    }

    public interface MyVibrator {
        void doVibrate();
    }

    private MyInterface mListener;
    private MyVibrator mVibrator;

    @Override
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        mVibrator = (MyVibrator) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.action_search));
        View V = inflater.inflate(R.layout.searchview_filter, container, false);

        // Initialise the views
        mSearchView = (SearchView) V.findViewById(R.id.search_view);
        mListView = (ListView) V.findViewById(R.id.list_view);

        // Remove the first item as it is the folder name
        String[] mTempStrings = FullscreenActivity.mSongFileNames;
        int templength=0;
        if (mTempStrings !=null) {
            templength= mTempStrings.length-1;
        }

        String[] mStrings = new String[templength];
        if (mTempStrings != null) {
            System.arraycopy(mTempStrings, 1, mStrings, 0, templength);
        }

        mListView.setAdapter(new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1,
                mStrings));

        mListView.setTextFilterEnabled(true);
        mListView.setFastScrollEnabled(true);

        setupSearchView();

        return V;
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
            dismiss();
            return false;

        } else {
            FullscreenActivity.songfilename = mListView.getItemAtPosition(0).toString();
            FullscreenActivity.setView = "N";
            FullscreenActivity.myToastMessage = FullscreenActivity.songfilename;
            //Save preferences
            Preferences.savePreferences();
            // Vibrate to indicate something has happened
            mVibrator.doVibrate();
            mListener.searchResults();
            dismiss();
            return false;
        }
    }

    private class SongClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String query = (String) mListView.getItemAtPosition(position);
            FullscreenActivity.songfilename = query;
            FullscreenActivity.setView = "N";
            FullscreenActivity.myToastMessage = query;
            //Save preferences
            Preferences.savePreferences();
            // Vibrate to indicate something has happened
            mVibrator.doVibrate();
            mListener.searchResults();
            dismiss();
        }
    }


    // This listener listens for long clicks in the song menu
    private class SongLongClickListener implements
            ListView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            String linkclicked = (String) mListView.getItemAtPosition(position);
            FullscreenActivity.songfilename = linkclicked;

            // Each song is saved in the set string as $**_Love everlasting_**$
            // Check if this song is already there. If it isn't, add it.
            // We need to figure out the file name and the folder (if any) it is in
            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                FullscreenActivity.whatsongforsetwork = "$**_" + linkclicked + "_**$";
            } else {
                FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.whichSongFolder + "/"	+ linkclicked + "_**$";
            }
            // Allow the song to be added, even if it is already there
            FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;

            // Save the set and other preferences
            Preferences.savePreferences();

            //String query = (String) ((TextView) view).getText();
            FullscreenActivity.setView = "Y";
            //Save preferences
            Preferences.savePreferences();
            // Vibrate to indicate something has happened
            mVibrator.doVibrate();
            FullscreenActivity.myToastMessage = "\"" + linkclicked + "\" " + getResources().getString(R.string.addedtoset);
            mListener.searchResults();
            dismiss();
            return true;
        }
    }

}
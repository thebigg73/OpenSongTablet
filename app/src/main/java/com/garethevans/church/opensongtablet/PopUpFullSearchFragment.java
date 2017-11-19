package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PopUpFullSearchFragment extends DialogFragment implements SearchView.OnQueryTextListener {

    private SearchView mSearchView;
    private ListView mListView;

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

    public interface MyInterface {
        void loadSong();
        void songLongClick();
    }

    public static PopUpFullSearchFragment newInstance () {
        PopUpFullSearchFragment frag;
        frag = new PopUpFullSearchFragment();
        return frag;
    }

    private MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (MyInterface) activity;
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity() != null && getDialog() != null) {
            PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);
        View V = inflater.inflate(R.layout.popup_searchview, container, false);

        TextView title = V.findViewById(R.id.dialogtitle);
        title.setText(getActivity().getResources().getString(R.string.action_search));
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe,getActivity());
                closeMe.setEnabled(false);
                dismiss();
            }
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setVisibility(View.GONE);

        super.onCreate(savedInstanceState);

        mSearchView = V.findViewById(R.id.search_view);
        mListView = V.findViewById(R.id.list_view);
        mSearchView.requestFocus();
        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
        // Decide if we are using full blown search or a simplified one
        if (FullscreenActivity.safetosearch) {
            Fullsearch();
        } else {
            Simplesearch();
        }

        return V;
    }

    public void Simplesearch() {
        try {
            // This gets called if the database wasn't built properly
            // Tell the user there was a problem
            FullscreenActivity.myToastMessage = getResources().getString(R.string.search_index_error);
            ShowToast.showToast(getActivity());

            // Convert the list of folder/files into a database sorted by filenames
            ArrayList<String> filesnfolders = new ArrayList<>();
            for (String foldernfile : FullscreenActivity.allfilesforsearch) {
                String[] file_split = foldernfile.split("/");
                String filename;
                String foldername;

                try {
                    filename = file_split[1];
                } catch (Exception e) {
                    filename = "";
                }

                try {
                    foldername = file_split[0];
                } catch (Exception e) {
                    foldername = "";
                }

                if (foldername.equals("")) {
                    foldername = FullscreenActivity.mainfoldername;
                }

                filesnfolders.add(filename + " _%%%_ " + foldername);
            }

            Collator coll = Collator.getInstance(FullscreenActivity.locale);
            coll.setStrength(Collator.SECONDARY);
            Collections.sort(filesnfolders, coll);

            // Copy the full search string, now it is sorted, into a song and folder array
            mFileName.clear();
            mFolder.clear();
            mTitle.clear();
            mAuthor.clear();
            mShortLyrics.clear();
            mTheme.clear();
            mKey.clear();
            mHymnNumber.clear();

            for (int d = 0; d < filesnfolders.size(); d++) {
                String[] songbits = filesnfolders.get(d).split("_%%%_");
                String filename = songbits[0].trim();
                String foldername = songbits[1].trim();
                String lyricstoadd = filename + " " + foldername;

                // Replace unwanted symbols
                lyricstoadd = ProcessSong.removeUnwantedSymbolsAndSpaces(lyricstoadd);

                mFileName.add(d, filename);
                mFolder.add(d, foldername);
                mTitle.add(d, filename);
                mAuthor.add(d, "");
                mShortLyrics.add(d, lyricstoadd);
                mTheme.add(d, "");
                mKey.add(d, "");
                mHymnNumber.add(d, "");
            }

            mListView.setTextFilterEnabled(true);
            mListView.setFastScrollEnabled(true);
            setupSearchView();

            for (int i = 0; i < filesnfolders.size(); i++) {
                SearchViewItems song = new SearchViewItems(mFileName.get(i), mTitle.get(i), mFolder.get(i), mAuthor.get(i), mKey.get(i), mTheme.get(i), mShortLyrics.get(i), mHymnNumber.get(i));
                searchlist.add(song);
            }

            adapter = new SearchViewAdapter(getActivity().getApplicationContext(), searchlist, "search");
            mListView.setAdapter(adapter);
            mListView.setTextFilterEnabled(true);
            mListView.setFastScrollEnabled(true);
            setupSearchView();

            mSearchView.setOnQueryTextListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void Fullsearch() {
        // Add locale sort
        try {
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


            for (int d = 0; d < FullscreenActivity.search_database.size(); d++) {
                String[] songbits = FullscreenActivity.search_database.get(d).split("_%%%_");
                mFileName.add(d, songbits[0].trim());
                mFolder.add(d, songbits[1].trim());
                mTitle.add(d, songbits[2].trim());
                mAuthor.add(d, songbits[3].trim());
                mShortLyrics.add(d, songbits[4].trim());
                mTheme.add(d, songbits[5].trim());
                mKey.add(d, songbits[6].trim());
                mHymnNumber.add(d, songbits[7].trim());
            }

            mListView.setTextFilterEnabled(true);
            mListView.setFastScrollEnabled(true);
            setupSearchView();


            for (int i = 0; i < FullscreenActivity.search_database.size(); i++) {
                SearchViewItems song = new SearchViewItems(mFileName.get(i), mTitle.get(i), mFolder.get(i), mAuthor.get(i), mKey.get(i), mTheme.get(i), mShortLyrics.get(i), mHymnNumber.get(i));
                searchlist.add(song);
            }

            adapter = new SearchViewAdapter(getActivity().getApplicationContext(), searchlist, "search");
            mListView.setAdapter(adapter);
            mListView.setTextFilterEnabled(true);
            mListView.setFastScrollEnabled(true);
            setupSearchView();

            mSearchView.setOnQueryTextListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onQueryTextSubmit(String newText) {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
        mListView.requestFocus();
        if (mListView.getCount() == 0) {
            dismiss();
            return false;

        } else {
            SearchViewItems item = (SearchViewItems) adapter.getItem(0);
            FullscreenActivity.songfilename = item.getFilename();
            FullscreenActivity.whichSongFolder = item.getFolder();
            FullscreenActivity.setView = false;
            FullscreenActivity.myToastMessage = FullscreenActivity.songfilename;
            //Save preferences
            Preferences.savePreferences();
            // Vibrate to indicate something has happened
            DoVibrate.vibrate(getActivity(),50);
            if (mListener!=null) {
                mListener.songLongClick();
                mListener.loadSong();
            }
            dismiss();
            return true;
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        // Replace unwanted symbols
        if (newText==null) {
            newText="";
        }
        newText = ProcessSong.removeUnwantedSymbolsAndSpaces(newText);
        if (adapter!=null) {
            adapter.getFilter().filter(newText);
        }
        return false;
    }

    private void setupSearchView() {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setQueryHint(getResources().getText(R.string.search_here).toString());
        mListView.setOnItemClickListener(new SongClickListener());
        mListView.setOnItemLongClickListener(new SongLongClickListener());
    }

    private class SongClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            // Vibrate to indicate that something has happened.
            DoVibrate.vibrate(getActivity(),50);

            TextView mFilename = view.findViewById(R.id.cardview_filename);
            TextView mFoldername = view.findViewById(R.id.cardview_folder);
            FullscreenActivity.songfilename = mFilename.getText().toString();
            FullscreenActivity.whichSongFolder = mFoldername.getText().toString();
            Preferences.savePreferences();
            FullscreenActivity.setView = false;
            if (mListener!=null) {
                mListener.loadSong();
            }
            dismiss();
        }
    }

    // This listener listens for long clicks in the song menu
    private class SongLongClickListener implements
            ListView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            // Each song is saved in the set string as $**_Love everlasting_**$
            // Vibrate to indicate that something has happened.
            DoVibrate.vibrate(getActivity(),50);

            TextView mFilename = view.findViewById(R.id.cardview_filename);
            TextView mFoldername = view.findViewById(R.id.cardview_folder);
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
            FullscreenActivity.myToastMessage = "\"" + tsong + "\" " +getResources().getString(R.string.addedtoset);
            ShowToast.showToast(getActivity());

            // Save the set and other preferences
            Preferences.savePreferences();

            if (mListener!=null) {
                mListener.songLongClick();
            }
                return true;
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

}

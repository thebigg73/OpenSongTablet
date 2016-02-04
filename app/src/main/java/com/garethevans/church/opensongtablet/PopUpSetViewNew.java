package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PopUpSetViewNew extends DialogFragment {

    static PopUpSetViewNew newInstance() {
        PopUpSetViewNew frag;
        frag = new PopUpSetViewNew();
        return frag;
    }

    public interface MyInterface {
        void loadSongFromSet();
        void shuffleSongsInSet();
        void refreshAll();
    }

    private static MyInterface mListener;

    @Override
    public void onAttach(Activity activity) {
        mListener = (MyInterface) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    public static ArrayList<String> mSongName = new ArrayList<>();
    public static ArrayList<String> mFolderName = new ArrayList<>();
    static RecyclerView mRecyclerView;

    static ItemTouchHelper.Callback callback;
    static ItemTouchHelper helper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.options_set));
        final View V = inflater.inflate(R.layout.popup_setview_new, container, false);

        mRecyclerView = (RecyclerView) V.findViewById(R.id.my_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(llm);

        // Grab the saved set list array and put it into a list
        // This way we work with a temporary version

        if (FullscreenActivity.doneshuffle && FullscreenActivity.mTempSetList!=null && FullscreenActivity.mTempSetList.size()>0) {
            Log.d("d","We've shuffled the set list");
        } else {
            FullscreenActivity.mTempSetList = new ArrayList<>();
            Collections.addAll(FullscreenActivity.mTempSetList, FullscreenActivity.mSetList);
        }

        extractSongsAndFolders();
        FullscreenActivity.doneshuffle = false;

        MyAdapter ma = new MyAdapter(createList(FullscreenActivity.mTempSetList.size()));
        mRecyclerView.setAdapter(ma);
        callback = new SetListItemTouchHelper(ma);
        helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecyclerView);

        Button cancel = (Button) V.findViewById(R.id.setview_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FullscreenActivity.mTempSetList = null;
                dismiss();
            }
        });

        Button save = (Button) V.findViewById(R.id.setview_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tempmySet = "";
                String tempItem;
                for (int z=0; z<FullscreenActivity.mTempSetList.size(); z++) {
                    tempItem = FullscreenActivity.mTempSetList.get(z);
                    tempmySet = tempmySet + "$**_"+ tempItem + "_**$";
                }
                FullscreenActivity.mySet = null;
                FullscreenActivity.mySet = tempmySet;
                FullscreenActivity.mTempSetList = null;
                Preferences.savePreferences();
                // Tell the listener to do something
                mListener.refreshAll();
                dismiss();
            }
        });

        ImageButton set_shuffle = (ImageButton) V.findViewById(R.id.shuffle);
        set_shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redraw the lists
                Collections.shuffle(FullscreenActivity.mTempSetList);

                // Prepare the page for redrawing....
                FullscreenActivity.doneshuffle = true;

                // Run the listener
                dismiss();
                mListener.shuffleSongsInSet();
            }
        });

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        return V;
    }

    @Override
    public void onResume() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        super.onResume();
    }

    public void extractSongsAndFolders() {
        // Populate the set list list view
        // Split the set items into song and folder
        mSongName = new ArrayList<>();
        mFolderName = new ArrayList<>();

        String tempTitle;
        if (FullscreenActivity.mTempSetList!=null && FullscreenActivity.mTempSetList.size()>0) {
            for (int i = 0; i < FullscreenActivity.mTempSetList.size(); i++) {
                if (!FullscreenActivity.mTempSetList.get(i).contains("/")) {
                    tempTitle = "/" + FullscreenActivity.mTempSetList.get(i);
                } else {
                    tempTitle = FullscreenActivity.mTempSetList.get(i);
                }
                String[] splitsongname = tempTitle.split("/");
                String mysongtitle = "";
                String mysongfolder = "";
                if (splitsongname.length > 1) {
                    // If works
                    mysongtitle = splitsongname[1];
                    mysongfolder = splitsongname[0];
                }

                if (mysongfolder.isEmpty() || mysongfolder.equals("")) {
                    mysongfolder = getResources().getString(R.string.mainfoldername);
                }

                if (mysongtitle.isEmpty() || mysongfolder.equals("")) {
                    mysongtitle = "!ERROR!";
                }

                mSongName.add(i, mysongtitle);
                mFolderName.add(i, mysongfolder);
            }
        }
    }

    private List<SetItemInfo> createList(int size) {
        Log.d("d", "size=" + size);

        List<SetItemInfo> result = new ArrayList<>();
        for (int i=1; i <= size; i++) {
            if (!mSongName.get(i - 1).equals("!ERROR!")) {
                SetItemInfo si = new SetItemInfo();
                si.songitem = i+".";
                si.songtitle = mSongName.get(i - 1);
                si.songfolder = mFolderName.get(i - 1);
                // Decide what image we'll need - song, image, note, slide, scripture
                if (mFolderName.get(i - 1).equals(FullscreenActivity.text_slide)) {
                    si.songicon = FullscreenActivity.text_slide;
                } else if (mFolderName.get(i - 1).equals(FullscreenActivity.text_note)) {
                    si.songicon = FullscreenActivity.text_note;
                } else if (mFolderName.get(i - 1).equals(FullscreenActivity.text_scripture)) {
                    si.songicon = FullscreenActivity.text_scripture;
                } else if (mFolderName.get(i - 1).equals(FullscreenActivity.image)) {
                    si.songicon = FullscreenActivity.image;
                } else if (mSongName.get(i - 1).contains(".pdf") || mSongName.get(i - 1).contains(".PDF")) {
                    si.songicon = ".pdf";
                } else {
                    si.songicon = FullscreenActivity.song;
                }
                result.add(si);
            }
        }
        return result;
    }

    public static void loadSong() {
        mListener.loadSongFromSet();
    }

}
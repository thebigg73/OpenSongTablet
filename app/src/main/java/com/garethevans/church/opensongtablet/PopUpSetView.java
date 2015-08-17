package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class PopUpSetView extends DialogFragment {

    static PopUpSetView newInstance() {
        PopUpSetView frag;
        frag = new PopUpSetView();
        return frag;
    }

    public interface MyInterface {
        void loadSongFromSet();
    }

    private MyInterface mListener;

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

    public Button popUpSetClose_Button;
    public ListView setList_ListView;

    public final ArrayList<String> mSongName = new ArrayList<>();
    public final ArrayList<String> mFolderName = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getActivity().getResources().getString(R.string.options_set));
        View V = inflater.inflate(R.layout.popup_setview, container, false);
        //V.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);

        // Initialise the views
        popUpSetClose_Button  = (Button) V.findViewById(R.id.popUpSetClose_Button);
        setList_ListView  = (ListView) V.findViewById(R.id.setList_ListView);
        setList_ListView.setBackgroundColor(FullscreenActivity.lyricsBackgroundColor);

        // Listen for close button
        popUpSetClose_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        // Populate the set list list view
        // Split the set items into song and folder
        String tempTitle;
        if (FullscreenActivity.mSetList!=null && FullscreenActivity.mSetList.length>0) {
            for (int i = 0; i < FullscreenActivity.mSetList.length; i++) {
                try {
                    // Just a check
                    // noinspection unchecked
                    String temp = FullscreenActivity.mSetList[i];
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                if (!FullscreenActivity.mSetList[i].contains("/")) {
                    tempTitle = "/" + FullscreenActivity.mSetList[i];
                } else {
                    tempTitle = FullscreenActivity.mSetList[i];
                }
                String[] splitsongname = tempTitle.split("/");
                String mysongtitle = "";
                String mysongfolder = "";
                if (splitsongname.length>1) {
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
                mFolderName.add(i,mysongfolder);
            }

            final ArrayList<String> mCurrentSetList = new ArrayList<>();
            Collections.addAll(mCurrentSetList, FullscreenActivity.mSetList);

            // Check colours to use
            final int normal_bg  = FullscreenActivity.lyricsVerseColor;
            final int notes_bg   = FullscreenActivity.lyricsChorusColor;
            final int normal_txt = FullscreenActivity.lyricsTextColor;
            final int notes_txt  = FullscreenActivity.lyricsChordsColor;
            ArrayAdapter adapter;
            //noinspection AndroidLintUnchecked
            adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_2, android.R.id.text1, mCurrentSetList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                    TextView text2 = (TextView) view.findViewById(android.R.id.text2);
                    text1.setTextColor(normal_txt);
                    view.setBackgroundColor(normal_bg);
                    text2.setTextColor(notes_txt);
                    try {
                        text1.setText(mSongName.get(position));
                        text2.setText(mFolderName.get(position));
                    } catch (Exception e) {
                        // Strange error
                        mSongName.add(position, "");
                        mFolderName.add(position,"");
                        text1.setText("error");
                        text2.setText("error");
                    }
                    if (mFolderName.get(position).equals(FullscreenActivity.text_slide) ||
                            mFolderName.get(position).equals(FullscreenActivity.text_note) ||
                            mFolderName.get(position).equals(FullscreenActivity.text_scripture)) {
                        view.setBackgroundColor(notes_bg);
                    }
                    return view;
                }
            };

            setList_ListView.setAdapter(adapter);

            // If set list is empty, remove this view
            if (mCurrentSetList.size()==1 && mSongName.get(0).equals("!ERROR!")) {
                setList_ListView.setVisibility(View.INVISIBLE);
            }
            setList_ListView.setFastScrollAlwaysVisible(true);
            setList_ListView.setFastScrollEnabled(true);
            setList_ListView.setSelection(FullscreenActivity.indexSongInSet);

            setList_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Fix folders for Scriptures, Notes and Slides
                    if (mFolderName.get(position).equals(getResources().getString(R.string.scripture))) {
                        FullscreenActivity.whichSongFolder = "../OpenSong Scripture";
                    } else if (mFolderName.get(position).equals(getResources().getString(R.string.slide))) {
                        FullscreenActivity.whichSongFolder = "../Slides/_cache";
                    } else if (mFolderName.get(position).equals(getResources().getString(R.string.note))) {
                        FullscreenActivity.whichSongFolder = "../Notes/_cache";
                    } else {
                        FullscreenActivity.whichSongFolder = mFolderName.get(position);
                    }
                    FullscreenActivity.songfilename = mSongName.get(position);
                    FullscreenActivity.indexSongInSet = position;
                    if (position == 0) {
                        FullscreenActivity.previousSongInSet = "";
                    } else {
                        FullscreenActivity.previousSongInSet = mCurrentSetList.get(position - 1);
                    }

                    if (position >= (mCurrentSetList.size() - 1)) {
                        FullscreenActivity.nextSongInSet = "";
                    } else {
                        FullscreenActivity.nextSongInSet = mCurrentSetList.get(position + 1);
                    }

                    mListener.loadSongFromSet();
                    dismiss();
                }
            });
        }
        return V;
    }

    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null) {
            return;
        }

        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

    }

}

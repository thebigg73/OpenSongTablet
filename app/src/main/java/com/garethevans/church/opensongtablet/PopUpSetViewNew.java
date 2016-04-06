package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
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
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
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

        TextView helpClickItem_TextView = (TextView) V.findViewById(R.id.helpClickItem_TextView);
        TextView helpDragItem_TextView = (TextView) V.findViewById(R.id.helpDragItem_TextView);
        TextView helpSwipeItem_TextView = (TextView) V.findViewById(R.id.helpSwipeItem_TextView);
        TextView helpVariationItem_TextView = (TextView) V.findViewById(R.id.helpVariationItem_TextView);
        helpVariationItem_TextView.setVisibility(View.GONE);
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

        ImageButton listSetTweetButton = (ImageButton) V.findViewById(R.id.listSetTweetButton);
        // Set up the Tweet button
        listSetTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doExportSetTweet();
            }
        });

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

        if (FullscreenActivity.whattodo.equals("setitemvariation")) {
            helpClickItem_TextView.setVisibility(View.GONE);
            helpDragItem_TextView.setVisibility(View.GONE);
            helpSwipeItem_TextView.setVisibility(View.GONE);
            listSetTweetButton.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
            set_shuffle.setVisibility(View.GONE);
            helpVariationItem_TextView.setVisibility(View.VISIBLE);
        }

        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }


        // Try to move to the corresponding item in the set that we are viewing.
        SetActions.indexSongInSet();

        // If the song is found (indexSongInSet>-1 and lower than the number of items shown), smooth scroll to it
        if (FullscreenActivity.indexSongInSet>-1 && FullscreenActivity.indexSongInSet<FullscreenActivity.mTempSetList.size()) {
            Log.d("d","position="+FullscreenActivity.indexSongInSet);
            mRecyclerView.smoothScrollToPosition(FullscreenActivity.indexSongInSet);
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
        List<SetItemInfo> result = new ArrayList<>();
        for (int i=1; i <= size; i++) {
            if (!mSongName.get(i - 1).equals("!ERROR!")) {
                SetItemInfo si = new SetItemInfo();
                si.songitem = i+".";
                si.songtitle = mSongName.get(i - 1);
                si.songfolder = mFolderName.get(i - 1);
                // Decide what image we'll need - song, image, note, slide, scripture, variation
                if (mFolderName.get(i - 1).equals("**"+FullscreenActivity.text_slide)) {
                    si.songicon = FullscreenActivity.text_slide;
                } else if (mFolderName.get(i - 1).equals("**"+FullscreenActivity.text_note)) {
                    si.songicon = FullscreenActivity.text_note;
                } else if (mFolderName.get(i - 1).equals("**"+FullscreenActivity.text_scripture)) {
                    si.songicon = FullscreenActivity.text_scripture;
                } else if (mFolderName.get(i - 1).equals("**"+FullscreenActivity.image)) {
                    si.songicon = FullscreenActivity.image;
                } else if (mFolderName.get(i - 1).equals("**"+FullscreenActivity.text_variation)) {
                    si.songicon = FullscreenActivity.text_variation;
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

    public static void makeVariation() {
        // Prepare the name of the new variation slide
        // If the file already exists, add _ to the filename
        String newfilename = FullscreenActivity.dirvariations + "/" + FullscreenActivity.songfilename;
        String newsongname = FullscreenActivity.songfilename;
        File newfile = new File(newfilename);
        while (newfile.exists()) {
            newfilename = newfilename + "_";
            newsongname = newsongname + "_";
            newfile = new File(newfilename);
        }

        // Original file
        File src;
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            src = new File(FullscreenActivity.dir + "/" + FullscreenActivity.songfilename);
        } else {
            src = new File(FullscreenActivity.dir + "/" + FullscreenActivity.whichSongFolder + "/" + FullscreenActivity.songfilename);
        }

        // Copy the file into the variations folder
        try {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(newfile);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fix the song name and folder for loading
        FullscreenActivity.songfilename = newsongname;
        FullscreenActivity.whichSongFolder = "../Variations";
        FullscreenActivity.whatsongforsetwork = "\"$**_**"+FullscreenActivity.text_variation+"/"+newsongname+"_**$";

        // Replace the set item with the variation item
        FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet] = "**"+FullscreenActivity.text_variation+"/"+newsongname;
        // Rebuild the mySet variable
        String new_mySet = "";
        for (String thisitem:FullscreenActivity.mSetList) {
            new_mySet = new_mySet + "$**_" + thisitem + "_**$";
        }
        FullscreenActivity.mySet = new_mySet;

        FullscreenActivity.myToastMessage = FullscreenActivity.variation_edit;
        // Now load the new variation item up
        loadSong();
    }

    public void doExportSetTweet() {
        // Add the set items
        String setcontents = "";

        for (String getItem:FullscreenActivity.mSetList) {
            int songtitlepos = getItem.indexOf("/")+1;
            getItem = getItem.substring(songtitlepos);
            setcontents = setcontents + getItem +", ";
        }

        setcontents = setcontents.substring(0,setcontents.length()-2);

        String tweet = setcontents;
        try {
            tweet = URLEncoder.encode("#OpenSongApp\n" + setcontents,"UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String tweetUrl = "https://twitter.com/intent/tweet?text=" + tweet;
        Uri uri = Uri.parse(tweetUrl);
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
    }
}
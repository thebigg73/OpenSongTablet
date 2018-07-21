package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
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

    public static Dialog setfrag;
    Context c;
    Activity a;
    boolean islongpressing = false;

    static PopUpSetViewNew newInstance() {
        PopUpSetViewNew frag;
        frag = new PopUpSetViewNew();
        return frag;
    }

    public interface MyInterface {
        void loadSongFromSet();
        void shuffleSongsInSet();
        void prepareOptionMenu();
        void refreshAll();
        void closePopUps();
        void pageButtonAlpha(String s);
        void windowFlags();
        void openFragment();
    }

    private static MyInterface mListener;

    @Override
    @SuppressWarnings("deprecation")
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
    RecyclerView mRecyclerView;

    static ItemTouchHelper.Callback callback;
    static ItemTouchHelper helper;
    FloatingActionButton saveMe;

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
        super.onCreateView(inflater,container,savedInstanceState);
        a = getActivity();
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        final View V = inflater.inflate(R.layout.popup_setview_new, container, false);
        setfrag = getDialog();
        TextView title = V.findViewById(R.id.dialogtitle);
        String titletext = getActivity().getResources().getString(R.string.options_set)+displaySetName();
        title.setText(titletext);
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomAnimations.animateFAB(closeMe, PopUpSetViewNew.this.getActivity());
                closeMe.setEnabled(false);
                PopUpSetViewNew.this.dismiss();
            }
        });
        saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopUpSetViewNew.this.doSave();
                refresh();
                close();
            }
        });
        if (FullscreenActivity.whattodo.equals("setitemvariation")) {
            CustomAnimations.animateFAB(saveMe,getActivity());
            saveMe.setEnabled(false);
            saveMe.setVisibility(View.GONE);
        }

        if (getDialog().getWindow()!=null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        if (mListener!=null) {
            mListener.pageButtonAlpha("set");
        }

        TextView helpClickItem_TextView = V.findViewById(R.id.helpClickItem_TextView);
        TextView helpDragItem_TextView = V.findViewById(R.id.helpDragItem_TextView);
        TextView helpSwipeItem_TextView = V.findViewById(R.id.helpSwipeItem_TextView);
        TextView helpVariationItem_TextView = V.findViewById(R.id.helpVariationItem_TextView);
        helpVariationItem_TextView.setVisibility(View.GONE);
        mRecyclerView = V.findViewById(R.id.my_recycler_view);
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

        MyAdapter ma = new MyAdapter(createList(FullscreenActivity.mTempSetList.size()),getActivity());
        mRecyclerView.setAdapter(ma);
        callback = new SetListItemTouchHelper(ma);
        helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecyclerView);

        FloatingActionButton listSetTweetButton = V.findViewById(R.id.listSetTweetButton);
        // Set up the Tweet button
        listSetTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopUpSetViewNew.this.doExportSetTweet();
            }
        });
        FloatingActionButton info = V.findViewById(R.id.info);
        final LinearLayout helptext = V.findViewById(R.id.helptext);
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (helptext.getVisibility() == View.VISIBLE) {
                    helptext.setVisibility(View.GONE);
                } else {
                    helptext.setVisibility(View.VISIBLE);
                }
            }
        });

        FloatingActionButton set_shuffle = V.findViewById(R.id.shuffle);
        set_shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save any changes to current set first
                doSave();

                if (FullscreenActivity.mTempSetList!=null && FullscreenActivity.mTempSetList.size()>0) {
                    // Redraw the lists
                    Collections.shuffle(FullscreenActivity.mTempSetList);

                    // Prepare the page for redrawing....
                    FullscreenActivity.doneshuffle = true;

                    // Run the listener
                    PopUpSetViewNew.this.dismiss();
                    if (mListener != null) {
                        mListener.shuffleSongsInSet();
                    }
                }
            }
        });

        FloatingActionButton saveAsProperSet = V.findViewById(R.id.saveAsProperSet);
        saveAsProperSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Save any changes to current set first
                doSave();

                File settosave = new File(FullscreenActivity.dirsets,FullscreenActivity.lastSetName);
                if (FullscreenActivity.lastSetName==null || FullscreenActivity.lastSetName.equals("")) {
                    FullscreenActivity.whattodo = "saveset";
                    if (mListener != null) {
                        mListener.openFragment();
                    }
                } else if (settosave.exists()) {
                    // Load the are you sure prompt
                    FullscreenActivity.whattodo = "saveset";
                    String setnamenice = FullscreenActivity.lastSetName.replace("__"," / ");
                    String message = getResources().getString(R.string.options_set_save) + " \'" + setnamenice + "\"?";
                    FullscreenActivity.myToastMessage = message;
                    DialogFragment newFragment = PopUpAreYouSureFragment.newInstance(message);
                    newFragment.show(getFragmentManager(), "dialog");
                    dismiss();
                } else {
                    FullscreenActivity.whattodo = "saveset";
                    if (mListener != null) {
                        mListener.openFragment();
                    }
                }
            }
        });

        if (FullscreenActivity.whattodo.equals("setitemvariation")) {
            helpVariationItem_TextView.setVisibility(View.VISIBLE);
            info.setVisibility(View.GONE);
            helpClickItem_TextView.setVisibility(View.GONE);
            helpDragItem_TextView.setVisibility(View.GONE);
            helpSwipeItem_TextView.setVisibility(View.GONE);
            listSetTweetButton.setVisibility(View.GONE);
            set_shuffle.setVisibility(View.GONE);
            helptext.setVisibility(View.VISIBLE);
        }


        // Try to move to the corresponding item in the set that we are viewing.
        SetActions.indexSongInSet();

        // If the song is found (indexSongInSet>-1 and lower than the number of items shown), smooth scroll to it
        if (FullscreenActivity.indexSongInSet>-1 && FullscreenActivity.indexSongInSet<FullscreenActivity.mTempSetList.size()) {
            //mRecyclerView.scrollToPosition(FullscreenActivity.indexSongInSet);
            //LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            llm.scrollToPositionWithOffset(FullscreenActivity.indexSongInSet , 0);
        }

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog());

        return V;
    }

    public void doSave() {
        String tempmySet = "";
        String tempItem;
        if (FullscreenActivity.mTempSetList == null) {
            FullscreenActivity.mTempSetList = new ArrayList<>();
        }
        for (int z=0; z<FullscreenActivity.mTempSetList.size(); z++) {
            tempItem = FullscreenActivity.mTempSetList.get(z);
            tempmySet = tempmySet + "$**_"+ tempItem + "_**$";
        }
        FullscreenActivity.mySet = null;
        FullscreenActivity.mySet = tempmySet;
        FullscreenActivity.mTempSetList = null;
        SetActions.prepareSetList();
        FullscreenActivity.myToastMessage = getActivity().getString(R.string.currentset) +
                " - " + getActivity().getString(R.string.ok);
        Preferences.savePreferences();
        // Tell the listener to do something

    }

    public void refresh() {
        if (mListener!=null) {
            mListener.refreshAll();
        }
    }

    public void close() {
        try {
            dismiss();
        } catch (Exception e) {
            Log.d("d","Error closing fragment");
        }
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
                // Replace the last instance of a / (as we may have subfolders)
                String mysongfolder = tempTitle.substring(0,tempTitle.lastIndexOf("/"));
                if (mysongfolder==null) {
                    mysongfolder="";
                }
                String mysongtitle = tempTitle.substring(tempTitle.lastIndexOf("/"));
                if (mysongtitle.startsWith("/")) {
                    mysongtitle = mysongtitle.substring(1);
                }
                if (mysongtitle==null) {
                    mysongtitle="";
                }

                if (mysongfolder.isEmpty() || mysongfolder.equals("")) {
                    mysongfolder = getResources().getString(R.string.mainfoldername);
                }

                if (mysongtitle.isEmpty() || mysongfolder.equals("")) {
                    mysongtitle = "!ERROR!";
                }

                Log.d("d","mysongtitle="+mysongtitle);
                Log.d("d","mysongfolder="+mysongfolder);

                if (i>-1) {
                    mSongName.add(i, mysongtitle);
                    mFolderName.add(i, mysongfolder);
                }
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
                String songLocation = LoadXML.getTempFileLocation(getActivity(),mFolderName.get(i-1),mSongName.get(i-1));
                si.songkey = LoadXML.grabNextSongInSetKey(getActivity(),songLocation);
                // Decide what image we'll need - song, image, note, slide, scripture, variation
                if (mFolderName.get(i - 1).equals("**"+getActivity().getResources().getString(R.string.slide))) {
                    si.songicon = getActivity().getResources().getString(R.string.slide);
                } else if (mFolderName.get(i - 1).equals("**"+getActivity().getResources().getString(R.string.note))) {
                    si.songicon = getActivity().getResources().getString(R.string.note);
                } else if (mFolderName.get(i - 1).equals("**"+getActivity().getResources().getString(R.string.scripture))) {
                    si.songicon = getActivity().getResources().getString(R.string.scripture);
                } else if (mFolderName.get(i - 1).equals("**"+getActivity().getResources().getString(R.string.image))) {
                    si.songicon = getActivity().getResources().getString(R.string.image);
                } else if (mFolderName.get(i - 1).equals("**"+getActivity().getResources().getString(R.string.variation))) {
                    si.songicon = getActivity().getResources().getString(R.string.variation);
                } else if (mSongName.get(i - 1).contains(".pdf") || mSongName.get(i - 1).contains(".PDF")) {
                    si.songicon = ".pdf";
                } else {
                    si.songicon = getActivity().getResources().getString(R.string.options_song);
                }
                result.add(si);
            }
        }
        return result;
    }

    public static void loadSong() {
        FullscreenActivity.setView = true;
        if (mListener!=null) {
            mListener.loadSongFromSet();
        }
        setfrag.dismiss();
    }

    public static void makeVariation(Context c) {
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
        FullscreenActivity.whatsongforsetwork = "\"$**_**"+c.getResources().getString(R.string.variation)+"/"+newsongname+"_**$";

        // Replace the set item with the variation item
        FullscreenActivity.mSetList[FullscreenActivity.indexSongInSet] = "**"+c.getResources().getString(R.string.variation)+"/"+newsongname;
        // Rebuild the mySet variable
        String new_mySet = "";
        for (String thisitem:FullscreenActivity.mSetList) {
            new_mySet = new_mySet + "$**_" + thisitem + "_**$";
        }
        FullscreenActivity.mySet = new_mySet;

        Preferences.savePreferences();

        FullscreenActivity.myToastMessage = c.getResources().getString(R.string.variation_edit);
        ShowToast.showToast(c);
        // Now load the new variation item up
        loadSong();
        if (mListener!=null) {
            mListener.prepareOptionMenu();
            // Close the fragment
            mListener.closePopUps();
        }
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

    @Override
    public void onDismiss(final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
            mListener.windowFlags();
            mListener.pageButtonAlpha(null);
        }
    }

    public void onPause() {
        super.onPause();
        this.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                islongpressing = event.isLongPress();
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (keyCode == FullscreenActivity.pedal1) {
                        if (islongpressing) {
                            doPedalAction(FullscreenActivity.pedal1longaction);
                        } else {
                            doPedalAction(FullscreenActivity.pedal1shortaction);
                        }
                        return true;
                    } else if (keyCode == FullscreenActivity.pedal2) {
                        if (islongpressing) {
                            doPedalAction(FullscreenActivity.pedal2longaction);
                        } else {
                            doPedalAction(FullscreenActivity.pedal2shortaction);
                        }
                        return true;
                    } else if (keyCode == FullscreenActivity.pedal3) {
                        if (islongpressing) {
                            doPedalAction(FullscreenActivity.pedal3longaction);
                        } else {
                            doPedalAction(FullscreenActivity.pedal3shortaction);
                        }
                        return true;
                    } else if (keyCode == FullscreenActivity.pedal4) {
                        if (islongpressing) {
                            doPedalAction(FullscreenActivity.pedal4longaction);
                        } else {
                            doPedalAction(FullscreenActivity.pedal4shortaction);
                        }
                        return true;
                    } else if (keyCode == FullscreenActivity.pedal5) {
                        if (islongpressing) {
                            doPedalAction(FullscreenActivity.pedal5longaction);
                        } else {
                            doPedalAction(FullscreenActivity.pedal5shortaction);
                        }
                        return true;
                    } else if (keyCode == FullscreenActivity.pedal6) {
                        if (islongpressing) {
                            doPedalAction(FullscreenActivity.pedal6longaction);
                        } else {
                            doPedalAction(FullscreenActivity.pedal6shortaction);
                        }
                        return true;
                    }

                    /*    if ((keyCode == FullscreenActivity.pageturner_PREVIOUS && FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) ||
                            keyCode == FullscreenActivity.pageturner_UP) {
                        PopUpSetViewNew.this.doScroll("up");
                        return true;
                    } else if ((keyCode == FullscreenActivity.pageturner_NEXT && FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) ||
                            keyCode == FullscreenActivity.pageturner_DOWN) {
                        PopUpSetViewNew.this.doScroll("down");
                        return true;
                    }*/
                }
                return false;
            }
        });
    }

    public void doPedalAction(String action) {
        switch (action) {
            case "prev":
                if (FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) {
                    PopUpSetViewNew.this.doScroll("up");
                }
                break;

            case "next":
                if (FullscreenActivity.toggleScrollBeforeSwipe.equals("Y")) {
                    PopUpSetViewNew.this.doScroll("down");
                }
                break;

            case "up":
                PopUpSetViewNew.this.doScroll("up");
                break;

            case "down":
                PopUpSetViewNew.this.doScroll("down");
                break;
        }
    }

    public void doScroll(String direction) {
        Interpolator customInterpolator = PathInterpolatorCompat.create(0.445f, 0.050f, 0.550f, 0.950f);
        if (direction.equals("up")) {
            mRecyclerView.smoothScrollBy(0,(int) (-FullscreenActivity.scrollDistance * mRecyclerView.getHeight()),customInterpolator);
        } else {
            mRecyclerView.smoothScrollBy(0,(int) (+FullscreenActivity.scrollDistance * mRecyclerView.getHeight()),customInterpolator);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    public String displaySetName() {
        // This decides on the set name to display as a title
        // If it is a new set (unsaved), it will be called 'current (unsaved)'
        // If it is a non-modified loaded set, it will be called 'set name'
        // If it is a modified, unsaved, loaded set, it will be called 'set name (unsaved)'

        String title;
        if (FullscreenActivity.lastSetName==null || FullscreenActivity.lastSetName.equals("")) {
            title = ": " + getActivity().getString(R.string.currentset) +
                    " (" + getActivity().getString(R.string.notsaved) + ")";
        } else {
            String name = FullscreenActivity.lastSetName.replace("__","/");
            title = ": " + name;
            if (!FullscreenActivity.mySet.equals(FullscreenActivity.lastLoadedSetContent)) {
                title += " (" + getActivity().getString(R.string.notsaved) + ")";
            }
        }

                return title;
    }
}
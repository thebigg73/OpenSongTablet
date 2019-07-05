package com.garethevans.church.opensongtablet;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.DialogFragment;
import androidx.core.view.animation.PathInterpolatorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PopUpSetViewNew extends DialogFragment {

    private static Dialog setfrag;
    private boolean islongpressing = false;

    static PopUpSetViewNew newInstance() {
        PopUpSetViewNew frag;
        frag = new PopUpSetViewNew();
        return frag;
    }

    static void makeVariation(Context c, Preferences preferences) {
        // Prepare the name of the new variation slide
        // If the file already exists, add _ to the filename
        StringBuilder newsongname = new StringBuilder(StaticVariables.songfilename);
        StorageAccess storageAccess = new StorageAccess();
        Uri uriVariation = storageAccess.getUriForItem(c, preferences, "Variations", "",
                StaticVariables.songfilename);

        // Original file
        Uri uriOriginal = storageAccess.getUriForItem(c, preferences, "Songs", StaticVariables.whichSongFolder,
                StaticVariables.songfilename);

        // Copy the file into the variations folder
        InputStream inputStream = storageAccess.getInputStream(c, uriOriginal);

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(c, preferences, uriVariation, null,
                "Variations", "", StaticVariables.songfilename);

        OutputStream outputStream = storageAccess.getOutputStream(c, uriVariation);
        storageAccess.copyFile(inputStream, outputStream);

        // Fix the song name and folder for loading
        StaticVariables.songfilename = newsongname.toString();
        StaticVariables.whichSongFolder = "../Variations";
        StaticVariables.whatsongforsetwork = "\"$**_**" + c.getResources().getString(R.string.variation) + "/" + newsongname + "_**$";

        // Replace the set item with the variation item
        StaticVariables.mSetList[StaticVariables.indexSongInSet] = "**" + c.getResources().getString(R.string.variation) + "/" + newsongname;
        // Rebuild the mySet variable
        StringBuilder new_mySet = new StringBuilder();
        for (String thisitem : StaticVariables.mSetList) {
            new_mySet.append("$**_").append(thisitem).append("_**$");
        }
        preferences.setMyPreferenceString(c,"setCurrent",new_mySet.toString());

        StaticVariables.myToastMessage = c.getResources().getString(R.string.variation_edit);
        ShowToast.showToast(c);
        // Now load the new variation item up
        loadSong(c,preferences);
        if (mListener != null) {
            mListener.prepareOptionMenu();
            // Close the fragment
            mListener.closePopUps();
        }
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

    static ArrayList<String> mSongName = new ArrayList<>();
    static ArrayList<String> mFolderName = new ArrayList<>();
    private RecyclerView mRecyclerView;

    static ItemTouchHelper.Callback callback;
    static ItemTouchHelper helper;
    FloatingActionButton saveMe;
    StorageAccess storageAccess;
    Preferences preferences;
    SetActions setActions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.dismiss();
        }
    }

    public static void loadSong(Context c, Preferences preferences) {
        StaticVariables.setView = true;
        if (StaticVariables.setchanged && mListener != null) {
            // We've edited the set and then clicked on a song, so save the set first
            FullscreenActivity.whattodo = "saveset";
            StringBuilder tempmySet = new StringBuilder();
            String tempItem;
            if (StaticVariables.mTempSetList == null) {
                StaticVariables.mTempSetList = new ArrayList<>();
            }
            for (int z = 0; z < StaticVariables.mTempSetList.size(); z++) {
                tempItem = StaticVariables.mTempSetList.get(z);
                tempmySet.append("$**_").append(tempItem).append("_**$");
            }
            preferences.setMyPreferenceString(c,"setCurrent",tempmySet.toString());
            mListener.confirmedAction();
        }
        if (mListener != null) {
            mListener.loadSongFromSet();
        }
        setfrag.dismiss();
    }

    public void doSave() {
        StringBuilder tempmySet = new StringBuilder();
        String tempItem;
        if (StaticVariables.mTempSetList == null) {
            StaticVariables.mTempSetList = new ArrayList<>();
        }
        for (int z = 0; z < StaticVariables.mTempSetList.size(); z++) {
            tempItem = StaticVariables.mTempSetList.get(z);
            tempmySet.append("$**_").append(tempItem).append("_**$");
        }
        preferences.setMyPreferenceString(getActivity(),"setCurrent",tempmySet.toString());
        StaticVariables.mTempSetList = null;
        setActions.prepareSetList(getActivity(),preferences);
        StaticVariables.myToastMessage = Objects.requireNonNull(getActivity()).getString(R.string.currentset) +
                " - " + getActivity().getString(R.string.ok);
    }

    public void refresh() {
        if (mListener != null) {
            mListener.refreshAll();
        }
    }

    public void close() {
        try {
            dismiss();
        } catch (Exception e) {
            Log.d("d", "Error closing fragment");
        }
    }

    private void extractSongsAndFolders() {
        // Populate the set list list view
        // Split the set items into song and folder
        mSongName = new ArrayList<>();
        mFolderName = new ArrayList<>();

        String tempTitle;
        if (StaticVariables.mTempSetList != null && StaticVariables.mTempSetList.size() > 0) {
            for (int i = 0; i < StaticVariables.mTempSetList.size(); i++) {
                if (!StaticVariables.mTempSetList.get(i).contains("/")) {
                    tempTitle = "/" + StaticVariables.mTempSetList.get(i);
                } else {
                    tempTitle = StaticVariables.mTempSetList.get(i);
                }
                // Replace the last instance of a / (as we may have subfolders)
                String mysongfolder = tempTitle.substring(0, tempTitle.lastIndexOf("/"));
                String mysongtitle = tempTitle.substring(tempTitle.lastIndexOf("/"));
                if (mysongtitle.startsWith("/")) {
                    mysongtitle = mysongtitle.substring(1);
                }

                if (mysongfolder.isEmpty()) {
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        storageAccess = new StorageAccess();
        preferences = new Preferences();
        setActions = new SetActions();

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getDialog().setCanceledOnTouchOutside(true);

        final View V = inflater.inflate(R.layout.popup_setview_new, container, false);
        setfrag = getDialog();
        TextView title = V.findViewById(R.id.dialogtitle);
        String titletext = Objects.requireNonNull(getActivity()).getResources().getString(R.string.options_set) + displaySetName();
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
            CustomAnimations.animateFAB(saveMe, getActivity());
            saveMe.setEnabled(false);
            saveMe.hide();
        }

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        if (mListener != null) {
            mListener.pageButtonAlpha("set");
        }

        // This is used if we edit the set then try to click on a song - it will save it first
        StaticVariables.setchanged = false;

        TextView helpClickItem_TextView = V.findViewById(R.id.helpClickItem_TextView);
        TextView helpDragItem_TextView = V.findViewById(R.id.helpDragItem_TextView);
        TextView helpSwipeItem_TextView = V.findViewById(R.id.helpSwipeItem_TextView);
        TextView helpVariationItem_TextView = V.findViewById(R.id.helpVariationItem_TextView);
        helpVariationItem_TextView.setVisibility(View.GONE);
        mRecyclerView = V.findViewById(R.id.my_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(RecyclerView.VERTICAL);

        mRecyclerView.setLayoutManager(llm);

        // Grab the saved set list array and put it into a list
        // This way we work with a temporary version
        if (StaticVariables.doneshuffle && StaticVariables.mTempSetList != null && StaticVariables.mTempSetList.size() > 0) {
            Log.d("d", "We've shuffled the set list");
        } else {
            StaticVariables.mTempSetList = new ArrayList<>();
            StaticVariables.mTempSetList.addAll(Arrays.asList(StaticVariables.mSetList));
        }

        extractSongsAndFolders();
        StaticVariables.doneshuffle = false;

        SetListAdapter ma = new SetListAdapter(createList(StaticVariables.mTempSetList.size()), getActivity(), preferences);
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

                if (StaticVariables.mTempSetList == null && StaticVariables.mSetList != null) {
                    // Somehow the temp set list is null, so build it again
                    StaticVariables.mTempSetList = new ArrayList<>();
                    Collections.addAll(StaticVariables.mTempSetList, StaticVariables.mSetList);
                }

                if (StaticVariables.mTempSetList!=null && StaticVariables.mTempSetList.size()>0) {
                    // Redraw the lists
                    Collections.shuffle(StaticVariables.mTempSetList);

                    // Prepare the page for redrawing....
                    StaticVariables.doneshuffle = true;

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

                String lastSetName = preferences.getMyPreferenceString(getActivity(),"setCurrentLastName","");
                Uri uri = storageAccess.getUriForItem(getActivity(), preferences, "Sets", "",
                        lastSetName);

                if (lastSetName==null || lastSetName.equals("")) {
                    FullscreenActivity.whattodo = "saveset";
                    if (mListener != null) {
                        mListener.openFragment();
                    }
                } else if (storageAccess.uriExists(getActivity(),uri)) {
                    // Load the are you sure prompt
                    FullscreenActivity.whattodo = "saveset";
                    String setnamenice = lastSetName.replace("__"," / ");
                    String message = getResources().getString(R.string.options_set_save) + " \'" + setnamenice + "\"?";
                    StaticVariables.myToastMessage = message;
                    DialogFragment newFragment = PopUpAreYouSureFragment.newInstance(message);
                    newFragment.show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "dialog");
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
            info.hide();
            helpClickItem_TextView.setVisibility(View.GONE);
            helpDragItem_TextView.setVisibility(View.GONE);
            helpSwipeItem_TextView.setVisibility(View.GONE);
            listSetTweetButton.hide();
            set_shuffle.hide();
            helptext.setVisibility(View.VISIBLE);
        }


        // Try to move to the corresponding item in the set that we are viewing.
        setActions.indexSongInSet();

        // If the song is found (indexSongInSet>-1 and lower than the number of items shown), smooth scroll to it
        if (StaticVariables.indexSongInSet>-1 && StaticVariables.indexSongInSet< StaticVariables.mTempSetList.size()) {
            //mRecyclerView.scrollToPosition(FullscreenActivity.indexSongInSet);
            //LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            llm.scrollToPositionWithOffset(StaticVariables.indexSongInSet , 0);
        }

        PopUpSizeAndAlpha.decoratePopUp(getActivity(),getDialog(), preferences);

        return V;
    }

    private List<SetItemInfo> createList(int size) {
        List<SetItemInfo> result = new ArrayList<>();
        for (int i=1; i <= size; i++) {
            if (!mSongName.get(i - 1).equals("!ERROR!")) {
                SetItemInfo si = new SetItemInfo();
                si.songitem = i+".";
                si.songtitle = mSongName.get(i - 1);
                si.songfolder = mFolderName.get(i - 1);
                String songLocation = LoadXML.getTempFileLocation(Objects.requireNonNull(getActivity()),mFolderName.get(i-1),mSongName.get(i-1));
                si.songkey = LoadXML.grabNextSongInSetKey(getActivity(), preferences, storageAccess, songLocation);
                // Decide what image we'll need - song, image, note, slide, scripture, variation
                if (mFolderName.get(i - 1).equals("**"+ Objects.requireNonNull(getActivity()).getResources().getString(R.string.slide))) {
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

    public interface MyInterface {
        void loadSongFromSet();

        void shuffleSongsInSet();

        void prepareOptionMenu();

        void confirmedAction();

        void refreshAll();

        void closePopUps();

        void pageButtonAlpha(String s);

        void windowFlags();

        void openFragment();
    }

    private void doExportSetTweet() {
        // Add the set items
        StringBuilder setcontents = new StringBuilder();

        for (String getItem: StaticVariables.mSetList) {
            int songtitlepos = getItem.indexOf("/")+1;
            getItem = getItem.substring(songtitlepos);
            setcontents.append(getItem).append(", ");
        }

        setcontents = new StringBuilder(setcontents.substring(0, setcontents.length() - 2));

        String tweet = setcontents.toString();
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
                    if (keyCode == preferences.getMyPreferenceInt(getActivity(),"pedal1Code",21)) {
                        if (islongpressing) {
                            doPedalAction(preferences.getMyPreferenceString(getActivity(),"pedal1LongPressAction","songmenu"));
                        } else {
                            doPedalAction(preferences.getMyPreferenceString(getActivity(),"pedal1ShortPressAction","prev"));
                        }
                        return true;
                    } else if (keyCode == preferences.getMyPreferenceInt(getActivity(),"pedal2Code",22)) {
                        if (islongpressing) {
                            doPedalAction(preferences.getMyPreferenceString(getActivity(),"pedal2LongPressAction","set"));
                        } else {
                            doPedalAction(preferences.getMyPreferenceString(getActivity(),"pedal2ShortPressAction","next"));
                        }
                        return true;
                    } else if (keyCode == preferences.getMyPreferenceInt(getActivity(),"pedal3Code",19)) {
                        if (islongpressing) {
                            doPedalAction(preferences.getMyPreferenceString(getActivity(),"pedal3LongPressAction","songmenu"));
                        } else {
                            doPedalAction(preferences.getMyPreferenceString(getActivity(),"pedal3ShortPressAction","prev"));
                        }
                        return true;
                    } else if (keyCode == preferences.getMyPreferenceInt(getActivity(),"pedal4Code",20)) {
                        if (islongpressing) {
                            doPedalAction(preferences.getMyPreferenceString(getActivity(),"pedal4LongPressAction","set"));
                        } else {
                            doPedalAction(preferences.getMyPreferenceString(getActivity(),"pedal4ShortPressAction","next"));
                        }
                        return true;
                    } else if (keyCode == preferences.getMyPreferenceInt(getActivity(),"pedal5Code",92)) {
                        if (islongpressing) {
                            doPedalAction(preferences.getMyPreferenceString(getActivity(),"pedal5LongPressAction","songmenu"));
                        } else {
                            doPedalAction(preferences.getMyPreferenceString(getActivity(),"pedal5ShortPressAction","prev"));
                        }
                        return true;
                    } else if (keyCode == preferences.getMyPreferenceInt(getActivity(),"pedal6Code",93)) {
                        if (islongpressing) {
                            doPedalAction(preferences.getMyPreferenceString(getActivity(),"pedal6LongPressAction","set"));
                        } else {
                            doPedalAction(preferences.getMyPreferenceString(getActivity(),"pedal6ShortPressAction","next"));
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
                if (preferences.getMyPreferenceBoolean(getActivity(),"pedalScrollBeforeMove",true)) {
                    PopUpSetViewNew.this.doScroll("up");
                }
                break;

            case "next":
                if (preferences.getMyPreferenceBoolean(getActivity(),"pedalScrollBeforeMove",true)) {
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

    private void doScroll(String direction) {
        Interpolator customInterpolator = PathInterpolatorCompat.create(0.445f, 0.050f, 0.550f, 0.950f);
        if (direction.equals("up")) {
            mRecyclerView.smoothScrollBy(0,(int) (-preferences.getMyPreferenceFloat(getActivity(),"scrollDistance", 0.7f) *
                    mRecyclerView.getHeight()),customInterpolator);
        } else {
            mRecyclerView.smoothScrollBy(0,(int) (+preferences.getMyPreferenceFloat(getActivity(),"scrollDistance", 0.7f) *
                    mRecyclerView.getHeight()),customInterpolator);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        this.dismiss();
    }

    private String displaySetName() {
        // This decides on the set name to display as a title
        // If it is a new set (unsaved), it will be called 'current (unsaved)'
        // If it is a non-modified loaded set, it will be called 'set name'
        // If it is a modified, unsaved, loaded set, it will be called 'set name (unsaved)'

        String title;
        String lastSetName = preferences.getMyPreferenceString(getActivity(),"setCurrentLastName","");
        if (lastSetName==null || lastSetName.equals("")) {
            title = ": " + Objects.requireNonNull(getActivity()).getString(R.string.currentset) +
                    " (" + getActivity().getString(R.string.notsaved) + ")";
        } else {
            String name = lastSetName.replace("__","/");
            title = ": " + name;
            if (!preferences.getMyPreferenceString(getActivity(),"setCurrent","")
                    .equals(preferences.getMyPreferenceString(getActivity(),"setCurrentBeforeEdits",""))) {
                title += " (" + Objects.requireNonNull(getActivity()).getString(R.string.notsaved) + ")";
            }
        }
        return title;
    }
}
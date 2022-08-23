package com.garethevans.church.opensongtablet;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.animation.PathInterpolatorCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@SuppressWarnings("ComparatorCombinators")
public class PopUpSetViewNew extends DialogFragment {

    private static Dialog setfrag;
    private boolean longKeyPress = false;
    private int keyRepeatCount = 0;
    private final String TAG = "PopUpSetViewNew";
    private static MyInterface mListener;
    private static SetActions setActions;
    static ArrayList<String> mSongName = new ArrayList<>();
    static ArrayList<String> mFolderName = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private StorageAccess storageAccess;
    private Preferences preferences;

    static PopUpSetViewNew newInstance() {
        PopUpSetViewNew frag;
        frag = new PopUpSetViewNew();
        return frag;
    }

    static void makeVariation(Context c, Preferences preferences) {
        // The set may have been edited and then the user clicks on a song, so save the set first
        if (mListener != null) {
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
            StaticVariables.setchanged = false;
        }

        // Prepare the name of the new variation slide
        StorageAccess storageAccess = new StorageAccess();
        // Original file
        Uri uriOriginal = storageAccess.getUriForItem(c, preferences, "Songs", StaticVariables.whichSongFolder,
                StaticVariables.songfilename);

        // IV - When a received song - use the stored received song filename
        if (StaticVariables.songfilename.equals("ReceivedSong")) {
            StaticVariables.songfilename = StaticVariables.receivedSongfilename;
        }

        StringBuilder newsongname = new StringBuilder(StaticVariables.songfilename);

        Uri uriVariation = storageAccess.getUriForItem(c, preferences, "Variations", "",
                storageAccess.safeFilename(StaticVariables.songfilename));

        // Copy the file into the variations folder
        InputStream inputStream = storageAccess.getInputStream(c, uriOriginal);

        // Check the uri exists for the outputstream to be valid
        storageAccess.lollipopCreateFileForOutputStream(c, preferences, uriVariation, null,
                "Variations", "", storageAccess.safeFilename(StaticVariables.songfilename));

        OutputStream outputStream = storageAccess.getOutputStream(c, uriVariation);
        storageAccess.copyFile(inputStream, outputStream);

        // Fix the song name and folder for loading
        StaticVariables.songfilename = storageAccess.safeFilename(newsongname.toString());
        StaticVariables.whichSongFolder = "../Variations";
        preferences.setMyPreferenceString(c,"whichSongFolder", "../Variations");
        StaticVariables.whatsongforsetwork = setActions.getSongForSetWork(c);

        // Replace the set item with the variation
        StaticVariables.mSetList[StaticVariables.indexSongInSet] = "**" + c.getResources().getString(R.string.variation) + "/" + storageAccess.safeFilename(newsongname.toString());
        StaticVariables.mTempSetList.set(StaticVariables.indexSongInSet,"**" + c.getResources().getString(R.string.variation) + "/" + storageAccess.safeFilename(newsongname.toString()));
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

    @Override
    public void onAttach(@NonNull Context context) {
        mListener = (MyInterface) context;
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

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
        try {
            setfrag.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doSave() {
        Log.d(TAG,"setCurrent:"+ preferences.getMyPreferenceString(getContext(),"setCurrent",""));
        StringBuilder tempmySet = new StringBuilder();
        String tempItem;
        if (StaticVariables.mTempSetList == null) {
            StaticVariables.mTempSetList = new ArrayList<>();
        }
        Log.d(TAG,"mTempSetList.size()="+StaticVariables.mTempSetList.size());
        for (int z = 0; z < StaticVariables.mTempSetList.size(); z++) {
            // IV - Use displayed info as it is in the desired order, with required 'MAIN' (root) folder handling
            tempItem = ("$**_" + mFolderName.get(z) + "/" + mSongName.get(z) + "_**$").
                    replace("$**_MAIN/", "$**_").
                    replace("$**_" + getString(R.string.mainfoldername) + "/", "$**_");
            tempmySet.append(tempItem);
        }
        Log.d(TAG,"tempmySet: "+tempmySet);
        preferences.setMyPreferenceString(getContext(),"setCurrent",tempmySet.toString());
        setActions.prepareSetList(getContext(),preferences);
        StaticVariables.myToastMessage = getString(R.string.currentset) +
                " - " + getString(R.string.ok);
    }

    private void close() {
        try {
            dismiss();
        } catch (Exception e) {
            Log.d(TAG, "Error closing fragment");
        }
    }

    private void extractSongsAndFolders() {
        // Populate the set list list view
        // Split the set items into song and folder
        mSongName = new ArrayList<>();
        mFolderName = new ArrayList<>();

        if (StaticVariables.mTempSetList==null) {
            Log.d(TAG,"mTempSetList is null");
        }
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
        if (getDialog()!=null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCanceledOnTouchOutside(true);
        }

        final View V = inflater.inflate(R.layout.popup_setview_new, container, false);
        setfrag = getDialog();
        TextView title = V.findViewById(R.id.dialogtitle);
        String titletext = getString(R.string.set) + displaySetName();
        title.setText(titletext);
        final FloatingActionButton setOptions = V.findViewById(R.id.setOptions);
        setOptions.setOnClickListener(view -> {
            // Save any changes to current set first
            doSave();

            // Open the Set menu
            StaticVariables.whichOptionMenu = "SET";
            if (mListener!=null) {
                mListener.prepareOptionMenu();
                mListener.openMyDrawers("option");
                dismiss();
            }
        });
        final FloatingActionButton closeMe = V.findViewById(R.id.closeMe);
        closeMe.setOnClickListener(view -> {
            CustomAnimations.animateFAB(closeMe, PopUpSetViewNew.this.getContext());
            closeMe.setEnabled(false);
            PopUpSetViewNew.this.dismiss();
        });
        FloatingActionButton saveMe = V.findViewById(R.id.saveMe);
        saveMe.setOnClickListener(view -> {
            doSave();
            // IV - If we have a set and the current song is not in the set - Display the first song
            if (StaticVariables.mSetList!=null && StaticVariables.mSetList.length>0 && !setActions.isSongInSet(getContext(), preferences)) {
                setActions.prepareFirstItem(getContext());
                // IV - Avoid double refresh for Presentation mode, see OnDismiss()
                if (mListener!=null && !StaticVariables.whichMode.equals("Presentation")) {
                    try {
                        mListener.refreshAll();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            close();
        });
        if (FullscreenActivity.whattodo.equals("setitemvariation")) {
            CustomAnimations.animateFAB(saveMe, getContext());
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
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(RecyclerView.VERTICAL);

        mRecyclerView.setLayoutManager(llm);

        if (StaticVariables.doneshuffle && StaticVariables.mTempSetList != null && StaticVariables.mTempSetList.size() > 0) {
            Log.d(TAG, "We've shuffled the set list");
        } else {
            // IV - We work with temporary set list array 'mTempSetList' prepared by this call
            setActions.prepareSetList(getContext(),preferences);
        }

        extractSongsAndFolders();

        SetListAdapter ma = new SetListAdapter(createList(StaticVariables.mTempSetList.size()), getContext(), preferences);
        mRecyclerView.setAdapter(ma);
        ItemTouchHelper.Callback callback = new SetListItemTouchHelper(ma);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(mRecyclerView);

        FloatingActionButton listSetTweetButton = V.findViewById(R.id.listSetTweetButton);
        // Set up the Tweet button
        listSetTweetButton.setOnClickListener(v -> PopUpSetViewNew.this.doExportSetTweet());
        FloatingActionButton info = V.findViewById(R.id.info);
        final LinearLayout helptext = V.findViewById(R.id.helptext);
        info.setOnClickListener(view -> {
            if (helptext.getVisibility() == View.VISIBLE) {
                helptext.setVisibility(View.GONE);
            } else {
                helptext.setVisibility(View.VISIBLE);
            }
        });

        FloatingActionButton set_shuffle = V.findViewById(R.id.shuffle);
        set_shuffle.setOnClickListener(v -> {
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
        });

        FloatingActionButton sort = V.findViewById(R.id.sort);
        sort.setOnClickListener(v -> {
            // Save any changes to current set first
            doSave();

            if (StaticVariables.mTempSetList == null && StaticVariables.mSetList != null) {
                // Somehow the temp set list is null, so build it again
                StaticVariables.mTempSetList = new ArrayList<>();
                Collections.addAll(StaticVariables.mTempSetList, StaticVariables.mSetList);
            }

            if (StaticVariables.mTempSetList!=null && StaticVariables.mTempSetList.size()>0) {
                // Redraw the lists
                Collections.sort(StaticVariables.mTempSetList,
                        (Comparator<? super String>) (o1, o2) -> o1.substring(o1.indexOf("/")).compareTo(o2.substring(o2.indexOf("/"))));

                // Prepare the page for redrawing....
                StaticVariables.doneshuffle = true;

                // Run the listener
                PopUpSetViewNew.this.dismiss();

                // Use the shuffle listener again - virtually the same anyway!
                if (mListener != null) {
                    mListener.shuffleSongsInSet();
                }
            }
        });

        FloatingActionButton saveAsProperSet = V.findViewById(R.id.saveAsProperSet);
        saveAsProperSet.setOnClickListener(view -> {
            // Save any changes to current set first
            doSave();

            String lastSetName = preferences.getMyPreferenceString(getContext(),"setCurrentLastName","");
            StaticVariables.settoload = lastSetName;
            Uri uri = storageAccess.getUriForItem(getContext(), preferences, "Sets", "",
                    lastSetName);

            if (lastSetName==null || lastSetName.equals("")) {
                FullscreenActivity.whattodo = "saveset";
                if (mListener != null) {
                    mListener.openFragment();
                }
            } else if (storageAccess.uriExists(getContext(),uri)) {
                // Load the are you sure prompt
                FullscreenActivity.whattodo = "saveset";
                String setnamenice = lastSetName.replace("__"," / ");
                String message = getResources().getString(R.string.save) + " \"" + setnamenice + "\"?";
                StaticVariables.myToastMessage = message;
                DialogFragment newFragment = PopUpAreYouSureFragment.newInstance(message);
                newFragment.show(requireActivity().getSupportFragmentManager(), "dialog");
            } else {
                FullscreenActivity.whattodo = "saveset";
                if (mListener != null) {
                    mListener.openFragment();
                }
            }
            // IV - Always dismiss the dialog
            dismiss();
        });

        if (FullscreenActivity.whattodo.equals("setitemvariation")) {
            helpVariationItem_TextView.setVisibility(View.VISIBLE);
            info.hide();
            helpClickItem_TextView.setVisibility(View.GONE);
            helpDragItem_TextView.setVisibility(View.GONE);
            helpSwipeItem_TextView.setVisibility(View.GONE);
            listSetTweetButton.hide();
            set_shuffle.hide();
            sort.hide();
            helptext.setVisibility(View.VISIBLE);
        }


        // Try to move to the corresponding item in the temporary set that we are viewing.
        // IV - Shuffle/Sort reopens this popup and can use stored song detail to index the song in TempSetList.
        if (StaticVariables.doneshuffle) {
            StaticVariables.doneshuffle = false;
            if (!StaticVariables.setSongDetail.equals("")) {
                StaticVariables.indexSongInSet = StaticVariables.mTempSetList.lastIndexOf(StaticVariables.setSongDetail);
            } else {
                StaticVariables.indexSongInSet = -1;
            }
        } else {
            setActions.indexSongInSet();
            // Store song detail
            if (StaticVariables.indexSongInSet > -1) {
                StaticVariables.setSongDetail = StaticVariables.mTempSetList.get(StaticVariables.indexSongInSet);
            } else {
                StaticVariables.setSongDetail = "";
            }
        }

        // If the song is found (indexSongInSet>-1 and lower than the number of items shown), smooth scroll to it
        if (StaticVariables.indexSongInSet>-1 && StaticVariables.indexSongInSet< StaticVariables.mTempSetList.size()) {
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
                String songLocation = LoadXML.getTempFileLocation(requireContext(),mFolderName.get(i-1),mSongName.get(i-1));
                si.songkey = LoadXML.grabNextSongInSetKey(getContext(), preferences, storageAccess, songLocation);
                // Decide what image we'll need - song, image, note, slide, scripture, variation
                if (mFolderName.get(i - 1).equals("**"+ getString(R.string.slide))) {
                    si.songicon = getString(R.string.slide);
                } else if (mFolderName.get(i - 1).equals("**"+getString(R.string.note))) {
                    si.songicon = getString(R.string.note);
                } else if (mFolderName.get(i - 1).equals("**"+getString(R.string.scripture))) {
                    si.songicon = getString(R.string.scripture);
                } else if (mFolderName.get(i - 1).equals("**"+getString(R.string.image))) {
                    si.songicon = getString(R.string.image);
                } else if (mFolderName.get(i - 1).equals("**"+getString(R.string.variation))) {
                    si.songicon = getString(R.string.variation);
                } else if (mSongName.get(i - 1).contains(".pdf") || mSongName.get(i - 1).contains(".PDF")) {
                    si.songicon = ".pdf";
                } else {
                    si.songicon = getString(R.string.song);
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

        void prepareSongMenu();

        void confirmedAction();

        void refreshAll();

        void onScrollAction();

        void closePopUps();

        void pageButtonAlpha(String s);

        void windowFlags();

        void openFragment();

        void openMyDrawers(String which);
    }

    private void doExportSetTweet() {
        // Add the set items
        StringBuilder setcontents = new StringBuilder();

        if (StaticVariables.mSetList!=null) {
            for (String getItem : StaticVariables.mSetList) {
                int songtitlepos = getItem.indexOf("/") + 1;
                getItem = getItem.substring(songtitlepos);
                setcontents.append(getItem).append(", ");
            }

            setcontents = new StringBuilder(setcontents.substring(0, setcontents.length() - 2));

            String tweet = setcontents.toString();
            try {
                tweet = URLEncoder.encode("#OpenSongApp\n" + setcontents, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }

            String tweetUrl = "https://twitter.com/intent/tweet?text=" + tweet;
            Uri uri = Uri.parse(tweetUrl);
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
    }

    @Override
    public void onDismiss(@NonNull final DialogInterface dialog) {
        if (mListener!=null) {
            mListener.pageButtonAlpha("");
            mListener.windowFlags();
            mListener.pageButtonAlpha(null);
            // IV - For presentation mode refresh all
            // IV - For performance modes call prepareSongMenu to update song list which handles ticks and use onScrollAction to update the set buttons
            if (StaticVariables.whichMode.equals("Presentation")) {
                mListener.refreshAll();
            } else {
                mListener.prepareSongMenu();
                mListener.onScrollAction();
            }
        }
    }

    public void onPause() {
        super.onPause();
        this.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog()!=null) {
            getDialog().setOnKeyListener((dialog, keyCode, event) -> {
                event.startTracking();
                longKeyPress = event.isLongPress();
                boolean actionrecognised;
                if (event.getAction() == KeyEvent.ACTION_DOWN && !longKeyPress) {
                    Log.d(TAG, "Pedal listener onKeyDown:" + keyCode);
                    event.startTracking();
                    // AirTurn pedals don't do long press, but instead autorepeat.  To deal with, count onKeyDown
                    // If the app detects more than a set number (reset when onKeyUp/onLongPress) it triggers onLongPress
                    keyRepeatCount++;
                    if (preferences.getMyPreferenceBoolean(getContext(), "airTurnMode", false) && keyRepeatCount > preferences.getMyPreferenceInt(getContext(), "keyRepeatCount", 20)) {
                        keyRepeatCount = 0;
                        longKeyPress = true;
                        doLongKeyPressAction(keyCode);
                        return true;
                    }
                    return true;
                } else if (event.getAction() == KeyEvent.ACTION_UP || longKeyPress) {
                    if (longKeyPress) {
                        event.startTracking();
                        actionrecognised = doLongKeyPressAction(keyCode);
                        Log.d(TAG, "Is long press!!!");
                        longKeyPress = false;
                        keyRepeatCount = 0;
                        if (actionrecognised) {
                            longKeyPress = true;
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        // onKeyUp
                        keyRepeatCount = 0;

                        Log.d(TAG, "Pedal listener onKeyUp:" + keyCode);
                        if (keyCode == preferences.getMyPreferenceInt(getContext(), "pedal1Code", 21)) {
                            doPedalAction(preferences.getMyPreferenceString(getContext(), "pedal1ShortPressAction", "prev"));
                            return true;
                        } else if (keyCode == preferences.getMyPreferenceInt(getContext(), "pedal2Code", 22)) {
                            doPedalAction(preferences.getMyPreferenceString(getContext(), "pedal2ShortPressAction", "next"));
                            return true;
                        } else if (keyCode == preferences.getMyPreferenceInt(getContext(), "pedal3Code", 19)) {
                            doPedalAction(preferences.getMyPreferenceString(getContext(), "pedal3ShortPressAction", "prev"));
                            return true;
                        } else if (keyCode == preferences.getMyPreferenceInt(getContext(), "pedal4Code", 20)) {
                            doPedalAction(preferences.getMyPreferenceString(getContext(), "pedal4ShortPressAction", "next"));
                            return true;
                        } else if (keyCode == preferences.getMyPreferenceInt(getContext(), "pedal5Code", 92)) {
                            doPedalAction(preferences.getMyPreferenceString(getContext(), "pedal5LongPressAction", "songmenu"));
                            return true;
                        } else if (keyCode == preferences.getMyPreferenceInt(getContext(), "pedal6Code", 93)) {
                            doPedalAction(preferences.getMyPreferenceString(getContext(), "pedal6ShortPressAction", "next"));
                            return true;
                        }
                        return false;
                    }
                }
                return true;
            });
        }
    }

    private boolean doLongKeyPressAction(int keyCode) {
        keyRepeatCount = 0;
        boolean actionrecognised = false;
        if (keyCode == preferences.getMyPreferenceInt(getContext(),"pedal1Code",21)) {
            actionrecognised = true;
            doPedalAction(preferences.getMyPreferenceString(getContext(),"pedal1LongPressAction","songmenu"));

        } else if (keyCode == preferences.getMyPreferenceInt(getContext(),"pedal2Code",22)) {
            actionrecognised = true;
            doPedalAction(preferences.getMyPreferenceString(getContext(),"pedal2LongPressAction","editset"));

        } else if (keyCode == preferences.getMyPreferenceInt(getContext(),"pedal3Code",19)) {
            actionrecognised = true;
            doPedalAction(preferences.getMyPreferenceString(getContext(),"pedal3LongPressAction","songmenu"));

        } else if (keyCode == preferences.getMyPreferenceInt(getContext(),"pedal4Code",20)) {
            actionrecognised = true;
            doPedalAction(preferences.getMyPreferenceString(getContext(),"pedal4LongPressAction","editset"));

        } else if (keyCode == preferences.getMyPreferenceInt(getContext(),"pedal5Code",92)) {
            actionrecognised = true;
            doPedalAction(preferences.getMyPreferenceString(getContext(),"pedal5LongPressAction","songmenu"));

        } else if (keyCode == preferences.getMyPreferenceInt(getContext(),"pedal6Code",93)) {
            actionrecognised = true;
            doPedalAction(preferences.getMyPreferenceString(getContext(),"pedal6LongPressAction","editset"));
        }
        Log.d(TAG,"actionrecognised="+actionrecognised);
        return actionrecognised;
    }


    private void doPedalAction(String action) {
        Log.d(TAG,"doPedalAction(\""+action+"\")");
        try {
            switch (action) {
                case "prev":
                    if (preferences.getMyPreferenceBoolean(getContext(), "pedalScrollBeforeMove", true)) {
                        PopUpSetViewNew.this.doScroll("up");
                    }
                    break;

                case "next":
                    if (preferences.getMyPreferenceBoolean(getContext(), "pedalScrollBeforeMove", true)) {
                        PopUpSetViewNew.this.doScroll("down");
                    }
                    break;

                case "up":
                    PopUpSetViewNew.this.doScroll("up");
                    break;

                case "down":
                    PopUpSetViewNew.this.doScroll("down");
                    break;

                case "editset":
                    try {
                        dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doScroll(String direction) {
        Interpolator customInterpolator = PathInterpolatorCompat.create(0.445f, 0.050f, 0.550f, 0.950f);
        if (direction.equals("up")) {
            mRecyclerView.smoothScrollBy(0,(int) (-preferences.getMyPreferenceFloat(getContext(),"scrollDistance", 0.7f) *
                    mRecyclerView.getHeight()),customInterpolator);
        } else {
            mRecyclerView.smoothScrollBy(0,(int) (+preferences.getMyPreferenceFloat(getContext(),"scrollDistance", 0.7f) *
                    mRecyclerView.getHeight()),customInterpolator);
        }
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        this.dismiss();
    }

    private String displaySetName() {
        // This decides on the set name to display as a title
        // If it is a new set (unsaved), it will be called 'current (unsaved)'
        // If it is a non-modified loaded set, it will be called 'set name'
        // If it is a modified, unsaved, loaded set, it will be called 'set name (unsaved)'

        String title;
        String lastSetName = preferences.getMyPreferenceString(getContext(),"setCurrentLastName","");
        if (lastSetName==null || lastSetName.equals("")) {
            title = ": " + getString(R.string.currentset) + " (" + getString(R.string.notsaved) + ")";
        } else {
            String name = lastSetName.replace("__","/");
            title = ": " + name;
            if (!preferences.getMyPreferenceString(getContext(),"setCurrent","")
                    .equals(preferences.getMyPreferenceString(getContext(),"setCurrentBeforeEdits",""))) {
                title += " (" + getString(R.string.notsaved) + ")";
            }
        }
        return title;
    }
}
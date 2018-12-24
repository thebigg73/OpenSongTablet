package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

class SongMenuAdapter extends BaseAdapter implements SectionIndexer {

    public interface MyInterface {
        void prepareOptionMenu();
        void fixSet();
    }

    private MyInterface mListener;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,Integer> sectionPositions;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,Integer> positionsForSection;
    String[] sections;
    Context context;
    String[] songs;
    private ArrayList<SongMenuViewItems> songList;
    ViewHolder viewHolder;
    // sparse boolean array for checking the state of the items
    private SparseBooleanArray itemStateArray = new SparseBooleanArray();

    @SuppressLint("UseSparseArrays")
    SongMenuAdapter(Context context, ArrayList<SongMenuViewItems> songList) {
        //super(context, R.layout.songlistitem, songList);
        this.context = context;
        this.songList = songList;

        songs = new String[songList.size()];
        this.songs = new String[songList.size()];
        for (int w=0;w<songList.size();w++) {
            songs[w] = songList.get(w).getTitle();
            this.songs[w] = songList.get(w).getTitle();
        }
        positionsForSection = null;
        positionsForSection = new HashMap<>();
        sectionPositions = null;
        sectionPositions = new HashMap<>();

        HashMap<String, Integer> mapIndex = new LinkedHashMap<>();
        sections = null;

        if (songs==null) {
            songs=new String[1];
            songs[0] = "  ";
        }

        for (int x = 0; x < songList.size(); x++) {
            String song = songList.get(x).getTitle();
            String key = songList.get(x).getKey();
            if (song==null) {
                song = " ";
            }
            if (key==null) {
                key = " ";
            }
            String ch;
            if (key.equals(context.getString(R.string.songsinfolder))) { // This is a directory
                ch = "/";
            } else if (song.length()>=1) {
                ch = song.substring(0, 1);
            } else {
                ch = "/";
            }
            ch = ch.toUpperCase(FullscreenActivity.locale);

            // HashMap will prevent duplicates
            if (!mapIndex.containsKey(ch)) {
                mapIndex.put(ch, x);
                positionsForSection.put(mapIndex.size() - 1, x);
            }
            sectionPositions.put(x, mapIndex.size() - 1);
        }

        Set<String> sectionLetters = mapIndex.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<>(sectionLetters);

        Collections.sort(sectionList);
        sections = new String[sectionList.size()];
        sectionList.toArray(sections);

    }

    static class ViewHolder {
        TextView lblListItem;
        TextView lblListItemAuthor;
        CheckBox lblListCheck;
        boolean isTicked;
        int position;
    }


    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(final int position , View convertView , ViewGroup parent ) {

        try {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            if (mInflater != null) {
                // Updated logic to stop checkboxes losing focus!

                convertView = mInflater.inflate(R.layout.list_item, null);
                viewHolder = new ViewHolder();

                viewHolder.lblListItem = convertView.findViewById(R.id.lblListItem);
                viewHolder.lblListItemAuthor = convertView.findViewById(R.id.lblListItemAuthor);
                viewHolder.lblListCheck = convertView.findViewById(R.id.lblListCheck);
                viewHolder.position = position;
                viewHolder.isTicked = false;

                convertView.setTag(viewHolder);

                viewHolder.lblListCheck.setTag(position); // This line is important.

                // Get the values for this song
                final SongMenuViewItems song = songList.get(position);
                final String item_filename = song.getFilename();
                final String item_title = song.getTitle();
                String item_author = song.getAuthor();
                String item_key = song.getKey();
                boolean item_isinset = song.getInSet();


                // Get the listener ready for item actions (press and long press items)
                final Context c = convertView.getContext();
                mListener = (MyInterface) parent.getContext();


                // Hide the empty stuff
                if (item_author.equals("")) {
                    viewHolder.lblListItemAuthor.setVisibility(View.GONE);
                } else {
                    viewHolder.lblListItemAuthor.setVisibility(View.VISIBLE);
                }

                boolean isdirectory = false;
                // If this is a directory - hide and disable the checkbox
                if (item_key.equals(c.getString(R.string.songsinfolder))) {
                    item_key = "";
                    isdirectory = true;
                    viewHolder.lblListCheck.setVisibility(View.GONE);
                    viewHolder.lblListCheck.setEnabled(false);
                    viewHolder.lblListItemAuthor.setVisibility(View.GONE);
                    Drawable d = c.getResources().getDrawable(R.drawable.ic_folder_edit_white_36dp);
                    viewHolder.lblListItem.setGravity(Gravity.CENTER_VERTICAL);
                    viewHolder.lblListItem.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                }

                if (!item_key.equals("") && !item_key.equals(" ")) {
                    item_key = " (" + item_key + ")";
                } else {
                    item_key = "";
                }

                if (FullscreenActivity.showSetTickBoxInSongMenu && !isdirectory) {
                    viewHolder.lblListCheck.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.lblListCheck.setVisibility(View.GONE);
                }

                final String item = item_title + item_key;
                viewHolder.lblListItem.setText(item);
                viewHolder.lblListItemAuthor.setText(item_author);
                viewHolder.lblListItem.setOnClickListener(SongMenuListeners.itemShortClickListener(position));
                viewHolder.lblListItemAuthor.setOnClickListener(SongMenuListeners.itemShortClickListener(position));

                // Don't do this for folders
                if (!isdirectory) {
                    viewHolder.lblListItem.setOnLongClickListener(SongMenuListeners.itemLongClickListener(position));
                    viewHolder.lblListItemAuthor.setOnLongClickListener(SongMenuListeners.itemLongClickListener(position));
                }

                // Set the checked state
                viewHolder.lblListCheck.setChecked(songList.get(position).getInSet());
                viewHolder.isTicked = songList.get(position).getInSet();

                // Now either hide the tick boxes or set up their actions
                if (FullscreenActivity.showSetTickBoxInSongMenu) {

                    viewHolder.lblListCheck.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = (Integer) v.getTag();
                            // Create the text to add to the set (sets FullscreenActivity.whatsongforsetwork
                            convertSongToSetItemText(item_filename);

                            // If it was checked already, uncheck it and remove it from the set
                            if (songList.get(position).getInSet()) {
                                viewHolder.isTicked = false;
                                songList.get(position).setInSet(false);
                                FullscreenActivity.mySet = FullscreenActivity.mySet.replace(FullscreenActivity.whatsongforsetwork, "");
                                SetActions setActions = new SetActions();
                                setActions.prepareSetList();

                                // Tell the user that the song has been removed.
                                FullscreenActivity.myToastMessage = "\"" + item_title + "\" " + c.getResources().getString(R.string.removedfromset);
                                ShowToast.showToast(c);

                                // Save the set and other preferences
                                Preferences.savePreferences();


                            } else {
                                // Trying to add to the set
                                FullscreenActivity.addingtoset = true;
                                if (userShouldConvertSongFormat(item_filename)) {
                                    // Tried to add a ChordPro song - Don't add song yet, but tell the user
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.convert_song);
                                    ShowToast.showToast(c);
                                    viewHolder.isTicked = false;
                                    songList.get(position).setInSet(false);

                                } else if (unsupportedSongFormat(item_filename)) {
                                    // Tried to add an unsupported song (MS Word) - Don't add song yet, but tell the user
                                    FullscreenActivity.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                    ShowToast.showToast(c);
                                    viewHolder.lblListCheck.setChecked(false);
                                    viewHolder.isTicked = false;
                                    songList.get(position).setInSet(false);
                                } else {
                                    viewHolder.isTicked = true;
                                    songList.get(position).setInSet(true);

                                    // If we are autologging CCLI information
                                    if (FullscreenActivity.ccli_automatic) {
                                        // Now we need to get the song info quickly to log it correctly
                                        // as this might not be the song loaded
                                        String[] vals = LoadXML.getCCLILogInfo(c, FullscreenActivity.whichSongFolder, item_filename);
                                        if (vals.length == 4 && vals[0] != null && vals[1] != null && vals[2] != null && vals[3] != null) {
                                            PopUpCCLIFragment.addUsageEntryToLog(c,FullscreenActivity.whichSongFolder + "/" + item_filename,
                                                    vals[0], vals[1], vals[2], vals[3], "6"); // Printed
                                        }
                                    }

                                    // Add the song
                                    FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;
                                    SetActions setActions = new SetActions();
                                    setActions.prepareSetList();

                                    // Tell the user that the song has been added.
                                    FullscreenActivity.myToastMessage = "\"" + item_title + "\" " + c.getResources().getString(R.string.addedtoset);
                                    ShowToast.showToast(c);


                                    // Save the set and other preferences
                                    Preferences.savePreferences();
                                }

                            }
                            // Check updates to the set in the Option menu
                            if (mListener != null) {
                                mListener.prepareOptionMenu();
                                mListener.fixSet();
                            }
                        }
                    });

                }
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return convertView;
    }


    private boolean userShouldConvertSongFormat(String filename) {
        filename = filename.toLowerCase(FullscreenActivity.locale);

        return filename.endsWith(".pro") || filename.endsWith(".pro") || filename.endsWith(".chopro") ||
                filename.endsWith(".cho") || filename.endsWith(".chordpro") ||
                filename.endsWith(".onsong") || filename.endsWith(".txt");
    }

    private boolean unsupportedSongFormat(String filename) {
        filename = filename.toLowerCase(FullscreenActivity.locale);

        return filename.endsWith(".doc") || filename.endsWith(".docx");
    }

    private void convertSongToSetItemText(String filename) {
        // Set the appropriate song filename
        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
            FullscreenActivity.whatsongforsetwork = "$**_" + filename + "_**$";
        } else {
            FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.whichSongFolder + "/" + filename + "_**$";
        }
    }

    public int getPositionForSection(int section) {
        try {
            if (positionsForSection!=null && section>-1 && positionsForSection.size()>section) {
                return positionsForSection.get(section);
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
    }

    @Override
    public String getItem(int position) {
        try {
            if (songs!=null && position>-1 && songs.length > position ) {
                return songs[position];
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public int getSectionForPosition(int position) {
        try {
            if (sectionPositions!=null && position>-1 && sectionPositions.size()>position) {
                return sectionPositions.get(position);
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }

    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getCount() {
        if (songs!=null) {
            return songs.length;
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    static void getIndexList(Context c) {
        FullscreenActivity.mapIndex = new LinkedHashMap<>();
        if (FullscreenActivity.songDetails != null) {
            for (int i = 0; i < FullscreenActivity.songDetails.length; i++) {
                String title;
                String index = "";
                if (FullscreenActivity.songDetails[i][2] != null &&
                        FullscreenActivity.songDetails[i][2].equals(c.getString(R.string.songsinfolder))) {
                    index = "/";
                }
                if (FullscreenActivity.songDetails[i][0] != null) {
                    title = FullscreenActivity.songDetails[i][0];
                    if (!index.equals("/") && title!=null && title.length()>=1) {
                        index = title.substring(0, 1);
                    }
                }

                if (FullscreenActivity.mapIndex.get(index) == null)
                    FullscreenActivity.mapIndex.put(index, i);
            }
        }
    }
}
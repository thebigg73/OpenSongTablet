package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.SectionIndexer;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class SongMenuAdapter extends BaseAdapter implements SectionIndexer {

    public interface MyInterface {
        void prepareOptionMenu();
        void prepareSongMenu();
        void fixSet();
    }

    private MyInterface mListener;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,Integer> sectionPositions;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,Integer> positionsForSection;
    private String[] sections;
    private final Context context;
    private String[] songs;
    private final ArrayList<SongMenuViewItems> songList;
    private final Preferences preferences;
    private ViewHolder viewHolder;
    private float textSize;

    @SuppressLint("UseSparseArrays")
    SongMenuAdapter(Context context, Preferences p, ArrayList<SongMenuViewItems> songList) {
        //super(context, R.layout.songlistitem, songList);
        this.context = context;
        this.songList = songList;
        this.textSize = p.getMyPreferenceFloat(context,"songMenuAlphaIndexSize",14.0f);

        preferences = p;
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
                key = "";
            }
            String ch;
            if (key.equals(context.getString(R.string.songsinfolder))) { // This is a directory
                ch = "/";
            } else if (song.length()>=1) {
                ch = song.substring(0, 1);
            } else {
                ch = "/";
            }
            ch = ch.toUpperCase(StaticVariables.locale);

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

    class ViewHolder {
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
                String item_author = song.getAuthor();
                String item_key = song.getKey();
                String temp_title;
                if (item_filename.startsWith("/") && item_filename.endsWith("/")) {
                    // This is a directory
                    temp_title = item_filename.replace("/", "");
                    item_author = "";
                    item_key = context.getString(R.string.songsinfolder);

                } else {
                    temp_title = song.getTitle();
                }

                if (song.getFilename().equals("")) {
                    // Hide it - it's just a reference for this folder, or we already have it
                    convertView.setVisibility(View.GONE);
                }

                final String item_title = temp_title;
                //boolean item_isinset = song.getInSet();

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
                    Drawable d = ResourcesCompat.getDrawable(c.getResources(),R.drawable.ic_folder_edit_white_36dp,null);
                    viewHolder.lblListItem.setGravity(Gravity.CENTER_VERTICAL);
                    viewHolder.lblListItem.setCompoundDrawablesWithIntrinsicBounds(d, null, null, null);
                }

                if (!item_key.equals("") && !item_key.equals(" ")) {
                    item_key = " (" + item_key + ")";
                } else {
                    item_key = "";
                }

                if (preferences.getMyPreferenceBoolean(c,"songMenuSetTicksShow",true) && !isdirectory) {
                    viewHolder.lblListCheck.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.lblListCheck.setVisibility(View.GONE);
                }

                final String item = item_title + item_key;
                viewHolder.lblListItem.setTextSize(textSize);
                viewHolder.lblListItem.setTextSize(textSize);
                viewHolder.lblListItem.setText(item);
                viewHolder.lblListItemAuthor.setText(item_author);

                // Don't do this for folders
                if (!isdirectory) {
                    viewHolder.lblListItem.setOnClickListener(SongMenuListeners.itemShortClickListener(item_filename,item_key,position));
                    viewHolder.lblListItemAuthor.setOnClickListener(SongMenuListeners.itemShortClickListener(item_filename,item_key,position));
                    viewHolder.lblListItem.setOnLongClickListener(SongMenuListeners.itemLongClickListener(item_filename, StaticVariables.whichSongFolder, position));
                    viewHolder.lblListItemAuthor.setOnLongClickListener(SongMenuListeners.itemLongClickListener(item_filename, StaticVariables.whichSongFolder, position));
                } else {
                    viewHolder.lblListItem.setOnClickListener(v -> {

                        // Don't save the folder preference yet until a song is clicked.
                        if (StaticVariables.whichSongFolder.equals("")||StaticVariables.whichSongFolder.equals(c.getString(R.string.mainfoldername))||
                        StaticVariables.whichSongFolder.equals("MAIN")) {
                            StaticVariables.whichSongFolder = item_filename;
                        } else {
                            StaticVariables.whichSongFolder = StaticVariables.whichSongFolder + "/" + item_filename;
                        }
                        if (mListener != null) {
                            mListener.prepareSongMenu();
                        }
                    });
                }

                // Set the checked state
                boolean ischecked = songList.get(position).getInSet();
                viewHolder.lblListCheck.setChecked(ischecked);
                viewHolder.isTicked = ischecked;

                // Now either hide the tick boxes or set up their actions
                if (preferences.getMyPreferenceBoolean(c,"songMenuSetTicksShow",true)) {

                    viewHolder.lblListCheck.setOnClickListener(v -> {
                        int position1 = (Integer) v.getTag();
                        // Create the text to add to the set (sets FullscreenActivity.whatsongforsetwork
                        convertSongToSetItemText(c, item_filename);

                        // If it was checked already, uncheck it and remove it from the set
                        if (songList.get(position1).getInSet()) {
                            viewHolder.isTicked = false;
                            songList.get(position1).setInSet(false);
                            String val = preferences.getMyPreferenceString(c,"setCurrent","").replace(StaticVariables.whatsongforsetwork,"");
                            preferences.setMyPreferenceString(c,"setCurrent",val);
                            SetActions setActions = new SetActions();
                            setActions.prepareSetList(c,preferences);

                            // Tell the user that the song has been removed.
                            StaticVariables.myToastMessage = "\"" + item_title + "\" " + c.getResources().getString(R.string.removedfromset);
                            ShowToast.showToast(c);

                        } else {
                            // Trying to add to the set
                            if (userShouldConvertSongFormat(item_filename)) {
                                // Tried to add a ChordPro song - Don't add song yet, but tell the user
                                StaticVariables.myToastMessage = c.getResources().getString(R.string.convert_song);
                                ShowToast.showToast(c);
                                viewHolder.isTicked = false;
                                songList.get(position1).setInSet(false);

                            } else if (unsupportedSongFormat(item_filename)) {
                                // Tried to add an unsupported song (MS Word) - Don't add song yet, but tell the user
                                StaticVariables.myToastMessage = c.getResources().getString(R.string.not_allowed);
                                ShowToast.showToast(c);
                                viewHolder.lblListCheck.setChecked(false);
                                viewHolder.isTicked = false;
                                songList.get(position1).setInSet(false);
                            } else {
                                viewHolder.isTicked = true;
                                songList.get(position1).setInSet(true);

                                // If we are autologging CCLI information
                                if (preferences.getMyPreferenceBoolean(c,"ccliAutomaticLogging",false)) {
                                    // Now we need to get the song info quickly to log it correctly
                                    // as this might not be the song loaded
                                    String[] vals = LoadXML.getCCLILogInfo(c, preferences, StaticVariables.whichSongFolder, item_filename);
                                    if (vals.length == 4 && vals[0] != null && vals[1] != null && vals[2] != null && vals[3] != null) {
                                        PopUpCCLIFragment.addUsageEntryToLog(c, preferences, StaticVariables.whichSongFolder + "/" + item_filename,
                                                vals[0], vals[1], vals[2], vals[3], "6"); // Printed
                                    }
                                }

                                // Add the song
                                String val = preferences.getMyPreferenceString(c,"setCurrent","") + StaticVariables.whatsongforsetwork;
                                preferences.setMyPreferenceString(c,"setCurrent",val);
                                SetActions setActions = new SetActions();
                                setActions.prepareSetList(c,preferences);

                                // Tell the user that the song has been added.
                                StaticVariables.myToastMessage = "\"" + item_title + "\" " + c.getResources().getString(R.string.addedtoset);
                                ShowToast.showToast(c);

                            }

                        }
                        // Check updates to the set in the Option menu
                        if (mListener != null) {
                            mListener.prepareOptionMenu();
                            mListener.fixSet();
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
        filename = filename.toLowerCase(StaticVariables.locale);

        return filename.endsWith(".pro") || filename.endsWith(".pro") || filename.endsWith(".chopro") ||
                filename.endsWith(".cho") || filename.endsWith(".chordpro") ||
                filename.endsWith(".onsong") || filename.endsWith(".txt");
    }

    private boolean unsupportedSongFormat(String filename) {
        filename = filename.toLowerCase(StaticVariables.locale);

        return filename.endsWith(".doc") || filename.endsWith(".docx");
    }

    private void convertSongToSetItemText(Context c, String filename) {
        // Set the appropriate song filename
        if (StaticVariables.whichSongFolder.equals(c.getString(R.string.mainfoldername)) || StaticVariables.whichSongFolder.equals("MAIN") ||
                StaticVariables.whichSongFolder.equals("")) {
            StaticVariables.whatsongforsetwork = "$**_" + filename + "_**$";
        } else {
            StaticVariables.whatsongforsetwork = "$**_" + StaticVariables.whichSongFolder + "/" + filename + "_**$";
        }
    }

    public int getPositionForSection(int section) {
        try {
            if (positionsForSection!=null && section>-1 && positionsForSection.size()>section && positionsForSection.get(section)!=null) {
                try {
                    return positionsForSection.get(section);
                } catch (Exception e) {
                    return 0;
                }
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
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
                try {
                    return sectionPositions.get(position);
                } catch (Exception e) {
                    return 0;
                }
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

    Map<String,Integer> getAlphaIndex(Context c, ArrayList<SongMenuViewItems> songlist) {
        Map<String,Integer> linkedHashMap = new LinkedHashMap<>();
        for (int i=0; i<songlist.size(); i++) {
            String index = "";
            if (songlist.get(i)!=null && songlist.get(i).getAuthor()!=null &&
                    songlist.get(i).getKey().equals(c.getString(R.string.songsinfolder))) {
                index = "/";
            } else if (songlist.get(i)!=null && songlist.get(i).getFilename()!=null && !songlist.get(i).getFilename().equals("")) {
                index = songlist.get(i).getFilename().substring(0, 1).toUpperCase(StaticVariables.locale);
            }

            if (linkedHashMap.get(index) == null) {
                linkedHashMap.put(index, i);
            }
        }
        return linkedHashMap;
    }

}
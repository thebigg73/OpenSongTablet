package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private HashMap<Integer,Integer> sectionPositions = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,Integer> positionsForSection = new HashMap<>();
    String[] sections;
    Context context;
    String[] songs;
    private ArrayList<SongMenuViewItems> songList;

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
            if (song==null) {
                song = " ";
            }
            String ch = song.substring(0, 1);
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

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(final int position , View convertView , ViewGroup parent ) {

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = mInflater.inflate(R.layout.list_item,null);
        TextView lblListItem = (TextView) convertView.findViewById(R.id.lblListItem);
        TextView lblListItemAuthor = (TextView) convertView.findViewById(R.id.lblListItemAuthor);
        final CheckBox lblListCheck = (CheckBox) convertView.findViewById(R.id.lblListCheck);

        if (!FullscreenActivity.showSetTickBoxInSongMenu) {
            lblListCheck.setVisibility(View.GONE);
        }
        final Context c = lblListCheck.getContext();

        mListener = (MyInterface) parent.getContext();

        final SongMenuViewItems song = songList.get(position);
        String key = song.getKey();
        if (key!=null && !key.equals("") && !key.equals(" ")) {
            key = " ("+key+")";
        } else {
            key = "";
        }
        final String item = song.getTitle() +  key;
        lblListItem.setText(item);

        // If this song is in the set, add an image to the right
        if (FullscreenActivity.showSetTickBoxInSongMenu) {
            if (song.getInset()) {
                //Drawable img = lblListItem.getContext().getResources().getDrawable( R.drawable.greendot);
                //img.setBounds( 0, 0, 24, 24 );
                //lblListItem.setCompoundDrawables( null, null, img, null );
                lblListCheck.setChecked(true);
            } else {
                //Drawable img = lblListItem.getContext().getResources().getDrawable( R.drawable.greendotblank);
                //img.setBounds( 0, 0, 24, 24 );
                //lblListItem.setCompoundDrawables( null, null, img, null );
                lblListCheck.setChecked(false);
            }

            lblListCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    String songtoadd = song.getFilename();
                    if (b) {
                        FullscreenActivity.addingtoset = true;

                        // If the song is in .pro, .onsong, .txt format, tell the user to convert it first
                        // This is done by viewing it (avoids issues with file extension renames)
                        // Just in case users running older than lollipop, we don't want to open the file
                        // In this case, store the current song as a string so we can go back to it
                        if (songtoadd.toLowerCase(FullscreenActivity.locale).endsWith(".pro") ||
                                songtoadd.toLowerCase(FullscreenActivity.locale).endsWith(".chopro") ||
                                songtoadd.toLowerCase(FullscreenActivity.locale).endsWith(".cho") ||
                                songtoadd.toLowerCase(FullscreenActivity.locale).endsWith(".chordpro") ||
                                songtoadd.toLowerCase(FullscreenActivity.locale).endsWith(".onsong") ||
                                songtoadd.toLowerCase(FullscreenActivity.locale).endsWith(".txt")) {

                            // Don't add song yet, but tell the user
                            FullscreenActivity.myToastMessage = c.getResources().getString(R.string.convert_song);
                            ShowToast.showToast(c);
                            lblListCheck.setChecked(false);

                        } else if (songtoadd.toLowerCase(FullscreenActivity.locale).endsWith(".doc") ||
                                songtoadd.toLowerCase(FullscreenActivity.locale).endsWith(".docx")) {
                            // Don't add song yet, but tell the user it is unsupported
                            FullscreenActivity.myToastMessage = c.getResources().getString(R.string.file_type_unknown);
                            ShowToast.showToast(c);
                            lblListCheck.setChecked(false);

                        } else {
                            // Set the appropriate song filename
                            if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                                FullscreenActivity.whatsongforsetwork = "$**_" + songtoadd + "_**$";
                            } else {
                                FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.whichSongFolder + "/" + songtoadd + "_**$";
                            }

                            // Allow the song to be added, even if it is already there
                            FullscreenActivity.mySet = FullscreenActivity.mySet + FullscreenActivity.whatsongforsetwork;
                            SetActions.prepareSetList();

                            // Tell the user that the song has been added.
                            FullscreenActivity.myToastMessage = "\"" + songtoadd + "\" " + c.getResources().getString(R.string.addedtoset);
                            ShowToast.showToast(c);

                            // Save the set and other preferences
                            Preferences.savePreferences();

                        }
                    } else {
                        if (FullscreenActivity.whichSongFolder.equals(FullscreenActivity.mainfoldername)) {
                            FullscreenActivity.whatsongforsetwork = "$**_" + songtoadd + "_**$";
                        } else {
                            FullscreenActivity.whatsongforsetwork = "$**_" + FullscreenActivity.whichSongFolder + "/" + songtoadd + "_**$";
                        }

                        FullscreenActivity.mySet = FullscreenActivity.mySet.replace(FullscreenActivity.whatsongforsetwork, "");
                        SetActions.prepareSetList();

                        // Tell the user that the song has been removed.
                        FullscreenActivity.myToastMessage = "\"" + songtoadd + "\" " + c.getResources().getString(R.string.removedfromset);
                        ShowToast.showToast(c);

                        // Save the set and other preferences
                        Preferences.savePreferences();
                    }

                    if (mListener != null) {
                        mListener.prepareOptionMenu();
                        mListener.fixSet();
                    }
                }
            });
        }
        lblListItemAuthor.setText(song.getAuthor());

        // Hide the empty stuff
        if (song.getAuthor().equals("")) {
            lblListItemAuthor.setVisibility(View.GONE);
        } else {
            lblListItemAuthor.setVisibility(View.VISIBLE);
        }

        lblListItem.setOnClickListener(SongMenuListeners.itemShortClickListener(position));
        lblListItemAuthor.setOnClickListener(SongMenuListeners.itemShortClickListener(position));

        lblListItem.setOnLongClickListener(SongMenuListeners.itemLongClickListener(position));
        lblListItemAuthor.setOnLongClickListener(SongMenuListeners.itemLongClickListener(position));

        return convertView;
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

    static void getIndexList() {
        FullscreenActivity.mapIndex = new LinkedHashMap<>();
        if (FullscreenActivity.songDetails != null) {
            for (int i = 0; i < FullscreenActivity.songDetails.length; i++) {
                String title;
                String index = "";
                if (FullscreenActivity.songDetails[i][0] != null) {
                    title = FullscreenActivity.songDetails[i][0];
                    index = title.substring(0, 1);
                }

                if (FullscreenActivity.mapIndex.get(index) == null)
                    FullscreenActivity.mapIndex.put(index, i);
            }
        }
    }
}
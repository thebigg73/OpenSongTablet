package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

class SongMenuAdapter extends BaseAdapter implements SectionIndexer {

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
    public View getView(int position , View convertView , ViewGroup parent ) {

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = mInflater.inflate(R.layout.list_item,null);
        TextView lblListItem = (TextView) convertView.findViewById(R.id.lblListItem);
        TextView lblListItemAuthor = (TextView) convertView.findViewById(R.id.lblListItemAuthor);

        SongMenuViewItems song = songList.get(position);
        String key = song.getKey();
        if (key!=null && !key.equals("") && !key.equals(" ")) {
            key = " ("+key+")";
        } else {
            key = "";
        }
        String item = song.getTitle() +  key;
        lblListItem.setText(item);
        lblListItemAuthor.setText(song.getAuthor());

            // Hide the empty stuff
            if (song.getAuthor().equals("")) {
                lblListItemAuthor.setVisibility(View.GONE);
            } else {
                lblListItemAuthor.setVisibility(View.VISIBLE);
            }

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

}
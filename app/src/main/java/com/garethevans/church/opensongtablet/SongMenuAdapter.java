package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.SectionIndexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

class SongMenuAdapter extends ArrayAdapter<String> implements SectionIndexer {

    private HashMap<String, Integer> mapIndex;
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,Integer> sectionPositions = new HashMap<>();
    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,Integer> positionsForSection = new HashMap<>();
    String[] sections;
    String[] songs;
    Context context;

    @SuppressLint("UseSparseArrays")
    SongMenuAdapter(Context context, String[] songList) {
        //super(context, android.R.layout.simple_list_item_1, songList);
        super(context, R.layout.songlistitem, songList);
        this.context = context;
        songs = null;
        positionsForSection = null;
        positionsForSection = new HashMap<>();
        sectionPositions = null;
        sectionPositions = new HashMap<>();
        this.songs = songList;
        songs = songList;
        mapIndex = null;
        mapIndex = new LinkedHashMap<>();
        sections = null;

        if (songs==null) {
            songs=new String[1];
            songs[0] = "";
        }

        for (int x = 0; x < songs.length; x++) {
            String song = songs[x];
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
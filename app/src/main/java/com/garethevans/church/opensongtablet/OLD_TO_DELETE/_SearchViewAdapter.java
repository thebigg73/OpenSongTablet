/*
package com.garethevans.church.opensongtablet.OLD_TO_DELETE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.StaticVariables;

import java.util.ArrayList;

public class _SearchViewAdapter extends BaseAdapter implements Filterable {

    private final Context context;
    private ArrayList<_SQLite> searchlist;
    private final ArrayList<_SQLite> mStringFilterList;
    private ValueFilter valueFilter;
    private final String what;
    //private HashMap<String, Integer> mapIndex;
    //String[] sections;

    _SearchViewAdapter(Context context , ArrayList<_SQLite> searchlist, String what) {
        this.context = context;
        this.searchlist = searchlist;
        mStringFilterList = searchlist;
        this.what = what;

        // TODO Struggling to get this working with sectionindexer, so turn off for now!
        */
/*//*
/ Get an array of the song values
        mapIndex = new LinkedHashMap<>();

        for (int x = 0; x < searchlist.size(); x++) {
            String filename = searchlist.get(x).getFilename();
            String ch = "";
            if (filename!=null && filename.length() > 0) {
                ch = filename.substring(0, 1);
            } else if (filename!=null){
                ch = filename;
            }
            ch = ch.toUpperCase(StaticVariables.locale);

            // HashMap will prevent duplicates
            mapIndex.put(ch, x);
        }

        Set<String> sectionLetters = mapIndex.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<>(sectionLetters);

        //Collections.sort(sectionList);

        sections = new String[sectionList.size()];

        sectionList.toArray(sections);*//*

    }

    @Override
    public int getCount() {
        if (searchlist==null) {
            searchlist = new ArrayList<>();
        }
        return searchlist.size();
    }

    @Override
    public Object getItem(int position) {
        return searchlist.get(position);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public long getItemId(int position) {
        return searchlist.indexOf(getItem(position));
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @Override
    public View getView(int position , View convertView , ViewGroup parent ) {

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (mInflater!=null) {
            convertView = mInflater.inflate(R.layout.searchrow, null);
        }
        TextView file_tv = convertView.findViewById(R.id.cardview_filename);
        TextView name_tv = convertView.findViewById(R.id.cardview_songtitle);
        TextView folder_tv = convertView.findViewById(R.id.cardview_folder);
        TextView text_key = convertView.findViewById(R.id.text_key);
        TextView author_tv = convertView.findViewById(R.id.cardview_author);
        TextView key_tv = convertView.findViewById(R.id.cardview_key);
        TextView text_author = convertView.findViewById(R.id.text_author);
        TextView theme_tv = convertView.findViewById(R.id.cardview_theme);
        TextView lyrics_tv = convertView.findViewById(R.id.cardview_lyrics);
        TextView hymnnum_tv = convertView.findViewById(R.id.cardview_hymn);

        _SQLite song = fixNullSongStuff(searchlist.get(position));

        if (what.equals("songmenu")) {
            name_tv.setTextSize(16.0f);
            name_tv.setText(song.getFilename());

            author_tv.setText(song.getAuthor());
            author_tv.setTextSize(10.0f);

            folder_tv.setVisibility(View.GONE);
            text_author.setVisibility(View.GONE);
            key_tv.setVisibility(View.GONE);
            text_key.setVisibility(View.GONE);

        } else {
            file_tv.setText(song.getFilename());
            name_tv.setText(song.getTitle());
            folder_tv.setText(song.getFolder());
            author_tv.setText(song.getAuthor());
            key_tv.setText(song.getKey());
            theme_tv.setText(song.getTheme());
            lyrics_tv.setText(song.getLyrics());
            hymnnum_tv.setText(song.getHymn_num());

            // Hide the empty stuff
            if (song.getAuthor().equals("")) {
                text_author.setVisibility(View.GONE);
                author_tv.setVisibility(View.GONE);
            } else {
                text_author.setVisibility(View.VISIBLE);
                author_tv.setVisibility(View.VISIBLE);
            }
            if (song.getKey().equals("")) {
                text_key.setVisibility(View.GONE);
                key_tv.setVisibility(View.GONE);
            } else {
                text_key.setVisibility(View.VISIBLE);
                key_tv.setVisibility(View.VISIBLE);
            }

        }
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null) {
            valueFilter = new ValueFilter();
        }
        return valueFilter;
    }

    private _SQLite fixNullSongStuff(_SQLite song) {
        if (song.getAka()==null) {
            song.setAka("");
        }
        if (song.getAlttheme()==null) {
            song.setAlttheme("");
        }
        if (song.getAuthor()==null) {
            song.setAuthor("");
        }
        if (song.getCcli()==null) {
            song.setCcli("");
        }
        if (song.getCopyright()==null) {
            song.setCcli("");
        }
        if (song.getFilename()==null) {
            song.setFilename("");
        }
        if (song.getFolder()==null) {
            song.setFolder("");
        }
        if (song.getHymn_num()==null) {
            song.setHymn_num("");
        }
        if (song.getKey()==null) {
            song.setKey("");
        }
        if (song.getLyrics()==null) {
            song.setLyrics("");
        }
        if (song.getSongid()==null) {
            song.setSongid("");
        }
        if (song.getTheme()==null) {
            song.setTheme("");
        }
        if (song.getTimesig()==null) {
            song.setTimesig("");
        }
        if (song.getTitle()==null) {
            song.setTitle("");
        }
        if (song.getUser1()==null) {
            song.setUser1("");
        }
        if (song.getUser2()==null) {
            song.setUser2("");
        }
        if (song.getUser3()==null) {
            song.setUser3("");
        }
        return song;
    }



    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<_SQLite> filterList = new ArrayList<>();
                for (int i = 0; i < mStringFilterList.size(); i++) {

                    String foundlyrics = mStringFilterList.get(i).getLyrics();
                    if (foundlyrics!=null) {
                        foundlyrics = foundlyrics.toUpperCase(StaticVariables.locale);
                    } else {
                        foundlyrics = "";
                        Log.d("d","foundlyrics = null");
                    }
                    if (foundlyrics.contains(constraint.toString().toUpperCase(StaticVariables.locale))) {

                        _SQLite song = new _SQLite();
                        song.setSongid(mStringFilterList.get(i).getSongid());
                        song.setFilename(mStringFilterList.get(i).getFilename());
                        song.setFolder(mStringFilterList.get(i).getFolder());
                        song.setTitle(mStringFilterList.get(i).getTitle());
                        song.setAuthor(mStringFilterList.get(i).getAuthor());
                        song.setCopyright(mStringFilterList.get(i).getCopyright());
                        song.setLyrics(mStringFilterList.get(i).getLyrics());
                        song.setHymn_num(mStringFilterList.get(i).getHymn_num());
                        song.setCcli(mStringFilterList.get(i).getCcli());
                        song.setTheme(mStringFilterList.get(i).getTheme());
                        song.setAlttheme(mStringFilterList.get(i).getAlttheme());
                        song.setUser1(mStringFilterList.get(i).getUser1());
                        song.setUser2(mStringFilterList.get(i).getUser2());
                        song.setUser3(mStringFilterList.get(i).getUser3());
                        song.setKey(mStringFilterList.get(i).getKey());
                        song.setTimesig(mStringFilterList.get(i).getTimesig());
                        song.setAka(mStringFilterList.get(i).getAka());

                        filterList.add(song);
                    }
                }
                results.count = filterList.size();
                results.values = filterList;
            } else {
                results.count = mStringFilterList.size();
                results.values = mStringFilterList;
            }
            return results;

        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            searchlist = (ArrayList<_SQLite>) results.values;
            //updateListIndex(searchlist);
            // Let the adapter know about the updated list
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }


}
*/

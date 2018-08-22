package com.garethevans.church.opensongtablet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

class SearchViewAdapter extends BaseAdapter implements Filterable, SectionIndexer {

    Context context;
    private ArrayList<SearchViewItems> searchlist;
    private ArrayList<SearchViewItems> mStringFilterList;
    private ValueFilter valueFilter;
    String what;
    private HashMap<String, Integer> mapIndex;
    String[] sections;

    SearchViewAdapter(Context context , ArrayList<SearchViewItems> searchlist, String what) {
        this.context = context;
        this.searchlist = searchlist;
        mStringFilterList = searchlist;
        this.what = what;

        mapIndex = new LinkedHashMap<>();

        for (int x = 0; x < FullscreenActivity.searchTitle.size(); x++) {
            String title = FullscreenActivity.searchTitle.get(x);
            String ch = title.substring(0, 1);
            ch = ch.toUpperCase(FullscreenActivity.locale);

            // HashMap will prevent duplicates
            mapIndex.put(ch, x);
        }

        Set<String> sectionLetters = mapIndex.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<>(sectionLetters);

        Collections.sort(sectionList);

        sections = new String[sectionList.size()];

        sectionList.toArray(sections);
    }

    @Override
    public int getCount() {
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

        SearchViewItems song = searchlist.get(position);

        if (what.equals("songmenu")) {
            name_tv.setTextSize(16.0f);
            name_tv.setText(song.getTitle());

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
            hymnnum_tv.setText(song.getHymnnum());

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

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int i) {
        if (mapIndex!=null && sections!=null && sections.length>0) {
            return mapIndex.get(sections[i]);
        } else {
            return 0;
        }
    }

    @Override
    public void notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public int getSectionForPosition(int i) {
        try {
            if (mapIndex!=null && i>-1 && mapIndex.size()>i) {
                return mapIndex.get(i);
            } else {
                return 0;
            }
        } catch (Exception e) {
            return 0;
        }

        //return i;
    }

    private class ValueFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint != null && constraint.length() > 0) {
                ArrayList<SearchViewItems> filterList = new ArrayList<>();
                for (int i = 0; i < mStringFilterList.size(); i++) {

                    if ( (mStringFilterList.get(i).getLyrics().toUpperCase(FullscreenActivity.locale) )
                            .contains(constraint.toString().toUpperCase(FullscreenActivity.locale))) {

                        SearchViewItems song = new SearchViewItems(
                                mStringFilterList.get(i).getFilename(),
                                mStringFilterList.get(i).getTitle(),
                                mStringFilterList.get(i).getFolder(),
                                mStringFilterList.get(i).getAuthor(),
                                mStringFilterList.get(i).getKey(),
                                mStringFilterList.get(i).getTheme(),
                                mStringFilterList.get(i).getLyrics(),
                                mStringFilterList.get(i).getHymnnum());

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
            searchlist = (ArrayList<SearchViewItems>) results.values;
            updateListIndex(searchlist);
            // Let the adapter know about the updated list
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }

    private void updateListIndex(ArrayList<SearchViewItems> results) {

        mapIndex = new LinkedHashMap<>();

        for (int x = 0; x < results.size(); x++) {
            String title = results.get(x).getTitle();
            String ch = title.substring(0, 1);
            ch = ch.toUpperCase(FullscreenActivity.locale);

            // HashMap will prevent duplicates
            mapIndex.put(ch, x);
        }

        Set<String> sectionLetters = mapIndex.keySet();

        // create a list from the set to sort
        ArrayList<String> sectionList = new ArrayList<>(sectionLetters);

        Collections.sort(sectionList);

        sections = new String[sectionList.size()];

        sectionList.toArray(sections);
    }
}

package com.garethevans.church.opensongtablet;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class SearchViewAdapter extends BaseAdapter implements Filterable {

    Context context;
    ArrayList<SearchViewItems> searchlist;
    ArrayList<SearchViewItems> mStringFilterList;
    ValueFilter valueFilter;

    SearchViewAdapter(Context context , ArrayList<SearchViewItems> searchlist) {
        this.context = context;
        this.searchlist = searchlist;
        mStringFilterList = searchlist;
    }

    @Override
    public int getCount() {
        return searchlist.size();
    }

    @Override
    public Object getItem(int position) {
        return searchlist.get(position);
    }

    @Override
    public long getItemId(int position) {
        return searchlist.indexOf(getItem(position));
    }

    @Override
    public View getView(int position , View convertView , ViewGroup parent ) {

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        convertView = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.searchrow, null);
            TextView file_tv = (TextView) convertView.findViewById(R.id.cardview_filename);
            TextView name_tv = (TextView) convertView.findViewById(R.id.cardview_songtitle);
            TextView folder_tv = (TextView) convertView.findViewById(R.id.cardview_folder);
            TextView text_key = (TextView) convertView.findViewById(R.id.text_key);
            TextView author_tv = (TextView) convertView.findViewById(R.id.cardview_author);
            TextView key_tv = (TextView) convertView.findViewById(R.id.cardview_key);
            TextView text_author = (TextView) convertView.findViewById(R.id.text_author);
            TextView theme_tv = (TextView) convertView.findViewById(R.id.cardview_theme);
            TextView lyrics_tv = (TextView) convertView.findViewById(R.id.cardview_lyrics);
            TextView hymnnum_tv = (TextView) convertView.findViewById(R.id.cardview_hymn);

            SearchViewItems song = searchlist.get(position);
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

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            searchlist = (ArrayList<SearchViewItems>) results.values;
            notifyDataSetChanged();
        }
    }
}

package com.garethevans.church.opensongtablet.importsongs;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class SongMatchAdapter extends RecyclerView.Adapter<SongMatchAdapter.ViewHolder> {

    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "SongMatchAdapter";
    private List<SongMatch> songMatches = new ArrayList<>();
    private final OnSongSelectedListener listener;

    public interface OnSongSelectedListener {
        void onSongSelected(SongMatch songMatch);
    }

    public SongMatchAdapter(OnSongSelectedListener listener) {
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSongMatches(List<SongMatch> matches) {
        Log.d(TAG,"matches:"+matches);
        this.songMatches = matches != null ? matches : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_online_song_search_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SongMatch match = songMatches.get(position);
        holder.title.setText(match.getTitle());
        holder.artist.setText(match.getArtist());
        double rating = match.getRating();
        int votes = match.getVotes();
        String extraInfo = "";
        if (rating>-1) {
            DecimalFormat df = new DecimalFormat("#.00");
            extraInfo = df.format(rating);
        }
        if (votes>-1) {
            extraInfo = extraInfo + " ("+ votes +")";
        }
        extraInfo = extraInfo.trim();
        String provider = (match.getProviderName() + "\n" + extraInfo).trim();
        holder.provider.setText(provider);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSongSelected(match);
            }
        });
    }

    @Override
    public int getItemCount() {
        return songMatches.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist, provider;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.songTitle);
            artist = itemView.findViewById(R.id.songArtist);
            provider = itemView.findViewById(R.id.songProvider);
        }
    }
}
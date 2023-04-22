package com.garethevans.church.opensongtablet.drummer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;

public class BBSongAdapter extends RecyclerView.Adapter<BBSongViewHolder> {

    private final MainActivityInterface mainActivityInterface;
    private final ArrayList<BBSong> foundSongs;
    private final String folder_string, song_string, timesig_string, kit_string, success_string;
    private final String TAG = "BBSongAdapter";
    private final BottomSheetBeatBuddySongs bottomSheetBeatBuddySongs;

    public BBSongAdapter(Context c, ArrayList<BBSong> foundSongs, BottomSheetBeatBuddySongs bottomSheetBeatBuddySongs) {
        mainActivityInterface = (MainActivityInterface) c;
        this.bottomSheetBeatBuddySongs = bottomSheetBeatBuddySongs;
        this.foundSongs = foundSongs;
        folder_string = c.getString(R.string.folder);
        song_string = c.getString(R.string.song);
        timesig_string = c.getString(R.string.time_signature);
        kit_string = c.getString(R.string.drum_kit);
        success_string = c.getString(R.string.success);
    }

    @NonNull
    @Override
    public BBSongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.view_bbsong, parent, false);
        return new BBSongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BBSongViewHolder holder, int position) {
        BBSong bbSong = foundSongs.get(position);
        String folderString = folder_string+" "+bbSong.folder+": "+bbSong.foldername;
        String songString = song_string+" "+bbSong.song+": "+bbSong.name;
        String signatureString = timesig_string+": "+bbSong.signature;
        String kitString = kit_string+" "+bbSong.kit+": "+bbSong.kitname;

        holder.bb_folder.setText(folderString);
        holder.bb_song.setText(songString);
        holder.bb_timesig.setText(signatureString);
        holder.bb_kit.setText(kitString);

        int pos = holder.getAbsoluteAdapterPosition();
        holder.bb_layout.setOnClickListener(view -> {
            Log.d(TAG,"pos:"+pos);
            String songCode = mainActivityInterface.getBeatBuddy().getSongCode(bbSong.folder, bbSong.song);
            Log.d(TAG,"songCode:"+songCode);
            String message = success_string + ": "+folder_string+" "+bbSong.folder+" / "+song_string+" "+bbSong.song;
            mainActivityInterface.getShowToast().doItBottomSheet(message,bottomSheetBeatBuddySongs.getView());
            bottomSheetBeatBuddySongs.updateSong(bbSong.folder,bbSong.song);
            mainActivityInterface.getMidi().sendMidiHexSequence(songCode);
        });
    }

    @Override
    public int getItemCount() {
        return foundSongs.size();
    }
}

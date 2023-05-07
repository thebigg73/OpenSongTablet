package com.garethevans.church.opensongtablet.beatbuddy;

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
    private final String folder_string, song_string, timesig_string, kit_string, success_string,
            save_string;
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
        save_string = c.getString(R.string.save);
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
        String folderString = folder_string+" "+bbSong.folder_num+": "+bbSong.folder_name;
        String songString = song_string+" "+bbSong.song_num+": "+bbSong.song_name;
        String signatureString = timesig_string+": "+bbSong.signature;
        String kitString = kit_string+" "+bbSong.kit_num+": "+bbSong.kit_name;

        boolean folderOk = bbSong.folder_num_name!=null && !bbSong.folder_num_name.isEmpty();
        boolean songOk = bbSong.song_num_name!=null && !bbSong.song_num_name.isEmpty();
        boolean timeOk = bbSong.signature!=null && !bbSong.signature.isEmpty();
        boolean kitOk = bbSong.kit_num != -1;
        holder.bb_folder.setVisibility(folderOk ? View.VISIBLE:View.GONE);
        holder.bb_song.setVisibility(songOk ? View.VISIBLE:View.GONE);
        holder.bb_timesig.setVisibility(timeOk ? View.VISIBLE:View.GONE);
        holder.bb_kit.setVisibility(kitOk ? View.VISIBLE:View.GONE);
        holder.bb_folder.setText(folderOk ? folderString:"");
        holder.bb_song.setText(songOk ? songString:"");
        holder.bb_timesig.setText(timeOk ? signatureString:"");
        holder.bb_kit.setText(kitOk ? kitString:"");

        int pos = holder.getAbsoluteAdapterPosition();
        if (folderOk && songOk) {
            holder.bb_layout.setOnClickListener(view -> {
                Log.d(TAG, "pos:" + pos);
                String songCode = mainActivityInterface.getBeatBuddy().getSongCode(bbSong.folder_num, bbSong.song_num);
                Log.d(TAG, "songCode:" + songCode);
                String message = success_string + ": " + folder_string + " " + bbSong.folder_num + " / " + song_string + " " + bbSong.song_num;
                mainActivityInterface.getShowToast().doItBottomSheet(message, bottomSheetBeatBuddySongs.getView());
                bottomSheetBeatBuddySongs.updateSong(bbSong.folder_num, bbSong.song_num);
                mainActivityInterface.getMidi().sendMidiHexSequence(songCode);
            });
            holder.bb_layout.setOnLongClickListener(view -> {
                mainActivityInterface.getSong().setBeatbuddysong(bbSong.song_name);
                mainActivityInterface.getSaveSong().updateSong(mainActivityInterface.getSong(),false);
                String message = save_string + " - " + success_string + ": " + song_string + " " + mainActivityInterface.getSong().getTitle();
                mainActivityInterface.getShowToast().doItBottomSheet(message, bottomSheetBeatBuddySongs.getView());
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return foundSongs.size();
    }
}

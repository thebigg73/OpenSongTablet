package com.garethevans.church.opensongtablet.presenter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setmenu.SetItemViewHolder;

import java.util.ArrayList;

public class SongSectionsAdapter extends RecyclerView.Adapter<SongSectionViewHolder> {

    private MainActivityInterface mainActivityInterface;
    private DisplayInterface displayInterface;
    private ArrayList<SongSectionInfo> songSections;

    SongSectionsAdapter(MainActivityInterface mainActivityInterface, DisplayInterface displayInterface) {
        this.mainActivityInterface = mainActivityInterface;
        this.displayInterface = displayInterface;
        buildSongSections();
    }

    private void buildSongSections() {
        if (songSections==null) {
            songSections = new ArrayList<>();
        } else {
            songSections.clear();
        }

        for (int x=0; x<mainActivityInterface.getSong().getSongSections().size(); x++) {
            SongSectionInfo songSectionInfo = new SongSectionInfo();
            songSectionInfo.heading = mainActivityInterface.getSong().getSongSectionHeadings().get(x);
            songSectionInfo.content = mainActivityInterface.getSong().getSongSections().get(x);
            songSectionInfo.needsImage = !mainActivityInterface.getSong().getFiletype().equals("XML");
            songSections.add(songSectionInfo);
        }
    }

    @NonNull
    @Override
    public SongSectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.view_song_section, parent, false);

        return new SongSectionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongSectionViewHolder holder, int position) {
        SongSectionInfo si = songSections.get(position);
        String heading = si.heading;
        String content = si.content;
        boolean needsImage = si.needsImage;

        if (heading!=null && !heading.isEmpty()) {
            holder.heading.setText(heading);
            holder.heading.setVisibility(View.VISIBLE);
        } else {
            holder.heading.setText("");
            holder.heading.setVisibility(View.GONE);
        }

        if (content!=null && !content.isEmpty()) {
            holder.content.setText(heading);
            holder.content.setVisibility(View.VISIBLE);
        } else {
            holder.content.setText("");
            holder.content.setVisibility(View.GONE);
        }

        if (needsImage) {
            holder.image.setVisibility(View.VISIBLE);
            holder.heading.setVisibility(View.GONE);
            holder.content.setVisibility(View.GONE);
        } else {
            holder.image.setVisibility(View.GONE);
            holder.heading.setVisibility(View.VISIBLE);
            holder.content.setVisibility(View.VISIBLE);
        }

        final int thisPos = holder.getAbsoluteAdapterPosition();
        holder.itemView.setOnClickListener(view -> displayInterface.presenterShowSection(thisPos));
    }


    @Override
    public int getItemCount() {
        return 0;
    }
}

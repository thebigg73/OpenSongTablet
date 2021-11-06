package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;

import java.util.ArrayList;

public class SongSectionsAdapter extends RecyclerView.Adapter<SongSectionViewHolder> {

    private Context c;
    private final MainActivityInterface mainActivityInterface;
    private final DisplayInterface displayInterface;
    private ArrayList<SongSectionInfo> songSections;
    private final RecyclerView recyclerView;
    private final SongSectionsFragment fragment;
    private final String TAG = "SongSetionsAdapter";

    SongSectionsAdapter(Context c, MainActivityInterface mainActivityInterface, SongSectionsFragment fragment,
                        DisplayInterface displayInterface, RecyclerView recyclerView) {
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
        this.displayInterface = displayInterface;
        this.recyclerView = recyclerView;
        this.fragment = fragment;
    }

    public void buildSongSections() {
        if (songSections==null) {
            songSections = new ArrayList<>();
        } else {
            int oldSize = songSections.size();
            songSections.clear();
            notifyItemRangeRemoved(0,oldSize);
        }

        Log.d(TAG,"title: "+mainActivityInterface.getSong().getTitle());
        Log.d(TAG, "songsectionsize: "+mainActivityInterface.getSong().getSongSections().size());
        for (int x=0; x<mainActivityInterface.getSong().getSongSections().size(); x++) {
            SongSectionInfo songSectionInfo = new SongSectionInfo();
            String content = mainActivityInterface.getSong().getSongSections().get(x);
            String heading = "";

            if (content.startsWith("[") && content.contains("]")) {
                // Extract the heading
                int toPos = content.indexOf("]");
                heading = content.substring(0,toPos+1);
                content = content.replace(heading,"").replace("[","").
                        replace("]","").trim();
                heading = mainActivityInterface.getProcessSong().beautifyHeading(c,mainActivityInterface,heading);
                heading = heading.replace("[","").replace("]","").trim();
            }
            // Tidy up the content
            String[] lines = content.split("\n");
            StringBuilder newContent = new StringBuilder();
            for (String line:lines) {
                line = line.trim();
                if (line.startsWith(".")) {
                    line = line.replaceFirst(".","");
                }
                if (!line.startsWith(";")) {
                    newContent.append(line).append("\n");
                }
            }
            content = newContent.toString().trim();
            heading = heading.trim();
            if (!content.isEmpty() && !heading.isEmpty()) {
                songSectionInfo.content = content;
                songSectionInfo.heading = heading;
                songSectionInfo.needsImage = !mainActivityInterface.getSong().getFiletype().equals("XML");
                songSectionInfo.position = x;
                songSections.add(songSectionInfo);
                Log.d(TAG, "content: " + songSectionInfo.content);
            }
        }
        notifyItemRangeChanged(0,mainActivityInterface.getSong().getSongSections().size());
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
        int section = si.position;
        boolean needsImage = si.needsImage;

        holder.content.setTypeface(Typeface.MONOSPACE);

        if (heading!=null && !heading.isEmpty()) {
            holder.heading.setText(heading);
            holder.heading.setVisibility(View.VISIBLE);
        } else {
            holder.heading.setText("");
            holder.heading.setVisibility(View.GONE);
        }

        if (content!=null && !content.isEmpty()) {
            holder.content.setText(content);
            holder.content.setVisibility(View.VISIBLE);
        } else {
            holder.content.setText("");
            holder.content.setVisibility(View.GONE);
        }
        if ((content==null||content.isEmpty()) && (heading==null||heading.isEmpty())) {
            holder.item.setVisibility(View.GONE);
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

        holder.itemView.setOnClickListener(view -> itemSelected(section));
        holder.itemView.setOnLongClickListener(view -> {
            // Open up the text for this section in a bottom sheet for editing
            TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(fragment,"SongSectionsFragment",heading,"",null,null,mainActivityInterface.getSong().getSongSections().get(section),false);
            textInputBottomSheet.show(mainActivityInterface.getMyFragmentManager(),"textInputBottomSheet");
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return songSections.size();
    }

    private void itemSelected(int thisPos) {
        int onColor = ContextCompat.getColor(c, R.color.colorSecondary);
        int offColor = ContextCompat.getColor(c, R.color.colorAltPrimary);

        for (int x=0; x<recyclerView.getChildCount(); x++) {
            CardView view = (CardView) recyclerView.getChildAt(x);
            if (x==thisPos) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.setBackgroundTintList(ColorStateList.valueOf(onColor));
                } else {
                    view.setBackgroundColor(onColor);
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.setBackgroundTintList(ColorStateList.valueOf(offColor));
                } else {
                    view.setBackgroundColor(offColor);
                }
            }
        }
        Log.d(TAG,"thisPos="+thisPos);
        displayInterface.presenterShowSection(thisPos);

    }
}

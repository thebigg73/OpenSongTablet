package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.DisplayInterface;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.TextInputBottomSheet;

import java.util.ArrayList;

public class SongSectionsAdapter extends RecyclerView.Adapter<SongSectionViewHolder> {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final DisplayInterface displayInterface;
    private ArrayList<SongSectionInfo> songSections;
    private final SongSectionsFragment fragment;
    private final String TAG = "SongSetionsAdapter";
    private final int onColor, offColor;
    private int selectedPosition = 0, sectionEdited = -1;

    SongSectionsAdapter(Context c, MainActivityInterface mainActivityInterface, SongSectionsFragment fragment,
                        DisplayInterface displayInterface) {
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
        this.displayInterface = displayInterface;
        this.fragment = fragment;
        onColor = ContextCompat.getColor(c, R.color.colorSecondary);
        offColor = ContextCompat.getColor(c, R.color.colorAltPrimary);
    }

    public void buildSongSections() {
        if (songSections == null) {
            songSections = new ArrayList<>();
        } else {
            int oldSize = songSections.size();
            songSections.clear();
            notifyItemRangeRemoved(0, oldSize);
        }

        Log.d(TAG, "title: " + mainActivityInterface.getSong().getTitle());
        Log.d(TAG, "songsectionsize: " + mainActivityInterface.getSong().getSongSections().size());
        for (int x = 0; x < mainActivityInterface.getSong().getSongSections().size(); x++) {
            // bits[0] = heading, bits[1] = content - heading
            String[] bits = splitHeadingAndContent(mainActivityInterface.getSong().getSongSections().get(x));

            SongSectionInfo songSectionInfo = new SongSectionInfo();
            songSectionInfo.heading = bits[0];
            songSectionInfo.content = bits[1];
            songSectionInfo.needsImage = !mainActivityInterface.getSong().getFiletype().equals("XML");
            songSectionInfo.position = x;
            songSections.add(songSectionInfo);
        }
        notifyItemRangeChanged(0, mainActivityInterface.getSong().getSongSections().size());
    }

    private String[] splitHeadingAndContent(String sectionContent) {
        String[] bits = new String[2];
        bits[0] = "";
        bits[1] = sectionContent;
        if (sectionContent.startsWith("[") && sectionContent.contains("]")) {
            // Extract the heading
            int toPos = sectionContent.indexOf("]");
            bits[0] = sectionContent.substring(0, toPos + 1);
            bits[1] = sectionContent.replace(bits[0], "").replace("[", "").
                    replace("]", "").trim();
            bits[0] = mainActivityInterface.getProcessSong().beautifyHeading(c, mainActivityInterface, bits[0]);
            bits[0] = bits[0].replace("[", "").replace("]", "").trim();
        }

        bits[0] = bits[0].trim();

        // Tidy up the content
        String[] lines = bits[1].split("\n");
        StringBuilder newContent = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith(".")) {
                line = line.replaceFirst(".", "");
            }
            if (line.startsWith(";")) {
                line = line.replaceFirst(";", "");
            }
            newContent.append(line).append("\n");
        }
        bits[1] = newContent.toString();
        return bits;
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
        if (position == selectedPosition) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.item.setBackgroundTintList(ColorStateList.valueOf(onColor));
                holder.edit.setBackgroundTintList(ColorStateList.valueOf(offColor));
            } else {
                holder.item.setBackgroundColor(onColor);
                holder.edit.setBackgroundColor(offColor);
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.item.setBackgroundTintList(ColorStateList.valueOf(offColor));
                holder.edit.setBackgroundTintList(ColorStateList.valueOf(onColor));
            } else {
                holder.item.setBackgroundColor(offColor);
                holder.edit.setBackgroundColor(onColor);
            }
        }
        boolean needsImage = si.needsImage;

        holder.content.setTypeface(mainActivityInterface.getMyFonts().getMonoFont());

        if (heading != null && !heading.isEmpty()) {
            holder.heading.setText(heading);
            holder.heading.setVisibility(View.VISIBLE);
        } else {
            holder.heading.setText("");
            holder.heading.setVisibility(View.GONE);
        }

        if (content != null && !content.isEmpty()) {
            holder.content.setText(content);
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

        holder.itemView.setOnClickListener(view -> itemSelected(section));
        holder.edit.setOnClickListener(view -> bottomSheetEdit(section));
    }

    private void bottomSheetEdit(int section) {
        // Keep a reference to this section
        sectionEdited = section;
        Log.d(TAG, "currentSongSection=" + mainActivityInterface.getSong().getSongSections().get(section));

        // Open up the text for this section in a bottom sheet for editing
        TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(fragment, "SongSectionsFragment",
                c.getString(R.string.edit_temporary), c.getString(R.string.content), null, null,
                mainActivityInterface.getSong().getSongSections().get(section), false);
        textInputBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "textInputBottomSheet");
    }

    @Override
    public int getItemCount() {
        return songSections.size();
    }

    private void itemSelected(int thisPos) {
        notifyItemChanged(selectedPosition);
        notifyItemChanged(thisPos);
        selectedPosition = thisPos;

        Log.d(TAG, "thisPos=" + thisPos);
        displayInterface.presenterShowSection(thisPos);
    }

    public void setSectionEdited(String content) {
        if (sectionEdited > -1) {
            try {
                // Update the song sections
                mainActivityInterface.getSong().getSongSections().set(sectionEdited, content);

                // Now edit the section card view to match
                String[] bits = splitHeadingAndContent(content);
                SongSectionInfo songSectionInfo = new SongSectionInfo();
                songSectionInfo.heading = bits[0];
                songSectionInfo.content = bits[1];
                songSectionInfo.needsImage = !mainActivityInterface.getSong().getFiletype().equals("XML");
                songSectionInfo.position = sectionEdited;
                songSections.set(sectionEdited, songSectionInfo);
                notifyItemChanged(sectionEdited);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sectionEdited = -1;
        }
    }
}

package com.garethevans.church.opensongtablet.presenter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
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
import java.util.List;

public class SongSectionsAdapter extends RecyclerView.Adapter<SongSectionViewHolder> {

    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final DisplayInterface displayInterface;
    private ArrayList<SongSectionInfo> songSections;
    private final SongSectionsFragment songSectionsFragment;
    private final int onColor, offColor;
    private int sectionEdited = -1, currentPosition = -1;
    private String sectionEditedContent, sectionEditedHeader = "";
    private final String colorChange = "color";
    private String newContent;
    private SparseBooleanArray highlightedArray = new SparseBooleanArray();
    private final String TAG = "SongSectionsAdapter";

    SongSectionsAdapter(Context c, MainActivityInterface mainActivityInterface,
                        SongSectionsFragment songSectionsFragment,
                        DisplayInterface displayInterface) {
        this.c = c;
        this.mainActivityInterface = mainActivityInterface;
        this.displayInterface = displayInterface;
        this.songSectionsFragment = songSectionsFragment;
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

        // Because we could be using presentation order, we need to get a temp song section for the buttons

        for (int x = 0; x < mainActivityInterface.getSong().getPresoOrderSongSections().size(); x++) {
            // bits[0] = heading, bits[1] = content - heading
            String[] bits = splitHeadingAndContent(mainActivityInterface.getSong().getPresoOrderSongSections().get(x));

            SongSectionInfo songSectionInfo = new SongSectionInfo();
            songSectionInfo.heading = bits[0];
            songSectionInfo.content = bits[1];
            songSectionInfo.needsImage = !mainActivityInterface.getSong().getFiletype().equals("XML");
            songSectionInfo.position = x;
            songSections.add(songSectionInfo);
        }
        highlightedArray = new SparseBooleanArray();
        notifyItemRangeChanged(0, mainActivityInterface.getSong().getPresoOrderSongSections().size());
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
            bits[0] = mainActivityInterface.getProcessSong().beautifyHeading(bits[0]);
            bits[0] = bits[0].replace("[", "").replace("]", "").trim();
        }

        bits[0] = bits[0].trim();

        // Tidy up the content
        bits[1] = tidyContent(bits[1]);
        return bits;
    }

    private String tidyContent(String str) {
        if (str.contains("____groupline____")) {
            str = str.replace("____groupline____","\n");
        } else {
            // Just text, so trim spaces
            str = mainActivityInterface.getProcessSong().fixExcessSpaces(str);
        }

        String[] lines = str.split("\n");
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
        return newContent.toString();
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
    public void onBindViewHolder(@NonNull SongSectionViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
        } else {
            // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
            for (Object payload : payloads) {
                if (payload.equals(colorChange)) {
                    // We want to update the highlight colour to on/off
                    if (highlightedArray.get(position,false)) {
                        setColor(holder, onColor, offColor);
                    } else {
                        setColor(holder, offColor, onColor);
                    }
                }
            }
        }
    }

    @Override
    public void onBindViewHolder(@NonNull SongSectionViewHolder holder, int position) {
        SongSectionInfo si = songSections.get(position);
        String heading = si.heading;
        String content = si.content;
        int section = si.position;
        if (highlightedArray.get(position,false)) {
            setColor(holder,onColor,offColor);
        } else {
            setColor(holder,offColor,onColor);
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

        //Log.d(TAG,"needsImage: "+needsImage);

        if (needsImage) {
            holder.image.setVisibility(View.VISIBLE);
            holder.heading.setVisibility(View.GONE);
            holder.content.setVisibility(View.GONE);
        } else {
            holder.image.setVisibility(View.GONE);
            holder.heading.setVisibility(View.VISIBLE);
            holder.content.setVisibility(View.VISIBLE);
        }

        // Disable the click sound as it might interfere with cast audio!
        holder.item.setSoundEffectsEnabled(false);
        holder.edit.setSoundEffectsEnabled(false);

        // Set the listeners
        holder.itemView.setOnClickListener(view -> itemSelected(section));
        holder.edit.setOnClickListener(view -> bottomSheetEdit(section));
    }

    private void bottomSheetEdit(int section) {
        // Keep a reference to this section
        sectionEdited = section;
        Log.d(TAG,"sectionEdited:"+sectionEdited);
        Log.d(TAG,"content before tweak:"+mainActivityInterface.getSong().getPresoOrderSongSections().get(section));
        sectionEditedContent = mainActivityInterface.getSong().getPresoOrderSongSections().get(section).replace("____groupline____","\n");
        Log.d(TAG,"content after tweak:"+sectionEditedContent);

        // Get the current header
        String[] lines = sectionEditedContent.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        sectionEditedHeader = "";
        for (String line:lines) {
            if (line.trim().startsWith("[") && line.trim().endsWith("]")) {
                sectionEditedHeader = line.trim();
            } else if (!line.trim().isEmpty()) {
                stringBuilder.append(line).append("\n");
            }
        }
        sectionEditedContent = stringBuilder.toString();

        Log.d(TAG, "sectionEditedHeader:"+sectionEditedHeader+"\nsectionEditedContent:"+sectionEditedContent);

        // Open up the text for this section in a bottom sheet for editing
        TextInputBottomSheet textInputBottomSheet = new TextInputBottomSheet(songSectionsFragment, "SongSectionsFragment",
                c.getString(R.string.edit_temporary), c.getString(R.string.content), null, null, sectionEditedContent, false);
        textInputBottomSheet.show(mainActivityInterface.getMyFragmentManager(), "textInputBottomSheet");
    }

    @Override
    public int getItemCount() {
        if (songSections==null) {
            return 0;
        } else {
            return songSections.size();
        }
    }

    private void setColor(SongSectionViewHolder holder, int cardColor, int buttonColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.item.setBackgroundTintList(ColorStateList.valueOf(cardColor));
            holder.edit.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        } else {
            holder.item.setBackgroundColor(cardColor);
            holder.edit.setBackgroundColor(buttonColor);
        }
    }

    public void itemSelected(int thisPos) {
        notifyItemChanged(thisPos);
        highlightedArray.put(currentPosition,false);
        highlightedArray.put(thisPos,true);
        notifyItemChanged(currentPosition,colorChange);
        notifyItemChanged(thisPos,colorChange);
        mainActivityInterface.getPresenterSettings().setCurrentSection(thisPos);
        displayInterface.presenterShowSection(thisPos);
        currentPosition = thisPos;

        // State we've started projection
        // This method checks that logo, black screen, blank screen are off too
        mainActivityInterface.getPresenterSettings().setStartedProjection(true);
    }

    public void setSectionEditedContent(String content) {
        Log.d(TAG,"returnedText:"+content);
        if (sectionEdited > -1) {
            try {
                // Update the song sections
                content = mainActivityInterface.getProcessSong().makeGroups(content,
                        mainActivityInterface.getPresenterSettings().getPresoShowChords());
                mainActivityInterface.getSong().getPresoOrderSongSections().set(sectionEdited, content);

                String newBits;
                if (sectionEditedHeader.isEmpty()) {
                    newBits = content;
                } else {
                    newBits = sectionEditedHeader + "\n" + content;
                }
                String[] bits = splitHeadingAndContent(newBits);

                SongSectionInfo songSectionInfo = new SongSectionInfo();
                Log.d(TAG,"bits[0]:"+bits[0]);
                Log.d(TAG,"bits[1]:"+bits[1]);
                songSectionInfo.heading = bits[0];
                songSectionInfo.content = bits[1];
                songSectionInfo.needsImage = !mainActivityInterface.getSong().getFiletype().equals("XML");
                songSectionInfo.position = sectionEdited;
                songSections.set(sectionEdited, songSectionInfo);
                notifyItemChanged(sectionEdited);

                // Now update the create views for second screen presenting
                newContent = content;
                displayInterface.updateDisplay("editView");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getSelectedPosition() {
        return mainActivityInterface.getPresenterSettings().getCurrentSection();
    }
    public void setSelectedPosition(int selectedPosition) {
        mainActivityInterface.getPresenterSettings().setCurrentSection(selectedPosition);
    }

    public String getNewContent() {
        return newContent;
    }
    public int getSectionEdited() {
        return sectionEdited;
    }
    public void setSectionEdited(int sectionEdited) {
        this.sectionEdited = sectionEdited;
    }
}

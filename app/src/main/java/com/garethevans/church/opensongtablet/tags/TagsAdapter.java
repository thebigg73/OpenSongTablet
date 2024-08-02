package com.garethevans.church.opensongtablet.tags;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.preferences.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder> {

    private final MainActivityInterface mainActivityInterface;
    private final ArrayList<String> tags, songsWithTags;
    private final ArrayList<Boolean> checked = new ArrayList<>();
    private final Fragment callingFragment;
    private final String fragName;
    private final Context c;
    private final FragmentManager fragmentManager;

    public TagsAdapter(Context c, MainActivityInterface mainActivityInterface,
                       FragmentManager fragmentManager,
                       Fragment callingFragment, String fragName) {
        this.mainActivityInterface = mainActivityInterface;
        this.callingFragment = callingFragment;
        this.fragName = fragName;
        this.c = c;
        this.fragmentManager = fragmentManager;

        // Search the song database for any existing tags to choose from
        tags = mainActivityInterface.getSQLiteHelper().getThemeTags();

        // Also add any in the current temp song tags if they aren't there already
        String currTag = mainActivityInterface.getTempSong().getTheme();
        if (currTag==null || currTag.isEmpty()) {
            currTag = ";";
        }

        String[] currTags = currTag.split(";");
        for (String tag:currTags) {
            if (!tag.trim().isEmpty() && !tags.contains(tag.trim())) {
                tags.add(tag.trim());
            }
        }
        // Sort again
        Collator coll = Collator.getInstance(mainActivityInterface.getLocale());
        coll.setStrength(Collator.SECONDARY);
        Collections.sort(tags, coll);

        // Now get the songs which have these tags
        songsWithTags = new ArrayList<>();
        for (int x=0; x<tags.size(); x++) {
            String which = mainActivityInterface.getSQLiteHelper().songsWithThemeTags("%"+tags.get(x)+"%");

            // Check if it is only in the current tempSong
            if (mainActivityInterface.getTempSong().getTheme().contains(tags.get(x)) &&
                !which.contains(mainActivityInterface.getTempSong().getSongid())) {
                which = which + ", " + mainActivityInterface.getTempSong().getSongid();
                if (which.startsWith(",")) {
                    which = which.replaceFirst(",", "").trim();
                }
            }
            songsWithTags.add(which);
        }
        initialiseCheckedArray();
    }

    @NonNull
    @Override
    public TagsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View cardView = inflater.inflate(R.layout.view_tag_item, parent, false);

        // Return a new holder instance
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Set item views based on your views and data model
        CheckBox tagName = holder.tagName;
        TextView matchingSongs = holder.matchingSongs;
        tagName.setText(tags.get(position));
        matchingSongs.setText(songsWithTags.get(position));
        FloatingActionButton tagDelete = holder.tagDelete;
        tagDelete.setOnClickListener(view -> {
            String textToFind = holder.tagName.getText().toString();
            positionToRemove = tags.indexOf(textToFind);
            removeThemeTag();
        });
        // Set the checkbox if the song is in the set
        bindCheckBox(holder.tagName, position);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public CheckBox tagName;
        public TextView matchingSongs;
        public FloatingActionButton tagDelete;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            tagName = itemView.findViewById(R.id.tagName);
            matchingSongs = itemView.findViewById(R.id.matchingSongs);
            tagDelete = itemView.findViewById(R.id.tagDelete);
        }
    }

    void bindCheckBox(CheckBox checkBox, int position) {
        // use the sparse boolean array to check
        checkBox.setChecked(checked.get(position));
        // Don't use checkchange listener as this happens when recyclerview draws on scroll
        checkBox.setOnClickListener(v -> updateThemeTags(checkBox.isChecked(),tags.get(position)));
    }

    private void initialiseCheckedArray() {
        for (int i = 0; i < tags.size(); i++) {
            if (mainActivityInterface.getTempSong().getTheme().contains(tags.get(i))) {
                checked.add(i, true);
            } else {
                checked.add(i, false);
            }
        }
    }

    private void updateThemeTags(boolean add, String themeTag) {
        String currThemeTag = mainActivityInterface.getTempSong().getTheme();

        // Do either add or remove
        if (add) {
            if (!currThemeTag.contains(themeTag + ";") &&
                    !currThemeTag.endsWith(themeTag)) {
                currThemeTag = currThemeTag + "; " + themeTag;
            }

        } else {
            if (currThemeTag.contains(themeTag + ";")) {
                currThemeTag = currThemeTag.replace(themeTag + "; ", "");
            } else if (currThemeTag.endsWith(themeTag)) {
                currThemeTag = currThemeTag.replace(themeTag,"");
            }

        }
        currThemeTag = mainActivityInterface.getProcessSong().tidyThemeString(currThemeTag);

        mainActivityInterface.getTempSong().setTheme(currThemeTag);
        // Update the edit song fragment
        mainActivityInterface.updateFragment(fragName, callingFragment, null);

    }

    public void insertThemeTag(String themeTag) {
        if (!tags.contains(themeTag)) {
            tags.add(0, themeTag);
            songsWithTags.add(0, mainActivityInterface.getTempSong().getSongid());
            checked.add(0,true);
            notifyItemInserted(0);
        }
        mainActivityInterface.updateFragment(fragName,callingFragment,null);
    }

    private void removeThemeTag() {
        // This is step 1.  We now need to check the 'are you sure' prompt/
        // This will warn users that this tag will be removed from all songs that have it
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(tags.get(positionToRemove));
        arguments.add(positionToRemove+"");
        AreYouSureBottomSheet bottomSheet = new AreYouSureBottomSheet(
                "removeThemeTag",
                "\"" + tags.get(positionToRemove) + "\"\n\n" + c.getString(R.string.tags_remove_from_songs) + "\n\n" +
                        songsWithTags.get(positionToRemove), arguments,
                fragName, callingFragment, null);
        bottomSheet.show(fragmentManager,"AreYouSureBottomSheet");
    }
    private int positionToRemove = -1;
    public void confirmedRemoveThemeTag(int position) {
        tags.remove(position);
        songsWithTags.remove(position);
        checked.remove(position);
        notifyItemRemoved(position);
    }
}

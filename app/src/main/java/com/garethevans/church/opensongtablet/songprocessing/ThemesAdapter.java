package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
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
import com.garethevans.church.opensongtablet.filemanagement.AreYouSureBottomSheet;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class ThemesAdapter extends RecyclerView.Adapter<ThemesAdapter.ViewHolder> {

    private final MainActivityInterface mainActivityInterface;
    private final ArrayList<String> tags, songsWithTags;
    private final SparseBooleanArray checkedArray = new SparseBooleanArray();
    private final String TAG = "ThemesAdapter";
    private final Fragment callingFragment;
    private final String fragName;
    private final Context c;
    private final FragmentManager fragmentManager;

    public ThemesAdapter(Context c, MainActivityInterface mainActivityInterface,
                         FragmentManager fragmentManager,
                         Fragment callingFragment, String fragName) {
        this.mainActivityInterface = mainActivityInterface;
        this.callingFragment = callingFragment;
        this.fragName = fragName;
        this.c = c;
        this.fragmentManager = fragmentManager;

        // Search the song database for any existing tags to choose from
        tags = mainActivityInterface.getSQLiteHelper().getThemeTags(c,mainActivityInterface);

        // Now get the songs which have these tags
        songsWithTags = new ArrayList<>();
        for (int x=0; x<tags.size(); x++) {
            songsWithTags.add(mainActivityInterface.getSQLiteHelper().songsWithThemeTags(c,
                    mainActivityInterface,"%"+tags.get(x)+"%"));
        }

        initialiseCheckedArray();
    }

    @NonNull
    @Override
    public ThemesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.view_theme_item, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
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
            Log.d(TAG,"positionToRemove: "+positionToRemove);
            removeThemeTag();
        });
        // Set the checkbox if the song is in the set
        bindCheckBox(holder.tagName, position);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

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
        checkBox.setChecked(checkedArray.get(position,false));
        checkBox.setOnCheckedChangeListener((compoundButton, b) -> updateThemeTags(b, tags.get(position)));
    }

    private void initialiseCheckedArray() {
        Log.d(TAG,"getTheme()="+mainActivityInterface.getTempSong().getTheme());
        for (int i = 0; i < tags.size(); i++) {
            Log.d(TAG, "tags.get(i): "+tags.get(i));
            if (mainActivityInterface.getTempSong().getTheme().contains(tags.get(i))) {
                checkedArray.put(i, true);
            }
        }
    }

    private void updateThemeTags(boolean add, String themeTag) {
        String currThemeTag = mainActivityInterface.getTempSong().getTheme();

        // Get rid of leading or trailing ; separator
        currThemeTag = clearLeadingSeparator(currThemeTag);
        currThemeTag = clearTrailingSeparator(currThemeTag);

        // Do either add or remove
        if (add) {
            if (!currThemeTag.contains(themeTag + "; ") &&
                    !currThemeTag.endsWith(themeTag)) {
                currThemeTag = currThemeTag + "; " + themeTag;
            }

        } else {
            if (currThemeTag.contains(themeTag + "; ")) {
                currThemeTag = currThemeTag.replace(themeTag + "; ", "");
            } else if (currThemeTag.endsWith(themeTag)) {
                currThemeTag = currThemeTag.replace(themeTag,"");
            }

        }
        // Get rid of leading or trailing ; separator
        currThemeTag = clearLeadingSeparator(currThemeTag);
        currThemeTag = clearTrailingSeparator(currThemeTag);

        mainActivityInterface.getTempSong().setTheme(currThemeTag);
        // Update the edit song fragment
        mainActivityInterface.updateFragment(fragName, callingFragment, null);

    }

    private String clearLeadingSeparator(String currThemeTag) {
        // Get rid of leading ; separator
        if (currThemeTag.startsWith("; ")) {
            currThemeTag = currThemeTag.substring(2);
        } else if (currThemeTag.startsWith(";")) {
            currThemeTag = currThemeTag.substring(1);
        }
        return currThemeTag;
    }
    private String clearTrailingSeparator(String currThemeTag) {
        if (currThemeTag.endsWith("; ")) {
            currThemeTag = currThemeTag.substring(0, currThemeTag.lastIndexOf("; "));
        } else if (currThemeTag.endsWith(";")) {
            currThemeTag = currThemeTag.substring(0, currThemeTag.lastIndexOf(";"));
        }
        return currThemeTag;
    }

    public void insertThemeTag(String themeTag) {
        if (!tags.contains(themeTag)) {
            tags.add(0, themeTag);
            songsWithTags.add(0, mainActivityInterface.getTempSong().getFolder() +
                    "/" + mainActivityInterface.getTempSong().getFilename());
            checkedArray.put(0,true);
            notifyItemInserted(0);
        }
        mainActivityInterface.updateFragment(fragName,callingFragment,null);
    }

    private void removeThemeTag() {
        // This is step 1.  We now need to check the 'are you sure' prompt/
        // This will warn users that this tag will be removed from all songs that have it
        Log.d(TAG, "about to call are you sure: "+songsWithTags.get(positionToRemove));
        ArrayList<String> arguments = new ArrayList<>();
        arguments.add(tags.get(positionToRemove));
        arguments.add(positionToRemove+"");
        AreYouSureBottomSheet bottomSheet = new AreYouSureBottomSheet(
                "removeThemeTag",
                c.getString(R.string.tags_remove_from_songs) + "\n" +
                        songsWithTags.get(positionToRemove), arguments,
                fragName, callingFragment, null);
        bottomSheet.show(fragmentManager,"AreYouSureBottomSheet");
    }
    private int positionToRemove = -1;
    public void confirmendRemoveThemeTag() {
        tags.remove(positionToRemove);
        songsWithTags.remove(positionToRemove);
        checkedArray.delete(positionToRemove);
        notifyItemRemoved(positionToRemove);
        mainActivityInterface.updateFragment(fragName,callingFragment,null);
    }
}

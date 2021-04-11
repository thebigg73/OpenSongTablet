package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.filemanagement.LoadSong;
import com.garethevans.church.opensongtablet.filemanagement.StorageAccess;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.preferences.Preferences;
import com.garethevans.church.opensongtablet.screensetup.ShowToast;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
import com.garethevans.church.opensongtablet.setprocessing.SetActions;
import com.garethevans.church.opensongtablet.songprocessing.ProcessSong;
import com.garethevans.church.opensongtablet.songprocessing.Song;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.Collections;
import java.util.List;

public class SetListAdapter extends RecyclerView.Adapter<SetItemViewHolder> implements FastScrollRecyclerView.SectionedAdapter  {

    // All the helpers we need to access are in the MainActivity
    private final MainActivityInterface mainActivityInterface;
    private final List<SetItemInfo> setList;

    SetListAdapter(MainActivityInterface mainActivityInterface, List<SetItemInfo> setList) {
        this.mainActivityInterface = mainActivityInterface;
        this.setList = setList;
    }

    @Override
    public int getItemCount() {
        return setList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull SetItemViewHolder setitemViewHolder, int i) {
        Context c = mainActivityInterface.getActivity();
        SetItemInfo si = setList.get(i);
        String key = si.songkey;
        String titlesongname = si.songtitle;
        if (!key.equals("")) {
            titlesongname = titlesongname + " ("+key+")";
        }

        setitemViewHolder.vItem.setText(si.songitem);
        String newfoldername = si.songfolder;
        if (newfoldername.startsWith("**")) {
            newfoldername = newfoldername.replace("**","");
        }
        setitemViewHolder.vSongTitle.setText(titlesongname);
        setitemViewHolder.vSongFolder.setText(newfoldername);
        boolean issong = false;
        if (si.songicon.equals(c.getResources().getString(R.string.slide))) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_projector_screen_white_36dp);
        } else if (si.songicon.equals(c.getResources().getString(R.string.note))) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_note_text_white_36dp);
        } else if (si.songicon.equals(c.getResources().getString(R.string.scripture))) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_book_white_36dp);
        } else if (si.songicon.equals(c.getResources().getString(R.string.image))) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_image_white_36dp);
        } else if (si.songicon.equals(c.getResources().getString(R.string.variation))) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_file_xml_white_36dp);
        } else if (si.songicon.equals(".pdf")) {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_file_pdf_white_36dp);
        } else {
            setitemViewHolder.vIcon.setImageResource(R.drawable.ic_music_note_white_36dp);
            issong = true;
        }

        String folderrelocate;
        if (si.songicon.equals(c.getResources().getString(R.string.image))) {
            folderrelocate = "../Images/_cache";
        } else if (si.songicon.equals(c.getResources().getString(R.string.note))) {
            folderrelocate = "../Notes/_cache";
        } else if (si.songicon.equals(c.getResources().getString(R.string.scripture))) {
            folderrelocate = "../Scripture/_cache";
        } else if (si.songicon.equals(c.getResources().getString(R.string.slide))) {
            folderrelocate = "../Slides/_cache";
        } else if (si.songicon.equals(c.getResources().getString(R.string.variation))) {
            folderrelocate = "../Variations";
        } else {
            folderrelocate = si.songfolder;
        }

        final String songname = si.songtitle;
        final String songfolder = folderrelocate;

        setitemViewHolder.vCard.setOnClickListener(v -> {
            Song song = mainActivityInterface.getSong();
            song.setFolder(songfolder);
            song.setFilename(songname);
            SetActions setActions = mainActivityInterface.getSetActions();
            CurrentSet currentSet = mainActivityInterface.getCurrentSet();
            Preferences preferences = mainActivityInterface.getPreferences();
            StorageAccess storageAccess = mainActivityInterface.getStorageAccess();
            ShowToast showToast = mainActivityInterface.getShowToast();
            LoadSong loadSong = mainActivityInterface.getLoadSong();
            ProcessSong processSong = mainActivityInterface.getProcessSong();
            setActions.setSongForSetWork(c,songfolder,songname);
            setActions.indexSongInSet(currentSet,song);

            if (mainActivityInterface.getWhattodo().equals("setitemvariation")) {
                setActions.makeVariation(c, storageAccess, preferences, currentSet, processSong, showToast, song, loadSong);

            } else {
                // TODO
                // Run on main activity and send to either presentation or performance fragment
            }
        });
        if (mainActivityInterface.getWhattodo().equals("setitemvariation") && !issong) {
            // Only songs should be able to have variations
            setitemViewHolder.vCard.setOnClickListener(null);
        }
    }

    @NonNull
    @Override
    public SetItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.setitem_cardview, viewGroup, false);

        return new SetItemViewHolder(itemView);
    }



    public void swap(CurrentSet currentSet, int firstPosition, int secondPosition){
        try {
            Collections.swap(setList, firstPosition, secondPosition);
            notifyItemMoved(firstPosition, secondPosition);
            Collections.swap(currentSet.getCurrentSet(), firstPosition, secondPosition);
            Collections.swap(currentSet.getCurrentSet_Folder(), firstPosition, secondPosition);
            Collections.swap(currentSet.getCurrentSet_Filename(), firstPosition, secondPosition);
            Collections.swap(currentSet.getCurrentSet_Key(), firstPosition, secondPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(CurrentSet currentSet, int position) {
        try {
            setList.remove(position);
            notifyItemRemoved(position);
            currentSet.getCurrentSet().remove(position);
            currentSet.getCurrentSet_Folder().remove(position);
            currentSet.getCurrentSet_Filename().remove(position);
            currentSet.getCurrentSet_Key().remove(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return null;
    }
}
package com.garethevans.church.opensongtablet.songsandsetsmenu;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setprocessing.CurrentSet;
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
        if (key!=null && !key.isEmpty()) {
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
        int icon;
        if (si.songicon.equals(c.getResources().getString(R.string.slide))) {
            icon = R.drawable.ic_projector_screen_white_36dp;
        } else if (si.songicon.equals(c.getResources().getString(R.string.note))) {
            icon = R.drawable.ic_note_text_white_36dp;
        } else if (si.songicon.equals(c.getResources().getString(R.string.scripture))) {
            icon = R.drawable.ic_book_white_36dp;
        } else if (si.songicon.equals(c.getResources().getString(R.string.image))) {
            icon = R.drawable.ic_image_white_36dp;
        } else if (si.songicon.equals(c.getResources().getString(R.string.variation))) {
            icon = R.drawable.ic_file_xml_white_36dp;
        } else if (si.songicon.equals(".pdf")) {
            icon = R.drawable.ic_file_pdf_white_36dp;
        } else {
            icon = R.drawable.ic_music_note_white_36dp;
            issong = true;
        }
        setitemViewHolder.vItem.setCompoundDrawablesWithIntrinsicBounds(icon,0,0,0);

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

        Log.d("SetListAdapter","songtitle="+si.songtitle+"  songfolder="+si.songfolder+ "  songkey="+si.songkey);

        setitemViewHolder.vCard.setOnClickListener(v -> {
            Song song = mainActivityInterface.getSong();
            song.setFolder(songfolder);
            song.setFilename(songname);

            Log.d("d","songtitle="+si.songtitle+"  songfolder="+si.songfolder+ "  songkey="+si.songkey);


            if (mainActivityInterface.getWhattodo().equals("setitemvariation")) {
                // TODO add this back in
                // setActions.makeVariation(c, mainActivityInterface);

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
                inflate(R.layout.menu_sets_item, viewGroup, false);

        return new SetItemViewHolder(itemView);
    }



    public void swap(CurrentSet currentSet, int firstPosition, int secondPosition){
        try {
            Collections.swap(setList, firstPosition, secondPosition);
            notifyItemMoved(firstPosition, secondPosition);
            Collections.swap(currentSet.getSetItems(), firstPosition, secondPosition);
            Collections.swap(currentSet.getSetFolders(), firstPosition, secondPosition);
            Collections.swap(currentSet.getSetFilenames(), firstPosition, secondPosition);
            Collections.swap(currentSet.getSetKeys(), firstPosition, secondPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void remove(CurrentSet currentSet, int position) {
        try {
            setList.remove(position);
            notifyItemRemoved(position);
            currentSet.getSetItems().remove(position);
            currentSet.getSetFolders().remove(position);
            currentSet.getSetFilenames().remove(position);
            currentSet.getSetKeys().remove(position);
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
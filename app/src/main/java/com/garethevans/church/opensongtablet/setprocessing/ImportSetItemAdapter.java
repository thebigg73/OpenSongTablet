package com.garethevans.church.opensongtablet.setprocessing;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MyMaterialTextView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.garethevans.church.opensongtablet.songprocessing.Song;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;

public class ImportSetItemAdapter  extends RecyclerView.Adapter<ImportSetItemViewHolder> {

    private final String TAG = "ImportSetItemAdapter";
    private final MainActivityInterface mainActivityInterface;
    private final int alreadyExistsColor, doesntExistColor;
    private final String processing_string;
    private final ArrayList<Boolean> itemsChecked;

    public ImportSetItemAdapter(Context c) {
        mainActivityInterface = (MainActivityInterface) c;
        processing_string = c.getString(R.string.processing);
        alreadyExistsColor = mainActivityInterface.getPalette().errorColor;
        doesntExistColor = mainActivityInterface.getPalette().secondary;
        itemsChecked = new ArrayList<>();
        Log.d(TAG,"currentSet.size():"+mainActivityInterface.getCurrentSet().getCurrentSetSize());
        for (int i=0;i<mainActivityInterface.getCurrentSet().getCurrentSetSize();i++) {
            boolean itemExists = mainActivityInterface.getSQLiteHelper().songExists(
                    mainActivityInterface.getCurrentSet().getSetItemInfo(i).songfolder,
                    mainActivityInterface.getCurrentSet().getSetItemInfo(i).songfilename);
            itemsChecked.add(!itemExists);
        }
    }

    @NonNull
    @Override
    public ImportSetItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.view_set_item, parent, false);

        return new ImportSetItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ImportSetItemViewHolder holder, int pos) {
        int position = holder.getAbsoluteAdapterPosition();
        boolean checked = itemsChecked.get(position);
        SetItemInfo si = mainActivityInterface.getCurrentSet().getSetItemInfo(position);
        String key = si.songkey;
        si.songitem = position+1;
        String titlesongname = si.songtitle;
        String filename = si.songfilename;
        String foldername = si.songfolder;
        String newfoldername = si.songfoldernice;

        if (key != null && !key.equals("null") && !key.isEmpty()) {
            titlesongname = titlesongname + " (" + key + ")";
        } else {
            si.songkey = "";
        }

        String text = si.songitem + ".";
        holder.cardItem.setText(text);

        //holder.cardTitle.setTextSize();
        holder.cardTitle.setText(titlesongname);
        //holder.cardFilename.setTextSize(titleSize);
        holder.cardFilename.setText(filename);
        //holder.cardFolder.setTextSize(subtitleSizeFile);
        holder.cardFilename.setVisibility(View.VISIBLE);
        holder.cardTitle.setVisibility(View.GONE);
        holder.cardFolder.setText(newfoldername);

        if (si.songicon==null || si.songicon.isEmpty()) {
            si.songicon = mainActivityInterface.getSetActions().getIconIdentifier(foldername,filename);
        }

        // Set the icon
        int icon = mainActivityInterface.getSetActions().getItemIcon(si.songicon);
        holder.cardItem.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);

        // If this song already exists in our database, let us know by color
        boolean exists = mainActivityInterface.getSQLiteHelper().songExists(newfoldername,filename);
        Log.d(TAG,"song "+newfoldername+"/"+filename+" exists? "+exists);
        holder.cardExists.setVisibility(exists ? View.VISIBLE:View.GONE);

        holder.cardCheckBox.setChecked(checked);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
                                               @Override
                                               public void onClick(View view) {
                                                   itemsChecked.set(position,!itemsChecked.get(position));
                                                   holder.cardCheckBox.setChecked(itemsChecked.get(position));
                                               }

        });
    }

    @Override
    public int getItemCount() {
        return mainActivityInterface.getCurrentSet().getCurrentSetSize();
    }

    public void importSelectedSongs(ImportSetBundleSongFragment importSetBundleSongFragment, MyMaterialTextView progressTextView) {
        // Go through the selected array
        for (int i=0;i<itemsChecked.size();i++) {
            if (itemsChecked.get(i) && mainActivityInterface.getOpenSongSetBundle().getAlive()) {
                // We want this file!
                SetItemInfo si = mainActivityInterface.getCurrentSet().getSetItemInfo(i);
                if (progressTextView != null) {
                    try {
                        progressTextView.post(() -> progressTextView.setHint(si.songfilename));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                File itemFile = mainActivityInterface.getOpenSongSetBundle().getItemFile(si.songfolder, si.songfilename);
                if (itemFile != null) {
                    // We have the file and we can now import it
                    Log.d(TAG, "itemFile:" + itemFile.getName());
                    Uri outputUri = null;
                    // Simply copy this file to the new location
                    outputUri = mainActivityInterface.getStorageAccess().getUriForItem("Songs", si.songfolder, si.songfilename);
                    mainActivityInterface.getStorageAccess().lollipopCreateFileForOutputStream(true, outputUri, null, "Songs", si.songfolder, si.songfilename);
                    if (outputUri != null) {
                        OutputStream outputStream = mainActivityInterface.getStorageAccess().getOutputStream(outputUri);
                        Log.d(TAG, "copy " + itemFile.getName() + " to " + outputUri);
                        if (mainActivityInterface.getStorageAccess().copyFile(mainActivityInterface.getStorageAccess().getInputStream(Uri.fromFile(itemFile)), outputStream)) {
                            // Make sure it is in our database(s) and if not, add it
                            Song tempSong = new Song();
                            tempSong.setFolder(si.songfolder);
                            tempSong.setFilename(si.songfilename);
                            tempSong.setTitle(si.songtitle);
                            if (!mainActivityInterface.getSQLiteHelper().songExists(si.songfolder, si.songfilename)) {
                                mainActivityInterface.getSQLiteHelper().createSong(si.songfolder, si.songfilename);
                            }
                            if (mainActivityInterface.getStorageAccess().isIMGorPDF(si.songfilename) &&
                                    !mainActivityInterface.getNonOpenSongSQLiteHelper().songExists(si.songfolder, si.songfilename)) {
                                // Add to the non-songs database
                                mainActivityInterface.getNonOpenSongSQLiteHelper().createSong(si.songfolder, si.songfilename);
                            } else {
                                tempSong = mainActivityInterface.getLoadSong().doLoadSongFile(tempSong, false);
                                Log.d(TAG,"lyrics:"+tempSong.getLyrics());
                                mainActivityInterface.getSQLiteHelper().updateSong(tempSong);
                            }
                            mainActivityInterface.updateSongMenu(tempSong);
                        }
                        ;
                    }
                }
            }
            mainActivityInterface.getShowToast().success();
        }
        if (importSetBundleSongFragment!=null) {
            try {
                importSetBundleSongFragment.finishedImporting();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

package com.garethevans.church.opensongtablet.openchords;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.OpenChordsFolderUUIDLayout;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.util.ArrayList;


public class OpenChordsFolderUUIDAdapter extends RecyclerView.Adapter<OpenChordsFolderUUIDViewHolder> {

    private final String TAG = "OpenChordsFolderAdapter";
    private final Context c;
    private final MainActivityInterface mainActivityInterface;
    private final ArrayList<OpenSongFolderRecordObject> folderRecordObjects;
    private final View anchor;

    public OpenChordsFolderUUIDAdapter(Context c, View anchor) {
        this.c = c;
        mainActivityInterface = (MainActivityInterface) c;
        folderRecordObjects = mainActivityInterface.getOpenChordsAPI().getOpenSongFolderRecordObjects();
        this.anchor = anchor;
    }

    @NonNull
    @Override
    public OpenChordsFolderUUIDViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OpenChordsFolderUUIDLayout customView = new OpenChordsFolderUUIDLayout(parent.getContext());
        customView.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        return new OpenChordsFolderUUIDViewHolder(customView);
    }

    @Override
    public void onBindViewHolder(@NonNull OpenChordsFolderUUIDViewHolder holder, int position) {
        OpenSongFolderRecordObject record = folderRecordObjects.get(position);
        holder.bind(c, record.getFolderUuid(), record.getFolderName(), mainActivityInterface, isLocked -> {
            Log.d(TAG,"lock clicked");
            // Handle lock action for this specific position/record
            if (isLocked) {
                // Check for a valid UUID
                if (holder.getFolderUUIDLayout().checkUUIDValid()) {
                    // Update the uuid for this folder
                    String folder = holder.getFolderUUIDLayout().getFolderName();
                    String uuid = holder.getFolderUUIDLayout().getText();
                    if (!uuid.equals(holder.getFolderUUIDLayout().getFolderUUID())) {
                        record.setFolderUuid(uuid);
                        holder.getFolderUUIDLayout().setFolderUUID(uuid);
                        Log.d(TAG, "update folder:" + folder + "  to:" + uuid);
                        mainActivityInterface.getOpenChordsAPI().updateFolderObjectUUID(position,uuid);
                    } else {
                        Log.d(TAG,"unchanged");
                    }
                } else {
                    // Reset back to default
                    String uuid = holder.getFolderUUIDLayout().getFolderUUID();
                    holder.getFolderUUIDLayout().setFolderUUID(uuid);
                    Log.d(TAG,"reset folder");
                    mainActivityInterface.getShowToast().doItBottomSheet(c.getString(R.string.uuid_not_valid),anchor);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (folderRecordObjects==null) {
            return 0;
        } else {
            return folderRecordObjects.size();
        }
    }
}

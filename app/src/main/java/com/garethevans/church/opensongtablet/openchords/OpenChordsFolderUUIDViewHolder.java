package com.garethevans.church.opensongtablet.openchords;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.garethevans.church.opensongtablet.customviews.OpenChordsFolderUUIDLayout;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

public class OpenChordsFolderUUIDViewHolder extends RecyclerView.ViewHolder {

    private final OpenChordsFolderUUIDLayout folderUUIDLayout;

    public OpenChordsFolderUUIDViewHolder(@NonNull View itemView) {
        super(itemView);
        folderUUIDLayout = (OpenChordsFolderUUIDLayout) itemView;
    }

    public void bind(Context context, String uuid, String name,
                     MainActivityInterface mainActivityInterface,
                     OpenChordsFolderUUIDLayout.OnFolderActionListener listener) {

        folderUUIDLayout.initialise(context, uuid, name);
        folderUUIDLayout.setOnFolderActionListener(listener);
        folderUUIDLayout.setFolderUUID(uuid);
        folderUUIDLayout.setFolderName(name);
    }

    public OpenChordsFolderUUIDLayout getFolderUUIDLayout() {
        return folderUUIDLayout;
    }
}

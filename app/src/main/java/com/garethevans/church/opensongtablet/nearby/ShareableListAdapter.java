package com.garethevans.church.opensongtablet.nearby;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ShareableListAdapter extends RecyclerView.Adapter<ShareableListAdapter.ViewHolder> {
    @NonNull
    @Override
    public ShareableListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ShareableListAdapter.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private CheckBox checkBox;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

        }
    }
}

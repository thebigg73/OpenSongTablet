package com.garethevans.church.opensongtablet.appdata;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;

import java.util.List;

public class SettingsCategoryAdapter extends RecyclerView.Adapter<SettingsCategoryViewHolder> {

    private final List<SettingsCategory> categories;
    private final View.OnClickListener clickListener;

    public SettingsCategoryAdapter(List<SettingsCategory> categories, View.OnClickListener clickListener) {
        this.categories = categories;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public SettingsCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_settings_category, parent, false);
        return new SettingsCategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SettingsCategoryViewHolder holder, int position) {
        SettingsCategory category = categories.get(position);
        holder.title.setText(category.getTitle());
        holder.icon.setImageResource(category.getIconRes());
        holder.itemView.setTag(category);
        holder.itemView.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }


}
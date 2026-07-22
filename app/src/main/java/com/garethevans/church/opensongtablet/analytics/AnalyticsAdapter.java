package com.garethevans.church.opensongtablet.analytics;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnalyticsAdapter extends RecyclerView.Adapter<AnalyticsViewHolder> {
    private final List<AnalyticsItem> items;
    private final OnSongClickListener listener;
    private final SimpleDateFormat formatter = new SimpleDateFormat("MMM d yyyy", Locale.getDefault());
    private final Context c;
    private final MainActivityInterface mainActivityInterface;

    // Add constructor
    public AnalyticsAdapter(Context c, List<AnalyticsItem> items, OnSongClickListener listener) {
        this.c = c;
        this.mainActivityInterface = (MainActivityInterface) c;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnalyticsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the MaterialCardView layout you provided
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_analytics_item, parent, false);
        AnalyticsViewHolder analyticsViewHolder = new AnalyticsViewHolder(view);
        Drawable drawable = ResourcesCompat.getDrawable(c.getResources(),R.drawable.circle,c.getTheme());
        if (drawable!=null) {
            DrawableCompat.setTint(drawable, mainActivityInterface.getPalette().secondary);
            analyticsViewHolder.counter.setBackground(drawable);
        }
        return new AnalyticsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AnalyticsViewHolder holder, int position) {
        AnalyticsItem item = items.get(position);

        holder.songInfo.setText(item.filename);
        holder.songInfo.setHint(item.folder);

        // Reset visibility to visible in case views are recycled
        holder.counter.setVisibility(View.VISIBLE);

        switch (SortMode.lastSortMethod) {
            case SortMode.LAST_CAST:
                holder.counter.setVisibility(View.GONE);
                holder.date.setText(item.lastCast > 0 ? formatter.format(new Date(item.lastCast)) : "Never");
                break;
            case SortMode.LAST_SET:
            case SortMode.COUNT_SET:
                holder.counter.setVisibility(View.VISIBLE);
                holder.counter.setText(String.valueOf(item.setCount));
                holder.date.setText(item.lastAddToSet > 0 ? formatter.format(new Date(item.lastAddToSet)) : "Never");
                break;
            case SortMode.LAST_VIEWED:
            case SortMode.POPULARITY:
            default:
                holder.counter.setVisibility(View.VISIBLE);
                holder.counter.setText(String.valueOf(item.viewCount));
                holder.date.setText(item.lastViewed > 0 ? formatter.format(new Date(item.lastViewed)) : "Never");
                break;
        }

        holder.itemView.setOnClickListener(v -> listener.onSongClick(item));
    }

    @Override
    public int getItemCount() { return items.size(); }

    public interface OnSongClickListener { void onSongClick(AnalyticsItem song); }
}
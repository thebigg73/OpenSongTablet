package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;

public class FastScrollRecyclerView extends RecyclerView {

    private FastScroller fastScroller;
    private final String TAG = "FastScrollRecyclerView";

    public FastScrollRecyclerView(@NonNull Context context) {
        super(context);
        layout(context, null);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }


    public FastScrollRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FastScrollRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        layout(context, attrs);
    }


    @Override
    public void setAdapter(@Nullable Adapter adapter) {
        super.setAdapter(adapter);

        if (adapter instanceof FastScroller.SectionIndexer) {
            fastScroller.setSectionIndexer((FastScroller.SectionIndexer) adapter);
        } else if (adapter == null) {
            fastScroller.setSectionIndexer(null);
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        fastScroller.setVisibility(visibility);
    }

    public void setFastScrollListener(@Nullable FastScroller.FastScrollListener fastScrollListener) {
        fastScroller.setFastScrollListener(fastScrollListener);
    }

    public void setSectionIndexer(@Nullable FastScroller.SectionIndexer sectionIndexer) {
        fastScroller.setSectionIndexer(sectionIndexer);
    }

    public void setFastScrollEnabled(boolean enabled) {
        fastScroller.setEnabled(enabled);
    }

    public void setHideScrollbar(boolean hideScrollbar) {
        fastScroller.setHideScrollbar(hideScrollbar);
    }

    public void setTrackVisible(boolean visible) {
        fastScroller.setTrackVisible(visible);
    }

    public void setTrackColor(@ColorInt int color) {
        fastScroller.setTrackColor(color);
    }

    public void setHandleColor(@ColorInt int color) {
        fastScroller.setHandleColor(color);
    }

    public void setBubbleVisible(boolean visible) {
        fastScroller.setBubbleVisible(visible);
    }

    public void setBubbleColor(@ColorInt int color) {
        fastScroller.setBubbleColor(color);
    }

    public void setBubbleTextColor(@ColorInt int color) {
        fastScroller.setBubbleTextColor(color);
    }

    public void setBubbleTextSize(int size) {
        fastScroller.setBubbleTextSize(size);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        fastScroller.attachRecyclerView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        fastScroller.detachRecyclerView();
        super.onDetachedFromWindow();
    }

    private void layout(Context context, AttributeSet attrs) {
        fastScroller = new FastScroller(context, attrs);
        fastScroller.setId(R.id.fast_scroller);
    }


}

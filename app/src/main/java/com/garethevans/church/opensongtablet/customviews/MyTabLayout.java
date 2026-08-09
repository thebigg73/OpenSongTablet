package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.garethevans.church.opensongtablet.screensetup.Palette;

import java.util.ArrayList;
import java.util.List;

public class MyTabLayout extends LinearLayout {

    private ViewPager2 viewPager;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

    private ColorStateList tabTextColors;
    private int indicatorColor = Color.TRANSPARENT;
    private ColorStateList tabIconTint;
    private Palette palette;

    private final List<Tab> tabs = new ArrayList<>();

    public MyTabLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MyTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MyTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        palette = new Palette(context);

        setBackgroundColor(palette.secondary);
        setTabTextColors(palette.textColor, palette.textColor);
        setSelectedTabIndicatorColor(palette.textColor);
        setTabIconTint(palette.textColor);
    }

    // --- Styling Methods ---

    public void setTabTextColors(int normalColor, int selectedColor) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_selected},
                new int[]{-android.R.attr.state_selected}
        };
        int[] colors = new int[]{selectedColor, normalColor};
        this.tabTextColors = new ColorStateList(states, colors);
        updateTabStyles();
    }

    public void setTabTextColors(@Nullable ColorStateList colorStateList) {
        this.tabTextColors = colorStateList;
        updateTabStyles();
    }

    public void setSelectedTabIndicatorColor(int color) {
        this.indicatorColor = color;
        updateTabStyles();
    }

    public void setTabIconTint(int color) {
        this.tabIconTint = ColorStateList.valueOf(color);
        updateTabStyles();
    }

    public void setTabIconTint(@Nullable ColorStateList tint) {
        this.tabIconTint = tint;
        updateTabStyles();
    }

    @Nullable
    public Tab getTabAt(int position) {
        if (position >= 0 && position < tabs.size()) {
            return tabs.get(position);
        }
        return null;
    }

    public int getTabCount() {
        return tabs.size();
    }

    // --- Setup with Strategy ---

    public void setupWithViewPager(@NonNull ViewPager2 targetViewPager, @NonNull TabConfigurationStrategy strategy) {
        this.viewPager = targetViewPager;
        removeAllViews();
        tabs.clear();

        int count = viewPager.getAdapter() != null ? viewPager.getAdapter().getItemCount() : 0;
        if (count == 0) return;

        for (int i = 0; i < count; i++) {
            final int position = i;

            LinearLayout tabLayoutItem = new LinearLayout(getContext());
            tabLayoutItem.setOrientation(LinearLayout.VERTICAL);
            tabLayoutItem.setGravity(Gravity.CENTER);
            tabLayoutItem.setClickable(true);
            tabLayoutItem.setFocusable(true);

            AppCompatTextView titleView = new AppCompatTextView(getContext());
            titleView.setGravity(Gravity.CENTER);
            titleView.setPadding(16, 16, 16, 12);
            titleView.setCompoundDrawablePadding(8);

            if (tabTextColors != null) {
                titleView.setTextColor(tabTextColors);
            }

            if (tabIconTint != null) {
                TextViewCompat.setCompoundDrawableTintList(titleView, tabIconTint);
            }

            Tab tabWrapper = new Tab(tabLayoutItem, titleView);
            tabs.add(tabWrapper);

            strategy.onConfigureTab(tabWrapper, position);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f
            );
            tabLayoutItem.addView(titleView, textParams);

            View indicatorView = new View(getContext());
            indicatorView.setBackgroundColor(indicatorColor);
            LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    8
            );
            tabLayoutItem.addView(indicatorView, indicatorParams);

            tabLayoutItem.setOnClickListener(v -> {
                if (viewPager != null) {
                    viewPager.setCurrentItem(position, true);
                }
            });

            LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f
            );
            addView(tabLayoutItem, itemParams);
        }

        if (pageChangeCallback != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }

        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                selectTab(position);
            }
        };
        viewPager.registerOnPageChangeCallback(pageChangeCallback);

        selectTab(viewPager.getCurrentItem());
    }

    private void updateTabStyles() {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout tabLayoutItem = (LinearLayout) child;
                if (tabLayoutItem.getChildCount() > 0 && tabLayoutItem.getChildAt(0) instanceof AppCompatTextView) {
                    AppCompatTextView titleView = (AppCompatTextView) tabLayoutItem.getChildAt(0);
                    if (tabTextColors != null) {
                        titleView.setTextColor(tabTextColors);
                    }
                    if (tabIconTint != null) {
                        TextViewCompat.setCompoundDrawableTintList(titleView, tabIconTint);
                    }
                }
                if (tabLayoutItem.getChildCount() > 1) {
                    View indicatorView = tabLayoutItem.getChildAt(1);
                    indicatorView.setBackgroundColor(indicatorColor);
                }
            }
        }
    }

    private void selectTab(int position) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            boolean isSelected = (i == position);
            child.setSelected(isSelected);

            if (child instanceof LinearLayout) {
                LinearLayout tabLayoutItem = (LinearLayout) child;
                if (tabLayoutItem.getChildCount() > 1) {
                    View indicatorView = tabLayoutItem.getChildAt(1);
                    indicatorView.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
                }
            }
        }
    }

    public static class Tab {
        public final View view;
        private final AppCompatTextView textView;

        public Tab(View view, AppCompatTextView textView) {
            this.view = view;
            this.textView = textView;
        }

        public void setText(CharSequence text) {
            textView.setText(text);
        }

        public void setIcon(Drawable icon) {
            if (icon != null) {
                icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            }
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, icon, null, null);
        }
    }

    public interface TabConfigurationStrategy {
        void onConfigureTab(@NonNull Tab tab, int position);
    }
}
package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.setmenu.SetItemInfo;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;

public class InlineSetList extends RecyclerView {

    private int width=0, widthPresenter;
    private int selectedItem = -1;
    private boolean showInline, showInlinePresenter;
    private final String TAG = "InlineSetList";
    private InlineSetListAdapter inlineSetListAdapter;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<InlineSetItemInfo> setList;

    public InlineSetList(@NonNull Context context) {
        super(context);
        setLayoutManager(new LinearLayoutManager(context));
    }

    public InlineSetList(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        setLayoutManager(new LinearLayoutManager(context));
    }

    public void initialisePreferences(Context c, MainActivityInterface mainActivityInterface) {
        this.mainActivityInterface = mainActivityInterface;
        showInline = mainActivityInterface.getPreferences().getMyPreferenceBoolean("inlineSet",false);
        int screenWidth = mainActivityInterface.getDisplayMetrics()[0];
        width = (int)(mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidth",0.3f)*screenWidth);
        showInlinePresenter = mainActivityInterface.getPreferences().getMyPreferenceBoolean("inlineSetPresenter",true);
        widthPresenter = (int)(mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidthPresenter", 0.3f)*screenWidth);
        inlineSetListAdapter = new InlineSetListAdapter(c);
        setAdapter(inlineSetListAdapter);
        setVisibility(View.GONE);
    }

    public void orientationChanged(int orientation) {
        int screenWidth;
        if (orientation== Configuration.ORIENTATION_PORTRAIT) {
            screenWidth = mainActivityInterface.getDisplayMetrics()[0];
        } else {
            screenWidth = mainActivityInterface.getDisplayMetrics()[1];
        }
        width = (int)(mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidth",0.3f)*screenWidth);
        checkVisibility();
    }

    public void checkVisibility() {
        if (mainActivityInterface.getCurrentSet().getSetItems().size()>0 && showInline) {
            setVisibility(View.VISIBLE);
            getLayoutParams().width = width;
        } else {
            setVisibility(View.GONE);
        }
    }

    public int getInlineSetWidth() {
        if (showInline && mainActivityInterface.getCurrentSet().getSetItems().size()>0) {
            return width;
        } else {
            return 0;
        }
    }
    public int getInlineSetWidthPresenter() {
        if (showInlinePresenter && mainActivityInterface.getCurrentSet().getSetItems().size()>0) {
            return widthPresenter;
        } else {
            return 0;
        }
    }

    // From the page button
    public void toggleInlineSet() {
        showInline = !showInline;
        checkVisibility();
    }

    public void updateInlineSet(boolean showInline, int width) {
        this.showInline = showInline;
        this.width = width;
        checkVisibility();
    }

    public void prepareSet(){
        if (showInline) {
            if (setList==null) {
                setList = new ArrayList<>();
            } else {
                setList.clear();
            }
            inlineSetListAdapter.clearSetList();
            for (int i = 0; i < mainActivityInterface.getCurrentSet().getSetItems().size(); i++) {
                InlineSetItemInfo info = new InlineSetItemInfo();
                info.item = i + 1;
                info.songfolder = mainActivityInterface.getCurrentSet().getFolder(i);
                info.songtitle = mainActivityInterface.getCurrentSet().getFilename(i);
                info.songkey = mainActivityInterface.getCurrentSet().getKey(i);
                setList.add(info);
            }
            inlineSetListAdapter.updateSetList();
        }
    }

    private class InlineSetItemInfo {
        public String songtitle;
        public String songfolder;
        public String songkey;
        public int item;
    }

    private class InlineSetListAdapter extends RecyclerView.Adapter<InlineSetItemViewHolder> implements FastScroller.SectionIndexer {

        // All the helpers we need to access are in the MainActivity
        private final int onColor, offColor;
        private final SparseBooleanArray highlightedArray = new SparseBooleanArray();

        InlineSetListAdapter(Context context) {
            //this.mainActivityInterface = (MainActivityInterface) context;
            onColor = context.getResources().getColor(R.color.colorSecondary);
            offColor = context.getResources().getColor(R.color.colorAltPrimary);
        }

        public void clearSetList() {
            if (setList != null) {
                int size = getItemCount();
                setList.clear();
                notifyItemRangeRemoved(0, size);
            } else {
                setList = new ArrayList<>();
            }
        }
        public void updateSetList() {
            for (int x = 0; x < setList.size(); x++) {
                notifyItemInserted(x);
            }
        }

        @Override
        public int getItemCount() {
            if (setList==null) {
                return 0;
            }
            return setList.size();
        }

        @Override
        public void onBindViewHolder(@NonNull InlineSetItemViewHolder holder, int position, @NonNull List<Object> payloads) {
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads);
            } else {
                // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
                for (Object payload : payloads) {
                    if (payload.equals("highlightItem")) {
                        // We want to update the highlight colour to on/off
                        if (highlightedArray.get(position,false)) {
                            setColor(holder, onColor);
                        } else {
                            setColor(holder, offColor);
                        }
                    }
                }
            }
        }

        private void setColor(InlineSetItemViewHolder holder, int cardColor) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.cardView.setBackgroundTintList(ColorStateList.valueOf(cardColor));
            } else {
                holder.cardView.setBackgroundColor(cardColor);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull InlineSetItemViewHolder setitemViewHolder, int i) {
            InlineSetItemInfo si = setList.get(i);
            String titlesongname = si.songtitle;
            if (highlightedArray.get(i,false)) {
                setColor(setitemViewHolder,onColor);
            } else {
                setColor(setitemViewHolder,offColor);
            }
            String text = si.item+". "+titlesongname;
            if (si.songkey!=null && !si.songkey.isEmpty()) {
                text = text + " ("+si.songkey+")";
            }
            setitemViewHolder.vSongTitle.setText(text);
        }

        @NonNull
        @Override
        public InlineSetItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.view_set_item, viewGroup, false);

            return new InlineSetItemViewHolder(itemView);
        }

        public void updateHighlightedItem(int position) {
            int oldPosition = selectedItem;
            selectedItem = position;
            if (oldPosition!=-1) {
                highlightedArray.put(oldPosition,false);
                notifyItemChanged(oldPosition,"highlightItem");
            }
            highlightedArray.put(selectedItem,true);
            notifyItemChanged(selectedItem,"highlightItem");
        }

        public void updateInlineSetMove(int from, int to) {
            if (setList!=null) {
                setList.get(from).item = (to + 1);
                setList.get(to).item = (from + 1);
                InlineSetItemInfo thisItem = setList.get(from);
                setList.remove(from);
                setList.add(to, thisItem);
                notifyItemChanged(from);
                notifyItemChanged(to);
                notifyItemMoved(from, to);
            }
        }

        public void updateInlineSetRemoved(int from) {
            setList.remove(from);
            notifyItemRemoved(from);
            // Go through the setList from this position and sort the numbers
            for (int x = from; x < setList.size(); x++) {
                setList.get(x).item = (x + 1);
                notifyItemChanged(x);
            }
        }

        public void updateInlineSetAddded(InlineSetItemInfo inlineSetItemInfo) {
            setList.add(inlineSetItemInfo);
            notifyItemInserted(setList.size()-1);
        }

        public void initialiseInlineSetItem(int position) {
            // If we already had a currentPosition, clear it
            if (selectedItem!=-1) {
                highlightedArray.put(selectedItem,false);
                notifyItemChanged(selectedItem,"highlightItem");
            }
            // Now highlight the loaded position
            selectedItem = position;
            highlightedArray.put(selectedItem,true);
            notifyItemChanged(selectedItem,"highlightItem");
        }

        @Override
        public CharSequence getSectionText(int position) {
            if (setList!=null &&
                    setList.size()>position) {
                return "" + setList.get(position).item;
            } else {
                return "";
            }
        }
    }

    private class InlineSetItemViewHolder extends RecyclerView.ViewHolder {

        final MaterialTextView vItem;
        final MaterialTextView vSongTitle;
        final MaterialTextView vSongFolder;
        final RelativeLayout vCard;
        final CardView cardView;

        private InlineSetItemViewHolder(View v) {
            super(v);
            cardView = v.findViewById(R.id.card_view);
            vCard = v.findViewById(R.id.cardview_layout);
            vItem = v.findViewById(R.id.cardview_item);
            vItem.setVisibility(View.GONE);
            vSongTitle = v.findViewById(R.id.cardview_songtitle);
            vSongTitle.setTextSize(12f);
            vSongFolder = v.findViewById(R.id.cardview_folder);
            vSongFolder.setVisibility(View.GONE);
            v.setOnClickListener((view) -> {
                updateSelected(getAbsoluteAdapterPosition());
                mainActivityInterface.loadSongFromSet(getAbsoluteAdapterPosition());
            });
        }
    }

    public void updateSelected(int selectedItem) {
        inlineSetListAdapter.updateHighlightedItem(selectedItem);
    }
    public void updateInlineSetMove(int from, int to) {
        inlineSetListAdapter.updateInlineSetMove(from,to);
    }
    public void updateInlineSetRemoved(int from) {
        inlineSetListAdapter.updateInlineSetRemoved(from);
    }
    public void updateInlineSetAdded(SetItemInfo setItemInfo) {
        InlineSetItemInfo info = new InlineSetItemInfo();
        info.item = setList.size()+1;
        info.songtitle = setItemInfo.songtitle;
        info.songfolder = setItemInfo.songfolder;
        info.songkey = setItemInfo.songkey;
        inlineSetListAdapter.updateInlineSetAddded(info);
    }
    public void initialiseInlineSetItem(int position) {
        inlineSetListAdapter.initialiseInlineSetItem(position);
    }
}

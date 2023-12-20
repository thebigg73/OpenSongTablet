package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
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

    private int width = 0;
    private int selectedItem = -1;
    private boolean showInline, showInlinePresenter;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "InlineSetList";
    private String mode_presenter_string="";
    private InlineSetListAdapter inlineSetListAdapter;
    private MainActivityInterface mainActivityInterface;
    private ArrayList<InlineSetItemInfo> setList;
    private final LinearLayoutManager llm;
    private float textSize = 12;
    private boolean useTitle = true;
    private final String highlightItem = "highlightItem", updateNumber = "updateNumber";

    public InlineSetList(@NonNull Context context) {
        super(context);
        llm = new LinearLayoutManager(context);
        setLayoutManager(llm);
        setItemAnimator(null);
    }

    public InlineSetList(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        llm = new LinearLayoutManager(context);
        setLayoutManager(llm);
        setItemAnimator(null);
    }


    public void initialisePreferences(Context c, MainActivityInterface mainActivityInterface) {
        this.mainActivityInterface = mainActivityInterface;
        showInline = mainActivityInterface.getPreferences().getMyPreferenceBoolean("inlineSet", true);
        int screenWidth = mainActivityInterface.getDisplayMetrics()[0];
        width = (int) (mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidth", 0.3f) * screenWidth);
        adjustTextSize();
        showInlinePresenter = mainActivityInterface.getPreferences().getMyPreferenceBoolean("inlineSetPresenter", true);
        inlineSetListAdapter = new InlineSetListAdapter(c);
        setAdapter(inlineSetListAdapter);
        setVisibility(View.GONE);
        mode_presenter_string = c.getString(R.string.mode_presenter);
        useTitle = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles",true);
    }

    public void orientationChanged(int orientation) {
        int[] metrics = mainActivityInterface.getDisplayMetrics();
        int portraitWidth = Math.min(metrics[0], metrics[1]);
        int landscapeWidth = Math.max(metrics[0], metrics[1]);
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            width = (int) (mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidth", 0.3f) * portraitWidth);
        } else {
            width = (int) (mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidth", 0.3f) * landscapeWidth);
        }
        checkVisibility();
    }

    public void checkVisibility() {
        boolean reloadSong = false;
        if (mainActivityInterface.getCurrentSet().getSetItems().size() > 0 && needInline()) {
            if (getVisibility() == View.GONE) {
                // This will require a reload of the song to resize it
                reloadSong = true;
            }
            setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams lp = getLayoutParams();
            lp.width = width;
            setLayoutParams(lp);
            requestLayout();
        } else {
            if (getVisibility() == View.VISIBLE) {
                // This will require a reload of the song to resize it
                reloadSong = true;
            }
            setVisibility(View.GONE);
        }
        if (reloadSong) {
            if (selectedItem==-1 || setList==null || setList.isEmpty()) {
                // Load the song
                mainActivityInterface.doSongLoad(mainActivityInterface.getSong().getFolder(),
                        mainActivityInterface.getSong().getFilename(),false);
            } else {
                // Load from the set
                mainActivityInterface.loadSongFromSet(selectedItem);
            }
        }
    }

    private boolean needInline() {
        if (getContext()!=null) {
            return (!mainActivityInterface.getMode().equals(mode_presenter_string) && showInline) ||
                    (mainActivityInterface.getMode().equals(mode_presenter_string) && showInlinePresenter);
        } else {
            return false;
        }
    }

    public int getInlineSetWidth() {
        if (showInline && mainActivityInterface.getCurrentSet().getSetItems().size() > 0) {
            return width;
        } else {
            return 0;
        }
    }

    // From the page button
    public void toggleInlineSet() {
        // Change the current value and save
        setInlineSet(!showInline);
        checkVisibility();
    }

    public void setInlineSet(boolean showInline) {
        this.showInline = showInline;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("inlineSet", showInline);
    }

    public void prepareSet() {
        if (inlineSetListAdapter!=null) {
            inlineSetListAdapter.clearSetList();
            selectedItem = -1;

            if (setList == null) {
                setList = new ArrayList<>();
            } else {
                setList.clear();
            }
            for (int i = 0; i < mainActivityInterface.getCurrentSet().getSetItems().size(); i++) {
                InlineSetItemInfo info = new InlineSetItemInfo();
                info.item = i + 1;
                info.songfolder = mainActivityInterface.getCurrentSet().getFolder(i);
                info.songtitle = mainActivityInterface.getCurrentSet().getTitle(i);
                info.songfilename = mainActivityInterface.getCurrentSet().getFilename(i);
                info.songkey = mainActivityInterface.getCurrentSet().getKey(i);
                setList.add(info);
            }
            if (inlineSetListAdapter!=null) {
                inlineSetListAdapter.updateSetList();
            }
            // Look for current item
            selectedItem = mainActivityInterface.getCurrentSet().getIndexSongInSet();
            scrollToItem(selectedItem);
            inlineSetListAdapter.initialiseInlineSetItem(selectedItem);
            checkVisibility();
        }
    }

    private static class InlineSetItemInfo {
        public String songtitle;
        public String songfilename;
        public String songfolder;
        public String songkey;
        public int item;
    }

    private class InlineSetListAdapter extends RecyclerView.Adapter<InlineSetItemViewHolder> {

        // All the helpers we need to access are in the MainActivity
        private final int onColor, offColor;

        InlineSetListAdapter(Context context) {
            //this.mainActivityInterface = (MainActivityInterface) context;
            onColor = context.getResources().getColor(R.color.colorSecondary);
            offColor = context.getResources().getColor(R.color.colorAltPrimary);
        }

        public void clearSetList() {
            if (setList != null) {
                int size = getItemCount();
                setList.clear();
                try {
                    notifyItemRangeRemoved(0, size);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                setList = new ArrayList<>();
            }
            selectedItem = -1;
        }

        public void updateSetList() {
            if (mainActivityInterface!=null) {
                useTitle = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles",true);
            }
            for (int x = 0; x < setList.size(); x++) {
                try {
                    notifyItemInserted(x);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getItemCount() {
            if (setList == null) {
                return 0;
            }
            return setList.size();
        }

        @Override
        public void onBindViewHolder(@NonNull InlineSetItemViewHolder holder, int position, @NonNull List<Object> payloads) {
            position = holder.getAbsoluteAdapterPosition();
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads);
            } else {
                // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
                for (Object payload : payloads) {
                    if (payload.equals(updateNumber)) {
                        InlineSetItemInfo si = setList.get(position);
                        if (si.item!=position+1) {
                            si.item = position+1;
                            setList.set(position,si);
                        }
                        String textsn = si.item + ". " + si.songtitle;
                        String textfn = si.item + ". " + si.songfilename;

                        if (si.songkey != null && !si.songkey.isEmpty()) {
                            textsn = textsn + " (" + si.songkey + ")";
                            textfn = textfn + " (" + si.songkey + ")";
                        }
                        holder.vSongTitle.setText(textsn);
                        holder.vSongFilename.setText(textfn);
                    }

                    if (payload.equals(highlightItem) || payload.equals(updateNumber)) {
                        // We want to update the highlight colour to on/off
                        if (position == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                            setColor(holder, onColor);
                            selectedItem = position;
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
            i = setitemViewHolder.getAbsoluteAdapterPosition();
            InlineSetItemInfo si = setList.get(i);
            String titlesongname = si.songtitle;
            String filename = si.songfilename;
            if (i == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                setColor(setitemViewHolder, onColor);
                selectedItem = i;
            } else {
                setColor(setitemViewHolder, offColor);
            }

            if (si.item!=i+1) {
                si.item = i+1;
                setList.set(i,si);
            }

            String textsn = si.item + ". " + titlesongname;
            String textfn = si.item + ". " + filename;

            if (si.songkey != null && !si.songkey.isEmpty()) {
                textsn = textsn + " (" + si.songkey + ")";
                textfn = textfn + " (" + si.songkey + ")";
            }
            setitemViewHolder.vSongTitle.setTextSize(textSize);
            setitemViewHolder.vSongTitle.setText(textsn);
            setitemViewHolder.vSongFilename.setTextSize(textSize);
            setitemViewHolder.vSongFilename.setText(textfn);
            setitemViewHolder.vSongTitle.setVisibility(useTitle ? View.VISIBLE:View.GONE);
            setitemViewHolder.vSongFilename.setVisibility(useTitle ? View.GONE:View.VISIBLE);
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
            // Unhighlight the previously selected item
            if (selectedItem != -1) {
                notifyItemChanged(selectedItem, highlightItem);
            }

            // Highlight the new item
            selectedItem = position;
            notifyItemChanged(position, highlightItem);
        }

        public void updateInlineSetMove(int from, int to) {
            if (setList != null) {
                setList.get(from).item = (to + 1);
                setList.get(to).item = (from + 1);
                InlineSetItemInfo thisItem = setList.get(from);
                setList.remove(from);
                setList.add(to, thisItem);
                notifyItemMoved(from, to);
                notifyItemChanged(from,updateNumber);
                notifyItemChanged(to,updateNumber);

                // If we have changed the position of the selected item...
                if (from == selectedItem) {
                    selectedItem = to;
                } else if (to == selectedItem) {
                    selectedItem = from;
                }
            }
        }

        public void updateInlineSetRemoved(int from) {
            setList.remove(from);
            notifyItemRemoved(from);

            if (selectedItem == from) {
                // reset the selected item
                selectedItem = -1;
            }

            // Go through the setList from this position and sort the numbers
            notifyItemRangeChanged(from,setList.size(),updateNumber);

            if (setList.size()==0) {
                // This was the last item removed, we need to check visibility
                checkVisibility();
            }
        }

        public void updateInlineSetChanged(int position, InlineSetItemInfo inlineSetItemInfo) {
            if (setList!=null) {
                setList.set(position, inlineSetItemInfo);
                notifyItemChanged(position,updateNumber);
            }
        }

        public void updateInlineSetAdded(InlineSetItemInfo inlineSetItemInfo) {
            setList.add(inlineSetItemInfo);
            notifyItemInserted(setList.size() - 1);
            if (setList.size()==1) {
                // This is the first item, we need to check visibility
                checkVisibility();
            }
        }

        public void updateInlineSetInserted(int position, InlineSetItemInfo inlineSetItemInfo) {
            setList.add(position,inlineSetItemInfo);
            notifyItemInserted(position);

            // Need to fix the items afterwards too
            notifyItemRangeChanged(position,setList.size(),updateNumber);
        }

        public void initialiseInlineSetItem(int position) {
            // If we already had a currentPosition, clear it
            if (selectedItem != -1) {
                notifyItemChanged(selectedItem, highlightItem);
            }
            // Now highlight the loaded position
            selectedItem = position;
            notifyItemChanged(selectedItem, highlightItem);
        }

        public void updateInlineSetAll() {
            mainActivityInterface.getMainHandler().post(() -> notifyItemRangeChanged(0,getItemCount()));
        }

    }

    private class InlineSetItemViewHolder extends RecyclerView.ViewHolder {

        final MaterialTextView vItem;
        final MaterialTextView vSongTitle;
        final MaterialTextView vSongFilename;
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
            vSongTitle.setTextSize(textSize);
            vSongFilename = v.findViewById(R.id.cardview_songfilename);
            vSongFilename.setTextSize(textSize);
            vSongFolder = v.findViewById(R.id.cardview_folder);
            vSongFolder.setVisibility(View.GONE);
            v.setOnClickListener((view) -> {
                updateSelected(getAbsoluteAdapterPosition());
                mainActivityInterface.loadSongFromSet(getAbsoluteAdapterPosition());
            });
            v.setOnLongClickListener(v1 -> {
                scrollToItem(selectedItem);
                return true;
            });
        }
    }

    public void updateSelected(int selectedItem) {
        mainActivityInterface.getCurrentSet().setIndexSongInSet(selectedItem);
        inlineSetListAdapter.updateHighlightedItem(selectedItem);
        postDelayed(() -> scrollToItem(selectedItem),200);
    }

    public void updateInlineSetMove(int from, int to) {
        inlineSetListAdapter.updateInlineSetMove(from, to);
    }

    public void updateInlineSetRemoved(int from) {
        inlineSetListAdapter.updateInlineSetRemoved(from);
    }

    public void updateInlineSetAdded(SetItemInfo setItemInfo) {
        InlineSetItemInfo info = new InlineSetItemInfo();
        info.item = setList.size() + 1;
        info.songtitle = setItemInfo.songtitle;
        info.songfilename = setItemInfo.songfilename;
        info.songfolder = setItemInfo.songfolder;
        info.songkey = setItemInfo.songkey;
        inlineSetListAdapter.updateInlineSetAdded(info);
    }

    public void updateInlineSetChanged(int position, SetItemInfo setItemInfo) {
        InlineSetItemInfo info = new InlineSetItemInfo();
        info.item = position + 1;
        info.songtitle = setItemInfo.songtitle;
        info.songfilename = setItemInfo.songfilename;
        info.songfolder = setItemInfo.songfolder;
        info.songkey = setItemInfo.songkey;
        inlineSetListAdapter.updateInlineSetChanged(position,info);
    }

    public void updateInlineSetInserted(int position, SetItemInfo setItemInfo) {
        InlineSetItemInfo info = new InlineSetItemInfo();
        info.item = position + 1;
        info.songtitle = setItemInfo.songtitle;
        info.songfilename = setItemInfo.songfilename;
        info.songfolder = setItemInfo.songfolder;
        info.songkey = setItemInfo.songkey;
        inlineSetListAdapter.updateInlineSetInserted(position,info);
    }

    public void updateInlineSetAll() {
        inlineSetListAdapter.updateInlineSetAll();
    }


    public void initialiseInlineSetItem(int position) {
        inlineSetListAdapter.initialiseInlineSetItem(position);
    }

    private void scrollToItem(int position) {
        this.post(() -> {
            if (position > -1 &&
                    position < mainActivityInterface.getCurrentSet().getSetItems().size()) {
                // Scroll to that item
                llm.scrollToPositionWithOffset(position, 0);
            } else if (position == -1 &&
                    mainActivityInterface.getCurrentSet().getSetItems().size() > 0) {
                // Scroll to the top
                llm.scrollToPositionWithOffset(0, 0);
            }
        });
    }

    private void adjustTextSize() {
        // Base the text size on the width of the inline set
        // Minimum size is 12, Maximum is 20
        if (mainActivityInterface.getMode().equals(mode_presenter_string)) {
            textSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetTextSizePresenter",12f);
        } else {
            textSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetTextSize",12f);
        }
    //    textSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("songMenuAlphaIndexSize",14f) - 2;
    }

    public void setUseTitle(boolean useTitle) {
        this.useTitle = useTitle;
    }

}

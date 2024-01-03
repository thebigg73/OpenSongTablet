package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
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

import java.util.List;

public class InlineSetList extends RecyclerView {

    // The inline set pulls all its values from the currentSet object

    private int width = 0;
    private boolean showInline, showInlinePresenter;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "InlineSetList";
    private String mode_presenter_string="";
    private InlineSetListAdapter inlineSetListAdapter;
    private MainActivityInterface mainActivityInterface;
    private final LinearLayoutManager llm;
    private float textSize = 12;
    private boolean useTitle = true;
    private final String highlightItem = "highlightItem", updateNumber = "updateNumber";

    public InlineSetList(@NonNull Context context) {
        super(context);
        Log.d(TAG,"InlineSetList()");

        llm = new LinearLayoutManager(context);
        setLayoutManager(llm);
        setItemAnimator(null);
    }

    public InlineSetList(@NonNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG,"InlineSetList()");

        llm = new LinearLayoutManager(context);
        setLayoutManager(llm);
        setItemAnimator(null);
    }


    public void initialisePreferences(Context c, MainActivityInterface mainActivityInterface) {
        Log.d(TAG,"initialisePreferences()");

        this.mainActivityInterface = mainActivityInterface;
        showInline = mainActivityInterface.getPreferences().getMyPreferenceBoolean("inlineSet", true);
        int screenWidth = mainActivityInterface.getDisplayMetrics()[0];
        width = (int) (mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetWidth", 0.20f) * screenWidth);
        adjustTextSize();
        showInlinePresenter = mainActivityInterface.getPreferences().getMyPreferenceBoolean("inlineSetPresenter", true);
        inlineSetListAdapter = new InlineSetListAdapter(c);
        setAdapter(inlineSetListAdapter);
        setVisibility(View.GONE);
        mode_presenter_string = c.getString(R.string.mode_presenter);
        useTitle = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles",true);
    }







    // TODO reinstate these once I've rationalised them






    public void orientationChanged(int orientation) {
        Log.d(TAG,"orientationChanged()");
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
        Log.d(TAG,"checkVisibility()");
        boolean reloadSong = false;
        if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>0 && needInline()) {
            if (getVisibility() == View.GONE) {
                // This will require a reload of the song to resize it
                reloadSong = true;
                setVisibility(View.VISIBLE);
                ViewGroup.LayoutParams lp = getLayoutParams();
                lp.width = width;
                setLayoutParams(lp);
            }
        } else if (mainActivityInterface.getCurrentSet().getCurrentSetSize()==0) {
            if (getVisibility() == View.VISIBLE) {
                // This will require a reload of the song to resize it
                reloadSong = true;
                setVisibility(View.GONE);
            }
        }
        if (reloadSong) {
            if (mainActivityInterface.getCurrentSet().getIndexSongInSet()==-1 || mainActivityInterface.getCurrentSet().getSetItemInfos()==null || mainActivityInterface.getCurrentSet().getCurrentSetSize()<1) {
                // Load the song
                mainActivityInterface.doSongLoad(mainActivityInterface.getSong().getFolder(),
                        mainActivityInterface.getSong().getFilename(),false);
            } else {
                // Load from the set
                mainActivityInterface.loadSongFromSet(mainActivityInterface.getCurrentSet().getIndexSongInSet());
            }
        }
    }

    private boolean needInline() {
        Log.d(TAG,"needInline()");
        if (getContext()!=null) {
            return (!mainActivityInterface.getMode().equals(mode_presenter_string) && showInline) ||
                    (mainActivityInterface.getMode().equals(mode_presenter_string) && showInlinePresenter);
        } else {
            return false;
        }
    }

    public int getInlineSetWidth() {
        Log.d(TAG,"getInlineSetWidth()");
        if (showInline && mainActivityInterface.getCurrentSet().getCurrentSetSize() > 0) {
            return width;
        } else {
            return 0;
        }
    }

    // From the page button
    public void toggleInlineSet() {
        Log.d(TAG,"toggleInlineSet()");
        // Change the current value and save
        setInlineSet(!showInline);
        checkVisibility();
    }

    // Called when we need to clear/reset the inline set
    public void notifyToClearInlineSet() {
        Log.d(TAG,"notifyToClearInlineSet()");
        if (inlineSetListAdapter!=null && mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
            mainActivityInterface.getMainHandler().post(() -> inlineSetListAdapter.notifyItemRangeRemoved(0,mainActivityInterface.getCurrentSet().getCurrentSetSize()));
        }
    }

    // Called when the set has been parsed (might be empty)
    public void notifyToInsertAllInlineSet() {
        Log.d(TAG,"notifyToInsertAllInlineSet()");
        if (inlineSetListAdapter!=null && mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
            inlineSetListAdapter.notifyItemRangeInserted(0,mainActivityInterface.getCurrentSet().getCurrentSetSize());
        }
        checkVisibility();
    }

    public void setInlineSet(boolean showInline) {
        Log.d(TAG,"setInlineSet()");
        this.showInline = showInline;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("inlineSet", showInline);
    }

    public void prepareSet() {
        Log.d(TAG,"prepareSet()");
        if (inlineSetListAdapter!=null) {
            scrollToItem(mainActivityInterface.getCurrentSet().getIndexSongInSet());
            inlineSetListAdapter.initialiseInlineSetItem();
            checkVisibility();
        }
    }

    // Go through each item and highlight only the currentSet index
    public void updateHighlight() {
        Log.d(TAG,"updateHighlight()");
        mainActivityInterface.getMainHandler().post(() -> {
            inlineSetListAdapter.notifyItemChanged(mainActivityInterface.getCurrentSet().getPrevIndexSongInSet(), highlightItem);
            inlineSetListAdapter.notifyItemChanged(mainActivityInterface.getCurrentSet().getIndexSongInSet(), highlightItem);
        });
    }

    private class InlineSetListAdapter extends RecyclerView.Adapter<InlineSetItemViewHolder> {

        // All the helpers we need to access are in the MainActivity
        private final int onColor, offColor;

        InlineSetListAdapter(Context context) {
            Log.d(TAG,"inlineSetListAdapter()");
            onColor = context.getResources().getColor(R.color.colorSecondary);
            offColor = context.getResources().getColor(R.color.colorAltPrimary);
        }

        @Override
        public int getItemCount() {
            Log.d(TAG,"getItemCount()");
            return mainActivityInterface.getCurrentSet().getCurrentSetSize();
        }

        @Override
        public void onBindViewHolder(@NonNull InlineSetItemViewHolder holder, int position, @NonNull List<Object> payloads) {
            Log.d(TAG,"onBindViewHolder(payload)");
            position = holder.getAbsoluteAdapterPosition();
            if (payloads.isEmpty()) {
                super.onBindViewHolder(holder, position, payloads);
            } else {
                // Compare each Object in the payloads to the PAYLOAD you provided to notifyItemChanged
                for (Object payload : payloads) {
                    if (payload.equals(updateNumber)) {
                        SetItemInfo si = mainActivityInterface.getCurrentSet().getSetItemInfo(position);
                        si.songitem = position+1;
                        String textsn = (position+1) + ". " + si.songtitle;
                        String textfn = (position+1) + ". " + si.songfilename;

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
                        } else {
                            setColor(holder, offColor);
                        }
                    }
                }
            }
        }

        private void setColor(InlineSetItemViewHolder holder, int cardColor) {
            Log.d(TAG,"setColor()");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.cardView.setBackgroundTintList(ColorStateList.valueOf(cardColor));
            } else {
                holder.cardView.setBackgroundColor(cardColor);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull InlineSetItemViewHolder setitemViewHolder, int position) {
            Log.d(TAG,"onBindViewHolder()");
            position = setitemViewHolder.getAbsoluteAdapterPosition();
            SetItemInfo si = mainActivityInterface.getCurrentSet().getSetItemInfo(position);
            if (position == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                setColor(setitemViewHolder, onColor);
            } else {
                setColor(setitemViewHolder, offColor);
            }

            si.songitem = position+1;
            String textsn = (position+1) + ". " + si.songtitle;
            String textfn = (position+1) + ". " + si.songfilename;

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
            Log.d(TAG,"InlineSetItemViewHolder()");
            View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.view_set_item, viewGroup, false);

            return new InlineSetItemViewHolder(itemView);
        }

        public void updateInlineSetMove(int from, int to) {
            Log.d(TAG,"updateInlineSetMove()");
            notifyItemChanged(from, updateNumber);
            notifyItemChanged(to, updateNumber);
            notifyItemMoved(from, to);
        }

        public void updateInlineSetRemoved(int from) {
            Log.d(TAG,"updateInlineSetRemoved()");
            notifyItemRemoved(from);

            // Go through the setList from this position and sort the numbers
            notifyItemRangeChanged(from,mainActivityInterface.getCurrentSet().getCurrentSetSize(),updateNumber);

            if (mainActivityInterface.getCurrentSet().getCurrentSetSize()==0) {
                // This was the last item removed, we need to check visibility
                checkVisibility();
            }
        }

        public void updateInlineSetChanged(int position) {
            Log.d(TAG,"updateInlineSetChanged()");
            if (mainActivityInterface.getCurrentSet().getSetItemInfos()!=null) {
                notifyItemChanged(position,updateNumber);
            }
        }

        public void updateInlineSetAdded() {
            Log.d(TAG,"updateInlineSetAdded()");
            notifyItemInserted(mainActivityInterface.getCurrentSet().getCurrentSetSize() - 1);
            if (mainActivityInterface.getCurrentSet().getCurrentSetSize()==1 ||
            mainActivityInterface.getCurrentSet().getCurrentSetSize()==0) {
                // This is/was the first item, we need to check visibility
                checkVisibility();
            }
        }

        public void updateInlineSetInserted(int position) {
            Log.d(TAG,"updateInlineSetInserted()");
            notifyItemInserted(position);

            // Need to fix the items afterwards too
            notifyItemRangeChanged(position,mainActivityInterface.getCurrentSet().getCurrentSetSize(),updateNumber);
        }

        public void initialiseInlineSetItem() {
            Log.d(TAG,"initiliseInlineSetItem()");
            notifyItemChanged(mainActivityInterface.getCurrentSet().getPrevIndexSongInSet(), highlightItem);
            notifyItemChanged(mainActivityInterface.getCurrentSet().getIndexSongInSet(), highlightItem);
        }

        public void updateInlineSetAll() {
            Log.d(TAG,"updateInlineSetAll()");
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
            Log.d(TAG,"inlineSetItemViewHolder()");

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
                // Load the song and that sends the updates to the setMenuFragment and inlineSetList
                mainActivityInterface.getThreadPoolExecutor().execute(() -> mainActivityInterface.loadSongFromSet(getAbsoluteAdapterPosition()));
            });
            v.setOnLongClickListener(v1 -> {
                scrollToItem(mainActivityInterface.getCurrentSet().getIndexSongInSet());
                return true;
            });
        }
    }

    public void updateSelected(int selectedItem) {
        Log.d(TAG,"updateSelected()");
        postDelayed(() -> scrollToItem(selectedItem),200);
    }

    public void updateInlineSetMove(int from, int to) {
        Log.d(TAG,"updateInlineSetMove()");
        inlineSetListAdapter.updateInlineSetMove(from, to);
    }

    public void updateInlineSetRemoved(int from) {
        Log.d(TAG,"updateInlineSetRemoved()");
        inlineSetListAdapter.updateInlineSetRemoved(from);
    }

    public void updateInlineSetAdded() {
        Log.d(TAG,"updateInlineSetAdded()");
        inlineSetListAdapter.updateInlineSetAdded();
    }

    public void updateInlineSetChanged(int position) {
        Log.d(TAG,"updateInlineSetChanged()");
        inlineSetListAdapter.updateInlineSetChanged(position);
    }

    public void updateInlineSetInserted(int position) {
        Log.d(TAG,"updateInlineSetInserted()");
        inlineSetListAdapter.updateInlineSetInserted(position);
    }

    public void updateInlineSetAll() {
        Log.d(TAG,"updateInlineSetAll()");
        inlineSetListAdapter.updateInlineSetAll();
    }


    public void initialiseInlineSetItem() {
        Log.d(TAG,"initialiseInlineSetItem()");
        inlineSetListAdapter.initialiseInlineSetItem();
    }

    private void scrollToItem(int position) {
        Log.d(TAG,"scrollToItem()");
        this.post(() -> {
            if (position > -1 &&
                    position < mainActivityInterface.getCurrentSet().getCurrentSetSize()) {
                // Scroll to that item
                llm.scrollToPositionWithOffset(position, 0);
            } else if (position == -1 &&
                    mainActivityInterface.getCurrentSet().getCurrentSetSize() > 0) {
                // Scroll to the top
                llm.scrollToPositionWithOffset(0, 0);
            }
        });
    }

    private void adjustTextSize() {
        Log.d(TAG,"adjustTextSize()");
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
        Log.d(TAG,"setUseTitle()");
        this.useTitle = useTitle;
    }

}

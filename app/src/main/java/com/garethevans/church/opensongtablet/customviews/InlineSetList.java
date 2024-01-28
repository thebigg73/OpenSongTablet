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

// All calls to the inline set notify commands come from the SetAdapter.class
// These are carried out after fixing the main set menu

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
    private boolean useTitle = true, reloadSong = false;
    private final String highlightItem = "highlightItem", updateNumber = "updateNumber";
    private String no_set_string;
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


    // Get the user preferences and set up the adapter
    public void initialisePreferences(Context c, MainActivityInterface mainActivityInterface) {
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
        no_set_string = c.getString(R.string.set_is_empty);
        useTitle = mainActivityInterface.getPreferences().getMyPreferenceBoolean("songMenuSortTitles",true);
    }

    // Change the preference to use the inlineSet
    public void setInlineSet(boolean showInline) {
        this.showInline = showInline;
        mainActivityInterface.getPreferences().setMyPreferenceBoolean("inlineSet", showInline);
        setVisibility(showInline ? View.VISIBLE:View.GONE);
        if (mainActivityInterface.getCurrentSet().getCurrentSetSize()==0) {
            mainActivityInterface.getShowToast().doIt(no_set_string);
        }
    }

    // Adjust the inline text size
    private void adjustTextSize() {
        // Base the text size on the width of the inline set
        // Minimum size is 12, Maximum is 20
        if (mainActivityInterface.getMode().equals(mode_presenter_string)) {
            textSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetTextSizePresenter",12f);
        } else {
            textSize = mainActivityInterface.getPreferences().getMyPreferenceFloat("inlineSetTextSize",12f);
        }
    }

    // Set if we are using titles or filenames
    public void setUseTitle(boolean useTitle) {
        this.useTitle = useTitle;
    }


    // Check the user preferences and mode for inline set display
    private boolean needInline() {
        if (getContext()!=null) {
            return (!mainActivityInterface.getMode().equals(mode_presenter_string) && showInline) ||
                    (mainActivityInterface.getMode().equals(mode_presenter_string) && showInlinePresenter);
        } else {
            return false;
        }
    }

    // If we rotate the device, the inline set needs redrawn to calculate the sixe
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


    // Scroll to an item
    private void scrollToItem(int position) {
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

    // Get the inline set width (or 0 if no set required)
    public int getInlineSetWidth() {
        if (showInline && mainActivityInterface.getCurrentSet().getCurrentSetSize() > 0) {
            return width;
        } else {
            return 0;
        }
    }


    // From the page button (show or hide)
    public void toggleInlineSet() {
        // Change the current value and save
        setInlineSet(!showInline);
        checkVisibility();
    }

    // Check if the inline set is required (Visible) or not (Gone)
    public void checkVisibility() {
        // Check if a reload is required
        // Do this check after a delay (allow the view to be drawn)
        mainActivityInterface.getMainHandler().postDelayed(() -> {
            checkReload();
            // Load the song if the song view is wider or narrower than it should be
            int screenWidth = mainActivityInterface.getDisplayMetrics()[0];
            int songWidth = mainActivityInterface.getSongWidth();

            boolean wrongSize = songWidth!=0 && screenWidth-songWidth-width!=0;

            if (reloadSong && wrongSize) {
                if (mainActivityInterface.getCurrentSet().getIndexSongInSet() == -1) {
                    // Load the song
                    mainActivityInterface.doSongLoad(mainActivityInterface.getSong().getFolder(),
                            mainActivityInterface.getSong().getFilename(), false);
                } else {
                    // Load from the set
                    mainActivityInterface.loadSongFromSet(mainActivityInterface.getCurrentSet().getIndexSongInSet());
                }
            }
            reloadSong = false;
        },0);
    }

    // Will reload be required?
    public void checkReload() {
        reloadSong = false;
        if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>0 && needInline()) {
            if (getVisibility() == View.GONE) {
                // This will require a reload of the song to resize it
                reloadSong = true;
                mainActivityInterface.getMainHandler().post(() -> {
                    ViewGroup.LayoutParams lp = getLayoutParams();
                    lp.width = width;
                    setLayoutParams(lp);
                    setVisibility(View.VISIBLE);
                    setLayoutParams(lp);
                });
            }
        } else if (mainActivityInterface.getCurrentSet().getCurrentSetSize()==0) {
            if (getVisibility() == View.VISIBLE) {
                // This will require a reload of the song to resize it
                reloadSong = true;
                mainActivityInterface.getMainHandler().post(() -> setVisibility(View.GONE));
            }
        }
    }

    // Called when we need to clear/reset the inline set
    public void notifyToClearInlineSet(int from, int count) {
        if (inlineSetListAdapter!=null) {
            mainActivityInterface.getMainHandler().post(() -> inlineSetListAdapter.notifyItemRangeRemoved(from,count));
        }
        checkVisibility();
    }

    // Called when the set has been parsed (might be empty)
    public void notifyToInsertAllInlineSet() {
        if (inlineSetListAdapter!=null && mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
            mainActivityInterface.getMainHandler().post(() -> inlineSetListAdapter.notifyItemRangeInserted(0,mainActivityInterface.getCurrentSet().getCurrentSetSize()));
        }
        checkVisibility();
    }

    // Refresh the entire set
    public void notifyInlineSetUpdated() {
        mainActivityInterface.getMainHandler().post(() -> inlineSetListAdapter.notifyItemRangeChanged(0, mainActivityInterface.getCurrentSet().getCurrentSetSize()));
    }

    // Insert a value at the end of the set
    public void notifyInlineSetInserted() {
        if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>0) {
            mainActivityInterface.getMainHandler().post(() -> inlineSetListAdapter.notifyItemInserted(mainActivityInterface.getCurrentSet().getCurrentSetSize() - 1));
        }
        checkVisibility();
    }

    // Add an item at a specific location (restoring previously deleted)
    public void notifyInlineSetInserted(int position) {
        mainActivityInterface.getMainHandler().post(() -> {
            // Notify this item
            inlineSetListAdapter.notifyItemInserted(position);
            // Update the items after this as they need renumbered
            inlineSetListAdapter.notifyItemRangeChanged(position,mainActivityInterface.getCurrentSet().getCurrentSetSize(), new String[]{updateNumber});
        });
        checkVisibility();
    }

    // Remove an item at a specific location
    public void notifyInlineSetRemoved(int position) {
        if (mainActivityInterface.getCurrentSet().getCurrentSetSize()>position) {
            mainActivityInterface.getMainHandler().post(() -> {
                // Notify the item that was removed
                inlineSetListAdapter.notifyItemRemoved(position);
                // Update the items after this
                inlineSetListAdapter.notifyItemRangeChanged(position,mainActivityInterface.getCurrentSet().getCurrentSetSize(), new String[]{updateNumber});
            });
        }
        checkVisibility();
    }

    // Swap the position of an item
    public void notifyInlineSetMove(int from, int to) {
        mainActivityInterface.getMainHandler().post(() -> {
            inlineSetListAdapter.notifyItemMoved(from, to);
            // Update the numbers
            inlineSetListAdapter.notifyItemChanged(from, updateNumber);
            inlineSetListAdapter.notifyItemChanged(to, updateNumber);
        });
        checkVisibility();
    }

    // Change an item
    public void notifyInlineSetChanged(int position) {
        mainActivityInterface.getMainHandler().post(() -> inlineSetListAdapter.notifyItemChanged(position,updateNumber));
    }
    // Update a range of items
    public void notifyInlineSetRangeChanged(int from, int count) {
        if (inlineSetListAdapter!=null) {
            mainActivityInterface.getMainHandler().post(() -> inlineSetListAdapter.notifyItemRangeChanged(from,count));
        }
    }
    // Check the the highlighting of set item (index of song in set and the previously selected item)
    public void notifyInlineSetHighlight() {
        if (inlineSetListAdapter!=null) {
            mainActivityInterface.getMainHandler().post(() -> {
                if (mainActivityInterface.getCurrentSet().getPrevIndexSongInSet()>-1) {
                    inlineSetListAdapter.notifyItemChanged(mainActivityInterface.getCurrentSet().getPrevIndexSongInSet(), highlightItem);
                }
                inlineSetListAdapter.notifyItemChanged(mainActivityInterface.getCurrentSet().getIndexSongInSet(), highlightItem);
            });
            checkVisibility();
        }
    }
    // Scroll to the item (called after loading)
    public void notifyInlineSetScrollToItem() {
        scrollToItem(mainActivityInterface.getCurrentSet().getIndexSongInSet());
    }



    // The adapter class that draws the inline set
    private class InlineSetListAdapter extends RecyclerView.Adapter<InlineSetItemViewHolder> {

        // All the helpers we need to access are in the MainActivity
        private final int onColor, offColor;

        InlineSetListAdapter(Context context) {
            onColor = context.getResources().getColor(R.color.colorSecondary);
            offColor = context.getResources().getColor(R.color.colorAltPrimary);
        }

        @Override
        public int getItemCount() {
            return mainActivityInterface.getCurrentSet().getCurrentSetSize();
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
                        SetItemInfo si = mainActivityInterface.getCurrentSet().getSetItemInfo(position);
                        si.songitem = position+1;

                        String textsn = si.songtitle;
                        String textfn = si.songfilename;

                        Log.d(TAG,"si.songfolder:"+si.songfolder);
                        Log.d(TAG,"si.songfilename:"+si.songfilename);

                        // If this is a variation, we can prettify the output (remove the reference to the original folder)
                        if (mainActivityInterface.getSetActions().getIsNormalOrKeyVariation(si.songfolder,si.songfilename)) {
                            textfn = "*" + textfn.substring(textfn.lastIndexOf("_")).replace("_","");
                            textsn = textfn;
                        }

                        textsn = (position+1) + ". " + textsn;
                        textfn = (position+1) + ". " + textfn;

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.cardView.setBackgroundTintList(ColorStateList.valueOf(cardColor));
            } else {
                holder.cardView.setBackgroundColor(cardColor);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull InlineSetItemViewHolder setitemViewHolder, int position) {
            position = setitemViewHolder.getAbsoluteAdapterPosition();
            SetItemInfo si = mainActivityInterface.getCurrentSet().getSetItemInfo(position);
            if (position == mainActivityInterface.getCurrentSet().getIndexSongInSet()) {
                setColor(setitemViewHolder, onColor);
            } else {
                setColor(setitemViewHolder, offColor);
            }

            si.songitem = position+1;

            String textsn = si.songtitle;
            String textfn = si.songfilename;

            Log.d(TAG,"si.songfolder:"+si.songfolder);
            Log.d(TAG,"si.songfilename:"+si.songfilename);

            // If this is a variation, we can prettify the output (remove the reference to the original folder)
            if (mainActivityInterface.getSetActions().getIsNormalOrKeyVariation(si.songfolder,si.songfilename)) {
                textfn = "*" + textfn.substring(textfn.lastIndexOf("_")).replace("_","");
                textsn = textfn;
            }

            textsn = (position+1) + ". " + textsn;
            textfn = (position+1) + ". " + textfn;

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
                // Load the song and that sends the updates to the setMenuFragment and inlineSetList
                mainActivityInterface.getThreadPoolExecutor().execute(() -> mainActivityInterface.loadSongFromSet(getAbsoluteAdapterPosition()));
            });
            v.setOnLongClickListener(v1 -> {
                scrollToItem(mainActivityInterface.getCurrentSet().getIndexSongInSet());
                return true;
            });
        }
    }

}

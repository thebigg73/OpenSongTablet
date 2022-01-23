package com.garethevans.church.opensongtablet.songprocessing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.garethevans.church.opensongtablet.R;
import com.garethevans.church.opensongtablet.customviews.MaterialTextView;
import com.garethevans.church.opensongtablet.interfaces.MainActivityInterface;
import com.garethevans.church.opensongtablet.interfaces.RecyclerInterface;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PresentationOrderAdapter extends RecyclerView.Adapter<PresentationOrderAdapter.ViewHolder> {

    private final MainActivityInterface mainActivityInterface;
    private final RecyclerInterface recyclerInterface;
    private final String TAG = "PresentationOrderAdapter";
    private final Fragment callingFragment;
    private final String fragName;
    private final Context c;
    private final ArrayList<String> currentOrder = new ArrayList<>();

    public PresentationOrderAdapter(Context c, Fragment bottomSheet, MainActivityInterface mainActivityInterface,
                         Fragment callingFragment, String fragName) {
        this.mainActivityInterface = mainActivityInterface;
        this.callingFragment = callingFragment;
        this.fragName = fragName;
        this.c = c;
        recyclerInterface = (RecyclerInterface) bottomSheet;

        // Process the song and get for any existing tags to choose from
        if (mainActivityInterface.getTempSong().getPresoOrderSongSections()==null ||
        mainActivityInterface.getTempSong().getPresoOrderSongSections().isEmpty()) {
            mainActivityInterface.getTempSong().setSongSectionHeadings(mainActivityInterface.getProcessSong().getSectionHeadings(
                    mainActivityInterface.getTempSong().getLyrics()));
        }

        // If tags are duplicated, warn the user
        Set<String> check = new HashSet<>(mainActivityInterface.getTempSong().getSongSectionHeadings());
        if (check.size() < mainActivityInterface.getTempSong().getSongSectionHeadings().size()){
            mainActivityInterface.getShowToast().doIt(c,c.getString(R.string.duplicate_sections));
        }

        // Set up the current order
        // Desktop app splits presentation order items by space delimiter
        String[] sectionOrder = mainActivityInterface.getTempSong().getPresentationorder().split(" ");
        for (String order:sectionOrder) {
            // Custom section names with spaces are restored from underscore value
            order = order.replace("_", " ");
            if (!order.trim().isEmpty()) {
                currentOrder.add(order);
            }
        }
    }

    @NonNull
    @Override
    public PresentationOrderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View cardView = inflater.inflate(R.layout.view_order_item, parent, false);

        // Return a new holder instance
        return new PresentationOrderAdapter.ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Set item views based on your views and data model
        CardView cardView = holder.cardView;
        MaterialTextView sectionName = holder.sectionName;
        String header = currentOrder.get(position);
        sectionName.setText(header);
        String niceheader = mainActivityInterface.getProcessSong().beautifyHeading(c,mainActivityInterface,header);
        if (!niceheader.equals(header)) {
            sectionName.setHint(niceheader);
        }

        if (!mainActivityInterface.getTempSong().getSongSectionHeadings().contains(header)) {
           // There is reference to a section that isn't in the song
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                cardView.setBackgroundTintList(ColorStateList.valueOf(c.getResources().getColor(R.color.vdarkred)));
            } else {
                cardView.setBackgroundColor(c.getResources().getColor(R.color.vdarkred));
            }
            sectionName.setHint(c.getString(R.string.section_not_found));
        }
    }

    @Override
    public int getItemCount() {
        return currentOrder.size();
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {

        public MaterialTextView sectionName;
        public CardView cardView;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            sectionName = itemView.findViewById(R.id.sectionName);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }

    public void onItemMoved(int fromPosition, int toPosition) {
        Collections.swap(currentOrder,fromPosition,toPosition);
        updateValue();
        notifyItemMoved(fromPosition,toPosition);
        recyclerInterface.onItemMove(fromPosition, toPosition);
    }

    public void onItemDismissed(int fromPosition) {
        currentOrder.remove(fromPosition);
        updateValue();
        notifyItemRemoved(fromPosition);
        recyclerInterface.onItemDismiss(fromPosition);
    }

    public void onItemAdded(String item) {
        int position = currentOrder.size();
        currentOrder.add(position,item);
        updateValue();
        notifyItemInserted(position);
    }

    private void updateValue() {
        mainActivityInterface.getTempSong().setPresentationorder(getPresoOrder());
        mainActivityInterface.updateFragment(fragName,callingFragment,null);
    }

    public String getPresoOrder() {
        // Return a string representaiton of the sections
        // Section names with spaces need to be encoded in { } with spaces replaced wuth
        StringBuilder stringBuilder = new StringBuilder();
        for (String item:currentOrder) {
            item = item.replace(" ","_");
            stringBuilder.append(item).append(" ");
        }
        return stringBuilder.toString().trim();
    }

    public void reset() {
        int size = currentOrder.size();
        currentOrder.clear();
        notifyItemRangeRemoved(0,size);
        updateValue();
    }
}

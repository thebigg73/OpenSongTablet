package com.garethevans.church.opensongtablet.interfaces;

import com.garethevans.church.opensongtablet.setmenu.SetListItemViewHolder;

public interface SetItemTouchInterface {
    void onItemMoved(int fromPosition, int toPosition);
    void onItemClicked(MainActivityInterface mainActivityInterface, int position);
    void onRowSelected(SetListItemViewHolder myViewHolder);
}

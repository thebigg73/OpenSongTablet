package com.garethevans.church.opensongtablet.interfaces;

public interface MidiItemTouchInterface {
    void onItemMoved(int fromPosition, int toPosition);
    void onItemSwiped(int fromPosition);
    void onItemClicked(MainActivityInterface mainActivityInterface, int position);
    void onContentChanged(int position);
}

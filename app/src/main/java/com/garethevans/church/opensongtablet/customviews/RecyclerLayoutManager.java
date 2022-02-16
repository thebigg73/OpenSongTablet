package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerLayoutManager extends LinearLayoutManager {

    // map of child adapter position to its height.
    private ArrayList<Integer> childSizes = new ArrayList<>();
    private final String TAG = "RecyclerManager";
    private int size;
    private int screenSize;

    public RecyclerLayoutManager(Context context) {
        super(context);
    }

    public void setSizes(ArrayList<Float> floatSizes, int screenSize) {
        float total = 0;
        childSizes = new ArrayList<>();
        for (float val:floatSizes) {
            total += val;
            childSizes.add((int)val);
        }
        size = (int)total;
        this.screenSize = screenSize;
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return size;
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return screenSize;
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        if (getChildCount()==0) {
            return 0;
        } else {
            View firstChild = getChildAt(0);
            if (firstChild!=null) {
                int firstChildPosition = getPosition(firstChild);
                int scrolledY = (int)-firstChild.getY();
                for (int i=0;i<firstChildPosition;i++) {
                    scrolledY += childSizes.get(i);
                }
                return scrolledY;
            }
        }
        return 0;
    }
}

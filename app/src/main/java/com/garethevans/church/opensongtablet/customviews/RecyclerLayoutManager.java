package com.garethevans.church.opensongtablet.customviews;

import android.content.Context;
import android.graphics.PointF;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerLayoutManager extends LinearLayoutManager {

    // map of child adapter position to its height.
    private ArrayList<Integer> childHSizes = new ArrayList<>(), childVSizes = new ArrayList<>();
    private int hSize;
    private int vSize;
    private int vScreenSize, hScreenSize;
    @SuppressWarnings({"unused","FieldCanBeLocal"})
    private final String TAG = "RecyclerLayoutMan";

    public RecyclerLayoutManager(Context context) {
        super(context);
    }

    public void setSizes(ArrayList<Float> floatHSizes, ArrayList<Float> floatVSizes,
                         int hScreenSize, int vScreenSize) {
        float vTotal = 0;
        float hTotal = 0;
        childHSizes = new ArrayList<>();
        childVSizes = new ArrayList<>();
        for (int x=0; x<floatVSizes.size(); x++) {
            float hVal = floatHSizes.get(x);
            float vVal = floatVSizes.get(x);
            hTotal += hVal;
            vTotal += vVal;
            childHSizes.add((int)hVal);
            childVSizes.add((int)vVal);
        }
        vSize = (int)vTotal;
        hSize = (int)hTotal;

        this.vScreenSize = vScreenSize;
        this.hScreenSize = hScreenSize;
    }

    @Override
    public int computeHorizontalScrollRange(RecyclerView.State state) {
        return hSize;
    }

    @Override
    public int computeVerticalScrollRange(RecyclerView.State state) {
        return vSize;
    }

    @Override
    public int computeHorizontalScrollExtent(RecyclerView.State state) {
        return hScreenSize;
    }

    @Override
    public int computeVerticalScrollExtent(RecyclerView.State state) {
        return vScreenSize;
    }

    @Override
    public int computeVerticalScrollOffset(RecyclerView.State state) {
        int scrolledY;
        if (getChildCount()==0) {
            return 0;
        } else {
            View firstChild = getChildAt(0);
            if (firstChild!=null) {
                int firstChildPosition = getPosition(firstChild);
                scrolledY = (int)-firstChild.getY();
                for (int i=0;i<firstChildPosition;i++) {
                    if (i<childVSizes.size()) {
                        scrolledY += childVSizes.get(i);
                    }
                }
                return scrolledY;
            }
        }
        return 0;
    }

    @Override
    public int computeHorizontalScrollOffset(RecyclerView.State state) {
        if (getChildCount()==0) {
            return 0;
        } else {
            // To work out the hscroll, we get the first visible position scrollX
            // This will either be 0 or negative (indicating not fully on screen)
            // Get the widths up to this point and subtract what isn't visible

            View firstChild = getChildAt(0);
            if (firstChild!=null) {
                int firstChildPosition = getPosition(firstChild);
                int scrolledX = (int) -firstChild.getX();
                for (int i=0;i<firstChildPosition;i++) {
                    if (i<childHSizes.size()) {
                        scrolledX += childHSizes.get(i);
                    }
                }
                return scrolledX;
            }
        }
        return 0;
    }

    public ArrayList<Integer> getChildVSizes() {
        return childVSizes;
    }

    public ArrayList<Integer> getChildHSizes() {
        return childHSizes;
    }

    public int getScrollYMax() {
        return vSize-vScreenSize-4;
    }

    public int getScrollXMax() {
        return hSize - hScreenSize;
    }

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        return super.computeScrollVectorForPosition(targetPosition);
    }

}

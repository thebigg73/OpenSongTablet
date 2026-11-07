package com.garethevans.church.opensongtablet.stickynotes;

import android.annotation.SuppressLint;

public class StickyNote {
    private int x;
    private int y;
    private String override; // "sticky_on", "sticky_off", or null/empty
    private int color;       // 1 to 4
    private String text;
    private int index;   // Runtime index in your PerformanceFragment's list

    public StickyNote(int index, int x, int y, String override, int color, String text) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.override = override;
        this.color = color;
        this.text = text;
    }

    // Getters and Setters
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public String getOverride() { return override; }
    public void setOverride(String override) { this.override = override; }

    public boolean overrideOn() {
        return override!=null && override.equals("sticky_on");
    }
    public boolean overrideOff() {
        return override!=null && override.equals("sticky_off");
    }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getIndex() { return index; }
    public void setIndex(int index) {
        this.index = index;
    }

    // NOTE: Do NOT include listIndex in your serialize() method,
    // because array indices can change if sticky notes are added/deleted.

    /**
     * Serializes this single sticky note back into the text format.
     */
    @SuppressLint("DefaultLocale")
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[x:%d, y:%d", x, y));
        if (override != null && !override.isEmpty()) {
            sb.append(", override:").append(override);
        }
        sb.append(", color:").append(color);
        sb.append("]\n");
        sb.append(text != null ? text : "");
        return sb.toString();
    }
}
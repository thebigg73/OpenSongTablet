package com.garethevans.church.opensongtablet.setprocessing;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class SetSlideGroupObject {

    // Used for all objects
    @Nullable private String name = null;                       // An attribute in <slide_group...>
    @Nullable private String type = null;                       // An attribute in <slide_group...>

    // Used for song objects
    @Nullable private String path = null;                       // An attribute in <slide_group...>
    @Nullable private String prefKey = null;                    // An attribute in <slide_group...>

    // Used for custom objects
    @Nullable private String print = null;                      // An attribute in <slide_group...>
    @Nullable private String seconds = null;                    // An attribute in <slide_group...>
    @Nullable private String loop = null;                       // An attribute in <slide_group...>
    @Nullable private String transition = null;                 // An attribute in <slide_group...>
    @Nullable private String title = null;                      // Text in <title>...</title>
    @Nullable private String subtitle = null;                   // Text in <subtitle>...</subtitle>
    @Nullable private String notes = null;                      // Text in <notes>...</notes>
    @Nullable private ArrayList<SetSlideObject> slides = null;  // An array found in <slides>...</slides>

    @Nullable public String getName() {
        return name;
    }
    @Nullable public String getType() {
        return type;
    }
    @Nullable public String getPath() {
        return path;
    }
    @Nullable public String getPrefKey() {
        return prefKey;
    }
    @Nullable public String getPrint() {
        return print;
    }
    @Nullable public String getSeconds() {
        return seconds;
    }
    @Nullable public String getLoop() {
        return loop;
    }
    @Nullable public String getTransition() {
        return transition;
    }
    @Nullable public String getTitle() {
        return title;
    }
    @Nullable public String getSubtitle() {
        return subtitle;
    }
    @Nullable public String getNotes() {
        return notes;
    }
    @Nullable public ArrayList<SetSlideObject> getSlides() {
        return slides;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }
    public void setType(@Nullable String type) {
        this.type = type;
    }
    public void setPath(@Nullable String path) {
        this.path = path;
    }
    public void setPrefKey(@Nullable String prefKey) {
        this.prefKey = prefKey;
    }
    public void setPrint(@Nullable String print) {
        this.print = print;
    }
    public void setSeconds(@Nullable String seconds) {
        this.seconds = seconds;
    }
    public void setLoop(@Nullable String loop) {
        this.loop = loop;
    }
    public void setTransition(@Nullable String transition) {
        this.transition = transition;
    }
    public void setTitle(@Nullable String title) {
        this.title = title;
    }
    public void setSubtitle(@Nullable String subtitle) {
        this.subtitle = subtitle;
    }
    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }
    public void setSlides(@Nullable ArrayList<SetSlideObject> slides) {
        this.slides = slides;
    }
}

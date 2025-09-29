package com.garethevans.church.opensongtablet.appdata;

public class SettingsCategory {
    private final String title;
    private final int iconRes;
    private final int navDestinationId;

    public SettingsCategory(String title, int iconRes, int navDestinationId) {
        this.title = title;
        this.iconRes = iconRes;
        this.navDestinationId = navDestinationId;
    }

    public String getTitle() { return title; }
    public int getIconRes() { return iconRes; }
    public int getNavDestinationId() { return navDestinationId; }
}

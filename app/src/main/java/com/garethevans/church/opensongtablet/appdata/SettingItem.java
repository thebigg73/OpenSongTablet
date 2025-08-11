package com.garethevans.church.opensongtablet.appdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingItem {

    // This is the object for each searchable setting item

    final String title;
    final String description;
    final List<String> keywords;
    final String deeplink;
    final String menulocation;

    // Preprocessed for speed
    final String titleLower;
    final String descriptionLower;
    final List<String> keywordsLower;

    public SettingItem(String title, String description, List<String> keywords, String deeplink, String menulocation) {
        this.title = title;
        this.description = description;
        this.keywords = keywords;
        this.deeplink = deeplink;
        this.menulocation = menulocation;

        this.titleLower = title != null ? title.toLowerCase() : "";
        this.descriptionLower = description != null ? description.toLowerCase() : "";
        if (keywords != null) {
            this.keywordsLower = new ArrayList<>();
            for (String kw : keywords) {
                this.keywordsLower.add(kw.toLowerCase());
            }
        } else {
            this.keywordsLower = Collections.emptyList();
        }
    }
}

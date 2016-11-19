package com.zxiu.lillyscard.entities;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Xiu on 10/10/2016.
 */

public class CardGroup {
    public String name;
    public Map<String, String> displayNames = new HashMap<>();
    public String imageUrl;


    public CardGroup() {

    }
    public CardGroup(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "CardGroup{" +
                "name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }

    public String getDisplayName() {
        return getDisplayName(Locale.getDefault());
    }

    public String getDisplayName(Locale locale) {
        return displayNames.get(locale.getLanguage()) != null ? displayNames.get(locale.getLanguage()) : name;
    }
}

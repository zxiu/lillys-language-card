package com.zxiu.lillyscard.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Xiu on 10/10/2016.
 */

public class CardItem {
    public String name;
    public Map<String, String> displayNames = new HashMap<>();
    public String imageUrl;
    public String audioUrl;
    public List<String> groups = new ArrayList<>();

    public CardItem() {
    }

    public CardItem(String name, String imageUrl, String audioUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.audioUrl = audioUrl;
    }

    public String getDisplayName() {
        return getDisplayName(Locale.getDefault());
    }

    public String getDisplayName(Locale locale) {
        return displayNames.get(locale.getLanguage()) != null ? displayNames.get(locale.getLanguage()) : name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CardItem cardItem = (CardItem) o;

        if (!name.equals(cardItem.name)) return false;
        return imageUrl != null ? imageUrl.equals(cardItem.imageUrl) : cardItem.imageUrl == null;

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CardItem{" +
                "name='" + name + '\'' +
                ", displayNames=" + displayNames +
                '}';
    }

}

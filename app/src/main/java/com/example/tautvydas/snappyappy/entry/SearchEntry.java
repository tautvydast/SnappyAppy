package com.example.tautvydas.snappyappy.entry;

/**
 * Created by Tautvydas on 2016-11-28.
 */

public class SearchEntry {
    private int index;
    private int id;
    private String email;
    private String displayName;

    public SearchEntry(int index, int id, String email, String displayName) {
        this.index = index;
        this.id = id;
        this.email = email;
        this.displayName = displayName;
    }

    public int getIndex() {
        return index;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void decreaseIndexByOne() {
        index--;
    }
}

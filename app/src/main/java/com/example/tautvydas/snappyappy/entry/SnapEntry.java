package com.example.tautvydas.snappyappy.entry;

/**
 * Created by Tautvydas on 2016-12-01.
 */

public class SnapEntry {
    private int index;
    private int id;
    private int user1;
    private int user2;
    private String url;
    private String text;
    private int duration;
    private int status;
    private String friendEmail;
    private String friendDisplayName;

    public SnapEntry(int index, int id, int user1, int user2, String url, String text, int duration, int status, String friendEmail, String friendDisplayName) {
        this.index = index;
        this.id = id;
        this.user1 = user1;
        this.user2 = user2;
        this.url = url;
        this.text = text;
        this.duration = duration;
        this.status = status;
        this.friendEmail = friendEmail;
        this.friendDisplayName = friendDisplayName;
    }

    public int getIndex() {
        return index;
    }

    public int getId() { return id; }

    public int getUser1() {
        return user1;
    }

    public int getUser2() {
        return user2;
    }

    public String getUrl() {
        return url;
    }

    public String getText() {
        return text;
    }

    public int getDuration() {
        return duration;
    }

    public int getStatus() {
        return status;
    }

    public String getFriendEmail() {
        return friendEmail;
    }

    public String getFriendDisplayName() {
        return friendDisplayName;
    }

    public void decreaseIndexByOne() {
        index--;
    }
}

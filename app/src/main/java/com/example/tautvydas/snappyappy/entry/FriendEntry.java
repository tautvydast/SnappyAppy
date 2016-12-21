package com.example.tautvydas.snappyappy.entry;

/**
 * Created by Tautvydas on 2016-11-29.
 */

public class FriendEntry {
    private int index;
    private int userId;
    private String email;
    private String lastSeen;
    private int snapsSent;
    private String displayName;
    private int friendshipId;
    private int user1;
    private int user2;
    private int status;

    public FriendEntry(
            int index,
            int userId,
            String email,
            String lastSeen,
            int snapsSent,
            String displayName,
            int friendshipId,
            int user1,
            int user2,
            int status
    ) {
        this.index = index;
        this.userId = userId;
        this.email = email;
        this.lastSeen = lastSeen;
        this.snapsSent = snapsSent;
        this.displayName = displayName;
        this.friendshipId = friendshipId;
        this.user1 = user1;
        this.user2 = user2;
        this.status = status;
    }

    public int getId() {
        return index;
    }

    public int getUserId() { return userId; }

    public String getEmail() {
        return email;
    }

    public String getLastSeen() {
        return lastSeen;
    }

    public int getSnapsSent() {
        return snapsSent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getFriendshipId() {
        return friendshipId;
    }

    public int getUser1() {
        return user1;
    }

    public int getUser2() {
        return user2;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int newStatus) {
        this.status = newStatus;
    }

    public void decreaseIndexByOne() {
        index--;
    }
}

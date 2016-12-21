package com.example.tautvydas.snappyappy.misc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;

import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Tautvydas on 2016-11-27.
 */

public class Config {
    //public final static String SERVER_URL = "http://10.0.2.2:8080/snappyappy/";
    public final static String SERVER_URL = "http://tautra1.stud.if.ktu.lt/snappyappy/";
    public final static String CREATE_USER_API = "createUser.php";
    public final static String LOGIN_API = "login.php";
    public final static String SEARCH_API = "searchUser.php";
    public final static String INVITE_FRIEND_API = "inviteFriend.php";
    public final static String FRIENDS_LIST_API = "friendList.php";
    public final static String PROCESS_FRIENDSHIP_API = "processFriendship.php";
    public final static String SNAP_UPLOAD_API = "snapUpload.php";
    public final static String PHOTO_API = "photo.php";
    public final static String SNAP_LIST_API = "snapList.php";
    public final static String UPDATE_LOGIN_STATUS_API = "updateLoginStatus.php";
    public final static String UPDATE_SNAP_STATUS_API = "updateSnapStatus.php";
    public final static String UPDATE_USER_API = "updateUser.php";


    public final static String SNAPS_DIRECTORY = "snaps/";
    public static Bitmap tempSnap = null;
    public static String snapFileName = null;
    public static String snapFilePath = null;

    public static int lastActivity = 0;
    public static JSONObject lastSearch;
    public static JSONObject lastFriendList;
    public static boolean friendsListPendingRefresh = false;

    public static JSONObject lastSnapList;
    public static boolean snapListPendingRefresh = false;
    public static String showSnapUrl;
    public static int showSnapId;
    public static int showSnapDuration;
    public static String showSnapMessage;

    public static final String MY_PREFERENCES = "appPrefs";

    public final static String STORAGE_DIR = Environment.getExternalStorageDirectory() + "/snappyappy";

    public static boolean internetConnectionAvailable() {
        InetAddress inetAddress = null;
        try {
            Future<InetAddress> future = Executors.newSingleThreadExecutor().submit(new Callable<InetAddress>() {
                @Override
                public InetAddress call() {
                    try {
                        return InetAddress.getByName("tautra1.stud.if.ktu.lt");
                    } catch (UnknownHostException e) {
                        return null;
                    }
                }
            });
            inetAddress = future.get(1000, TimeUnit.MILLISECONDS);
            future.cancel(true);
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        } catch (TimeoutException e) {
        }
        return inetAddress!=null && !inetAddress.equals("");
    }

    public void clearUserInfo(Context context) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE).edit();
        prefs.clear();
        prefs.commit();
        Config.lastActivity = 0;
        Config.lastSearch = null;
        Config.lastFriendList = null;
        Config.friendsListPendingRefresh = false;
        Config.showSnapUrl = null;
        Config.snapListPendingRefresh = false;
        Config.lastSnapList = null;
    }

    public static String getPrefs(String key, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        return prefs.getString(key, "");
    }

    public static void putPrefs(String key, String value, Context context) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE).edit();
        prefs.putString(key, value);
        prefs.commit();
    }
}

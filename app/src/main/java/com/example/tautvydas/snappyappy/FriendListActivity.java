package com.example.tautvydas.snappyappy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tautvydas.snappyappy.adapter.FriendsAdapter;
import com.example.tautvydas.snappyappy.adapter.SearchAdapter;
import com.example.tautvydas.snappyappy.entry.FriendEntry;
import com.example.tautvydas.snappyappy.entry.SearchEntry;
import com.example.tautvydas.snappyappy.misc.Config;
import com.example.tautvydas.snappyappy.misc.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendListActivity extends AppCompatActivity {

    private ArrayList<FriendEntry> friendsContents;
    private FriendsAdapter friendsAdapter;
    private ListView friendsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_friend_list);
        Config.lastActivity = 1;

        if (Config.lastFriendList == null || Config.friendsListPendingRefresh) {
            Config.friendsListPendingRefresh = false;
            getFriends();
        } else {
            setList(Config.lastFriendList);
        }
    }

    public void startHomeActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void startSnapsActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), SnapsActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void startAddFriendActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void getFriends() {
        if (Config.internetConnectionAvailable()) {

            class FriendsList extends AsyncTask<Void, Void, String> {
                private ProgressDialog pDialog = new ProgressDialog(FriendListActivity.this);
                private String response;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog = new ProgressDialog(FriendListActivity.this);
                    pDialog.setMessage("Getting friends list...");
                    pDialog.setCancelable(false);
                    pDialog.show();
                }

                @Override
                protected void onPostExecute(String s) {
                    if (pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                    try {
                        JSONArray allData = new JSONArray(response);
                        JSONObject data = allData.getJSONObject(0);
                        Log.e("from data", data.getString("success"));
                        if (data.getString("success").equals("1")) {
                            //search successful
                            setList(data);
                        } else {
                            //search failed
                            JSONArray errors = data.getJSONArray("errors");
                            Toast.makeText(getApplicationContext(), errors.getString(0), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                protected String doInBackground(Void... v) {
                    HashMap<String, String> params = new HashMap<>();
                    SharedPreferences prefs = getSharedPreferences(Config.MY_PREFERENCES, Context.MODE_PRIVATE);
                    params.put("userId", prefs.getString("userId", ""));
                    String createUserUrl = Config.SERVER_URL + Config.FRIENDS_LIST_API;
                    HttpHandler httpHandler = new HttpHandler();
                    String jsonString = httpHandler.makeServiceCall(createUserUrl, params);
                    Log.e("Res", "Response from url: " + jsonString);
                    response = jsonString;
                    return "";
                }
            }

            FriendsList friends = new FriendsList();
            friends.execute();
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void setList(JSONObject data) {
        Config.lastFriendList = data;
        friendsContents = new ArrayList<>();
        friendsAdapter = new FriendsAdapter(friendsContents, this);
        friendsListView = (ListView) findViewById(R.id.friendsList);
        friendsListView.setAdapter(friendsAdapter);

        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final FriendEntry entry = friendsContents.get(position);
                final AlertDialog.Builder alertDialog= new AlertDialog.Builder(FriendListActivity.this);
                SharedPreferences prefs = getSharedPreferences(Config.MY_PREFERENCES, Context.MODE_PRIVATE);
                final String myId = prefs.getString("userId", "");

                if (entry.getStatus() == 0) {
                    //nepatvirtintas draugas
                    Integer user2Id = entry.getUser2();
                    if (myId.equals(user2Id.toString())) {
                        //pakviete draugas
                        alertDialog.setTitle("Accept invitation?");
                        alertDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                processFriendship("accept", entry.getFriendshipId(), position);
                                dialog.dismiss();
                            }
                        });
                        alertDialog.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                processFriendship("decline", entry.getFriendshipId(), position);
                                dialog.dismiss();
                            }
                        });
                        alertDialog.setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    } else {
                        alertDialog.setTitle("Cancel request?");
                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                processFriendship("delete", entry.getFriendshipId(), position);
                                dialog.dismiss();
                            }
                        });
                        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                    }
                } else {
                    //jau draugas
                    alertDialog.setTitle("User information");
                    alertDialog.setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialog.setNegativeButton("Unfriend", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            processFriendship("delete", entry.getFriendshipId(), position);
                            dialog.dismiss();
                        }
                    });
                }
                alertDialog.show();
            }
        });

        try {
            JSONArray foundUsersArray = data.getJSONArray("friend_data");
            if (friendsContents != null) {
                friendsContents.clear();
                for (int i = 0; i < foundUsersArray.length(); i++) {
                    JSONObject obj = foundUsersArray.getJSONObject(i);
                    String tempDisplayName = obj.getString("display_name");
                    if (tempDisplayName.equals("") || tempDisplayName.equals("null")) {
                        tempDisplayName = "-";
                    }
                    FriendEntry entry = new FriendEntry(
                            i,
                            Integer.parseInt(obj.getString("id")),
                            obj.getString("email"),
                            obj.getString("last_seen"),
                            Integer.parseInt(obj.getString("snaps_sent")),
                            tempDisplayName,
                            Integer.parseInt(obj.getString("friendship_id")),
                            Integer.parseInt(obj.getString("user1")),
                            Integer.parseInt(obj.getString("user2")),
                            Integer.parseInt(obj.getString("status"))
                            );
                    friendsContents.add(entry);
                }
                friendsAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void processFriendship(String type, int id, final int position) {
        final int idFinal = id;
        final String typeFinal = type;
        final int positionFinal = position;
        if (Config.internetConnectionAvailable()) {

            class Friendship extends AsyncTask<Void, Void, String> {
                private ProgressDialog pDialog = new ProgressDialog(FriendListActivity.this);
                private String response;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog = new ProgressDialog(FriendListActivity.this);
                    pDialog.setMessage("Processing...");
                    pDialog.setCancelable(false);
                    pDialog.show();
                }

                @Override
                protected void onPostExecute(String s) {
                    if (pDialog.isShowing()) {
                        pDialog.dismiss();
                    }
                    try {
                        JSONArray allData = new JSONArray(response);
                        JSONObject data = allData.getJSONObject(0);
                        if (data.getString("success").equals("1")) {
                            //search successful
                            Config.friendsListPendingRefresh = true;
                            if (typeFinal.equals("accept")) {
                                friendsContents.get(positionFinal).setStatus(1);
                                friendsAdapter.notifyDataSetChanged();
                            }
                            if (typeFinal.equals("decline") || typeFinal.equals("delete")) {
                                friendsContents.remove(positionFinal);
                                friendsAdapter.notifyDataSetChanged();
                                friendsAdapter.decreaseByOne(positionFinal);
                            }
                            Toast.makeText(getApplicationContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                        } else {
                            //search failed
                            JSONArray errors = data.getJSONArray("errors");
                            Toast.makeText(getApplicationContext(), errors.getString(0), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                protected String doInBackground(Void... v) {
                    Integer friendshipId = idFinal;
                    HashMap<String, String> params = new HashMap<>();
                    params.put("process_type", typeFinal);
                    params.put("friendship_id", friendshipId.toString());
                    String createUserUrl = Config.SERVER_URL + Config.PROCESS_FRIENDSHIP_API;
                    HttpHandler httpHandler = new HttpHandler();
                    String jsonString = httpHandler.makeServiceCall(createUserUrl, params);
                    Log.e("Res", "Response from url: " + jsonString);
                    response = jsonString;
                    return "";
                }
            }

            Friendship friendship = new Friendship();
            friendship.execute();
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    public void refresh(View view) {
        getFriends();
    }
}

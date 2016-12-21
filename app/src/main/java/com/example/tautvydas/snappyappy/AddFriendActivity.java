package com.example.tautvydas.snappyappy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.example.tautvydas.snappyappy.adapter.SearchAdapter;
import com.example.tautvydas.snappyappy.entry.SearchEntry;
import com.example.tautvydas.snappyappy.misc.Config;
import com.example.tautvydas.snappyappy.misc.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class AddFriendActivity extends AppCompatActivity {

    private ArrayList<SearchEntry> searchContents;
    private SearchAdapter searchAdapter;
    private ListView searchListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_add_friend);
        Config.lastActivity = 2;
        if (Config.lastSearch != null) {
            setList(Config.lastSearch);
        }
    }

    public void startHomeActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    public void startFriendListActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), FriendListActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void startSnapsActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), SnapsActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void doSearch(View view) {
        if (Config.internetConnectionAvailable()) {
            EditText searchBox = (EditText) findViewById(R.id.editText);
            final String searchString = searchBox.getText().toString().trim();

            class Search extends AsyncTask<Void, Void, String> {
                private ProgressDialog pDialog = new ProgressDialog(AddFriendActivity.this);
                private String response;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog = new ProgressDialog(AddFriendActivity.this);
                    pDialog.setMessage("Searching...");
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
                            emptyList();
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
                    params.put("pattern", searchString);
                    Log.e("SEARCH_STRING", searchString);
                    String createUserUrl = Config.SERVER_URL + Config.SEARCH_API;
                    HttpHandler httpHandler = new HttpHandler();
                    String jsonString = httpHandler.makeServiceCall(createUserUrl, params);
                    Log.e("Res", "Response from url: " + jsonString);
                    response = jsonString;
                    return "";
                }
            }

            Search search = new Search();
            search.execute();
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void setList(JSONObject data) {
        Config.lastSearch = data;
        searchContents = new ArrayList<>();
        searchAdapter = new SearchAdapter(searchContents, this);
        searchListView = (ListView) findViewById(R.id.searchList);
        searchListView.setAdapter(searchAdapter);

        searchListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final AdapterView<?> parentFinal = parent;
                final int pos = position;
                final AlertDialog.Builder alertDialog= new AlertDialog.Builder(AddFriendActivity.this);
                alertDialog.setTitle("Add friend?");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendInvite(pos);
                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.show();
            }
        });

        try {
            JSONArray foundUsersArray = data.getJSONArray("search_data");
            if (searchContents != null) {
                searchContents.clear();
                for (int i = 0; i < foundUsersArray.length(); i++) {
                    JSONObject obj = foundUsersArray.getJSONObject(i);
                    String tempDisplayName = obj.getString("display_name");
                    if (tempDisplayName.equals("") || tempDisplayName.equals("null")) {
                        tempDisplayName = "-";
                    }
                    SearchEntry entry = new SearchEntry(i, Integer.parseInt((obj.getString("id"))), obj.getString("email"), tempDisplayName);
                    searchContents.add(entry);
                }
                searchAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void emptyList() {
        searchContents = new ArrayList<>();
        searchAdapter = new SearchAdapter(searchContents, this);
        searchListView = (ListView) findViewById(R.id.searchList);
        searchListView.setAdapter(searchAdapter);
        searchAdapter.notifyDataSetChanged();
    }

    private void sendInvite(int ind) {
        final SearchEntry se = (SearchEntry) searchAdapter.getItem(ind);

        if (Config.internetConnectionAvailable()) {
            class Invite extends AsyncTask<Void, Void, String> {
                private ProgressDialog pDialog = new ProgressDialog(AddFriendActivity.this);
                private String response;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog = new ProgressDialog(AddFriendActivity.this);
                    pDialog.setMessage("Inviting...");
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
                            //invitation successful
                            searchContents.remove(se.getIndex());
                            searchAdapter.notifyDataSetChanged();
                            searchAdapter.decreaseByOne(se.getIndex());
                            Toast.makeText(getBaseContext(), "Friend request sent", Toast.LENGTH_SHORT).show();
                            Config.friendsListPendingRefresh = true;
                        } else {
                            //invitation failed
                            JSONArray errors = data.getJSONArray("errors");
                            Toast.makeText(getBaseContext(), errors.getString(0), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                protected String doInBackground(Void... v) {
                    HashMap<String, String> params = new HashMap<>();
                    SharedPreferences prefs = getSharedPreferences(Config.MY_PREFERENCES, Context.MODE_PRIVATE);
                    Integer friendId = se.getId();
                    params.put("userId", prefs.getString("userId", ""));
                    params.put("friendId", friendId.toString());
                    String createUserUrl = Config.SERVER_URL + Config.INVITE_FRIEND_API;
                    HttpHandler httpHandler = new HttpHandler();
                    String jsonString = httpHandler.makeServiceCall(createUserUrl, params);
                    Log.e("Res", "Response from url: " + jsonString);
                    response = jsonString;
                    return "";
                }
            }

            Invite invite = new Invite();
            invite.execute();
        }
    }
}

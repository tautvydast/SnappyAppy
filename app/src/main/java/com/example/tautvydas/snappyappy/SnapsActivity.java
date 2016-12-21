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
import com.example.tautvydas.snappyappy.adapter.SnapAdapter;
import com.example.tautvydas.snappyappy.entry.SearchEntry;
import com.example.tautvydas.snappyappy.entry.SnapEntry;
import com.example.tautvydas.snappyappy.misc.Config;
import com.example.tautvydas.snappyappy.misc.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SnapsActivity extends AppCompatActivity {

    private ArrayList<SnapEntry> snapContents;
    private SnapAdapter snapAdapter;
    private ListView snapListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_snaps);
        Config.lastActivity = 0;

        if (Config.lastSnapList == null || Config.snapListPendingRefresh) {
            Config.snapListPendingRefresh = false;
            getList();
        } else {
            setList(Config.lastSnapList);
        }
    }

    public void getList() {
        if (Config.internetConnectionAvailable()) {

            class SnapList extends AsyncTask<Void, Void, String> {
                private ProgressDialog pDialog = new ProgressDialog(SnapsActivity.this);
                private String response;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog = new ProgressDialog(SnapsActivity.this);
                    pDialog.setMessage("Getting snaps...");
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
                            emptyList();
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
                    String createUserUrl = Config.SERVER_URL + Config.SNAP_LIST_API;
                    HttpHandler httpHandler = new HttpHandler();
                    String jsonString = httpHandler.makeServiceCall(createUserUrl, params);
                    Log.e("Res", "Response from url: " + jsonString);
                    response = jsonString;
                    return "";
                }
            }

            SnapList snapList = new SnapList();
            snapList.execute();
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    private void setList(JSONObject data) {
        Config.lastSnapList = data;
        snapContents = new ArrayList<>();
        snapAdapter = new SnapAdapter(snapContents, this);
        snapListView = (ListView) findViewById(R.id.snapList);
        snapListView.setAdapter(snapAdapter);

        snapListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Config.snapListPendingRefresh = true;
                Config.showSnapUrl = snapContents.get(position).getUrl();
                Config.showSnapId = snapContents.get(position).getId();
                Config.showSnapDuration = snapContents.get(position).getDuration();
                Config.showSnapMessage = snapContents.get(position).getText();
                Intent intent = new Intent(getApplicationContext(), ShowSnapActivity.class);
                startActivity(intent);
            }
        });

        try {
            JSONArray foundUsersArray = data.getJSONArray("snap_data");
            if (snapContents != null) {
                snapContents.clear();
                for (int i = 0; i < foundUsersArray.length(); i++) {
                    JSONObject obj = foundUsersArray.getJSONObject(i);
                    String tempDisplayName = obj.getString("friend_name");
                    if (tempDisplayName.equals("") || tempDisplayName.equals("null")) {
                        tempDisplayName = "-";
                    }
                    SnapEntry entry = new SnapEntry(
                            i,
                            Integer.parseInt(obj.getString("id")),
                            Integer.parseInt((obj.getString("user1"))),
                            Integer.parseInt((obj.getString("user2"))),
                            obj.getString("url"),
                            obj.getString("text"),
                            Integer.parseInt((obj.getString("duration"))),
                            Integer.parseInt((obj.getString("status"))),
                            obj.getString("friend_email"),
                            tempDisplayName
                    );
                    snapContents.add(entry);
                }
                snapAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void emptyList() {
        snapContents = new ArrayList<>();
        snapAdapter = new SnapAdapter(snapContents, this);
        snapListView = (ListView) findViewById(R.id.snapList);
        snapListView.setAdapter(snapAdapter);
        snapAdapter.notifyDataSetChanged();
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

    public void startAddFriendActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), AddFriendActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void refresh(View view) {
        getList();
    }
}

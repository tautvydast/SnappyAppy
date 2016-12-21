package com.example.tautvydas.snappyappy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.tautvydas.snappyappy.adapter.FriendsAdapter;
import com.example.tautvydas.snappyappy.entry.FriendEntry;
import com.example.tautvydas.snappyappy.misc.Config;
import com.example.tautvydas.snappyappy.misc.HttpHandler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class SendSnapActivity extends AppCompatActivity {

    private ArrayList<FriendEntry> friendsContents;
    private FriendsAdapter friendsAdapter;
    private ListView friendsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_send_snap);

        if (Config.lastFriendList == null || Config.friendsListPendingRefresh) {
            Config.friendsListPendingRefresh = false;
            getFriends();
        } else {
            setList(Config.lastFriendList);
        }
    }

    private void getFriends() {
        if (Config.internetConnectionAvailable()) {

            class FriendsList extends AsyncTask<Void, Void, String> {
                private ProgressDialog pDialog = new ProgressDialog(SendSnapActivity.this);
                private String response;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog = new ProgressDialog(SendSnapActivity.this);
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
        friendsListView = (ListView) findViewById(R.id.snapFriendList);
        friendsListView.setAdapter(friendsAdapter);
        SharedPreferences prefs = getSharedPreferences(Config.MY_PREFERENCES, Context.MODE_PRIVATE);
        final String myId = prefs.getString("userId", "");

        friendsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder alertDialog= new AlertDialog.Builder(SendSnapActivity.this);
                alertDialog.setTitle("Send snap?");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendSnap(position);
                        dialog.dismiss();
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
            JSONArray foundUsersArray = data.getJSONArray("friend_data");
            if (friendsContents != null) {
                friendsContents.clear();
                int cellIndex = 0;
                for (int i = 0; i < foundUsersArray.length(); i++) {
                    JSONObject obj = foundUsersArray.getJSONObject(i);
                    String tempDisplayName = obj.getString("display_name");
                    if (tempDisplayName.equals("") || tempDisplayName.equals("null")) {
                        tempDisplayName = "-";
                    }
                    if (obj.getString("status").equals("1")) {
                        FriendEntry entry = new FriendEntry(
                                cellIndex++,
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
                }
                friendsAdapter.notifyDataSetChanged();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void sendSnap(int id) {
        if (Config.internetConnectionAvailable()) {
            final FriendEntry entry = friendsContents.get(id);
            SharedPreferences prefs = getSharedPreferences(Config.MY_PREFERENCES, Context.MODE_PRIVATE);
            final String myId = prefs.getString("userId", "");
            final Integer friendId = entry.getUserId();
            final String snapDuration = prefs.getString("snap_duration", "");
            EditText messageBox = (EditText) findViewById(R.id.snapText);
            final String message = messageBox.getText().toString();
            final String snapTitle = Config.snapFileName;

            class SendSnap extends AsyncTask<Void, Void, String> {
                private ProgressDialog pDialog = new ProgressDialog(SendSnapActivity.this);
                private String response;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog = new ProgressDialog(SendSnapActivity.this);
                    pDialog.setMessage("Sending...");
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
                            //success
                            new UploadTask().execute();
                            Toast.makeText(getApplicationContext(), data.getString("message"), Toast.LENGTH_SHORT).show();
                        } else {
                            //fail
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

                    params.put("userId", myId);
                    params.put("friendId", friendId.toString());
                    params.put("message", message);
                    params.put("duration", snapDuration);
                    params.put("snapTitle", snapTitle);
                    /*params.put("partsSize", partsSize.toString());

                    Log.e("PARTS SIZE", partsSize.toString());

                    for (int i = 0; i < encodedParts.size(); i++) {
                        params.put("fileString"+i, encodedParts.get(i));
                    }*/

                    String createUserUrl = Config.SERVER_URL + Config.SNAP_UPLOAD_API;
                    HttpHandler httpHandler = new HttpHandler();
                    String jsonString = httpHandler.makeServiceCall(createUserUrl, params);
                    Log.e("Res", "Response from url: " + jsonString);
                    response = jsonString;
                    return "";
                }
            }

            SendSnap snap = new SendSnap();
            snap.execute();
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    class UploadTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog pDialog = new ProgressDialog(SendSnapActivity.this);
        private String response;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            //Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(Void... v) {

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            File image = new File(Config.snapFilePath);
            builder.addBinaryBody("myFile", image);

            HttpEntity reqEntity = builder.build();

            try {
                URL url = new URL(Config.SERVER_URL + Config.PHOTO_API);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setUseCaches(false);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.addRequestProperty("Content-length", reqEntity.getContentLength()+"");
                conn.addRequestProperty(reqEntity.getContentType().getName(), reqEntity.getContentType().getValue());

                OutputStream os = conn.getOutputStream();
                reqEntity.writeTo(conn.getOutputStream());
                os.close();
                conn.connect();

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return readStream(conn.getInputStream());
                }

            } catch (Exception e) {
                //Log.e(TAG, "multipart post error " + e + "(" + urlString + ")");
            }
            return null;
        }

        private String readStream(InputStream in) {
            BufferedReader reader = null;
            StringBuilder builder = new StringBuilder();
            try {
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return builder.toString();
        }

    }

}

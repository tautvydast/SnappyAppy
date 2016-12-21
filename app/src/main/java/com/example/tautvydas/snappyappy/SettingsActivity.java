package com.example.tautvydas.snappyappy;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tautvydas.snappyappy.misc.Config;
import com.example.tautvydas.snappyappy.misc.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    public static final String MY_PREFERENCES = "appPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_settings);

        setValues();
    }

    private void setValues() {
        TextView email = (TextView) findViewById(R.id.emailField);
        EditText editDisplayName = (EditText) findViewById(R.id.editDisplayName);

        email.setText(Config.getPrefs("email", this));
        editDisplayName.setText(Config.getPrefs("display_name", this).equals("null") ? "" : Config.getPrefs("display_name", this));
    }

    public void update(View view) {
        EditText editDisplayName = (EditText) findViewById(R.id.editDisplayName);
        EditText newPwField = (EditText) findViewById(R.id.newPwField);
        EditText newPwRepeatField = (EditText) findViewById(R.id.repeatPwField);

        final String displayName = editDisplayName.getText().toString().trim();
        final String newPw = newPwField.getText().toString().trim();
        final String newPwR = newPwRepeatField.getText().toString().trim();

        if (newPw.equals("")) {
            //update only display name
            new UpdateUser(displayName, "").execute();
        } else {
            if (newPw.equals(newPwR)) {
                //update everything
                new UpdateUser(displayName, newPw).execute();
            } else {
                Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
            }
        }

    }

    class UpdateUser extends AsyncTask<Void, Void, String> {
        private ProgressDialog pDialog = new ProgressDialog(SettingsActivity.this);
        private String response;
        private String dName;
        private String pw;

        public UpdateUser(String dName, String pw) {
            this.dName = dName;
            this.pw = pw;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SettingsActivity.this);
            pDialog.setMessage("Updating...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            if (pDialog.isShowing()) {
                pDialog.dismiss();
            }
            Config.putPrefs("display_name", dName, getApplicationContext());
        }

        @Override
        protected String doInBackground(Void... v) {
            HashMap<String, String> params = new HashMap<>();

            params.put("userId", Config.getPrefs("userId", getApplicationContext()));
            params.put("displayName", dName);
            params.put("newPassword", pw);

            String createUserUrl = Config.SERVER_URL + Config.UPDATE_USER_API;
            HttpHandler httpHandler = new HttpHandler();
            String jsonString = httpHandler.makeServiceCall(createUserUrl, params);
            Log.e("Res", "Response from url: " + jsonString);
            response = jsonString;
            return "";
        }
    }

    public void logout(View view) {
        SharedPreferences.Editor prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE).edit();
        prefs.clear();
        prefs.commit();
        Config.lastActivity = 0;
        Config.lastSearch = null;
        Config.lastFriendList = null;
        Config.friendsListPendingRefresh = false;
        Config.showSnapUrl = null;
        Config.snapListPendingRefresh = false;
        Config.lastSnapList = null;

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        ComponentName cn = intent.getComponent();
        Intent mainIntent = IntentCompat.makeRestartActivityTask(cn);
        startActivity(mainIntent);
        this.finish();
    }
}

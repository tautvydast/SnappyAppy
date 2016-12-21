package com.example.tautvydas.snappyappy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.example.tautvydas.snappyappy.misc.Config;
import com.example.tautvydas.snappyappy.misc.HttpHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    public static final String MY_PREFERENCES = "appPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
    }

    public void loginAction(View view) {
        if (Config.internetConnectionAvailable()) {
            EditText name = (EditText) findViewById(R.id.editText2);
            EditText pass = (EditText) findViewById(R.id.editText3);
            final String email = name.getText().toString().trim();
            final String password = pass.getText().toString().trim();

            class Login extends AsyncTask<Void, Void, String> {
                private ProgressDialog pDialog = new ProgressDialog(LoginActivity.this);
                private String response;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog = new ProgressDialog(LoginActivity.this);
                    pDialog.setMessage("Logging in...");
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
                            //login successful
                            SharedPreferences.Editor prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE).edit();
                            JSONObject userDataArray = data.getJSONObject("user_data");
                            prefs.putString("userId", userDataArray.getString("id"));
                            prefs.putString("email", userDataArray.getString("email"));
                            prefs.putString("reg_time", userDataArray.getString("reg_time"));
                            prefs.putString("last_seen", userDataArray.getString("last_seen"));
                            prefs.putString("snaps_sent", userDataArray.getString("snaps_sent"));
                            prefs.putString("display_name", userDataArray.getString("display_name"));
                            prefs.putString("snap_duration", userDataArray.getString("snap_duration"));
                            prefs.commit();
                            Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_SHORT).show();
                            startMainScreen();
                        } else {
                            //login failed
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
                    params.put("email", email);
                    params.put("password", password);
                    String createUserUrl = Config.SERVER_URL + Config.LOGIN_API;
                    HttpHandler httpHandler = new HttpHandler();
                    String jsonString = httpHandler.makeServiceCall(createUserUrl, params);
                    Log.e("Res", "Response from url: " + jsonString);
                    response = jsonString;
                    return "";
                }
            }

            Login login = new Login();
            login.execute();
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFERENCES, Context.MODE_PRIVATE);
        if (prefs.getString("username", "") == "") {
            super.onBackPressed();
        } else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public void startRegisterActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }

    public void startMainScreen() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        this.finish();
    }
}

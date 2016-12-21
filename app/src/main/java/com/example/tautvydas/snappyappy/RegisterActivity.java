package com.example.tautvydas.snappyappy;

import android.app.ProgressDialog;
import android.content.Intent;
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

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);
    }

    public void doRegistration(View view) {
        if (Config.internetConnectionAvailable()) {
            EditText name = (EditText) findViewById(R.id.regEmailField);
            EditText pass = (EditText) findViewById(R.id.regPass);
            EditText pass2 = (EditText) findViewById(R.id.regPassRepeat);
            final String email = name.getText().toString().trim();
            final String password = pass.getText().toString().trim();
            final String password2 = pass2.getText().toString().trim();

            class CreateUser extends AsyncTask<Void, Void, String> {
                private ProgressDialog pDialog = new ProgressDialog(RegisterActivity.this);
                private String response;

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                    pDialog = new ProgressDialog(RegisterActivity.this);
                    pDialog.setMessage("Registering...");
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
                        if (data.getString("success").equals("0")) {
                            JSONArray errors = data.getJSONArray("errors");
                            Toast.makeText(getApplicationContext(), errors.getString(0), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT).show();
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
                    String createUserUrl = Config.SERVER_URL + Config.CREATE_USER_API;
                    HttpHandler httpHandler = new HttpHandler();
                    String jsonString = httpHandler.makeServiceCall(createUserUrl, params);
                    Log.e("Res", "Response from url: " + jsonString);
                    response = jsonString;
                    return "";
                }
            }

            if (password.equals("") || !password.equals(password2)) {
                Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
            } else {
                CreateUser createUser = new CreateUser();
                createUser.execute();
            }
        } else {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

}

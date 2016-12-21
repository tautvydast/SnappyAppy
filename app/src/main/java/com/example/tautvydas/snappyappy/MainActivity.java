package com.example.tautvydas.snappyappy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.tautvydas.snappyappy.misc.Config;
import com.example.tautvydas.snappyappy.misc.HttpHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int IMAGE_CAPTURE_REQUEST = 1;
    private String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(Config.MY_PREFERENCES, Context.MODE_PRIVATE);
        String username = prefs.getString("email", "");
        if (username == "") {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            this.finish();
        }
        if (!Config.internetConnectionAvailable()) {
            Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor pref = getSharedPreferences(Config.MY_PREFERENCES, Context.MODE_PRIVATE).edit();
            pref.clear();
            pref.commit();
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

        } else {
            UpdateStatus updateStatus = new UpdateStatus();
            updateStatus.execute();
        }
    }

    class UpdateStatus extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {

        }

        @Override
        protected String doInBackground(Void... v) {
            HashMap<String, String> params = new HashMap<>();

            params.put("userId", Config.getPrefs("userId", getApplicationContext()));
            String createUserUrl = Config.SERVER_URL + Config.UPDATE_LOGIN_STATUS_API;
            HttpHandler httpHandler = new HttpHandler();
            String jsonString = httpHandler.makeServiceCall(createUserUrl, params);
            Log.e("Res", "Response from url: " + jsonString);
//            response = jsonString;
            return "";
        }
    }

    public void startFriendsActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), FriendsActivity.class);
        startActivity(intent);
    }

    public void startSettingsActivity(View view) {
        Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        startActivity(intent);
    }

    public void startSnapsActivity(View view) {
        Intent intent;
        switch (Config.lastActivity) {
            case 0:
                intent = new Intent(getApplicationContext(), SnapsActivity.class);
                break;
            case 1:
                intent = new Intent(getApplicationContext(), FriendListActivity.class);
                break;
            case 2:
                intent = new Intent(getApplicationContext(), AddFriendActivity.class);
                break;
            default:
                intent = new Intent(getApplicationContext(), SnapsActivity.class);
        }
        startActivity(intent);
    }

    public void startCameraActivity(View view) {
        /*Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        startActivity(intent);*/
        /*Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        startActivityForResult(intent, 0);*/

        /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, IMAGE_CAPTURE_REQUEST);*/
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, IMAGE_CAPTURE_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        String imageFileName = UUID.randomUUID().toString() + "_" + + System.currentTimeMillis();
        Config.snapFileName = imageFileName;
        File dir = new File(Config.STORAGE_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        Config.snapFilePath = Config.STORAGE_DIR + "/" + imageFileName + ".jpg";
        File image = new File(Config.snapFilePath);
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_CAPTURE_REQUEST && resultCode == RESULT_OK) {

            //Config.tempSnap = BitmapFactory.decodeFile(mCurrentPhotoPath);
//            Config.tempSnap = getCompressedBitmap();
//            Config.tempSnap = codec(BitmapFactory.decodeFile(mCurrentPhotoPath), Bitmap.CompressFormat.JPEG, 10);

//            Bitmap tempBM = getCompressedBitmap();
//            Bitmap.createScaledBitmap(tempBM, tempBM.getWidth()/2, tempBM.getHeight()/2, true);

            //Bitmap bmp = BitmapFactory.decodeFile(mCurrentPhotoPath);
            Bitmap tempBM = BitmapFactory.decodeFile(mCurrentPhotoPath);
            int w = (int) (tempBM.getWidth()*0.75);
            int h = (int) (tempBM.getHeight()*0.75);
            Bitmap bmp = Bitmap.createScaledBitmap(tempBM, w, h, true);
            OutputStream stream = null;
            try {
                Config.snapFileName += "_o";
                Config.snapFilePath = Config.STORAGE_DIR + "/" + Config.snapFileName + ".jpg";
                stream = new FileOutputStream(Config.snapFilePath);
                bmp.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(getApplicationContext(), SendSnapActivity.class);
            startActivity(intent);
        }
    }

}

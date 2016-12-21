package com.example.tautvydas.snappyappy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tautvydas.snappyappy.misc.Config;
import com.example.tautvydas.snappyappy.misc.HttpHandler;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.util.HashMap;

import static android.R.attr.width;
import static com.example.tautvydas.snappyappy.R.attr.height;

public class ShowSnapActivity extends AppCompatActivity {

    private boolean status = true;
    private Toast toaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_show_snap);

        toaster = Toast.makeText(getApplicationContext(), Config.showSnapMessage, Toast.LENGTH_SHORT);

        new DownloadImageTask((ImageView) findViewById(R.id.showSnap))
                .execute(Config.SERVER_URL + Config.SNAPS_DIRECTORY + Config.showSnapUrl + ".jpg");
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                //Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            return mIcon11 == null ? mIcon11 : RotateBitmap(mIcon11, 90);
        }

        private Bitmap RotateBitmap(Bitmap source, float angle) {
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        }

        protected void onPostExecute(Bitmap result) {
            if (result == null) {
                Toast.makeText(getApplicationContext(), "Snap not found", Toast.LENGTH_SHORT).show();
                final Intent mainIntent = new Intent(getApplicationContext(), SnapsActivity.class);
                startActivity(mainIntent);
                ShowSnapActivity.this.finish();
            } else {
//                bmImage.setImageBitmap(result);

                /* adapt the image to the size of the display */
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                Bitmap bmp = Bitmap.createScaledBitmap(result,size.x,size.y,true);

                /* fill the background ImageView with the resized image */
//                ImageView iv_background = (ImageView) findViewById(R.id.iv_background);
                bmImage.setImageBitmap(bmp);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (status) {
                            final Intent mainIntent = new Intent(getApplicationContext(), SnapsActivity.class);
                            startActivity(mainIntent);
                        }
                        ShowSnapActivity.this.finish();
                    }
                }, Config.showSnapDuration * 1000);
                new CountDownTimer(Config.showSnapDuration * 1000 + 1000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        TextView counterTextView = (TextView) findViewById(R.id.counterTextView);
                        counterTextView.setText((millisUntilFinished / 1000) + "");
                        if (!Config.showSnapMessage.equals("")) {
                            toaster.show();
                        }
                    }

                    public void onFinish() {
                        toaster.cancel();
                    }

                }.start();
            }
            new UpdateStatus().execute();
        }
    }

    @Override
    public void onBackPressed() {

    }

    public void tapAction(View view) {
        toaster.cancel();
        status = false;
        final Intent mainIntent = new Intent(getApplicationContext(), SnapsActivity.class);
        startActivity(mainIntent);
        ShowSnapActivity.this.finish();
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

            params.put("snapId", Config.showSnapId+"");
            String createUserUrl = Config.SERVER_URL + Config.UPDATE_SNAP_STATUS_API;
            HttpHandler httpHandler = new HttpHandler();
            String jsonString = httpHandler.makeServiceCall(createUserUrl, params);
            Log.e("Res", "Response from url: " + jsonString);
//            response = jsonString;
            return "";
        }
    }

}

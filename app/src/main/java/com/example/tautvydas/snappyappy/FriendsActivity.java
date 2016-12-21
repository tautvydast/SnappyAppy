package com.example.tautvydas.snappyappy;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TabHost;

public class FriendsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_friends);

        TabHost host = (TabHost)findViewById(R.id.tabHost);
        host.setup();

        //Tab 1
        TabHost.TabSpec spec = host.newTabSpec("snapsTab");
        spec.setContent(R.id.snapsTab);
        spec.setIndicator("Snaps");
        host.addTab(spec);

        //Tab 2
        spec = host.newTabSpec("friendsTab");
        spec.setContent(R.id.friendsTab);
        spec.setIndicator("Friends");
        host.addTab(spec);

        //Tab 3
        spec = host.newTabSpec("newFriendTab");
        spec.setContent(R.id.newFriendTab);
        spec.setIndicator("Add new");
        host.addTab(spec);

    }
}

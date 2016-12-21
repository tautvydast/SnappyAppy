package com.example.tautvydas.snappyappy.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.tautvydas.snappyappy.R;
import com.example.tautvydas.snappyappy.entry.SnapEntry;

import java.util.ArrayList;

/**
 * Created by Tautvydas on 2016-11-29.
 */

public class SnapAdapter extends BaseAdapter {
    private ArrayList<SnapEntry> dataList;
    private Activity activity;

    public SnapAdapter(ArrayList<SnapEntry> list, Activity a) {
        this.dataList = list;
        this.activity = a;
    }

    public int getCount() {
        if (dataList != null) {
            return dataList.size();
        }
        return 0;
    }

    public Object getItem(int index) {
        if (dataList != null) {
            return dataList.get(index);
        }
        return null;
    }

    public void decreaseByOne(int index) {
        if (dataList != null && dataList.size() > 0) {
            for (int i = index; i < dataList.size(); i++) {
                dataList.get(i).decreaseIndexByOne();
            }
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int index, View convertView, ViewGroup viewGroup) {
        View view = convertView;
        if (view == null) {
            LayoutInflater li = LayoutInflater.from(activity);
            view = li.inflate(R.layout.snap_list_layout, null);
        }

        TextView displayName = (TextView) view.findViewById(R.id.listview_display_name_snap);
        //TextView infoText = (TextView) view.findViewById(R.id.listview_info_friend);

        SnapEntry se = dataList.get(index);

        String showName = se.getFriendDisplayName();
        if (showName.equals("-")) {
            showName = se.getFriendEmail();
        }

        String info = "";
        if (se.getStatus() == 0) {
            info = "waiting for approval";
        }

        displayName.setText(showName);
        //infoText.setText(info);

        return view;
    }
}

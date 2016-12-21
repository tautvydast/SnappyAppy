package com.example.tautvydas.snappyappy.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.tautvydas.snappyappy.R;
import com.example.tautvydas.snappyappy.entry.SearchEntry;

import java.util.ArrayList;

/**
 * Created by Tautvydas on 2016-11-28.
 */

public class SearchAdapter extends BaseAdapter {

    private ArrayList<SearchEntry> dataList;
    private Activity activity;

    public SearchAdapter(ArrayList<SearchEntry> list, Activity a) {
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
            view = li.inflate(R.layout.search_list_layout, null);
        }

        TextView displayName = (TextView) view.findViewById(R.id.listview_display_name);
        TextView email = (TextView) view.findViewById(R.id.listview_email);

        SearchEntry se = dataList.get(index);

        displayName.setText(se.getDisplayName());
        email.setText(se.getEmail());

        return view;
    }
}

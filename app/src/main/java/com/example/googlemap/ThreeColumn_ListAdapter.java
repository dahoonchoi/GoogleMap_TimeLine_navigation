package com.example.googlemap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class ThreeColumn_ListAdapter extends ArrayAdapter<Path> {

    private LayoutInflater mInflater;
    private ArrayList<Path> paths;
    private int mViewResourceId;

    public ThreeColumn_ListAdapter(Context context, int textViewResourceId, ArrayList<Path> paths){
        super(context, textViewResourceId,paths);
        this.paths = paths;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = textViewResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parents){
        convertView = mInflater.inflate(mViewResourceId,null);

        Path path = paths.get(position);
        if(path != null){
            TextView num = (TextView) convertView.findViewById(R.id.textFirstName);
            TextView latitude = (TextView) convertView.findViewById(R.id.textLastName);
            TextView longtitude = (TextView) convertView.findViewById(R.id.textFavFood);
            TextView pnum = (TextView) convertView.findViewById(R.id.textpnum);
            if(num != null) {
                num.setText((path.getNUmName()));
            }
            if(latitude != null) {
                latitude.setText((path.getLatName()));
            }
            if(longtitude != null) {
                longtitude.setText((path.getLongName()));
            }
            if(pnum != null) {
                pnum.setText((path.getpnum()));
            }
        }
        return convertView;
    }

}
